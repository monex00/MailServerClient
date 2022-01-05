package com.server;
import com.Email;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import com.google.gson.*;
import static com.CONSTANTS.EMAILSFILEPATH;

public class ManageEmails {
    private ArrayList<Email> emails = null;
    private ManageUsers manageUsers = null;
    private Gson gson;

    public ManageEmails(ManageUsers mu) {
        this.manageUsers = mu;
        gson = new GsonBuilder().setPrettyPrinting().create();
        loadEmails();
    }

    private void loadEmails() {
        Email[] es = null;
        try (FileReader reader = new FileReader(EMAILSFILEPATH))
        {
            es = gson.fromJson(reader, Email[].class);
            if(es != null)
                emails = new ArrayList<>(Arrays.asList(es));
            else
                emails = new ArrayList<>();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addEmail(Email email){
        emails.add(email);
    }
    public boolean isReceiverInSystem(Email email){
        System.out.println(email.getReceivers().stream().allMatch(obj -> manageUsers.isEmailInSystem(obj)));
        return email.getReceivers().stream().allMatch(obj -> manageUsers.isEmailInSystem(obj));
    }

    public synchronized void addEmailToFile()  {
        System.out.println("ok");
        try (FileWriter file = new FileWriter(EMAILSFILEPATH)) {
            gson.toJson(this.emails, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void deleteEmail(String id,String myEmail){
        System.out.println(id);
        int index = findEmailById(Long.parseLong(id));

        if(index != -1){
            Email ema = this.emails.get(index);
            if(myEmail.equals(ema.getSender())){
                ema.setSenderDeleted(true);
            }else{
                ema.getReceivers().
                        remove(myEmail);
            }
            this.emails.set(index,ema);
            addEmailToFile();
        }
    }

    public ArrayList<Email> getReceivedMyEmails(String email){
        ArrayList<Email> ems = new ArrayList<>();
        this.emails.forEach( ema -> {
            if(ema.getReceivers().contains(email) || ema.getSender().equals(email)){
                ems.add(ema);
            }
        } );

        return ems;
    }

    public int findEmailById(long id){
        for (int i = 0; i < this.emails.size(); i++)
            if (this.emails.get(i).getId() == id)
                return i;
        return -1;
    }
}
