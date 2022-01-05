package com.client;

import com.Email;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.InputEvent;
import com.model.ClientModel;
import javafx.stage.Stage;


import java.io.IOException;

/**
 * Classe Controller
 */

public class SendController {

    @FXML
    private Label lblAddress;

    @FXML
    private TextArea txtMessage;

    @FXML
    private TextField txtSubject;

    @FXML
    private TextField txtTo;

    private final ClientModel model;
    private final Stage stage;

    public SendController(ClientModel model, Stage stage){
        this.model = model;
        this.stage = stage;
    }

    @FXML
    public void initialize(){
        lblAddress.textProperty().bind(model.emailAddressProperty());
    }

    public Alert makeAlert(String title, String head, String text, Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(head);
        alert.setContentText(text);
        alert.showAndWait();
        return alert;
    }

    @FXML
    protected void onBackButtonClick(InputEvent e) {
        Alert alert = makeAlert("Are you sure?", "Do you want to close?", "The email will be deleted", Alert.AlertType.CONFIRMATION);
        if(alert.getResult() == ButtonType.OK) {
            stage.close();
        }
    }

    @FXML
    protected void onSendButtonClick(InputEvent e) {
        if(!model.statusTextProperty().getValue().equals("Online")) {
            if(!model.reconnect(true)){
                makeAlert("Error", "You are offline", "You can't send an email while you are offline", Alert.AlertType.ERROR);
                return;
            }
        }

        String to = txtTo.getText();
        //check if the email is valid
        if(!Email.isValid(to)){
            makeAlert("Error", "Invalid email", "Please insert a valid email", Alert.AlertType.ERROR);
        }else{
            //Alert alert = makeAlert("Are you sure?", "Do you want to send this email?", "After sending you will not be able to undo this operation", Alert.AlertType.CONFIRMATION);

            model.receiversProperty().bind(txtTo.textProperty());
            model.subjectProperty().bind(txtSubject.textProperty());
            model.textProperty().bind(txtMessage.textProperty());
            model.sendEmail();
            stage.close();

        }
    }

}
