import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class storageReaderWriter {
    static String[] read(Boolean onlyfirst) throws FileNotFoundException 
    {
        int lineCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("serverstorage.txt"))) 
        {
            while (br.readLine() != null) 
            {
                lineCount++;
            }
        } catch (IOException e) 
        {
            e.printStackTrace();
        }
        String[] dummyStrings = new String[lineCount];
        try (BufferedReader br = new BufferedReader(new FileReader("serverstorage.txt"))) 
        {
            int i = 0;
            String line;
            while ((line = br.readLine()) != null) 
            {
                String key = Keygen.keygenerator(Long.parseLong(splicer.splicelast(line)));
                String finalsString = AES.decrypt(splicer.splicefirst(line), key);
                if (onlyfirst) 
                {
                    dummyStrings[i] = finalsString;
                    i++;
                } else 
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
        int lineCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("serverstorage.txt"))) 
        {
            while (br.readLine() != null) 
            {
                lineCount++;
            }
        } catch (IOException e) 
        {
            e.printStackTrace();
        }
        return lineCount;
    }

    static boolean write(String authorString) throws FileNotFoundException 
    {
      try (BufferedWriter bw = new BufferedWriter(new FileWriter("serverstorage.txt", true))) 
      {
          if (countlines()>0)
          {
          bw.newLine();
          }
          bw.write(authorString);
          bw.flush();
          return true;
      } catch (IOException a) 
      {
          a.printStackTrace();
          return false;
      }
  }
}
