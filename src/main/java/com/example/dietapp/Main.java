package com.example.dietapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import javafx.application.Platform;

public class Main extends Application {
    private Controller controller;

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        controller = new Controller(webView);

        webView.getEngine().load(Main.class.getResource("/index.html").toExternalForm());

        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, newVal) -> {
            if (newVal == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    // Εξαγωγή της τρέχουσας τοποθεσίας URL
                    String location = webView.getEngine().getLocation();

                    // Bridge setup
                    System.out.println(" Setting up JavaFX bridge...");

                    JSObject window = (JSObject) webView.getEngine().executeScript("window");
                    window.setMember("javaConnector", controller);
                    webView.getEngine().executeScript(
                            "if (window.bridge) { console.log('Bridge already exists'); } " +
                                    "else { window.bridge = { postMessage: function(msg) { javaConnector.handleMessage(msg); } }; "
                                    +
                                    "console.log('Bridge created successfully'); }");
                    System.out.println(" JavaFX bridge setup complete");

                    // Φόρτωσε τα γεύματα
                    if (location.endsWith("meals.html")) {
                        System.out.println(" On meals page, scheduling delayed loading of meals...");
                        new Thread(() -> {
                            try {
                                Thread.sleep(400); // ή 500 αν χρειαστεί
                                Platform.runLater(() -> controller.loadMealsAndSendInitialData());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                } catch (Exception e) {
                    System.err.println(" Error setting up JavaFX bridge: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        Scene scene = new Scene(webView, 975, 650);

        stage.setTitle("Diet Plan App");
        stage.setMinWidth(975);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {

        launch(args);
    }

}