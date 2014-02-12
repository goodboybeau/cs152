/**
 * 
 */
package a1;

import java.io.Console;
import java.util.Deque;
import java.util.Stack;
import java.util.LinkedList;
import java.util.Scanner;


/**
 * Class to parse input and determine syntactic correctness based on the grammar
 *  below by attempting to build a parse tree via recursive decent parsing.
 *  
 *	Expr -> Literal | Var | FCall | LetExpr | IfExpr
 *	Literal -> ListLiteral | SymbolLiteral
 *	ListLiteral -> [ {Literal} ]
 *	FCall -> FName ( {Expr} )
 *	FName -> UserFName | PrimFName
 *	LetExpr -> let {Def} Expr
 *	Def -> define Sig Expr
 *	Sig -> UserFName ( {Var} )
 *	IfExpr -> if Expr Expr Expr
 * 
 *
 * Solves CS152 HW Assignment #1
 *  
 * @author jaronhalt
 * 
 * @version 1.1 2014/2/9
 */
public class Parser {
	
	// holds the input text as a list of Tokens
	private LinkedList<Token> preterminals;
	// the current token that is being evaluated
	private Token currentToken;
	// the position in the list that corresponds to the 
	// next token in the list @preterminals
	private int position;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String example = "let define append ( X Y ) if X cons ( car ( X ) append ( cdr ( X ) Y ) ) Y"
				+ " append ( [ `a ] [ `b ] )";
		Parser parser = new Parser();
		System.out.println(parser.parse(example));
	}

	/**
	 * Default constructor of a recursive descent parser.
	 */
	public Parser()
	{
		this.preterminals = new LinkedList<Token>();
		this.position = 0;
		this.currentToken = null;
	}
		
	/**
	 * Builds a parse tree of the inputted string based on the 
	 *  grammar noted above. 
	 * @param input The string to parse and build a tree
	 * @return The OrderedTree<Token> instance that represents a
	 * 		properly constructed parse tree of the input.
	 * @throws IllegalArgumentException If the input violates
	 * 		the grammar above.
	 */
	public OrderedTree<Token> parse(String input) 
			throws IllegalArgumentException
	{
		this.preterminals = Token.tokenize(input);
		return this.expr();
	}
	
	/**
	 * Retrieves the next token on which to be parsed and
	 * 		incrememnts the current position.
	 * @return The token corresponding to the current token
	 * 		in the list of preterminals.
	 */
	private Token token()
	{
		// get the next token if there is one.
		if(this.position < this.preterminals.size())
		{
			this.currentToken = this.preterminals.get(this.position);
			this.position++;
			return this.currentToken;
		}
		// End of Input...
		else
		{
			return new Token();
		}
	}
	
	/**
	 * Retrieves the token ahead of the current token.
	 * @return The token after the currently parsed token.
	 */
	private Token lookAhead()
	{
		if(this.position < this.preterminals.size())
		{
			return this.preterminals.get(this.position);
		}
		else
		{
			return new Token();
		}
	}
	
	/**
	 * Evaluates the current token and recursively descends 
	 * 		the list of preterminals.
	 * @return The OrderedTree<Token> that represents the 
	 * 		current token and its children as a parse tree.
	 * @throws IllegalArgumentException If the grammar is found
	 * 		to be violated.
	 */
	private OrderedTree<Token> expr()
			throws IllegalArgumentException
	{				
		// separate actual decent from EOI
		switch(this.lookAhead().getType())
		{
			// Primitive function name
			case ("PrimFName"):
			{
				return new OrderedTree<Token>(new Token("FCall"), this.FCall());
			}
			// User function name
			case ("UserFName"):
			{
				return (new OrderedTree<Token>(new Token("FCall"), this.FCall()));
			}
			// "let" expression
			case ("let"):
			{
				return (new OrderedTree<Token>(new Token("LetExpr"), this.let()));
			}
			// "if" expression
			case ("if"):
			{
				return (new OrderedTree<Token>(new Token("IfExpr"), this.ifExpr()));
			}
			// "var" expression
			case ("Var"):
			{
				return (new OrderedTree<Token>(new Token("Var"), this.Var()));
			}
			// List of literals
			case ("["):
			{
				return (new OrderedTree<Token>(new Token("ListLiteral"), this.listLiteral()));
			}
			/**
			 *  If syntax is correct, could be a literal value or 
			 *  end-of-input. If incorrect, could be other values.
			 */
			default:
			{
				return (new OrderedTree<Token>(this.token()));
			}
		}
		// we're done, so be done.
	}
	
	/**
	 * Constrains the parsing descent to the grammar above by 
	 * 		allowing only a legal "if" expression to be next in the
	 * 		list of preterminals.
	 * @return The LinkedList of OrderedTree<Token>s that represents
	 * 		the 3 expressions following an "if" clause as well as
	 * 		the "if" clause itself.
	 * @throws IllegalArgumentException If the actual and expected
	 * 		types do not match.
	 */
	private LinkedList<OrderedTree<Token>> ifExpr() 
			throws IllegalArgumentException
	{
		this.match(this.lookAhead(), "if");
		
		LinkedList<OrderedTree<Token>> ifChildren = new LinkedList<OrderedTree<Token>>();
		
		// add "if"
		ifChildren.add(new OrderedTree<Token>(this.token()));

		// "if" statement is followed by 3 "expr"
		for(int i=0; i<3; i++)
		{
			// If next is "var", they are preterminals...
			if(this.lookAhead().getType().equals("Var"))
			{
				ifChildren.addAll(this.Var());
			}
			else
			{
				ifChildren.add(this.expr());
			}
		}
		return ifChildren;
	}
	
	/**
	 * Constrains the parsing descent to the grammar above by
	 * 		allowing only a legal "let" expression to be next in the
	 * 		list of preterminals.
	 * @return The LinkedList of OrderedTree<Token>s that represents
	 * 		a legal "let" expression as well as "let" itself.
	 * @throws IllegalArgumentException If the actual and expected
	 * 		types do not match.
	 */
	private LinkedList<OrderedTree<Token>> let()
			throws IllegalArgumentException
	{		
		// "define" always follows "let"
		this.match(this.lookAhead(), "let");
		
		LinkedList<OrderedTree<Token>> lChildren 
			= new LinkedList<OrderedTree<Token>>();
		// add "let"
		lChildren.add(new OrderedTree<Token>(this.token()));
		// "let" always followed by "define" "expr"
		lChildren.add(this.Def());
		lChildren.add(this.expr());
		
		return lChildren;
	}
	
	/**
	 * Constrains the parsing descent to the grammar above by
	 * 		allowing only a legal "define" expression to be next in
	 * 		the list of preterminals.
	 * @return The OrderedTree<Token> that represents the legal
	 * 		"define" expression.
	 * @throws IllegalArgumentException If the actual and expected
	 * 		types do not match.
	 */
	private OrderedTree<Token> Def()
			throws IllegalArgumentException
	{	
		this.match(this.lookAhead(), "define");
		
		LinkedList<OrderedTree<Token>> dChildren 
			= new LinkedList<OrderedTree<Token>>();		
		// add "define"
		dChildren.add(new OrderedTree<Token>(this.token()));
		dChildren.add(this.Sig());
		dChildren.add(this.expr());
		return new OrderedTree<Token>(new Token("Def"), dChildren);
	}
	
	/**
	 * Constrains the parsing descent to the grammar above by
	 * 		allowing only a legal "Sig" expression to be next in
	 * 		the list of preterminals.
	 * @return The OrderedTree<Token> that represents the legal
	 * 		"Sig" expression.
	 * @throws IllegalArgumentException If the actual and expected
	 * 		types do not match.
	 */
	private OrderedTree<Token> Sig()
			throws IllegalArgumentException
	{
		this.match(this.lookAhead(), "UserFName");
		
		LinkedList<OrderedTree<Token>> sigChildren = 
				new LinkedList<OrderedTree<Token>>();
		// add "UserFName"
		sigChildren.add(new OrderedTree<Token>(this.token()));
		
		// after FName comes "("
		this.match(this.lookAhead(),"(");
		{
			sigChildren.add(new OrderedTree<Token>(this.token()));
		}
		
		// if there are params, add them
		if(this.lookAhead().getType().equals("Var"))
		{
			sigChildren.addAll(this.Var());
		}
		
		// whether or not there were params,
		// ")" must be next...
		this.match(this.lookAhead(), ")");
		{
			sigChildren.add(new OrderedTree<Token>(this.token()));
		}
		
		return new OrderedTree<Token>(new Token("Sig"), sigChildren);	
	}
	
	/**
	 * Continues parsing the list of preterminals until a 
	 * 		non-"Var" expression is reached.
	 * @return The list of "Var"s that were side-by-side.
	 * @throws IllegalArgumentException If the actual and expected
	 * 		types do not match.
	 */
	private LinkedList<OrderedTree<Token>> Var()
			throws IllegalArgumentException
	{
		LinkedList<OrderedTree<Token>> vars = new LinkedList<OrderedTree<Token>>();
		
		// add all the "Var"s
		while(this.lookAhead().getType().equals("Var"))
		{
			vars.add(new OrderedTree<Token>(this.token()));
		}
		
		// just rid the heap of 'vars'
		if (vars.size() == 0)
		{
			return null;
		}
		
		return vars;
	}
	
	/**
	 * Constrains the parsing descent to the grammar above by
	 * 		allowing only a legal "ListLiteral" expression to be next in
	 * 		the list of preterminals.
	 * @return The list of literals that followed the token that 
	 * 		triggered this call.
	 * @throws IllegalArgumentException If the actual and expected
	 * 		types do not match.
	 */
	private LinkedList<OrderedTree<Token>> listLiteral()
			throws IllegalArgumentException
	{
		LinkedList<OrderedTree<Token>> literals = new LinkedList<OrderedTree<Token>>();
		
		// "[" is a child of "ListLiteral"
		this.match(this.lookAhead(), "[");
		{
			literals.add(new OrderedTree<Token>(this.token()));
		}		
		
		// There could be 0..N "SymbolLiterals"
		while(this.lookAhead().getType().equals("SymbolLiteral"))
		{
			literals.add(new OrderedTree<Token>(this.token()));
		}
		
		// "]" must follow regardless of the number of "SymbolLiteral"s
		this.match(this.lookAhead(), "]");
		{
			literals.add(new OrderedTree<Token>(this.token()));
		}

		return literals;
	}
	
	/**
	 * Constrains the parsing descent to the grammar above by
	 * 		allowing only a legal "FCall" expression to be next in
	 * 		the list of preterminals.
	 * @return The LinkedList of OrderedTree<Token> that represents 
	 * 		the legal "FCall" expression.
	 * @throws IllegalArgumentException If the actual and expected
	 * 		types do not match.
	 */
	private LinkedList<OrderedTree<Token>> FCall()
			throws IllegalArgumentException
	{
		LinkedList<OrderedTree<Token>> fChildren = 
				new LinkedList<OrderedTree<Token>>();
		
		// Syntactically, a PrimFName or "UserFName" 
		// 		could start the "FCall"
		this.match(this.lookAhead(), "PrimFName", "UserFName");
		{
			fChildren.add(new OrderedTree<Token>(this.token()));
		}
		
		// "(" always follows a function name with this grammar
		this.match(this.lookAhead(), "(");
		{
			fChildren.add(new OrderedTree<Token>(this.token()));
		}

		// until the function parameters are finished or an 
		// illegal Token type is found to be next in the list,
		// add them to a new linked list to build the return tree.
		String next = this.lookAhead().getType();
		while(!next.equals(")") && !next.equals(Token.END_OF_INPUT_TYPE) && !next.equals("]"))
		{
			// if next is "Var", just add them all
			// since they be preterminals
			if(next.equals("Var"))
			{
				fChildren.addAll(this.Var());
			}
			// otherwise, a legal "Expr" is allowed
			else
			{
				fChildren.add(this.expr());
			}
			// continue...
			next = this.lookAhead().getType();
		}
		
		// ")" ends the "FCall"
		this.match(this.lookAhead(), ")");
		{
			fChildren.add(new OrderedTree<Token>(this.token()));
		}
		
		// go back up...
		return fChildren;
	}

	/**
	 * Checks if the token type matches what's expected.
	 * @param actual The Token at question
	 * @param expected The expected type of "actual"
	 * @throws IllegalArgumentException If the actual and expected
	 * 		types do not match.
	 */
	private void match(Token actual, String expected)
			throws IllegalArgumentException
	{
		if(!actual.getType().equals(expected))
		{
			this.error(actual.getType(), expected);
		}
	}
	
	/**
	 * Checks if the token type matches what's expected.
	 * @param actual The Token at question
	 * @param expected1 An expected type of "actual"
	 * @param expected2 Another expected type of "actual"
	 * @throws IllegalArgumentException If actual type does not
	 * 		match either of the expected types.
	 */
	private void match(Token actual, String expected1, String expected2)
			throws IllegalArgumentException
	{
		if(!actual.getType().equals(expected1) && !actual.getType().equals(expected2))
		{
			this.error(actual.getType(), expected1+" or "+expected2);
		}
	}
	
	/**
	 * Checks if the token type matches what's expected.
	 * @param actual The token at question.
	 * @param expected The String of possible types that "actual"
	 * 		may be.
	 * @return True if the actual type matches any of the 
	 * 		expected types, False otherwise
	 */
	private boolean match(Token actual, String[] expected)
	{
		boolean result = false;
		for(String potential : expected)
		{
			result |= actual.getType().equals(potential);
		}
		return result;
	}
	
	/**
	 * Constructs a proper error message and throws exception
	 * 		with this error message
	 * @param spelling The spelling of the found type.
	 * @param expectedType The type that was expected to be found.
	 * @throws IllegalArgumentException Explains the issue with the
	 * 		type mismatch.
	 */
	private void error(String spelling, String expectedType)
			throws IllegalArgumentException
	{
		String message = spelling + " found, " + expectedType + " expected.";
		throw new IllegalArgumentException(message);
	}
	
}
