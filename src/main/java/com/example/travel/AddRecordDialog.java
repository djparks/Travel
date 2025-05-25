package com.example.travel;

import com.example.travel.model.TravelRecord;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import javafx.stage.Modality;
import javafx.stage.Window;

public class AddRecordDialog extends Dialog<TravelRecord> {
    private final TextField descriptionField = new TextField();
    private final TextField stateField = new TextField();
    private final TextField cityField = new TextField();
    private final TextField addressField = new TextField();
    private final TextField zipField = new TextField();
    private final TextArea picturesField = new TextArea();
    private final TextArea notesField = new TextArea();

    public AddRecordDialog(Window owner) {
        setTitle("Add New Travel Record");
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        // Create the content grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Add form fields
        grid.add(new Label("Description:*"), 0, 0);
        grid.add(descriptionField, 1, 0);
        
        grid.add(new Label("State:*"), 0, 1);
        grid.add(stateField, 1, 1);
        
        grid.add(new Label("City:*"), 0, 2);
        grid.add(cityField, 1, 2);
        
        grid.add(new Label("Address:*"), 0, 3);
        grid.add(addressField, 1, 3);
        
        grid.add(new Label("ZIP:"), 0, 4);
        grid.add(zipField, 1, 4);
        
        grid.add(new Label("Pictures:"), 0, 5);
        picturesField.setPrefRowCount(2);
        grid.add(picturesField, 1, 5);
        
        grid.add(new Label("Notes:"), 0, 6);
        notesField.setPrefRowCount(2);
        grid.add(notesField, 1, 6);

        getDialogPane().setContent(grid);

        // Add buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Enable/Disable save button depending on whether required fields are filled
        Node saveButton = getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Add validation listeners
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> 
            validateFields(saveButton));
        stateField.textProperty().addListener((obs, oldVal, newVal) -> 
            validateFields(saveButton));
        cityField.textProperty().addListener((obs, oldVal, newVal) -> 
            validateFields(saveButton));
        addressField.textProperty().addListener((obs, oldVal, newVal) -> 
            validateFields(saveButton));

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                TravelRecord record = new TravelRecord();
                record.setDescription(descriptionField.getText());
                record.setState(stateField.getText());
                record.setCity(cityField.getText());
                record.setAddress(addressField.getText());
                record.setZip(zipField.getText());
                record.setPictures(picturesField.getText());
                record.setNotes(notesField.getText());
                return record;
            }
            return null;
        });
    }

    private void validateFields(Node saveButton) {
        boolean isValid = !descriptionField.getText().trim().isEmpty() &&
                         !stateField.getText().trim().isEmpty() &&
                         !cityField.getText().trim().isEmpty() &&
                         !addressField.getText().trim().isEmpty();
        saveButton.setDisable(!isValid);
    }
}
