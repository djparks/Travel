package com.example.travel;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class App extends Application {

    private static final String DB_URL = "jdbc:h2:file:./traveldb";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    @Override
    public void start(Stage stage) {
        initDatabase();
        
        var label = new Label("Travel Application");
        var scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(scene);
        stage.setTitle("Travel Application");
        stage.show();
    }

    private void initDatabase() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            
            // Create example table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS destinations (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    country VARCHAR(255) NOT NULL,
                    description TEXT
                )
            """);
            
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
