package com.example.redteamp1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ImageFilterApp extends Application {

    private ImageView imageView;
    private ChoiceBox<String> filterDropdown;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Filter App");

        // Image Section
        imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(300);

        // Insert Image Button
        Button insertImageButton = new Button("Insert Image");
        insertImageButton.setOnAction(e -> insertImage());

        // Filter Dropdown
        filterDropdown = new ChoiceBox<>();
        filterDropdown.getItems().addAll("Grey Scale", "Inverted", "Gaussian", "Sepia", "Increase Brightness");
        filterDropdown.setValue("Select Filter");

        // Apply Filter Button
        Button applyFilterButton = new Button("Apply Filter");
        applyFilterButton.setOnAction(e -> applyFilter());

        // Layout
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10, 10, 10, 10));
        hbox.getChildren().addAll(insertImageButton, filterDropdown, applyFilterButton, imageView);

        // Scene
        Scene scene = new Scene(hbox, 650, 350);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private void insertImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            imageView.setImage(image);
        }
    }

    private void applyFilter() {
        String selectedFilter = filterDropdown.getValue();
        // Implement filter logic based on the selectedFilter
        // Apply the filter to the imageView
        // Example: imageView.setEffect(new SomeFilterEffect());
    }
}
