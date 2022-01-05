package com;


import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Email implements Serializable {
    private long id = 0;
    private String sender = "";
    private ArrayList<String> receivers = null;
    private String subject = "";
    private String message ="";
    private String date = null;
    private Boolean senderDeleted = false;


    public Email() {
        this.date = LocalDateTime.now().toString();
    }
    public Email(String sender, ArrayList<String> receivers, String subject, String message) {
        this.id = new Timestamp(System.currentTimeMillis()).getTime();
        this.sender = sender;
        this.receivers = receivers;
        this.subject = subject;
        this.message = message;
        this.date = LocalDateTime.now().toString();
    }

    public Email(long id , String sender, ArrayList<String> receivers, String subject, String message , String date) {
        this.id = id;
        this.sender = sender;
        this.receivers = receivers;
        this.subject = subject;
        this.message = message;
        this.date = date;
    }
    public Email(long id , String sender, ArrayList<String> receivers, String subject, String message , String date,boolean senderDeleted) {
        this.id = id;
        this.sender = sender;
        this.receivers = receivers;
        this.subject = subject;
        this.message = message;
        this.date = date;
        this.senderDeleted=senderDeleted;
    }
    public Email(String sender, String receiver, String subject, String message) {
        this.id = new Timestamp(System.currentTimeMillis()).getTime(); //TODO: aggiungere controllo id non prensente nel file
        this.sender = sender;
        this.receivers = new ArrayList<>();
        this.receivers.add(receiver);
        this.subject = subject;
        this.message = message;
        this.date = LocalDateTime.now().toString();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public ArrayList<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(ArrayList<String> receivers) {
        this.receivers = receivers;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Boolean getSenderDeleted() {
        return senderDeleted;
    }

    public void setSenderDeleted(Boolean senderDeleted) {
        this.senderDeleted = senderDeleted;
    }

    //TODO: inutile
    public String toFile() {
        String re ="";
        for (String rec:
                this.receivers) {
            re+=rec+"-";
        }

        return this.id+","+this.sender+","+re+","+this.subject+","+this.message+","+this.date;
    }

    @Override
    public String toString() {
        String re ="";
        for (String rec:
                this.receivers) {
            re+=rec+"-";
        }

        return this.sender + ": " + this.subject.toUpperCase() + "\n" + this.message.substring(0, this.message.length() - 3) + " " + this.date.split("T")[0];
    }

    public static boolean isValid(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }
}