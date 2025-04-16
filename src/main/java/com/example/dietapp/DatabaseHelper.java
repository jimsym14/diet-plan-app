package com.example.dietapp;

import com.example.dietapp.model.Meal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:dietapp/mealsdb.sqlite";

    public List<Meal> getMeals() {
        List<Meal> meals = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM meals")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int servings = rs.getInt("servings");
                Double calories = rs.getDouble("calories");
                if (rs.wasNull())
                    calories = null;
                Double protein = rs.getDouble("protein");
                if (rs.wasNull())
                    protein = null;
                Double carbs = rs.getDouble("carbs");
                if (rs.wasNull())
                    carbs = null;
                Double fat = rs.getDouble("fat");
                if (rs.wasNull())
                    fat = null;
                Double servingWeight = rs.getDouble("serving_weight");
                if (rs.wasNull())
                    servingWeight = null;
                String imageUrl = rs.getString("image_url");
                boolean vegan = rs.getBoolean("vegan");
                boolean vegetarian = rs.getBoolean("vegetarian");
                boolean glutenFree = rs.getBoolean("gluten_free");
                boolean dairyFree = rs.getBoolean("dairy_free");
                boolean veryHealthy = rs.getBoolean("very_healthy");
                boolean cheap = rs.getBoolean("cheap");
                boolean sustainable = rs.getBoolean("sustainable");
                boolean lowFodmap = rs.getBoolean("low_fodmap");
                String diets = rs.getString("diets");

                meals.add(new Meal(id, name, servings, calories, protein, carbs, fat,
                        servingWeight, imageUrl, vegan, vegetarian, glutenFree,
                        dairyFree, veryHealthy, cheap, sustainable, lowFodmap, diets));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return meals;
    }
}