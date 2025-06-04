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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.xml.bind.JAXBException;
import com.example.travel.util.XmlUtils;
import com.example.travel.util.DatabaseUpdater;
import com.example.travel.util.WordReportGenerator;
import com.example.travel.model.State;
import com.example.travel.model.TravelRecord;

public class App extends Application {

    private static final String DB_URL = "jdbc:h2:file:./traveldb;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private TableView<TravelRecord> table;
    private ObservableList<TravelRecord> records;
    private Connection connection;
    private TextField descriptionFilter;
    private ComboBox<State> stateFilter;
    private CheckBox hideVisitedFilter;

    @Override
    public void start(Stage stage) {
        initDatabase();

        // Set custom application icon
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(getClass().getResourceAsStream("/travel-icon.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Create menu bar
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        MenuItem exportMenuItem = new MenuItem("Export to XML");
        exportMenuItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Travel Records");
            fileChooser.setInitialFileName("Travel.xml");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml")
            );
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    List<TravelRecord> records = TravelRecord.findAll();
                    XmlUtils.exportToXml(records, file);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Export Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("Records have been exported successfully.");
                    alert.showAndWait();
                } catch (JAXBException | SQLException | IOException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Export Error");
                    alert.setHeaderText("Could not export records");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }
        });

        MenuItem importMenuItem = new MenuItem("Import from XML");
        importMenuItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Travel Records");
            fileChooser.setInitialFileName("Travel.xml");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    List<TravelRecord> importedRecords = XmlUtils.importFromXml(file);
                    refreshTableData();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Import Successful");
                    alert.setHeaderText(null);
                    alert.setContentText(importedRecords.size() + " travel records have been imported successfully!");
                    alert.showAndWait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Import Error");
                    alert.setHeaderText("Could not import records");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }
        });

        MenuItem manageTagsMenuItem = new MenuItem("Manage Tags");
        manageTagsMenuItem.setOnAction(e -> {
            TagsDialog tagsDialog = new TagsDialog(stage);
            tagsDialog.showAndWait();
            // Refresh the table data in case tags were updated
            refreshTableData();
        });

        fileMenu.getItems().addAll(exportMenuItem, importMenuItem, new SeparatorMenuItem(), manageTagsMenuItem);
        menuBar.getMenus().add(fileMenu);
        root.setTop(menuBar);

        // Create TableView
        table = new TableView<>();
        records = FXCollections.observableArrayList();
        table.setItems(records);

        // Double click to edit
        table.setRowFactory(tv -> {
            TableRow<TravelRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showEditDialog(stage, row.getItem());
                }
            });
            return row;
        });

        // Configure table properties
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setMinWidth(800);

        // Create a VBox to hold the table and allow it to grow
        VBox tableContainer = new VBox(table);
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

        TableColumn<TravelRecord, Boolean> visitedCol = new TableColumn<>("Visited");
        visitedCol.setCellValueFactory(new PropertyValueFactory<>("visited"));
        visitedCol.setPrefWidth(0);
        visitedCol.setMinWidth(0);
        visitedCol.setResizable(false);

        TableColumn<TravelRecord, Boolean> planCol = new TableColumn<>("Plan");
        planCol.setCellValueFactory(new PropertyValueFactory<>("plan"));
        planCol.setPrefWidth(50);
        planCol.setMinWidth(50);
        planCol.setResizable(true);

        TableColumn<TravelRecord, String> tagCol = new TableColumn<>("Tag");
        tagCol.setCellValueFactory(new PropertyValueFactory<>("tag"));
        tagCol.setPrefWidth(80);
        tagCol.setMinWidth(80);
        tagCol.setResizable(true);

        table.getColumns().add(descCol);
        table.getColumns().add(tagCol);
        table.getColumns().add(urlCol);
        table.getColumns().add(stateCol);
        table.getColumns().add(cityCol);
        table.getColumns().add(addressCol);
        table.getColumns().add(zipCol);
        table.getColumns().add(geoCol);
        table.getColumns().add(visitedCol);
        table.getColumns().add(planCol);
        table.getSortOrder().add(descCol); // Default sort by description

        // Filter fields
        descriptionFilter = new TextField();
        descriptionFilter.setPromptText("Filter by description...");
        stateFilter = new ComboBox<>();
        stateFilter.setPromptText("Filter by state...");
        stateFilter.getItems().addAll(State.values());
        stateFilter.getItems().add(0, null); // Add null option for "All states"

        // Add Hide Visited checkbox
        hideVisitedFilter = new CheckBox("Hide Visited");
        hideVisitedFilter.setSelected(true); // Set to checked by default

        // Add filter listeners
        descriptionFilter.textProperty().addListener((obs, oldVal, newVal) -> 
            applyFilters(descriptionFilter, stateFilter, hideVisitedFilter));
        stateFilter.valueProperty().addListener((obs, oldVal, newVal) -> 
            applyFilters(descriptionFilter, stateFilter, hideVisitedFilter));
        hideVisitedFilter.selectedProperty().addListener((obs, oldVal, newVal) -> 
            applyFilters(descriptionFilter, stateFilter, hideVisitedFilter));

        HBox filterBox = new HBox(10);
        filterBox.getChildren().addAll(
            new Label("Description:"), descriptionFilter,
            new Label("State:"), stateFilter,
            hideVisitedFilter
        );
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(0, 0, 10, 0));

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
                            applyFilters(descriptionFilter, stateFilter, hideVisitedFilter); // Refresh with filters
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

        // Edit button
        Button editButton = new Button("Update Record");
        editButton.setDisable(true); // Initially disabled

        // Enable/disable edit button based on selection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            editButton.setDisable(newSelection == null);
        });

        editButton.setOnAction(e -> {
            TravelRecord selectedRecord = table.getSelectionModel().getSelectedItem();
            if (selectedRecord != null) {
                showEditDialog(stage, selectedRecord);
            }
        });

        // Report generation button
        Button reportButton = new Button("Generate Planned Visits Report");
        reportButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Planned Visits Report");
            fileChooser.setInitialFileName("TravelPlanned.docx");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Word documents (*.docx)", "*.docx")
            );
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    WordReportGenerator.generatePlannedVisitsReport(file.getAbsolutePath());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Report Generated");
                    alert.setHeaderText(null);
                    alert.setContentText("Planned visits report has been generated successfully.");
                    alert.showAndWait();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Report Generation Error");
                    alert.setHeaderText("Could not generate report");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.getChildren().addAll(addButton, editButton, deleteButton, reportButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        VBox centerBox = new VBox(10);
        centerBox.getChildren().addAll(filterBox, tableContainer);

        root.setCenter(centerBox);
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
            // Create new table with proper BLOB column
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = connection.createStatement();

            // Create travel records table if it doesn't exist
            stmt.execute("CREATE TABLE IF NOT EXISTS travel_records ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "description VARCHAR(255) UNIQUE NOT NULL,"
                + "url VARCHAR(1024),"
                + "state VARCHAR(255),"
                + "city VARCHAR(255),"
                + "address VARCHAR(255),"
                + "zip VARCHAR(10),"
                + "geo VARCHAR(255),"
                + "picture BLOB,"
                + "picture2 BLOB,"
                + "picture3 BLOB,"
                + "notes TEXT,"
                + "date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "date_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "visited BOOLEAN DEFAULT FALSE NOT NULL,"
                + "plan BOOLEAN DEFAULT FALSE NOT NULL"
                + ")");

            stmt.close();

            // Update database schema if needed (for existing databases)
            DatabaseUpdater.updateSchema();

            // Update TravelRecord schema to add picture2 and picture3 columns
            TravelRecord.updateDatabaseSchema();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Could not initialize database");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void showAddDialog(Stage owner) {
        AddRecordDialog dialog = new AddRecordDialog(owner);
        dialog.showAndWait().ifPresent(record -> {
            try {
                record.save();
                refreshTableData();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                if (e.getMessage().contains("description already exists")) {
                    alert.setHeaderText("Duplicate Description");
                    alert.setContentText("A record with this description already exists. Please use a different description.");
                } else {
                    alert.setHeaderText("Could not save record");
                    alert.setContentText(e.getMessage());
                }
                alert.showAndWait();
            }
        });
    }

    private void showEditDialog(Stage owner, TravelRecord record) {
        EditRecordDialog dialog = new EditRecordDialog(owner, record);
        dialog.showAndWait().ifPresent(result -> {
            try {
                result.save();
                refreshTableData();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                if (e.getMessage().contains("description already exists")) {
                    alert.setHeaderText("Duplicate Description");
                    alert.setContentText("A record with this description already exists. Please use a different description.");
                } else {
                    alert.setHeaderText("Could not save record");
                    alert.setContentText(e.getMessage());
                }
                alert.showAndWait();
            }
        });
    }

    private void applyFilters(TextField descriptionFilter, ComboBox<State> stateFilter, CheckBox hideVisitedFilter) {
        String descriptionText = descriptionFilter.getText().toLowerCase().trim();
        State selectedState = stateFilter.getValue();
        boolean hideVisited = hideVisitedFilter.isSelected();

        try {
            List<TravelRecord> allRecords = TravelRecord.findAll();
            List<TravelRecord> filteredRecords = allRecords.stream()
                .filter(record -> 
                    (descriptionText.isEmpty() || 
                     record.getDescription().toLowerCase().contains(descriptionText)) &&
                    (selectedState == null || 
                     (record.getState() != null && 
                      record.getState().equals(selectedState.name()))) &&
                    (!hideVisited || record.getVisited() == null || !record.getVisited())
                )
                .collect(Collectors.toList());

            table.setItems(FXCollections.observableArrayList(filteredRecords));
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load records");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void refreshTableData() {
        try {
            // Instead of just loading all records, apply the current filters
            if (descriptionFilter != null && stateFilter != null && hideVisitedFilter != null) {
                applyFilters(descriptionFilter, stateFilter, hideVisitedFilter);
            } else {
                // This will only happen before the UI is fully initialized
                records.clear();
                records.addAll(TravelRecord.findAll());
            }
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
