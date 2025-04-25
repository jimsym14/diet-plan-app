package com.example.dietapp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DietPlan {
    // Map where Key is the day ("MO", "TU", etc.) and Value is a list of Meal IDs selected for that day
    private Map<String, List<Integer>> dailySelections;
    private double dailyTargetCalories; // Store the target

    public DietPlan() {
        this.dailySelections = new HashMap<>();
        // Initialize with empty lists for each day
        String[] days = {"MO", "TU", "WE", "TH", "FR", "SA", "SU"};
        for (String day : days) {
            this.dailySelections.put(day, new ArrayList<>());
        }
        this.dailyTargetCalories = 2000; // Default target, can be set later
    }

    public void addMealToDay(String day, int mealId) {
        List<Integer> meals = this.dailySelections.getOrDefault(day, new ArrayList<>());
        if (!meals.contains(mealId)) {
            meals.add(mealId);
            this.dailySelections.put(day, meals); // Ensure map is updated
        }
    }

    public void removeMealFromDay(String day, int mealId) {
        List<Integer> meals = this.dailySelections.getOrDefault(day, new ArrayList<>());
        meals.remove(Integer.valueOf(mealId)); // Remove by object, not index
        this.dailySelections.put(day, meals); // Ensure map is updated
    }

    public List<Integer> getMealsForDay(String day) {
        // Return a copy to prevent external modification
        return new ArrayList<>(this.dailySelections.getOrDefault(day, new ArrayList<>()));
    }

    public Map<String, List<Integer>> getAllSelections() {
        // Consider returning a deep copy if external modification is a concern
        return dailySelections;
    }

    public double getDailyTargetCalories() {
        return dailyTargetCalories;
    }

    public void setDailyTargetCalories(double dailyTargetCalories) {
        this.dailyTargetCalories = dailyTargetCalories;
    }

    // You might add methods here to calculate total calories for a day, etc.
    // This would require access to the Meal objects or their calorie data.
}