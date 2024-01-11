module com.example.redteamp1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.redteamp1 to javafx.fxml;
    exports com.example.redteamp1;
}