package com.example.dietapp;

import com.example.dietapp.model.Meal;
import com.google.gson.Gson;
import javafx.scene.web.WebView;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.Comparator;

public class Controller {
    private final WebView webView;
    private static final String DB_URL = "jdbc:sqlite:mealsdb.sqlite";

    public Controller(WebView webView) {
        this.webView = webView;
    }

    public void handleMessage(String message) {
        System.out.println("📨 Received message from JavaScript: " + message);
        // Handle different message types here
    }

    public void showAllMealsPage() {
        try {
            System.out.println("🔍 Loading meals from database...");
            List<Meal> meals = getAllMeals();
            String mealsJson = new Gson().toJson(meals);
            System.out.println("📋 Found " + meals.size() + " meals");

            // Execute the createMealCards function with the meals data
            String script = String.format("createMealCards(%s);", mealsJson);
            webView.getEngine().executeScript(script);
            System.out.println("✅ Meals loaded successfully");

        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Meal> getAllMeals() throws SQLException {
        File dbFile = new File("mealsdb.sqlite");
        System.out.println("💾 Database path: " + dbFile.getAbsolutePath());

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM meals")) {
            return parseMeals(rs);
        }
    }

    private List<Meal> parseMeals(ResultSet rs) throws SQLException {
        List<Meal> meals = new ArrayList<>();
        while (rs.next()) {
            meals.add(new Meal(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("servings"),
                    getDoubleOrNull(rs, "calories"),
                    getDoubleOrNull(rs, "protein"),
                    getDoubleOrNull(rs, "carbs"),
                    getDoubleOrNull(rs, "fat"),
                    getDoubleOrNull(rs, "serving_weight"),
                    rs.getString("image_url"),
                    rs.getBoolean("vegan"),
                    rs.getBoolean("vegetarian"),
                    rs.getBoolean("gluten_free"),
                    rs.getBoolean("dairy_free"),
                    rs.getBoolean("very_healthy"),
                    rs.getBoolean("cheap"),
                    rs.getBoolean("sustainable"),
                    rs.getBoolean("low_fodmap"),
                    rs.getString("diets")));
        }

        // Sort meals: valid macros first, then null values
        meals.sort(Comparator.comparing(Meal::hasValidMacros).reversed());

        return meals;
    }

    private Double getDoubleOrNull(ResultSet rs, String columnName) throws SQLException {
        double value = rs.getDouble(columnName);
        return rs.wasNull() ? null : value;
    }
}