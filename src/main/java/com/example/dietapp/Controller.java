package com.example.dietapp;

import javafx.scene.web.WebView;

public class Controller {
    private final WebView webView;

    public Controller(WebView webView) {
        this.webView = webView;
    }

    public void generatePlan(double weight, double height, int age, String gender) {
        // Mifflin-St Jeor BMR calculation
        double bmr;
        if (gender.equals("male")) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        }
        // Adjust for activity level (sedentary for simplicity)
        double calories = bmr * 1.2;

        // Sample meal plan
        String mealsJson = "["
                + "{\"meal\":\"Breakfast\",\"food\":\"Oatmeal with Berries\",\"calories\":\"300 kcal\"},"
                + "{\"meal\":\"Lunch\",\"food\":\"Grilled Chicken Salad\",\"calories\":\"400 kcal\"},"
                + "{\"meal\":\"Dinner\",\"food\":\"Baked Salmon with Quinoa\",\"calories\":\"500 kcal\"}"
                + "]";
        // Update table
        webView.getEngine().executeScript("updateTable(" + mealsJson + ")");
    }
}