package com.example.redteamp1;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
//import java.awt.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import  javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ImageFilterApp extends Application {

    private ImageView originalImageView;
    private ImageView filteredImageView;
    private ChoiceBox<String> filterDropdown;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Filter App");

        // Original Image Section
        originalImageView = new ImageView();
        originalImageView.setFitWidth(300);
        originalImageView.setFitHeight(300);

        // Filtered Image Section
        filteredImageView = new ImageView();
        filteredImageView.setFitWidth(300);
        filteredImageView.setFitHeight(300);

        // Insert Image Button
        Button insertImageButton = new Button("Insert Image");
        insertImageButton.setOnAction(e -> insertImage());

        // Filter Dropdown
        filterDropdown = new ChoiceBox<>();
        filterDropdown.getItems().addAll("Grey Scale", "Womp Womp", "Negative", "Gaussian", "Sepia", "Increase Brightness");
        filterDropdown.setValue("Select Filter");

        // Apply Filter Button
        Button applyFilterButton = new Button("Apply Filter");
        applyFilterButton.setOnAction(e -> applyFilter());

        // Save Image Button
        Button saveImageButton = new Button("Save Filtered Image");
        saveImageButton.setOnAction(e -> saveImage());

        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10, 10, 10, 10));
        hbox.getChildren().addAll(insertImageButton, filterDropdown, applyFilterButton, saveImageButton, originalImageView);

        // VBox to stack original and filtered images
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(hbox, new HBox(originalImageView, filteredImageView));

        // Scene
        Scene scene = new Scene(vbox, 650, 450);
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
            originalImageView.setImage(image);
        }
    }

    private void saveImage() {
        Image filteredImage = filteredImageView.getImage();
        if (filteredImage != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                    new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                try {
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(filteredImage, null);
                    javax.imageio.ImageIO.write(bufferedImage, "png", file);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void applyFilter() {
        String selectedFilter = filterDropdown.getValue();

        if (originalImageView.getImage() != null) {
            // Clone the original image for comparison
            Image originalImage = originalImageView.getImage();
            originalImageView.setImage(originalImage);

            // Implement filter code based on the selectedFilter
            if ("Grey Scale".equals(selectedFilter)) {
                convertToGrayscale();
            } else if ("Womp Womp".equals(selectedFilter)) {
                convertToWompWomp();
            } else if ("Negative".equals(selectedFilter)) {
                convertToNegative();
            } else if ("Sepia".equals(selectedFilter)) {
                applySepiaFilter();
            } else if ("Increase Brightness".equals(selectedFilter)) {
                increaseBrightness();
            } else if (Objects.equals(selectedFilter, "Gaussian")) {
                applyFilterBlur();
            }

            // Display the filtered image
            Image filteredImage = filteredImageView.getImage();
            filteredImageView.setImage(filteredImage);
        }
    }

    private void applyFilterBlur() {
        System.out.println("Blur");
        int[][] kernel = new int[7][7];


        // Converts the javafx Image type to a buffered img type
        Image nonBufferedImage = originalImageView.getImage();
        BufferedImage img = SwingFXUtils.fromFXImage(nonBufferedImage, null);
        for(int i = 0; i < 1; i++) {
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    kernel = inputKernelData(x, y, kernel, img);
                    kernel = fillKernelValues(kernel);


                    double StDR = 6/2;
                    double StDG = 6/2;
                    double StDB = 6/2;


                    /* (kernel[centerKernel][centerKernel] & 0xff0000) >> 16 --> Makes Red
                     * (kernel[centerKernel][centerKernel] & 0xff00) >> 8    --> Makes Green
                     * (kernel[centerKernel][centerKernel] & 0xff)           --> Makes Blue
                     */
                    double[][] redGaussianKernel = getGaussianWeight(StDR, kernel, 0xff0000, 16);
                    double[][] greenGaussianKernel = getGaussianWeight(StDG, kernel, 0xff00, 8);
                    double[][] blueGaussianKernel = getGaussianWeight(StDB, kernel, 0xff, 0);


                    int red = convolutionKernelResult(redGaussianKernel, kernel, 0xff0000, 16);
                    int green = convolutionKernelResult(greenGaussianKernel, kernel, 0xff00, 8);
                    int blue = convolutionKernelResult(blueGaussianKernel, kernel, 0xff, 0);

                    int newPixelColor = (red << 16) | (green << 8) | blue;
                    img.setRGB(x, y, newPixelColor);
                }
            }
            filteredImageView.setImage(SwingFXUtils.toFXImage(img, null));
        }
    }
    private static double[][] getGaussianWeight(double sigma, int[][] kernel, int shift, int bitShift){
        double[][] gaussianWeightKernel = new double[kernel.length][kernel.length];
        double sum = 0;
        int kernelHalf = (int) Math.floor(kernel.length/2);
        for(int y = (int) (-1 * Math.floor(kernel.length/2)); y <= Math.floor(kernel.length/2); y++){
            for(int x = (int) (-1 * Math.floor(kernel.length/2)); x <= Math.floor(kernel.length/2); x++){
                // Ryan Seaman's Gaussian formula
                //double gaussianResult = ((Math.sqrt(2*Math.PI*Math.pow(standardDeviation, 2)))) * Math.pow(Math.E,
                //          -((kernel[x][y] * kernel[x][y])/(2*(standardDeviation * standardDeviation))));
                int xSquared = (int) Math.pow(x, 2);
                int ySquared = (int) Math.pow(y, 2);
                double sigmaSquared = Math.pow(sigma, 2);


                double exponent = -(xSquared + ySquared) / (2 * sigmaSquared);
                double halfOfTwoTimesPITimesSigmaSquared = (1/(2 * Math.PI * sigmaSquared));
                double gaussianResult =  halfOfTwoTimesPITimesSigmaSquared * Math.exp(exponent);


                gaussianWeightKernel[(x+kernelHalf)][(y+kernelHalf)] = gaussianResult;
                sum+=gaussianResult;
            }
        }
        // Normalize weight by dividing each value by the sum.
        // This ensures all the weights are from the values 0 - 1.
        for(int y = 0; y < gaussianWeightKernel.length; y++){
            for(int x = 0; x < gaussianWeightKernel.length; x++){
                gaussianWeightKernel[x][y] /= sum;
            }
        }


        return gaussianWeightKernel;
    }
    private static int convolutionKernelResult(double[][] gaussianKernel, int[][] kernel, int shift, int bitShift){
        double convolutionSum = 0;
        for(int y = 0; y < gaussianKernel.length; y++){
            for(int x = 0; x < gaussianKernel.length; x++){
                int kernelValue = (kernel[x][y] >> bitShift) & 0xFF;
                convolutionSum+=(kernelValue * gaussianKernel[x][y]);
            }
        }
        if(convolutionSum > 255){
            convolutionSum = 255;
        }else if(convolutionSum < 0) {
            convolutionSum = 0;
        }
        return (int)(convolutionSum);
    }


    /*
     * Input Kernel Data searches the relative pixels to the current x and y
     * If the new positions of x or y is negative or is greater than the bounds of the image
     *  set the kernel value to 256.
     * If the new position are on the image get the rgb value of that pixel
     */
    private static int[][] inputKernelData(int currentX, int currentY, int[][] kernel, BufferedImage nonBlurredImage) {
        int colorBinary[][] = new int[kernel.length][kernel.length];
        int kernelHalf = (int) Math.floor(kernel.length/2);
        for(int y = -kernelHalf; y <= kernelHalf; y++){
            for(int x = -kernelHalf; x <= kernelHalf; x++){
                int newX = currentX + x;
                int newY = currentY + y;
                int kernelXpos = (x + kernelHalf);
                int kernelYpos = (y + kernelHalf);
                if(newX >= 0 && newY >= 0 && newY < nonBlurredImage.getHeight() && newX < nonBlurredImage.getWidth()){
                    colorBinary[kernelXpos][kernelYpos] = nonBlurredImage.getRGB(newX, newY);
                }else{
                    colorBinary[kernelXpos][kernelYpos] = 256;
                }
            }
        }
        return colorBinary;
    }
    /*
     * Fill in unknown values in 7 by 7 kernel where the unknown values are equal to 256.
     * Regular value range less than or equal to 255 or are in the range of -12662255 representing all colors
     *
     *       --- Extreme Case ---
     *     0   1   2   3   4   5   6
     * 0  256 256 256 256 256 256 256
     * 1  256 256 256 256 256 256 256
     * 2  256 256 256 256 256 256 256
     * 3  256 256 256 100 100 13  120
     * 4  256 256 256 84  70  150 240
     * 5  256 256 256 96  80  132 186
     * 6  256 256 256 60  32  14  12
     *
     * >>> This Kernel is the most extreme instance at pixel value 0,0 and kernel position 3,3
     *      meaning 75% of the kernel will have unknown values since you can't have a pixel that's negative.
     *
     * >>> fillKernelValues will find a 256 value in the kernel then check the opposing quadrants
     *      If 256 is in quadrant 2 check 4
     *      If 256 is in quadrant 1 check 3
     *      and so on
     * >>> If there isn't a value in the opposing quadrant the algorithm check the quadrant either above or below the value
     *      If 256 is in quadrant 2 check 4 -- Fail
     *          Then check quadrant 3        -- Success
     *      If 256 is in quadrant 1 check 3 -- Fail
     *          Then check quadrant 4        -- Success
     *      and so on
     *
     * >>> Then if that fails the known must be in the quadrant adjacant to 256.
     *      If 256 is in quadrant 2 check 4 -- Fail
     *          Then check quadrant 3        -- Fail
     *              Then check quadrant 1     -- Success
     *      If 256 is in quadrant 1 check 3 -- Fail
     *          Then check quadrant 4        -- Fail
     *              Then check quadrant 2     -- Success
     *      and so on
     */
    private static int[][] fillKernelValues(int[][] kernel){
        for(int y = 0; y < kernel.length; y++){
            for(int x = 0; x < kernel.length; x++){
                if(kernel[x][y] == 256){
                    /*  The opposing pixel value in the kernel is always found by
                     *  subtracting the kernel length minus 1 to the current x or y value
                     *  then multiplying by negative 1
                     *
                     *  >>> In the case of 0
                     *         0 - (7- 1)
                     *         0 - 6 = -6
                     *         -6 * -1 = 6
                     *  (0,0) and (6,6) are on the other sides of the kernel
                     */
                    int newOpposingPixelX = ((x-(kernel.length - 1)) * -1);
                    int newOpposingPixelY = ((y-(kernel.length - 1)) * -1);
                    if(kernel[newOpposingPixelX][newOpposingPixelY] != 256){
                        kernel[x][y] = kernel[newOpposingPixelX][newOpposingPixelY];
                    }else if(kernel[x][newOpposingPixelY] != 256){
                        kernel[x][y] = kernel[x][newOpposingPixelY];
                    }else if(kernel[newOpposingPixelX][y] != 256){
                        kernel[x][y] = kernel[newOpposingPixelX][y];
                    }else{
                        // In case the algorithm fails set the kernel value to same as the center kernel since it has to have a value
                        kernel[x][y] = kernel[(int) (double) (kernel.length / 2)][(int) Math.floor(kernel.length/2)];
                    }
                }
            }
        }
        return kernel;
    }


    private void convertToGrayscale() {
        Image originalImage = originalImageView.getImage();
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

        filteredImageView.setImage(grayscaleImage);
    }

    private void convertToWompWomp() {
        Image originalImage = originalImageView.getImage();
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

        filteredImageView.setImage(wompWompImage);
    }


    // Applies the sepia filter using Pixel writer and Writable image class
    private void applySepiaFilter() {
        Image image = originalImageView.getImage();
        PixelReader pixelReader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixelReader.getArgb(x, y);

                // Extract alpha, red, green, and blue components
                int alpha = (argb >> 24) & 0xFF;
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;

                // Calculate sepia values
                int newRed = (int) Math.min(255, (0.393 * red + 0.769 * green + 0.189 * blue));
                int newGreen = (int) Math.min(255, (0.349 * red + 0.686 * green + 0.168 * blue));
                int newBlue = (int) Math.min(255, (0.272 * red + 0.534 * green + 0.131 * blue));

                // Compose new ARGB value with preserved alpha
                int sepiaArgb = (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue;

                // Write the new ARGB value to the writable image
                pixelWriter.setArgb(x, y, sepiaArgb);
            }
        }

        // Set the modified image with sepia filter applied
        filteredImageView.setImage(writableImage);
    }

//hiiiiiiiiiiiiiiiiiiiiiiiiii
    private void convertToNegative() {
        Image originalImage = originalImageView.getImage();
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();

        javafx.scene.image.WritableImage negativeImage = new javafx.scene.image.WritableImage(width, height);
        javafx.scene.image.PixelWriter pixelWriter = negativeImage.getPixelWriter();
        javafx.scene.image.PixelReader pixelReader = originalImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the color of each pixel
                Color color = pixelReader.getColor(x, y);

                // Convert color channel values to the range [0, 255] before negation
                int red = (int) (255 * (1 - color.getRed()));
                int green = (int) (255 * (1 - color.getGreen()));
                int blue = (int) (255 * (1 - color.getBlue()));

                pixelWriter.setColor(x, y, Color.rgb(red, green, blue));
            }
        }
        filteredImageView.setImage(negativeImage);
    }

    //this method increases the brightness of the original image
    private void increaseBrightness() {
        Image originalImage = originalImageView.getImage();
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();

        javafx.scene.image.WritableImage brightnessImage = new javafx.scene.image.WritableImage(width, height);
        javafx.scene.image.PixelWriter pixelWriter = brightnessImage.getPixelWriter();
        javafx.scene.image.PixelReader pixelReader = originalImage.getPixelReader();

        // loop through each pixel coordinate of the image
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                // Get the color of each pixel
                Color color = pixelReader.getColor(x, y);

                //multiply each value by 1.5
                double red = Math.min(color.getRed()*1.5,1);
                double green = Math.min(color.getGreen()*1.5,1);
                double blue = Math.min(color.getBlue()*1.5,1);

                // Set the brightness adjustment to the pixel
                pixelWriter.setColor(x, y, Color.color(red, green, blue));
            }
        } // set the viewed image as the new image with increased brightness
        filteredImageView.setImage(brightnessImage);
    }
}

