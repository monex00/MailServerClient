module com.example.prog3project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires com.google.gson;

    exports com.client.model;
    exports com.client.views;
    exports com.client.controllers;

    opens com.client.views to javafx.fxml;
    opens com.client.controllers to javafx.fxml;

    exports com.server.view;
    exports com.server.model;
    exports com.server.controller;
    exports com.test;


    opens com.server.controller to javafx.fxml;
    opens com.server.view to javafx.fxml;

    exports com to com.google.gson;

    opens com to com.google.gson;
    opens com.test to javafx.fxml;
}