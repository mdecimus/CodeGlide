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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jwajnerman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UpdateQuery extends Query
{
	private String table;
	private Map values = new HashMap();
	private ArrayList filters;
	
	public UpdateQuery(SqlDialect dialect)
	{
		super(dialect);
	}

	public UpdateQuery(String tableName, SqlDialect dialect)
	{
		super(dialect);
		this.table = tableName;
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
	
	public void setValue(String column, String value)
	{
		setValue(column, new Argument(value));
	}
	
	public void setValue(String column, byte[] value)
	{
		setValue(column, new Argument(value));
	}
	
	public void addFilter(BooleanExpression filter)
	{
		if (filters == null)
			 filters = new ArrayList();
		filters.add(filter);
	}
	
	public ArrayList getFilters()
	{
		return filters;
	}
	
	public Map getValues()
	{
		return values;
	}
	
	public void accept(ExpressionVisitor v)
	{
		v.visit(this);
	}
}
