package a5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class Interpreter 
{
	Parser parser;
	
	HashMap<String, Value> userFunctions;
	ScopeStack scopeStack;
	
	OrderedTree<Token> program;
	static int call = 1;
	public Interpreter()
	{
		this.parser = new Parser();
		this.userFunctions = new HashMap<String, Value>();
		this.scopeStack = new ScopeStack();
		this.program = new OrderedTree<Token>();
	}
	
	/** 
	 * @param program String to parse and interpret
	 * @throws IllegalArgumentException
	 */
	public Value interpret(String program) throws IllegalArgumentException
	{
		this.program = this.parser.parse(program);
		this.scopeStack.push("main");
		System.out.println();
		return this.interpret(this.program);
	}
	
	public Value interpret(OrderedTree<Token> tree) throws IllegalArgumentException
	{
		Lyst list = new Lyst();		
		Token root = tree.getRootData();
		switch(root.getType())
		{
			case("LetExpr"):
			{
				return this.handleLetExpr(tree);
			}
			case("Def"):
			{
				this.handleDef(tree);
				break;
			}
			case("SymbolLiteral"):
			{
				return new Symbol(root.getSpelling());
			}
			case("List"):
			{
				return this.generateList(tree);
			}
			case("UserFName"):
			{
				return this.callUserFunc(tree);
			}
			case("PrimFName"):
			{
				return this.callPrimFunc(tree);
			}
			case("IfExpr"):
			{
				return this.handleIfExpr(tree);
			}
			default:
			{
				throw new IllegalArgumentException("Cannot accept " + root.getSpelling() + " here.\n" + this.scopeStack.stackTrace());
			}
		}		
		return list;
	}
		
	private Value callPrimFunc(OrderedTree<Token> primFunc) 
	{
		String fName = primFunc.getRootData().getSpelling();
		this.scopeStack.push(fName);
		int childCount = primFunc.getNumberOfChildren();
		Value result;
		
		switch(fName)
		{
		case("car"):
		{
			// Illegal number of arguments for car. The correct number is 1.
			if(childCount != 1)
			{
				String msg = "Cannot call 'car' with " + childCount + " arguments...\n";
				msg += this.scopeStack.stackTrace();
				throw new IllegalArgumentException(msg);
			}
			
			// The final argument in each case is to be a list.
			String argType = primFunc.getKthChild(1).getRootData().getType();
			if(argType == "SymbolLiteral")
			{
				String msg = "Cannot call 'car' with argument " + argType + "...\n";
				msg += this.scopeStack.stackTrace();
				throw new IllegalArgumentException(msg);
			}
			Value newValue = this.interpret(primFunc.getKthChild(1));
			if(newValue.toLyst().getList().size() == 0)
			{
				String msg = "Cannot call 'car' with empty list!\n";
				msg += this.scopeStack.stackTrace();
				throw new IllegalArgumentException(msg);
			}
			Lyst newLyst = new Lyst(newValue.toLyst().getList());
			result = newLyst.popFront();
			break;
		}
		case("cdr"):
		{
			// Illegal number of arguments for car. The correct number is 1.
			if(childCount != 1)
			{
				String msg = "Cannot call 'cdr' with " + childCount + " arguments...\n";
				msg += this.scopeStack.stackTrace();
				throw new IllegalArgumentException(msg);
			}
			// The final argument in each case is to be a list.
			String argType = primFunc.getKthChild(1).getRootData().getType();
			if(argType == "SymbolLiteral")
			{
				String msg = "Cannot call 'cdr' with argument " + argType + "...\n";
				msg += this.scopeStack.stackTrace();
				throw new IllegalArgumentException(msg);
			}
			
			Value tempValue = this.interpret(primFunc.getKthChild(1));
			if(tempValue.toLyst().getList().size() == 0)
			{
				throw new IllegalArgumentException("Cannot call 'cdr' on empy list!\n" + this.scopeStack.stackTrace());
			}
			Lyst tempLyst = new Lyst(tempValue.toLyst().getList());
			tempLyst.popFront();
			result = tempLyst;
			break;
		}
		case("cons"):
		{
			// Illegal number of arguments for car. The correct number is 1.
			if(childCount != 2)
			{
				String msg = "Cannot call 'cdr' with " + childCount + " arguments...\n";
				msg += this.scopeStack.stackTrace();
				throw new IllegalArgumentException(msg);
			}
			
			OrderedTree<Token> firstChild = primFunc.getKthChild(1);
			OrderedTree<Token> secondChild = primFunc.getKthChild(2);
			Value firstArg = this.interpret(firstChild);
			Value secondArg = this.interpret(secondChild);
			// Illegal argument types. The final argument is to be a list. The first argument is to be a symbol.
			if(firstArg.type != "Symbol" || secondArg.type != "Lyst")
			{
				String msg = "Cannot call 'cons' with arguments of types '"+firstChild.getRootData().getType()+"' and '"+secondChild.getRootData().getType()+"'\n";
				msg += this.scopeStack.stackTrace();
				throw new IllegalArgumentException(msg);
			}

			Lyst newLyst = new Lyst();
			newLyst.add(firstChild.getRootData().getSpelling());
			newLyst.add(secondArg);
			result = newLyst;
			break;
		}
		default:
		{
			throw new IllegalArgumentException("Call to undefined function '"
					+ primFunc.getRootData().getSpelling()+"'\n" 
					+ this.scopeStack.stackTrace());
		}
		}
		this.scopeStack._pop();
		return result;
	}
	
	private Value callUserFunc(OrderedTree<Token> userFunc) 
	{
		// Calls to user-defined functions with arguments
		if(userFunc.getNumberOfChildren() > 0)
		{
			String msg = "Cannot call User-defined functions with arguments!\n";
			msg += this.scopeStack.stackTrace();
			throw new IllegalArgumentException(msg);
		}
		
		// Calls (with no arguments) to undefined functions
		if(!this.userFunctions.containsKey(userFunc.getRootData().getSpelling()))
		{
			String msg = "Call to undefined function!\n";
			msg += this.scopeStack.stackTrace();
			throw new IllegalArgumentException(msg); 
		}
		
		// return the hashed Value
		String fName = userFunc.getRootData().getSpelling();
		this.scopeStack.push(fName);
		Value result = this.userFunctions.get(fName);
		this.scopeStack._pop();
		return result;
	}
	
	private Value handleIfExpr(OrderedTree<Token> ifExpr) throws IllegalArgumentException
	{
		Value tempLyst;
		tempLyst = this.interpret(ifExpr.getKthChild(1));
		if(tempLyst.toLyst().getList().size() == 0)
		{
			return this.interpret(ifExpr.getKthChild(3));
		}
		else
		{
			return this.interpret(ifExpr.getKthChild(2));
		}
	}
	
	public Value handleLetExpr(OrderedTree<Token> let) throws IllegalArgumentException
	{
		int i=1;
		while(let.getKthChild(i).getRootData().getType() == "Def")
		{
			this.handleDef(let.getKthChild(i));
			i++;		
		}
		return this.interpret(let.getKthChild(i));
	}
	
	public void handleDef(OrderedTree<Token> def)
	{		
		String userFName = this.handleMakeUserFunction(def.getKthChild(1));
		Value v = this.interpret(def.getKthChild(2));

		this.userFunctions.put(userFName, v);
	}

	public String handleMakeUserFunction(OrderedTree<Token> userFunc)
	{
		// Definitions of user-defined functions with arguments
		if(userFunc.getNumberOfChildren() > 0)
		{
			String msg = "User-defined functions must not have arguments!\n";
			for(int i=1; i<=userFunc.getNumberOfChildren(); i++)
			{
				Token arg = userFunc.getKthChild(i).getRootData();
				msg += "\tType: " + arg.getType() + " Value: " + arg.getSpelling() + "\n";
			}
			msg += this.scopeStack.stackTrace();
			throw new IllegalArgumentException(msg);
		}
		else
		{
			return userFunc.getRootData().getSpelling();
		}
	}
	
	public Lyst generateList(OrderedTree<Token> list)
	{
		Lyst newList = new Lyst();
		for(int i=1; i<=list.getNumberOfChildren(); i++)
		{
			newList.add(new Symbol(list.getKthChild(i).getRootData().getSpelling()));
		}
		return newList;
	}
}
