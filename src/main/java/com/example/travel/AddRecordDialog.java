package com.example.travel;

import com.example.travel.model.TravelRecord;
import com.example.travel.model.State;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import com.example.travel.components.ImageDropPane;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.scene.Node;
import java.sql.SQLException;
import java.util.List;

public class AddRecordDialog extends Dialog<TravelRecord> {
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
    private final ImageDropPane imageDropPane = new ImageDropPane();
    private final ImageDropPane imageDropPane2 = new ImageDropPane();
    private final ImageDropPane imageDropPane3 = new ImageDropPane();
    private final TextArea notesField = new TextArea();
    private final CheckBox visitedCheckBox = new CheckBox("Visited");
    private final CheckBox planCheckBox = new CheckBox("Plan to Visit");
    private final ComboBox<String> tagComboBox = new ComboBox<>();

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

        grid.add(new Label("URL:"), 2, 0);
        grid.add(urlField, 3, 0);

        grid.add(new Label("City:"), 0, 1);
        grid.add(cityField, 1, 1);

        grid.add(new Label("State:"), 2, 1);
        stateComboBox.getItems().addAll(State.values());
        stateComboBox.setPromptText("Select a state");
        grid.add(stateComboBox, 3, 1);

        grid.add(new Label("Address:"), 0, 3);
        grid.add(addressField, 1, 3);

        grid.add(new Label("ZIP:"), 0, 4);
        grid.add(zipField, 1, 4);
        zipField.setPrefColumnCount(5);

        grid.add(new Label("Phone:"), 2, 4);
        grid.add(phoneNumberField, 3, 4);
        phoneNumberField.setPrefColumnCount(13);

        grid.add(new Label("Geo:"), 0, 5);
        grid.add(geoField, 1, 5);

        grid.add(new Label("Pictures:"), 0, 6);

        // Set a smaller size for each image pane
        imageDropPane.setPrefSize(150, 150);
        imageDropPane2.setPrefSize(150, 150);
        imageDropPane3.setPrefSize(150, 150);

        // Add each image pane to the grid
        grid.add(imageDropPane, 1, 6);
        grid.add(imageDropPane2, 2, 6);
        grid.add(imageDropPane3, 3, 6);

        grid.add(new Label("Notes:"), 0, 7);
        notesField.setPrefRowCount(6);
        notesField.setMaxWidth(Double.MAX_VALUE);
        grid.add(notesField, 1, 7, 3, 1);

        // Add checkboxes for visited and plan
        grid.add(new Label("Status:"), 0, 8);
        grid.add(visitedCheckBox, 1, 8);
        grid.add(planCheckBox, 2, 8);

        // Add tag combobox
        grid.add(new Label("Tag:"), 0, 9);

        // Load tags from database
        try {
            // Always add an empty option
            tagComboBox.getItems().add("");

            // Add all tags from the database
            List<com.example.travel.model.Tag> tagList = com.example.travel.model.Tag.findAll();
            for (com.example.travel.model.Tag tag : tagList) {
                tagComboBox.getItems().add(tag.getTag());
            }
        } catch (SQLException e) {
            System.err.println("Error loading tags: " + e.getMessage());
            // Fallback to empty tag list
            tagComboBox.getItems().add("");
        }

        tagComboBox.setPromptText("Select a tag");
        grid.add(tagComboBox, 1, 9);

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

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                TravelRecord record = new TravelRecord();
                record.setDescription(descriptionField.getText());
                record.setUrl(urlField.getText());
                record.setState(stateComboBox.getValue() != null ? stateComboBox.getValue().name() : null);
                record.setCity(cityField.getText());
                record.setAddress(addressField.getText());
                record.setZip(zipField.getText());
                record.setPhoneNumber(phoneNumberField.getText());
                record.setGeo(geoField.getText());
                record.setPicture(imageDropPane.getImageData());
                record.setPicture2(imageDropPane2.getImageData());
                record.setPicture3(imageDropPane3.getImageData());
                record.setNotes(notesField.getText());
                record.setVisited(visitedCheckBox.isSelected());
                record.setPlan(planCheckBox.isSelected());
                record.setTag(tagComboBox.getValue());
                return record;
            }
            return null;
        });
    }

    private void validateFields(Node saveButton) {
        boolean isValid = !descriptionField.getText().trim().isEmpty();
        saveButton.setDisable(!isValid);
    }
}
