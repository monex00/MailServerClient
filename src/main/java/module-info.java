module com.example.prog3project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    requires com.google.gson;

    exports com.client;
    exports com.server;
    exports com.test;
    exports com to com.google.gson;

    opens com to com.google.gson;
    opens com.client to javafx.fxml;
    opens com.server to javafx.fxml;
    opens com.test to javafx.fxml;
}