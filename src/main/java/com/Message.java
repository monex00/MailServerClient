package com;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    private String messageType ="";
    private String message ="";
    private Email email = null;
    private ArrayList<Email> ems = null;

    public Message(String messageType, String message) {
        this.messageType = messageType;
        this.message = message;
    }
    public Message(String messageType, ArrayList<Email> ems) {
        this.messageType = messageType;
        this.ems = ems;
    }
    public Message(String messageType, String message, Email email) {
        this.messageType = messageType;
        this.message = message;
        this.email = email;
    }
    public Message(String messageType, Email email) {
        this.messageType = messageType;
        this.email = email;
    }
    public ArrayList<Email> getEms() {
        return ems;
    }

    public void setEms(ArrayList<Email> ems) {
        this.ems = ems;
    }
    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    @Override
    public String toString() {
        if (email != null){
            return "proj.Message{" +
                    "messageType='" + messageType + '\'' +
                    ", message='" + message + '\'' +
                    ", " + email.toFile() +
                    '}';
        }
        if(this.ems != null){
            return "proj.Message{" +
                    "messageType='" + messageType + '\'' +
                    ", message='" + message + '\'' +
                    ", ems "  + ems.toString() +
                    '}';
        }else{
            return "proj.Message{" +
                    "messageType='" + messageType + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }


    }
}
