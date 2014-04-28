package a5;

import java.util.Stack;

public class ScopeStack extends Stack<ScopeStack.Scope>
{
	private static final long serialVersionUID = 1L;
	private int currentDepth = 0;
	
	public ScopeStack.Scope push(String scopeName)
	{
		ScopeStack.Scope newScope = new ScopeStack.Scope(scopeName, this.currentDepth);
		this.currentDepth ++;
		this.push(newScope);
		return newScope;
	}
	
	public ScopeStack.Scope _pop() 
	{
		ScopeStack.Scope top = this.pop();
		this.currentDepth --;
		return top;
	}
	
	public String stackTrace()
	{
		String stackTrace = "Stack Trace: ";
		do
		{
			stackTrace += "\n\t" + this._pop().getName();
		} while(this.currentDepth > 0);
		return stackTrace;
	}
	
	class Scope
	{
		private int depth = 0;
		private String name;
		
		public Scope(String name, int depth)
		{
			this.name = name;
			this.depth = depth;
		}			
		
		public String getName()
		{
			return this.name;
		}
		
		public int getDepth()
		{
			return this.depth;
		}
	}
	
	public class ScopeStackException extends Exception
	{
		private static final long serialVersionUID = 1L;
	
		public ScopeStackException(String msg)
		{
			super(msg);
		}
	}

}