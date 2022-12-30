import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;


public class ServerConnectionHandler implements Runnable
{

    public static String CRLF = "\r\n";
    public static String LF = "\n";
    public static String ServerDomainName = "ServerDomain.gr";
    private static ArrayList<String> rPath_buffer =  new ArrayList<String>();
    private static ArrayList<String> fPath_buffer =  new ArrayList<String>();
    private static String[] cmdSequenceStrArr =  new String[3];// isRcptReady + isFromReady + dataMap.toString()
    private static HashMap <String,String> dataMap = new HashMap <String,String>();
    Boolean isReady = false;
    Boolean isHello = false;

    
    socketManager _socketMngObjVar = null;
    ArrayList<socketManager> _active_clients = null;
    
    public ServerConnectionHandler (ArrayList<socketManager> inArrayListVar, socketManager inSocMngVar)
    {
        _socketMngObjVar = inSocMngVar;
        _active_clients = inArrayListVar; 
    }
    
    public void run(){
        try{
            System.out.println("0 Client " + _socketMngObjVar.soc.getPort() + " Connected");
            System.out.println("0 SERVER : active clients : "+_active_clients.size());
            while (!_socketMngObjVar.soc.isClosed()) 
            {
                String skey = Keygen.keygenerator(Keygen.timetoseed());
                String clientMSG = AES.decrypt(_socketMngObjVar.input.readUTF(),skey);
                System.out.println("SERVER : message FROM CLIENT : " + _socketMngObjVar.soc.getPort() + " --> " + clientMSG); 

                //Check for Quit message for client 
                if (clientMSG.contains("QUIT")) {
                    System.out.println("5 SERVER : quiting client");
                    //
                    // SYNTAX (page 12 RFC 821)
                    // QUIT <SP> <SERVER domain> <SP> Service closing transmission channel<CRLF>
                    //          
                    _socketMngObjVar.output.writeUTF("221" + LF + ServerDomainName + LF + " Service closing transmission channel" + CRLF);                    
                    _active_clients.remove(_socketMngObjVar);
                    System.out.print("5 SERVER : active clients : "+_active_clients.size());
                    rPath_buffer.clear();
                    fPath_buffer.clear();
                    isReady = false;
                    
                    return;     // exiting thread
                }
               
                Server_SMTP_Handler(_socketMngObjVar, clientMSG);
            }   //while socket NOT CLOSED
        }
        catch (Exception except){
            //Exception thrown (except) when something went wrong, pushing clientMSG to the console
            System.out.println("Error in Server Connection Handler --> " + except.getMessage());
        }
    }


    
    
    
    
    private void Server_SMTP_Handler(socketManager sm, String clientMSG) 
    {
        boolean REQUESTED_DOMAIN_NOT_AVAILABLE = false;
        String ServerDomainName = "ServerDomain.gr";       
        boolean SMTP_OUT_OF_STORAGE = false;
        boolean SMTP_INSUFFICIENT_STORAGE = false;
        boolean SMTP_LOCAL_PROCESSING_ERROR = false;
        boolean SUCCESS_STATE = false;
        boolean WAIT_STATE = true;    
        String sResponceToClient = "";
        
        

        ArrayList<String> Users = new ArrayList<String>();
        Users.add("Elliot");
        Users.add("Benjamin");
        Users.add("Alex");

        ArrayList<String> KnownMails = new ArrayList<String>();
        KnownMails.add("elliotalderson@mydomain.com");
        KnownMails.add("alexdanyliuk@mydomain.ua");
        KnownMails.add("benjaminengel@mydomain.de");

        

        ArrayList<String> mail_data_buffer = new ArrayList<String>();
        ArrayList<String> Rcpts = new ArrayList<String>(); 

        boolean GO_ON_CHECKS = true;
        



        try{
            if(clientMSG.contains(CRLF))
            {
                //System.out.println("SERVER SIDE command RECEIVED--> " + clientMSG);
            ////////////////////////////////////////////////////////////////////
            // HELO CMD MESSSAGES PACK
            ////////////////////////////////////////////////////////////////////
                // error 500 -> Line too long ! COMMAND CASE = 512
                if (clientMSG.toUpperCase().contains("HELP HELLO")&& GO_ON_CHECKS)
                {
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("214 HELO command is mendatory to establish connection with the server" + CRLF,key);
                }
                else if (clientMSG.toUpperCase().contains("HELP MAIL")&& GO_ON_CHECKS)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("214 MAIL command checks to see if your mail is verified and save it in the server when you want to initiate a mail transaction" + CRLF,key);
                }
                else if (clientMSG.toUpperCase().contains("HELP RCPT")&& GO_ON_CHECKS)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("214 RCPT command can be used one or multiple times to add one or many recipients" + CRLF,key);
                }
                else if (clientMSG.toUpperCase().contains("HELP DATA")&& GO_ON_CHECKS)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("214 DATA command is used to input your actual mail data" + CRLF,key);
                }
                else if (clientMSG.toUpperCase().contains("HELP RSET")&& GO_ON_CHECKS)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("214 RSET command resets the application to start a new conversation with new or the same recipient\\s" + CRLF,key);
                }
                else if (clientMSG.toUpperCase().contains("HELP VRFY")&& GO_ON_CHECKS)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("214 VRFY command is used to check if a domain name is verified on the server" + CRLF,key);
                }
                else if (clientMSG.toUpperCase().contains("HELP EXPN")&& GO_ON_CHECKS)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("214 EXPN command is used when to expand a mail list" + CRLF,key);
                }
                else if (clientMSG.toUpperCase().contains("HELP NOOP")&& GO_ON_CHECKS)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("214 NOOP command is used to check the clients connection with the server" + CRLF,key);
                }
                else if (clientMSG.toUpperCase().contains("HELP QUIT")&& GO_ON_CHECKS)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("214 QUIT command is used to Quit the client and close the socket" + CRLF,key);
                }
                else if (clientMSG.contains("QUIT"))
                {
                    GO_ON_CHECKS = false;
                    rPath_buffer.clear();
                    fPath_buffer.clear();
                    isReady = false;
                }   
                else if (clientMSG.length()> 512 && GO_ON_CHECKS) 
                {
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("500"+ CRLF,key);
                    System.out.println("error 500 -> Line too long");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                }                
                // error 501 -> Syntax error in parameters or arguments
                else if (clientMSG.split(" ").length < 1  && GO_ON_CHECKS) 
                {
                    // no SP
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("501"+ CRLF,key);
                    //System.out.println("error 501 -> Syntax error in parameters or arguments");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                } 
                // error 504 -> Command parameter not implemented
                else if (clientMSG.length()<4 && GO_ON_CHECKS) 
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("504"+ CRLF,key);
                    //System.out.println("error 504 -> Command parameter not implemented");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                } 
                // error 421 -> <domain> Service not available
                else if (REQUESTED_DOMAIN_NOT_AVAILABLE && GO_ON_CHECKS) 
                {
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("421"+ CRLF,key);
                    String domain_not_found = clientMSG.replaceAll("HELO ", "");
                    domain_not_found = domain_not_found.replaceAll(CRLF,"");
                    //System.out.println("error 421 -> "+ domain_not_found +" Service not available");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                } 
                else if (clientMSG.contains("HELO") && GO_ON_CHECKS) 
                {
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("250" + LF + ServerDomainName + CRLF,key);
                    //System.out.println("SERVER responce: "+ sResponceToClient);
                    SUCCESS_STATE = true;
                    GO_ON_CHECKS = false;
                    isHello = true;
                    System.out.println("HELO");
                }
                else if (clientMSG.contains("MAIL FROM:") && GO_ON_CHECKS)
                {
                    //change the check to log in and not here
                   Boolean isContained = false;
                   String clientmsgclr = clientMSG.replace("MAIL FROM:","").replaceAll("\\<|>","").replace(CRLF,"").trim();
                   rPath_buffer.add(clientmsgclr); // add reverse-path to the list
                   String key = Keygen.keygenerator(Keygen.timetoseed());
                   sResponceToClient = AES.encrypt("250" + CRLF,key); //Requested mail action okay, completed
                   if(!fPath_buffer.isEmpty() && !rPath_buffer.isEmpty())
                    {
                        isReady = true;
                        sResponceToClient = AES.encrypt("354" + CRLF,key);
                        System.out.println("Server Ready To Recieve Data");
                    }
                   
                }
                else if (clientMSG.contains("VRFY") && GO_ON_CHECKS)
                {
                    Boolean isContained = false;
                    String clientmsgclr = clientMSG.replace("VRFY","").replaceAll("\\<|>","").replace(CRLF,"").trim();
                    for(int i=0; i<KnownMails.size(); i++)
                    {
                        if (KnownMails.get(i).contains(clientmsgclr))
                        isContained = true;                       
                    }
                    if (isContained == true)
                    {
                        System.out.println(clientmsgclr + " is a Verified mail");
                        String key = Keygen.keygenerator(Keygen.timetoseed());
                        sResponceToClient = AES.encrypt("250" + CRLF,key); //Requested mail action okay, completed
                    }
                    else
                    {
                        System.out.println(clientmsgclr + " is a non-Verified mail");
                        String key = Keygen.keygenerator(Keygen.timetoseed());
                        sResponceToClient = AES.encrypt("553" + CRLF,key); //Requested action not taken: mailbox name not allowed
                    }
                }
                else if (clientMSG.contains("NOOP")&& GO_ON_CHECKS)
                {
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    sResponceToClient = AES.encrypt("250" + CRLF,key);
                }
                else if (clientMSG.contains("RSET")&& GO_ON_CHECKS)
                {
                    //clear buffers
                rPath_buffer.clear();
                Rcpts.clear();
                mail_data_buffer.clear(); 
                fPath_buffer.clear();
                String key = Keygen.keygenerator(Keygen.timetoseed());
                sResponceToClient = AES.encrypt("250" + CRLF,key);
                System.out.println("All lists cleared, RSET Successful");
                }
                else if (clientMSG.contains("DATA")&&GO_ON_CHECKS)
                {
                    //data 354
                    //isFromReady.empty checks
                    //isRcptReady.empty checks
                    //isReady check (354 sent)
                    //isHello check
                    if (!isHello)
                    {
                        //no HELO
                        String key = Keygen.keygenerator(Keygen.timetoseed());
                        sResponceToClient = AES.encrypt("503" + CRLF,key);
                        System.out.println("Missing HELLO");
                    }
                    else if (isReady)
                    {
                        //passed checks
                        String dataStr;
                        LocalDateTime dateNow = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
                        DateTimeFormatter keyFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy/HH/mm");
                        String dateNowFormated = dateNow.format(formatter);
                        dataStr = clientMSG.replace("DATA" , "").replaceAll("\\<|>","").trim();
                        dataMap.put(dataStr, dateNowFormated);
                        System.out.println(dataMap);
                        cmdSequenceStrArr[0] = fPath_buffer.toString(); 
                        cmdSequenceStrArr[1] = rPath_buffer.toString();
                        cmdSequenceStrArr[2] = dataMap.toString();
                        String key = Keygen.keygenerator(Keygen.timetoseed());
                        String keyFormatted = dateNow.format(keyFormatter);
                        String cmdSequenceListString=String.join(" ",cmdSequenceStrArr);
                        String enctypString = AES.encrypt(cmdSequenceListString, key);
                        if(storageReaderWriter.write((enctypString+keyFormatted),"serverstorage.txt"))
                        sResponceToClient = AES.encrypt("250" + CRLF,key);
                        else
                        sResponceToClient = AES.encrypt("451"+ CRLF, key);
                        dataStr = "";
                        dataMap.clear();
                    }
                    else
                    {
                        //missing rcpt or mail from 
                        //503	Bad sequence of commands
                        System.out.println("Missing RCPT or MAIL FROM");
                        String key = Keygen.keygenerator(Keygen.timetoseed());
                        sResponceToClient = AES.encrypt("503" + CRLF,key);
                    }
                }
                else if (clientMSG.contains("RCPT")&&GO_ON_CHECKS)
                {
                    //Helo check only in rcpt as recommended in 
                    if (isHello)
                    {
                        String rcpt;
                        rcpt = clientMSG.replace("RCPT TO:","").replaceAll("\\<|>","").replace(CRLF,"").trim();
                        fPath_buffer.add(rcpt);
                        if (!fPath_buffer.isEmpty() && !rPath_buffer.isEmpty())
                        {
                            isReady = true;
                            String key = Keygen.keygenerator(Keygen.timetoseed());
                            sResponceToClient = AES.encrypt("354" + CRLF,key);
                            System.out.println("Server Ready To Recieve Data");
                        }
                    }
                    else
                    {
                        //no HELO
                        //503	Bad sequence of commands
                        String key = Keygen.keygenerator(Keygen.timetoseed());
                        sResponceToClient = AES.encrypt("503" + CRLF,key);
                        System.out.println("Missing HELO");
                    }
                }
                else if (clientMSG.contains("LOGIN")&&GO_ON_CHECKS)
                {   
                    String loginer = clientMSG.replace("LOGIN ","").replace(CRLF,"").trim();
                    if(storageReaderWriter.authenticator("accounts_database.txt",loginer))
                    {   
                        String key = Keygen.keygenerator(Keygen.timetoseed());
                        sResponceToClient = AES.encrypt("LOGGED "+CRLF,key);

                      //String[] split = loginer.split("|");
                      //if(split[0].contains("elliotalderson@mydomain.com"))
                      //{
                      //    String key = Keygen.keygenerator(Keygen.timetoseed());
                      //    sResponceToClient = AES.encrypt("LOGGED ELLIOT"+CRLF,key);
                      //}
                      //else if(split[0].contains("benjaminengel@mydomain.de"))
                      //{
                      //    String key = Keygen.keygenerator(Keygen.timetoseed());
                      //    sResponceToClient = AES.encrypt("LOGGED BENJAMIN"+CRLF,key);
                      //}
                      //else if(split[0].contains("alexdanyliuk@mydomain.ua"))
                      //{
                      //    String key = Keygen.keygenerator(Keygen.timetoseed());
                      //    sResponceToClient = AES.encrypt("LOGGED ALEX"+CRLF,key);
                      //}

                    }
                    else 
                    sResponceToClient = "FAILED"+CRLF;
                }
                else
                {
                    sResponceToClient = "500"; //Syntax error, command unrecognised
                }
            ////////////////////////////////////////////////////////////////////
            // END HELO
            ////////////////////////////////////////////////////////////////////
                clientMSG = "";      //empty buffer after CRLF   
            }         //if CRLF

            sm.output.writeUTF(sResponceToClient);
        }
        catch (Exception e){
            //Exception thrown (except) when something went wrong, pushing message to the console
            System.out.println("Error --> " + e.getMessage());
        }        
    }
}