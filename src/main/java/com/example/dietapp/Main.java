package com.example.dietapp;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import com.example.dietapp.model.SavefromDatabase;


public class Main extends Application {
    private Controller controller;

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        controller = new Controller(webView);

        // Load the initial page
        webView.getEngine().load(Main.class.getResource("/index.html").toExternalForm());

        // Set up bridge once the engine is ready
        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, newVal) -> {
            if (newVal == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    // Get the current page URL
                    String location = webView.getEngine().getLocation();

                    // Set up the bridge
                    System.out.println("🌉 Setting up JavaFX bridge...");
                    System.out.println("χψωγηξιθυτφδφωγβηξγγη");
                    System.out.println("γβηφη");
                    JSObject window = (JSObject) webView.getEngine().executeScript("window");
                    window.setMember("javaConnector", controller);
                    webView.getEngine().executeScript(
                            "if (window.bridge) { console.log('Bridge already exists'); } " +
                                    "else { window.bridge = { postMessage: function(msg) { javaConnector.handleMessage(msg); } }; "
                                    +
                                    "console.log('Bridge created successfully'); }");
                    System.out.println("✅ JavaFX bridge setup complete");

                    // If we're on the meals page, load the meals
                    if (location.endsWith("meals.html")) {
                        System.out.println("📋 On meals page, loading meals...");
                        controller.loadMealsAndSendInitialData();
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error setting up JavaFX bridge: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        // Get the operating system name
        String osName = System.getProperty("os.name").toLowerCase();
        Scene scene;

        if (osName.contains("mac")) {
            // For macOS: Set the stage to occupy the entire screen bounds
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            scene = new Scene(webView, screenBounds.getWidth(), screenBounds.getHeight());
        } else if (osName.contains("win")) {
            // For Windows: Maximize the stage
            stage.setMaximized(true);
            scene = new Scene(webView, 900, 700); // Default size, will be overridden by maximization
        } else {
            // Fallback for other OS (e.g., Linux)
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            scene = new Scene(webView, screenBounds.getWidth(), screenBounds.getHeight());
        }

        // Set stage title and scene
        stage.setTitle("Diet Plan App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);

    }



}