package org.olivetree.recipes.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.olivetree.recipes.domain.Recipe;
import org.olivetree.recipes.domain.RecipeSearch;
import org.olivetree.recipes.server.RecipeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.olivetree.recipes.client.RecipesRestCommands.*;

public class RecipesClient {

    private static final Logger LOG = LoggerFactory.getLogger(RecipesClient.class);

    private final Client client;
    private final String RECIPES_URI = RecipeServer.BASE_URI + "/recipes";
    private final String SEARCH_RECIPES_URI = RecipeServer.BASE_URI + "/search/recipe";

    public RecipesClient() {
        client = ClientBuilder.newClient();
    }

    public Recipe get(Long id) {
        return client
                .target(RECIPES_URI)
                .path(String.valueOf(id))
                .request(MediaType.APPLICATION_JSON)
                .get(Recipe.class);
    }

    public List<Recipe> get() {
        Response response = client
                .target(RECIPES_URI)
                .request(MediaType.APPLICATION_JSON)
                .get();

        return response.readEntity(new GenericType<>() {
        });
    }

    public List<Recipe> search(RecipeSearch recipeSearch) {
        try (Response response = client
                .target(SEARCH_RECIPES_URI)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(recipeSearch, MediaType.APPLICATION_JSON))) {

            if(response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                return Collections.emptyList();
            }

            return response.readEntity(new GenericType<>() {});
        }
    }

    public Response post(Recipe recipe) {
        return client.target(RECIPES_URI)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(recipe, MediaType.APPLICATION_JSON));
    }

    public Response put(Recipe recipe) {
        return client.target(RECIPES_URI)
                .path(String.valueOf(recipe.getId()))
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(recipe, MediaType.APPLICATION_JSON));
    }

    public Response delete(Long id) {
        return client.target(RECIPES_URI)
                .path(String.valueOf(id))
                .request()
                .delete();
    }

    private static void displayHelp() {
        System.out.printf("""
                Recipes REST API Client application
                        
                %s - List all recipes
                %s - Searches for recipes
                %s - Add recipe
                %s - Delete recipe
                %s - Updates an existing recipe
                %s - Display this menu
                %s - Exit the application
                %n""", LIST, SEARCH, ADD, DELETE, UPDATE, HELP, QUIT);
    }

    public static void main(String[] args) {

        LOG.info("Recipes REST API Client Application started");
        displayHelp();

        boolean isFinished = false;

        while(!isFinished) {
            var scanner = new Scanner(System.in);

            String line = "";
            while (line.isEmpty()) {
                line = scanner.nextLine().trim();
            }

            RecipesRestCommands cliCommand = RecipesRestCommands.getCommand(line);

            if(cliCommand == null) {
                LOG.error("Command not recognized: " + line);
            } else {
                switch (cliCommand) {
                    case LIST -> listRecipes();
                    case SEARCH -> searchRecipes(scanner);
                    case ADD -> createRecipe(scanner);
                    case DELETE -> deleteRecipe(scanner);
                    case UPDATE -> updateRecipe(scanner);
                    case HELP -> displayHelp();
                    case QUIT -> isFinished = true;
                }
            }
        }
    }

    private static void searchRecipes(Scanner scanner) {
        RecipeSearch recipeSearch = getRecipeSearchFromUser(scanner);

        RecipesClient client = new RecipesClient();
        List<Recipe> recipes = client.search(recipeSearch);

        recipes.forEach(r -> LOG.info(r.toString()));
    }

    private static void listRecipes() {
        RecipesClient client = new RecipesClient();

        List<Recipe> recipes = client.get();
        recipes.forEach(r -> LOG.info(r.toString()));
    }

    private static void createRecipe(Scanner scanner) {
        Recipe recipe = getRecipeFromUser(scanner);

        RecipesClient client = new RecipesClient();
        Response response = client.post(recipe);

        switch (response.getStatusInfo().toEnum()) {
            case OK -> LOG.info("Recipe successfully created");
            case BAD_REQUEST -> LOG.error("Recipe is not valid");
        }
    }

    private static void deleteRecipe(Scanner scanner) {
        long recipeId = getRecipeIdFromUser(scanner);

        RecipesClient client = new RecipesClient();
        Response response = client.delete(recipeId);

        switch (response.getStatusInfo().toEnum()) {
            case NOT_FOUND -> LOG.error("Recipe with id {} does not exist", recipeId);
            case NO_CONTENT -> LOG.info("Recipe with id {} successfully deleted", recipeId);
        }
    }

    private static void updateRecipe(Scanner scanner) {

        long recipeId = getRecipeIdFromUser(scanner);
        Recipe recipe = getRecipeFromUser(scanner);
        recipe.setId(recipeId);

        RecipesClient client = new RecipesClient();
        Response response = client.put(recipe);

        switch (response.getStatusInfo().toEnum()) {
            case NOT_FOUND -> LOG.error("Recipe with id {} does not exist", recipeId);
            case BAD_REQUEST -> LOG.error("Recipe is not valid");
            case NO_CONTENT -> LOG.info("Recipe with id {} successfully updated", recipeId);
        }
    }

    private static Recipe getRecipeFromUser(Scanner scanner) {
        System.out.print("Recipe Name: ");
        String recipeName = scanner.nextLine().trim();

        System.out.print("Recipe Description: ");
        String recipeDescription = scanner.nextLine().trim();

        long recipeDuration = getLong(scanner, "Recipe Duration: ");

        Recipe recipe = new Recipe();
        recipe.setName(recipeName);
        recipe.setDescription(recipeDescription);
        recipe.setDurationInMinutes(recipeDuration);

        return recipe;
    }

    private static long getRecipeIdFromUser(Scanner scanner) {
        return getLong(scanner, "Recipe ID: ");
    }

    private static RecipeSearch getRecipeSearchFromUser(Scanner scanner) {
        long durationFrom = getLong(scanner, "Duration From: ");
        long durationTo = getLong(scanner, "Duration To: ");

        return new RecipeSearch(durationFrom, durationTo);
    }

    private static long getLong(Scanner scanner, String label) {
        long l = -1;

        while(l == -1) {
            System.out.print(label);
            String trim = scanner.nextLine().trim();

            try {
                l = Long.parseLong(trim);
            } catch(NumberFormatException nfe) {
            }
        }

        return l;
    }
}
