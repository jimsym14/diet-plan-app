package com.example.dietapp.model;

public class User {
    private String name, email, gender, goal, activityLevel, dietaryPreferences, foodAllergies;
    private int age, mealsPerDay;
    private double height, weight;

    public User(String name, String email, int age, double height, double weight,
                String gender, String goal, String activityLevel, String dietaryPreferences,
                String foodAllergies, int mealsPerDay) {

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
    }

    // Getters (μόνο, αν δεν κάνεις updates)
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public double getHeight() { return height; }
    public double getWeight() { return weight; }
    public String getGender() { return gender; }
    public String getGoal() { return goal; }
    public String getActivityLevel() { return activityLevel; }
    public String getDietaryPreferences() { return dietaryPreferences; }
    public String getFoodAllergies() { return foodAllergies; }
    public int getMealsPerDay() { return mealsPerDay; }
}