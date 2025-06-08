package com.example.dietapp.model;

import java.util.List;

public class User {
    private String name, email, gender, goal, activityLevel, dietaryPreferences;
    private List<String> foodAllergies;
    private int age, mealsPerDay;
    private double height, weight, targetCalories;
    private boolean autoSelectMeals;

    public User(String name, String email, int age, double height, double weight,
            String gender, String activityLevel, String goal, String dietaryPreferences,
            List<String> foodAllergies, int mealsPerDay, boolean autoSelectMeals) {

        if (weight < 30 || weight > 200) {
            throw new IllegalArgumentException("Weight must be between 30 and 200 kg");
        }
        if (age < 5 || age > 100) {
            throw new IllegalArgumentException("Age must be between 5 and 100");
        }

        this.name = name;
        this.email = email;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.goal = goal;
        this.activityLevel = activityLevel;
        this.dietaryPreferences = dietaryPreferences;
        this.foodAllergies = foodAllergies;
        this.mealsPerDay = mealsPerDay;
        this.autoSelectMeals = autoSelectMeals;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getFullname() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public double getHeight() {
        return height;
    }

    public double getWeight() {
        return weight;
    }

    public String getGender() {
        return gender;
    }

    public String getGoal() {
        return goal;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public String getDietaryPreferences() {
        return dietaryPreferences;
    }

    public List<String> getFoodAllergies() {
        return foodAllergies;
    }

    public int getMealsPerDay() {
        return mealsPerDay;
    }

    public boolean isAutoSelectMeals() {
        return autoSelectMeals;
    }

    public double getTargetCalories() {
        return targetCalories;
    }

    // Setters
    public void setTargetCalories(double targetCalories) {
        this.targetCalories = targetCalories;
    }

    public void setAutoSelectMeals(boolean autoSelectMeals) {
        this.autoSelectMeals = autoSelectMeals;
    }
}