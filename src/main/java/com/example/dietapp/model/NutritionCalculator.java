package com.example.dietapp.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles nutrition calculations for the diet plan.
 * This moves nutrition calculation logic from JavaScript to Java.
 */
public class NutritionCalculator {
    // Default nutrition targets
    public static final double DEFAULT_DAILY_CALORIES = 2000.0;
    public static final double DEFAULT_PROTEIN_GOAL = 120.0;
    public static final double DEFAULT_CARBS_GOAL = 250.0;
    public static final double DEFAULT_FAT_GOAL = 65.0;

    private final Map<String, Double> dailyCalorieTargets;
    private final Map<String, Double> dailyProteinTotals;
    private final Map<String, Double> dailyCarbsTotals;
    private final Map<String, Double> dailyFatTotals;

    /**
     * Constructor initializes the nutrition maps with default values
     */
    public NutritionCalculator() {
        this.dailyCalorieTargets = new HashMap<>();
        this.dailyProteinTotals = new HashMap<>();
        this.dailyCarbsTotals = new HashMap<>();
        this.dailyFatTotals = new HashMap<>();

        // Initialize default values for each day
        String[] days = { "MO", "TU", "WE", "TH", "FR", "SA", "SU" };
        for (String day : days) {
            dailyCalorieTargets.put(day, DEFAULT_DAILY_CALORIES);
            dailyProteinTotals.put(day, 0.0);
            dailyCarbsTotals.put(day, 0.0);
            dailyFatTotals.put(day, 0.0);
        }
    }

    /**
     * Calculates nutrition totals for a specific day based on selected meals
     * 
     * @param day             The day to calculate for
     * @param selectedMealIds List of selected meal IDs for this day
     * @param allMeals        All available meals to choose from
     */
    public void calculateNutritionForDay(String day, List<Integer> selectedMealIds, List<Meal> allMeals) {
        // Reset totals for the day
        dailyProteinTotals.put(day, 0.0);
        dailyCarbsTotals.put(day, 0.0);
        dailyFatTotals.put(day, 0.0);

        // Reset calories to default value (this represents "remaining calories")
        dailyCalorieTargets.put(day, DEFAULT_DAILY_CALORIES);

        double totalCalories = 0.0;
        double totalProtein = 0.0;
        double totalCarbs = 0.0;
        double totalFat = 0.0;

        // Calculate totals from selected meals
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

        // Update nutrition maps with calculated values
        double remainingCalories = DEFAULT_DAILY_CALORIES - totalCalories;
        dailyCalorieTargets.put(day, remainingCalories);
        dailyProteinTotals.put(day, totalProtein);
        dailyCarbsTotals.put(day, totalCarbs);
        dailyFatTotals.put(day, totalFat);
    }

    /**
     * Add a meal to the nutrition totals for a day
     */
    public void addMealNutrition(String day, Meal meal) {
        if (meal == null)
            return;

        // Update protein, carbs, fat
        if (meal.getProtein() != null) {
            double currentProtein = dailyProteinTotals.getOrDefault(day, 0.0);
            dailyProteinTotals.put(day, currentProtein + meal.getProtein());
        }

        if (meal.getCarbs() != null) {
            double currentCarbs = dailyCarbsTotals.getOrDefault(day, 0.0);
            dailyCarbsTotals.put(day, currentCarbs + meal.getCarbs());
        }

        if (meal.getFat() != null) {
            double currentFat = dailyFatTotals.getOrDefault(day, 0.0);
            dailyFatTotals.put(day, currentFat + meal.getFat());
        }

        // Update calories
        if (meal.getCalories() != null) {
            double currentTarget = dailyCalorieTargets.getOrDefault(day, DEFAULT_DAILY_CALORIES);
            dailyCalorieTargets.put(day, Math.max(0, currentTarget - meal.getCalories()));
        }
    }

    /**
     * Remove a meal from the nutrition totals for a day
     */
    public void removeMealNutrition(String day, Meal meal) {
        if (meal == null)
            return;

        // Update protein, carbs, fat
        if (meal.getProtein() != null) {
            double currentProtein = dailyProteinTotals.getOrDefault(day, 0.0);
            dailyProteinTotals.put(day, Math.max(0, currentProtein - meal.getProtein()));
        }

        if (meal.getCarbs() != null) {
            double currentCarbs = dailyCarbsTotals.getOrDefault(day, 0.0);
            dailyCarbsTotals.put(day, Math.max(0, currentCarbs - meal.getCarbs()));
        }

        if (meal.getFat() != null) {
            double currentFat = dailyFatTotals.getOrDefault(day, 0.0);
            dailyFatTotals.put(day, Math.max(0, currentFat - meal.getFat()));
        }

        // Update calories
        if (meal.getCalories() != null) {
            double currentTarget = dailyCalorieTargets.getOrDefault(day, DEFAULT_DAILY_CALORIES);
            dailyCalorieTargets.put(day, currentTarget + meal.getCalories());
        }
    }

    /**
     * Format the calorie display for a day
     */
    public String[] formatCalorieDisplay(String day) {
        double remainingCalories = dailyCalorieTargets.getOrDefault(day, DEFAULT_DAILY_CALORIES);
        String displayValue;
        String displayLabel;
        String cssClass = "";

        if (remainingCalories < 0) {
            // Over budget - format with "Over" and red color
            displayValue = String.valueOf(Math.abs((int) Math.round(remainingCalories)));
            displayLabel = "kcal Over";
            cssClass = "over";
        } else if (remainingCalories <= 100) {
            // Close to budget - format with orange warning color
            displayValue = String.valueOf((int) Math.round(remainingCalories));
            displayLabel = "kcal Remaining";
            cssClass = "warning";
        } else {
            // Under budget - default green color
            displayValue = String.valueOf((int) Math.round(remainingCalories));
            displayLabel = "kcal Remaining";
        }

        return new String[] { displayValue, displayLabel, cssClass };
    }

    /**
     * Format the macronutrient displays for a day
     */
    public String[] formatMacronutrientDisplays(String day) {
        double proteinTotal = dailyProteinTotals.getOrDefault(day, 0.0);
        double carbsTotal = dailyCarbsTotals.getOrDefault(day, 0.0);
        double fatTotal = dailyFatTotals.getOrDefault(day, 0.0);

        String proteinDisplay = String.format("%dg/%dg", Math.round(proteinTotal), (int) DEFAULT_PROTEIN_GOAL);
        String proteinClass = proteinTotal >= DEFAULT_PROTEIN_GOAL ? "met" : "not-met";

        String carbsDisplay = String.format("%dg/%dg", Math.round(carbsTotal), (int) DEFAULT_CARBS_GOAL);
        String carbsClass = carbsTotal >= DEFAULT_CARBS_GOAL ? "met" : "not-met";

        String fatDisplay = String.format("%dg/%dg", Math.round(fatTotal), (int) DEFAULT_FAT_GOAL);
        String fatClass = fatTotal >= DEFAULT_FAT_GOAL ? "met" : "not-met";

        return new String[] {
                proteinDisplay, proteinClass,
                carbsDisplay, carbsClass,
                fatDisplay, fatClass
        };
    }

    /**
     * Find a meal by its ID in a list of meals
     */
    private Meal findMealById(int id, List<Meal> meals) {
        for (Meal meal : meals) {
            if (meal.getId() == id) {
                return meal;
            }
        }
        return null;
    }

    // Getters for each map
    public Map<String, Double> getDailyCalorieTargets() {
        return dailyCalorieTargets;
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