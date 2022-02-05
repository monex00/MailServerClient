package com.client.controllers;

import com.client.views.StartClient;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class LoginController {
    private String clientEmailAddress="";
    private String clientPng="";

    @FXML
    private Button btnLogin;


    private ImageView imgPrevious=null;

    @FXML
    public void initialize(){
        btnLogin.setDisable(true);
    }

    @FXML
    private void updateClientMail(MouseEvent event)
    {
        ImageView imgClicked = ((ImageView) event.getSource()); //yields complete string

        btnLogin.setDisable(false);

        if(imgPrevious == null){
            imgPrevious = imgClicked;
        }
        resetOpacity(imgClicked);
        clientEmailAddress = getEmails(imgClicked.getId());
        clientPng = imgClicked.getId().replace("_img", ".png");
        imgPrevious = imgClicked;
    }

    private String getEmails(String id) {
        return switch (id) {
            case "alice_img" -> "a@gmail.com";
            case "bob_img" -> "b@gmail.com";
            case "charlie_img" -> "c@gmail.com";
            default -> "";
        };
    }

    @FXML
    public void login() throws IOException {
        StartClient client = new StartClient();
        client.setEmailAddress(clientEmailAddress);
        client.setImgPath(clientPng);
        client.start(new Stage());
    }

    private void resetOpacity(ImageView imgClicked){
        imgPrevious.setOpacity(0.5);
        imgClicked.setOpacity(1);
    }

    public void setStage(Stage stage){
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }
}
