package com;

public class CONSTANTS {
    //message constants
    public static final String GETEMAILS = "GETEMAILS";
    public static final String MESSAGE = "MESSAGE";
    public static final String EMAIL = "EMAIL";
    public static final String EMAILUPDATE = "EMAILUPDATE";
    public static final String DISCONNECT = "QUIT";
    public static final String NEWEMAIL = "NEWEMAIL";
    public static final String RECEIVERNOTFOUND = "RECEIVERNOTFOUND";
    public static final String DELETEEMAIL = "DELETEEMAIL";

    //global path
    public static final String USERSFILEPATH = "users.csv";
    public static final String EMAILSFILEPATH = "emails.csv";

    //server constants
    public static final String SERVERIP = "127.0.0.1";
    public static final int SERVERPORT = 5555;

    //user constants
    public static final String NEWEMAILTEXT = "Hai ricevuto una nuova E-mail da: ";
    public static final String RECEIVERNOTFOUNDTEXT = "Non è stato possibile trovare uno dei destinatari!";
    public static final String DELETETEXT = "E-mail eliminata";
    public static final String CONNESSIONERRORTEXT = "Connessione non riuscita";
    public static final String CONNESSIONSUCCESSTEXT = "Connessione riuscita";


    //color constants
    public static final String RED = "#ff0000";
    public static final String GREEN = "#00ff00";


    private CONSTANTS() {}
}
