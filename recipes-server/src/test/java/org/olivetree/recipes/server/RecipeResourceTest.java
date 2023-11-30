package org.olivetree.recipes.server;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.olivetree.recipes.domain.Recipe;
import org.olivetree.recipes.repository.RecipeRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecipeResourceTest {
    public static final String RECIPES_RESOURCE_PATH = "recipes";
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

    @Nested
    @DisplayName("GET list of recipes should")
    public class GetRecipesEndpointTest {
        @Test
        @DisplayName("return recipes")
        public void shouldReturnExpectedRecipes() {
            when(recipeRepository.getAllRecipes())
                    .thenReturn(getMockedRecipes());

            List<Recipe> recipes = target
                    .path(RECIPES_RESOURCE_PATH)
                    .request(MediaType.APPLICATION_JSON)
                    .get()
                    .readEntity(new GenericType<>() {
                    });

            assertNotNull(recipes);
            assertEquals(3, recipes.size());

            Recipe firstRecipe = recipes.get(0);
            assertEquals(1L, firstRecipe.getId());
            assertEquals("Recipe 1", firstRecipe.getName());
            assertEquals("Recipe 1 description", firstRecipe.getDescription());
            assertEquals(50L, firstRecipe.getDurationInMinutes());
        }
    }

    @Nested
    @DisplayName("GET recipe should")
    public class GetRecipeEndpointTest {
        @Test
        @DisplayName("throw exception if recipe does not exist")
        public void shouldThrowNotFoundExceptionIfRecipeDoesNotExist() {
            when(recipeRepository.getRecipeById(1L))
                    .thenReturn(Optional.empty());

            Response response = target
                    .path(RECIPES_RESOURCE_PATH)
                    .path(String.valueOf(1L))
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            assertEquals(404, response.getStatus());
        }

        @Test
        @DisplayName("return recipe")
        public void shouldReturnExpectedRecipe() {
            Recipe recipe = getRecipe(1L, "Recipe 1", "Recipe 1 description", 50L);

            when(recipeRepository.getRecipeById(1L))
                    .thenReturn(Optional.of(recipe));

            Response response = target
                    .path(RECIPES_RESOURCE_PATH)
                    .path(String.valueOf(1L))
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            assertEquals(200, response.getStatus());

            recipe = response.readEntity(Recipe.class);
            assertEquals("Recipe 1", recipe.getName());
        }
    }

    @Nested
    @DisplayName("PUT recipe should")
    public class PutRecipeEndpointTest {
        @Test
        @DisplayName("throw exception if recipe does not exist")
        public void shouldThrowExceptionIfRecipeDoesNotExist() {
            Recipe recipe = getRecipe(null, "Recipe 1", "Recipe 1 description", 50L);

            when(recipeRepository.getRecipeById(1L))
                    .thenReturn(Optional.empty());

            Response response = target
                    .path(RECIPES_RESOURCE_PATH)
                    .path(String.valueOf(1L))
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.entity(recipe, MediaType.APPLICATION_JSON));

            assertEquals(404, response.getStatus());
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
                , Recipe 1 description, 50
                Recipe 1,, 50
                Recipe 1, Recipe 1 description, 9950
                """)
        @DisplayName("throw exception if recipe does not comply with constraints")
        public void shouldThrowConstraintsExceptionIfRecipeDoesNotComply(String name, String description, Long duration) {
            Recipe recipe = getRecipe(null, name, description, duration);

            when(recipeRepository.getRecipeById(1L))
                    .thenReturn(Optional.of(recipe));

            Response response = target
                    .path(RECIPES_RESOURCE_PATH)
                    .path(String.valueOf(1L))
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.entity(recipe, MediaType.APPLICATION_JSON));

            assertEquals(400, response.getStatus());
        }

        @Test
        @DisplayName("return a 204 status code if recipe was updated")
        public void shouldReturnNoContentStatusCode() {
            Recipe recipe = getRecipe(1L, "Recipe 1", "Recipe 1 description", 50L);

            when(recipeRepository.getRecipeById(1L))
                    .thenReturn(Optional.of(recipe));

            Response response = target
                    .path(RECIPES_RESOURCE_PATH)
                    .path(String.valueOf(1L))
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.entity(recipe, MediaType.APPLICATION_JSON));

            assertEquals(204, response.getStatus());
        }
    }

    @Nested
    @DisplayName("POST recipe should")
    public class PostRecipeEndpointTest {
        @ParameterizedTest
        @CsvSource(textBlock = """
                , Recipe 1 description, 50
                Recipe 1,, 50
                Recipe 1, Recipe 1 description, 9950
                """)
        @DisplayName("throw exception if recipe does not comply with constraints")
        public void shouldThrowConstraintsExceptionIfRecipeDoesNotComply(String name, String description, Long duration) {
            Recipe recipe = getRecipe(null, name, description, duration);

            Response response = target
                    .path(RECIPES_RESOURCE_PATH)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(recipe, MediaType.APPLICATION_JSON));

            assertEquals(400, response.getStatus());
        }
    }

    @Nested
    @DisplayName("DELETE recipe should")
    public class DeleteRecipeEndpointTest {
        @Test
        @DisplayName("throw exception if recipe does not exist")
        public void shouldThrowExceptionIfRecipeDoesNotExist() {
            when(recipeRepository.getRecipeById(1L))
                    .thenReturn(Optional.empty());

            Response response = target
                    .path(RECIPES_RESOURCE_PATH)
                    .path(String.valueOf(1L))
                    .request(MediaType.APPLICATION_JSON)
                    .delete();

            assertEquals(404, response.getStatus());
        }

        @Test
        @DisplayName("return a 204 status code if recipe was deleted")
        public void shouldReturnNoContentStatusCode() {
            Recipe recipe = getRecipe(1L, "Recipe 1", "Recipe 1 description", 50L);

            when(recipeRepository.getRecipeById(1L))
                    .thenReturn(Optional.of(recipe));

            Response response = target
                    .path(RECIPES_RESOURCE_PATH)
                    .path(String.valueOf(1L))
                    .request(MediaType.APPLICATION_JSON)
                    .delete();

            assertEquals(204, response.getStatus());
        }
    }

    private List<Recipe> getMockedRecipes() {
        return List.of(
                getRecipe(3L, "Recipe 3", "Recipe 3 description", 50L),
                getRecipe(1L, "Recipe 1", "Recipe 1 description", 50L),
                getRecipe(2L, "Recipe 2", "Recipe 2 description", 50L)
        );
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