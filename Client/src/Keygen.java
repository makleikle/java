import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Keygen {
    public static Long timetoseed()
    {
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
        int num = random.nextInt();
        String key = Integer.toString(num);
        return key;
    }
}
