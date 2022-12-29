public class splicer
{
    static String splicefirst(String inputString)
    {
        // Remove the last 16 characters from the string
        String outputString = inputString.substring(0, inputString.length() - 16);
        return outputString;
    }
    static String splicelast(String inputString)
    {
        // Remove the last 3 characters from the string and keep the last 13 after
        String last13Chars_minus3last = inputString.substring(0, inputString.length() - 3);
        last13Chars_minus3last = last13Chars_minus3last.substring(last13Chars_minus3last.length() - 13);
        return last13Chars_minus3last.replaceAll("/","");
    }
}