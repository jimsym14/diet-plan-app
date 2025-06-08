package com.example.dietapp;

import com.example.dietapp.model.Meal;
import java.util.List;

public class MealCardGenerator {

    public static String generateAllMealCardsHtml(List<Meal> meals) {
        if (meals == null || meals.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        for (Meal meal : meals) {
            html.append(generateMealCardHtml(meal));
        }

        return html.toString();
    }

    public static String generateMealCardHtml(Meal meal) {
        if (meal == null || meal.getName() == null) {
            return "";
        }

        boolean hasValidMacros = meal.hasValidMacros();

        StringBuilder html = new StringBuilder();
        html.append("<div class='meal-card' data-meal-id='").append(meal.getId()).append("'>");
        html.append("<h3>").append(escapeHtml(meal.getName())).append("</h3>");

        String imageUrl = meal.getImageUrl() != null ? meal.getImageUrl()
                : "https://via.placeholder.com/200x150?text=No+Image";
        html.append("<img class='meal-image' src='").append(imageUrl)
                .append("' onerror=\"this.onerror=null; this.src='https://via.placeholder.com/200x150?text=No+Image';\">");

        html.append("<div class='stats-row'>");
        if (meal.getCalories() != null) {
            html.append("<span class='stats-text'>").append(Math.round(meal.getCalories())).append(" kcal</span>");
        } else {
            html.append("<span class='stats-text'>Θερμίδες Μη Διαθέσιμες</span>");
        }

        if (meal.getServingWeight() != null) {
            html.append("<span class='stats-text'>").append(meal.getServingWeight()).append("g</span>");
        } else {
            html.append("<span class='stats-text'></span>");
        }
        html.append("</div>");

        // Πληροφορίες διατροφής
        html.append("<p class='nutrients ").append(hasValidMacros ? "" : "unavailable").append("'>");
        if (hasValidMacros) {
            html.append("Protein: ").append(String.format("%.1f", meal.getProtein())).append("g | ");
            html.append("Carbs: ").append(String.format("%.1f", meal.getCarbs())).append("g | ");
            html.append("Fat: ").append(String.format("%.1f", meal.getFat())).append("g");
        } else {
            html.append("Δεδομένα διατροφής μη διαθέσιμα");
        }
        html.append("</p>");

        // Tags
        html.append("<div class='tags'>");
        if (meal.isVegan())
            html.append("<div class='tag vegan-tag'>Vegan</div>");
        if (meal.isVegetarian())
            html.append("<div class='tag vegetarian-tag'>Vegetarian</div>");
        if (meal.isGlutenFree())
            html.append("<div class='tag gf-tag'>Gluten Free</div>");
        if (meal.isDairyFree())
            html.append("<div class='tag df-tag'>Dairy Free</div>");
        if (meal.isVeryHealthy())
            html.append("<div class='tag healthy-tag'>Healthy</div>");
        if (meal.isCheap())
            html.append("<div class='tag cheap-tag'>Budget</div>");
        if (meal.isSustainable())
            html.append("<div class='tag eco-tag'>Eco</div>");
        if (meal.isLowFodmap())
            html.append("<div class='tag fodmap-tag'>Low FODMAP</div>");
        html.append("</div>");

        html.append("</div>");
        return html.toString();
    }

    /**
     * Απλό escaping HTML για βασική ασφάλεια
     */
    private static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}