package com.server.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ServerModel {

    private StringProperty logProperty;

    public ServerModel() {
        logProperty = new SimpleStringProperty();
        logProperty.set("");
    }

    public StringProperty logProperty() {
        return logProperty;
    }

    public void setLogText(String log) {
        logProperty.set(log);
    }

    synchronized public void appendLogText(String log) {
        logProperty.set(logProperty.get() + log + "\n");
    }

    public void clearLogText() {
        logProperty.set("");
    }

}
