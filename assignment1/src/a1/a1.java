package a1;

/**
    Tests a scanner for the grammar with start symbol Program
    and productions
<pre>
       Program -> Expr | Local {Local} find Expr
       Local -> with Binding
       Binding -> VariableName Expr | UserFunctionName Definition
       Definition -> begin {VariableName} body Program end
       Application -> FunctionName ( {Expr} )
       FunctionName -> UserFunctionName | + | -
       Expr -> IntegerLiteral | VariableName | Application
</pre>

    In these productions, the braces and the vertical bar
      are metasymbols, while the parentheses are terminal
      symbols of the grammar.
    Also, the following are preterminals:
      Var:  a string that begins with a capital letter
      SymbolLiteral:  a string that begins with the backquote character `
      PrimFName:  one of the strings car, cdr, or cons
      UserFunctionName: a string that begins with a lower-case letter, 
        is not a PrimFName, and does not appear on the right-hand side of any rule. 

     @author Jeff Smith
     @version for Assignment 1, CS 152, SJSU, Spring 2014
  */


public class a1
{

   private static Parser parser = new Parser();

  /**
      Takes an input string purporting to represent a program in
      L(G), where G is the context-free grammar defined above, and
      prints the sequence of tokens of the program, terminated by
      a dummy token.
      If an illegal token is present, it prints an error message
      to that effect.
      @param expr the string, represented as a string of tokens
        separated by whitespace characters as recognized by the
        <code>Character.isWhitespace</code> predicate.
   */

  private static void test(String program)                                   {
    try                 
    	{
	    	parser = new Parser();
	    	OrderedTree<Token>  tree = parser.parse(program);
	    	System.out.println(tree);  
    	}
	catch(IllegalArgumentException e)                                      {
	  System.out.println(e.getMessage());                                  } }


  /**
      The main test program
      @param args is ignored
   */

  public static void main(String[] args)                                      {
    String newline = System.getProperty("line.separator");

    // syntactically and semantically ok
    
    test("`x"); 
    test("[ `y `z `w ]");
    test("if [ ] `b `c");
    test("if [ `a0 `b0 ] `a0 [ ]");
    test("if `a1 `b1 `c1");
    test("if `a2 if `b2 `c2 `d2 `e2");
    test("if if `a3 `b3 `c3 `d3 `e3");
    test("cons ( `a4 [ `b4 ] )");
    test("cons ( `a5 cons ( `b5 [ `c5 ] ) )");
    test("car ( cons ( `a6 [ `b6 ] ) )");
    test("cdr ( cons ( `a7 [ `b7 ] ) )");
    test("let `x5");
    test("let define x6 ( ) [ `b6 ] " + 
         "x6 ( ) ");
    test("let define x7 ( ) [ `b7 ] " + 
         "cons ( `a7 x7 ( ) ) ");
    test("let define x8 ( ) cons ( `b8 [ `b9 ] ) " + 
         "x8 ( ) ");
    test("let define xx1 ( ) `x1  " + 
         "    define xx2 ( ) `x2  " +  newline + 
         "    define xx3 ( ) [ `x3 ] " + 
         "cons ( xx1 ( ) cons ( xx2 ( ) xx3 ( ) ) ) ");
    test("let define xy1 ( ) `xy1  " + 
         "    define xy1 ( ) `xy2  " + 
         "    define xy3 ( ) [ `xy3 ] " + 
         "cons ( xy1 ( ) xy3 ( ) ) ");
    test("let define xy4 ( ) `xy4  " + 
         "    define xy5 ( ) cons ( `xy4 [ ] ) " + 
         "cons ( xy4 ( ) xy5 ( ) ) ");
    test("let let define xy6 ( ) [ `xy6 ] xy6 ( )");
    System.out.println();    
    
    // syntactically and semantically ok
    //   with 1 or more user-defined functions
    
    test("let define g0 ( X ) X  " + 
         "g0 ( `g ) ");    
    test("let define f0 ( X Y ) cons ( X Y ) " + 
         "`f ");
    test("let define f1 ( X Y ) cons ( X Y ) " + 
         "f1 ( `af1 [ `bf1 ] )");
    test("let define f2 ( X Y ) cons ( X Y ) " + 
         "f2 ( `af2 [ `bf2 ] )");
    test("let define g1 ( X ) cons ( `g1 X ) " + 
         "    define g2 ( Y ) cons ( `g2 Y ) " + 
         "g1 ( g2 ( [ ] ) )");
    test("let define f3 ( X Y ) if X cons ( car ( X ) f3 ( cdr ( X ) Y ) ) Y " + 
         "`f ");
    test("let define append ( X Y ) if X cons ( car ( X ) append ( cdr ( X ) Y ) ) Y " + 
         "append ( [ `a ] [ `b ] ) ");
    test("let define append ( X Y ) if X cons ( car ( X ) append ( cdr ( X ) Y ) ) Y " + 
         "    define g3 ( G ) append ( cons ( `g3 G ) cons ( `g03 G ) ) " +
         "g3 ( [ `g30 ] ) ");
    test("let define append ( X Y ) if X cons ( car ( X ) append ( cdr ( X ) Y ) ) Y " + 
         "    define g30 ( G ) append ( cdr ( G ) cdr ( G ) ) " +
         "g30 ( [ `f03 `f30 ] ) ");
    test("let define reverse ( X32 ) if X32 append ( reverse ( cdr ( X32 ) ) cons ( car ( X32 ) [ ] ) ) X32" +
         "    define append ( X31 Y31 ) if X31 cons ( car ( X31 ) append ( cdr ( X31 ) Y31 ) ) Y31 " + 
         " reverse ( [ `g31 `g32 `g33 `g34 ] )");
    test("let define reverse ( X42 ) if X42 append ( reverse ( cdr ( X42 ) ) cons ( car ( X42 ) [ ] ) ) X42 " +
         "  let define append ( X41 Y41 )  " + 
         "  if X41 cons ( car ( X41 ) append ( cdr ( X41 ) Y41 ) ) Y41 " +
         "reverse ( [ `g41 `g42 `g43 `g44 ] )");
    test("let define f5 ( X Y ) if X cons ( car ( X ) f5 ( cdr ( X ) Y ) ) Y " + 
         "    define g5 ( X ) cons ( car ( X ) [ ] ) " + 
         "f5 ( g5 ( [ `a5 `a5 ] ) g5 ( [ `b5 `b5 ] ) ) ");
    test("let define f6 ( X Y ) if X cons ( car ( X ) f6 ( cdr ( X ) Y ) ) Y " + 
         "    define g6 ( X ) cons ( car ( X ) [ ] ) " + 
         "g6 ( f6 ( [ `a6 `a6 ] [ `b6 `b6 ] ) ) ");
    test("let define g7 ( ) [ `b ] " + 
         "    define f7 ( X Y ) if X cons ( car ( X ) f7 ( cdr ( X ) Y ) ) Y " + 
         "f7 ( g7 ( ) g7 ( ) )");
    test("let define weave ( X8 Y8 ) if X8 cons ( car ( X8 ) weave ( Y8 cdr ( X8 ) ) ) Y8 " +
         "weave ( [ `f81 `f82 `f83 ] [ `g81 `g82  `g83 `g84 ] )");
    test("let define p ( X ) cons ( `p  X )" + 
         "    define q ( Z ) " +
         "      let define p ( Y ) cons ( car ( Z ) Y ) " +
         "      cons ( `q p ( Z ) ) " + 
         "p ( q ( [ `pp ] ) )");
    test("let define q1 ( Z ) " +
         "      let define p1 ( Y ) cons ( Y Z ) " +
         "      p1 ( car ( Z ) ) " + 
         "q1 ( [ `p1 `p2 ] )");
    test("let define addlast ( P Q ) " +
         "  let define q2 ( Q2 ) if Q2 cons ( car ( Q2 ) q2 ( cdr ( Q2 ) ) ) cons ( P [ ] ) " +
         "  q2 ( Q ) " + 
         "addlast ( `p2 [ `q2 `p2 `q2 ] )");
    test("let define h9 ( X9 ) cons ( `h9 X9 ) " +
         "      define k9 ( Y9 ) cons ( `k9 Y9 ) " +
         "h9 ( k9 ( [ `hk9 ] ) )");
    test("let define k8 ( Y8 ) " + 
         "  let define h8 ( X8 ) if X8 cons ( `h8 k8 ( cdr ( X8 ) ) ) [ ] " +
         "  if Y8 cons ( `k8 h8 ( cdr ( Y8 ) ) ) [ ] " +
         "k8 ( [ `k81 `k81 `k81 `k81 `k81 ] )");
  System.out.println();     
         
    // syntactically ok, semantically bad     
    
    test("Q");
    test("null ( [ ] )");
    test("car ( ) ");
    test("cdr ( )");
    test("car ( `v1 )");
    test("cdr ( `v2 )");
    test("car ( [ ] )");
    test("cdr ( [ ] )");
    test("car ( [ `v3 ] [ `v3 ] )");
    test("cdr ( [ `v4 ] [ `v4 ] )");
    test("cons ( )");
    test("cons ( `v5 )");
    test("cons ( [ ] )");
    test("cons ( `v6 `v6 )");
    test("cons ( [ `v7 ] [ `v7 ] )");
    test("cons ( [ `v8 ] `v8 ) ");
    test("cons ( `v9 [ `v9 ] [ `v9 ] )");
    test("h0 ( `w0 )");
    test("let define x6 ( X6 ) y6 ( ) " +
         "x6 ( `x6 ) ");
    test("let define x7 ( X7 ) cons ( Y7 X7 ) " +
         "x7 ( [ ] ) ");
    test("let define x8 ( ) [ `b8 ] " + 
         "cons ( `a8 x8 ( `b8 ) ) ");
    test("let define x9 ( X9 ) [ `b9 ] " + 
         "cons ( `a9 v0 ( ) ) ");
    test("let define h1 ( X Y ) if X cons ( car ( X ) h1 ( cdr ( X ) Y ) ) Y " + 
         "h1 ( [ `h11 ] [ `h12 ] [ `h13 ] ) ");
    test("let define h2 ( X Y ) if Z cons ( car ( X ) h2 ( cdr ( X ) Y ) ) Y " + 
         "h2 ( [ `h21 ] [ `h22 ]  ) ");
    test("let define h3 ( X Y ) if X cons ( car ( X ) h1 ( cdr ( X ) Y ) ) Y " + 
         "h3 ( [ `h31 ] [ `h32 ]  ) ");
    test("let define h5 ( X5 ) cons ( `h5 X5 ) " +  
         "    define k5 ( Y5 ) cons ( `k5 X5 ) " + 
         "k5 ( h5 ( [ `hk5 ] ) )");
    test("let define h6 ( X6 ) " +
         "  let define k6 ( Y6 ) cons ( `k6 Y6 ) " +
         "  cons ( `h6 X6 ) " +  
         "h6 ( k6 ( [ `hk6 ] ) )");
    test("let define k7 ( ) " + 
         "  let define h7 ( X7 ) cons ( k7 ( ) X7 ) " +
         "  `k7 " +
         "h7 ( [ `h71 ] )");
    System.out.println();   
       
    // syntactically bad
    
    test(null);
    test("");
    test("3");
    test("d");
    test("A B");
    test("d0 d00");
    test("( car [ `d000 ] )");
    test("[ )");
    test("[ d1 ]");
    test("[ `d2 ");
    test("1776");
    test("car ( ");
    test("car [ [ `d3 ] ] ");
    test("car ( [ `d4 ] ]");
    test("car ( [ `d5 ] ) )");
    test("`u0 ( )");
    test("if");
    test("if `u1");
    test("if `u2 `u3");
    test("if `u4 `u5 `u6 `u7");
    test("if ) `u8 `u9");
    test("let");
    test("let )");
    test("let if");
    test("let define");
    test("let define V0 ( W0 ) cons ( W0 [ ] ) V0 ( `w8 )");
    test("let define define w0 ( ) [ `w0 ] w0 ( ) ");
    test("let define car ( ) `v0");
    test("let define w1 W `v1");
    test("let define w2 ( w ) `v2");
    test("let define w3 ( W3 ] W3");
    test("let define w4 ( `v ) `v4");
    test("let define w5 [ W5 ] W5");
    test("let define w6 ( W0 W6 `v6");
    test("let define w7 ( W7 )");
    test("let define w8 ( W8 ) cons ( W8 [ ] ) ( `w8 )");
    test("let define w9 ( ) [ `w9 ] w9 ( ) w9 ( )");
    test("let define v9 ( ) [ `v9 ] v9 v9 ( )");
    test("let define v8 ( ) [ `v8 ] v8 ( v8 )");
    test("let define v7 ( ) [ `v7 ] ( ) ");
    System.out.println();     

    // reused variable names

    test("let define p2 ( ) `p2 " +
         "    define q2 ( ) [ `q2 ] " +
         "    define p2 ( ) `r2 " +
         "cons ( p2 ( ) q2 ( ) )");
    test("let define p3 ( ) `p3 " +
         "    define q3 ( ) [ `q3 ] " +
         "    define p3 ( X ) car ( X )  " +
         "cons ( p3 ( ) q3 ( ) )");
    test("let define p4 ( ) `p4 " +
         "    define q4 ( ) [ `q4 ] " +
         "    define p4 ( X ) car ( X ) " +
         "p4 ( q4 ( ) )");
    test("let define p5 ( X Y X ) cons ( X Y ) " +
         "p5 ( `p5 [ `q5 ] `q5 )");
    System.out.println();     

    // scoping tests
    test("let define k4 ( ) `k4 " + 
         "    define h4 ( X4 ) cons ( k4 ( ) X4 ) " +
         "h4 ( [ `h41 ] )");
    test("let define h5 ( H5 ) cons ( `h5 H5 ) " +
         "    define k5 ( K5 ) cons ( `k5 h5 ( K5 ) )" +
         "    define h5 ( H51 ) cons ( `h51 H51 ) " +
         "k5 ( [ ] ) ");
    test("let define append ( X31 Y31 ) if X31 cons ( car ( X31 ) append ( cdr ( X31 ) Y31 ) ) Y31 " + 
         "    define reverse ( X32 ) if X32 append ( reverse ( cdr ( X32 ) ) cons ( car ( X32 ) [ ] ) ) X32" +
         " reverse ( [ `g31 `g32 `g33 `g34 ] )");
    test("let define append ( U V ) if U cons ( car ( U ) append ( cdr ( U ) V ) ) V " + 
         "let define f ( X Y ) " +
         "  let define g ( Y V ) h ( `25 `26 ) " +
         "  let define h ( X W ) cons ( X cons ( W cons ( Y [ ] ) ) ) " +
         "  append ( g ( `15 `16 ) h ( `0 `1 ) ) " +
         "f ( `5 `6 ) ");
    test("let define k ( XX ) `k " +
         "let define h ( VV ) k ( VV ) " +
         "let define g ( WW ) " +
         "  let define k ( X ) cons ( X cons ( WW [ ] ) ) " +
         "  h ( WW ) " +
         "cons ( k ( `1 ) g ( `2 ) ) ");


}

}
