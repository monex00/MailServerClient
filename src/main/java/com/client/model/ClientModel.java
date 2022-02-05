package com.client.model;

import com.CONSTANTS;
import com.Email;
import com.Message;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Paint;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

/**
 * Classe Client, conterrà la lista di mail che sarà il com.model
 */

public class ClientModel {
    private final ListProperty<Email> inbox;
    private final ObservableList<Email> inboxContent;
    private final StringProperty emailAddress;
    private final BooleanProperty listViewSwitch; //true = received, false = sent
    private int oldEmailsSize;

    private ArrayList<Email> receivedEmails;
    private ArrayList<Email> sentEmails;

    //alert property
    private final StringProperty popupProperty;

    //status property
    private final StringProperty statusText; //status text: Online, Offline, etc.
    private final ObjectProperty<Paint> statusColor; //circle status color

    private final ClientSocket clientSocket;

    private Email toAdd; //email to add to inbox in case of successful send

    public ClientModel(String emailAddress) {
        this.inboxContent = FXCollections.observableList(new LinkedList<>());
        this.inbox = new SimpleListProperty<>();
        this.inbox.set(inboxContent);
        this.emailAddress = new SimpleStringProperty(emailAddress);

        this.sentEmails = new ArrayList<>();
        this.receivedEmails = new ArrayList<>();

        //alert property
        popupProperty = new SimpleStringProperty();

        //status property
        statusText  = new SimpleStringProperty("Online");
        statusColor = new SimpleObjectProperty<>(Paint.valueOf("#00ff00"));

        clientSocket = new ClientSocket();
        oldEmailsSize = -1; //set to -1 to prevent doing new email popup at the start

        listViewSwitch = new SimpleBooleanProperty(true); //set to true to show received emails
        listViewSwitch.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                //concatenate the two lists sent and received
                ArrayList<Email> allEmails = new ArrayList<>();
                allEmails.addAll(sentEmails);
                allEmails.addAll(receivedEmails);
                addEmails(new ArrayList<>(allEmails));
            }
        });
    }

    public ListProperty<Email> inboxProperty() {
        return inbox;
    }

    public StringProperty emailAddressProperty() {
        return emailAddress;
    }

    public StringProperty popupProperty(){ return popupProperty; }

    public StringProperty statusTextProperty(){ return statusText;}

    public ObjectProperty<Paint> statusColorProperty(){ return statusColor;}

    public BooleanProperty listViewSwitchProperty(){ return listViewSwitch;}

    public ClientSocket getClientSocket() {
        return clientSocket;
    }


    /**
    * Delete email from the list and send to server to delete it
     * @param email
    */
    public void deleteEmail(Email email) {
        if(email.getSender().equals(emailAddress.get())) {
            sentEmails.remove(email);
        }else{
            receivedEmails.remove(email);
        }
        inboxContent.remove(email);
        clientSocket.sendMessageToServer(new Message(CONSTANTS.DELETEEMAIL,email.getId() + ""));
    }

    /**
     * Get emails from server
     */
    public void getEmails() {
        //inboxContent.clear(); //clear email list
        clientSocket.sendMessageToServer(new Message(CONSTANTS.GETEMAILS, ""));
    }

    /**
     * Add emails to the list
     * @param emails
     */
    public void addEmails(ArrayList<Email> emails){
        if(oldEmailsSize != -1){
            oldEmailsSize = receivedEmails.size(); //save the old emails size to compare it with the new ones and establish if there are new emails
        }

        sentEmails.clear();
        receivedEmails.clear();

        for (Email email : emails) {
            if(email.getSender().equals(emailAddress.get())) {
                if (!email.isSenderDeleted())  sentEmails.add(email);
            }else{
                if (!email.isReceiverDeleted(emailAddress.get())) receivedEmails.add(email);
            }
        }

        if(oldEmailsSize!=-1 && oldEmailsSize != receivedEmails.size()){ //if there are new emails and the emails are not the emails loaded at the start
            popupProperty.set(CONSTANTS.GENERICNEWEMAILTEXT);
            popupProperty.set(null); //reset popup to trigger the listener next time
            listViewSwitch.set(true);
        }else
            oldEmailsSize = 0; //after getting and setting the first emails, set oldEmailsSize to 0

        showEmails();
    }

    /**
     * add 1 email to the list
     * @param email
     */
    public void addEmail(Email email){
        if(email.getSender().equals(emailAddress.get())) {
            sentEmails.add(0,email);
        }else
            receivedEmails.add(0, email);
        showEmails();
    }

    /**
     * Show emails in the list in base of the listViewSwitch
     */
    public void showEmails(){
        inboxContent.clear();
        if(listViewSwitch.get())
            inboxContent.addAll(receivedEmails);
        else
            inboxContent.addAll(sentEmails);
    }

    /**
     * reconnect to server
     * @param updateEmailList if true, update the list of emails
     */
    public boolean reconnect(boolean updateEmailList){
        if(clientSocket.isConnected()){
            return true;
        }

        try {
            clientSocket.connectionSeq();
            if(updateEmailList)
                getEmails();

            changeStatus("Online");
            return true;
        } catch (IOException e) {
            clientSocket.attemptToReconnect();
            System.out.println("Error: IOException in reconnect");
            return false;
        }
    }

    /**
     * Change the status of the client
     * @param status
     */
    public void changeStatus(String status){
        switch (status) {
            case "Online" -> statusColor.set(Paint.valueOf(CONSTANTS.GREEN));
            case "Offline" -> statusColor.set(Paint.valueOf(CONSTANTS.RED));
            case "Inactive" -> statusColor.set(Paint.valueOf(CONSTANTS.YELLOW));
        }
        statusText.set(status);
    }

    /**
     * Get email fields from the properties and send to server to add it
     */
    public void sendEmail(String to, String subject, String text) {
        String[] re = to.split(";");
        ArrayList<String> r = new ArrayList<>(Arrays.asList(re));
        toAdd = new Email(this.emailAddress.getValue(), r, subject, text);
        clientSocket.sendMessageToServer(new Message(CONSTANTS.EMAIL, "", toAdd));
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

        private boolean attempingToReconnect = false;

        public ClientSocket() {
            try {
                connectionSeq();
            } catch (IOException e) {
               attemptToReconnect();
                System.out.println("Error: IOException in ClientSocket");
            }
        }

        /**
         * Connect to server, log on it and listen for messages
         * @throws IOException
         */
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
                attemptToReconnect();
                System.out.println("Error: IOException in logOnServer");
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
                        Object o = objectInputStream.readObject();
                        if (!(o instanceof Message)){
                            System.out.println("Error: unknown message type");
                            return;
                        }

                        Message message = (Message) o;
                        switch (message.getMessageType()){
                            case CONSTANTS.GETEMAILS -> Platform.runLater( () -> addEmails(message.getEms()));
                            case CONSTANTS.NEWEMAIL -> Platform.runLater( () -> {
                                addEmail(message.getEmail());
                                popupProperty.set(CONSTANTS.NEWEMAILTEXT +  message.getEmail().getSender());
                                popupProperty.set(null); //reset popup to trigger the listener next time
                                listViewSwitch.set(true); //switch list view on received emails
                            });
                            case CONSTANTS.EMAILSENT -> Platform.runLater( () -> {
                                addEmail(toAdd);
                                toAdd=null;
                                popupProperty.set(CONSTANTS.EMAILSENTTEXT);
                                popupProperty.set(null); //reset popup to trigger the listener next time
                                listViewSwitch.set(false); //switch list view on sent emails
                            });
                            case CONSTANTS.RECEIVERNOTFOUND -> Platform.runLater( () -> {
                                popupProperty.set(CONSTANTS.RECEIVERNOTFOUNDTEXT);
                                popupProperty.set(null); //reset popup to trigger the listener next time
                            });
                            case CONSTANTS.DISCONNECT ->{
                                closeConnection();
                                Platform.runLater( () -> {
                                    changeStatus("Inactive");
                                });
                            }
                        }
                    } catch(IOException e) {
                        attemptToReconnect();
                        System.out.println("Error: IOException in listenForMessage");
                        break;
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        /**
         * a Timer that start a TimerTask that try to reconnect to the server every 5 seconds
         *  if the connection is established the timer will be cancelled, and it will pop-up a message to the user
         */
        public void attemptToReconnect(){
            if(!attempingToReconnect){
                attempingToReconnect = true;
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("attempt to reconnect");
                        try {
                            closeConnection();
                            connectionSeq();
                            attempingToReconnect = false;
                            t.cancel(); //cancel the timer if the connection is established
                            Platform.runLater(() -> {
                                popupProperty.set(CONSTANTS.CONNESSIONSUCCESSTEXT);
                                popupProperty.set(null); //reset popup to trigger the listener next time
                                changeStatus("Online");
                                getEmails();
                            });
                        } catch (IOException ex) {
                            //if connection is not established when the client start or if the popup didn't show yet
                            //show the popup and change the status to offline
                            //else do nothing (try to reconnect again after 5 seconds)
                            System.out.println(statusText.getValue());
                            if (!statusText.getValue().equals("Inactive") && (popupProperty.getValue() == null || !popupProperty.getValue().equals(CONSTANTS.CONNESSIONERRORTEXT))) {
                                Platform.runLater(() -> {
                                    popupProperty.set(CONSTANTS.CONNESSIONERRORTEXT);
                                });
                            }
                            changeStatus("Offline");
                        }
                    }
                },  0, 5000);
            }
        }

        /**
         * Send message to the server
         */
        public void sendMessageToServer(Message msg){
            if(isConnected() && msg != null){
                try {
                    objectOutputStream.writeObject(msg);
                    objectOutputStream.flush();
                } catch (IOException e){
                    attemptToReconnect();
                    System.out.println("Error: IOException in sendMessageToServer");
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
