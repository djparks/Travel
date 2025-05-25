package com.example.travel.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ImageDropPane extends VBox {
    private final ImageView imageView;
    private final Label promptLabel;
    private final ObjectProperty<byte[]> imageData = new SimpleObjectProperty<>();
    private Consumer<byte[]> onImageChanged;

    public ImageDropPane() {
        setAlignment(Pos.CENTER);
        setSpacing(10);
        setStyle("-fx-border-color: #cccccc; -fx-border-style: dashed; -fx-border-width: 2; -fx-padding: 10;");

        imageView = new ImageView();
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);

        promptLabel = new Label("Drag and drop an image here or click to choose");
        promptLabel.setWrapText(true);

        getChildren().addAll(imageView, promptLabel);

        // Enable drag and drop
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);

        // Enable click to choose file
        setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File file = fileChooser.showOpenDialog(getScene().getWindow());
            if (file != null) {
                try {
                    setImageData(Files.readAllBytes(file.toPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            File file = db.getFiles().get(0);
            String filename = file.getName().toLowerCase();
            if (filename.endsWith(".png") || filename.endsWith(".jpg") || 
                filename.endsWith(".jpeg") || filename.endsWith(".gif")) {
                try {
                    setImageData(Files.readAllBytes(file.toPath()));
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    public void setImageData(byte[] data) {
        this.imageData.set(data);
        if (data != null && data.length > 0) {
            Image image = new Image(new ByteArrayInputStream(data));
            imageView.setImage(image);
            promptLabel.setText("Click or drag to change image");
        } else {
            imageView.setImage(null);
            promptLabel.setText("Drag and drop an image here or click to choose");
        }
        if (onImageChanged != null) {
            onImageChanged.accept(data);
        }
    }

    public byte[] getImageData() {
        return imageData.get();
    }

    public ObjectProperty<byte[]> imageDataProperty() {
        return imageData;
    }

    public void setOnImageChanged(Consumer<byte[]> handler) {
        this.onImageChanged = handler;
    }
}
