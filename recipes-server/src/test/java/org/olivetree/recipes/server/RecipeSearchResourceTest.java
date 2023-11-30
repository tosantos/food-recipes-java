package org.olivetree.recipes.server;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.olivetree.recipes.domain.Recipe;
import org.olivetree.recipes.domain.RecipeSearch;
import org.olivetree.recipes.repository.RecipeRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecipeSearchResourceTest {
    public static final String SEARCH_RECIPES_RESOURCE_PATH = "search/recipe";
    private HttpServer server;
    private WebTarget target;

    @Mock
    private RecipeRepository recipeRepository;

    @BeforeEach
    public void setUp() {
        // start the test server where we are passing the mocked recipe repository
        server = RecipeServer.createHttpServer(recipeRepository);
        target = ClientBuilder.newClient().target(RecipeServer.BASE_URI);
    }

    @AfterEach
    public void tearDown() {
        server.shutdown();
    }

    @Test
    @DisplayName("POST recipe search should throw not found if search produced no results")
    public void shouldThrowNotFoundIfSearchProducedNoResults() {

        RecipeSearch search = new RecipeSearch(10L, 30L);

        when(recipeRepository.findRecipes(search))
                .thenReturn(Collections.emptyList());

        Response response = target
                .path(SEARCH_RECIPES_RESOURCE_PATH)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(search, MediaType.APPLICATION_JSON));

        assertEquals(404, response.getStatus());

        when(recipeRepository.findRecipes(search))
                .thenReturn(Collections.emptyList());

        response = target
                .path(SEARCH_RECIPES_RESOURCE_PATH)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(search, MediaType.APPLICATION_JSON));

        assertEquals(404, response.getStatus());
    }

    @Test
    @DisplayName("POST recipe search should return ok")
    public void shouldReturnOk() {
        RecipeSearch search = new RecipeSearch(10L, 30L);

        Recipe recipe = getRecipe(1L, "Spaghetti", "How to make Spaghetti", 10L);

        when(recipeRepository.findRecipes(search))
                .thenReturn(List.of(recipe));

        Response response = target
                .path(SEARCH_RECIPES_RESOURCE_PATH)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(search, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());
    }

    private Recipe getRecipe(Long id, String name, String description, Long duration) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setName(name);
        recipe.setDescription(description);
        recipe.setDurationInMinutes(duration);

        return recipe;
    }
}