package com.server;

import com.CONSTANTS;
import com.Message;
import javafx.beans.property.StringProperty;

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

    private StringProperty logProperty;


    public ClientHandler(Socket socket, ManageUsers manageUsers, ManageEmails manageEmails, StringProperty logProperty) {
        try {
            this.socket = socket;
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());

            synchronized (clientHandlers) {
                if (clientHandlers.size() == CONSTANTS.MAXSOCKETS) {
                    clientHandlers.get(0).closeConn();
                }
            }

            this.logProperty = logProperty;

            this.email = ((Message)objectInputStream.readObject()).getMessage();
            clientHandlers.add(this);
            this.manageUsers = manageUsers;
            this.manageEmails = manageEmails;
            checkEmailIfNotExistAdd();
            logProperty.set(logProperty.get() + "ADDED USER: " + this.email + " TO CLIENTHANDLERS\n");
            System.out.println("ADDED USER: " + this.email + " TO CLIENTHANDLERS");

        }catch (IOException | ClassNotFoundException e){
            System.out.println("ECCEZIONE");
            System.out.println(e.toString() + " " + e.getMessage());
            closeConn();

        }
    }

    public Timer disconnectUserIfNoActivity(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendMessage(new Message(CONSTANTS.DISCONNECT,""));
                closeConn();
                timer.cancel();
            }
        },  10000);
        return timer;
    }

    private void checkEmailIfNotExistAdd() {
        if(!manageUsers.isEmailInSystem(this.email)){
            manageUsers.addEmail(this.email);
            manageUsers.addEmailToFile(this.email);
        }
    }

    @Override
    public void run() {
        Message message;
        while(isConnected()){
            try {
                Timer t = disconnectUserIfNoActivity();
                message = (Message) objectInputStream.readObject();
                t.cancel();
                checkMessage(message);
            }catch(IOException | ClassNotFoundException e) {
                closeConn();
                break;
            }
        }
    }

    private void checkMessage(Message message) {
        if(message.getMessageType().equalsIgnoreCase(CONSTANTS.MESSAGE)){
            System.out.println(message);
        }else if(message.getMessageType().equalsIgnoreCase(CONSTANTS.EMAIL)){
            if(this.manageEmails.isReceiverInSystem(message.getEmail())){
                this.manageEmails.addEmail(message.getEmail());
                this.manageEmails.addEmailToFile(); //rimuovere aggiunta manuale e mettere un timer
                notifyUserNewEmailReceived(message);
                //notify sender email sent
                sendMessage(new Message(CONSTANTS.EMAILSENT,CONSTANTS.EMAILSENTTEXT));
            }else{
                notifySenderReceiverNotFound();
            }
        }else if(message.getMessageType().equalsIgnoreCase(CONSTANTS.EMAILUPDATE)){
            //do stuff
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

    //TODO: Controllare se ha senso usare synchronized
    public synchronized void closeConn(){
        if(isConnected()){
            clientHandlers.remove(this);
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
        try {
            objectOutputStream.writeObject(msg);
            objectOutputStream.flush();
        }catch (IOException e) {
            closeConn();
        }
    }

    private boolean isConnected(){
        if (socket != null && socket.isConnected() && !socket.isClosed())
            return true;
        return false;
    }
}
