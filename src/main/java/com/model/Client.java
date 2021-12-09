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
import javafx.concurrent.Task;

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
        clientSocket.deleteEmailFromServer(email);
    }

    /**
     * genera email random da aggiungere alla lista di email, ese verranno mostrate nella ui
     */
    /*
    public void generateRandomEmails(int n) {
        String[] people = new String[] {"Paolo", "Alessandro", "Enrico", "Giulia", "Gaia", "Simone"};
        String[] subjects = new String[] {
                "Importante", "A proposito della nostra ultima conversazione", "Tanto va la gatta al lardo",
                "Non dimenticare...", "Domani scuola" };
        String[] texts = new String[] {
                "È necessario che ci parliamo di persona, per mail rischiamo sempre fraintendimenti",
                "Ricordati di comprare il latte tornando a casa",
                "L'appuntamento è per domani alle 9, ci vediamo al solito posto",
                "Ho sempre pensato valesse 42, tu sai di cosa parlo"
        };
        Random r = new Random();
        for (int i=0; i<n; i++) {
            Email email = new Email(
                    people[r.nextInt(people.length)],
                    List.of(people[r.nextInt(people.length)]),
                    subjects[r.nextInt(subjects.length)],
                    texts[r.nextInt(texts.length)]);
            inboxContent.add(email);
        }
    }
    */
    public void updateEmails() {
        clientSocket.getEmailsFromServer();
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
        clientSocket.closeConn();
    }


    private class ClientSocket {
        private ObjectOutputStream objectOutputStream;
        private ObjectInputStream objectInputStream;
        private Socket socket;

        public ClientSocket() {
            try {
                socket = new Socket(CONSTANTS.SERVERIP, CONSTANTS.SERVERPORT);
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());

                objectOutputStream.writeObject(new Message(CONSTANTS.MESSAGE, emailAddress.getValue(), null));
                objectOutputStream.flush();

                listenForMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public ArrayList<Email> getEmailsFromServer() {
            Message message = null;
            try {
                objectOutputStream.writeObject(new Message(CONSTANTS.GETEMAILS, ""));
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        public void deleteEmailFromServer(Email email) {
            try {
                objectOutputStream.writeObject(new Message(CONSTANTS.DELETEEMAIL,email.getId() + ""));
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void listenForMessage(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("ok");

                    while(socket.isConnected()){
                        try {
                            final Message message = (Message) objectInputStream.readObject();
                            System.out.println(message.toString());

                            if (message.getMessageType().equals(CONSTANTS.GETEMAILS)) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        setEmails(message.getEms());
                                    }
                                });
                            }else if(message.getMessageType().equals(CONSTANTS.NEWEMAIL)){
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        inboxContent.add(message.getEmail()); //TODO: pop-up
                                    }
                                });
                            }else if(message.getMessageType().equals(CONSTANTS.DISCONNECT)){
                                killClient();
                                break;
                            }
                        }catch(IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                            killClient();
                            break;
                        }
                    }
                }
            }).start();

        }

        //TODO: creare un sendMessage generico per comunicare con server
        private void sendMessageToServer(Message msg){
            try {
                objectOutputStream.writeObject(msg);
                objectOutputStream.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        //TODO: refactoring del codice
        private void closeConn() {
            try {
                objectOutputStream.writeObject(new Message(CONSTANTS.DISCONNECT,""));
                objectOutputStream.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        private void killClient(){
            if(socket != null) {
                try {
                    socket.close();
                    if(objectInputStream != null) objectInputStream.close();
                    if(objectOutputStream != null) objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    }

}
