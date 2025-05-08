package com.example.dietapp.model;

public class Meal {
    private int id;
    private String name;
    private int servings;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double servingWeight;
    private String imageUrl;
    private boolean vegan;
    private boolean vegetarian;
    private boolean glutenFree;
    private boolean dairyFree;
    private boolean veryHealthy;
    private boolean cheap;
    private boolean sustainable;
    private boolean lowFodmap;
    private String diets;

    public Meal(int id, String name, int servings, Double calories, Double protein, Double carbs, Double fat,
            Double servingWeight, String imageUrl, boolean vegan, boolean vegetarian, boolean glutenFree,
            boolean dairyFree, boolean veryHealthy, boolean cheap, boolean sustainable, boolean lowFodmap,
            String diets) {
        this.id = id;
        this.name = name;
        this.servings = servings;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.servingWeight = servingWeight;
        this.imageUrl = imageUrl;
        this.vegan = vegan;
        this.vegetarian = vegetarian;
        this.glutenFree = glutenFree;
        this.dairyFree = dairyFree;
        this.veryHealthy = veryHealthy;
        this.cheap = cheap;
        this.sustainable = sustainable;
        this.lowFodmap = lowFodmap;
        this.diets = diets;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getServings() {
        return servings;
    }

    public Double getCalories() {
        return calories;
    }

    public Double getProtein() {
        return protein;
    }

    public Double getCarbs() {
        return carbs;
    }

    public Double getFat() {
        return fat;
    }

    public Double getServingWeight() {
        return servingWeight;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isVegan() {
        return vegan;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public boolean isGlutenFree() {
        return glutenFree;
    }

    public boolean isDairyFree() {
        return dairyFree;
    }

    public boolean isVeryHealthy() {
        return veryHealthy;
    }

    public boolean isCheap() {
        return cheap;
    }

    public boolean isSustainable() {
        return sustainable;
    }

    public boolean isLowFodmap() {
        return lowFodmap;
    }

    public String getDiets() {
        return diets;
    }

    // Helper method to check if macro values are valid
    public boolean hasValidMacros() {
        return calories != null && protein != null && carbs != null && fat != null;
    }

    // Convenience method for display
    public String getFormattedNutrition() {
        return String.format("%d kcal | P: %.1fg | C: %.1fg | F: %.1fg",
                calories != null ? calories.intValue() : 0,
                protein != null ? protein : 0,
                carbs != null ? carbs : 0,
                fat != null ? fat : 0);
    }
}