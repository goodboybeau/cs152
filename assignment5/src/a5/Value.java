package a5;

import java.util.ArrayList;

abstract class Value
{
	public String type;
	public abstract String toString();
	public abstract Lyst toLyst();
}

class Symbol extends Value
{
	String spelling;
	
	public Symbol(String spelling)
	{
		this.type = "Symbol";
		this.spelling = spelling;
	}
	
	public Lyst toLyst()
	{
		Lyst newLyst = new Lyst();
		newLyst.add(this.spelling);
		return newLyst;
	}
	
	@Override
	public String toString() 
	{

		return this.spelling;
	}
}

class Lyst extends Value
{
	ArrayList<Value> lyst;
	public Lyst()
	{
		this.type = "Lyst";
		this.lyst = new ArrayList<Value>();
	}
	
	public Lyst(ArrayList<Value> lyst)
	{
		this.type = "Lyst";
		this.lyst = lyst;
	}
	
	public void add(Value v)
	{
		this.lyst.addAll(v.toLyst().getList());
	}
	
	public void add(Symbol s)
	{
		this.lyst.add(s);
	}
	
	public void add(String s)
	{
		if (s == "")
		{
			return;
		}
		else
		{
			this.add(new Symbol(s));
		}
	}

	public Value at(int pos)
	{
		return this.lyst.get(pos);
	}
	
	public Value popFront()
	{
		return this.lyst.remove(0);
	}

	public Value popBack()
	{
		try
		{
			return this.lyst.remove(this.lyst.size()-1);
		}
		catch (IndexOutOfBoundsException ioobe)
		{
			return null;
		}
	}
	
	public ArrayList<Value> getList()
	{
		return this.lyst;
	}
	
	public Lyst toLyst()
	{
		Lyst newLyst = new Lyst(this.lyst);
		return newLyst;
	}
	
	@Override
	public String toString()
	{
		String result="[";
		for(Value l : this.lyst)
		{
			result += l + ", ";
		}
		result = result.substring(0, result.length()-2);
		result += "]";
		return result;
	}
}
