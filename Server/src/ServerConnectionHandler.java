import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;


public class ServerConnectionHandler implements Runnable
{
    // Initialization of SCH variables
    public static String CRLF = "\r\n";
    public static String LF = "\n";
    private static ArrayList<String> rPath_buffer =  new ArrayList<String>();
    private static ArrayList<String> fPath_buffer =  new ArrayList<String>();
    private static boolean isHelo_buffer = false;
    private static String[] dataCollectorStrArr =  new String[3];// isRcptReady + isFromReady + dataMap.toString()
    private static HashMap <String,String> dataMap = new HashMap <String,String>();
    private static Boolean isReady = false;
    private static Boolean isHelo = false;
    public static String ServerDomainName;
    
    socketManager _socketManagerVar = null;
    ArrayList<socketManager> _active_clients = null;
    

    public ServerConnectionHandler (ArrayList<socketManager> inArrayListVar, socketManager inSocMngVar)
    {
        _socketManagerVar = inSocMngVar;
        _active_clients = inArrayListVar; 
    }
    
    public void run(){ //Impliments runnable
        try{
            //Prints out the number connected users whenever on connects after the first time
            System.out.println("0 Client " + _socketManagerVar.soc.getPort() + " Connected");
            System.out.println("0 SERVER : active clients : "+_active_clients.size());
            // Keeps looping while the socket is open
            while (!_socketManagerVar.soc.isClosed()) 
            {
                // Decryption key generation beforehand 
                String skey = Keygen.keygenerator(Keygen.timetoseed());
                String clientMSG = AES.decrypt(_socketManagerVar.input.readUTF(),skey);
                System.out.println("DATA FROM CLIENT : " + _socketManagerVar.soc.getPort() + " " + clientMSG); 

                //Check for Quit message for client 
                if (clientMSG.contains("QUIT")) {
                    System.out.println("5 SERVER : quiting client");        
                    _socketManagerVar.output.writeUTF("221" + LF + ServerDomainName + LF + " Service closing transmission channel" + CRLF);                    
                    _active_clients.remove(_socketManagerVar);
                    System.out.print("5 SERVER : active clients : "+_active_clients.size());
                    // clears and resets buffers before exiting
                    rPath_buffer.clear();
                    fPath_buffer.clear();
                    isReady = false;
                    
                    // exits thread
                    return;     
                }
               
                Server_SMTP_Handler(_socketManagerVar, clientMSG);
            }   //while socket NOT CLOSED
        }
        catch (Exception e){
            //Exception thrown (except) when something went wrong, pushing clientMSG to the console
            System.out.println("Error in SCH" + e.getMessage());
        }
    }


    
    
    
    
    private void Server_SMTP_Handler(socketManager sm, String clientMsg_Buffer) 
    {
        // Intialization of some variables
        boolean domain_unavialable_rply = false;       
        String serverRply = "";
        
        // Lists for EXPN 
        ArrayList<String> usResidentsList = new ArrayList<String>();
        usResidentsList.add ("elliotalderson@mydomain.com");
        usResidentsList.add ("alexdanyliuk@mydomain.ua");
        ArrayList<String> deResidentsList = new ArrayList<String>();
        deResidentsList.add("benjaminengel@mydomain.de");

        // List of UserNames
        ArrayList<String> Users = new ArrayList<String>();
        Users.add("Elliot");
        Users.add("Benjamin");
        Users.add("Alex");

        // List of Known (Trusted) Emails
        ArrayList<String> KnownMails = new ArrayList<String>();
        KnownMails.add("elliotalderson@mydomain.com");
        KnownMails.add("alexdanyliuk@mydomain.ua");
        KnownMails.add("benjaminengel@mydomain.de");

        
        // Variable Intialization
        ArrayList<String> md_buffer = new ArrayList<String>();
        ArrayList<String> Rcpts = new ArrayList<String>(); 
        boolean passChecks = true;
        



        try{
            if(clientMsg_Buffer.contains(CRLF))
            {
                if (clientMsg_Buffer.toUpperCase().contains("HELP HELO")&& passChecks)
                {
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("214 HELO command is mendatory to establish connection with the server" + CRLF,key);
                }
                else if (clientMsg_Buffer.toUpperCase().contains("HELP MAIL")&& passChecks)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("214 MAIL command checks to see if your mail is verified and save it in the server when you want to initiate a mail transaction" + CRLF,key);
                }
                else if (clientMsg_Buffer.toUpperCase().contains("HELP RCPT")&& passChecks)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("214 RCPT command can be used one or multiple times to add one or many recipients" + CRLF,key);
                }
                else if (clientMsg_Buffer.toUpperCase().contains("HELP DATA")&& passChecks)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("214 DATA command is used to input your actual mail data" + CRLF,key);
                }
                else if (clientMsg_Buffer.toUpperCase().contains("HELP RSET")&& passChecks)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("214 RSET command resets the application to start a new conversation with new or the same recipient\\s" + CRLF,key);
                }
                else if (clientMsg_Buffer.toUpperCase().contains("HELP VRFY")&& passChecks)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("214 VRFY command is used to check if a domain name is verified on the server" + CRLF,key);
                }
                else if (clientMsg_Buffer.toUpperCase().contains("HELP EXPN")&& passChecks)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("214 EXPN command is used when to expand a mail list (Available lists are USRESIDENTS, DERESIDENTS)" + CRLF,key);
                }
                else if (clientMsg_Buffer.toUpperCase().contains("HELP NOOP")&& passChecks)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("214 NOOP command is used to check the clients connection with the server" + CRLF,key);
                }
                else if (clientMsg_Buffer.toUpperCase().contains("HELP QUIT")&& passChecks)
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("214 QUIT command is used to Quit the client and close the socket" + CRLF,key);
                }
                else if (clientMsg_Buffer.contains("QUIT"))
                {
                    // On quit clear buffers
                    passChecks = false;
                    rPath_buffer.clear();
                    fPath_buffer.clear();
                    isReady = false;
                }   
                else if (clientMsg_Buffer.length()> 512 && passChecks) 
                {
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("500"+ CRLF,key);
                    System.out.println("error 500 -> Line too long");
                    passChecks = false;
                }                
                // error 501 -> Syntax error in parameters or arguments
                else if (clientMsg_Buffer.split(" ").length < 1  && passChecks) 
                {
                    // no SP
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("501"+ CRLF,key);
                    //System.out.println("error 501 -> Syntax error in parameters or arguments");
                    passChecks = false;
                } 
                // error 504 -> Command parameter not implemented
                else if (clientMsg_Buffer.length()<4 && passChecks) 
                {   
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("504"+ CRLF,key);
                    //System.out.println("error 504 -> Command parameter not implemented");
                    passChecks = false;
                } 
                // error 421 -> <domain> Service not available
                else if (domain_unavialable_rply && passChecks) 
                {
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("421"+ CRLF,key);
                    String domain_not_found = clientMsg_Buffer.replaceAll("HELO ", "");
                    domain_not_found = domain_not_found.replaceAll(CRLF,"");
                    //System.out.println("error 421 -> "+ domain_not_found +" Service not available");
                    
                } 
                else if (clientMsg_Buffer.contains("HELO") && passChecks) 
                {
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("250" + LF + ServerDomainName + CRLF,key);
                    //System.out.println("SERVER responce: "+ sResponceToClient);
                    passChecks = false;
                    isHelo = true;
                    isHelo_buffer = true;
                    if(!fPath_buffer.isEmpty() && !rPath_buffer.isEmpty())
                    {
                        isReady = true;
                        serverRply = AES.encrypt("354" + CRLF,key);
                        System.out.println("Server Ready To Recieve Data");
                    }
                    System.out.println("HELO");
                }
                else if (clientMsg_Buffer.contains("MAIL FROM:") && passChecks)
                {
                    //change the check to log in and not here
                   String clientmsgclr = clientMsg_Buffer.replace("MAIL FROM:","").replaceAll("\\<|>","").replace(CRLF,"").trim();
                   rPath_buffer.add(clientmsgclr); // add reverse-path to the list
                   String key = Keygen.keygenerator(Keygen.timetoseed());
                   serverRply = AES.encrypt("250" + CRLF,key); //Requested mail action okay, completed
                   if(!fPath_buffer.isEmpty() && isHelo_buffer)
                    {
                        isReady = true;
                        serverRply = AES.encrypt("354" + CRLF,key);
                        System.out.println("Server Ready To Recieve Data");
                    }
                   
                }
                else if (clientMsg_Buffer.contains("VRFY") && passChecks)
                {
                    Boolean isContained = false;
                    String clientmsgclr = clientMsg_Buffer.replace("VRFY","").replaceAll("\\<|>","").replace(CRLF,"").trim();
                    for(int i=0; i<KnownMails.size(); i++)
                    {
                        // Checks if the give mail is verified
                        if (KnownMails.get(i).contains(clientmsgclr))
                        isContained = true;                       
                    }
                    if (isContained == true)
                    {
                        System.out.println(clientmsgclr + " is a Verified mail");
                        String key = Keygen.keygenerator(Keygen.timetoseed());
                        serverRply = AES.encrypt("250" + CRLF,key); //Requested mail action okay, completed
                    }
                    else
                    {
                        System.out.println(clientmsgclr + " is a non-Verified mail");
                        String key = Keygen.keygenerator(Keygen.timetoseed());
                        serverRply = AES.encrypt("553" + CRLF,key); //Requested action not taken: mailbox name not allowed
                    }
                }
                else if (clientMsg_Buffer.contains("NOOP")&& passChecks)
                {
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("250" + CRLF,key); //Requested mail action okay, completed
                }
                else if (clientMsg_Buffer.contains("RSET")&& passChecks)
                {
                    //clear buffers
                rPath_buffer.clear();
                Rcpts.clear();
                md_buffer.clear(); 
                fPath_buffer.clear();
                String key = Keygen.keygenerator(Keygen.timetoseed());
                serverRply = AES.encrypt("250" + CRLF,key); //Requested mail action okay, completed
                System.out.println("All lists cleared, RSET Successful");
                }
                else if (clientMsg_Buffer.contains("DATA")&&passChecks)
                {
                    if (!isHelo)
                    {
                        //no HELO
                        String key = Keygen.keygenerator(Keygen.timetoseed());
                        serverRply = AES.encrypt("503" + CRLF,key); // Wrong sequence of commands
                        System.out.println("Missing HELO");
                    }
                    else if (isReady)
                    {
                        //passed checks
                        String dataStr;
                        // Snapshot of date and time
                        LocalDateTime dateNow = LocalDateTime.now();
                        // Create a specific format to get the output we want for the TimeStamp
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
                        // Secound format for key generation
                        DateTimeFormatter keyFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy/HH/mm");
                        String dateNowFormated = dateNow.format(formatter); // Applies the format
                        dataStr = clientMsg_Buffer.replace("DATA" , "").replaceAll("\\<|>","").trim(); // removes excess data from the clients message
                        dataMap.put(dataStr, dateNowFormated); // Saves the data and the times stamp in a hashmap
                        System.out.println(dataMap);    // Prints it into the server terminal and saves path buffers and the data with the time stamp accordingly in the String Array
                        dataCollectorStrArr[0] = fPath_buffer.toString(); 
                        dataCollectorStrArr[1] = rPath_buffer.toString();
                        dataCollectorStrArr[2] = dataMap.toString();
                        String key = Keygen.keygenerator(Keygen.timetoseed()); // generates a key
                        String keyFormatted = dateNow.format(keyFormatter);    //formats it to be saved
                        String dataCollectorStr=String.join(" ",dataCollectorStrArr); //StrArr to Str
                        String enctypString = AES.encrypt(dataCollectorStr, key);
                        if(storageReaderWriter.write((enctypString+keyFormatted),"serverstorage.txt"))
                        serverRply = AES.encrypt("250" + CRLF,key); //Requested mail action okay, completed
                        else
                        serverRply = AES.encrypt("451"+ CRLF, key);
                        dataStr = "";
                        dataMap.clear();
                    }
                    else
                    {
                        //missing rcpt or mail from 
                        //503 Bad sequence of commands
                        System.out.println("Missing RCPT or MAIL FROM");
                        String key = Keygen.keygenerator(Keygen.timetoseed());
                        serverRply = AES.encrypt("503" + CRLF,key);
                    }
                }
                else if (clientMsg_Buffer.contains("RCPT")&&passChecks)
                {
                    //Helo check only in rcpt as recommended in 
                    if (isHelo)
                    {
                        String rcpt;
                        rcpt = clientMsg_Buffer.replace("RCPT TO:","").replaceAll("\\<|>","").replace(CRLF,"").trim();
                        fPath_buffer.add(rcpt);
                        if (!rPath_buffer.isEmpty())
                        {
                            isReady = true;
                            String key = Keygen.keygenerator(Keygen.timetoseed());
                            serverRply = AES.encrypt("354" + CRLF,key); // Server Ready To Recieve Data
                            System.out.println("Server Ready To Recieve Data");
                        }
                    }
                    else
                    {
                        //no HELO
                        //503 Bad sequence of commands
                        String key = Keygen.keygenerator(Keygen.timetoseed());
                        serverRply = AES.encrypt("503" + CRLF,key);
                        System.out.println("Missing HELO");
                    }
                }
                else if (clientMsg_Buffer.contains("LOGIN")&&passChecks)
                {   
                    // log in check with authenticator method
                    String loginer = clientMsg_Buffer.replace("LOGIN ","").replace(CRLF,"").trim();
                    if(storageReaderWriter.authenticator("accounts_database.txt",loginer))
                    {   
                        String key = Keygen.keygenerator(Keygen.timetoseed());
                        serverRply = AES.encrypt("LOGGED "+CRLF,key);
                    }
                    else 
                    serverRply = "FAILED"+CRLF; // Failed to Authenticated (Wrong email or password)
                }
                else if (clientMsg_Buffer.contains("EXPN")&&passChecks)
                {
                    // 2 List impimented USRESIDENTS AND DERESIDENTS
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("250"+ CRLF,key);
                    if (clientMsg_Buffer.toUpperCase().contains("USRESIDENTS"))
                    {
                        serverRply = AES.encrypt("LIST "+(String.join(", ",usResidentsList))+CRLF, key);
                    }
                    else if (clientMsg_Buffer.toUpperCase().contains("DERESIDENTS"))
                    {
                        serverRply = AES.encrypt("LIST "+(String.join(", ",deResidentsList))+CRLF, key);
                    }
                    else serverRply = AES.encrypt("504"+CRLF,key);
                    
                }
                else if (clientMsg_Buffer.contains("EHLO")&&passChecks)
                {
                    String key = Keygen.keygenerator(Keygen.timetoseed());
                    serverRply = AES.encrypt("550"+ CRLF,key); // Not Implimented
                }
                else
                {
                    serverRply = "500"; //Syntax error, command unrecognised
                }

                clientMsg_Buffer = "";    // clear buffer 
            } 
            sm.output.writeUTF(serverRply);
        }
        catch (Exception e){
            //Exception thrown (except) when something went wrong, pushing message to the console
            System.out.println("Error --> " + e.getMessage());
        }        
    }
}