package com.example.travel;

import com.example.travel.model.Tag;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class TagsDialog extends Dialog<Void> {
    private final TableView<Tag> tagsTable = new TableView<>();
    private final ObservableList<Tag> tags = FXCollections.observableArrayList();
    private final TextField tagField = new TextField();
    private Tag selectedTag = null;

    public TagsDialog(Stage owner) {
        setTitle("Manage Tags");
        setHeaderText("Add, edit, or delete tags");
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        // Create the main layout
        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(10));

        // Create the table
        tagsTable.setItems(tags);
        tagsTable.setMinWidth(300);
        tagsTable.setMaxHeight(300);

        // Create the table column
        TableColumn<Tag, String> tagColumn = new TableColumn<>("Tag");
        tagColumn.setCellValueFactory(cellData -> {
            String tagValue = cellData.getValue().getTag();
            return new javafx.beans.property.SimpleStringProperty(tagValue);
        });
        tagColumn.setPrefWidth(300);

        tagsTable.getColumns().add(tagColumn);

        // Add selection listener to the table
        tagsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedTag = newSelection;
                tagField.setText(newSelection.getTag());
            } else {
                selectedTag = null;
                tagField.clear();
            }
        });

        // Create the form for adding/editing tags
        VBox formPane = new VBox(10);
        formPane.setPadding(new Insets(10));

        Label tagLabel = new Label("Tag:");
        HBox tagBox = new HBox(10, tagLabel, tagField);
        HBox.setHgrow(tagField, Priority.ALWAYS);
        tagField.setPromptText("Enter tag name");

        // Create buttons
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> addTag());

        Button updateButton = new Button("Update");
        updateButton.setOnAction(e -> updateTag());
        updateButton.setDisable(true);

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteTag());
        deleteButton.setDisable(true);

        // Enable/disable buttons based on selection
        tagsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            updateButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });

        HBox buttonBox = new HBox(10, addButton, updateButton, deleteButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        formPane.getChildren().addAll(tagBox, buttonBox);

        // Add components to the main pane
        mainPane.setCenter(tagsTable);
        mainPane.setBottom(formPane);

        // Load existing tags
        loadTags();

        // Set the dialog content
        getDialogPane().setContent(mainPane);
        getDialogPane().setPrefSize(400, 500);

        // Add close button
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Set result converter
        setResultConverter(dialogButton -> null);
    }

    private void loadTags() {
        try {
            List<Tag> tagList = Tag.findAll();
            tags.setAll(tagList);
        } catch (SQLException e) {
            showError("Error loading tags", e.getMessage());
        }
    }

    private void addTag() {
        String tagName = tagField.getText().trim();
        if (tagName.isEmpty()) {
            showError("Validation Error", "Tag name cannot be empty");
            return;
        }

        try {
            Tag tag = new Tag(tagName);
            tag.save();
            loadTags();
            tagField.clear();
            selectedTag = null;
        } catch (SQLException e) {
            showError("Error adding tag", e.getMessage());
        }
    }

    private void updateTag() {
        if (selectedTag == null) {
            return;
        }

        String tagName = tagField.getText().trim();
        if (tagName.isEmpty()) {
            showError("Validation Error", "Tag name cannot be empty");
            return;
        }

        try {
            selectedTag.setTag(tagName);
            selectedTag.save();
            loadTags();
            tagField.clear();
            selectedTag = null;
            tagsTable.getSelectionModel().clearSelection();
        } catch (SQLException e) {
            showError("Error updating tag", e.getMessage());
        }
    }

    private void deleteTag() {
        if (selectedTag == null) {
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Tag");
        confirmDialog.setContentText("Are you sure you want to delete the tag '" + selectedTag.getTag() + "'?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    selectedTag.delete();
                    loadTags();
                    tagField.clear();
                    selectedTag = null;
                    tagsTable.getSelectionModel().clearSelection();
                } catch (SQLException e) {
                    showError("Error deleting tag", e.getMessage());
                }
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
