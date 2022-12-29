import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

public class storageReaderWriter
{
    static String[] read() throws FileNotFoundException{
    String[] dummyStrings={"","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""};
    URL path = storageReaderWriter.class.getResource("serverStorage.txt");
    File serverstoragFile = new File(path.getFile());
    try (BufferedReader br = new BufferedReader(new FileReader(serverstoragFile))) 
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
    return dummyStrings;
    }
    private void write(String[]sequenceString) throws IOException
    {
      URL path = storageReaderWriter.class.getResource("serverStorage.txt");
      File serverstoragFile = new File(path.getFile());
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(serverstoragFile)))
      {
        

        
      }

    }
    
}