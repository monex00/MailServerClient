package com.server;

import java.io.*;
import java.util.ArrayList;

import static com.CONSTANTS.USERSFILEPATH;

public class ManageUsers {
    private ArrayList<String> emailsOfUsers = null;
    public ManageUsers() {
        this.emailsOfUsers = new ArrayList<>();
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

    public synchronized void addEmailToFile(String email)  {
        try {
            FileWriter fw = new FileWriter(USERSFILEPATH, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.newLine();
            bw.append(email);
            bw.close();
        } catch (IOException e) {
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
