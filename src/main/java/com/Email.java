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
    private ArrayList<String> receiversDeleted = new ArrayList<>();


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
        this.id = new Timestamp(System.currentTimeMillis()).getTime();
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

    public Boolean isSenderDeleted() {
        return senderDeleted;
    }

    public void setSenderDeleted(Boolean senderDeleted) {
        this.senderDeleted = senderDeleted;
    }

    public void setReceiverDeleted(String receiver, boolean deleted) {
        if(deleted)
            this.receiversDeleted.add(receiver);
        else
            this.receiversDeleted.remove(receiver);
    }
    public boolean isReceiverDeleted(String receiver){
        return receiversDeleted.contains(receiver);
    }

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
            re += rec+"-";
        }

        return this.sender + ": " + this.subject.toUpperCase() + "\n" + this.message.substring(0, this.message.length() - 3) + " " + this.date.split("T")[0];
    }

    public static boolean isValid(String to) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        String[] emails = to.split(";");
        for (String email : emails) {
            if(!email.matches(regex))
                return false;
        }
        return true;
    }
}