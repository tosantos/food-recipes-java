package org.olivetree.recipes.server;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.olivetree.recipes.domain.Recipe;
import org.olivetree.recipes.domain.RecipeSearch;
import org.olivetree.recipes.repository.RecipeRepository;

import java.util.List;

@Path("search/recipe")
public class RecipeSearchResource {
    private final RecipeRepository recipeRepository;

    public RecipeSearchResource(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response searchForRecipes(RecipeSearch recipeSearch) {
        List<Recipe> recipes = recipeRepository.findRecipes(recipeSearch);

        if(recipes == null || recipes.isEmpty()) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }

        return Response.ok().entity(new GenericEntity<>(recipes) {}).build();
    }
}
