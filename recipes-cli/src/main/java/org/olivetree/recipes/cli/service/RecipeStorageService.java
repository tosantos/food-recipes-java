package org.olivetree.recipes.cli.service;

import org.olivetree.recipes.cli.service.exception.RecipeConstraintsException;
import org.olivetree.recipes.cli.service.exception.RecipeNotFoundException;
import org.olivetree.recipes.domain.Recipe;
import org.olivetree.recipes.domain.RecipeSearch;
import org.olivetree.recipes.repository.RecipeRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RecipeStorageService {
    private final RecipeRepository recipeRepository;

    public RecipeStorageService(RecipeRepository repo) {
        this.recipeRepository = repo;
    }

    public Recipe getRecipe(long recipeId) throws RecipeNotFoundException {
        Optional<Recipe> recipeById = recipeRepository.getRecipeById(recipeId);

        if(recipeById.isEmpty()) {
            throw new RecipeNotFoundException();
        }

        return recipeById.get();
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.getAllRecipes()
                .stream()
                .sorted(Comparator.comparing(Recipe::getId))
                .toList();
    }

    public void updateRecipe(Long id, Recipe recipe) throws RecipeNotFoundException, RecipeConstraintsException {
        Recipe existingRecipe = getRecipe(id);

        Recipe updatedRecipe = getUpdatedRecipe(existingRecipe, recipe);

        if(!Recipe.isValidRecipe(updatedRecipe)) {
            throw new RecipeConstraintsException();
        }

        recipeRepository.updateRecipe(id, recipe);
    }

    public void createRecipe(Recipe recipe) throws RecipeConstraintsException {
        if(!Recipe.isValidRecipe(recipe)) {
            throw new RecipeConstraintsException();
        }
        recipeRepository.createRecipe(recipe);
    }

    public void deleteRecipe(Long id) throws RecipeNotFoundException {
        getRecipe(id);

        recipeRepository.deleteRecipe(id);
    }

    public List<Recipe> findRecipes(RecipeSearch recipeSearch) {
        return recipeRepository.findRecipes(recipeSearch);
    }

    private Recipe getUpdatedRecipe(Recipe existingRecipe, Recipe recipe) {
        Recipe r = new Recipe();

        String recipeName = isNotNullAndNotBlank(recipe.getName()) ?
                recipe.getName() :
                existingRecipe.getName();

        String recipeDescription = isNotNullAndNotBlank(recipe.getDescription()) ?
                recipe.getDescription() :
                existingRecipe.getDescription();

        Long durationInMinutes = recipe.getDurationInMinutes() != null ?
                recipe.getDurationInMinutes() :
                existingRecipe.getDurationInMinutes();

        r.setName(recipeName);
        r.setDescription(recipeDescription);
        r.setDurationInMinutes(durationInMinutes);

        return r;
    }

    private boolean isNotNullAndNotBlank(String str) {
        return str != null && !str.isBlank();
    }
}
