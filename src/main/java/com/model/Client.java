package com.model;

import com.CONSTANTS;
import com.Email;
import com.Message;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Classe Client, conterrà la lista di mail che sarà il com.model
 */

public class Client {
    private final ListProperty<Email> inbox;
    private final ObservableList<Email> inboxContent;
    private final StringProperty emailAddress;

    private ClientSocket clientSocket;

    /**
     * Costruttore della classe.
     *
     * @param emailAddress indirizzo email
     */

    public Client(String emailAddress) {
        this.inboxContent = FXCollections.observableList(new LinkedList<>());
        this.inbox = new SimpleListProperty<>();
        this.inbox.set(inboxContent);
        this.emailAddress = new SimpleStringProperty(emailAddress);

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


    public void close(){
        clientSocket.sendMessageToServer(new Message(CONSTANTS.DISCONNECT,""));
    }


    private class ClientSocket {
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
                listenForMessage(); //TODO: aggiungere risposta server per sincronizzare il listenForMessage()

            } catch (IOException e) {
                reconnect();
            }
        }


        public void listenForMessage(){
            new Thread(() -> {
                while(isConnected()){
                    try {
                        final Message message = (Message) objectInputStream.readObject(); //TODO: controllare final
                        String type = message.getMessageType();
                        switch (type){
                            case CONSTANTS.GETEMAILS -> Platform.runLater( () -> setEmails(message.getEms() ));
                            case CONSTANTS.NEWEMAIL -> Platform.runLater( () -> inboxContent.add(message.getEmail())); //TODO: pop-up
                            case CONSTANTS.DISCONNECT -> closeConnection();
                        }
                    }catch(IOException | ClassNotFoundException e) {
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
                        logOnServer();
                        Platform.runLater( () ->getEmails());
                    } catch (IOException ex) {
                        try {
                            Thread.sleep(5000);
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
                }catch (IOException e){
                    reconnect();
                }
            }
        }

        public void closeConnection(){
            if(socket != null) {
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
