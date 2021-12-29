package com.client;

import com.model.ClientModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class StartNew extends Application {

    ClientModel model;
    @Override
    public void start(Stage stage) throws IOException {
        if(model == null) {
            System.out.println("can't start");
            return;
        }

        URL clientUrl = ClientApp.class.getResource("send.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(clientUrl);

        //creating a controller instance manually to setting attributes before loading the fxml
        SendController controller = new SendController(model, stage);
        fxmlLoader.setController(controller);

        Scene scene = new Scene(fxmlLoader.load(), 400, 600);
        stage.setTitle("New Email");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

    }

    public void setModel(ClientModel model) {this.model = model;}
}
