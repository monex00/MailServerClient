package com.client;

import com.Email;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import com.model.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.IOException;

/**
 * Classe Controller
 */

public class SendController {

    private String emailAddress;

    @FXML
    private Label lblAddress;

    @FXML
    private TextArea txtMessage;

    @FXML
    private TextField txtSubject;

    @FXML
    private TextField txtTo;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @FXML
    public void initialize(){
        Platform.runLater(() -> { // Bisogna metterlo per evitare che la mail sia nulla
            lblAddress.setText(emailAddress);
        });
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

    @FXML
    protected void onBackButtonClick(InputEvent e) {
        makeAlert("Are you sure?", "You wat to close?", "Really?");
        closeWindow(e);
    }

    @FXML
    protected void onSendButtonClick(InputEvent e) {
        makeAlert(txtSubject.getText(), "To : "+txtTo.getText(), txtMessage.getText());
        closeWindow(e);
    }

    private void closeWindow(InputEvent e){
        final Node source = (Node) e.getSource();
        final Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    public void setStage(Stage stage) {

    }
}
