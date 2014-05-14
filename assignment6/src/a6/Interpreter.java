package a6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

class Frame extends HashMap<String, MapValue> 
{
	private static final long serialVersionUID = -2505130992225465215L; 
} 

public class Interpreter 
{
	/**
	 * Class to interpret grammar used throughout CS152
	 *  in Spring, 2014 under professor Jeffrey Smith
	 *  
	 * Uses Parser class defined by Dr. Smith
	 * 
	 * @author jaronhalt
	 *
	 */
	private class Environment
	{
		/**
		 * Class to represent the environment for a particular interpreter.
		 */
		private LinkedList<Frame> frameStack;
		
		public Environment()
		{
			this.frameStack = new LinkedList<Frame>();
			// Initial frame entry
			this.frameStack.add(new Frame());
		}
		
		/**
		 * Pushes new frame to the front of the frame stack.
		 */
		public void pushFrame()
		{
			this.frameStack.addFirst(new Frame());
		}
		
		/**
		 * Removes the frame from the front of the frame stack
		 */
		public void popFrame()
		{
			this.frameStack.removeFirst();
		}
		
		/**
		 * Searches the frameStack for the first occurrence of var.
		 * @param var The variable to find the value of.
		 * @return MapValue of var.
		 */
		public MapValue getValueOf(String var)
		{
			MapValue value;
			for(int i=0; i<this.frameStack.size(); i++)
			{
				value = this.frameStack.get(i).get(var);
				if(value != null)
				{
					return value;
				}
			}
			return null;
		}

		/**
		 * Adds a value to the current frame in the frameStack
		 * @param var The variable 
		 * @param val The value of the variable
		 */
		public void putValueOf(String var, MapValue val)
		{
			this.frameStack.getFirst().put(var, val);
		}

	}
	
	Parser parser;
	Environment environment;

	public Interpreter()
	{
		this.parser = new Parser();
		this.environment = new Environment();
	}
	
	/** 
	 * @param program String to parse and interpret
	 * @throws IllegalArgumentException
	 */
	public MapValue interpret(String program) throws IllegalArgumentException
	{
		OrderedTree<Token> parsedProgram = this.parser.parse(program);
		return this.interpret(parsedProgram);
	}
	
	/**
	 * Interprets the Expr passed and returns the interpreted MapValue
	 * @param tree The code to interpret
	 * @return The MapValue determined via interpretation
	 * @throws IllegalArgumentException
	 */
	public MapValue interpret(OrderedTree<Token> tree) throws IllegalArgumentException
	{
		Token root = tree.getRootData();
		switch(root.getType())
		{
			case("LetExpr"):
			{
				return this.handleLetExpr(tree);
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
			case("Var"):
			{
				MapValue result = this.environment.getValueOf(root.getSpelling());
				if(result != null)
				{
					return result;
				}
				else
				{
					// go to default
				}
			}
			default:
			{
				throw new IllegalArgumentException("Cannot accept undefined '" + root.getSpelling() + "' of type '" + root.getType() + "' here.");
			}
		}
	}
		
	private MapValue callPrimFunc(OrderedTree<Token> primFunc) 
	{
		String fName = primFunc.getRootData().getSpelling();
		int childCount = primFunc.getNumberOfChildren();
		MapValue result;
		
		switch(fName)
		{
		case("car"):
		{
			// Illegal number of arguments for car. The correct number is 1.
			if(childCount != 1)
			{
				String msg = "Cannot call 'car' with " + childCount + " arguments...\n";
				throw new IllegalArgumentException(msg);
			}
			
			// The final argument in each case is to be a list.
			String argType = primFunc.getKthChild(1).getRootData().getType();
			if(argType == "SymbolLiteral")
			{
				String msg = "Cannot call 'car' with argument " + argType + "...\n";
				throw new IllegalArgumentException(msg);
			}
			MapValue newValue = this.interpret(primFunc.getKthChild(1));
			if(newValue.toLyst().getList().size() == 0)
			{
				String msg = "Cannot call 'car' with empty list!\n";
				throw new IllegalArgumentException(msg);
			}
			Lyst newLyst = new Lyst(newValue.toLyst().getList());
			result = newLyst.at(0);
			break;
		}
		case("cdr"): 
		{
			// Illegal number of arguments for car. The correct number is 1.
			if(childCount != 1)
			{
				String msg = "Cannot call 'cdr' with " + childCount + " arguments...\n";
				throw new IllegalArgumentException(msg);
			}
			// The final argument in each case is to be a list.
			String argType = primFunc.getKthChild(1).getRootData().getType();
			if(argType == "SymbolLiteral")
			{
				String msg = "Cannot call 'cdr' with argument " + argType + "...\n";
				throw new IllegalArgumentException(msg);
			}
			
			// indirect recursive dive...
			MapValue tempValue = this.interpret(primFunc.getKthChild(1));
			if(tempValue.toLyst().getList().size() == 0)
			{
				throw new IllegalArgumentException("Cannot call 'cdr' on empy list!");
			}
			ArrayList<MapValue> newItems = new ArrayList<MapValue>(tempValue.toLyst().getList().subList(1, tempValue.toLyst().getList().size()));
			Lyst tempLyst = new Lyst(newItems);
			result = tempLyst;
			break;
		}
		case("cons"):
		{
			// Illegal number of arguments for car. The correct number is 1.
			if(childCount != 2)
			{
				String msg = "Cannot call 'cons' with " + childCount + " arguments...\n";
				throw new IllegalArgumentException(msg);
			}
			
			OrderedTree<Token> firstChild = primFunc.getKthChild(1);
			OrderedTree<Token> secondChild = primFunc.getKthChild(2);
			
			// Indirect recursive dives...
			MapValue firstArg = this.interpret(firstChild);
			MapValue secondArg = this.interpret(secondChild);
			
			// Illegal argument types. The final argument is to be a list. The first argument is to be a symbol.
			if(firstArg.getType() != Symbol.type || secondArg.getType() != Lyst.type)
			{
				String msg = "Cannot call 'cons' with arguments of types '"+firstArg.getType()+"' and '"+secondArg.getType()+"'\n";
				throw new IllegalArgumentException(msg);
			}

			Lyst newLyst = new Lyst();
			newLyst.addAll(firstArg);
			newLyst.addAll(secondArg);
			result = newLyst;
			break;
		}
		default:
		{
			throw new IllegalArgumentException("Call to undefined function '"
					+ primFunc.getRootData().getSpelling() + "'");
		}
		}

		return result;
	}
	
	private MapValue callUserFunc(OrderedTree<Token> userFunc) 
	{
		// push!
		this.environment.pushFrame();
		MapValue result;
		String funcName = userFunc.getRootData().getSpelling();

		// get the environment's mapped definition
		MapValue mappedFunction = this.environment.getValueOf(funcName);

		// Calls to undefined functions
		if(mappedFunction == null)
		{
			String msg = "Call to undefined function: " + funcName;
			throw new IllegalArgumentException(msg); 
		}
		
		// it's a function so get the parameters to map to this environment
		ArrayList<MapValue> paramList = mappedFunction.getList();
		
		// param count mismatch
		if(paramList.size() != userFunc.getNumberOfChildren())
		{
			String msg = "Call to user-defined function with " + userFunc.getNumberOfChildren() + "arguments,\n";
			msg += funcName + " expects " + paramList.size() + "arguments.";
			throw new IllegalArgumentException(msg);
		}
		
		// map the values corresponding to the Funciton's parameter list...
		for(int i=0; i<paramList.size(); i++)
		{
			this.environment.putValueOf(paramList.get(i).toString(), this.interpret(userFunc.getKthChild(i+1)));
		}
		
		// finally, interpret the function indirectly recursively.
		result = this.interpret( ((Function)mappedFunction).call() );
		
		// pop!
		this.environment.popFrame();
		return result;
	}
	
	private MapValue handleIfExpr(OrderedTree<Token> ifExpr) throws IllegalArgumentException
	{
		MapValue tempLyst = this.interpret(ifExpr.getKthChild(1));
		if(tempLyst.getType() == Function.type)
		{
			throw new IllegalArgumentException ("HOW DID THIS HAPPEN!");
		}
		
		if(tempLyst.getList().size() == 0)
		{
			return this.interpret(ifExpr.getKthChild(3));
		}
		else
		{
			return this.interpret(ifExpr.getKthChild(2));
		}
	}
	
	public MapValue handleLetExpr(OrderedTree<Token> let) throws IllegalArgumentException
	{
		// push!
		this.environment.pushFrame();
		int i=1;
		while(let.getKthChild(i).getRootData().getType() == "Def")
		{
			this.define(let.getKthChild(i));
			i++;		
		}
		return this.interpret(let.getKthChild(i));
	}
	
	public void define(OrderedTree<Token> def)
	{	
		String userFName = def.getKthChild(1).getRootData().getSpelling();
		MapValue v = new Function(def);
		this.environment.putValueOf(userFName, v);
	}
	
	/**
	 * Generates a list and accepts nested lists...
	 * @param list the root node of children that will be added to a new Lyst
	 * @return a Lyst of all the children of 'list'
	 */
	public Lyst generateList(OrderedTree<Token> list)
	{
		Lyst newList = new Lyst();
		OrderedTree<Token> child;
		Token rootData;
		for(int i=1; i<=list.getNumberOfChildren(); i++)
		{
			child = list.getKthChild(i);
			rootData = child.getRootData();
			// I know there's a way to do this more Java-friendly...<? super > something..no internet now...
			if(rootData.getType() == "SymbolLiteral")
			{
				newList.add(new Symbol(rootData.getSpelling()));
			}
			else if (rootData.getType() == "List")
			{
				newList.add(new Lyst(child));
			}
			else
			{
				throw new IllegalArgumentException("Cannot create Lyst from value: " + rootData.getSpelling() + " type: " + rootData.getType());
			}
		}
		return newList;
	}
}
