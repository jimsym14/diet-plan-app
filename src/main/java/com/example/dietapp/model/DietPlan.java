package com.example.dietapp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DietPlan {
    private Map<String, List<Integer>> dailySelections;
    private double dailyTargetCalories; // Αποθήκευση στόχου ημερήσιων θερμίδων

    public DietPlan() {
        this.dailySelections = new HashMap<>();
        // Αρχικοποίηση με κενές λίστες για κάθε μέρα
        String[] days = { "MO", "TU", "WE", "TH", "FR", "SA", "SU" };
        for (String day : days) {
            this.dailySelections.put(day, new ArrayList<>());
        }
        this.dailyTargetCalories = 2000; // Default στόχος
    }

    public void addMealToDay(String day, int mealId) {
        List<Integer> meals = this.dailySelections.getOrDefault(day, new ArrayList<>());
        if (!meals.contains(mealId)) {
            meals.add(mealId);
            this.dailySelections.put(day, meals);
        }
    }

    public void removeMealFromDay(String day, int mealId) {
        List<Integer> meals = this.dailySelections.getOrDefault(day, new ArrayList<>());
        meals.remove(Integer.valueOf(mealId));
        this.dailySelections.put(day, meals);
    }

    public void clearMealsForDay(String day) {
        if (this.dailySelections.containsKey(day)) {
            this.dailySelections.get(day).clear();
        }
    }

    public List<Integer> getMealsForDay(String day) {
        return new ArrayList<>(this.dailySelections.getOrDefault(day, new ArrayList<>()));
    }

    public Map<String, List<Integer>> getAllSelections() {
        return dailySelections;
    }

    public double getDailyTargetCalories() {
        return dailyTargetCalories;
    }

    public void setDailyTargetCalories(double dailyTargetCalories) {
        this.dailyTargetCalories = dailyTargetCalories;
    }
}