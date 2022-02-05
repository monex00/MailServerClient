package com.server;

import com.CONSTANTS;
import com.server.model.ServerModel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket = null;
    private final ServerModel serverModel;

    ManageUsers manageUsers = null;
    ManageEmails manageEmails = null;

    public Server(ServerSocket serverSocket , ServerModel serverModel) {
        this.serverSocket = serverSocket;
        manageUsers = new ManageUsers();
        manageEmails = new ManageEmails(this.manageUsers, serverModel);
        this.serverModel = serverModel;
    }
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
    public void startServer() {
        new Thread(() -> {
            ExecutorService executor = Executors.newFixedThreadPool(CONSTANTS.MAXTHREADS);
            try {
                while (!serverSocket.isClosed()){
                    Socket socket = this.serverSocket.accept();
                    serverModel.appendLogText("New Client connected to Server");
                    ClientHandler clientHandler = new ClientHandler(socket, manageUsers, manageEmails, serverModel);
                    executor.execute(clientHandler);
                }
            }catch (IOException e){
                System.exit(0);
            }
        }).start();

    }
    public void closeServerSocket() {
        this.manageEmails.addEmailToFile();
        synchronized (ClientHandler.clientHandlers){
            System.out.println("Closing Server");
            for (ClientHandler clientHandler : ClientHandler.clientHandlers) {
                clientHandler.closeSock();
            }
        }


        try {
            if(serverSocket != null){
                serverSocket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
