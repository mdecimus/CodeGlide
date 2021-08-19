/*
 * 	Copyright (C) 2007, CodeGlide - Entwickler, S.A.
 *	All rights reserved.
 *
 *	You may not distribute this software, in whole or in part, without
 *	the express consent of the author.
 *
 *	There is no warranty or other guarantee of fitness of this software
 *	for any purpose.  It is provided solely "as is".
 *
 */
package com.codeglide.interfaces.xmldb.sql;

/**
 * @author jwajnerman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Column extends Expression
{
	public static final Column ALL = new Column("*");
	
	private Table table;
	private String tableName;
	private String name;
	
	public Column(String name)
	{
		this.name = name;
	}
	
	public Column(Table table, String name)
	{
		this.table = table;
		this.name = name;
	}
	
	public Column(String tableName, String name)
	{
		this.tableName = tableName;
		this.name = name;
	}
	
	/**
	 * @return Returns the table.
	 */
	public Table getTable()
	{
		return table;
	}
	
	/**
	 * @return Returns the tableName.
	 */
	public String getTableName()
	{
		return tableName;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.Expression#accept(com.mintersoft.sql.ExpressionVisitor)
	 */
	public void accept(ExpressionVisitor v)
	{
		v.visit(this);
	}
}
