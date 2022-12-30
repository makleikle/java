import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    
    public static void main(String[] args)
    { 
        // portNumber that the data traffic will take place
        int portNumber = 5000;
        try{
            // Socket and socketManager Initialization
            ServerSocket serverSocket = new ServerSocket(portNumber);
            ArrayList<socketManager> clients = new ArrayList<socketManager>();
            
            while (true)
            {   
                // Server waits for clinet to join in 
                // when a client join they gain a temporary id number (till they disconnect from the socket)
                // So they are seperated by the socketManager             
                System.out.println("Waiting for client");
                Socket soc = serverSocket.accept();
                socketManager temp = new socketManager(soc);
                clients.add(temp);
                ServerConnectionHandler SconHandler = new ServerConnectionHandler(clients, temp);
                Thread sConThread = new Thread(SconHandler);
                sConThread.start();
            }
            
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }   
}