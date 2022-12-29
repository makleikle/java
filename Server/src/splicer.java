public class splicer
{
    static String splicefirst(String inputString, int removeXLastChars)
    {
        // Remove the last 16 characters from the string
        String outputString = inputString.substring(0, inputString.length() - removeXLastChars);
        return outputString;
    }
    static String splicelast(String inputString, int returnLastXChars, int removeYLastChars, String regex)
    {
        // Remove the last 3 characters from the string and keep the last 13 after
        String lastXCharsRemoveYLastChars = inputString.substring(0, inputString.length() - removeYLastChars);
        lastXCharsRemoveYLastChars = lastXCharsRemoveYLastChars.substring(lastXCharsRemoveYLastChars.length() - returnLastXChars);
        return lastXCharsRemoveYLastChars.replaceAll(regex ,"");
    }
}