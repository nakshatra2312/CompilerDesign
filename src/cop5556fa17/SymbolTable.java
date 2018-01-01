package cop5556fa17;

import java.util.*;

import cop5556fa17.AST.Declaration;

public class SymbolTable {
	HashMap<String, Declaration> symbolTable;
	
	public SymbolTable()
	{
		symbolTable=new HashMap<String, Declaration>();
	}
	
	public TypeUtils.Type lookupType(String name)
	{
		TypeUtils.Type type=null;
		if(symbolTable.get(name)!=null)
		{
			type= TypeUtils.getType(symbolTable.get(name).firstToken);
		}
		
		
		
		return type;
	}
	
	public Declaration lookupDec(String name)
	{
		if(name!=null)
		{
		return symbolTable.get(name);
		}
		else
		{
			return null;
		}
	}
	
	public void insert(String name, Declaration dec)
	{
		symbolTable.put(name, dec);
	}

}
