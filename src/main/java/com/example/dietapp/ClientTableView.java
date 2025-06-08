package com.example.dietapp;

import com.example.dietapp.model.User;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class ClientTableView extends Application {

    @Override
    public void start(Stage stage) {
        TableView<User> tableView = new TableView<>();

        // Στήλη Full Name
        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(300);

        // Στήλη Email
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(350);

        tableView.getColumns().addAll(nameCol, emailCol);
        tableView.setItems(getUsersFromDatabase());

        VBox root = new VBox(tableView);
        Scene scene = new Scene(root, 650, 400);

        stage.setTitle("Clients");
        stage.setScene(scene);
        stage.show();
    }

    private ObservableList<User> getUsersFromDatabase() {
        ObservableList<User> users = FXCollections.observableArrayList();
        String url = "jdbc:sqlite:mealsdb.sqlite"; // ή το πλήρες path αν είσαι εκτός IDE

        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT name, email, age FROM users")) {

            while (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                int age = rs.getInt("age");
                System.out.println("Found user: " + name + " (" + email + ")");
                users.add(new User(name, email, age, 170.0, 70.0, "male", "moderate", "maintain", "none",
                        java.util.Arrays.asList("none"), 3, true));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
