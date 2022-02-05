package com.server;
import com.CONSTANTS;
import com.Email;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.gson.*;
import com.server.model.ServerModel;
import javafx.application.Platform;

import static com.CONSTANTS.EMAILSFILEPATH;


public class ManageEmails {
    private ArrayList<Email> emails = null;
    private ManageUsers manageUsers = null;
    private ServerModel serverModel;

    private ReadWriteLock readWriteLock;
    private Lock rlock;
    private Lock wlock;

    private Gson gson;

    private int oldSize;

    public ManageEmails(ManageUsers mu, ServerModel serverModel) {
        this.manageUsers = mu;
        this.serverModel = serverModel;
        gson = new GsonBuilder().setPrettyPrinting().create();
        readWriteLock = new ReentrantReadWriteLock();
        rlock = readWriteLock.readLock();
        wlock = readWriteLock.writeLock();

        loadEmails();
        setWritingFileTimer();
    }

    private void setWritingFileTimer(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                addEmailToFile();
            }
        },  0, CONSTANTS.SECONDTOWRITEEMAILS * 1000);
    }

    private void loadEmails() {
        rlock.lock();
        Email[] es = null;
        try (FileReader reader = new FileReader(EMAILSFILEPATH))
        {
            es = gson.fromJson(reader, Email[].class);
            if(es != null)
                emails = new ArrayList<>(Arrays.asList(es));
            else
                emails = new ArrayList<>();
            Platform.runLater(() -> {serverModel.appendLogText("Emails loaded from file");});
            this.oldSize = emails.size();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            rlock.unlock();
        }
    }

    public void addEmail(Email email){
        wlock.lock();
        emails.add(email);
        Platform.runLater(() -> {serverModel.appendLogText("New email with id: " + email.getId() + " added to list");});
        wlock.unlock();
    }

    public boolean isReceiverInSystem(Email email){
        boolean isInSystem = false;
        rlock.lock();
        isInSystem = email.getReceivers().stream().allMatch(obj -> manageUsers.isEmailInSystem(obj));
        rlock.unlock();
        return isInSystem;
    }

    public void addEmailToFile()  {
        if(this.oldSize != this.emails.size()) {
            wlock.lock();
            System.out.println("Writing emails to file");
            oldSize = emails.size();
            try (FileWriter file = new FileWriter(EMAILSFILEPATH)) {
                gson.toJson(this.emails, file);
                Platform.runLater(() -> {
                    serverModel.appendLogText("Emails saved to file");
                });
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                wlock.unlock();
            }
        }
    }

    public void deleteEmail(String id, String myEmail){
        wlock.lock();
        int index = findEmailById(Long.parseLong(id));

        if(index != -1){
            Email ema = this.emails.get(index);
            if(myEmail.equals(ema.getSender())){
                ema.setSenderDeleted(true);
            }else{
                ema.setReceiverDeleted(myEmail, true);
            }
            this.emails.set(index,ema);
            Platform.runLater(() -> {serverModel.appendLogText("Email with id: " + id +  " deleted");});
        }
        wlock.unlock();

    }

    public ArrayList<Email> getReceivedMyEmails(String email){
        rlock.lock();
        ArrayList<Email> ems = new ArrayList<>();

        //add email from last to first
        for(int i = emails.size()-1; i >= 0; i--){
            Email ema = emails.get(i);
            if(ema.getReceivers().contains(email) || ema.getSender().equals(email)){
                ems.add(ema);
            }
        }

        rlock.unlock();
        return ems;
    }

    public int findEmailById(long id){
        int idx = -1;
        rlock.lock();
        for (int i = 0; i < this.emails.size(); i++)
            if (this.emails.get(i).getId() == id){
                idx =  i;
                break;
            }
        rlock.unlock();
        return idx;
    }
}
