package com.example.dietapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        webView.getEngine().load(Main.class.getResource("/index.html").toExternalForm());
        Controller controller = new Controller(webView);

        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, newVal) -> {
            if (newVal == javafx.concurrent.Worker.State.SUCCEEDED) {
                webView.getEngine().executeScript("window.javaBridge = " + controller);
            }
        });

        Scene scene = new Scene(webView, 900, 700);
        stage.setTitle("Diet Plan App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}