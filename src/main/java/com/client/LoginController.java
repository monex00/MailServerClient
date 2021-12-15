package com.client;

import com.Email;
import com.client.StartClient;
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

import java.io.IOException;
import java.util.ArrayList;

public class LoginController {
    private String clientEmailAddress="";

    @FXML
    private Button btnLogin; // TODO: nascondere il bottone se non è selezionato nessun utente

    private ImageView imgPrevious=null;

    public void initialize(){

    }

    @FXML
    private void updateClientMail(MouseEvent event)
    {
        ImageView imgClicked = ((ImageView) event.getSource()); //yields complete string
        if(imgPrevious == null){
            imgPrevious = imgClicked;
        }
        resetOpacity(imgClicked);
        clientEmailAddress = getEmails(imgClicked.getId());
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
        //makeAlert("Login Completed!", "Welcome", clientEmailAddress);
        StartClient client = new StartClient();
        client.setEmailAddress(clientEmailAddress);
        client.start(new Stage());
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
