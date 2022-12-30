import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;




public class Client 
{

    //Main
    public static void main(String[] args)
    { 
        
        int portNumber = 5000;
        String serverIP = "localhost";   
        
        try{
         //Open new socket to start traffic
            Socket soc = new Socket(serverIP,portNumber);
         // Initialize ClientReader Thread and start it
            ClientReader clientRead = new ClientReader(soc);
            Thread clientReadThread = new Thread(clientRead);
            clientReadThread.start();
            
         // Initialize ClientWriter Thread and start it
            ClientWriter clientWrite = new ClientWriter(soc);
            Thread clientWriteThread = new Thread(clientWrite);
            clientWriteThread.start();
        }
        catch (Exception except)
        {
            //Exception thrown (except) when something went wrong, pushing message to the console
            System.out.println("Client Error: " + except.getMessage());
        }
    }
}



// This thread Reads incomming traffic
class ClientReader implements Runnable
{
    //Initialization of variables
    public static String CRLF = "\r\n";
    public static String LF = "\n";
    
    static Socket clientReaderSocket = null;
    String DataIn= "";
    String dataToServerString;
    public static String forwardpathString = "";
    int loginCounter = 3; //counts down how many login attemps you have left after 3 exits the application
    
    public ClientReader (Socket inputSoc)
    {
        clientReaderSocket = inputSoc;
    }

    public void run(){
        while(!clientReaderSocket.isClosed())
        {
            try
            {
                //Keygenerator generates a key for AES more in README.txt
                String skey = Keygen.keygenerator(Keygen.timetoseed());
                //incomming data from the socket
                DataInputStream dataInStream = new DataInputStream(clientReaderSocket.getInputStream());    
                DataIn = AES.decrypt(dataInStream.readUTF(),skey);                               

                //all error code generatable and non implimented and some extra ones for other functions
                if (DataIn.contains("221"))  
                {
                    System.out.println("Service closing transmission channel");
                    clientReaderSocket.close();
                    return;
                }
                else if (DataIn.contains("LOGGED"))                
                    ClientWriter.isLoggedIn = true;
                else if (DataIn.contains("FAILED"))
                    ClientWriter.isLoggedIn = false;
                else if(DataIn.contains("LIST"))
                    System.out.println(DataIn.replace("LIST","").replace(CRLF, ""));
                else if  (DataIn.contains("200"))
                    System.out.println("(nonstandard success response, see rfc876)");
                else if (DataIn.contains("211"))
                    System.out.println("System status, or system help reply\n"+DataIn.replace("211",""));
                else if (DataIn.contains("214"))
                    System.out.println("Help message\n"+DataIn.replace("214",""));
                else if (DataIn.contains("220"))
                    System.out.println("Service ready");
                else if (DataIn.contains("250"))  
                    System.out.println("(250) OK\tRequested mail action okay, completed");
                else if (DataIn.contains("251")) 
                    System.out.println("User not local; will forward to "+ forwardpathString);
                else if (DataIn.contains("252"))
                    System.out.println("Cannot VRFY user, but will accept message and attempt delivery");
                else if (DataIn.contains("354"))
                    System.out.println("Server is ready for (4) DATA command");
                else if (DataIn.contains("421"))  
                    System.out.println("Service not available, closing transmission channel");
                else if (DataIn.contains("450"))
                    System.out.println("Requested mail action not taken: mailbox unavailable");
                else if (DataIn.contains("451"))
                    System.out.println("Requested action aborted: local error in processing");
                else if (DataIn.contains("452"))
                    System.out.println("Requested action not taken: insufficient system storage");
                else if (DataIn.contains("500"))  
                    System.out.println("Syntax error, command unrecognised");
                else if (DataIn.contains("501"))  
                    System.out.println("Syntax error in parameters or arguments");
                else if (DataIn.contains("503"))
                    System.out.println("Bad sequence of commands");        
                else if (DataIn.contains("504"))  
                    System.out.println("Command parameter not implemented");
                else if (DataIn.contains("521"))
                    System.out.println("Domain does not accept mail (see rfc1846)");
                else if (DataIn.contains("550"))
                    System.out.println("Requested action not taken: mailbox unavailable\\Not implemented");
                else if (DataIn.contains("551"))
                    System.out.println("User not local; please try"+ forwardpathString);
                else if (DataIn.contains("552"))
                    System.out.println("Requested mail action aborted: exceeded storage allocation");  
                else if (DataIn.contains("553"))
                    System.out.println("Requested action not taken: mailbox name not allowed");
                else if (DataIn.contains("554"))
                    System.out.println("Transaction failed");
                 
            }  
            catch (Exception except)
            {
              //Exception thrown (except) when something went wrong, pushing message to the console
              System.out.println("Error in ClientReader --> " + except.getMessage());
            }
        }
    }
}


class ClientWriter implements Runnable
{
    //Variable Initialization 
    public static boolean isLoggedIn;
    public static String CRLF = "\r\n";
    public static String LF = "\n";   
    public static String SP= " ";
    public static String ClientDomainName = "MyTestDomain.gr";
    public static String ClientEmailAddress = "myEmail@"+ClientDomainName;
    
    Socket cwSocket = null;
    
    public ClientWriter (Socket outputSoc)
    {
        cwSocket = outputSoc;
    } 
    



    public void run(){
        //Variable Initialization 
        String msgToServer ="";
        int triesCounter = 3;

        String email="";
        String password ="";
        Boolean goBack = false;
        Boolean writeOnce = false;
    
        try{
            //Outgoing dataStream
            DataOutputStream dataOut = new DataOutputStream(cwSocket.getOutputStream());
            Scanner user_input = new Scanner(System.in);
            do 
            {
                //login
                if(!isLoggedIn)
                {

                    Scanner logScanner = new Scanner(System.in);
                    System.out.println("Email:");
                    email = logScanner.nextLine();
                    System.out.println("Password:");
                    password = logScanner.nextLine();
                    String key = Keygen.keygenerator(Keygen.timetoseed());  //more fore keygenerator in txt and in class
                    dataOut.writeUTF(AES.encrypt("LOGIN"+SP+email+" | "+password+SP+CRLF,key));     // password cant have "|" if it does we cant split (not implemented)
                    dataOut.flush(); //flashes the data
                    System.out.println("Waiting on server.....");
                    TimeUnit.MILLISECONDS.sleep(2000); //sleep accounted for any delays on email/password authentication
                    triesCounter--;
                    if(!isLoggedIn) // boolean that checks if you got logged in changes in ClientReader when it gets the confirmation from the server that the data matches
                        System.out.println("Wrong log in information check email and password \n Tries to log in left: "+ triesCounter);
                }
                else if(!isLoggedIn)        //idk what im doing here but it works ¯\_(ツ)_/¯ if else if with the same condition...??
                {               
                    break;
                }
                if (isLoggedIn)         //pass though when you get loggedin
                {

                    //this boolean allows you to go back to menu when the specific button is press and after that it gets reset(has to be in the loop)
                    goBack = false;
                    System.out.println("MENU\n 1-NEWMAIL 2-MAILBOX 3-QUIT 4-LOGOUT");
                    switch(user_input.nextInt())
                    {   
                        case 1:
                        {
                            System.out.println ("CLIENT: SELECT COMMAND 0-MENU 1-HELO 2-MAIL FROM 3-RCTP TO 4-DATA 5-RSET 6-VRFY 7-EXPN 8-HELP 9-NOOP 10-EHLO 11-QUIT");
                            while (!cwSocket.isClosed()&&!goBack) 
                            {
                        
                                switch(user_input.nextInt())
                                {
                                    case 0:
                                    {
                                        goBack =true;
                                        break;
                                    }
                                    case 1: 
                                    {
                                        System.out.println("HELO\n---------------------------");
                                        //
                                        // SYNTAX (page 12 RFC 821)
                                        // HELO <SP> <domain> <CRLF>
                                        //
                                        msgToServer = ("HELO"+SP+ClientDomainName+CRLF);
                                        String key = Keygen.keygenerator(Keygen.timetoseed()); //key generation
                                        String msgToServerEnc = AES.encrypt(msgToServer,key);  //encryption with the key (key is never shared between server and client)
                                        dataOut.writeUTF(msgToServerEnc);                      //prepares data to flush
                                        dataOut.flush();                                       //flushes the data 
                                        break;
                                    }
                                    case 2: 
                                    {
                                        System.out.println("MAIL FROM\n----------------------------------");
                                        msgToServer ="MAIL"+SP+"FROM:"+"<"+ email +">"+CRLF;            
                                        String key = Keygen.keygenerator(Keygen.timetoseed());                  //key generation
                                        String msgToServerEnc = AES.encrypt(msgToServer,key);                   //encryption with the key (key is never shared between server and client)    
                                        dataOut.writeUTF(msgToServerEnc);                                       //prepares data to flush  
                                        dataOut.flush();                                                        //flushes the data         
                                        break;
                                    }                    
                                    case 3: 
                                    {
                                        System.out.println("RCPT TO\n----------------------------");
                                        Scanner lineinput = new Scanner(System.in);                             //Scanner initialization (for some reason if i dont start a new one the old one crashes and the socket closes)
                                        String input = lineinput.nextLine();                                   //reads nextline
                                        msgToServer ="RCPT" + SP + "TO:"+ "<" + input + ">" + CRLF;
                                        ClientReader.forwardpathString = input;
                                        String key = Keygen.keygenerator(Keygen.timetoseed());                            
                                        String msgToServerEnc = AES.encrypt(msgToServer,key);                       
                                        dataOut.writeUTF(msgToServerEnc);                                                   
                                        dataOut.flush();                                                                        
                                        break;
                                    } 
                                    case 4: 
                                    {
                                        System.out.println("DATA\n----------------------------\nInput the data you want to send and hit enter (max size 502 bytes)");
                                        Scanner lineinput = new Scanner(System.in);            
                                        String input = lineinput.nextLine();
                                        msgToServer = "DATA"+"<"+input+">"+CRLF; 
                                        String key = Keygen.keygenerator(Keygen.timetoseed());
                                        String msgToServerEnc = AES.encrypt(msgToServer,key);
                                        dataOut.writeUTF(msgToServerEnc);
                                        dataOut.flush();      
                                        break;
                                    }
                                    case 5:
                                    {//rset no sp needed
                                        System.out.println("RSET\n----------------------------");
                                        msgToServer = "RSET"+CRLF;
                                        String key = Keygen.keygenerator(Keygen.timetoseed());
                                        String msgToServerEnc = AES.encrypt(msgToServer,key);
                                        dataOut.writeUTF(msgToServerEnc);
                                        dataOut.flush();
                                        break;
                                    }
                                    case 6:
                                    {
                                        System.out.println("VRFY\n----------------------------");  
                                        Scanner lineinput = new Scanner(System.in);          
                                        String input = lineinput.nextLine();
                                        msgToServer ="VRFY"+SP+"<"+input+">"+CRLF;
                                        String key = Keygen.keygenerator(Keygen.timetoseed());
                                        String msgToServerEnc = AES.encrypt(msgToServer,key);
                                        dataOut.writeUTF(msgToServerEnc);
                                        dataOut.flush();
                                        break;
                                    }
                                    case 7: 
                                    {
                                        System.out.println("EXPN\n----------------------------");
                                        Scanner lineinput = new Scanner(System.in);
                                        String input = lineinput.nextLine();
                                        msgToServer="EXPN"+SP+"<"+input+">"+CRLF;
                                        String key = Keygen.keygenerator(Keygen.timetoseed());
                                        String msgToServerEnc = AES.encrypt(msgToServer,key);
                                        dataOut.writeUTF(msgToServerEnc);
                                        dataOut.flush();
                                        break;
                                    }
                                    case 8:
                                    {
                                        System.out.println("HELP\n----------------------------\nType the command you need help with choose from bellow\n HELO/MAIL/RCPT/DATA/RSET/VRFY/EXPN/NOOP/QUIT");            
                                        String input = user_input.nextLine();
                                        System.out.println("Sending request....");
                                        msgToServer ="HELP"+SP+input+CRLF;
                                        String key = Keygen.keygenerator(Keygen.timetoseed());
                                        String msgToServerEnc = AES.encrypt(msgToServer,key);
                                        dataOut.writeUTF(msgToServerEnc);
                                        dataOut.flush();
                                        break;
                                    }
                                    case 9:
                                    {
                                        System.out.println("NOOP\n----------------------------");
                                        msgToServer = ("NOOP"+CRLF);
                                        String key = Keygen.keygenerator(Keygen.timetoseed());
                                        String msgToServerEnc = AES.encrypt(msgToServer,key);
                                        dataOut.writeUTF(msgToServerEnc);
                                        dataOut.flush();
                                        System.out.println("....Wating OK");
                                        break;
                                    } 
                                    case 10:
                                    {
                                        System.out.println("EHLO\n----------------------------");   // not implimented but still sends the request and gets back 550
                                        msgToServer = ("EHLO"+CRLF);
                                        String key = Keygen.keygenerator(Keygen.timetoseed());
                                        String msgToServerEnc = AES.encrypt(msgToServer,key);
                                        dataOut.writeUTF(msgToServerEnc);
                                        dataOut.flush();
                                        break;
                                    }
                                    case 11:
                                    {
                                        System.out.println("QUIT\n----------------------------");                       
                                        msgToServer = ("QUIT"+CRLF);
                                        String key = Keygen.keygenerator(Keygen.timetoseed());
                                        String msgToServerEnc = AES.encrypt(msgToServer,key);
                                        dataOut.writeUTF(msgToServerEnc);
                                        dataOut.flush();                         
                                        System.out.println("...Socket closing");
                                        user_input.close();
                                        ClientReader.clientReaderSocket.close();
                                        break;
                                    }
                                    default:
                                    {
                                     System.out.println("Non valid input try again");                  //any other input thats not supported
                                    }
                                }       
                            }
                            break;
                        }
                        case 2:
                        { 
                            Boolean keeplooping = true;
                            Boolean firstpass = true;
                            do
                            {
                                if (firstpass)
                                {
                                    int counter = 1;
                                    dataOut.writeUTF(msgToServer);
                                    dataOut.flush();
                                    List <Integer> lines = storageReaderWriter.compare("serverstorage.txt", email);                //all mehtods explained in their classes
                                    List <String> mailList = storageReaderWriter.readOnlyXLines("serverstorage.txt", lines);     
                                    System.out.println("Press ` (tilde) to return to MENU and 0 to Refresh\nX:Index [RECIPIENTS][SENDER]{DATA=TIMESTAMP}");   //the application is 95% done to delete the mails as well those with multiple recipients only u as the recipient gets deleted so it doesnt interfier with other recipients
                                    for (int i = 0; i < mailList.size(); i++) //counts and prints the mails that have you as the recipient with a counter infront(thats supposed to corrispond to input to select a mail)
                                    {
                                        String str = mailList.get(i);
                                        System.out.println(counter + ": " + str);
                                        counter++;
                                    }
                                    firstpass = false;
                                    
                                    
                                    while (true)
                                    {
                                       if (lines.isEmpty())// if there is no mails to show
                                       {   
                                           if(!writeOnce) 
                                           {
                                           System.out.println("Your Mailbox is Empty");
                                           writeOnce = true;
                                           }
                                       }

                                        char inputchar = user_input.next().charAt(0);//returns you to the previous menu by breaking out of 2 loops
                                        if (inputchar=='`')
                                        {
                                            writeOnce = false;
                                            keeplooping = false;
                                            firstpass = true;
                                            break;
                                        }
                                        else if (inputchar=='0')    //refreshes the mailbox by exiting the loop and re-entering
                                        {                           
                                            writeOnce = false;
                                            firstpass = true;
                                            break;
                                        }
                                        else
                                        {   
                                            System.out.println("Wrong input");
                                        }
                                    }
                                }
                            }
                            while(keeplooping);
                            break;
                        }
                        case 3:
                        {
                            //Quit closes socket and threads therfore communications end 
                            msgToServer = ("QUIT"+CRLF);
                            String key = Keygen.keygenerator(Keygen.timetoseed());
                            String msgToServerEnc = AES.encrypt(msgToServer,key);
                            dataOut.writeUTF(msgToServerEnc);
                            dataOut.flush();                         
                            System.out.println("...Socket closing");
                            user_input.close();
                            ClientReader.clientReaderSocket.close();
                            break;
                        }
                        case 4:
                        {   //loggs you out of the client
                            isLoggedIn = false;
                            break;
                        }
                        default :
                        {
                            System.out.println("WRONG INPUT AVAILABLE INPUTS 1-3\nMENU\n 1-NEWMAIL 2-MAILBOX 3-QUIT 4-LOGGOUT");
                        }
                    }
                }
            }
            while (true);           
        }           
        catch (Exception except)
        {
            //Exception thrown (except) when something went wrong, pushing message to the console
            System.out.println("Client Error: " + except.getMessage());
        }
    }
}       