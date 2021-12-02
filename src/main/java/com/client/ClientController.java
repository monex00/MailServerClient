package com.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import model.*;


import java.util.List;

/**
 * Classe Controller 
 */

public class ClientController {
    @FXML
    private Label lblFrom;

    @FXML
    private Label lblTo;

    @FXML
    private Label lblSubject;

    @FXML
    private Label lblUsername;

    @FXML
    private TextArea txtEmailContent;

    @FXML
    private ListView<Email> lstEmails;

    private Client model;
    private Email selectedEmail;
    private Email emptyEmail;

    @FXML
    public void initialize(){
        if (this.model != null)
            throw new IllegalStateException("Model can only be initialized once");
        //istanza nuovo client
        model = new Client("mickeymouse@disney.com");
        model.generateRandomEmails(10);

        selectedEmail = null;

        //binding tra lstEmails e inboxProperty
        lstEmails.itemsProperty().bind(model.inboxProperty());
        lstEmails.setOnMouseClicked(this::showSelectedEmail);
        lblUsername.textProperty().bind(model.emailAddressProperty());

        emptyEmail = new Email("", List.of(""), "", "");

        updateDetailView(emptyEmail);
    }

    /**
     * Elimina la mail selezionata
     */
    @FXML
    protected void onDeleteButtonClick() {
        model.deleteEmail(selectedEmail);
        updateDetailView(emptyEmail);
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
            txtEmailContent.setText(email.getText());
        }
    }

}
