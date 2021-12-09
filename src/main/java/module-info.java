module com.example.prog3project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;


    opens com.client to javafx.fxml;
    exports com.client;
    exports com.test;
    opens com.test to javafx.fxml;
}