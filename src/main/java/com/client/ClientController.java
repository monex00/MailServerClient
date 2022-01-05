package com.client;

import com.Email;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import com.model.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;


import java.io.IOException;
import java.util.ArrayList;

/**
 * Classe Controller 
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

    private ClientModel model;
    private Email selectedEmail;
    private Email emptyEmail;

    private Stage stage;


    public ClientController(String emailAddress, String imgPath, Stage stage){
        this.emailAddress = emailAddress;
        this.imgPath = imgPath;
        this.stage = stage;
    }

    @FXML
    public void initialize(){
        if( model != null){ throw new IllegalStateException("Model can only be initialized once"); }
        model = new ClientModel(emailAddress);
        model.getEmails();

        //checking if the image exists
        if(imgPath != null) {
            if (getClass().getResource("images/" + imgPath) != null) {
                imgIcon.setImage(new Image(getClass().getResource("images/" + imgPath).toString()));
            }
        }else{ //if the image doesn't exist, set the default image
            imgIcon.setImage(new Image(getClass().getResource("images/account_icon.png").toString()));
        }

        //status properties
        cleStatus.fillProperty().bind(model.statusColorProperty());
        lblStatus.textProperty().bind(model.statusTextProperty());

        //defining how the list of emails will be displayed
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
                                        if(item.getSender().equals(emailAddress)){
                                            v.setStyle("-fx-background-color: rgba(74,106,166,0.63); -fx-background-radius: 5;");
                                        }
                                        from.setText(item.getSender() + ": " + item.getSubject());
                                        date.setText(item.getDate().split("T")[0]);

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

        //binding tra lstEmails e inboxProperty
        lstEmails.itemsProperty().bind(model.inboxProperty());
        lstEmails.setOnMouseClicked(this::showSelectedEmail);
        lblUsername.textProperty().bind(model.emailAddressProperty());

        //popup-listener
        model.alertProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if(t1 != null) {
                    makePopup(t1);
                }
            }
        });

        this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                model.close();
            }
        });

        selectedEmail = null;
        emptyEmail = new Email("", new ArrayList<>(), "", "");
        updateDetailView(emptyEmail);
    }


    /**
     * Elimina la mail selezionata
     */
    @FXML
    protected void onDeleteButtonClick() {
        if(!model.statusTextProperty().getValue().equals("Online")) {
            if(!model.reconnect(false)){
                makeAlert("Error", "You are offline", "You can't delete an email while you are offline", Alert.AlertType.ERROR);
                return;
            }
        }
        model.deleteEmail(selectedEmail);
        updateDetailView(emptyEmail);
    }

    @FXML
    protected void onReplyButtonClick() {
        makeAlert("reply", "reply", "reply");
    }

    @FXML
    protected void onNewButtonClick() throws IOException {
        if(!model.statusTextProperty().getValue().equals("Online")) {
            if(!model.reconnect(true)){
                makeAlert("Error", "You are offline", "You can't send an email while you are offline", Alert.AlertType.ERROR);
                return;
            }
        }

        StartNew nm = new StartNew();
        nm.setModel(model);
        nm.start(new Stage());
    }

    @FXML
    protected void onRefreshPresssed(){
        if(!model.reconnect(true)) {
            makePopup("You are offline");
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


    public void makePopup(String message){
        Popup popup = new Popup();
        popup.setAutoFix(true);
        Label label = new Label(message);
        label.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        label.getStyleClass().add("popup");
        popup.getContent().add(label);
        popup.setAutoHide(true); //chiusura al click
        popup.setX(stage.getX() + (stage.getWidth()/2) - (popup.getWidth()/2));
        popup.setY(stage.getY() + stage.getHeight()/2 - popup.getHeight()/2);

        //if(stage.isFocused() && !popup.isShowing()){
        popup.show(stage);
        //}

        //Hide del pop-up dopo 3 secondi
        new Timeline(new KeyFrame(
                Duration.millis(3000),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent ae) {
                        popup.hide();
                    }
                })).play();
    }

    public void makeAlert(String title, String head, String text){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(head);
        //alert.setContentText(text);
        alert.showAndWait().ifPresent(rs -> {
            if (rs == ButtonType.OK) {
                System.out.println("Pressed OK.");
            }
        });
    }
}
