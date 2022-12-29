public class splicer
{
    static String splicefirst(String inputString)
    {
        // Remove the last x characters from the string
        String outputString = inputString.substring(0, inputString.length() - 16);
        return outputString;
    }
    static String splicelast(String inputString)
    {
        String last13Chars_minus3last = inputString.substring(0, inputString.length() - 3);
        last13Chars_minus3last = last13Chars_minus3last.substring(inputString.length() - 13);
        return last13Chars_minus3last.replaceAll("/","");
    }
}