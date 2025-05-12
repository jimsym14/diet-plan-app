package com.example.dietapp;

import com.example.dietapp.model.DietPlan;
import com.example.dietapp.model.Meal;
import com.example.dietapp.model.NutritionCalculator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.scene.web.WebView;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import com.example.dietapp.model.User;
import com.example.dietapp.model.SavefromDatabase;
import com.example.dietapp.model.CalorieCalculator;

public class Controller {
    private final WebView webView;
    private static final String DB_URL = "jdbc:sqlite:mealsdb.sqlite";
    private final DietPlan dietPlan;
    private String currentDay = "MO"; // Default to Monday
    private List<Meal> allMealsCache; // Cache loaded meals

    // Use the NutritionCalculator for all nutrition calculations
    private final NutritionCalculator nutritionCalculator;

    // Add MealSearchService for handling search and filter operations
    private final MealSearchService mealSearchService;

    // Store active filters
    private boolean filterVegan = false;
    private boolean filterVegetarian = false;
    private boolean filterGlutenFree = false;
    private boolean filterDairyFree = false;
    private double filterMaxCalories = 0; // 0 means no limit
    private double filterMinProtein = 0; // 0 means no minimum
    private boolean filterHasValidNutrition = false;
    private String currentSearchQuery = "";

    public Controller(WebView webView) {
        this.webView = webView;
        this.dietPlan = new DietPlan(); // Initialize the diet plan
        this.nutritionCalculator = new NutritionCalculator(); // Initialize our nutrition calculator
        this.mealSearchService = new MealSearchService(); // Initialize the meal search service
    }

    public void handleMessage(String message) {
        System.out.println("📨 Received message from JavaScript: " + message);
        try {
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
            String action = jsonObject.get("action").getAsString();

            switch (action) {
                case "selectDay":
                    String day = jsonObject.get("day").getAsString();
                    selectDay(day);
                    break;
                case "toggleMealSelection":
                    String mealDay = jsonObject.get("day").getAsString();
                    int mealId = jsonObject.get("mealId").getAsInt();
                    toggleMealSelection(mealDay, mealId);
                    break;
                case "getInitialData":
                    sendInitialData();
                    break;
                case "searchMeals":
                    if (jsonObject.has("query")) {
                        String query = jsonObject.get("query").getAsString();
                        currentSearchQuery = query;
                        applySearchAndFilters();
                    }
                    break;
                case "applyFilters":
                    if (jsonObject.has("filters")) {
                        updateFiltersFromJson(jsonObject.getAsJsonObject("filters"));
                        applySearchAndFilters();
                    }
                    break;
                case "validateForm":
                    if (jsonObject.has("formData")) {
                        JsonObject formData = jsonObject.get("formData").getAsJsonObject();
                        validateUserForm(formData);
                    }
                    break;
                default:
                    System.out.println("Unknown action: " + action);
            }
        } catch (Exception e) {
            System.err.println("❌ Error parsing or handling message: " + message + " | Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update filter values from JSON object
     */
    private void updateFiltersFromJson(JsonObject filters) {
        if (filters.has("vegan")) {
            filterVegan = filters.get("vegan").getAsBoolean();
        }
        if (filters.has("vegetarian")) {
            filterVegetarian = filters.get("vegetarian").getAsBoolean();
        }
        if (filters.has("glutenFree")) {
            filterGlutenFree = filters.get("glutenFree").getAsBoolean();
        }
        if (filters.has("dairyFree")) {
            filterDairyFree = filters.get("dairyFree").getAsBoolean();
        }
        if (filters.has("maxCalories")) {
            filterMaxCalories = filters.get("maxCalories").getAsDouble();
        }
        if (filters.has("minProtein")) {
            filterMinProtein = filters.get("minProtein").getAsDouble();
        }
        if (filters.has("hasValidNutrition")) {
            filterHasValidNutrition = filters.get("hasValidNutrition").getAsBoolean();
        }

        System.out.println("🔍 Filters updated: " +
                "Vegan: " + filterVegan +
                ", Vegetarian: " + filterVegetarian +
                ", GF: " + filterGlutenFree +
                ", DF: " + filterDairyFree +
                ", Max Cal: " + filterMaxCalories +
                ", Min Protein: " + filterMinProtein +
                ", Valid Nutrition: " + filterHasValidNutrition);
    }

    private void selectDay(String day) {
        this.currentDay = day;
        System.out.println("🗓️ Day selected: " + day);
        updateMealCardSelectionUI(day);
        updateNutritionDisplays(day);
    }

    private void toggleMealSelection(String day, int mealId) {
        List<Integer> selectedMeals = dietPlan.getMealsForDay(day);

        if (selectedMeals.contains(mealId)) {
            dietPlan.removeMealFromDay(day, mealId);
            System.out.println("➖ Meal removed: ID " + mealId + " from day " + day);
        } else {
            dietPlan.addMealToDay(day, mealId);
            System.out.println("➕ Meal added: ID " + mealId + " to day " + day);
        }

        // ✅ Πάντα υπολογίζουμε από την αρχή όλη τη διατροφή για τη μέρα
        nutritionCalculator.calculateNutritionForDay(day, dietPlan.getMealsForDay(day), allMealsCache);

        // ✅ Ενημέρωση UI
        updateNutritionDisplays(day);
    }

    private Meal findMealById(int id) {
        if (allMealsCache != null) {
            for (Meal meal : allMealsCache) {
                if (meal.getId() == id) {
                    return meal;
                }
            }
        }
        return null;
    }

    private void updateCalorieDisplay(String day) {
        // Get formatted calorie information from our nutrition calculator
        String[] calorieInfo = nutritionCalculator.formatCalorieDisplay(day);
        String displayValue = calorieInfo[0];
        String displayLabel = calorieInfo[1];
        String cssClass = calorieInfo[2];

        // Send updated calories with formatting to JavaScript
        String script = String.format(
                "const valueDisplay = document.querySelector('.calorie-value');" +
                        "const labelDisplay = document.querySelector('.calorie-label');" +
                        "if (valueDisplay && labelDisplay) {" +
                        "  valueDisplay.textContent = '%s';" +
                        "  labelDisplay.textContent = '%s';" +
                        "  valueDisplay.classList.remove('over', 'warning');" +
                        "  %s" +
                        "}",
                displayValue,
                displayLabel,
                cssClass.isEmpty() ? "" : "valueDisplay.classList.add('" + cssClass + "');");

        Platform.runLater(() -> {
            try {
                webView.getEngine().executeScript(script);
                System.out.println("🔢 Updated calorie display for day " + day);
            } catch (Exception e) {
                System.err.println("❌ Failed to update calorie display: " + e.getMessage());
            }
        });
    }

    // Renamed from showAllMealsPage to loadMealsAndSendInitialData
    public void loadMealsAndSendInitialData() {
        try {
            System.out.println("🔍 Loading meals from database...");
            if (allMealsCache == null) { // Load only if cache is empty
                allMealsCache = getAllMeals();
            }
            System.out.println("📋 Found " + allMealsCache.size() + " meals");

            // Calculate initial nutrition values using our calculator
            nutritionCalculator.calculateNutritionForDay(currentDay, dietPlan.getMealsForDay(currentDay),
                    allMealsCache);

            sendInitialData(); // Send meals and initial day's selections

        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendInitialData() {
        if (allMealsCache == null) {
            System.err.println("❌ Meal cache is empty, cannot send initial data.");
            return;
        }

        // Generate meal cards HTML in Java instead of JavaScript
        String mealCardsHtml = MealCardGenerator.generateAllMealCardsHtml(allMealsCache);

        List<Integer> initialSelectedIds = dietPlan.getMealsForDay(currentDay);
        String initialSelectedIdsJson = new Gson().toJson(initialSelectedIds);

        double dailyTarget = dietPlan.getDailyTargetCalories();
        String script = String.format(
                "document.getElementById('mealContainer').innerHTML = `%s`; setupPageWithHtml('%s', %s, %f);",
                mealCardsHtml.replace("`", "\\`"), // Escape backticks in HTML
                currentDay,
                initialSelectedIdsJson,
                dailyTarget
        );

        Platform.runLater(() -> {
            try {
                webView.getEngine().executeScript(script);
                System.out.println("✅ Initial page data sent successfully for day: " + currentDay);

                // Update nutritional displays
                updateNutritionDisplays(currentDay);
            } catch (Exception e) {
                System.err.println("❌ Failed to execute setup script: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Sends the list of selected meal IDs for the given day to JavaScript
    private void updateMealCardSelectionUI(String day) {
        List<Integer> selectedMealIds = dietPlan.getMealsForDay(day);
        String selectedIdsJson = new Gson().toJson(selectedMealIds);
        String script = String.format("updateSelectedCards(%s);", selectedIdsJson);

        Platform.runLater(() -> {
            try {
                webView.getEngine().executeScript(script);
                System.out.println("🔄 Updated card selections for day: " + day);
            } catch (Exception e) {
                System.err.println("❌ Failed to execute updateSelectedCards script: " + e.getMessage());
            }
        });
    }

    // Update all nutrition displays using the NutritionCalculator
    private void updateNutritionDisplays(String day) {
        // First update calories
        updateCalorieDisplay(day);

        // Then update macronutrients
        updateMacronutrientDisplays(day);
    }

    // Update protein, carbs, and fat displays
    private void updateMacronutrientDisplays(String day) {
        // Use the NutritionCalculator to get formatted macro displays
        String[] macroInfo = nutritionCalculator.formatMacronutrientDisplays(day);

        // Extract values from the returned array
        String proteinDisplay = macroInfo[0];
        String proteinClass = macroInfo[1];
        String carbsDisplay = macroInfo[2];
        String carbsClass = macroInfo[3];
        String fatDisplay = macroInfo[4];
        String fatClass = macroInfo[5];

        // Create JavaScript to update all macro displays
        String script = String.format(
                "// Update protein display\n" +
                        "const proteinDisplay = document.querySelector('.macro-pill.protein .macro-value');\n" +
                        "if (proteinDisplay) {\n" +
                        "  proteinDisplay.classList.remove('met', 'not-met');\n" +
                        "  proteinDisplay.textContent = '%s';\n" +
                        "  proteinDisplay.classList.add('%s');\n" +
                        "}\n" +

                        "// Update carbs display\n" +
                        "const carbsDisplay = document.querySelector('.macro-pill.carbs .macro-value');\n" +
                        "if (carbsDisplay) {\n" +
                        "  carbsDisplay.classList.remove('met', 'not-met');\n" +
                        "  carbsDisplay.textContent = '%s';\n" +
                        "  carbsDisplay.classList.add('%s');\n" +
                        "}\n" +

                        "// Update fat display\n" +
                        "const fatDisplay = document.querySelector('.macro-pill.fat .macro-value');\n" +
                        "if (fatDisplay) {\n" +
                        "  fatDisplay.classList.remove('met', 'not-met');\n" +
                        "  fatDisplay.textContent = '%s';\n" +
                        "  fatDisplay.classList.add('%s');\n" +
                        "}",
                proteinDisplay, proteinClass,
                carbsDisplay, carbsClass,
                fatDisplay, fatClass);

        Platform.runLater(() -> {
            try {
                webView.getEngine().executeScript(script);
                System.out.println("📊 Updated macronutrient displays for day " + day);
            } catch (Exception e) {
                System.err.println("❌ Failed to update macronutrient displays: " + e.getMessage());
            }
        });
    }

    // Apply search and filters then update UI
    private void applySearchAndFilters() {
        if (allMealsCache == null) {
            System.err.println("❌ Meal cache is empty, cannot perform search/filter.");
            return;
        }

        // Use MealSearchService to perform search and filtering
        List<Meal> filteredMeals = mealSearchService.searchAndFilter(
                allMealsCache,
                currentSearchQuery,
                filterVegan,
                filterVegetarian,
                filterGlutenFree,
                filterDairyFree,
                filterMaxCalories,
                filterMinProtein,
                filterHasValidNutrition);

        System.out.println("🔍 Search/filter found " + filteredMeals.size() + " meals");

        // Generate HTML for filtered meals
        String filteredMealsHtml = MealCardGenerator.generateAllMealCardsHtml(filteredMeals);

        // Create final variables for the lambda
        final String htmlContent = filteredMealsHtml.replace("`", "\\`"); // Escape backticks
        final String selectedIdsJson = new Gson().toJson(dietPlan.getMealsForDay(currentDay));

        // Update the UI with filtered results
        final String script = String.format(
                "document.getElementById('mealContainer').innerHTML = `%s`; updateSelectedCards(%s);",
                htmlContent,
                selectedIdsJson);

        Platform.runLater(() -> {
            try {
                webView.getEngine().executeScript(script);
                System.out.println(" Updated UI with filtered meals");
            } catch (Exception e) {
                System.err.println(" Failed to update meal cards: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Process form validation from index.html
    private void validateUserForm(JsonObject formData) {
        final boolean[] isValid = { true };
        final String[] errorMessage = { "" };

        try {
            //  Ανάκτηση δεδομένων
            String fullname = formData.has("fullname") ? formData.get("fullname").getAsString() : "";
            String email = formData.has("email") ? formData.get("email").getAsString() : "";
            String weightStr = formData.has("weight") ? formData.get("weight").getAsString() : "";
            String heightStr = formData.has("height") ? formData.get("height").getAsString() : "";
            String ageStr = formData.has("age") ? formData.get("age").getAsString() : "";
            String gender = formData.has("gender") ? formData.get("gender").getAsString() : "";
            String goal = formData.has("goal") ? formData.get("goal").getAsString() : "";
            String activity = formData.has("activityLevel") ? formData.get("activityLevel").getAsString() : "";
            String preferences = formData.has("dietaryPreferences") ? formData.get("dietaryPreferences").getAsString() : "";
            String allergies = formData.has("foodAllergies") ? formData.get("foodAllergies").getAsString() : "";
            int mealsPerDay = formData.has("mealsPerDay") ? formData.get("mealsPerDay").getAsInt() : 3;

            //  Μετατροπή αριθμητικών τιμών
            double weight = Double.parseDouble(weightStr);
            double height = Double.parseDouble(heightStr);
            int age = Integer.parseInt(ageStr);

            //  Δημιουργία χρήστη & υπολογισμός στόχου θερμίδων
            User user = new User(fullname, email, age, height, weight, gender, goal, activity, preferences, allergies, mealsPerDay);
            SavefromDatabase.saveUser(user);

            double targetCalories = CalorieCalculator.calculateCalories(user);
            dietPlan.setDailyTargetCalories(targetCalories);
            nutritionCalculator.setAllDailyTargets(targetCalories);

            System.out.println(" Υπολογισμένες θερμίδες χρήστη: " + targetCalories);
            System.out.println(" Ελεγχος στόχων ανά ημέρα:");
            nutritionCalculator.getDailyCalorieTargets().forEach((day, cal) -> {
                System.out.println("   " + day + " ➜ " + cal + " kcal");
            });

            // ➕ Αν δεν έχουν φορτωθεί γεύματα, τα φέρνουμε
            if (allMealsCache == null) {
                try {
                    allMealsCache = getAllMeals();
                } catch (SQLException ex) {
                    System.err.println("Σφάλμα φόρτωσης meals από DB: " + ex.getMessage());
                }
            }

            // ➕ Υπολογισμός μακροθρεπτικών για την τρέχουσα μέρα
            nutritionCalculator.calculateNutritionForDay(currentDay, dietPlan.getMealsForDay(currentDay), allMealsCache);

            System.out.println(" Ολοκληρώθηκε η καταγραφή & υπολογισμός θερμίδων");

        } catch (Exception e) {
            System.err.println("❌ Σφάλμα κατά την επεξεργασία δεδομένων φόρμας: " + e.getMessage());
            e.printStackTrace();
        }

        // ➕ Επιστροφή αποτελέσματος στη JavaScript
        final String safeErrorMessage = errorMessage[0].replace("\"", "\\\"");
        final String resultJson = String.format("{ \"valid\": %b, \"errorMessage\": \"%s\" }", isValid[0], safeErrorMessage);
        final String script = String.format("handleFormValidationResult(%s);", resultJson);

        Platform.runLater(() -> {
            try {
                // ✔ Αντί να απαντήσεις με JS μήνυμα, απλά φόρτωσε τη σελίδα
                webView.getEngine().load(getClass().getResource("/meals.html").toExternalForm());
            } catch (Exception e) {
                System.err.println("❌ Failed to load meals.html: " + e.getMessage());
                e.printStackTrace();
            }
        });
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