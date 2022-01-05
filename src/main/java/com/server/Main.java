package com.server;

import com.CONSTANTS;

import java.io.IOException;
import java.net.ServerSocket;

public class Main  {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(CONSTANTS.SERVERPORT);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}


