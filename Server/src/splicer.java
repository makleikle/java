public class splicer
{
    static String splicefirst(String inputString)
    {
        // Remove the last x characters from the string
        String outputString = inputString.substring(0, inputString.length() - 13);
        return outputString;
    }
    static String splicelast(String inputString)
    {
        String last13Chars = inputString.substring(inputString.length() - 13);
        return last13Chars.replaceAll("\\/","");
    }
}