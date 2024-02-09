package com.example.redteamp1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;

import java.io.File;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.math3.distribution.NormalDistribution;

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
        if(Objects.equals(selectedFilter, "Gaussian")){
            applyFilterBlur();
        }
    }
    private void applyFilterBlur() {
        System.out.println("Blur");
        int[][] kernel = new int[7][7];

        // Converts the javafx Image type to a buffered img type
        Image nonBufferedImage = imageView.getImage();
        BufferedImage img = SwingFXUtils.fromFXImage(nonBufferedImage, null);
        for(int i = 0; i < 5; i++) {
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

                    Color newPixelColor = new Color(red, green, blue, 255);
                    img.setRGB(x, y, newPixelColor.getRGB());
                }
            }
            imageView.setImage(SwingFXUtils.toFXImage(img, null));
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
}