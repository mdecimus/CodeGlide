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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author jwajnerman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TableDefinition
{
	private String name;
	private ArrayList columns;
	private ArrayList indexes;
	
	public TableDefinition(String name)
	{
		this.name = name;
		this.columns = new ArrayList();
		this.indexes = new ArrayList();
	}
	
	public void addColumn(ColumnDefinition col)
	{
		columns.add(col);
	}
	
	public void addIndex(IndexDefinition index)
	{
		indexes.add(index);
	}
	
	public String getName()
	{
		return name;
	}
	
	public List getColumns()
	{
		return columns;
	}
	
	public ColumnDefinition getColumn(String name)
	{
		for (Iterator colIt = columns.iterator(); colIt.hasNext();)
		{
			ColumnDefinition col = (ColumnDefinition) colIt.next();
			if (col.getName().equals(name))
				return col;
		}
		
		return null;
	}
	
	public List getIndexes()
	{
		return indexes;
	}
}
