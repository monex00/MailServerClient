package com.server;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.CONSTANTS.USERSFILEPATH;

public class ManageUsers {
    private ArrayList<String> emailsOfUsers = null;
    private ReadWriteLock readWriteLock;
    private Lock rlock;
    private Lock wlock;

    public ManageUsers() {
        this.emailsOfUsers = new ArrayList<>();
        this.readWriteLock = new ReentrantReadWriteLock();
        this.rlock = readWriteLock.readLock();
        this.wlock = readWriteLock.writeLock();
        loadEmailsOfUsers();
    }

    private void loadEmailsOfUsers() {
        rlock.lock();
        try (BufferedReader br = new BufferedReader(new FileReader(USERSFILEPATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                this.emailsOfUsers.add(values[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            rlock.unlock();
        }
    }

    public void addEmail(String email){
        wlock.lock();
        emailsOfUsers.add(email);
        wlock.unlock();
    }

    public boolean isEmailInSystem(String email){
        boolean isInSystem = false;

        rlock.lock();
        isInSystem = emailsOfUsers.contains(email);
        rlock.unlock();

        return isInSystem;
    }

    public void addEmailToFile(String email)  {
        wlock.lock();
        try {
            FileWriter fw = new FileWriter(USERSFILEPATH, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.newLine();
            bw.append(email);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public String toString() {
        return "ManageUsers{" +
                "emailsOfUsers=" + emailsOfUsers +
                '}';
    }
}
