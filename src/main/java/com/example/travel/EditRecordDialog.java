package com.example.travel;

import com.example.travel.model.State;
import com.example.travel.model.TravelRecord;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import com.example.travel.components.ImageDropPane;
import java.sql.SQLException;
import java.util.List;

public class EditRecordDialog extends Dialog<TravelRecord> {
    private final TextField descriptionField = new TextField();
    private final TextField urlField = new TextField();
    private final ComboBox<State> stateComboBox = new ComboBox<>();
    private final TextField cityField = new TextField();
    private final TextField addressField = new TextField();

    // Set preferred column counts for wider display
    {
        descriptionField.setPrefColumnCount(30);
        urlField.setPrefColumnCount(30);
        addressField.setPrefColumnCount(30);
    }
    private final TextField zipField = new TextField();
    private final TextField phoneNumberField = new TextField();
    private final TextField geoField = new TextField();
    private final TextArea notesArea = new TextArea();
    private final ImageDropPane imageDropPane = new ImageDropPane();
    private final ImageDropPane imageDropPane2 = new ImageDropPane();
    private final ImageDropPane imageDropPane3 = new ImageDropPane();
    private final CheckBox visitedCheckBox = new CheckBox("Visited");
    private final CheckBox planCheckBox = new CheckBox("Plan to Visit");
    private final ComboBox<String> tagComboBox = new ComboBox<>();
    private final TravelRecord record;  // Used to initialize dialog fields and for validation

    private boolean hasChanges = false;
    private final ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);

    public EditRecordDialog(Stage owner, TravelRecord record) {
        this.record = record;
        setTitle("Edit Travel Record");
        setHeaderText("Edit travel record details");
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        // Create UI
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Initialize fields with record data
        descriptionField.setText(record.getDescription());
        urlField.setText(record.getUrl());
        cityField.setText(record.getCity());
        addressField.setText(record.getAddress());
        zipField.setText(record.getZip());
        phoneNumberField.setText(record.getPhoneNumber());
        geoField.setText(record.getGeo());
        notesArea.setText(record.getNotes());
        imageDropPane.setImageData(record.getPicture());
        imageDropPane2.setImageData(record.getPicture2());
        imageDropPane3.setImageData(record.getPicture3());
        visitedCheckBox.setSelected(record.getVisited() != null ? record.getVisited() : false);
        planCheckBox.setSelected(record.getPlan() != null ? record.getPlan() : false);

        // Initialize tag combobox with tags from database
        try {
            // Always add an empty option
            tagComboBox.getItems().add("");

            // Add all tags from the database
            List<com.example.travel.model.Tag> tagList = com.example.travel.model.Tag.findAll();
            for (com.example.travel.model.Tag tag : tagList) {
                tagComboBox.getItems().add(tag.getTag());
            }

            tagComboBox.setValue(record.getTag());
        } catch (SQLException e) {
            System.err.println("Error loading tags: " + e.getMessage());
            // Fallback to empty tag list
            tagComboBox.getItems().add("");
            tagComboBox.setValue("");
        }

        // Initialize state combobox
        stateComboBox.getItems().addAll(State.values());
        if (record.getState() != null && !record.getState().isEmpty()) {
            try {
                stateComboBox.setValue(State.valueOf(record.getState()));
            } catch (IllegalArgumentException e) {
                // Invalid state value in record, leave combobox empty
            }
        }

        // Add change listeners to all fields
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        urlField.textProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        stateComboBox.valueProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        cityField.textProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        addressField.textProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        zipField.textProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        phoneNumberField.textProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        geoField.textProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        notesArea.textProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        imageDropPane.imageDataProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        imageDropPane2.imageDataProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        imageDropPane3.imageDataProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        visitedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        planCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        tagComboBox.valueProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));

        grid.add(new Label("Description:*"), 0, 0);
        grid.add(descriptionField, 1, 0);

        grid.add(new Label("URL:"), 0, 1);
        grid.add(urlField, 1, 1);

        grid.add(new Label("City:"), 0, 2);
        grid.add(cityField, 1, 2);
        cityField.setText(record.getCity());

        grid.add(new Label("State:"), 2, 2);
        grid.add(stateComboBox, 3, 2);

        grid.add(new Label("Address:"), 0, 4);
        grid.add(addressField, 1, 4);
        addressField.setText(record.getAddress());

        grid.add(new Label("ZIP:"), 0, 5);
        grid.add(zipField, 1, 5);
        zipField.setText(record.getZip());
        zipField.setPrefColumnCount(5);

        grid.add(new Label("Phone:"), 2, 5);
        grid.add(phoneNumberField, 3, 5);
        phoneNumberField.setText(record.getPhoneNumber());
        phoneNumberField.setPrefColumnCount(13);

        grid.add(new Label("Geo:"), 0, 6);
        grid.add(geoField, 1, 6);
        geoField.setText(record.getGeo());

        grid.add(new Label("Pictures:"), 0, 7);

        // Set a smaller size for each image pane
        imageDropPane.setPrefSize(150, 150);
        imageDropPane2.setPrefSize(150, 150);
        imageDropPane3.setPrefSize(150, 150);

        // Add each image pane to the grid
        grid.add(imageDropPane, 1, 7);
        grid.add(imageDropPane2, 2, 7);
        grid.add(imageDropPane3, 3, 7);

        grid.add(new Label("Notes:"), 0, 8);
        grid.add(notesArea, 1, 8, 3, 1);
        notesArea.setText(record.getNotes());
        notesArea.setPrefRowCount(6);
        notesArea.setMaxWidth(Double.MAX_VALUE);

        // Add checkboxes for visited and plan
        grid.add(new Label("Status:"), 0, 9);
        grid.add(visitedCheckBox, 1, 9);
        grid.add(planCheckBox, 2, 9);

        // Add tag combobox
        grid.add(new Label("Tag:"), 0, 10);
        grid.add(tagComboBox, 1, 10);

        getDialogPane().setContent(grid);

        // Add buttons
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Get the save button
        Node saveButton = getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true); // Initially disabled until changes are made

        // Convert the result to TravelRecord object when save is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType && hasChanges) {
                record.setDescription(descriptionField.getText());
                record.setUrl(urlField.getText());
                record.setState(stateComboBox.getValue() != null ? stateComboBox.getValue().name() : null);
                record.setCity(cityField.getText());
                record.setAddress(addressField.getText());
                record.setZip(zipField.getText());
                record.setPhoneNumber(phoneNumberField.getText());
                record.setGeo(geoField.getText());
                record.setNotes(notesArea.getText());
                record.setPicture(imageDropPane.getImageData());
                record.setPicture2(imageDropPane2.getImageData());
                record.setPicture3(imageDropPane3.getImageData());
                record.setVisited(visitedCheckBox.isSelected());
                record.setPlan(planCheckBox.isSelected());
                record.setTag(tagComboBox.getValue());
                return record;
            }
            return null;
        });
    }

    private void setHasChanges(boolean changed) {
        hasChanges = changed;
        Node saveButton = getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(!hasChanges || descriptionField.getText().trim().isEmpty());
    }
}
