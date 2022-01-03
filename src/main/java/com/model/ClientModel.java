package com.model;

import com.CONSTANTS;
import com.Email;
import com.Message;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Paint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final StringProperty popupProperty;

    //status property
    private final StringProperty statusText;
    private final ObjectProperty<Paint> statusColor;

    private final ClientSocket clientSocket;

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
        popupProperty = new SimpleStringProperty();

        //status property
        statusText  = new SimpleStringProperty("Online");
        statusColor = new SimpleObjectProperty<>(Paint.valueOf("#00ff00"));

        clientSocket = new ClientSocket();

    }


    public ListProperty<Email> inboxProperty() {
        return inbox;
    }

    public StringProperty emailAddressProperty() {
        return emailAddress;
    }

    public StringProperty alertProperty(){ return popupProperty; }

    public StringProperty receiversProperty(){ return receivers;}

    public StringProperty textProperty(){ return text;}

    public StringProperty subjectProperty(){ return subject;}

    public StringProperty statusTextProperty(){ return statusText;}

    public ObjectProperty<Paint> statusColorProperty(){ return statusColor;}

    public ClientSocket getClientSocket() {
        return clientSocket;
    }


    /**
    * Delete email from the list and send to server to delete it
    */
    public void deleteEmail(Email email) {
        inboxContent.remove(email);
        clientSocket.sendMessageToServer(new Message(CONSTANTS.DELETEEMAIL,email.getId() + ""));
    }

    /**
     * Get emails from server
     */
    public void getEmails() {
        inboxContent.clear(); //clear email list
        clientSocket.sendMessageToServer(new Message(CONSTANTS.GETEMAILS, ""));
    }

    /**
     * Add emails to the list
     */
    public void setEmails(ArrayList<Email> emails){
        if(emails != null){
            for (Email email : emails) {
                System.out.println(email.toFile());
                inboxContent.add(email);
            }
        }
    }

    /**
     * Get email fields from the properties and send to server to add it
     */
    public void sendEmail(){
        String[] re = this.receivers.getValue().split(";");
        ArrayList<String> r = new ArrayList<>(Arrays.asList(re));
        String email = this.emailAddress.getValue();
        String sub = this.subject.getValue();
        String text = this.text.getValue();
        clientSocket.sendMessageToServer(new Message(CONSTANTS.EMAIL, "", new Email(email, r ,sub, text)));
    }


    /**
     * Close connection with server
     */
    public void close(){
        clientSocket.sendMessageToServer(new Message(CONSTANTS.DISCONNECT,""));
    }

    /**
     * Inner class ClientSocket, used to communicate with server
     */
    public class ClientSocket {
        private ObjectOutputStream objectOutputStream;
        private ObjectInputStream objectInputStream;
        private Socket socket = null;


        public ClientSocket() {
            try {
                connectionSeq();
            } catch (IOException e) {
               reconnect();
            }
        }

        public void connectionSeq() throws IOException {
            socket = new Socket(CONSTANTS.SERVERIP, CONSTANTS.SERVERPORT);
            logOnServer();
            listenForMessage();
        }

        /**
         * Send the email address to the server to initialize the connection
         */
        public void logOnServer(){
            try {
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());

                objectOutputStream.writeObject(new Message(CONSTANTS.MESSAGE, emailAddress.getValue(), null));
                objectOutputStream.flush();
            } catch (IOException e) {
                reconnect();
            }
        }

        /**
         * A Thread that Listen for message from server
         *  if the message is a getEmail message, it will add the emails to the list
         *  if the message is a new email, add it to the list and notify the user with popup
         *  if the message is a disconnect message, close the connection
         */
        public void listenForMessage(){
            new Thread(() -> {
                while(isConnected()){
                    try {
                        final Message message = (Message) objectInputStream.readObject(); //TODO: controllare final
                        switch (message.getMessageType()){
                            case CONSTANTS.GETEMAILS -> Platform.runLater( () -> setEmails(message.getEms()));
                            case CONSTANTS.NEWEMAIL -> Platform.runLater( () -> {
                                inboxContent.add(message.getEmail());
                                popupProperty.set(CONSTANTS.NEWEMAILTEXT +  message.getEmail().getSender());
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

        /**
         * a Thread that try to reconnect to the server every 5 seconds
         *  if the connection is established, it will popup a message to the user
         */
        public void reconnect() {
            closeConnection();
            new Thread(() -> {
                while(!isConnected()){
                    try {
                        connectionSeq();
                        Platform.runLater(() -> {
                            popupProperty.set(CONSTANTS.CONNESSIONSUCCESSTEXT);
                            statusColor.set(Paint.valueOf(CONSTANTS.GREEN));
                            statusText.set("Online");
                            getEmails();
                        });
                    } catch (IOException ex) {
                        if(popupProperty.getValue() == null || !popupProperty.getValue().equals(CONSTANTS.CONNESSIONERRORTEXT)) {
                            Platform.runLater(() -> {
                                popupProperty.set(CONSTANTS.CONNESSIONERRORTEXT);
                                statusColor.set(Paint.valueOf(CONSTANTS.RED));
                                statusText.set("Offline");
                            });
                        }

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        /**
         * Send mesage to the server
         */
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

        /**
         * Close the connection
         */
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

        /**
         * Check if the connection is established
         */
        private boolean isConnected(){
            if (socket != null && socket.isConnected() && !socket.isClosed())
                return true;
            return false;
        }

    }

}
