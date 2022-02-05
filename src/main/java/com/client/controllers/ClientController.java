package com.client.controllers;

import com.Email;
import com.client.model.ClientModel;
import com.client.views.ClientApp;
import com.client.views.StartNew;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;


import java.io.IOException;
import java.util.ArrayList;

/**
 * Controller class for the client application.
 */

public class ClientController {

    private String emailAddress;
    private String imgPath;
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
    private TextArea txtEmailContent;

    @FXML
    private ListView<Email> lstEmails;

    @FXML
    private Label lblStatus;

    @FXML
    private Circle cleStatus;

    @FXML
    private Button senButton;

    @FXML
    private Button recButton;

    private ClientModel model;
    private Email selectedEmail;
    private Email emptyEmail;

    private final Stage stage;

    private boolean popupShowing = false;


    public ClientController(String emailAddress, String imgPath, Stage stage){
        this.emailAddress = emailAddress;
        this.imgPath = imgPath;
        this.stage = stage;
    }

    /**
     * Define how the list of emails is displayed dynamically.
     */
    private void initializeCellFactory(){
        lstEmails.setCellFactory(new Callback<ListView<Email>, ListCell<Email>>() {
            @Override
            public ListCell<Email> call(ListView<Email> list) {
                return new ListCell<Email>(){
                    @Override
                    protected void updateItem(Email item, boolean empty) {
                        super.updateItem(item, empty);
                        VBox v = new VBox();

                        Label from = new Label();
                        Label date = new Label();

                        if(item != null){
                            from.setText(item.getSender() + ": " + item.getSubject());

                            //formatting the date
                            String data = item.getDate().split("T")[0];
                            data = data.split("-")[2] + "/" + data.split("-")[1] + "/" + data.split("-")[0];

                            //formatting the hour
                            String hour = item.getDate().split("T")[1].split("\\.")[0];
                            hour = hour.split(":")[0] + ":" + hour.split(":")[1];

                            date.setText(hour + " - " + data);

                            //aligning the labels to the right
                            date.setMaxWidth(Double.MAX_VALUE);
                            date.setAlignment(Pos.CENTER_RIGHT);
                        }

                        //set the padding for the label from to be 5 from the left
                        from.setPadding(new Insets(0,0,0,5));
                        //set the padding for the label date to be 5 from the right
                        date.setPadding(new Insets(0,5,0,0));

                        from.setStyle("-fx-font-size: 12;");
                        date.setStyle("-fx-font-size: 12;");

                        v.getChildren().add(from);
                        v.getChildren().add(date);
                        setGraphic(v);
                    }
                };
            }
        });
    }

    /**
     * Bind the properties of the controller to the model.
     * Adding listeners for popups and the list of emails.
     */
    private void initializeModelProps(){
        //status properties
        cleStatus.fillProperty().bind(model.statusColorProperty());
        lblStatus.textProperty().bind(model.statusTextProperty());

        //binding tra lstEmails e inboxProperty
        lstEmails.itemsProperty().bind(model.inboxProperty());
        lstEmails.setOnMouseClicked(this::showSelectedEmail);
        lblUsername.textProperty().bind(model.emailAddressProperty());

        //popup-listener, listening for the popup label to be changed
        model.popupProperty().addListener((observableValue, s, t1) -> {
            if(t1 != null) {
                makePopup(t1);
            }
        });

        //list switch listener, used to switch from received/sent emails and vice versa when model decides
        model.listViewSwitchProperty().addListener((observableValue, aBoolean, t1) -> {
            if(t1) {
                onRecPressed();
            }else{
                onSenPressed();
            }
        });
    }

    /**
     * Initialize the view with img profile and empty email as default.
     */
    private void initializeView(){
        //checking if the image exists
        if(imgPath != null && ClientApp.class.getResource("images/" + imgPath) != null)
            imgIcon.setImage(new Image(ClientApp.class.getResource("images/" + imgPath).toString()));
        else //if the image doesn't exist, set the default image
            imgIcon.setImage(new Image(ClientApp.class.getResource("images/account_icon.png").toString()));

        //handling the closed event
        this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                model.close();
            }
        });

        onRecPressed(); // set the button receiver to be pressed as default
        selectedEmail = null;
        emptyEmail = new Email("", new ArrayList<>(), "", "");
        updateDetailView(emptyEmail);
    }

    @FXML
    public void initialize(){
        if(model != null){ throw new IllegalStateException("Model can only be initialized once"); }
        model = new ClientModel(emailAddress);
        model.getEmails();

        initializeModelProps();

        initializeView();

        initializeCellFactory();
    }


    /**
     * Delete the selected email from the list and from the server
     */
    @FXML
    protected void onDeleteButtonClick() {
        if(selectedEmail == null) {
            makeAlert("Error", "No email selected", "You must select the email you want to delete", Alert.AlertType.ERROR);
            return;
        }

        if(checkStatusAndReconnect("You can't delete an email while you are offline")) return;

        model.deleteEmail(selectedEmail);
        updateDetailView(emptyEmail);
    }

    /**
    * Check the status of the connection and reconnect if needed
    * @param message the message to show if the connection is offline
    * @return true if the connection is offline and the thread "reconnect" has started, false otherwise
    */
    private boolean checkStatusAndReconnect(String message){
        if(!model.statusTextProperty().getValue().equals("Online")) {
            if(!model.reconnect(false)){
                makeAlert("Error", "You are offline", message, Alert.AlertType.ERROR);
                return true;
            }
        }
        return false;
    }

    /**
     * Reply to all the receivers of the selected email
     * if the email is a sent email, sender is not included in the receivers
     * if the email is a received email, sender is included in the receivers
     */
    @FXML
    protected void onReplyAllButtonClick() throws IOException{
        if(selectedEmail == null) {
            makeAlert("Error", "No email selected", "You must select the email you want to reply", Alert.AlertType.ERROR);
            return;
        }

        if(selectedEmail.getReceivers().size() == 1){
            onReplyButtonClick();
            return;
        }

        if(checkStatusAndReconnect("You can't reply an email while you are offline")) return;

        StartNew nm = new StartNew();
        nm.setModel(model);
        String to = "";

        for (String s : selectedEmail.getReceivers()) {
            if(!s.equals(model.emailAddressProperty().getValue())) {
                to += s + ";";
            }
        }

        if(!selectedEmail.getSender().equals(model.emailAddressProperty().getValue())) { // if the email is not sent by the user
            nm.setTo(selectedEmail.getSender() + ";" + to);
        }else{ // if the email is sent by the user
            nm.setTo(to);
        }


        if(selectedEmail.getSubject().contains("RE:")) {
            nm.setSubject(selectedEmail.getSubject());
        }else{
            nm.setSubject("RE:" + selectedEmail.getSubject());
        }

        nm.setText(selectedEmail.getMessage());
        nm.start(new Stage());
    }

    /**
     * Forward the selected email
     * @throws IOException
     */
    @FXML
    protected void onForwardClicked() throws IOException{
        if(selectedEmail == null) {
            makeAlert("Error", "No email selected", "You must select the email you want to forward", Alert.AlertType.ERROR);
            return;
        }
        System.out.println("OK");
        if(checkStatusAndReconnect("You can't forward an email while you are offline")) return;

        StartNew nm = new StartNew();
        nm.setModel(model);

        nm.setText(selectedEmail.getMessage());
        nm.setSubject(selectedEmail.getSubject());
        nm.start(new Stage());

    }

    /**
     * Reply to the sender of the selected email
     * if the email is a sent email, sender is excluded and the first receiver is the sender
     * if the email is a received email, sender is the receiver
     */
    @FXML
    protected void onReplyButtonClick() throws IOException {
        if(selectedEmail == null) {
            makeAlert("Error", "No email selected", "You must select the email you want to reply", Alert.AlertType.ERROR);
            return;
        }

        if(checkStatusAndReconnect("You can't reply an email while you are offline")) return;

        StartNew nm = new StartNew();
        nm.setModel(model);

        if(!selectedEmail.getSender().equals(model.emailAddressProperty().getValue())) { // if the email is not sent by the user
            nm.setTo(selectedEmail.getSender());
        }else{ // if the email is sent by the user then the receiver is the sender
            nm.setTo(selectedEmail.getReceivers().get(0));
        }


        if(selectedEmail.getSubject().contains("RE:")) {
            nm.setSubject(selectedEmail.getSubject());
        }else{
            nm.setSubject("RE:" + selectedEmail.getSubject());
        }

        nm.setText(selectedEmail.getMessage());
        nm.start(new Stage());

    }

    /**
     * Start a new email window
     */
    @FXML
    protected void onNewButtonClick() throws IOException {
        if(checkStatusAndReconnect("You can't send an email while you are offline")) return;

        StartNew nm = new StartNew();
        nm.setModel(model);
        nm.start(new Stage());
    }

    /**
     * Refresh the list of emails reconnecting to the server
     */
    @FXML
    protected void onRefreshPressed(){
        if(!model.reconnect(true)) {
            makePopup("You are offline");
        }
    }

    /**
     * Switch the list view to the sent emails
     */
    @FXML
    protected void onSenPressed(){
        senButton.setStyle("-fx-opacity: 1;");
        recButton.setStyle("-fx-opacity: 0.5;");
        model.listViewSwitchProperty().set(false); // switch to sent emails in model
    }

    /**
     * Switch the list view to the received emails
     */
    @FXML
    protected void onRecPressed(){
        recButton.setStyle("-fx-opacity: 1;");
        senButton.setStyle("-fx-opacity: 0.5;");
        model.listViewSwitchProperty().set(true); // switch to received emails in model
    }

    /**
     * Create a new alert
     * @param title title of the alert
     * @param head header of the alert
     * @param text content of the alert
     * @param type type of the alert
     * @return the alert
     */
    public Alert makeAlert(String title, String head, String text, Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(head);
        alert.setContentText(text);
        alert.showAndWait();
        return alert;
    }

     /**
     * Show in detail the selected email
     */
    protected void showSelectedEmail(MouseEvent mouseEvent) {
        Email email = lstEmails.getSelectionModel().getSelectedItem();
        selectedEmail = email;
        updateDetailView(email);
    }

     /**
     * Update the detail view of the selected email
      * @param email the selected email
     */
    protected void updateDetailView(Email email) {
        if(email != null) {
            lblFrom.setText(email.getSender());
            lblTo.setText(String.join(", ", email.getReceivers()));
            lblSubject.setText(email.getSubject());
            txtEmailContent.setText(email.getMessage());
        }
    }

    /**
     * Create a popup that automatically closes after a 3 seconds
     * @param message
     */
    public void makePopup(String message){

        Popup popup = new Popup();
        popup.setAutoFix(true);
        Label label = new Label(message);
        label.getStylesheets().add(ClientApp.class.getResource("styles.css").toExternalForm());
        label.getStyleClass().add("popup");
        popup.getContent().add(label);
        popup.setAutoHide(true); // close the popup clicking
        popup.setX(stage.getX() + (stage.getWidth()/2) - (popup.getWidth()/2));
        popup.setY(stage.getY() + stage.getHeight()/2 - popup.getHeight()/2);

        //if(stage.isFocused() && !popup.isShowing()){
        popup.show(stage);
        //}
        popupShowing = true;

        //Hide popup after 3 seconds
        new Timeline(new KeyFrame(
                Duration.millis(3000),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent ae) {
                        popupShowing = false;
                        popup.hide();
                    }
                })).play();
    }


}
