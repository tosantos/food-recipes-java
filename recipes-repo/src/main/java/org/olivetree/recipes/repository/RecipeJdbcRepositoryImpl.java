package org.olivetree.recipes.repository;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.olivetree.recipes.domain.Recipe;
import org.olivetree.recipes.domain.RecipeSearch;
import org.olivetree.recipes.repository.exception.RepositoryException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RecipeJdbcRepositoryImpl implements RecipeRepository {
    private static final String INSERT_RECIPE = """
        INSERT INTO Recipes(name, description, duration)
        VALUES (?, ?, ?)
    """;

    private static final String UPDATE_RECIPE = """
        UPDATE Recipes
        SET name = ?, description = ?, duration = ?
        WHERE id = ?
    """;
    private static final String GET_RECIPES = "SELECT * FROM Recipes";

    private static final String GET_RECIPE_BY_ID = "SELECT * FROM Recipes WHERE id = ?";

    private static final String SEARCH_RECIPES = "SELECT * FROM Recipes WHERE duration >= ? AND duration <= ?";

    private static final String DELETE_RECIPE = "DELETE FROM Recipes WHERE id = ?";

    private final DataSource dataSource;

    public RecipeJdbcRepositoryImpl() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setServerName("localhost");
        dataSource.setPortNumber(3306);
        dataSource.setDatabaseName("recipes_db");
        dataSource.setUser("root");
        dataSource.setPassword("pass");

        this.dataSource = dataSource;
    }

    @Override
    public Recipe createRecipe(Recipe recipe) {
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(INSERT_RECIPE, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, recipe.getName());
            statement.setString(2, recipe.getDescription());
            statement.setLong(3, recipe.getDurationInMinutes());
            statement.executeUpdate();

            try(ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if(generatedKeys.next()) {
                    recipe.setId(generatedKeys.getLong(1));
                }
            }

            return recipe;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save recipe", e);
        }
    }

    @Override
    public List<Recipe> getAllRecipes() {
        try(Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(GET_RECIPES);

            return getRecipesFromResultSet(rs);

        } catch(SQLException e) {
            throw new RepositoryException("Failed to get recipes", e);
        }
    }

    @Override
    public void deleteRecipe(Long id) {
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(DELETE_RECIPE)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete recipe", e);
        }
    }

    @Override
    public Optional<Recipe> getRecipeById(Long id) {
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(GET_RECIPE_BY_ID)) {
            statement.setLong(1, id);

            ResultSet rs = statement.executeQuery();

            if(rs.next()) {
                return Optional.of(getRecipe(rs));
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to get recipe with id " + id, e);
        }
    }

    @Override
    public void updateRecipe(Long id, Recipe recipe) {
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(UPDATE_RECIPE)) {
            statement.setString(1, recipe.getName());
            statement.setString(2, recipe.getDescription());
            statement.setLong(3, recipe.getDurationInMinutes());
            statement.setLong(4, id);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update recipe with id " + recipe.getId(), e);
        }
    }

    @Override
    public List<Recipe> findRecipes(RecipeSearch recipeSearch) {
        if(recipeSearch.durationFrom() == null || recipeSearch.durationTo() == null) {
            return getAllRecipes();
        }

        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(SEARCH_RECIPES)) {

            statement.setLong(1, recipeSearch.durationFrom());
            statement.setLong(2, recipeSearch.durationTo());

            ResultSet rs = statement.executeQuery();

            return getRecipesFromResultSet(rs);

        } catch (SQLException e) {
            throw new RepositoryException("Failed to search recipes", e);
        }
    }

    private static List<Recipe> getRecipesFromResultSet(ResultSet rs) throws SQLException {
        List<Recipe> recipes = new ArrayList<>();
        while(rs.next()) {
            Recipe recipe = getRecipe(rs);

            recipes.add(recipe);
        }

        return Collections.unmodifiableList(recipes);
    }

    private static Recipe getRecipe(ResultSet rs) throws SQLException {
        Recipe recipe = new Recipe();
        recipe.setId(rs.getLong(1));
        recipe.setName(rs.getString(2));
        recipe.setDescription(rs.getString(3));
        recipe.setDurationInMinutes(rs.getLong(4));
        return recipe;
    }
}
