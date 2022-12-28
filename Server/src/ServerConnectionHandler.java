import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


    public class ServerConnectionHandler implements Runnable
    {

    
        public static String CRLF = "\r\n";
        public static String LF = "\n";
        public static String ServerDomainName = "ServerDomain.gr";
        private static ArrayList<String> rPath_buffer =  new ArrayList <String>();
        private static ArrayList<String> fPath_buffer = new ArrayList <String>();
        private static ArrayList<String> cmdSequenceList;// isRcptReady + isFromReady + dataMap.toString()
        private static HashMap<String,String> dataMap = new HashMap<String,String>();

        Boolean isReady = false;
        Boolean isHelo = false;
        

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
                    String clientMSG = _socketMngObjVar.input.readUTF();
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
            
            
    
            ArrayList<String> UsersInServerDomain = new ArrayList<String>();
            UsersInServerDomain.add("Alice");
            UsersInServerDomain.add("Bob");
            UsersInServerDomain.add("Mike");
    
            ArrayList<String> KnownDomains = new ArrayList<String>();
            KnownDomains.add("ThatDomain.gr");
            KnownDomains.add("MyTestDomain.gr");
            KnownDomains.add("ServerDomain.gr");
    
            
    
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
                    if (clientMSG.contains("QUIT")) 
                    {
                        GO_ON_CHECKS = false;
                        rPath_buffer.clear();
                        fPath_buffer.clear();
                        isReady = false;
                    }   
                    else if (clientMSG.length()> 512 && GO_ON_CHECKS) 
                    {
                        sResponceToClient = "500"+ CRLF;
                        System.out.println("error 500 -> Line too long");
                        SUCCESS_STATE = false;
                        GO_ON_CHECKS = false;
                    }                
                    // error 501 -> Syntax error in parameters or arguments
                    else if (clientMSG.split(" ").length < 1  && GO_ON_CHECKS) 
                    {
                        // no SP
                        sResponceToClient = "501"+ CRLF;
                        //System.out.println("error 501 -> Syntax error in parameters or arguments");
                        SUCCESS_STATE = false;
                        GO_ON_CHECKS = false;
                    } 
                    // error 504 -> Command parameter not implemented
                    else if (clientMSG.length()<4 && GO_ON_CHECKS) 
                    {
                        sResponceToClient = "504"+ CRLF;
                        //System.out.println("error 504 -> Command parameter not implemented");
                        SUCCESS_STATE = false;
                        GO_ON_CHECKS = false;
                    } 
                    // error 421 -> <domain> Service not available
                    else if (REQUESTED_DOMAIN_NOT_AVAILABLE && GO_ON_CHECKS) 
                    {
                        sResponceToClient = "421"+ CRLF;
                        String domain_not_found = clientMSG.replaceAll("HELO ", "");
                        domain_not_found = domain_not_found.replaceAll(CRLF,"");
                        //System.out.println("error 421 -> "+ domain_not_found +" Service not available");
                        SUCCESS_STATE = false;
                        GO_ON_CHECKS = false;
                    } 
                    else if (clientMSG.contains("HELO") && GO_ON_CHECKS) 
                    {
                        sResponceToClient = "250" + LF + ServerDomainName + CRLF;
                        //System.out.println("SERVER responce: "+ sResponceToClient);
                        SUCCESS_STATE = true;
                        GO_ON_CHECKS = false;
                        isHelo = true;
                        System.out.println("HELO");
                    }
                    else if (clientMSG.contains("MAIL FROM:") && GO_ON_CHECKS)
                    {
                       Boolean isContained = false;
                       String clientmsgclr = clientMSG.replace("MAIL FROM:","").replaceAll("\\<|>","").replace(CRLF,"").trim();
                       for(int i=0; i<KnownDomains.size(); i++)
                       {
                       if (KnownDomains.get(i).contains(clientmsgclr))
                       isContained = true;                       
                       }
                       if (isContained == true)
                       {
                       System.out.println(clientmsgclr+" is a Verified mail");
                       rPath_buffer.add(clientmsgclr); // add reverse-path to the list
                       sResponceToClient = "250" + CRLF; //Requested mail action okay, completed
                       if(!fPath_buffer.isEmpty() && !rPath_buffer.isEmpty())
                        {
                            isReady = true;
                            sResponceToClient = "354" + CRLF;
                            System.out.println("Server Ready To Recieve Data");
                        }
                       }
                       else
                       {
                       System.out.println(clientmsgclr+" is a non-Verified mail");
                       sResponceToClient = "553" + CRLF; //Requested action not taken: mailbox name not allowed
                    }

                    }
                    else if (clientMSG.contains("VRFY") && GO_ON_CHECKS)
                    {
                       Boolean isContained = false;
                       String clientmsgclr = clientMSG.replace("VRFY","").replaceAll("\\<|>","").replace(CRLF,"").trim();

                       for(int i=0; i<KnownDomains.size(); i++)
                       {
                       if (KnownDomains.get(i).contains(clientmsgclr))
                       isContained = true;                       
                       }
                       if (isContained == true)
                       {
                       System.out.println(clientmsgclr + " is a Verified mail");
                       sResponceToClient = "250" + CRLF; //Requested mail action okay, completed
                       }
                       else
                       {
                       System.out.println(clientmsgclr + " is a non-Verified mail");
                       sResponceToClient = "553" + CRLF; //Requested action not taken: mailbox name not allowed
                       }
                    }
                    else if (clientMSG.contains("NOOP")&& GO_ON_CHECKS)
                    {
                        sResponceToClient = "250" + CRLF;
                    }
                    else if (clientMSG.contains("HELP")&& GO_ON_CHECKS)
                    {
                        sResponceToClient = "214" + LF + "HELO command is mendatory to establish connection with the server\nMAIL command checks to see if your mail is verified and save it in the server when you want to initiate a mail transaction\nRCPT command can be use one or multiple times to add one or many recipients\nDATA command is use to input your actual mail data\nRSET command resets the application to start a new conversation with new or the same recipient\\s\nVRFY command is used to check if a domain name is verified on the server\nEXPN command is used when to expand a mail list\nNOOP command is used to check the clients connection with the server\nQUIT command is used to Quit the client and close the socket"+CRLF;
                    }
                    else if (clientMSG.contains("RSET")&& GO_ON_CHECKS)
                    {
                        //clear buffers
                    rPath_buffer.clear();
                    Rcpts.clear();
                    mail_data_buffer.clear(); 
                    fPath_buffer.clear();
                    sResponceToClient = "250" + CRLF;
                    System.out.println("All lists cleared, RSET Successful");
                    }
                    else if (clientMSG.contains("DATA")&&GO_ON_CHECKS)
                    {
                        //data 354
                        //isFromReady.empty checks
                        //isRcptReady.empty checks
                        //isReady check (354 sent)
                        //isHelo check
                        if (!isHelo)
                        {
                            //no HELO
                            sResponceToClient = "503" + CRLF;
                            System.out.println("Missing HELO");

                        }
                        else if (isReady)
                        {
                            //passed checks
                            String dataStr;
                            LocalDateTime dateNow = LocalDateTime.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
                            String dateNowFormated = dateNow.format(formatter);
                            dataStr = clientMSG.replace("DATA" , "").replaceAll("\\<|>","").trim();
                            dataMap.put(dataStr, dateNowFormated);
                            System.out.println(dataMap);
                            sResponceToClient = "250" + CRLF;

                        }
                        else
                        {
                            //missing rcpt or mail from 
                            //503	Bad sequence of commands
                            System.out.println("Missing RCPT or MAIL FROM");
                            sResponceToClient = "503" + CRLF;

                        }
                    }
                    else if (clientMSG.contains("RCPT")&&GO_ON_CHECKS)
                    {
                        //Helo check only in rcpt as recommended in 
                        if (isHelo)
                        {
                            String rcpt;
                            rcpt = clientMSG.replace("RCPT TO:","").replaceAll("\\<|>","").replace(CRLF,"").trim();
                            fPath_buffer.add(rcpt);

                            if (!fPath_buffer.isEmpty() && !rPath_buffer.isEmpty())
                            {
                                isReady = true;
                                sResponceToClient = "354" + CRLF;
                                System.out.println("Server Ready To Recieve Data");
                            }
                        }
                        else
                        {
                            //no HELO
                            //503	Bad sequence of commands
                            
                            sResponceToClient = "503" + CRLF;
                            System.out.println("Missing HELO");
                        }
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
            catch (Exception except){
                //Exception thrown (except) when something went wrong, pushing message to the console
                System.out.println("Error --> " + except.getMessage());
            }        
        }
    }
    

    