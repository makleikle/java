import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class storageReaderWriter 
{
  static String[] read(Boolean onlyfirst, String fileName) throws FileNotFoundException 
  {
    int lineCount = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) 
    {
        while (br.readLine() != null) 
        {
            lineCount++;
        }
    } 
    catch (IOException e) 
    {
        e.printStackTrace();
    }
    String[] dummyStrings = new String[lineCount];
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) 
    {
        int i = 0;
        String line;
        while ((line = br.readLine()) != null) 
        {
            String key = Keygen.keygenerator(Long.parseLong(splicer.splicelast(line,13,3,"/")));
            String finalsString = AES.decrypt(splicer.splicefirst(line,16), key);
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
    } 
    catch (IOException e) 
    {
        e.printStackTrace();
    }
    return dummyStrings;
  }
  public void delete(String filename, int lineNumber, String email) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(filename));
    if (lineNumber > 0) {
        lines.remove(lineNumber - 1);
    } else {
        String decryptedLine=readOnlyXLine(filename, lineNumber);
        Pattern recipientPattern = Pattern.compile("^\\[(.+?)\\]");
        Matcher recipientMatcher = recipientPattern.matcher(decryptedLine);
        if (recipientMatcher.find()) {
            String recipients = recipientMatcher.group(1);
            String updatedRecipients = recipients.replace(email, "");
            decryptedLine = decryptedLine.replace(recipients, updatedRecipients);
            lines.set(Math.abs(lineNumber) - 1, decryptedLine);
        }
    }
    Files.write(Paths.get(filename), lines);
}
  static int countlines(String fileName) throws FileNotFoundException 
  {
    int lineCount = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) 
    {
        while (br.readLine() != null) 
        {
            lineCount++;
        }
    } 
    catch (IOException e) 
    {
        e.printStackTrace();
    }
    return lineCount;
  }
  public static List<Integer> compare(String fileName, String compareString) throws IOException {
    Path filePath = Paths.get(fileName);
    List<Integer> lineNumbers = new ArrayList<>();

    try (BufferedReader reader = Files.newBufferedReader(filePath)) 
    {
      String line;
      int lineNumber = 0;
      while ((line = reader.readLine()) != null) 
      {
        lineNumber++;
        // Decrypt the line
        String key = Keygen.keygenerator(Long.parseLong(splicer.splicelast(line, 13, 3, "/")));
        String decryptedLine = AES.decrypt(splicer.splicefirst(line, 16), key);
        // Use a regular expression to extract the recipient from the decrypted line
        Pattern recipientPattern = Pattern.compile("^\\[(.+?)\\]");
        Matcher recipientMatcher = recipientPattern.matcher(decryptedLine);
        if (recipientMatcher.find()) 
        {
          String recipient = recipientMatcher.group(1);
          // Check if the extracted recipient matches the compareString
          if (recipient.contains(compareString)) 
          {
            // Check if the recipient contains strings separated by a comma
            if (recipient.contains(",")) {
              // Add the line number to the list with a negative value
              lineNumbers.add(-lineNumber);
            } else {
              // Add the positive value of the line number to the list
              lineNumbers.add(lineNumber);
            }
          }
        }
      }
    }

    return lineNumbers;
  }

  static boolean write(String authorString, String fileName) throws FileNotFoundException 
  {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) 
    {
        if (countlines(fileName)>0)
        {
        bw.newLine();
        }
        bw.write(authorString);
        bw.flush();
        return true;
    } 
    catch (IOException a) 
    {
        a.printStackTrace();
        return false;
    }
  }
  public static List<String> readOnlyXLines(String fileName, List<Integer> lineNumbers) throws FileNotFoundException 
  {
    List<String> lines = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) 
    {
        String line;
        int lineCount = 0;
        while ((line = br.readLine()) != null) 
        {
            lineCount++;
            if (lineNumbers.contains(lineCount)||lineNumbers.contains(lineCount*-1)) 
            {
                String key = Keygen.keygenerator(Long.parseLong(splicer.splicelast(line,13,3,"/")));
                String decryptedLine = AES.decrypt(splicer.splicefirst(line,16), key);
                lines.add(decryptedLine);
            }
        }
    } 
    catch (IOException e) 
    {
        e.printStackTrace();
    }
    return lines;
  }
  public static boolean authenticator(String fileName, String logininfo) 
  {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) 
    {
      String line;
      while ((line = reader.readLine()) != null) 
      {
        String key = Keygen.keygenerator(Long.parseLong(splicer.splicelast(line,13,3,"/")));
        String finalsString = AES.decrypt(splicer.splicefirst(line,16), key);

        if (finalsString.equals(logininfo)) 
        return true;           
      }
    }
    catch (IOException e) 
    {
      e.printStackTrace();
    }
    return false;
  }
  public static String readOnlyXLine(String fileName, int lineNumber) throws FileNotFoundException 
  {
    String theLine="";
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) 
    {
        String line;
        int lineCount = 0;
        while ((line = br.readLine()) != null) 
        {
            lineCount++;
            if (lineNumber==(lineCount)||lineNumber==(lineCount*-1)) 
            {
                String key = Keygen.keygenerator(Long.parseLong(splicer.splicelast(line,13,3,"/")));
                String decryptedLine = AES.decrypt(splicer.splicefirst(line,16), key);
                theLine=decryptedLine;
            }
        }
    } 
    catch (IOException e) 
    {
        e.printStackTrace();
    }
    return theLine;
  }
  
}