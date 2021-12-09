package com.server;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static com.CONSTANTS.USERSFILEPATH;

public class ManageUsers {
    private ArrayList<String> emailsOfUsers = null;
    Semaphore sem = null;
    public ManageUsers(Semaphore sem) {
        this.emailsOfUsers = new ArrayList<>();
        this.sem = sem;
        loadEmailsOfUsers();
    }
    private void loadEmailsOfUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader(USERSFILEPATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                this.emailsOfUsers.add(values[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addEmail(String email){
        emailsOfUsers.add(email);
    }
    public boolean isEmailInSystem(String email){
        return emailsOfUsers.contains(email);
    }
    public void addEmailToFile(String email)  {
        try {
            sem.acquire();
            FileWriter fw = new FileWriter(USERSFILEPATH, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.newLine();
            bw.append(email);
            bw.close();
            sem.release();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ManageUsers{" +
                "emailsOfUsers=" + emailsOfUsers +
                '}';
    }
}
