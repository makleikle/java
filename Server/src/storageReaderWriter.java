import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class storageReaderWriter
{
    static String[] read() throws FileNotFoundException
    {
    String[] dummyStrings = new String[512];
    try (BufferedReader br = new BufferedReader(new FileReader("serverstorage.txt"))) 
      {
        int i=0;
        String line;
        
        while ((line = br.readLine()) != null) 
        {
          dummyStrings[i] = line ;
          i++;
        }
      } catch (IOException e) 
      {
        e.printStackTrace();
      }
      //decrypt first
    return dummyStrings;
    }
    static boolean write(String authorString) throws FileNotFoundException
    {
      try (BufferedWriter bw = new BufferedWriter(new FileWriter("serverstorage.txt")))
      {
       bw.write(authorString);
       bw.flush();
       return true;
      }
      catch(IOException a)
      {
        a.printStackTrace();
        return false;
      }
    }
    
}