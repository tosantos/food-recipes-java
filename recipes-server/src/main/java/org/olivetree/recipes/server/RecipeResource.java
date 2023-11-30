package org.olivetree.recipes.server;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.olivetree.recipes.domain.Recipe;
import org.olivetree.recipes.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Path("/recipes")
public class RecipeResource {

    private static final Logger LOG = LoggerFactory.getLogger(RecipeResource.class);

    private final RecipeRepository recipeRepository;

    public RecipeResource(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Recipe> getRecipes() {
        return recipeRepository
                .getAllRecipes()
                .stream()
                .sorted(Comparator.comparing(Recipe::getId))
                .toList();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Recipe getRecipe(@PathParam("id") Long id) {
        Optional<Recipe> recipeById = recipeRepository.getRecipeById(id);

        if(recipeById.isEmpty()) {
            throw new NotFoundException();
        }

        return recipeById.get();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRecipe(@PathParam("id") Long id, Recipe recipe) {

        Recipe existingRecipe = getRecipe(id);

        Recipe updatedRecipe = getUpdatedRecipe(existingRecipe, recipe);

        if(!Recipe.isValidRecipe(updatedRecipe)) {
            throw new BadRequestException();
        }

        recipeRepository.updateRecipe(id, updatedRecipe);

        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteRecipe(@PathParam("id") Long id) {

        // Ensure recipe with id exists
        getRecipe(id);

        recipeRepository.deleteRecipe(id);
        return Response.noContent().build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRecipe(Recipe recipe) {
        if(!Recipe.isValidRecipe(recipe)) {
            throw new BadRequestException();
        }

        return Response.ok(recipeRepository.createRecipe(recipe)).build();
    }

    private boolean isNotNullAndNotBlank(String str) {
        return str != null && !str.isBlank();
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
}
