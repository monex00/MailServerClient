package com.server;

import com.Email;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;

import static com.CONSTANTS.EMAILSFILEPATH;

public class ManageEmails {
    private ArrayList<Email> emails = null; //potrebbe essere un hashmap
    Semaphore sem = null;
    ManageUsers manageUsers = null;
    public ManageEmails(Semaphore sem , ManageUsers mu) {
        this.emails = new ArrayList<>();
        this.sem = sem;
        this.manageUsers = mu;
        loadEmails();
    }
    private void loadEmails() {
        try (BufferedReader br = new BufferedReader(new FileReader(EMAILSFILEPATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String[] receivers = values[2].split("-");
                ArrayList<String> arrReceivers = new ArrayList<>();
                Collections.addAll(arrReceivers,receivers);
                this.emails.add(new Email(Long.parseLong(values[0]),
                        values[1],arrReceivers,values[3],
                        values[4], LocalDateTime.parse(values[5]).toString()));
            }
        } catch (IOException  e) {
            e.printStackTrace();
        }
    }

    public void addEmail(Email email){
        emails.add(email);
    }
    public boolean isReceiverInSystem(Email email){
        return email.getReceivers().stream().allMatch(obj -> manageUsers.isEmailInSystem(obj));
    }
    public void addEmailToFile(Email email)  {
        try {
            sem.acquire();
            FileWriter fw = new FileWriter(EMAILSFILEPATH, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(email.toFile());
            bw.newLine();
            bw.close();
            sem.release();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void deleteEmail(String id,String myEmail){ //potrebbe essere syncronized
        System.out.println(id);
        int index = findEmailById(Long.parseLong(id));

        if(index != -1){
            if(myEmail.equals(this.emails.get(index).getSender())){
                this.emails.get(index).setSender("REMOVED");
            }else{
                this.emails.get(index).getReceivers().
                        remove(myEmail);
            }
            try {
                sem.acquire();
                FileWriter fw = new FileWriter(EMAILSFILEPATH, false);
                BufferedWriter bw = new BufferedWriter(fw);
                for (Email e :
                        this.emails) {
                    bw.write(e.toFile());
                    bw.newLine();
                }
                bw.close();
                sem.release();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Email> getEmails(String email, boolean includeSent){
        ArrayList<Email> ems = new ArrayList<>();
        for (Email e:
             this.emails) {
            if(includeSent){
                if(e.getReceivers().contains(email) || e.getSender().equals(email)){
                    ems.add(e);
                }
            }else {
                if(e.getReceivers().contains(email)){
                    ems.add(e);
                }
            }
        }
        return ems;
    }


    public int findEmailById(long id){
        for (int i = 0; i < this.emails.size(); i++)
            if (this.emails.get(i).getId() == id)
                return i;
        return -1;
    }

    public void printEmails (){
        for (Email e:
             emails) {
            System.out.println(e.getReceivers().toString());
        }
    }
}
