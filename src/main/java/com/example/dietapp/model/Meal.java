package com.example.dietapp.model;

public class Meal {
    private String meal, food, calories;

    public Meal(String meal, String food, String calories) {
        this.meal = meal;
        this.food = food;
        this.calories = calories;
    }

    public String getMeal() { return meal; }
    public String getFood() { return food; }
    public String getCalories() { return calories; }
}