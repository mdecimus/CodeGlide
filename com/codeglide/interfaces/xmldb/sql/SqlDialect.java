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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.codeglide.interfaces.xmldb.sql.schema.TableDefinition;

/**
 * @author jwajnerman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class SqlDialect
{
	public abstract SqlGenerator createGenerator();
	
	public SelectQuery createSelect()
	{
		return new SelectQuery(this);
	}
	
	public InsertQuery createInsert()
	{
		return new InsertQuery(this);
	}
	
	public InsertQuery createInsert(String tableName)
	{
		return new InsertQuery(tableName, this);
	}
	
	public UpdateQuery createUpdate()
	{
		return new UpdateQuery(this);
	}
	
	public UpdateQuery createUpdate(String tableName)
	{
		return new UpdateQuery(tableName, this);
	}
	
	public abstract String[] generateTableStatements(TableDefinition table);
	
	public boolean supportsMultipleQueries()
	{
		return true;
	}
	
	public void executeMultipleUpdates(Query[] queries, Connection conn) throws SQLException
	{
		if (supportsMultipleQueries())
		{
			BatchQuery batch = new BatchQuery(this);
			for (int i = 0; i < queries.length; i++)
				batch.addQuery(queries[i]);
			
			PreparedStatement stmt = batch.prepareStatement(conn);
			try
			{
				stmt.executeUpdate();
			}
			finally
			{
				stmt.close();
			}
		}
		else
		{
			for (int i = 0; i < queries.length; i++)
			{
				PreparedStatement stmt = queries[i].prepareStatement(conn);
				try
				{
					stmt.executeUpdate();
				}
				finally
				{
					stmt.close();
				}
			}
		}
	}

	/**
	 * @return
	 */
	public DeleteQuery createDelete(String tableName)
	{
		return new DeleteQuery(this, tableName);
	}
}
