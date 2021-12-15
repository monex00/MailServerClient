package com.server;

import com.CONSTANTS;
import com.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private Socket socket = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    private String email = "";
    private ManageUsers manageUsers = null;
    private ManageEmails manageEmails = null;

    public ClientHandler(Socket socket,ManageUsers manageUsers,ManageEmails manageEmails) {
        try {
            this.socket = socket;
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());

            this.email = ((Message)objectInputStream.readObject()).getMessage();
            System.out.println("EMAIL: " + this.email);
            clientHandlers.add(this);
            this.manageUsers = manageUsers;
            this.manageEmails = manageEmails;
            checkEmailIfNotExistAdd();
            System.out.println("Added user with "+ this.email +" to clientHandlers");
        }catch (IOException | ClassNotFoundException e){
            System.out.println("ECCEZIONE");
            System.out.println(e.toString() + " " + e.getMessage());
            closeConn(socket,objectInputStream,objectOutputStream);

        }
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
        while(!socket.isClosed() && socket.isConnected()){
            try {
                message = (Message) objectInputStream.readObject();
                checkMessage(message);
            }catch(IOException | ClassNotFoundException e) {
                System.out.println(socket.isConnected());
                closeConn(socket,objectInputStream,objectOutputStream);
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
                this.manageEmails.addEmailToFile(message.getEmail());
                notifyUserNewEmailReceived(message);
                //notify sender email sent
            }else{
                notifySenderReceiverNotFound();
            }
        }else if(message.getMessageType().equalsIgnoreCase(CONSTANTS.EMAILUPDATE)){
            //do stuff
        }else if(message.getMessageType().equalsIgnoreCase(CONSTANTS.GETEMAILS)){
            sendMessage(new Message(CONSTANTS.GETEMAILS, manageEmails.getReceivedMyEmails(this.email)),objectOutputStream);
        }else if(message.getMessageType().equalsIgnoreCase(CONSTANTS.DELETEEMAIL)){
            this.manageEmails.deleteEmail(message.getMessage(),this.email);
        }else if(message.getMessageType().equalsIgnoreCase(CONSTANTS.DISCONNECT)){
            sendMessage(new Message(CONSTANTS.DISCONNECT,""), objectOutputStream);
            closeConn(socket,objectInputStream,objectOutputStream);
        }
    }

    private void notifySenderReceiverNotFound() {
        sendMessage(new Message(CONSTANTS.RECEIVERNOTFOUND,CONSTANTS.RECEIVERNOTFOUNDTEXT),this.objectOutputStream);
    }

    private void notifyUserNewEmailReceived(Message message) {
        ArrayList<String> strings = message.getEmail().getReceivers();
        for (String email :
                strings) {
            for (int i = 0; i < clientHandlers.size(); i++) {
                if(email.equals(clientHandlers.get(i).email)){
                    sendMessage(new Message(CONSTANTS.NEWEMAIL,CONSTANTS.NEWEMAILTEXT, message.getEmail()),clientHandlers.get(i).objectOutputStream);
                }
            }
        }
     }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        System.out.println(this.email +" had been disconnected");
    }

    public void closeConn(Socket socket, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream){
        removeClientHandler();
        try {
            socket.close();
            if(objectInputStream != null) objectInputStream.close();
            if( objectOutputStream != null) objectOutputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void sendMessage(Message msg , ObjectOutputStream objectOutputStream)  {
        try {
            objectOutputStream.writeObject(msg);
            objectOutputStream.flush();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
