package com.example.dietapp.model;

import com.example.dietapp.DatabaseConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.example.dietapp.DatabaseConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SavefromDatabase {
    public static void saveUser(User user) {
        String sql = """
            INSERT INTO users (name, email, age, height, weight, gender, goal,
            activity_level, dietary_preferences, food_allergies, meals_per_day)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setInt(3, user.getAge());
            pstmt.setDouble(4, user.getHeight());
            pstmt.setDouble(5, user.getWeight());
            pstmt.setString(6, user.getGender());
            pstmt.setString(7, user.getGoal());
            pstmt.setString(8, user.getActivityLevel());
            pstmt.setString(9, user.getDietaryPreferences());
            pstmt.setString(10, user.getFoodAllergies());
            pstmt.setInt(11, user.getMealsPerDay());

            pstmt.executeUpdate();
            System.out.println("✅ Ο χρήστης αποθηκεύτηκε με επιτυχία!");

        } catch (SQLException e) {
            System.out.println("❌ Σφάλμα κατά την αποθήκευση: " + e.getMessage());
        }
    }
    public static List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("age"),
                        rs.getDouble("height"),
                        rs.getDouble("weight"),
                        rs.getString("gender"),
                        rs.getString("goal"),
                        rs.getString("activity_level"),
                        rs.getString("dietary_preferences"),
                        rs.getString("food_allergies"),
                        rs.getInt("meals_per_day")
                );
                users.add(user);
            }

        } catch (SQLException e) {
            System.err.println("❌ Σφάλμα κατά την ανάγνωση χρηστών: " + e.getMessage());
        }

        return users;
    }
    public static void printAllUsers() {
        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("📋 Καταχωρημένοι χρήστες:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("name") + ", Email: " + rs.getString("email"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Σφάλμα κατά την ανάγνωση χρηστών: " + e.getMessage());
        }
    }

}
