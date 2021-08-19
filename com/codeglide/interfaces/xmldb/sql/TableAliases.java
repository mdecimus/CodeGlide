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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author jwajnerman
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class TableAliases
{
	private Map aliases = new HashMap();
	private int i = 1;

	public void setAlias(Table table)
	{
		String alias = "t" + i++;
		aliases.put(table, alias);
	}

	public String getAlias(String tableName)
	{
		String alias = null;

		for (Iterator entryIt = aliases.entrySet().iterator(); entryIt.hasNext();)
		{
			Map.Entry entry = (Map.Entry) entryIt.next();
			Table table = (Table) entry.getKey();
			if (table.getName().equals(tableName))
			{
				if (alias != null)
					throw new Error("More than one table was found with same name. Explicit table reference must be used");
				
				alias = (String) entry.getValue();
			}
		}

		if (alias == null)
			throw new Error("No tables were found with specified name");

		return alias;
	}

	public String getAlias(Table table)
	{
		return (String) aliases.get(table);
	}
}