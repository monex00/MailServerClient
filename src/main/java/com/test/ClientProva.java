package com.test;

import com.CONSTANTS;
import com.Email;
import com.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientProva {
    private  Socket socket = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    private String email = "";

    public ClientProva(Socket socket, String email) {
        try {
            this.socket = socket;
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.email = email;

            objectOutputStream.writeObject(new Message("MESSAGE",this.email,null));
            objectOutputStream.flush();
        }catch (IOException e){
            closeConn();
        }
    }
    public void sendMessage(){
        try{
            objectOutputStream.writeObject(
                    new Message(CONSTANTS.EMAIL, new Email(this.email, "a@gmail.com", "OGGETTO5", "CONTENUTO 5")));
            objectOutputStream.flush();
        }catch (IOException e){
            e.printStackTrace();
            closeConn();
        }
        closeConn();
    }

    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message;
                while(socket.isConnected()){
                    try {
                        message = (Message) objectInputStream.readObject();
                        System.out.println(message.toString());
                    }catch(IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        closeConn();
                        break;
                    }
                }
            }
        }).start();
    }

    private void closeConn() {
        try {
            if(socket != null) socket.close();
            if(objectInputStream != null) objectInputStream.close();
            if( objectOutputStream != null) objectOutputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Socket sock = new Socket(CONSTANTS.SERVERIP, CONSTANTS.SERVERPORT);
            ClientProva cl = new ClientProva(sock, "b@gmail.com");
            cl.sendMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
