package a6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

abstract class MapValue {

	public String spelling;
	
	public MapValue()
	{
		this.spelling = null;
	}
	
	protected MapValue(String spelling, String type)
	{
		this.spelling = spelling;
	}
	public String toString()
	{
		return this.spelling;
	}
	
	public abstract ArrayList<MapValue> getList();
	public abstract MapValue toLyst();
	public abstract String getType();
}

class Function extends MapValue
{
	public static String type = "Function";
	private String name;
	private OrderedTree<Token> algorithm;
	private ArrayList<MapValue> params;
	
	/**
	 * Pass the Root of the function definition ("Def")
	 * @param definition The function's definition
	 */
	public Function(OrderedTree<Token> definition)
	{
		this.name = definition.getKthChild(1).getRootData().getSpelling();
		this.algorithm = definition.getKthChild(2);

		this.params = new ArrayList<MapValue>(0);
		OrderedTree<Token> paramsList = definition.getKthChild(1);
		for(int i=1; i <= paramsList.getNumberOfChildren(); i++)	
		{
			this.params.add(new Symbol(paramsList.getKthChild(i).getRootData().getSpelling()));
		}
	}
	
	public OrderedTree<Token> call()
	{
		return this.algorithm;
	}

	@Override
	public ArrayList<MapValue> getList() {
		return this.getParamsList();
	}
	
	public ArrayList<MapValue> getParamsList()
	{
		return this.params;
	}

	@Override
	public MapValue toLyst() {
		return this.paramsToLyst();
	}
	
	public MapValue paramsToLyst() {
		Lyst list = new Lyst(this.getParamsList());
		return list;
	}

	@Override
	public String getType() {
		return Function.type;
	}
}

class Symbol extends MapValue
{	
	public static String type = "Symbol";
	
	public Symbol(String spelling)
	{
		super(spelling, "Symbol");
	}
	
	public Lyst toLyst()
	{
		Lyst newLyst = new Lyst();
		newLyst.add(this.spelling);
		return newLyst;
	}
	
	public MapValue getValue()
	{
		return this;
	}

	@Override
	public String getType() {
		return Symbol.type;
	}

	@Override
	public ArrayList<MapValue> getList() {
		ArrayList<MapValue> list = new ArrayList<MapValue>(0);
		list.add(this);
		return list;
	}
}

class Lyst extends MapValue
{
	public static String type = "Lyst";
	
	ArrayList<MapValue> lyst;
	public Lyst()
	{
		this.lyst = new ArrayList<MapValue>(0);
	}
	
	public Lyst(ArrayList<MapValue> lyst)
	{
		this.lyst = new ArrayList<MapValue>(lyst);
	}
	
	public Lyst(OrderedTree<Token> list)
	{
		
		this.lyst = new ArrayList<MapValue>(0);
		OrderedTree<Token> child;
		Token rootData;
		String type;
		for(int i=1; i<=list.getNumberOfChildren(); i++)
		{
			child = list.getKthChild(i);
			rootData = child.getRootData();
			type = rootData.getType();
			if(type == "SymbolLiteral")
			{
				this.lyst.add(new Symbol(rootData.getSpelling()));
			}
			else if (type == "List")
			{
				this.lyst.add(new Lyst(child));
			}
			else
			{
				throw new IllegalArgumentException("Cannot create Lyst from value: " + rootData.getSpelling() + " type: " + rootData.getType());
			}
		}
	}
	
	public void addAll(MapValue v)
	{
		this.lyst.addAll(v.toLyst().getList());
	}
	
	public void add(MapValue s)
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

	public MapValue at(int pos)
	{
		return this.lyst.get(pos);
	}
	
	public MapValue popFront()
	{
		return this.lyst.remove(0);
	}

	public MapValue popBack()
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
	
	public ArrayList<MapValue> getList()
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
		for(MapValue l : this.lyst)
		{
			result += l + ", ";
		}
		if(result.length()>3)
		{
			result = result.substring(0, result.length()-2);
		}
		result += "]";
		return result;
	}

	@Override
	public String getType() {
		return Lyst.type;
	}
}
