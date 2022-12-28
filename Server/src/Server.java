import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    
    public static void main(String[] args){ 
        
        int portNumber = 5000;
        try{
            ServerSocket serverSoc = new ServerSocket(portNumber);
            ArrayList<socketManager> clients = new ArrayList<socketManager>();
            
            while (true){               
                System.out.println("Waiting for client");
                Socket soc = serverSoc.accept();
                socketManager temp = new socketManager(soc);
                clients.add(temp);
                ServerConnectionHandler Sch = new ServerConnectionHandler(clients, temp);
                Thread schThread = new Thread(Sch);
                schThread.start();
            }
            
        }
        catch (Exception except){
            System.out.println("Error --> " + except.getMessage());
        }
    }   
}