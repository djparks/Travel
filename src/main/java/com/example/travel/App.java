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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;
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
        
        // Configure table properties
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setMinWidth(800);

        // Create ScrollPane to handle horizontal scrolling
        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Create a VBox to hold the ScrollPane and allow it to grow
        VBox tableContainer = new VBox(scrollPane);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        tableContainer.setFillWidth(true);

        // Create columns with fixed widths
        TableColumn<TravelRecord, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(250);
        descCol.setMinWidth(250);
        descCol.setResizable(true);

        TableColumn<TravelRecord, String> urlCol = new TableColumn<>("URL");
        urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));
        urlCol.setPrefWidth(300);
        urlCol.setMinWidth(300);
        urlCol.setResizable(true);

        TableColumn<TravelRecord, String> stateCol = new TableColumn<>("State");
        stateCol.setCellValueFactory(new PropertyValueFactory<>("state"));
        stateCol.setPrefWidth(100);
        stateCol.setMinWidth(100);
        stateCol.setResizable(true);

        TableColumn<TravelRecord, String> cityCol = new TableColumn<>("City");
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        cityCol.setPrefWidth(150);
        cityCol.setMinWidth(150);
        cityCol.setResizable(true);

        TableColumn<TravelRecord, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setPrefWidth(250);
        addressCol.setMinWidth(250);
        addressCol.setResizable(true);

        TableColumn<TravelRecord, String> zipCol = new TableColumn<>("ZIP");
        zipCol.setCellValueFactory(new PropertyValueFactory<>("zip"));
        zipCol.setPrefWidth(100);
        zipCol.setMinWidth(100);
        zipCol.setResizable(true);

        TableColumn<TravelRecord, String> geoCol = new TableColumn<>("Geo");
        geoCol.setCellValueFactory(new PropertyValueFactory<>("geo"));
        geoCol.setPrefWidth(200);
        geoCol.setMinWidth(200);
        geoCol.setResizable(true);

        table.getColumns().add(descCol);
        table.getColumns().add(urlCol);
        table.getColumns().add(stateCol);
        table.getColumns().add(cityCol);
        table.getColumns().add(addressCol);
        table.getColumns().add(zipCol);
        table.getColumns().add(geoCol);
        table.getSortOrder().add(descCol); // Default sort by description

        // Add button
        Button addButton = new Button("Add New Record");
        addButton.setOnAction(e -> showAddDialog(stage));

        // Delete button
        Button deleteButton = new Button("Delete Record");
        deleteButton.setDisable(true); // Initially disabled

        // Enable/disable delete button based on selection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            deleteButton.setDisable(newSelection == null);
        });

        deleteButton.setOnAction(e -> {
            TravelRecord selectedRecord = table.getSelectionModel().getSelectedItem();
            if (selectedRecord != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Delete");
                alert.setHeaderText("Delete Record");
                alert.setContentText("Are you sure you want to delete this record?");

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            selectedRecord.delete();
                            refreshTableData();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Error");
                            errorAlert.setHeaderText("Could not delete record");
                            errorAlert.setContentText(ex.getMessage());
                            errorAlert.showAndWait();
                        }
                    }
                });
            }
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(deleteButton, addButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        root.setCenter(tableContainer);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, 1024, 768);
        
        // Make the window responsive
        stage.setMinWidth(800);
        stage.setMinHeight(600);
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
                    url VARCHAR(1024),
                    state VARCHAR(255),
                    city VARCHAR(255),
                    address VARCHAR(255),
                    zip VARCHAR(10),
                    geo VARCHAR(255),
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
