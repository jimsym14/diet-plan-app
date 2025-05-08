package com.example.dietapp.model;

public class CalorieCalculator {

    public static double calculateCalories(User user) {
        double bmr;

        // Mifflin-St Jeor Formula
        if (user.getGender().equalsIgnoreCase("male")) {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        } else {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
        }

        // Επίπεδο δραστηριότητας
        double multiplier = switch (user.getActivityLevel().toLowerCase()) {
            case "low" -> 1.2;
            case "moderate" -> 1.55;
            case "high" -> 1.9;
            default -> 1.3;
        };

        double maintenance = bmr * multiplier;

        // Ανάλογα με τον στόχο
        return switch (user.getGoal().toLowerCase()) {
            case "lose" -> maintenance - 400;
            case "gain" -> maintenance + 300;
            default -> maintenance;
        };
    }
}
