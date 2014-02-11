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
 * @author jaronhalt
 *
 */
public class Parser {
	
	LinkedList<Token> preterminals;
	Token currentToken;
	int position;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String example = "let define append ( X Y ) if X cons ( car ( X ) append ( cdr ( X ) Y ) ) Y"
				+ " append ( [ `a ] [ `b ] )";
		Parser parser = new Parser();
		System.out.println(parser.parse(example));
	}

	public Parser()
	{
		this.preterminals = new LinkedList<Token>();
		this.position = 0;
		this.currentToken = null;
	}
		
	public OrderedTree<Token> parse(String input)
	{
		this.preterminals = Token.tokenize(input);
		return this.expr();
	}
	
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
	
	private OrderedTree<Token> expr()
	{				
		// separate actual decent from EOI
		switch(this.lookAhead().getType())
		{
			case ("PrimFName"):
			{
				return new OrderedTree<Token>(new Token("FCall"), this.FCall());
			}
			case ("UserFName"):
			{
				return (new OrderedTree<Token>(new Token("FCall"), this.FCall()));
			}
			case ("let"):
			{
				return (new OrderedTree<Token>(new Token("LetExpr"), this.let()));
			}
			case ("if"):
			{
				return (new OrderedTree<Token>(new Token("IfExpr"), this.ifExpr()));
			}
			case ("Var"):
			{
				return (new OrderedTree<Token>(new Token("Var"), this.Var()));
			}
			case ("SymbolLiteral"):
			{
				return (new OrderedTree<Token>(this.token()));
			}
			case ("["):
			{
				return (new OrderedTree<Token>(new Token("ListLiteral"), this.literal()));
			}
			// default Token() is EOI
			default:
			{
				return (new OrderedTree<Token>(new Token()));
			}
		}
		// we're done, so be done.
	}
	
	private LinkedList<OrderedTree<Token>> ifExpr()
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
	
	private LinkedList<OrderedTree<Token>> let()
	{		
		// "define" always follows "let"
		this.match(this.lookAhead(), "let");
		
		LinkedList<OrderedTree<Token>> lChildren = new LinkedList<OrderedTree<Token>>();

		// add "let"
		lChildren.add(new OrderedTree<Token>(this.token()));
		// "let" always followed by "define" "expr"
		lChildren.add(this.Def());
		lChildren.add(this.expr());
		
		return lChildren;
	}
	
	private OrderedTree<Token> Def()
	{	
		this.match(this.lookAhead(), "define");
		
		LinkedList<OrderedTree<Token>> dChildren = new LinkedList<OrderedTree<Token>>();
		
		// add "define"
		dChildren.add(new OrderedTree<Token>(this.token()));
		dChildren.add(this.Sig());
		dChildren.add(this.expr());
		return new OrderedTree<Token>(new Token("Def"), dChildren);
	}
	
	private OrderedTree<Token> Sig()
	{
		this.match(this.lookAhead(), "UserFName");
		
		LinkedList<OrderedTree<Token>> sigChildren = new LinkedList<OrderedTree<Token>>();
		
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
	
	private LinkedList<OrderedTree<Token>> Var()
	{
		LinkedList<OrderedTree<Token>> vars = new LinkedList<OrderedTree<Token>>();
		while(this.lookAhead().getType().equals("Var"))
		{
			vars.add(new OrderedTree<Token>(this.token()));
		}
		if (vars.size() == 0)
		{
			return null;
		}
		return vars;
	}
	
	private LinkedList<OrderedTree<Token>> literal()
	{
		LinkedList<OrderedTree<Token>> literals = new LinkedList<OrderedTree<Token>>();
		
		this.match(this.lookAhead(), "[");
		{
			literals.add(new OrderedTree<Token>(this.token()));
		}		
		
		while(this.lookAhead().getType().equals("SymbolLiteral"))
		{
			literals.add(new OrderedTree<Token>(this.token()));
		}

		this.match(this.lookAhead(), "]");
		{
			literals.add(new OrderedTree<Token>(this.token()));
		}

		return literals;
	}
	
	private LinkedList<OrderedTree<Token>> FCall()
	{
		LinkedList<OrderedTree<Token>> fChildren = new LinkedList<OrderedTree<Token>>();
		
		this.match(this.lookAhead(), "PrimFName", "UserFName");
		{
			fChildren.add(new OrderedTree<Token>(this.token()));
		}
		
		// "(" always follows a function name with this grammar
		this.match(this.lookAhead(), "(");
		{
			fChildren.add(new OrderedTree<Token>(this.token()));
		}

		// until the function parameters are finished,
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
			else
			{
				fChildren.add(this.expr());
			}
			next = this.lookAhead().getType();
		}
		
		this.match(this.lookAhead(), ")");
		{
			fChildren.add(new OrderedTree<Token>(this.token()));
		}
		
		// go back up...
		return fChildren;
	}

	private void match(Token actual, String expected)
	{
		if(!actual.getType().equals(expected))
		{
			this.error(actual.getType(), expected);
		}
	}
	
	private void match(Token actual, String expected1, String expected2)
	{
		if(!actual.getType().equals(expected1) && !actual.getType().equals(expected2))
		{
			this.error(actual.getType(), expected1+" or "+expected2);
		}
	}
	
	private boolean match(Token actual, String[] expected)
	{
		boolean result = false;
		for(String potential : expected)
		{
			result |= actual.getType().equals(potential);
		}
		return result;
	}
	
	private void error(String spelling, String expectedType)
	{
		String message = spelling + " found, " + expectedType + " expected.";
		throw new IllegalArgumentException(message);
	}
	
}
