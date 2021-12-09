package com.client;

import com.Email;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import com.model.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.util.ArrayList;

public class LoginController {
    private String clientEmailAddress="";

    @FXML
    private ImageView alice_img;

    @FXML
    private ImageView bob_img;

    @FXML
    private ImageView charlie_img;

    private ImageView imgPrevious=null;
    public void initialize(){

        /*
        alice_img = new ImageView(new Image(ClientApp.class.getResource("images/alice.png").toExternalForm()));
        bob_img = new ImageView(new Image(ClientApp.class.getResource("images/bob.png").toExternalForm()));
        charlie_img = new ImageView(new Image(ClientApp.class.getResource("images/charlie.png").toExternalForm()));

        alice_img.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                updateClientMail(alice_img, "a@gmail.com");
            }
        });
        bob_img.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                updateClientMail(bob_img, "b@gmail.com");
            }
        });
        charlie_img.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                updateClientMail(charlie_img, "c@gmail.com");
            }
        });
    */
    }

    /*
    public void updateClientMail(ImageView v, String s) {
        System.out.println("CLICK");
        resetOpacity();
        v.setOpacity(1);
        clientEmailAddress = s;
    }
    */

    @FXML
    private void updateClientMail(MouseEvent event)
    {
        ImageView imgClicked = ((ImageView) event.getSource()); //yields complete string
        if(imgPrevious == null){
            imgPrevious = imgClicked;
        }
        resetOpacity(imgClicked);
        imgClicked.setOpacity(1);
        clientEmailAddress = getEmails(imgClicked.getId());
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
    public void login() {
        makeAlert("Login Completed!", "Welcome", clientEmailAddress);
    }

    public void makeAlert(String title, String head, String text){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(head);
        alert.setContentText(text);
        alert.showAndWait().ifPresent(rs -> {
            if (rs == ButtonType.OK) {
                System.out.println("Pressed OK.");
            }
        });
    }

    private void resetOpacity(ImageView imgCurrent){
        imgCurrent.setOpacity(0.5);
        imgPrevious.setOpacity(0.5);
    }


    public void setStage(Stage stage){
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {

            }
        });
    }
}
