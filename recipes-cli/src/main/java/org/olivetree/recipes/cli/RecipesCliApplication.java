package org.olivetree.recipes.cli;

import org.olivetree.recipes.cli.service.RecipeStorageService;
import org.olivetree.recipes.cli.service.exception.RecipeConstraintsException;
import org.olivetree.recipes.cli.service.exception.RecipeNotFoundException;
import org.olivetree.recipes.domain.Recipe;
import org.olivetree.recipes.domain.RecipeSearch;
import org.olivetree.recipes.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;

import static org.olivetree.recipes.cli.RecipesCliCommands.*;

public class RecipesCliApplication {

    private static final Logger LOG = LoggerFactory.getLogger(RecipesCliApplication.class);

    private static RecipeStorageService recipeStorageService;
    public static void main(String[] args) {
        RecipeRepository recipeRepo = RecipeRepository.openRecipeRepository();

        recipeStorageService = new RecipeStorageService(recipeRepo);

        LOG.info("Recipes CLI Application started");

        displayHelp();

        boolean isFinished = false;

        while(!isFinished) {
            var scanner = new Scanner(System.in);

            String line = "";
            while (line.isEmpty()) {
                line = scanner.nextLine().trim();
            }

            RecipesCliCommands cliCommand = RecipesCliCommands.getCommand(line);

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

    private static void displayHelp() {
        System.out.printf("""
                Recipes CLI application
                        
                %s - List all recipes from DB
                %s - Search recipes in DB
                %s - Add recipe to DB
                %s - Delete recipe from DB
                %s - Updates an existing recipe
                %s - Display this menu
                %s - Exit the CLI
                %n""", LIST, SEARCH, ADD, DELETE, UPDATE, HELP, QUIT);
    }

    private static void updateRecipe(Scanner scanner) {

        long recipeId = getRecipeIdFromUser(scanner);
        Recipe recipe = getRecipeFromUser(scanner);

        try {
            recipeStorageService.updateRecipe(recipeId, recipe);
            LOG.info("Recipe successfully updated");
        } catch (RecipeNotFoundException e) {
            LOG.error("Recipe with id {} does not exist", recipeId);
        } catch (RecipeConstraintsException e) {
            LOG.error("Recipe constraints violation");
        }
    }

    private static void searchRecipes(Scanner scanner) {
        LOG.info("Searching recipes");
        RecipeSearch recipeSearch = getRecipeSearchFromUser(scanner);

        List<Recipe> recipes = recipeStorageService.findRecipes(recipeSearch);
        recipes.forEach(r -> LOG.info(r.toString()));
    }

    private static void listRecipes() {
        LOG.info("Getting all recipes");
        recipeStorageService.getAllRecipes()
                .forEach(r -> LOG.info(r.toString()));
    }

    private static void deleteRecipe(Scanner scanner) {
        long recipeId = getRecipeIdFromUser(scanner);

        try {
            recipeStorageService.deleteRecipe(recipeId);
            LOG.info("Recipe {} successfully deleted", recipeId);
        } catch (RecipeNotFoundException e) {
            LOG.error("Recipe with id {} does not exist", recipeId);
        }
    }

    private static void createRecipe(Scanner scanner) {
        Recipe recipe = getRecipeFromUser(scanner);

        try {
            recipeStorageService.createRecipe(recipe);
            LOG.info("Recipe successfully created");
        } catch (RecipeConstraintsException e) {
            LOG.error("Recipe provided is not valid");
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
