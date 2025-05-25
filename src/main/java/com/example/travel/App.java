package com.example.travel;

import com.example.travel.model.TravelRecord;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class App extends Application {

    private static final String DB_URL = "jdbc:h2:file:./traveldb;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private TableView<TravelRecord> table;
    private ObservableList<TravelRecord> records;
    private Connection connection;

    @Override
    public void start(Stage stage) {
        initDatabase();
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Create TableView
        table = new TableView<>();
        records = FXCollections.observableArrayList();
        table.setItems(records);

        // Create columns
        TableColumn<TravelRecord, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(150);

        TableColumn<TravelRecord, String> stateCol = new TableColumn<>("State");
        stateCol.setCellValueFactory(new PropertyValueFactory<>("state"));
        stateCol.setPrefWidth(100);

        TableColumn<TravelRecord, String> cityCol = new TableColumn<>("City");
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        cityCol.setPrefWidth(100);

        TableColumn<TravelRecord, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setPrefWidth(150);

        TableColumn<TravelRecord, String> zipCol = new TableColumn<>("ZIP");
        zipCol.setCellValueFactory(new PropertyValueFactory<>("zip"));
        zipCol.setPrefWidth(80);

        table.getColumns().add(descCol);
        table.getColumns().add(stateCol);
        table.getColumns().add(cityCol);
        table.getColumns().add(addressCol);
        table.getColumns().add(zipCol);
        table.getSortOrder().add(descCol); // Default sort by description

        // Add button
        Button addButton = new Button("Add New Record");
        addButton.setOnAction(e -> showAddDialog(stage));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(addButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        root.setCenter(table);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Travel Records");
        stage.show();

        // Load initial data
        refreshTableData();
    }

    private void initDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = connection.createStatement();
            
            // Create travel records table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS travel_records (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    description VARCHAR(255) NOT NULL,
                    state VARCHAR(255) NOT NULL,
                    city VARCHAR(255) NOT NULL,
                    address VARCHAR(255) NOT NULL,
                    zip VARCHAR(10),
                    pictures TEXT,
                    notes TEXT,
                    date_created TIMESTAMP NOT NULL,
                    date_updated TIMESTAMP NOT NULL
                )
            """);
            
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAddDialog(Stage owner) {
        AddRecordDialog dialog = new AddRecordDialog(owner);
        dialog.showAndWait().ifPresent(record -> {
            try {
                record.save();
                refreshTableData();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not save record");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });
    }

    private void refreshTableData() {
        try {
            records.clear();
            records.addAll(TravelRecord.findAll());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load records");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @Override
    public void stop() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
