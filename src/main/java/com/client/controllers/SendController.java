package com.client.controllers;

import com.Email;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.InputEvent;
import com.client.model.ClientModel;
import javafx.stage.Stage;

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

    private String to;
    private String subject;
    private String message;


    public SendController(ClientModel model, Stage stage){
        this.model = model;
        this.stage = stage;
    }


    public SendController(ClientModel model, Stage stage, String to, String subject, String message){
        this.model = model;
        this.stage = stage;
        this.to = to;
        this.subject = subject;
        this.message = message;
    }

    public SendController(ClientModel model, Stage stage, String subject, String message){
        this.model = model;
        this.stage = stage;
        this.subject = subject;
        this.message = message;
    }

    @FXML
    public void initialize(){
        lblAddress.textProperty().bind(model.emailAddressProperty());

        //change the style of the text field if the email is valid or not
        txtTo.textProperty().addListener(event -> {
                txtTo.pseudoClassStateChanged( //pseudoClasses represent the state of a component (e.g. focused, hover, etc.)
                        PseudoClass.getPseudoClass("error"), //the pseudoClass to apply
                        !txtTo.getText().isEmpty() && !Email.isValid(txtTo.getText())); //whether the pseudoClass should be applied or not
                });

        //if in reply mode
        if(to != null && subject != null && message != null){
            txtTo.setText(to);
            txtTo.setDisable(true);
            txtSubject.setText(subject);
            String line = "----------------------------------------------------------";
            txtMessage.setText("\n\n" + to.split(";")[0] + " " + line + "\n" + message);
            txtMessage.positionCaret(0); //set the caret to the beginning of the text
        }else if (subject != null && message != null) { //if in forward mode
            txtSubject.setDisable(true);
            txtMessage.setDisable(true);
            txtSubject.setText(subject);
            txtMessage.setText(message);
        }
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

        if(!Email.isValid(txtTo.getText()) || txtTo.getText().equals("")) {
            makeAlert("Error", "Invalid email", "Please insert a valid email", Alert.AlertType.ERROR);
            return;
        }

        if (txtSubject.getText().isEmpty()){
            makeAlert("Error", "invalid subject", "You must enter a subject", Alert.AlertType.ERROR);
            return;
        }

        if (txtMessage.getText().isEmpty()){
            makeAlert("Error", "invalid message", "You must enter a message", Alert.AlertType.ERROR);
            return;
        }

        model.sendEmail(txtTo.getText(), txtSubject.getText(), txtMessage.getText());
        stage.close();
    }

}
