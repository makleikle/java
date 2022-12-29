import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class storageReaderWriter
{
    static String[] read(Boolean onlyfirst) throws FileNotFoundException
    {
    String[] dummyStrings = new String[countlines()-1];
    try (BufferedReader br = new BufferedReader(new FileReader("serverstorage.txt"))) 
      {
        int i=0;
        String line;
        //decrypt+split
        while ((line = br.readLine()) != null) 
        {
          String first = splicer.splicefirst(line);
          String last = splicer.splicelast(line);
          String key = Keygen.keygenerator(Long.parseLong(last));
          String finalsString = AES.decrypt(first, key);
          if (onlyfirst)
          {
          dummyStrings[i] = finalsString;
          i++;
          }
          else
          {
            dummyStrings[i] = line;
            i++;
          }
        }
      } catch (IOException e) 
      {
        e.printStackTrace();
      }
    return dummyStrings;
    }

    static int countlines() throws FileNotFoundException
    {
      int i=0;
      try (BufferedReader br = new BufferedReader(new FileReader("serverstorage.txt"))) 
      { 
        while (br.readLine() != null) 
        {
          i++;
        }
      } catch (IOException e) 
      {
        e.printStackTrace();
      }
      return i;
    }

    static boolean write(String authorString) throws FileNotFoundException
    {
      try (BufferedWriter bw = new BufferedWriter(new FileWriter("serverstorage.txt")))
      {
        //count lines add em back with \n inbetween before flushing
        Integer linesbefore = countlines();
        String[] olddata = new String[linesbefore];  //linebefore-1+1
        //read(false);
        for (int i = 0; i < linesbefore; i++)
        {
          olddata[i]=read(false)[i];
        }
        olddata[linesbefore+1] = authorString;
        authorString = String.join("\n",olddata);
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