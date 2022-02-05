package com.server.controller;

import com.CONSTANTS;
import com.server.Server;
import com.server.model.ServerModel;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;


/**
 * Classe Controller
 */

public class ServerController {

    @FXML
    private TextArea txt_log;


    private Server server;
    private ServerModel serverModel;
    private Stage stage;

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public ServerController(Stage stage){
        this.stage = stage;
    }

    @FXML
    public void initialize(){
        serverModel = new ServerModel();
        try {
            ServerSocket serverSocket = new ServerSocket(CONSTANTS.SERVERPORT);
            server = new Server(serverSocket, serverModel);
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        txt_log.textProperty().bind(serverModel.logProperty());

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                server.closeServerSocket();
            }
        });
    }

    /**
     * Elimina la mail selezionata
     */
    @FXML
    protected void onCleanButtonClick() {
        System.out.println("clean");
        serverModel.clearLogText();
    }
}
