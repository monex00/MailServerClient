package com.server;

import com.CONSTANTS;
import com.Message;
import com.server.model.ServerModel;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class ClientHandler implements Runnable {
    public static final ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private Socket socket = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    private String email = "";
    private ManageUsers manageUsers = null;
    private ManageEmails manageEmails = null;

    private ServerModel serverModel;


    public ClientHandler(Socket socket, ManageUsers manageUsers, ManageEmails manageEmails, ServerModel serverModel) {
        try {
            this.socket = socket;
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.serverModel = serverModel;
            this.email = ((Message)objectInputStream.readObject()).getMessage();
            this.manageUsers = manageUsers;
            this.manageEmails = manageEmails;

            //removing first client if max socket is reached
            synchronized (clientHandlers) {
                if (clientHandlers.size() == CONSTANTS.MAXSOCKETS) {
                    clientHandlers.get(0).sendMessage(new Message(CONSTANTS.DISCONNECT,""));
                    clientHandlers.get(0).closeConn();
                }
                clientHandlers.add(this);
                Platform.runLater(() -> {serverModel.appendLogText("TOTAL CONNECTED CLIENTS: " + clientHandlers.size());});
            }

            checkEmailIfNotExistAdd();
            Platform.runLater(() -> {serverModel.appendLogText("ADDED USER: " + this.email + " TO CLIENTHANDLERS");});
            System.out.println("ADDED USER: " + this.email + " TO CLIENTHANDLERS");

        }catch (IOException | ClassNotFoundException e){
            Platform.runLater(() -> { serverModel.appendLogText(e.toString() + " " + e.getMessage());});
            System.out.println(e.toString() + " " + e.getMessage());
            closeConn();
        }
    }

    /**
     * Set a timout connection if the user doesn't send a message for a certain amount of time
     * @return
     */
    public Timer disconnectUserIfNoActivity(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendMessage(new Message(CONSTANTS.DISCONNECT,""));
                closeConn();
                timer.cancel();
            }
        },  CONSTANTS.SECONDTOTIMEOUT * 1000);
        return timer;
    }

    private void checkEmailIfNotExistAdd() {
        if(!manageUsers.isEmailInSystem(this.email)){
            manageUsers.addEmail(this.email);
            manageUsers.addEmailToFile(this.email);
        }
    }

    /**
     * Wait for message and send the response
     */
    @Override
    public void run() {
        Message message;
        while(isConnected()){
            try {
                Timer t = disconnectUserIfNoActivity(); //start the timer
                message = (Message) objectInputStream.readObject();
                t.cancel(); //cancel the timer if the user sends a message resetting the timer
                checkMessage(message);
            }catch(IOException | ClassNotFoundException e) {
                closeConn();
                break;
            }
        }
    }

    /**
     * Check the message and send the response
     * if the type is MESSAGE, print it
     * if the type is EMAIL, store the email in manageEmails list and send it to the client if is online
     * if the type is GETEMAILS, send all the emails of a user to him
     * if the type is DELETEEMAIL, delete the email from the list
     * if the type is DISCONNECT, close the connection, and send a disconnect message to the client
     * @param message
     */
    private void checkMessage(Message message) {
        if(message.getMessageType().equalsIgnoreCase(CONSTANTS.MESSAGE)){
            Platform.runLater(() -> { serverModel.appendLogText("MESSAGGE FROM: " + this.email + "MESSAGE: " + message.getMessage());});
            System.out.println(message);
        }else if(message.getMessageType().equalsIgnoreCase(CONSTANTS.EMAIL)){
            if(this.manageEmails.isReceiverInSystem(message.getEmail())){
                this.manageEmails.addEmail(message.getEmail());
                notifyUserNewEmailReceived(message);
                //notify sender email sent
                sendMessage(new Message(CONSTANTS.EMAILSENT,CONSTANTS.EMAILSENTTEXT));
            }else{
                notifySenderReceiverNotFound();
            }
        }else if(message.getMessageType().equalsIgnoreCase(CONSTANTS.GETEMAILS)){
            sendMessage(new Message(CONSTANTS.GETEMAILS, manageEmails.getReceivedMyEmails(this.email)));
        }else if(message.getMessageType().equalsIgnoreCase(CONSTANTS.DELETEEMAIL)){
            this.manageEmails.deleteEmail(message.getMessage(),this.email);
        }else if(message.getMessageType().equalsIgnoreCase(CONSTANTS.DISCONNECT)){
            sendMessage(new Message(CONSTANTS.DISCONNECT,""));
            closeConn();
        }
    }

    private void notifySenderReceiverNotFound() {
        sendMessage(new Message(CONSTANTS.RECEIVERNOTFOUND,CONSTANTS.RECEIVERNOTFOUNDTEXT));
    }

    private void notifyUserNewEmailReceived(Message message) {
        synchronized (clientHandlers){
            ArrayList<String> strings = message.getEmail().getReceivers();
            for (String email :
                    strings) {
                for (int i = 0; i < clientHandlers.size(); i++) {
                    if(email.equals(clientHandlers.get(i).email)){
                        clientHandlers.get(i).sendMessage(new Message(CONSTANTS.NEWEMAIL,message.getEmail()));
                    }
                }
            }
        }
    }

    public synchronized void closeConn(){
        if(isConnected()){
            synchronized (clientHandlers){
                clientHandlers.remove(this);
            }

            Platform.runLater(() -> {serverModel.appendLogText("CLOSED CONNECTION WITH: " + this.email);});
            System.out.println("REMOVED USER WITH EMAIL: " + email);
            try {
                socket.close();
                if(objectInputStream != null) objectInputStream.close();
                if(objectOutputStream != null) objectOutputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public synchronized void closeSock(){
        if(isConnected()){
            Platform.runLater(() -> {serverModel.appendLogText("CLOSED CONNECTION WITH: " + this.email);});
            System.out.println("REMOVED USER WITH EMAIL: " + email);
            try {
                socket.close();
                if(objectInputStream != null) objectInputStream.close();
                if(objectOutputStream != null) objectOutputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(Message msg)  {
        if(isConnected()){
            try {
                objectOutputStream.writeObject(msg);
                objectOutputStream.flush();
            }catch (IOException e) {
                closeConn();
            }
        }
    }

    private boolean isConnected(){
        if (socket != null && socket.isConnected() && !socket.isClosed())
            return true;
        return false;
    }
}
