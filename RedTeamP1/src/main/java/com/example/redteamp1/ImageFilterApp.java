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
import javafx.scene.paint.Color;
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
        filterDropdown.getItems().addAll("Grey Scale", "Womp Womp", "Inverted", "Gaussian", "Sepia", "Increase Brightness");
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
        // Implement filter code based on the selectedFilter

        if ("Grey Scale".equals(selectedFilter)) {
            convertToGrayscale();
        }

        if ("Womp Womp".equals(selectedFilter)) {
            convertToWompWomp();
        }

        // Apply the filter to the imageView
        // Example: imageView.setEffect(new SomeFilterEffect());
    }

    private void convertToGrayscale() {
        Image originalImage = imageView.getImage();
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();

        javafx.scene.image.WritableImage grayscaleImage = new javafx.scene.image.WritableImage(width, height);
        javafx.scene.image.PixelWriter pixelWriter = grayscaleImage.getPixelWriter();

        javafx.scene.image.PixelReader pixelReader = originalImage.getPixelReader();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Get the color of each pixel
                Color color = pixelReader.getColor(x, y);

                // Calculate grayscale value
                double grayscaleValue = (color.getRed() + color.getGreen() + color.getBlue()) / 3;

                // Set the grayscale color to the pixel
                pixelWriter.setColor(x, y, Color.color(grayscaleValue, grayscaleValue, grayscaleValue));
            }
        }

        imageView.setImage(grayscaleImage);
    }

    private void convertToWompWomp() {
        Image originalImage = imageView.getImage();
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();

        javafx.scene.image.WritableImage wompWompImage = new javafx.scene.image.WritableImage(width, height);
        javafx.scene.image.PixelWriter pixelWriter = wompWompImage.getPixelWriter();

        javafx.scene.image.PixelReader pixelReader = originalImage.getPixelReader();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Get the color of each pixel
                Color color = pixelReader.getColor(x, y);

                // Preserve the original red and green values, apply filtering to blue
                double red = color.getRed();
                double green = color.getGreen();
                double blue = Math.min(color.getBlue() * 3, 1.0); // Ensure blue doesn't exceed 1.0

                // Set the wompWomp color to the pixel
                pixelWriter.setColor(x, y, Color.color(red, green, blue));
            }
        }

        imageView.setImage(wompWompImage);
    }

}
