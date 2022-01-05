package com.server;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket = null;
    private StringProperty logProperty;

    ManageUsers manageUsers = null;
    ManageEmails manageEmails = null;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        manageUsers = new ManageUsers();
        manageEmails = new ManageEmails(this.manageUsers);
        logProperty = new SimpleStringProperty();
        logProperty.set("");
    }
    public StringProperty logProperty() { return logProperty; }
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
    public void startServer() {
        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()){
                    Socket socket = this.serverSocket.accept();
                    logProperty.set(logProperty.get() + "New Client connected to Server\n");
                    ClientHandler clientHandler = new ClientHandler(socket, manageUsers, manageEmails, logProperty);
                    Thread thread = new Thread(clientHandler); //TODO: utilizzare thread pool
                    thread.start();
                }
            }catch (IOException e){
                System.exit(0);
            }
        }).start();

    }
    public void closeServerSocket(){
        try {
            if(serverSocket != null){
                serverSocket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
