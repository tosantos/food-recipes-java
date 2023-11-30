package org.olivetree.recipes.repository;

import org.olivetree.recipes.domain.Recipe;
import org.olivetree.recipes.domain.RecipeSearch;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository {

    Recipe createRecipe(Recipe recipe);

    List<Recipe> getAllRecipes();

    Optional<Recipe> getRecipeById(Long id);

    void deleteRecipe(Long id);

    void updateRecipe(Long id, Recipe recipe);

    List<Recipe> findRecipes(RecipeSearch recipeSearch);
}
