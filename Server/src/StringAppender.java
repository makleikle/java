import java.io.FileNotFoundException;

public class StringAppender
{
    private void append() throws FileNotFoundException
    {
        String[] readerString = storageReaderWriter.read();
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < readerString.length; i++) 
        {
            sb.append(readerString[i]+"|");
        }
    }
}