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
import java.util.Iterator;
import java.util.Map;

/**
 * @author jwajnerman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BatchDeleter
{
	private int deleteBatchSize;
	private Map deletes;
	
	public BatchDeleter(int deleteBatchSize)
	{
		this.deleteBatchSize = deleteBatchSize;
		this.deletes = new HashMap();
	}

	public void addDelete(String tableName, String idColumnName, int id)
	{
		DeleteEntry entry;
		
		if (deletes.containsKey(tableName))
		{
			entry = (DeleteEntry) deletes.get(tableName);
			if (!entry.idColumnName.equals(idColumnName))
				throw new IllegalStateException("Table already registered with other id column");
		}
		else
		{
			entry = new DeleteEntry(tableName, idColumnName);
			deletes.put(tableName, entry);
		}
		
		entry.ids.add(new Integer(id));
	}
	
	public Iterator getQueryIterator(SqlDialect dialect)
	{
		return new DeleterIterator(dialect);
	}
	
	private class DeleteEntry
	{
		public String tableName;
		public String idColumnName;
		public ArrayList ids = new ArrayList();
		
		public DeleteEntry(String tableName, String idColumnName)
		{
			this.tableName = tableName;
			this.idColumnName = idColumnName;
		}
	}

	private class DeleterIterator implements Iterator
	{
		private DeleteEntry currentEntry;
		private SqlDialect dialect;
		
		public DeleterIterator(SqlDialect dialect)
		{
			this.dialect = dialect;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		public boolean hasNext()
		{
			return currentEntry != null || !deletes.isEmpty();
		}

		public Object next()
		{
			if (currentEntry == null)
			{
				Object key = deletes.keySet().iterator().next();
				currentEntry = (DeleteEntry) deletes.remove(key);
			}
			
			StringBuffer ids = new StringBuffer();
			int count = 0;
			while (!currentEntry.ids.isEmpty() && count < deleteBatchSize)
			{
				Integer id = (Integer) currentEntry.ids.remove(currentEntry.ids.size() - 1);
				if (count > 0)
					ids.append(",");
				ids.append(id.intValue());
				count++;
			}
			
			DeleteQuery delete = dialect.createDelete(currentEntry.tableName);
			delete.addFilter(new In(new Column(currentEntry.idColumnName), ids.toString()));
			
			if (currentEntry.ids.isEmpty())
				currentEntry = null;
			
			return delete;
		}
		
	}
}
