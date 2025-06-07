package com.example.dietapp;

import com.example.dietapp.model.Meal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MealSearchService {
    public List<Meal> searchByName(List<Meal> allMeals, String query) {
        if (allMeals == null || query == null || query.trim().isEmpty()) {
            return allMeals;
        }

        String lowercaseQuery = query.toLowerCase().trim();
        return allMeals.stream()
                .filter(meal -> meal.getName() != null && meal.getName().toLowerCase().contains(lowercaseQuery))
                .collect(Collectors.toList());
    }

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

        // Αν δεν υπάρχουν φίλτρα, επιστρέφουμε όλα τα γεύματα
        if (filters.isEmpty()) {
            return allMeals;
        }

        // Συνδυασμός όλων των φίλτρων με λογική AND
        Predicate<Meal> combinedFilter = filters.stream()
                .reduce(Predicate::and)
                .orElse(meal -> true);

        return allMeals.stream()
                .filter(combinedFilter)
                .collect(Collectors.toList());
    }

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

        // Αν δεν υπάρχουν φίλτρα, επιστρέφουμε όλα τα γεύματα
        if (filters.isEmpty()) {
            return allMeals;
        }

        // Συνδυάζουμε όλα τα φίλτρα με λογική AND
        Predicate<Meal> combinedFilter = filters.stream()
                .reduce(Predicate::and)
                .orElse(meal -> true);

        return allMeals.stream()
                .filter(combinedFilter)
                .collect(Collectors.toList());
    }

    public List<Meal> searchAndFilter(List<Meal> allMeals, String query,
            boolean isVegan, boolean isVegetarian, boolean isGlutenFree, boolean isDairyFree,
            double maxCalories, double minProtein, boolean hasValidNutrition) {

        List<Meal> searchResults = searchByName(allMeals, query);

        // Φιλτραρισμα με βάση τις διατροφικές προτιμήσεις
        List<Meal> dietaryFiltered = filterByDietaryPreferences(
                searchResults, isVegan, isVegetarian, isGlutenFree, isDairyFree);

        // Φιλτραρισμα με βάση τη διατροφή
        return filterByNutrition(dietaryFiltered, maxCalories, minProtein, hasValidNutrition);
    }
}