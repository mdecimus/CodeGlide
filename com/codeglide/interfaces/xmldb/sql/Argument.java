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

/**
 * @author jwajnerman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Argument extends Expression
{
	private Object value;
	private int nullType;
	
	private Argument()
	{
		this.value = null;
	}
	
	public Argument(int value)
	{
		this.value = new Integer(value);
	}
	
	public Argument(long value)
	{
		this.value = new Long(value);
	}
	
	public Argument(double value)
	{
		this.value = new Double(value);
	}
	
	public Argument(String value)
	{
		this.value = value;
	}
	
	public Argument(byte[] value)
	{
		this.value = value;
	}

	public Argument(InputStream value)
	{
		this.value = value;
	}

	/**
	 * @return Returns the value.
	 */
	public Object getValue()
	{
		return value;
	}
	
	public int getNullType()
	{
		return nullType;
	}
	
	public static Argument getNull(int sqlType)
	{
		Argument arg = new Argument();
		arg.nullType = sqlType;
		return arg;
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.Expression#accept(com.mintersoft.sql.ExpressionVisitor)
	 */
	public void accept(ExpressionVisitor v)
	{
		v.visit(this);
	}
}
