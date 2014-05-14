package a6;

import java.util.LinkedList;
import java.util.ListIterator;

/**

     This class provides a recursive descent parser              
     for the grammar with productions
       Expr -> Literal | Var | FCall | LetExpr | IfExpr
       Literal -> ListLiteral | SymbolLiteral
       ListLiteral -> [ {Literal} ]
       FCall -> FName ( {Expr} )
       FName -> UserFName | PrimFName
       LetExpr -> let {Def} Expr
       Def -> define Sig Expr
       Sig -> UserFName ( {Var} )
       IfExpr -> if Expr Expr Expr
       
    The start symbol is Expr.
    
    In these productions, the parentheses and brackets are terminal symbols; 
      the braces and vertical bars are metasymbols. 
      
    Also, the following are preterminals:
      Var:  a string that begins with a capital letter
      SymbolLiteral:  a string that begins with the backquote character `
      PrimFName:  one of the strings car, cdr, or cons
      UserFunctionName: a string that begins with a lower-case letter, 
        is not a PrimFName, and does not appear on the right-hand side of any rule. 
        
    The trees returned by the parse method are syntax trees rather than parse trees.
    The syntax trees are constrained as follows:
 
    For the categories Expr, Literal, and FName, the syntax tree is identical to that 
       of the appropriate right-hand side nonterminal.
    For the category ListLiteral, the syntax tree has a root token of the new type ListLiteral, 
       and children that correspond to each of the Literal constituents in the list.
    For the category FCall, the syntax tree has the FName token as its root, 
       and children that correspond to each of the Expr constituents.
    For the category LetExpr, the syntax tree has a root token of the new type LetExpr. 
       The children correspond to the Def constitutents, 
       except that the last child corresponds to the Expr constituent.
    For the category Def, the syntax tree has a root token of the new type Def. 
       The first of its two children corresponds to the Sig constitutents, 
       and its second child corresponds to the Expr constituent.
    For the category IfExpr, the syntax tree has a root token of the new type LetExpr. 
       The children correspond to the Def constitutents, 
       except that the last child corresponds to the Expr constituent.
    For the preterminal categories SymbolLiteral, Var, and UserFName, 
       the syntax trees have no children. 
       The roots are tokens of the type that corresponds to the category.    
      
    Note that recursive descent parsing 
    is appropriate for this grammar.
    
     @author Jeff Smith
     @version for Assignment 2, CS 152, SJSU, Spring 2014 
 */

public class Parser
{
    // the next token to be processed
    private Token lookahead = null;

    // the list of tokens in the program
    private LinkedList<Token> tokens = null;
    
    // an iterator over the list of tokens
    private ListIterator<Token> iterator = null;
    
    

    /**
     * Constructor for objects of class Parser
     */
    
    public Parser()
    {
       
    }

     /**
        Parses a string in the given grammar, using
          recursive descent
        @param the input string
        @return a parse tree for the string
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
    
  public OrderedTree<Token> parse(String input)               {
     tokens = Token.tokenize(input);
     iterator = tokens.listIterator();
     getNextToken();       // initialize lookahead token
     OrderedTree<Token> output = parseExpr();
      if (!lookahead.getType().equals("end-of-input"))
         throw new IllegalArgumentException(
            "extra tokens beginning with " + 
            lookahead.getSpelling() +
            " after parsed input string");
      else
        return output;                                        }

        
     /**
        Finds a parse tree for the category Expr
          for a string that starts with the first 
          unconsumed symbol in the input string.
        Updates the iterator over this string 
          and the lookahead symbol as appropriate
        Unchanged from Assignment 1
        @return a syntax tree for the substring
          of category Expr that starts at the
          first unconsumed symbol
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
    
    private OrderedTree<Token> parseExpr()                               {
      if (lookahead.getType().equals("[") ||
          lookahead.getType().equals("SymbolLiteral"))         
        return parseLiteral();
      else if (lookahead.getType().equals("Var"))      
        return parseVar();                                      
      else if (lookahead.getType().equals("UserFName") ||
               lookahead.getType().equals("PrimFName"))       
        return parseFCall();                            
      else if (lookahead.getType().equals("let"))      
        return parseLetExpr();                                      
      else if (lookahead.getType().equals("if"))      
        return parseIfExpr();                                      
      else throw new IllegalArgumentException(
        errorString("Expr"));                                             }

        
     /**
        Finds a parse tree for the category Literal
          for a string that starts with the first 
          unconsumed symbol in the input string.
        Updates the iterator over this string 
          and the lookahead symbol as appropriate.
        Unchanged from Assignment 1
        @return a parse tree for the substring
          of category Literal that starts at the
          first unconsumed symbol
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
    
    private OrderedTree<Token> parseLiteral()                  {
      if (lookahead.getType().equals("["))
        return parseListLiteral();
      else if (lookahead.getType().equals("SymbolLiteral"))
        return parseSymbolLiteral();
      else                                                     
        throw new IllegalArgumentException(
          errorString("Literal"));                             }
        
    
     /**
        Finds a parse tree for the category ListLiteral
          for a string that starts with the first 
          unconsumed symbol in the input string.
        Updates the iterator over this string 
          and the lookahead symbol as appropriate
        @return a syntax tree for the substring
          of category ListLiteral that starts at the
          first unconsumed symbol
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
       
    private OrderedTree<Token> parseListLiteral()                        {
      LinkedList<OrderedTree<Token>> children = new LinkedList<>();
      match("[");
      while (lookahead.getType().equals("[") ||
          lookahead.getType().equals("SymbolLiteral"))    
         children.add(parseLiteral());                                     
      match("]");              
      return new OrderedTree<Token>(new Token("List"),children);   }
      
      
     /**
        Finds a parse tree for the category FCall
          for a string that starts with the first 
          unconsumed symbol in the input string.
        Updates the iterator over this string 
          and the lookahead symbol as appropriate
        @return a parse tree for the substring
          of category FCall that starts at the
          first unconsumed symbol
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
       
    private OrderedTree<Token> parseFCall()                        {
      LinkedList<OrderedTree<Token>> children = new LinkedList<>();
      Token fname = parseFName().getRootData();
      match("(");
      while (lookahead.getType().equals("[") ||
          lookahead.getType().equals("SymbolLiteral") ||      
          lookahead.getType().equals("Var") ||
          lookahead.getType().equals("UserFName") ||
          lookahead.getType().equals("PrimFName") ||
          lookahead.getType().equals("let") ||
          lookahead.getType().equals("if"))    
        children.add(parseExpr());                                     
      match(")");              
      return new OrderedTree<Token>(fname,children);              }
      
      
     /**
        Finds a parse tree for the category FName
          for a string that starts with the first 
          unconsumed symbol in the input string.
        Updates the iterator over this string 
          and the lookahead symbol as appropriate
        Unchanged from Assignment 1
        @return a parse tree for the substring
          of category FName that starts at the
          first unconsumed symbol
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
       
    private OrderedTree<Token> parseFName()                        {
      if (lookahead.getType().equals("UserFName"))
        return parseUserFName();
      else if (lookahead.getType().equals("PrimFName"))
        return parsePrimFName();
      else                                                     
        throw new IllegalArgumentException(
          errorString("FName"));                                   }
      
      
     /**
        Finds a parse tree for the category LetExpr
          for a string that starts with the first 
          unconsumed symbol in the input string.
        Updates the iterator over this string 
          and the lookahead symbol as appropriate
        @return a parse tree for the substring
          of category Program that starts at the
          first unconsumed symbol
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
    
    private OrderedTree<Token> parseLetExpr()                 {
      LinkedList<OrderedTree<Token>> children = 
        new LinkedList<OrderedTree<Token>>();
      match("let");
      while (lookahead.getType().equals("define"))         
        children.add(parseDef());                                
      children.add(parseExpr());
      return new OrderedTree<Token>(
        new Token("LetExpr"), children);                      }

       
     /**
        Finds a parse tree for the category Def
          for a string that starts with the first 
          unconsumed symbol in the input string.
        Updates the iterator over this string 
          and the lookahead symbol as appropriate
        @return a parse tree for the substring
          of category Def that starts at the
          first unconsumed symbol
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
    
    private OrderedTree<Token> parseDef()                         {
      LinkedList<OrderedTree<Token>> children = 
        new LinkedList<OrderedTree<Token>>();
      match("define");
      children.add(parseSig());
      children.add(parseExpr());
      return new OrderedTree<Token>(
        new Token("Def"), children);                              }
      
        
     /**
        Finds a parse tree for the category Sig
          for a string that starts with the first 
          unconsumed symbol in the input string.
        Updates the iterator over this string 
          and the lookahead symbol as appropriate
        @return a parse tree for the substring
          of category Sig that starts at the
          first unconsumed symbol
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
    
    private OrderedTree<Token> parseSig()                                 {
      LinkedList<OrderedTree<Token>> children = new LinkedList<>();
      Token fname = match("UserFName");
      match("(");
      while (lookahead.getType().equals("Var"))               
        children.add(new OrderedTree<Token>(match("Var")));           
      match(")");
     return new OrderedTree<Token>(fname, children);                      }
        
     
     /**
        Finds a parse tree for the category IfExpr
          for a string that starts with the first 
          unconsumed symbol in the input string.
        Updates the iterator over this string 
          and the lookahead symbol as appropriate
        @return a parse tree for the substring
          of category IfExpr that starts at the
          first unconsumed symbol
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
    
    private OrderedTree<Token> parseIfExpr()                    {
      LinkedList<OrderedTree<Token>> children = 
        new LinkedList<OrderedTree<Token>>();
      match("if");
      children.add(parseExpr());
      children.add(parseExpr());
      children.add(parseExpr());
      return new OrderedTree<Token>(
        new Token("IfExpr"), children);                         }
      
        
    
     /**
        Finds a parse tree for the category SymbolLiteral
          for a string that starts with the first 
          unconsumed symbol in the input string.
        Updates the iterator over this string 
          and the lookahead symbol as appropriate
        @return a parse tree for the substring
          of category SymbolLiteral that starts at the
          first unconsumed symbol
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
    
    private OrderedTree<Token> parseSymbolLiteral()                  {
      return new OrderedTree<Token> (match("SymbolLiteral"));        }     
      
      
     /**
        Finds a parse tree for the category Var
          for a string that starts with the first 
          unconsumed symbol in the input string.
        Updates the iterator over this string 
          and the lookahead symbol as appropriate
        @return a parse tree for the substring
          of category Var that starts at the
          first unconsumed symbol
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
    
    private OrderedTree<Token> parseVar()                            {
      return new OrderedTree<Token> (match("Var"));                  }       
      
      
     /**
        Finds a parse tree for the category UserFName
          for a string that starts with the first 
          unconsumed symbol in the input string.
        Updates the iterator over this string 
          and the lookahead symbol as appropriate
        @return a parse tree for the substring
          of category UserFName that starts at the
          first unconsumed symbol
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
    
    private OrderedTree<Token> parseUserFName()                      {
      return new OrderedTree<Token> (match("UserFName"));            }       
      
      
     /**
        Finds a parse tree for the category PrimFName
          for a string that starts with the first 
          unconsumed symbol in the input string.
        Updates the iterator over this string 
          and the lookahead symbol as appropriate
        @return a parse tree for the substring
          of category PrimFName that starts at the
          first unconsumed symbol
        @throws IllegalArgumentException if a syntactic 
          error is found
     */
    
    private OrderedTree<Token> parsePrimFName()                      {
      return new OrderedTree<Token> (match("PrimFName"));            }                                               
        
      
  /**
       Checks whether the lookahead symbol is an instance
         of a given token type.  Replaces it by the next
         input token if so.
       @param expectedType the token type
       @throws IllegalArgumentException (with an informative
         message) if the lookahead is not an instance of this type
   */

  private Token match(String expectedType)                       {
    if (!lookahead.getType().equals(expectedType))
      throw new IllegalArgumentException(
        errorString(expectedType));
    Token output = lookahead;
    getNextToken();                          
    return output;                                               }
    
    
  /**
       Advances to the next token in the input string
         and updates the lookahead token
  */  
 
  private void getNextToken()                                  {
    if (iterator.hasNext())
      lookahead = iterator.next();  
    else
      lookahead = new Token();                                 }
      
      
  /**
       Constructs an error message in the case of
         an input token (that is, the lookahead symbol) 
         of an unexpected type
       @param expectedType the type expected for the
          lookahead symbol
       @return the error message   
   */    
  
    private String errorString(String expectedType)              {
      return lookahead.getSpelling() + " found, " + 
                expectedType + " expected";                      }
 
}