package com.example.dietapp.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NutritionCalculator {
    public static final double DEFAULT_DAILY_CALORIES = 2000.0;
    public static final double DEFAULT_PROTEIN_GOAL = 120.0;
    public static final double DEFAULT_CARBS_GOAL = 250.0;
    public static final double DEFAULT_FAT_GOAL = 65.0;

    public double getProteinGoal() {
        return proteinGoal;
    }

    public double getCarbsGoal() {
        return carbsGoal;
    }

    public double getFatGoal() {
        return fatGoal;
    }

    private double proteinGoal = DEFAULT_PROTEIN_GOAL;
    private double carbsGoal = DEFAULT_CARBS_GOAL;
    private double fatGoal = DEFAULT_FAT_GOAL;

    private final Map<String, Double> dailyCalorieTargets;
    private final Map<String, Double> dailyCaloriesConsumed;
    private final Map<String, Double> dailyProteinTotals;
    private final Map<String, Double> dailyCarbsTotals;
    private final Map<String, Double> dailyFatTotals;

    public NutritionCalculator() {
        this.dailyCalorieTargets = new HashMap<>();
        this.dailyCaloriesConsumed = new HashMap<>();
        this.dailyProteinTotals = new HashMap<>();
        this.dailyCarbsTotals = new HashMap<>();
        this.dailyFatTotals = new HashMap<>();

        String[] days = { "MO", "TU", "WE", "TH", "FR", "SA", "SU" };
        for (String day : days) {
            dailyCalorieTargets.put(day, DEFAULT_DAILY_CALORIES);
            dailyCaloriesConsumed.put(day, 0.0);
            dailyProteinTotals.put(day, 0.0);
            dailyCarbsTotals.put(day, 0.0);
            dailyFatTotals.put(day, 0.0);
        }
    }

    public void calculateNutritionForDay(String day, List<Integer> selectedMealIds, List<Meal> allMeals) {
        double totalCalories = 0.0;
        double totalProtein = 0.0;
        double totalCarbs = 0.0;
        double totalFat = 0.0;

        for (Integer mealId : selectedMealIds) {
            Meal meal = findMealById(mealId, allMeals);
            if (meal != null) {
                if (meal.getCalories() != null)
                    totalCalories += meal.getCalories();
                if (meal.getProtein() != null)
                    totalProtein += meal.getProtein();
                if (meal.getCarbs() != null)
                    totalCarbs += meal.getCarbs();
                if (meal.getFat() != null)
                    totalFat += meal.getFat();
            }
        }

        // Ενημερώνει τους χάρτες
        dailyCaloriesConsumed.put(day, totalCalories);
        dailyProteinTotals.put(day, totalProtein);
        dailyCarbsTotals.put(day, totalCarbs);
        dailyFatTotals.put(day, totalFat);
    }

    public void addMealNutrition(String day, Meal meal) {
        if (meal == null)
            return;

        if (meal.getCalories() != null) {
            double current = dailyCaloriesConsumed.getOrDefault(day, 0.0);
            dailyCaloriesConsumed.put(day, current + meal.getCalories());
        }
        if (meal.getProtein() != null) {
            double current = dailyProteinTotals.getOrDefault(day, 0.0);
            dailyProteinTotals.put(day, current + meal.getProtein());
        }
        if (meal.getCarbs() != null) {
            double current = dailyCarbsTotals.getOrDefault(day, 0.0);
            dailyCarbsTotals.put(day, current + meal.getCarbs());
        }
        if (meal.getFat() != null) {
            double current = dailyFatTotals.getOrDefault(day, 0.0);
            dailyFatTotals.put(day, current + meal.getFat());
        }
    }

    public void removeMealNutrition(String day, Meal meal) {
        if (meal == null)
            return;

        if (meal.getCalories() != null) {
            double current = dailyCaloriesConsumed.getOrDefault(day, 0.0);
            dailyCaloriesConsumed.put(day, Math.max(0, current - meal.getCalories()));
        }
        if (meal.getProtein() != null) {
            double current = dailyProteinTotals.getOrDefault(day, 0.0);
            dailyProteinTotals.put(day, Math.max(0, current - meal.getProtein()));
        }
        if (meal.getCarbs() != null) {
            double current = dailyCarbsTotals.getOrDefault(day, 0.0);
            dailyCarbsTotals.put(day, Math.max(0, current - meal.getCarbs()));
        }
        if (meal.getFat() != null) {
            double current = dailyFatTotals.getOrDefault(day, 0.0);
            dailyFatTotals.put(day, Math.max(0, current - meal.getFat()));
        }
    }

    public String[] formatCalorieDisplay(String day) {
        double target = dailyCalorieTargets.getOrDefault(day, DEFAULT_DAILY_CALORIES);
        double consumed = dailyCaloriesConsumed.getOrDefault(day, 0.0);
        double remaining = target - consumed;

        String displayValue;
        String displayLabel;
        String cssClass = "";

        if (remaining < 0) {
            displayValue = String.valueOf(Math.abs((int) Math.round(remaining)));
            displayLabel = "kcal Over";
            cssClass = "over";
        } else if (remaining <= 100) {
            displayValue = String.valueOf((int) Math.round(remaining));
            displayLabel = "kcal Remaining";
            cssClass = "warning";
        } else {
            displayValue = String.valueOf((int) Math.round(remaining));
            displayLabel = "kcal Remaining";
        }

        return new String[] { displayValue, displayLabel, cssClass };
    }

    public String[] formatMacronutrientDisplays(String day) {
        double protein = dailyProteinTotals.getOrDefault(day, 0.0);
        double carbs = dailyCarbsTotals.getOrDefault(day, 0.0);
        double fat = dailyFatTotals.getOrDefault(day, 0.0);

        String proteinDisplay = String.format("%dg/%dg", Math.round(protein), (int) proteinGoal);
        String proteinClass = protein >= proteinGoal ? "met" : "not-met";

        String carbsDisplay = String.format("%dg/%dg", Math.round(carbs), (int) carbsGoal);
        String carbsClass = carbs >= carbsGoal ? "met" : "not-met";

        String fatDisplay = String.format("%dg/%dg", Math.round(fat), (int) fatGoal);
        String fatClass = fat >= fatGoal ? "met" : "not-met";

        return new String[] {
                proteinDisplay, proteinClass,
                carbsDisplay, carbsClass,
                fatDisplay, fatClass
        };
    }

    public void setAllDailyTargets(double calories) {
        for (String day : dailyCalorieTargets.keySet()) {
            dailyCalorieTargets.put(day, calories);
        }
    }

    public void setMacroGoals(double protein, double carbs, double fat) {
        this.proteinGoal = protein;
        this.carbsGoal = carbs;
        this.fatGoal = fat;
    }

    private Meal findMealById(int id, List<Meal> meals) {
        for (Meal meal : meals) {
            if (meal.getId() == id) {
                return meal;
            }
        }
        return null;
    }

    // Getters
    public Map<String, Double> getDailyCalorieTargets() {
        return dailyCalorieTargets;
    }

    public Map<String, Double> getDailyCaloriesConsumed() {
        return dailyCaloriesConsumed;
    }

    public Map<String, Double> getDailyProteinTotals() {
        return dailyProteinTotals;
    }

    public Map<String, Double> getDailyCarbsTotals() {
        return dailyCarbsTotals;
    }

    public Map<String, Double> getDailyFatTotals() {
        return dailyFatTotals;
    }
}