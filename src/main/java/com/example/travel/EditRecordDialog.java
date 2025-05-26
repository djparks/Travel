package com.example.travel;

import com.example.travel.model.State;
import com.example.travel.model.TravelRecord;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import com.example.travel.components.ImageDropPane;

public class EditRecordDialog extends Dialog<TravelRecord> {
    private final TextField descriptionField = new TextField();
    private final TextField urlField = new TextField();
    private final ComboBox<State> stateComboBox = new ComboBox<>();
    private final TextField cityField = new TextField();
    private final TextField addressField = new TextField();
    private final TextField zipField = new TextField();
    private final TextField phoneNumberField = new TextField();
    private final TextField geoField = new TextField();
    private final TextArea notesArea = new TextArea();
    private final ImageDropPane imageDropPane = new ImageDropPane();
    private final CheckBox visitedCheckBox = new CheckBox("Visited");
    private final CheckBox planCheckBox = new CheckBox("Plan to Visit");
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
        visitedCheckBox.setSelected(record.getVisited() != null ? record.getVisited() : false);
        planCheckBox.setSelected(record.getPlan() != null ? record.getPlan() : false);

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
        visitedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));
        planCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> setHasChanges(true));

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

        grid.add(new Label("Picture:"), 0, 7);
        grid.add(imageDropPane, 1, 7);
        imageDropPane.setImageData(record.getPicture());

        grid.add(new Label("Notes:"), 0, 8);
        grid.add(notesArea, 1, 8);
        notesArea.setText(record.getNotes());
        notesArea.setPrefRowCount(6);
        notesArea.setMaxWidth(Double.MAX_VALUE);

        // Add checkboxes for visited and plan
        grid.add(new Label("Status:"), 0, 9);
        grid.add(visitedCheckBox, 1, 9);
        grid.add(planCheckBox, 2, 9);

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
                record.setVisited(visitedCheckBox.isSelected());
                record.setPlan(planCheckBox.isSelected());
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
