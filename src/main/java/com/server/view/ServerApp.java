package com.server.view;

import com.server.controller.ServerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import java.net.URL;

public class ServerApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        URL serverUrl = ServerApp.class.getResource("serverGUI.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(serverUrl);

        ServerController controller = new ServerController(stage);

        fxmlLoader.setController(controller);

        Scene scene = new Scene(fxmlLoader.load(), 600, 600);
        stage.setTitle("ServerLogs");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

    }


    public static void main(String[] args) throws IOException{
        launch();
    }
}
