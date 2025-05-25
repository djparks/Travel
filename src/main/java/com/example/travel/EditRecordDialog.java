package com.example.travel;

import com.example.travel.model.State;
import com.example.travel.model.TravelRecord;
import javafx.geometry.Insets;
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
    private final TextField geoField = new TextField();
    private final TextArea notesArea = new TextArea();
    private final ImageDropPane imageDropPane = new ImageDropPane();
    private final TravelRecord record;  // Used to initialize dialog fields and for validation

    public EditRecordDialog(Stage owner, TravelRecord record) {
        this.record = record;
        setTitle("Edit Travel Record");
        setHeaderText("Edit travel record details");
        initOwner(owner);

        // Create UI
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Add fields
        grid.add(new Label("Description:"), 0, 0);
        grid.add(descriptionField, 1, 0);
        descriptionField.setText(record.getDescription());

        grid.add(new Label("URL:"), 0, 1);
        grid.add(urlField, 1, 1);
        urlField.setText(record.getUrl());

        grid.add(new Label("State:"), 0, 2);
        grid.add(stateComboBox, 1, 2);
        stateComboBox.getItems().addAll(State.values());
        if (record.getState() != null) {
            try {
                stateComboBox.setValue(State.valueOf(record.getState()));
            } catch (IllegalArgumentException e) {
                // Invalid state value in record, leave combobox empty
            }
        }

        grid.add(new Label("City:"), 0, 3);
        grid.add(cityField, 1, 3);
        cityField.setText(record.getCity());

        grid.add(new Label("Address:"), 0, 4);
        grid.add(addressField, 1, 4);
        addressField.setText(record.getAddress());

        grid.add(new Label("ZIP:"), 0, 5);
        grid.add(zipField, 1, 5);
        zipField.setText(record.getZip());

        grid.add(new Label("Geo:"), 0, 6);
        grid.add(geoField, 1, 6);
        geoField.setText(record.getGeo());

        grid.add(new Label("Picture:"), 0, 7);
        grid.add(imageDropPane, 1, 7);
        imageDropPane.setImageData(record.getPicture());

        grid.add(new Label("Notes:"), 0, 8);
        grid.add(notesArea, 1, 8);
        notesArea.setText(record.getNotes());
        notesArea.setPrefRowCount(3);

        getDialogPane().setContent(grid);

        // Add buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Enable/Disable save button depending on whether description is empty
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> 
            saveButton.setDisable(newValue.trim().isEmpty()));

        // Convert the result to TravelRecord object when save is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                record.setDescription(descriptionField.getText().trim());
                record.setUrl(urlField.getText().trim());
                record.setState(stateComboBox.getValue() != null ? stateComboBox.getValue().name() : null);
                record.setCity(cityField.getText().trim());
                record.setAddress(addressField.getText().trim());
                record.setZip(zipField.getText().trim());
                record.setGeo(geoField.getText().trim());
                record.setPicture(imageDropPane.getImageData());
                record.setNotes(notesArea.getText().trim());
                return record;
            }
            return null;
        });
    }
}
