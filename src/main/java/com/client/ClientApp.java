package com.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ClientApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        URL clientUrl = ClientApp.class.getResource("client.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(clientUrl);
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setTitle("MyMail");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
