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
    public static final String EMAILSENT = "SENDEMAIL";
    public static final String EMAILSENTTEXT = "Email sent correctly";

    //global path
    public static final String USERSFILEPATH = "src/main/java/com/server/users.csv";
    public static final String EMAILSFILEPATH = "src/main/java/com/server/emails.json";

    //server constants
    public static final String SERVERIP = "127.0.0.1";
    public static final int SERVERPORT = 5555;
    public static final int MAXSOCKETS = 10;
    public static final int MAXTHREADS = 10;
    public static final int SECONDTOTIMEOUT = 30;
    public static final int SECONDTOWRITEEMAILS = 10;

    //user constants
    public static final String NEWEMAILTEXT = "Hai ricevuto una nuova mail da: ";
    public static final String GENERICNEWEMAILTEXT = "Sono presenti nuove mail";
    public static final String RECEIVERNOTFOUNDTEXT = "Non è stato possibile trovare uno dei destinatari!";
    public static final String DELETETEXT = "mail eliminata";
    public static final String CONNESSIONERRORTEXT = "Connessione non riuscita";
    public static final String CONNESSIONSUCCESSTEXT = "Connessione riuscita";


    //color constants
    public static final String RED = "#ff0000";
    public static final String GREEN = "#00ff00";
    public static final String YELLOW = "#ffff00";


    private CONSTANTS() {}
}
