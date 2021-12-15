package com.client;

import com.server.ClientHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class StartNew extends Application {

    String emailAddress;
    @Override
    public void start(Stage stage) throws IOException {

        URL clientUrl = ClientApp.class.getResource("send.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(clientUrl);

        Scene scene = new Scene(fxmlLoader.load(), 400, 600);
        stage.setTitle("New Email");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        SendController controller = fxmlLoader.getController();
        controller.setEmailAddress(emailAddress);
        controller.setStage(stage); //passo lo stage al controller per poter gestirne la CloseRequest della window

    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public static void main(String[] args) {
        launch();
    }
}