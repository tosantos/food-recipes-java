package org.olivetree.recipes.cli.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.olivetree.recipes.cli.service.exception.RecipeConstraintsException;
import org.olivetree.recipes.cli.service.exception.RecipeNotFoundException;
import org.olivetree.recipes.domain.Recipe;
import org.olivetree.recipes.repository.RecipeRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecipeStorageServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    private RecipeStorageService recipeStorageService;

    @BeforeEach
    public void setUp() {
        recipeStorageService = new RecipeStorageService(recipeRepository);
    }

    @Nested
    @DisplayName("Get recipes should")
    public class GetRecipesTests {
        @Test
        @DisplayName("return recipes")
        public void shouldReturnExpectedRecipes() {
            when(recipeRepository.getAllRecipes())
                    .thenReturn(getMockedRecipes());

            List<Recipe> recipes = recipeStorageService.getAllRecipes();

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
    @DisplayName("Get recipe with id should")
    public class GetRecipeWithIdTests {
        @Test
        @DisplayName("throw exception if recipe does not exist")
        public void shouldThrowNotFoundExceptionIfRecipeDoesNotExist() {
            when(recipeRepository.getRecipeById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(RecipeNotFoundException.class, () -> recipeStorageService.getRecipe(1L));
        }

        @Test
        @DisplayName("return recipe")
        public void shouldReturnExpectedRecipe() {
            Recipe recipe = getRecipe(1L, "Recipe 1", "Recipe 1 description", 50L);

            when(recipeRepository.getRecipeById(1L))
                    .thenReturn(Optional.of(recipe));

            Recipe returnedRecipe = assertDoesNotThrow(() -> recipeStorageService.getRecipe(1L));
            assertEquals("Recipe 1", returnedRecipe.getName());
        }
    }

    @Nested
    @DisplayName("Update recipe should")
    public class UpdateRecipeWithIdTests {
        @Test
        @DisplayName("throw exception if recipe does not exist")
        public void shouldThrowNotFoundExceptionIfRecipeDoesNotExist() {
            when(recipeRepository.getRecipeById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(RecipeNotFoundException.class, () -> recipeStorageService.updateRecipe(1L, getRecipe(null, "Foo", "Bar", 50L)));
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

            assertThrows(RecipeConstraintsException.class, () -> recipeStorageService.updateRecipe(1L, recipe));
        }

        @Test
        @DisplayName("modify the recipe")
        public void shouldModifyRecipe() {
            Recipe recipe = getRecipe(null, "Recipe 1", "Recipe 1 description", 90L);

            when(recipeRepository.getRecipeById(1L))
                    .thenReturn(Optional.of(recipe));

            assertDoesNotThrow(() -> recipeStorageService.updateRecipe(1L, recipe));
        }
    }

    @Nested
    @DisplayName("Create recipe should")
    public class CreateRecipeTests {
        @ParameterizedTest
        @CsvSource(textBlock = """
                , Recipe 1 description, 50
                Recipe 1,, 50
                Recipe 1, Recipe 1 description, 9950
                """)
        @DisplayName("throw exception if recipe does not comply with constraints")
        public void shouldThrowConstraintsExceptionIfRecipeDoesNotComply(String name, String description, Long duration) {
            Recipe recipe = getRecipe(null, name, description, duration);

            assertThrows(RecipeConstraintsException.class, () -> recipeStorageService.createRecipe(recipe));
        }
    }

    @Nested
    @DisplayName("Delete recipe should")
    public class DeleteRecipeTests {
        @Test
        @DisplayName("throw exception if recipe does not exist")
        public void shouldThrowNotFoundExceptionIfRecipeDoesNotExist() {
            when(recipeRepository.getRecipeById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(RecipeNotFoundException.class, () -> recipeStorageService.deleteRecipe(1L));
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