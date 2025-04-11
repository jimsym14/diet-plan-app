package com.example.dietapp.model;

import java.util.List;

public class DietPlan {
    private List<Meal> meals;

    public DietPlan(List<Meal> meals) {
        this.meals = meals;
    }

    public List<Meal> getMeals() { return meals; }
}