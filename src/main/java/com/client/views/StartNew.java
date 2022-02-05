package com.client.views;

import com.client.controllers.SendController;
import com.client.model.ClientModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class StartNew extends Application {

    private ClientModel model;
    private String to;
    private String subject;
    private String text;

    @Override
    public void start(Stage stage) throws IOException {
        if(model == null) {
            System.out.println("can't start");
            return;
        }

        URL clientUrl = ClientApp.class.getResource("send.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(clientUrl);

        //creating a controller instance manually to setting attributes before loading the fxml
        SendController controller = null;
        if(to != null && subject != null && text != null) { //in reply mode
            controller = new SendController(model, stage, to, subject, text);
        }else if(text != null && subject != null) { //forward mode
            controller = new SendController(model, stage, subject, text);
        }else{ //new message mode
            controller = new SendController(model, stage);
        }

        fxmlLoader.setController(controller);

        Scene scene = new Scene(fxmlLoader.load(), 400, 600);
        stage.setTitle("New Email");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

    }

    public void setModel(ClientModel model) {this.model = model;}

    public void setTo(String to) {this.to = to;}
    public void setSubject(String subject) {this.subject = subject;}
    public void setText(String text) {this.text = text;}

}
