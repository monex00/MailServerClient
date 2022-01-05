package com.server;

import com.CONSTANTS;
import com.Email;
import com.server.Server;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import com.model.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * Classe Controller
 */

public class ServerController {

    @FXML
    private TextArea txt_log;


    private Server server;
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
        try {
            ServerSocket serverSocket = new ServerSocket(CONSTANTS.SERVERPORT);
            server = new Server(serverSocket);
            server.startServer();

        } catch (IOException e) {
            e.printStackTrace();
        }
        txt_log.textProperty().bind(server.logProperty());
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
        server.logProperty().set("");
    }


    public void makeAlert(String title, String head, String text){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(head);
        alert.setContentText(text);
        alert.showAndWait().ifPresent(rs -> {
            if (rs == ButtonType.OK) {
                System.out.println("Pressed OK.");
            }
        });
    }

}
