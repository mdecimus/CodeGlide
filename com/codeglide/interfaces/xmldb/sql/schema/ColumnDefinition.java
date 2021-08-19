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
package com.codeglide.interfaces.xmldb.sql.schema;

/**
 * @author jwajnerman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ColumnDefinition
{
	private String name;
	private int type;
	private int size;
	private boolean isNull;
	private boolean isPK;
	private boolean isAutonumeric;
	
	public final static int TYPE_INT = 1;
	public final static int TYPE_BIGINT = 2;
	public final static int TYPE_VARCHAR = 3;
	public final static int TYPE_BINARY = 4;
	public final static int TYPE_TEXT = 5;
	public final static int TYPE_NVARCHAR = 6;
	public final static int TYPE_NTEXT = 7;
	
	public ColumnDefinition(String name, int type, int size, boolean isNull, boolean isPK, boolean isAutonumeric)
	{
		this.name = name;
		this.type = type;
		this.size = size;
		this.isNull = isNull;
		this.isPK = isPK;
		this.isAutonumeric = isAutonumeric;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getType()
	{
		return type;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public boolean isNull()
	{
		return isNull;
	}
	
	public boolean isPrimaryKey()
	{
		return isPK;
	}
	
	public boolean isAutonumeric()
	{
		return isAutonumeric;
	}
	
	public boolean isUnicode()
	{
		return type == TYPE_NVARCHAR || type == TYPE_NTEXT;
	}
}
