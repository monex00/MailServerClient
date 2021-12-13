package com.client;

import com.Email;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import com.model.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;


import java.util.ArrayList;

/**
 * Classe Controller 
 */

public class ClientController {

    private String emailAddress;
    @FXML
    private Label lblFrom;

    @FXML
    private Label lblTo;

    @FXML
    private Label lblSubject;

    @FXML
    private Label lblUsername;

    @FXML
    private ImageView imgIcon;

    @FXML
    private TextArea txtEmailContent; //contenuto email sulla destra

    @FXML
    private ListView<Email> lstEmails; //lista di email sulla sinistra

    private Client model;
    private Email selectedEmail;
    private Email emptyEmail;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @FXML
    public void initialize(){

        Platform.runLater(() -> { // Bisogna metterlo per evitare che la mail sia nulla
            if (this.model != null)
                throw new IllegalStateException("Model can only be initialized once");
            //istanza nuovo client
            model = new Client(emailAddress);
            model.updateEmails();
            selectedEmail = null;
            String imgName = "alice.png";
            if(emailAddress.equals("b@gmail.com")){
                imgName="bob.png";
            }if(emailAddress.equals("c@gmail.com")){
                imgName="charlie.png";
            }
            Image image = new Image(getClass().getResource("images/"+imgName).toExternalForm());
            imgIcon.setImage(image);
            //binding tra lstEmails e inboxProperty
            lstEmails.itemsProperty().bind(model.inboxProperty());
            lstEmails.setOnMouseClicked(this::showSelectedEmail);
            lblUsername.textProperty().bind(model.emailAddressProperty());

            emptyEmail = new Email("", new ArrayList<>(), "", "");
            updateDetailView(emptyEmail);
        });


    }


    /**
     * Elimina la mail selezionata
     */
    @FXML
    protected void onDeleteButtonClick() {
        model.deleteEmail(selectedEmail);
        updateDetailView(emptyEmail);
    }

    //Gestione chiusura window
    public void setStage(Stage stage){
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                model.close();
            }
        });
    }

     /**
     * Mostra la mail selezionata nella vista
     */
    protected void showSelectedEmail(MouseEvent mouseEvent) {
        Email email = lstEmails.getSelectionModel().getSelectedItem();

        selectedEmail = email;
        updateDetailView(email);
    }

     /**
     * Aggiorna la vista con la mail selezionata
     */
    protected void updateDetailView(Email email) {
        if(email != null) {
            lblFrom.setText(email.getSender());
            lblTo.setText(String.join(", ", email.getReceivers()));
            lblSubject.setText(email.getSubject());
            txtEmailContent.setText(email.getMessage());
        }
    }

}
