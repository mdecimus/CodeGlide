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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO Enter class description
 */
public class InsertQuery extends Query
{
	private String table;
	private Map values;
	
	InsertQuery(String table, SqlDialect dialect)
	{
		this(dialect);
		this.table = table;
	}
	
	InsertQuery(SqlDialect dialect)
	{
		super(dialect);
		values = new HashMap();
	}

	public void setTable(String table)
	{
		this.table = table;
	}
	
	public String getTable()
	{
		return table;
	}
	
	public void setValue(String column, Argument value)
	{
		values.put(column, value);
	}
	
	public void setValue(String column, int value)
	{
		setValue(column, new Argument(value));
	}
	
	public void setValue(String column, long value)
	{
		setValue(column, new Argument(value));
	}
	
	public void setValue(String column, String value)
	{
		setValue(column, new Argument(value));
	}
	
	public void setValue(String column, byte[] value)
	{
		setValue(column, new Argument(value));
	}
	
	public void setValue(String column, InputStream value)
	{
		setValue(column, new Argument(value));
	}
	
	public Map getValues()
	{
		return values;
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.Expression#accept(com.mintersoft.sql.ExpressionVisitor)
	 */
	public void accept(ExpressionVisitor v)
	{
		v.visit(this);
	}
}
