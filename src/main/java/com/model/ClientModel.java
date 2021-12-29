package com.model;

import com.CONSTANTS;
import com.Email;
import com.Message;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Paint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Classe Client, conterrà la lista di mail che sarà il com.model
 */

public class ClientModel {
    private final ListProperty<Email> inbox;
    private final ObservableList<Email> inboxContent;
    private final StringProperty emailAddress;

    //send property
    private final StringProperty receivers;
    private final StringProperty subject;
    private final StringProperty text;

    //alert property
    private final StringProperty alertContent;

    //status property
    private final StringProperty statusText;
    private final ObjectProperty<Paint> statusColor;


    private ClientSocket clientSocket;

    /**
     * Costruttore della classe.
     *
     * @param emailAddress indirizzo email
     */
    //TODO: FARE CONTROLLI
    public ClientModel(String emailAddress) {
        this.inboxContent = FXCollections.observableList(new LinkedList<>());
        this.inbox = new SimpleListProperty<>();
        this.inbox.set(inboxContent);
        this.emailAddress = new SimpleStringProperty(emailAddress);

        //send property
        receivers = new SimpleStringProperty();
        text = new SimpleStringProperty();
        subject = new SimpleStringProperty();

        //alert property
        alertContent = new SimpleStringProperty();

        //status property
        statusText  = new SimpleStringProperty("Online");
        statusColor = new SimpleObjectProperty<>(Paint.valueOf("#00ff00"));

        clientSocket = new ClientSocket();
        getEmails();
    }

    /**
     * @return lista di email
     */
    public ListProperty<Email> inboxProperty() {
        return inbox;
    }

    /**
     * @return indirizzo email della casella postale
     */
    public StringProperty emailAddressProperty() {
        return emailAddress;
    }

    public StringProperty alertProperty(){ return alertContent; }

    public StringProperty receiversProperty(){ return receivers;}

    public StringProperty textProperty(){ return text;}

    public StringProperty subjectProperty(){ return subject;}

    public StringProperty statusTextProperty(){ return statusText;}

    public ObjectProperty<Paint> statusColorProperty(){ return statusColor;}


    /**
     * @return elimina l'email specificata
     */
    public void deleteEmail(Email email) {
        inboxContent.remove(email);
        clientSocket.sendMessageToServer(new Message(CONSTANTS.DELETEEMAIL,email.getId() + ""));
    }

    public void getEmails() {
        inboxContent.clear(); //clear email list
        clientSocket.sendMessageToServer(new Message(CONSTANTS.GETEMAILS, ""));
    }

    public void setEmails(ArrayList<Email> emails){
        if(emails != null){
            for (Email email : emails) {
                System.out.println(email.toFile());
                inboxContent.add(email);
            }
        }
    }

    public void sendEmail(){
        String[] re = this.receivers.getValue().split(";");
        ArrayList<String> r = new ArrayList<>(Arrays.asList(re));
        String email = this.emailAddress.getValue();
        String sub = this.subject.getValue();
        String text = this.text.getValue();
        clientSocket.sendMessageToServer(new Message(CONSTANTS.EMAIL, "", new Email(email, r ,sub, text)));
    }

    public ClientSocket getClientSocket() {
        return clientSocket;
    }

    public void close(){
        clientSocket.sendMessageToServer(new Message(CONSTANTS.DISCONNECT,""));
    }


    public class ClientSocket {
        private ObjectOutputStream objectOutputStream;
        private ObjectInputStream objectInputStream;
        private Socket socket = null;


        public ClientSocket() {
            try {
                socket = new Socket(CONSTANTS.SERVERIP, CONSTANTS.SERVERPORT);
                logOnServer();
            } catch (IOException e) {
                reconnect();
            }
        }

        public void logOnServer(){
            try {
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());

                objectOutputStream.writeObject(new Message(CONSTANTS.MESSAGE, emailAddress.getValue(), null));
                objectOutputStream.flush();
                listenForMessage();

            } catch (IOException e) {
                reconnect();
            }
        }


        public void listenForMessage(){
            new Thread(() -> {
                while(isConnected()){
                    try {
                        final Message message = (Message) objectInputStream.readObject(); //TODO: controllare final
                        switch (message.getMessageType()){
                            case CONSTANTS.GETEMAILS -> Platform.runLater( () -> setEmails(message.getEms()));
                            case CONSTANTS.NEWEMAIL -> Platform.runLater( () ->{
                                inboxContent.add(message.getEmail());
                                alertContent.set("new Email from: " + message.getEmail().getSender());
                            });
                            case CONSTANTS.DISCONNECT -> closeConnection();
                        }
                    } catch(IOException | ClassNotFoundException e) {
                        reconnect();
                        break;
                    }
                }
            }).start();
        }

        public void reconnect() {
            closeConnection();
            new Thread(() -> {
                while(!isConnected()){
                    try {
                        socket = new Socket(CONSTANTS.SERVERIP, CONSTANTS.SERVERPORT);
                        Platform.runLater(() -> {
                            alertContent.set("Riconnessione riuscita");
                            statusColor.set(Paint.valueOf("#00ff00"));
                            statusText.set("Online");
                        });
                        logOnServer();
                        Platform.runLater( () -> getEmails());
                    } catch (IOException ex) {
                        if(alertContent.getValue() == null || !alertContent.getValue().equals("Connessione al server non riuscita")) {
                            Platform.runLater(() -> {
                                alertContent.set("Connessione al server non riuscita");
                                statusColor.set(Paint.valueOf("#ff0000"));
                                statusText.set("Offline");
                            });
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        public void sendMessageToServer(Message msg){
            if(isConnected() && msg != null){
                try {
                    objectOutputStream.writeObject(msg);
                    objectOutputStream.flush();
                } catch (IOException e){
                    reconnect();
                }
            }
        }

        public void closeConnection(){
            if(isConnected()) {
                try {
                    if(objectInputStream != null) objectInputStream.close();
                    if(objectOutputStream != null) objectOutputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean isConnected(){
            if (socket != null && socket.isConnected() && !socket.isClosed())
                return true;
            return false;
        }

    }

}
