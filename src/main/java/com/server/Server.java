package com.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Server {
    private ServerSocket serverSocket = null;
    Semaphore semUser = null;
    Semaphore semEmail = null;

    ManageUsers manageUsers = null;
    ManageEmails manageEmails = null;
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        semUser = new Semaphore(1);
        semEmail = new Semaphore(1);
        manageUsers = new ManageUsers(semUser);
        manageEmails = new ManageEmails(semEmail,this.manageUsers);
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
    public void startServer() {
        try {
            while (!serverSocket.isClosed()){
                Socket socket = this.serverSocket.accept();
                System.out.println("New Client connected to Server");
                ClientHandler clientHandler = new ClientHandler(socket, manageUsers, manageEmails);
                Thread thread = new Thread(clientHandler); //TODO: utilizzare thread pool
                thread.start();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
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
