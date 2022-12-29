import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;




public class Client {

    //Main Method:- called when running the class file.
    public static void main(String[] args){ 
        
        int portNumber = 5000;
        String serverIP = "localhost";   
        
        try{
        //Create a new socket for communication
            Socket soc = new Socket(serverIP,portNumber);
        // use a semaphpre for thread synchronisation
            AtomicBoolean isDATA = new AtomicBoolean(false);
        // create new instance of the client writer thread, intialise it and start it running
            ClientReader clientRead = new ClientReader(soc, isDATA);
            Thread clientReadThread = new Thread(clientRead);
        //Thread.start() is required to actually create a new thread 
        //so that the runnable's run method is executed in parallel.
        //The difference is that Thread.start() starts a thread that calls the run() method,
        //while Runnable.run() just calls the run() method on the current thread
            clientReadThread.start();
            
        // create new instance of the client writer thread, intialise it and start it running
            ClientWriter clientWrite = new ClientWriter(soc, isDATA);
            Thread clientWriteThread = new Thread(clientWrite);
            clientWriteThread.start();
        }
        catch (Exception except){
            //Exception thrown (except) when something went wrong, pushing message to the console
            System.out.println("Error in SMTP_Client --> " + except.getMessage());
        }
    }
}



//This thread is responcible for writing messages
class ClientReader implements Runnable
{
    public static String ClientDomainName = "MyTestDomain.gr";
    public static String CRLF = "\r\n";
    public static String LF = "\n";
    
    Socket crSocket = null;
    AtomicBoolean isDATAflag;
    String BYTESin= "";
    String sDataToServer;
    public static String forwardpathString = "";
    
    
    
    public ClientReader (Socket inputSoc, AtomicBoolean isDATA)
    {
        crSocket = inputSoc;
        this.isDATAflag = isDATA;  
    }


    public void run(){
    





        while(!crSocket.isClosed() && !isDATAflag.get())
        {
        // while connection is open and NOT IN DATA exchange STATE

            try
            {
                String skey = Keygen.keygenerator(Keygen.timetoseed());
                DataInputStream dataIn = new DataInputStream(crSocket.getInputStream());
                BYTESin = AES.decrypt(dataIn.readUTF(),skey);
                if (BYTESin.contains("221"))  
                {
                    System.out.println("Service closing transmission channel");
                    crSocket.close();
                    return;
                }
                else if  (BYTESin.contains("200"))
                    System.out.println("(nonstandard success response, see rfc876)");
                else if (BYTESin.contains("211"))
                    System.out.println("System status, or system help reply\n"+BYTESin.replace("211",""));
                else if (BYTESin.contains("214"))
                    System.out.println("Help message\n"+BYTESin.replace("214",""));
                else if (BYTESin.contains("220"))
                    System.out.println("Service ready");
                else if (BYTESin.contains("250"))  
                    System.out.println("(250) OK\tRequested mail action okay, completed");
                else if (BYTESin.contains("251")) 
                    System.out.println("User not local; will forward to "+ forwardpathString);
                else if (BYTESin.contains("252"))
                    System.out.println("Cannot VRFY user, but will accept message and attempt delivery");
                else if (BYTESin.contains("354"))
                    System.out.println("Server is ready for (4) DATA command");
                else if (BYTESin.contains("421"))  
                    System.out.println("Service not available, closing transmission channel");
                else if (BYTESin.contains("450"))
                    System.out.println("Requested mail action not taken: mailbox unavailable");
                else if (BYTESin.contains("451"))
                    System.out.println("Requested action aborted: local error in processing");
                else if (BYTESin.contains("452"))
                    System.out.println("Requested action not taken: insufficient system storage");
                else if (BYTESin.contains("500"))  
                    System.out.println("Syntax error, command unrecognised");
                else if (BYTESin.contains("501"))  
                    System.out.println("Syntax error in parameters or arguments");
                else if (BYTESin.contains("503"))
                    System.out.println("Bad sequence of commands");        
                else if (BYTESin.contains("504"))  
                    System.out.println("Command parameter not implemented");
                else if (BYTESin.contains("521"))
                    System.out.println("Domain does not accept mail (see rfc1846)");
                else if (BYTESin.contains("550"))
                    System.out.println("Requested action not taken: mailbox unavailable");
                else if (BYTESin.contains("551"))
                    System.out.println("User not local; please try"+ forwardpathString);
                else if (BYTESin.contains("552"))
                    System.out.println("Requested mail action aborted: exceeded storage allocation");  
                else if (BYTESin.contains("553"))
                    System.out.println("Requested action not taken: mailbox name not allowed");
                else if (BYTESin.contains("554"))
                    System.out.println("Transaction failed");
                 
            }  
            catch (Exception except){
              //Exception thrown (except) when something went wrong, pushing message to the console
              System.out.println("Error in ClientReader --> " + except.getMessage());
            }
        }
    }
}


class ClientWriter implements Runnable
{
    public static String CRLF = "\r\n";
    public static String LF = "\n";   
    public static String SP= " ";
    public static String ClientDomainName = "MyTestDomain.gr";
    public static String ClientEmailAddress = "myEmail@"+ClientDomainName;
    
    
    Socket cwSocket = null;
    AtomicBoolean isDATAflag;
    
    
    public ClientWriter (Socket outputSoc, AtomicBoolean isDATA)
    {
        cwSocket = outputSoc;
        this.isDATAflag=isDATA;
    } 
    



    public void run(){
        String msgToServer ="";
        ///String BYTESin= "";
        String ClientDomainName = "MyTestDomain.gr";
        Boolean isLoggedIn =  false;
        int triesCounter = 3;
        String email="";
        String password ="";
    
        try{
            System.out.println ("CLIENT: SELECT COMMAND 1-HELO 2-MAIL FROM 3-RCTP TO 4-DATA 5-RSET 6-VRFY 7-EXPN 8-HELP 9-NOOP 10-QUIT");
            DataOutputStream dataOut = new DataOutputStream(cwSocket.getOutputStream());
            Scanner user_input = new Scanner(System.in);
            do 
            {
                //login
                System.out.println("Email:");
                email = user_input.nextLine();
                System.out.println("Password:");
                password =  user_input.nextLine();
                dataOut.writeUTF(AES.encrypt("LOGIN"+email+"|"+password,Keygen.keygenerator(Keygen.timetoseed()))); // password cant have "|" if it does we cant split (not implemented)
                dataOut.flush();  





                if (isLoggedIn)
                {
                    while (!cwSocket.isClosed()) {
                        
                        switch(user_input.nextInt()){
                            ///default:{
                            ///           
                            ///break;
                            ///}
                            case 1: {
                                System.out.println("HELO\n---------------------------");
                                //
                                // SYNTAX (page 12 RFC 821)
                                // HELO <SP> <domain> <CRLF>
                                //
                                msgToServer = ("HELO"+SP+ClientDomainName+CRLF);
                                String key = Keygen.keygenerator(Keygen.timetoseed());
                                String msgToServerEnc = AES.encrypt(msgToServer,key);
                                dataOut.writeUTF(msgToServerEnc);
                                dataOut.flush();                         
                                break;
                            }
                            case 2: {
                                System.out.println("MAIL FROM\n----------------------------------");
                                msgToServer ="MAIL"+SP+"FROM:"+"<"+ClientDomainName +">"+CRLF;
                                String key = Keygen.keygenerator(Keygen.timetoseed());
                                String msgToServerEnc = AES.encrypt(msgToServer,key);
                                dataOut.writeUTF(msgToServerEnc);
                                dataOut.flush();
                                break;
                            }                    
                            case 3: {
                                System.out.println("RCPT TO\n----------------------------");  
                                Scanner scanner = new Scanner(System.in);    
                                String input = scanner.nextLine();
                                msgToServer ="RCPT" + SP + "TO:"+ "<" + input + ">" + CRLF;
                                ClientReader.forwardpathString = input;
                                String key = Keygen.keygenerator(Keygen.timetoseed());
                                String msgToServerEnc = AES.encrypt(msgToServer,key);
                                dataOut.writeUTF(msgToServerEnc);
                                dataOut.flush();

                                break;
                            } 
                            case 4: {
                                System.out.println("DATA\n----------------------------\nInput the data you want to send and hit enter (max size 502 bytes)");
                                Scanner terminalInput = new Scanner(System.in);                 
                                String input = terminalInput.nextLine();
                                msgToServer = "DATA"+"<"+input+">"+CRLF; 
                                String key = Keygen.keygenerator(Keygen.timetoseed());
                                String msgToServerEnc = AES.encrypt(msgToServer,key);
                                dataOut.writeUTF(msgToServerEnc);
                                dataOut.flush();      
                                break;
                            }
                            case 5:{//rset no sp needed
                                System.out.println("RSET\n----------------------------");
                                msgToServer = "RSET"+CRLF;
                                String key = Keygen.keygenerator(Keygen.timetoseed());
                                String msgToServerEnc = AES.encrypt(msgToServer,key);
                                dataOut.writeUTF(msgToServerEnc);
                                dataOut.flush();
                                break;
                            }
                            case 6:{
                                System.out.println("VRFY\n----------------------------");
                                Scanner terminalInput = new Scanner(System.in);                 
                                String input = terminalInput.nextLine();
                                msgToServer ="VRFY"+SP+"<"+input+">"+CRLF;
                                String key = Keygen.keygenerator(Keygen.timetoseed());
                                String msgToServerEnc = AES.encrypt(msgToServer,key);
                                dataOut.writeUTF(msgToServerEnc);
                                dataOut.flush();
                                break;
                            }
                            case 7: {
                                System.out.println("EXPN\n----------------------------");

                                break;
                            }
                            case 8:{
                                System.out.println("HELP\n----------------------------\nType the command you need help with choose from bellow\n HELLO/MAIL/RCPT/DATA/RSET/VRFY/EXPN/NOOP/QUIT");
                                Scanner terminalInput = new Scanner(System.in);                 
                                String input = terminalInput.nextLine();
                                System.out.println("Sending request....");
                                msgToServer ="HELP"+SP+input+CRLF;
                                String key = Keygen.keygenerator(Keygen.timetoseed());
                                String msgToServerEnc = AES.encrypt(msgToServer,key);
                                dataOut.writeUTF(msgToServerEnc);
                                dataOut.flush();
                                break;
                            }
                            case 9:{
                                System.out.println("NOOP\n----------------------------");
                                msgToServer = ("NOOP"+CRLF);
                                String key = Keygen.keygenerator(Keygen.timetoseed());
                                String msgToServerEnc = AES.encrypt(msgToServer,key);
                                dataOut.writeUTF(msgToServerEnc);
                                dataOut.flush();
                                System.out.println("....Wating OK");
                                break;
                            }
                            case 10:{
                                System.out.println("QUIT\n----------------------------");                       
                                msgToServer = ("QUIT"+CRLF);
                                String key = Keygen.keygenerator(Keygen.timetoseed());
                                String msgToServerEnc = AES.encrypt(msgToServer,key);
                                dataOut.writeUTF(msgToServerEnc);
                                dataOut.flush();                         
                                System.out.println("...Socket closing");
                                user_input.close();
                                break;
                            }
                            default:{
                             /// reutrn code 5xx
                            }



                        }       
                    }
                }
                else if (triesCounter>0)
                {
                    triesCounter--;
                    System.out.println("Wrong log in information check email and password \n Tries to log in left: "+ triesCounter);
                }
                else
                {
                    System.out.println("No more tries left the application will exit");
                    cwSocket.close();
                    break;
                }
            } 
            while (!isLoggedIn);            
        }           
        catch (Exception except){
            //Exception thrown (except) when something went wrong, pushing message to the console
            System.out.println("Client Error: " + except.getMessage());
        }
    }
    
}