package com.example.dietapp;

import com.example.dietapp.model.Meal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service for searching and filtering meals
 * This moves the meal search and filtering logic from JavaScript to Java
 */
public class MealSearchService {

    /**
     * Search for meals by name
     * 
     * @param allMeals List of all available meals
     * @param query    Search query
     * @return List of meals matching the query
     */
    public List<Meal> searchByName(List<Meal> allMeals, String query) {
        if (allMeals == null || query == null || query.trim().isEmpty()) {
            return allMeals;
        }

        String lowercaseQuery = query.toLowerCase().trim();
        return allMeals.stream()
                .filter(meal -> meal.getName() != null && meal.getName().toLowerCase().contains(lowercaseQuery))
                .collect(Collectors.toList());
    }

    /**
     * Filter meals by dietary preferences
     * 
     * @param allMeals     List of all available meals
     * @param isVegan      Filter for vegan meals
     * @param isVegetarian Filter for vegetarian meals
     * @param isGlutenFree Filter for gluten-free meals
     * @param isDairyFree  Filter for dairy-free meals
     * @return Filtered list of meals
     */
    public List<Meal> filterByDietaryPreferences(List<Meal> allMeals,
            boolean isVegan, boolean isVegetarian,
            boolean isGlutenFree, boolean isDairyFree) {

        if (allMeals == null) {
            return new ArrayList<>();
        }

        List<Predicate<Meal>> filters = new ArrayList<>();

        if (isVegan) {
            filters.add(Meal::isVegan);
        }

        if (isVegetarian) {
            filters.add(Meal::isVegetarian);
        }

        if (isGlutenFree) {
            filters.add(Meal::isGlutenFree);
        }

        if (isDairyFree) {
            filters.add(Meal::isDairyFree);
        }

        // If no filters are applied, return all meals
        if (filters.isEmpty()) {
            return allMeals;
        }

        // Combine all filters with AND logic
        Predicate<Meal> combinedFilter = filters.stream()
                .reduce(Predicate::and)
                .orElse(meal -> true);

        return allMeals.stream()
                .filter(combinedFilter)
                .collect(Collectors.toList());
    }

    /**
     * Filter meals by nutrition criteria
     * 
     * @param allMeals          List of all available meals
     * @param maxCalories       Maximum calories (0 for no limit)
     * @param minProtein        Minimum protein (0 for no limit)
     * @param hasValidNutrition Only include meals with valid nutrition info
     * @return Filtered list of meals
     */
    public List<Meal> filterByNutrition(List<Meal> allMeals,
            double maxCalories, double minProtein, boolean hasValidNutrition) {

        if (allMeals == null) {
            return new ArrayList<>();
        }

        List<Predicate<Meal>> filters = new ArrayList<>();

        if (maxCalories > 0) {
            filters.add(meal -> meal.getCalories() != null && meal.getCalories() <= maxCalories);
        }

        if (minProtein > 0) {
            filters.add(meal -> meal.getProtein() != null && meal.getProtein() >= minProtein);
        }

        if (hasValidNutrition) {
            filters.add(Meal::hasValidMacros);
        }

        // If no filters are applied, return all meals
        if (filters.isEmpty()) {
            return allMeals;
        }

        // Combine all filters with AND logic
        Predicate<Meal> combinedFilter = filters.stream()
                .reduce(Predicate::and)
                .orElse(meal -> true);

        return allMeals.stream()
                .filter(combinedFilter)
                .collect(Collectors.toList());
    }

    /**
     * Combined search and filter
     * 
     * @param allMeals         List of all available meals
     * @param query            Search query (can be null or empty)
     * @param dietaryFilters   Map of dietary filters (vegan, vegetarian, etc.)
     * @param nutritionFilters Map of nutrition filters (max calories, min protein,
     *                         etc.)
     * @return List of meals matching all criteria
     */
    public List<Meal> searchAndFilter(List<Meal> allMeals, String query,
            boolean isVegan, boolean isVegetarian, boolean isGlutenFree, boolean isDairyFree,
            double maxCalories, double minProtein, boolean hasValidNutrition) {

        // First filter by search query
        List<Meal> searchResults = searchByName(allMeals, query);

        // Then filter by dietary preferences
        List<Meal> dietaryFiltered = filterByDietaryPreferences(
                searchResults, isVegan, isVegetarian, isGlutenFree, isDairyFree);

        // Finally filter by nutrition
        return filterByNutrition(dietaryFiltered, maxCalories, minProtein, hasValidNutrition);
    }
}