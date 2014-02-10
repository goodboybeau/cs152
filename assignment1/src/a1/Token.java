package a1;

import java.util.LinkedList;
import java.util.Arrays;
import java.util.Scanner;


/**
    Instances of this class represent tokens appropriate
      for use in a recursive descent parser as implemented
      by the Parser class of this package (using the grammar
      recognized by that class).

   @author Jeff Smith
   @version for CS 152, Spring 2014, SJSU
 */

public class Token
{

    // tokens have a type name (e.g., "StringLiteral")
    //   and a spelling (e.g., "xyz")
    private String type;
    private String spelling;
    
    /**
         Type name for the dummy end-of-input token
     */
    public static final String END_OF_INPUT_TYPE = "end-of-input";

    /**
         The set of primitive function names
     */ 
    public static final String[] primitives =
      {"car","cdr","cons",};


    /**
        The set of remaining singleton types that aren't primitive
          function names.  These types correspond to terminals
     */ 
    public static final String[] singletons =
      {"(",")","[","]","define","if","let","find","with"};

      
    /**
         Constructs a dummy token to mark the end of input
     */  
    
    public Token()                  {
      this(END_OF_INPUT_TYPE);      }

      
    /**
         Constructs a token whose type equals its spelling
         @param v the type and spelling
     */

    public Token(String v)
    {
        type = v;
        spelling = v;
    }


    /**
         Constructs a token given a type and a spelling
         @param t the type
         @param v the spelling
      */

    public Token(String t, String v)
    {
        type = t;
        spelling = v;
    }


    /**
        Finds the type of the token
        @return the type of the token
     */

    public String getType()      {
        return type;             }


    /**
        Finds the value of the token
        @return the value of the token
     */

    public String getSpelling()   {
        return spelling;          }


    /**
        Finds a string representing the token
        @return the value of the token
     */

    public String toString()                   {
      return type + ":" + spelling;            }


    /**
        Determines whether a string is the spelling of a
          legal identifier
        @return <code>true</code> iff the string is 
          the spelling of a legal identifier
     */

    public static boolean isVariableName(String s)      {
      if (s == null || s.equals(""))
        return false;
      char firstChar = s.charAt(0);
      return Character.isUpperCase(firstChar);        }


    /**
        Determines whether a string is the spelling of a
          user function name
        @return <code>true</code> iff the string is 
          the spelling of a user function name
     */

    public static boolean isUserFunctionName(String s)  {
      if (s == null || s.equals(""))
        return false;
      char firstChar = s.charAt(0);
      return Character.isLowerCase(firstChar);          }


    /**
        Determines whether a string is the spelling of a
          legal symbol literal
        @param the string to test
        @return <code>true</code> iff the string is the
          spelling of a legal symbol literal
     */

    public static boolean isSymbolLiteral(String s)   {
      if (s == null || s.equals(""))
        return false;
      return s.charAt(0) == '`';                      }


    /**
        Finds the type (name) of a token with a given spelling
        @param the given spelling
        @return the appropriate type name
        @throws IllegalArgumentException if no token exists with
           the given spelling
     */

    static String getTokenType(String spelling)         {
      if (Arrays.binarySearch(singletons, spelling) >= 0)
        return spelling;
      if (Arrays.binarySearch(primitives, spelling) >= 0)
        return "PrimFName";
      if (isVariableName(spelling))
        return "Var";
      if (isUserFunctionName(spelling))
        return "UserFName";
      if (isSymbolLiteral(spelling))
        return "SymbolLiteral";
      else
        throw new IllegalArgumentException("illegal token: " + spelling);   }


    /**
        Gets the spelling to be used for a given token.
        The only effect of this method is to strip the leading backquote
          character from symbol literals
        @param the token spelling as it appears in the program
        @return the appropriate spelling for the corresponding token object
     */

    private static String getSpelling(String s)                            {
      if (s.charAt(0) == '`')
        return s.substring(1);
      else
        return s;                                                          }
        
        
    /**
         Converts a text representing a program into a list of tokens
         @return the list of tokens, with a dummy token at the end
           whose spelling and token type are both "end-of-input".
         @throws IllegalArgumentException if the input is null
           or if some substring delimited by white space
           fails to be the spelling of a legal token
     */

    public static LinkedList<Token> tokenize(String program)          {
      if (program == null)
        throw new IllegalArgumentException(
          "input string cannot be null");
      Scanner sc = new Scanner(program);
      LinkedList<Token> output = new LinkedList<Token>();
      while (sc.hasNext())                              {
        String s = sc.next();
        output.add(
          new Token(getTokenType(s), getSpelling(s)));  }
      return output;                                                  }


}
