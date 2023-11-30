package org.olivetree.recipes.domain;

public class Recipe {
    private static final int MAX_RECIPE_DURATION_IN_MINUTES = 1439;

    private Long id;
    private String name;
    private String description;
    private Long durationInMinutes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(Long durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    public static boolean isValidRecipe(Recipe recipe) {
        if(recipe == null || recipe.getName() == null ||
                recipe.getDescription() == null ||
                recipe.getDurationInMinutes() == null) {
            return false;
        }

        Long durationInMinutes = recipe.getDurationInMinutes();
        return durationInMinutes <= MAX_RECIPE_DURATION_IN_MINUTES;
    }

    @Override
    public String toString() {
        return "id=" + id + ", name=" + name + ", description=" + description + ", duration=" + durationInMinutes;
    }
}
