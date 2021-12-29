package com.client;

import com.server.ClientHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class StartClient extends Application {

    private String emailAddress = "b@gmail.com";
    private String imgPath = "bob.png";

    @Override
    public void start(Stage stage) throws IOException {

        URL clientUrl = ClientApp.class.getResource("client.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(clientUrl);

        //creating a controller instance manually to setting attributes before loading the fxml
        ClientController controller = new ClientController(emailAddress, imgPath, stage);
        fxmlLoader.setController(controller);

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle(emailAddress);
        stage.setScene(scene);
        stage.show();
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public void setImgPath(String imgPath){ this.imgPath = imgPath; }

    public static void main(String[] args) {
        launch();
    }
}
