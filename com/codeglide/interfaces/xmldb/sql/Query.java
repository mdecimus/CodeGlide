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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.codeglide.core.Logger;

/**
 * TODO Enter class description
 */
public abstract class Query extends Expression
{
	private SqlDialect dialect;
	
	public Query(SqlDialect dialect)
	{
		this.dialect = dialect;
	}

	public String toString()
	{
		SqlGenerator generator = dialect.createGenerator();
		accept(generator);
		return generator.getGeneratedSql();
	}

	public PreparedStatement prepareStatement(Connection conn) throws SQLException
	{
		return prepareStatement(conn, false);
	}
	
	public PreparedStatement prepareStatement(Connection conn, boolean returnKeys) throws SQLException
	{
		SqlGenerator generator = dialect.createGenerator();
		accept(generator);
		String sql = generator.getGeneratedSql();
		
		PreparedStatement statement;
		if (returnKeys)
			statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		else
			statement = conn.prepareStatement(sql);
		
		//TODO remove logger
		Logger.debug(sql);
		
		int i = 1;
		for (Iterator argIt = generator.getArguments().iterator(); argIt.hasNext(); i++)
		{
			Argument arg = (Argument) argIt.next();
			Object value = arg.getValue();
			
			if (value == null)
			{
				statement.setNull(i, arg.getNullType());
			}
			if (value instanceof String)
			{
				statement.setString(i, (String) value);
				//statement.setBytes(i, ((String) arg).getBytes());
			}
			else if (value instanceof Integer)
			{
				statement.setInt(i, ((Integer) value).intValue());
			}
			else if (value instanceof Long)
			{
				statement.setLong(i, ((Long) value).longValue());
			}
			else if (value instanceof byte[])
			{
				statement.setBytes(i, (byte[]) value);
			} 
			else if (value instanceof InputStream)
			{
				statement.setBinaryStream(i, (InputStream) value);
			} 
		}
		
		return statement;
	}
}
