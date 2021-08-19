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
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class GreaterOrLess extends BooleanExpression
{
	private Expression left;
	private Expression right;
	private short type;

	public final static short GREATERTHAN = 1;
	public final static short GREATERTHANEQUAL = 2;
	public final static short LESSTHAN = 3;
	public final static short LESSTHANEQUAL = 4;
	
	public GreaterOrLess(Expression left, Expression right, short type )
	{
		this.left = left;
		this.right = right;
		this.type = type;
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.Expression#accept(com.mintersoft.sql.ExpressionVisitor)
	 */
	public void accept(ExpressionVisitor v)
	{
		v.visit(this);
	}

	/**
	 * @return Returns the left.
	 */
	public Expression getLeft()
	{
		return left;
	}

	/**
	 * @param left The left to set.
	 */
	public void setLeft(Expression left)
	{
		this.left = left;
	}

	/**
	 * @return Returns the right.
	 */
	public Expression getRight()
	{
		return right;
	}

	/**
	 * @param right The right to set.
	 */
	public void setRight(Expression right)
	{
		this.right = right;
	}
	
	public short getType() {
		return this.type;
	}
}