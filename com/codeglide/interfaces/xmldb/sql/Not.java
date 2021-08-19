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

public class Not extends BooleanExpression {
	private final Expression notExpr;

	public Not(Expression notExpr)
	{
		this.notExpr = notExpr;
	}
	
	/**
	 * @return Returns the notExpr.
	 */
	public Expression getExpr()
	{
		return notExpr;
	}

	/* (non-Javadoc)
	 * @see com.mintersoft.sql.Expression#accept(com.mintersoft.sql.ExpressionVisitor)
	 */
	public void accept(ExpressionVisitor v)
	{
		v.visit(this);
	}
}
