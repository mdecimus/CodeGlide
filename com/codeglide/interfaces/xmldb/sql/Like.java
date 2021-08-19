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
public class Like extends BooleanExpression
{
	private Expression arg;
	private Expression pattern;
	
	public Like(Expression arg, Expression pattern)
	{
		this.arg = arg;
		this.pattern = pattern;
	}

	/**
	 * @param column
	 * @param string
	 */
	public Like(Expression arg, String string)
	{
		this.arg = arg;
		this.pattern = new Literal(string);
	}
	
	/**
	 * @return Returns the arg.
	 */
	public Expression getArgument()
	{
		return arg;
	}
	
	/**
	 * @return Returns the pattern.
	 */
	public Expression getPattern()
	{
		return pattern;
	}

	/* (non-Javadoc)
	 * @see com.mintersoft.sql.Expression#accept(com.mintersoft.sql.ExpressionVisitor)
	 */
	public void accept(ExpressionVisitor v)
	{
		v.visit(this);
	}
}
