package com.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class StartClient extends Application {

    private static String emailAddress = "b@gmail.com";
    private static String imgPath = "bob.png";

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

    public void setEmailAddress(String emailAddress) { emailAddress = emailAddress; }

    public void setImgPath(String imgPath){ imgPath = imgPath; }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your email address: ");

        emailAddress = scanner.nextLine();
        System.out.println("Enter your image path: ");
        imgPath = scanner.nextLine();
        scanner.close();

        launch();
    }
}
