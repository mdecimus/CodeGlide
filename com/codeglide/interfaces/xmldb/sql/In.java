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
public class In extends BooleanExpression
{
	private final Expression expr;
	private SelectQuery subQuery;
	private String values;

	public In(Expression expr, SelectQuery subQuery)
	{
		this.expr = expr;
		this.subQuery = subQuery;
	}
	
	public In(Expression expr, String values)
	{
		this.expr = expr;
		this.values = values;
	}
	
	/**
	 * @return Returns the expr.
	 */
	public Expression getExpr()
	{
		return expr;
	}
	
	/**
	 * @return Returns the subQuery.
	 */
	public SelectQuery getSubQuery()
	{
		return subQuery;
	}
	
	/**
	 * @return Returns the values.
	 */
	public String getValues()
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
