module com.example.redteamp1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires commons.math3;
    requires javafx.swing;


    opens com.example.redteamp1 to javafx.fxml;
    exports com.example.redteamp1;
}