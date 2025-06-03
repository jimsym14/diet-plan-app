package com.example.dietapp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MealService {
    public static void printMeals() {
        String sql = "SELECT * FROM meals";

        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(rs.getString("name") + " - " + rs.getInt("calories") + " kcal");
            }

        } catch (SQLException e) {
            System.out.println(" Σφάλμα στο query: " + e.getMessage());
        }
    }
    public static void printTables() {
        String sql = "SELECT name FROM sqlite_master WHERE type='table'";

        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println(" Πίνακες στη βάση:");
            while (rs.next()) {
                System.out.println("• " + rs.getString("name"));
            }

        } catch (SQLException e) {
            System.out.println(" Σφάλμα στο query: " + e.getMessage());
        }
    }
}


