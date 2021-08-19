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

/**
 * @author jwajnerman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DeleteQuery extends Query
{
	private String table;
	private ArrayList filters;
	
	public DeleteQuery(SqlDialect dialect)
	{
		super(dialect);
	}
	
	public DeleteQuery(SqlDialect dialect, String table)
	{
		super(dialect);
		this.table = table;
	}
	
	/**
	 * @return Returns the table.
	 */
	public String getTable()
	{
		return table;
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

	public void accept(ExpressionVisitor v)
	{
		v.visit(this);
	}

}
