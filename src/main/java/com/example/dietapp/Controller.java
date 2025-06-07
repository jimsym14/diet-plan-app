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
import java.util.Collections;
import com.example.dietapp.model.User;
import com.example.dietapp.model.SavefromDatabase;
import com.example.dietapp.model.CalorieCalculator;

public class Controller {
    private final WebView webView;
    private static final String DB_URL = "jdbc:sqlite:mealsdb.sqlite";
    private final DietPlan dietPlan;
    private String currentDay = "MO"; // Προεπιλεγμένη ημέρα Δευτέρα
    private List<Meal> allMealsCache; // Cache με όλα τα γεύματα

    private final NutritionCalculator nutritionCalculator;

    // Αναζήτηση και φιλτράρισμα γευμάτων
    private final MealSearchService mealSearchService;

    private boolean filterVegan = false;
    private boolean filterVegetarian = false;
    private boolean filterGlutenFree = false;
    private boolean filterDairyFree = false;
    private double filterMaxCalories = 0; // 0 σημαίνει χωρίς όριο θερμίδων
    private double filterMinProtein = 0; // 0 σημαίνει χωρίς ελάχιστη πρωτεΐνη
    private boolean filterHasValidNutrition = false;
    private String currentSearchQuery = "";

    public Controller(WebView webView) {
        this.webView = webView;
        this.dietPlan = new DietPlan();
        this.nutritionCalculator = new NutritionCalculator();
        this.mealSearchService = new MealSearchService();
    }

    public void handleMessage(String message) {
        System.out.println("📨 Ελήφθη μήνυμα από JavaScript: " + message);
        try {
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
            String action = jsonObject.get("action").getAsString();
            System.out.println("🎯 Εντοπίστηκε ενέργεια: " + action);

            switch (action) {
                case "selectDay":
                    String day = jsonObject.get("day").getAsString();
                    System.out.println("🗓️ Επεξεργασία ενέργειας selectDay για: " + day);
                    selectDay(day);
                    break;
                case "toggleMealSelection":
                    String mealDay = jsonObject.get("day").getAsString();
                    int mealId = jsonObject.get("mealId").getAsInt();
                    toggleMealSelection(mealDay, mealId);
                    break;
                case "openClients":
                    Platform.runLater(() -> {
                        try {
                            new ClientTableView().start(new javafx.stage.Stage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
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
                    System.out.println("Άγνωστη ενέργεια: " + action);
            }
        } catch (Exception e) {
            System.err.println(
                    "❌ Σφάλμα κατά την ανάλυση ή επεξεργασία μηνύματος: " + message + " | Σφάλμα: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

        System.out.println("Φίλτρα ενημερώθηκαν: " +
                "Vegan: " + filterVegan +
                ", Vegetarian: " + filterVegetarian +
                ", GF: " + filterGlutenFree +
                ", DF: " + filterDairyFree +
                ", Max Cal: " + filterMaxCalories +
                ", Min Protein: " + filterMinProtein +
                ", Valid Nutrition: " + filterHasValidNutrition);
    }

    private void selectDay(String day) {
        System.out.println("🗓️ ==== Ξεκινά η επιλογή ημέρας ====");
        System.out.println("🗓️ Προηγούμενη ημέρα: " + this.currentDay);
        System.out.println("🗓️ Ζητήθηκε νέα ημέρα: " + day);

        this.currentDay = day;
        System.out.println("🗓️ Επιλέχθηκε ημέρα: " + day);

        List<Integer> selectedMealsForDay = dietPlan.getMealsForDay(day);
        System.out.println(
                "📋 Γεύματα για " + day + ": " + selectedMealsForDay.size() + " γεύματα - IDs: " + selectedMealsForDay);

        JsonObject responseMessage = new JsonObject();
        responseMessage.addProperty("action", "selectDay");
        responseMessage.addProperty("day", day);
        responseMessage.add("selectedMealIds", new Gson().toJsonTree(selectedMealsForDay));

        String responseJson = responseMessage.toString();
        System.out.println("📤 Αποστολή απάντησης selectDay σε JavaScript: " + responseJson);

        Platform.runLater(() -> {
            try {
                String script = String.format("handleMessageFromJava('%s');", responseJson.replace("'", "\\'"));
                webView.getEngine().executeScript(script);
                System.out.println("✅ Απεστάλη απάντηση selectDay στην JavaScript");
            } catch (Exception e) {
                System.err.println("❌ Αποτυχία αποστολής απάντησης selectDay: " + e.getMessage());
            }
        });
        System.out.println("🗓️ ==== ΕΠΙΛΟΓΗ ΗΜΕΡΑΣ ΤΕΛΟΣ ====");
    }

    private void toggleMealSelection(String day, int mealId) {
        List<Integer> selectedMeals = dietPlan.getMealsForDay(day);

        if (selectedMeals.contains(mealId)) {
            dietPlan.removeMealFromDay(day, mealId);
            System.out.println(" Meal αφαιρέθηκε: ID " + mealId + " από ημέρα " + day);
        } else {
            dietPlan.addMealToDay(day, mealId);
            System.out.println(" Meal προστέθηκε: ID " + mealId + " σε ημέρα " + day);
        }

        // Πάντα υπολογίζουμε από την αρχή όλη τη διατροφή για τη μέρα
        nutritionCalculator.calculateNutritionForDay(day, dietPlan.getMealsForDay(day), allMealsCache);

        // Ενημέρωση UI
        updateNutritionDisplays(day);
    }

    private void updateCalorieDisplay(String day) {
        String[] calorieInfo = nutritionCalculator.formatCalorieDisplay(day);
        String displayValue = calorieInfo[0];
        String displayLabel = calorieInfo[1];
        String cssClass = calorieInfo[2];

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
                System.out.println(" Ενημέρωση οθόνης θερμίδων για ημέρα " + day);
            } catch (Exception e) {
                System.err.println(" Αποτυχία ενημέρωσης οθόνης θερμίδων: " + e.getMessage());
            }
        });
    }

    public void loadMealsAndSendInitialData() {
        try {
            System.out.println(" Φόρτωση γευμάτων από τη βάση δεδομένων...");
            if (allMealsCache == null) {
                allMealsCache = getAllMeals();
            }
            System.out.println("🚀 Βρέθηκαν " + allMealsCache.size() + " γεύματα");

            nutritionCalculator.calculateNutritionForDay(currentDay, dietPlan.getMealsForDay(currentDay),
                    allMealsCache);

            sendInitialData();

        } catch (SQLException e) {
            System.err.println(" Σφάλμα βάσης δεδομένων: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(" Απροσδόκητο σφάλμα: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendInitialData() {
        if (allMealsCache == null) {
            System.err.println(" Το cache γευμάτων είναι άδειο, δεν μπορώ να στείλω αρχικά δεδομένα.");
            return;
        }

        String mealCardsHtml = MealCardGenerator.generateAllMealCardsHtml(allMealsCache);

        List<Integer> initialSelectedIds = dietPlan.getMealsForDay(currentDay);
        String initialSelectedIdsJson = new Gson().toJson(initialSelectedIds);
        String allSelectedIdsJson = new Gson().toJson(dietPlan.getAllSelections());

        double dailyTarget = dietPlan.getDailyTargetCalories();
        double proteinGoal = nutritionCalculator.getProteinGoal();
        double carbsGoal = nutritionCalculator.getCarbsGoal();
        double fatGoal = nutritionCalculator.getFatGoal();

        String script = String.format(
                "document.getElementById('mealContainer').innerHTML = `%s`; " +
                        "setupPageWithHtml('%s', %s, %s, %f, %f, %f, %f);",
                mealCardsHtml.replace("`", "\\`"),
                currentDay,
                initialSelectedIdsJson,
                allSelectedIdsJson,
                dailyTarget,
                proteinGoal,
                carbsGoal,
                fatGoal);

        Platform.runLater(() -> {
            try {
                webView.getEngine().executeScript(script);
                System.out.println("✅ Αρχικά δεδομένα σελίδας αποστάλθηκαν για ημέρα: " + currentDay);

                updateNutritionDisplays(currentDay);
            } catch (Exception e) {
                System.err.println("❌ Αποτυχία εκτέλεσης σεναρίου setup: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void updateNutritionDisplays(String day) {
        updateCalorieDisplay(day);

        updateMacronutrientDisplays(day);
    }

    private void updateMacronutrientDisplays(String day) {
        String[] macroInfo = nutritionCalculator.formatMacronutrientDisplays(day);

        String proteinDisplay = macroInfo[0];
        String proteinClass = macroInfo[1];
        String carbsDisplay = macroInfo[2];
        String carbsClass = macroInfo[3];
        String fatDisplay = macroInfo[4];
        String fatClass = macroInfo[5];

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
                System.out.println(" Ενημέρωση μακροθρεπτικών στοιχείων για ημέρα " + day);
            } catch (Exception e) {
                System.err.println(" Αποτυχία ενημέρωσης μακροθρεπτικών στοιχείων: " + e.getMessage());
            }
        });
    }

    private void applySearchAndFilters() {
        if (allMealsCache == null) {
            System.err.println(" Το cache γευμάτων είναι άδειο, δεν μπορώ να εκτελέσω αναζήτηση/φιλτράρισμα.");
            return;
        }

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

        System.out.println(" Search/filter βρήκε " + filteredMeals.size() + " γεύματα");

        String filteredMealsHtml = MealCardGenerator.generateAllMealCardsHtml(filteredMeals);

        final String htmlContent = filteredMealsHtml.replace("`", "\\`");
        final String selectedIdsJson = new Gson().toJson(dietPlan.getMealsForDay(currentDay));

        final String script = String.format(
                "document.getElementById('mealContainer').innerHTML = `%s`; updateSelectedCards(%s);",
                htmlContent,
                selectedIdsJson);

        Platform.runLater(() -> {
            try {
                webView.getEngine().executeScript(script);
                System.out.println("✅ UI ενημερώθηκε με τα φιλτραρισμένα γεύματα");
            } catch (Exception e) {
                System.err.println("❌ Αποτυχία ενημέρωσης καρτών γευμάτων: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void validateUserForm(JsonObject formData) {
        try {
            // Ανάκτηση δεδομένων
            String fullname = formData.has("fullname") ? formData.get("fullname").getAsString() : "";
            String email = formData.has("email") ? formData.get("email").getAsString() : "";
            String weightStr = formData.has("weight") ? formData.get("weight").getAsString() : "";
            String heightStr = formData.has("height") ? formData.get("height").getAsString() : "";
            String ageStr = formData.has("age") ? formData.get("age").getAsString() : "";
            String gender = formData.has("gender") ? formData.get("gender").getAsString() : "";
            String goal = formData.has("goal") ? formData.get("goal").getAsString() : "";
            String activity = formData.has("activityLevel") ? formData.get("activityLevel").getAsString() : "";
            String preferences = formData.has("dietaryPreferences") ? formData.get("dietaryPreferences").getAsString()
                    : "";

            List<String> allergiesList = new ArrayList<>();
            if (formData.has("foodAllergies") && formData.get("foodAllergies").isJsonArray()) {
                formData.getAsJsonArray("foodAllergies").forEach(element -> {
                    String allergy = element.getAsString();
                    if (!allergy.equals("none")) {
                        allergiesList.add(allergy);
                    }
                });
            }

            int mealsPerDay = formData.has("mealsPerDay") ? formData.get("mealsPerDay").getAsInt() : 3;
            boolean autoSelectMeals = formData.has("autoSelectMeals") ? formData.get("autoSelectMeals").getAsBoolean()
                    : false;

            if (fullname.trim().isEmpty() || email.trim().isEmpty() ||
                    weightStr.trim().isEmpty() || heightStr.trim().isEmpty() ||
                    ageStr.trim().isEmpty() || gender.trim().isEmpty() ||
                    goal.trim().isEmpty() || activity.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Όλα τα πεδία είναι υποχρεωτικά. Παρακαλώ συμπληρώστε όλα τα πεδία της φόρμας.");
            }

            // Μετατροπή αριθμητικών τιμών
            double weight = Double.parseDouble(weightStr);
            double height = Double.parseDouble(heightStr);
            int age = Integer.parseInt(ageStr);

            // Δημιουργία χρήστη & υπολογισμός στόχου θερμίδων
            User user = new User(fullname, email, age, height, weight, gender, activity, goal, preferences,
                    allergiesList, mealsPerDay, autoSelectMeals);

            SavefromDatabase.saveUser(user);

            double targetCalories = CalorieCalculator.calculateCalories(user);
            user.setTargetCalories(targetCalories);

            dietPlan.setDailyTargetCalories(targetCalories);
            nutritionCalculator.setAllDailyTargets(targetCalories);
            // Υπολογισμός στόχων μακροθρεπτικών με βάση τις συνολικές θερμίδες
            double proteinGoal = (targetCalories * 0.20) / 4; // 20% protein (4 kcal per g)
            double carbsGoal = (targetCalories * 0.50) / 4; // 50% carbs (4 kcal per g)
            double fatGoal = (targetCalories * 0.30) / 9; // 30% fat (9 kcal per g)

            nutritionCalculator.setMacroGoals(proteinGoal, carbsGoal, fatGoal);

            System.out.println(" Protein Goal: " + Math.round(proteinGoal) + "g");
            System.out.println(" Carbs Goal: " + Math.round(carbsGoal) + "g");
            System.out.println(" Fat Goal: " + Math.round(fatGoal) + "g");

            System.out.println(" Υπολογισμένες θερμίδες χρήστη: " + targetCalories);
            System.out.println(" Έλεγχος στόχων ανά ημέρα:");
            nutritionCalculator.getDailyCalorieTargets().forEach((day, cal) -> {
                System.out.println("   " + day + " ➜ " + cal + " kcal");
            });

            // Αν δεν έχουν φορτωθεί γεύματα, τα φέρνουμε
            if (allMealsCache == null) {
                try {
                    allMealsCache = getAllMeals();
                } catch (SQLException ex) {
                    System.err.println("Σφάλμα φόρτωσης meals από DB: " + ex.getMessage());
                }
            }

            // Υπολογισμός μακροθρεπτικών για την τρέχουσα μέρα
            nutritionCalculator.calculateNutritionForDay(currentDay, dietPlan.getMealsForDay(currentDay),
                    allMealsCache);

            // Αυτόματη επιλογή γευμάτων
            if (autoSelectMeals) {
                try {
                    autoSelectMealsForUser(user, allMealsCache);
                    System.out.println("✅ Αυτόματη επιλογή γευμάτων ολοκληρώθηκε με επιτυχία.");
                } catch (Exception e) {
                    System.err.println("❌ Σφάλμα κατά την αυτόματη επιλογή γευμάτων: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println(" Ολοκληρώθηκε η καταγραφή & υπολογισμός θερμίδων");

            Platform.runLater(() -> {
                try {
                    webView.getEngine().load(getClass().getResource("/meals.html").toExternalForm());
                    System.out.println(" Φόρτωση meals.html μετά την επικύρωση της φόρμας.");
                } catch (Exception e) {
                    System.err.println(" Αποτυχία φόρτωσης meals.html: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Σφάλμα κατά την επεξεργασία δεδομένων φόρμας: " + e.getMessage());
            e.printStackTrace();

            String errorMessage = e.getMessage().replace("\"", "\\\"");
            String script = String.format("handleFormValidationResult({valid: false, errorMessage: \"%s\"});",
                    errorMessage);

            Platform.runLater(() -> {
                try {
                    webView.getEngine().executeScript(script);
                } catch (Exception scriptError) {
                    System.err.println(" Αποτυχία εκτέλεσης σεναρίου σφάλματος: " + scriptError.getMessage());
                }
            });
        }
    }

    // Λειτουργικότητα αυτόματης επιλογής γευμάτων για τον χρήστη
    private void autoSelectMealsForUser(User user, List<Meal> allMeals) throws Exception {
        System.out.println("🔄 Ξεκινάει η αυτόματη επιλογή γευμάτων για χρήστη: " + user.getFullname());

        // Υπολογισμός θερμίδων ανά γεύμα
        double targetCaloriesPerMeal = user.getTargetCalories() / user.getMealsPerDay();
        System.out.println("📊 Στόχος θερμίδων ανά γεύμα: " + Math.round(targetCaloriesPerMeal));

        // Φιλτράρισμα γευμάτων βάσει προτιμήσεων και αλλεργιών χρήστη
        List<Meal> filteredMeals = filterMealsForAutoSelect(allMeals, user);
        System.out.println("🔍 Πλήθος φιλτραρισμένων γευμάτων: " + filteredMeals.size() + " από " + allMeals.size());

        // Έλεγχος αν έχουμε αρκετά γεύματα
        if (filteredMeals.size() < user.getMealsPerDay()) {
            throw new Exception("Δεν βρέθηκαν αρκετά κατάλληλα γεύματα. Βρέθηκαν " + filteredMeals.size() +
                    ", χρειάζονται τουλάχιστον " + user.getMealsPerDay());
        }

        // Μέρες της εβδομάδας - καθαρισμός υπαρχόντων και επιλογή νέων
        String[] days = { "MO", "TU", "WE", "TH", "FR", "SA", "SU" };
        for (String day : days) {
            dietPlan.clearMealsForDay(day);
            // Δημιουργία νέας λίστας για ποικιλία
            List<Meal> dayMeals = new ArrayList<>(filteredMeals);
            selectMealsForDay(day, dayMeals, targetCaloriesPerMeal, user.getMealsPerDay());
        }

        // 📋 ΠΕΡΙΛΗΨΗ ΕΠΙΛΟΓΗΣ ΓΕΥΜΑΤΩΝ:
        System.out.println("📋 ΠΕΡΙΛΗΨΗ ΕΠΙΛΟΓΗΣ ΓΕΥΜΑΤΩΝ:");
        for (String day : days) {
            List<Integer> selectedMealIds = dietPlan.getMealsForDay(day);
            System.out.println(
                    "   " + day + ": " + selectedMealIds.size() + " γεύματα επιλέχθηκαν - IDs: " + selectedMealIds);
        }

        System.out.println("✅ Ολοκληρώθηκε αυτόματη επιλογή γευμάτων για όλες τις μέρες");
    }

    private List<Meal> filterMealsForAutoSelect(List<Meal> allMeals, User user) {
        List<Meal> filtered = new ArrayList<>();

        for (Meal meal : allMeals) {
            if (isMealSuitableForUser(meal, user)) {
                filtered.add(meal);
            }
        }

        return filtered;
    }

    private boolean isMealSuitableForUser(Meal meal, User user) {
        // Προτιμήσεις διατροφής
        String preferences = user.getDietaryPreferences();
        if (preferences != null && !preferences.equals("none")) {
            switch (preferences.toLowerCase()) {
                case "vegetarian":
                    if (!meal.isVegetarian())
                        return false;
                    break;
                case "vegan":
                    if (!meal.isVegan())
                        return false;
                    break;
                case "healthy":
                    if (!meal.isVeryHealthy())
                        return false;
                    break;
                case "lowfodmap":
                    if (!meal.isLowFodmap())
                        return false;
                    break;
            }
        }

        // Αλλεργίες τροφίμων
        List<String> allergies = user.getFoodAllergies();
        if (allergies != null && !allergies.isEmpty()) {
            for (String allergy : allergies) {
                if (allergy.equals("none"))
                    continue;

                switch (allergy.toLowerCase()) {
                    case "dairy":
                        if (!meal.isDairyFree())
                            return false;
                        break;
                    case "gluten":
                        if (!meal.isGlutenFree())
                            return false;
                        break;
                    case "nuts":
                    case "tree nuts":
                    case "peanuts":
                        break;
                }
            }
        }

        return meal.getCaloriesNotNull() > 0;
    }

    private void selectMealsForDay(String day, List<Meal> availableMeals, double targetCaloriesPerMeal,
            int mealsPerDay) {
        System.out.println("🗓️ Επιλογή γευμάτων για " + day);

        // Ανακάτεμα της λίστας για τυχαιότητα
        java.util.Collections.shuffle(availableMeals);

        // Εύρος θερμίδων στόχου (±20%)
        double minCalories = targetCaloriesPerMeal * 0.8;
        double maxCalories = targetCaloriesPerMeal * 1.2;

        for (int i = 0; i < mealsPerDay; i++) {
            Meal selectedMeal = findBestMealForCalories(availableMeals, minCalories, maxCalories);

            if (selectedMeal != null) {
                dietPlan.addMealToDay(day, selectedMeal.getId());
                availableMeals.remove(selectedMeal); // Αποφυγή διπλών την ίδια μέρα
                System.out.println("   ✓ Επιλέχθηκε: " + selectedMeal.getName() +
                        " (" + Math.round(selectedMeal.getCaloriesNotNull()) + " kcal)");
            } else {
                // Αν δεν υπάρχει στο εύρος, διάλεξε το πιο κοντινό
                selectedMeal = findClosestCalorieMeal(availableMeals, targetCaloriesPerMeal);
                if (selectedMeal != null) {
                    dietPlan.addMealToDay(day, selectedMeal.getId());
                    availableMeals.remove(selectedMeal);
                    System.out.println("   ~ Κοντινότερο γεύμα: " + selectedMeal.getName() +
                            " (" + Math.round(selectedMeal.getCaloriesNotNull()) + " kcal)");
                } else {
                    System.out.println("   ❌ Κανένα κατάλληλο γεύμα για τη θέση " + (i + 1));
                    break; // Τέλος διαθέσιμων γευμάτων
                }
            }
        }

        System.out.println("   📊 Σύνολο επιλεγμένων γευμάτων για " + day + ": " + dietPlan.getMealsForDay(day).size());
    }

    private Meal findBestMealForCalories(List<Meal> meals, double minCalories, double maxCalories) {
        List<Meal> inRange = new ArrayList<>();

        for (Meal meal : meals) {
            double calories = meal.getCaloriesNotNull();
            if (calories >= minCalories && calories <= maxCalories) {
                inRange.add(meal);
            }
        }

        if (inRange.isEmpty()) {
            return null;
        }

        // Τυχαιότητα
        Collections.shuffle(inRange);
        // Επιστρέφει τυχαίο γεύμα από το εύρος
        return inRange.get((int) (Math.random() * inRange.size()));
    }

    private Meal findClosestCalorieMeal(List<Meal> meals, double targetCalories) {
        if (meals.isEmpty()) {
            return null;
        }

        Meal closest = meals.get(0);
        double closestDiff = Math.abs(closest.getCaloriesNotNull() - targetCalories);

        for (Meal meal : meals) {
            double diff = Math.abs(meal.getCaloriesNotNull() - targetCalories);
            if (diff < closestDiff) {
                closest = meal;
                closestDiff = diff;
            }
        }

        return closest;
    }

    private List<Meal> getAllMeals() throws SQLException {
        File dbFile = new File("mealsdb.sqlite");
        System.out.println(" Database path: " + dbFile.getAbsolutePath());

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

        // Ταξινόμηση γευμάτων με βάση την εγκυρότητα μακροθρεπτικών
        meals.sort(Comparator.comparing(Meal::hasValidMacros).reversed());

        return meals;
    }

    private Double getDoubleOrNull(ResultSet rs, String columnName) throws SQLException {
        double value = rs.getDouble(columnName);
        return rs.wasNull() ? null : value;
    }
}