import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Keygen {
    public static Long timetoseed() 
    {
        //this method takes LocalDateTime (bad option for diffrent timezones) turns it in as a seed ddMMyyyyHH 
        //(also if there is delay between and the hr just changes they wont be synchronized anymore and the key will be worng a way to fix it is to go on the exeption 
        //for wrong key input in AES and regenerate the key with 
        //-1 Hr (day in case id 23.59 and it turns 00.00) or whatever needed to match the old key)
        Long SEED;
        LocalDateTime dateNow = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyHH");
        String dateNowFormated = dateNow.format(formatter);
        Long LongdateNowFormated = Long.parseLong(dateNowFormated);
        SEED = LongdateNowFormated;
        return SEED;
    }
    public static String keygenerator(Long seed)
    {
  
        Random random = new Random(seed) ;
        int num = random.nextInt(); //takes next integer that random generates
        String key = Integer.toString(num);
        return key;
    }
}