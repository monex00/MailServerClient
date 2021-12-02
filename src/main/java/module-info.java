module com.example.prog3project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens com.client to javafx.fxml;
    exports com.client;
}