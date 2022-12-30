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
  //reads line from given file if boolean is true it returns a string array with decrypted data of the whole file otherwise it returns the lines that have the data
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
          //Decrypts each line until it gets to a null line
            String key = Keygen.keygenerator(Long.parseLong(splicer.splicelast(line,13,3,"/")));
            String finalsString = AES.decrypt(splicer.splicefirst(line,16), key);
            if (onlyfirst) 
            {
              //saves strings into the array
                dummyStrings[i] = finalsString;
                i++;
            } 
            else 
            {
              //saves the line number into the array
                dummyStrings[i] = line;
                i++;
            }
        }
    } 
    catch (IOException e) 
    {
        e.printStackTrace();
    }
    return dummyStrings; //returns a string array with the decrypted lines
  }
    //delete does two things lineNumber can be negative to show that there are two recipients and email is the email of the user thats logged in
    //if the number is possative removes the full line since there arent anyother recipients and the mail is safe to be removed from the serverstorage and removes
    //if its negative it creates a pattern to find the first list where the recipients are saved and extracts the given email from that after getting the line back
    //decrypted from readOnlyXLine(returns the string decrypted) (not readOnlyXLines returns list with the lines) which takes the lineNumber index and decrypts it 
    //and sends it back as a string all files in data are stored in this pattern ([RECIPIENT\S][SENDER]{DATA=TIMESTAMP})dd/MM/yyy/HH/mm the decryption also alows you to splice
    //the sting so u just remove the last 16 that are not decrypted then you decrypt the part in (parenthsis)but do decrypt it u need to remove the last 3 chars and then take the 
    //13 last remaining ones and removing all "/" with regex to get the seed to be randomized and finilay used as a key for the decryption also this method is made to not leave 
    //empty lines since they can be read saddly i tried to code that in and almost bricked the app so im not gonna touch it dont want to go to older commits and losing valuable time
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
  //this method counts the lines that are not null helps on other methods that need to know how many lines there are in the txt
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

  //compare method takes a sting and compares it to the first patterns it finds which returns whatever is in the first set of brackets [RECIPIENT\S] and then
  //compares them with the user thats logged in to check if that email was reffering to this specific user and determings if it show up or not if there is a comma in 
  // the brackets it save the line with a negative value for the reasons that were mentioned above in delete 
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
        // Regex to extract the recipient from the decrypted line
        Pattern recipientPattern = Pattern.compile("^\\[(.+?)\\]");
        Matcher recipientMatcher = recipientPattern.matcher(decryptedLine);
        if (recipientMatcher.find()) 
        {
          String recipient = recipientMatcher.group(1);
          // Check if the extracted recipient matches the recipient give to the method
          if (recipient.contains(compareString)) 
          {
            // CHecks to if there are multiple recipients or only one
            if (recipient.contains(",")) 
            {
              // adds the negative value of the line number to the list so it flags it as multiple recipient mail
              lineNumbers.add(-lineNumber);
            } else 
            {
              // Add the the number of the line if the recipient is a single person
              lineNumbers.add(lineNumber);
            }
          }
        }
      }
    }

    return lineNumbers;
  }
  // Write method takes the pre-encrypted string given, to be saved into the txt file on the next file
  static boolean write(String authorString, String fileName) throws FileNotFoundException 
  {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) 
    {
        // Checks to see if the the file is empty or not
        if (countlines(fileName)>0)
        {
        // Jump a line if there is already data before hand so its one mail per line
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
  // readOnlyXLines method (not to be mistaken with readOnlyXline) takes as input a list with the lines wanted to be extracted and returns them in a list
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
            // Incase the number is nagative in the list because its flagged with multiple recipients
            if (lineNumbers.contains(lineCount)||lineNumbers.contains(lineCount*-1)) 
            {
              // decrypts the line of the integer given
                String key = Keygen.keygenerator(Long.parseLong(splicer.splicelast(line,13,3,"/")));
                String decryptedLine = AES.decrypt(splicer.splicefirst(line,16), key);
              // and adds it to the list to be returned
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
  // authenticator method compares "email | passward" with the encrypted version in the file and returns true if they match
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
  // readOnlyXLine method takes a specific line and reads it then returns it as a string not like Xlines tha returns a list of multiple strings
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