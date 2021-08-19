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

import java.util.Iterator;
import java.util.Map;

/**
 * TODO Enter class description
 */
public abstract class SqlGenerator implements ExpressionVisitor
{
	protected ArgumentsHolder args;
	protected StringBuffer sql;
	protected TableAliases tables;
	private static Class generatorClass;
	
	public SqlGenerator()
	{
		args = new ArgumentsHolder();
		tables = new TableAliases();
		sql = new StringBuffer();
	}
	
	public String getGeneratedSql()
	{
		return sql.toString();
	}
	
	public ArgumentsHolder getArguments()
	{
		return args;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.Argument)
	 */
	public void visit(Argument argument)
	{
		if (args != null)
			args.append(argument);

		sql.append("?");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.Column)
	 */
	public void visit(Column column)
	{
		String alias;
		Table table = column.getTable();
		String tableName = column.getTableName();
		String name = column.getName();

		if (table != null)
			alias = tables.getAlias(table);
		else if (tableName != null)
			alias = tables.getAlias(tableName);
		else
		{
			sql.append(name);
			return;
		}

		sql.append(alias + '.' + name);
	}

	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.Count)
	 */
	public void visit(Count count)
	{
		sql.append("COUNT(*)");
	}

	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.Equals)
	 */
	public void visit(Equals equals)
	{
		equals.getLeft().accept(this);
		sql.append('=');
		equals.getRight().accept(this);
	}
	
	public void visit(GreaterOrLess gtl)
	{
		gtl.getLeft().accept(this);
		if( gtl.getType() == GreaterOrLess.GREATERTHAN )
			sql.append('>');
		else if( gtl.getType() == GreaterOrLess.GREATERTHANEQUAL )
			sql.append(">=");
		else if( gtl.getType() == GreaterOrLess.LESSTHAN )
			sql.append('<');
		else if( gtl.getType() == GreaterOrLess.LESSTHANEQUAL )
			sql.append("<=");
		gtl.getRight().accept(this);
	}
	
	public void visit(RawExpression raw)
	{
		sql.append(raw.getQuery());
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.Or)
	 */
	public void visit(Or or)
	{
		sql.append('(');
		BooleanExpression[] exp = or.getExpressions();
		for( int i = 0; i < exp.length; i++ ) {
			exp[i].accept(this);
			if( i < exp.length - 1)
				sql.append(" OR ");
		}
		sql.append(')');
	}
	
	public void visit(And and)
	{
		sql.append('(');
		BooleanExpression[] exp = and.getExpressions();
		for( int i = 0; i < exp.length; i++ ) {
			exp[i].accept(this);
			if( i < exp.length - 1)
				sql.append(" AND ");
		}
		sql.append(')');
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.Exists)
	 */
	public void visit(Exists exists)
	{
		sql.append("EXISTS (");
		exists.getSubQuery().accept(this);
		sql.append(')');
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.In)
	 */
	public void visit(In in)
	{
		in.getExpr().accept(this);
		sql.append(" IN (");
		if (in.getSubQuery() != null)
			in.getSubQuery().accept(this);
		else
			sql.append(in.getValues());
		sql.append(')');
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.IsNull)
	 */
	public void visit(IsNull isNull)
	{
		isNull.getExpr().accept(this);
		sql.append(" IS NULL");
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.Like)
	 */
	public void visit(Like like)
	{
		like.getArgument().accept(this);
		sql.append(" LIKE ");
		like.getPattern().accept(this);
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.Literal)
	 */
	public void visit(Literal literal)
	{
		sql.append(literal.getValue());
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.Length)
	 */
	public void visit(Length length)
	{
		throw new UnsupportedOperationException();
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.Sum)
	 */
	public void visit(Sum sum)
	{
		sql.append("SUM(");
		sum.getExpr().accept(this);
		sql.append(')');
	}
	
	public void visit(Not not)
	{
		sql.append("NOT(");
		not.getExpr().accept(this);
		sql.append(')');
	}

	/** @deprecated */
	public static void setGeneratorClass(Class generatorClass)
	{
		if (!SqlGenerator.class.isAssignableFrom(generatorClass))
			throw new Error("Invalid SqlGenerator class");
		
		SqlGenerator.generatorClass = generatorClass;
	}
	
	/*public static SqlGenerator createInstance()
	{
		if (generatorClass == null)
			throw new Error("SqlGenerator derived class was undefined");
		
		try
		{
			return (SqlGenerator) generatorClass.newInstance();
		}
		catch (Exception e)
		{
			throw new Error(e);
		}
	}*/
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.InsertQuery)
	 */
	public void visit(InsertQuery query)
	{
		sql.append("INSERT INTO ");
		sql.append(query.getTable());
		sql.append("(");
		
		Map values = query.getValues();
		for (Iterator valueIt = values.entrySet().iterator(); valueIt.hasNext();)
		{
			Map.Entry entry = (Map.Entry) valueIt.next();
			String column = (String) entry.getKey();
			Argument value = (Argument) entry.getValue();
			
			sql.append(column);
			if (valueIt.hasNext())
				sql.append(", ");
			if (args != null)
				args.append(value);
		}
		
		sql.append(") VALUES (");
		int argCount = values.size();
		for (int i = 1; i <= argCount; i++)
		{
			sql.append("?");
			if (i < argCount)
				sql.append(", ");
		}
		sql.append(")");
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.UpdateQuery)
	 */
	public void visit(UpdateQuery query)
	{
		tables.setAlias(new Table(query.getTable()));
		
		sql.append("UPDATE ");
		sql.append(query.getTable());
		sql.append(" SET ");
		
		
		Map values = query.getValues();
		for (Iterator valueIt = values.entrySet().iterator(); valueIt.hasNext();)
		{
			Map.Entry entry = (Map.Entry) valueIt.next();
			String column = (String) entry.getKey();
			Argument value = (Argument) entry.getValue();
			
			sql.append(column);
			sql.append('=');
			value.accept(this);
			
			if (valueIt.hasNext())
				sql.append(", ");
		}
		
		if (query.getFilters() != null)
		{
			sql.append(" WHERE ");
			for (Iterator filterIt = query.getFilters().iterator(); filterIt.hasNext();)
			{
				BooleanExpression filter = (BooleanExpression) filterIt.next();
				filter.accept(this);
				if (filterIt.hasNext())
					sql.append(" AND ");
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.DeleteQuery)
	 */
	public void visit(DeleteQuery query)
	{
		tables.setAlias(new Table(query.getTable()));
		
		sql.append("DELETE FROM ");
		sql.append(query.getTable());
		//sql.append(" " + tables.getAlias(query.getTable()));
		
		if (query.getFilters() != null)
		{
			sql.append(" WHERE ");
			for (Iterator filterIt = query.getFilters().iterator(); filterIt.hasNext();)
			{
				BooleanExpression filter = (BooleanExpression) filterIt.next();
				filter.accept(this);
				if (filterIt.hasNext())
					sql.append(" AND ");
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.BatchQuery)
	 */
	public void visit(BatchQuery batch)
	{
		Iterator queryIt = batch.iterateQueries();
		while (queryIt.hasNext())
		{
			Query query = (Query) queryIt.next();
			query.accept(this);
			
			if (queryIt.hasNext())
				sql.append(";");
		}
	}
}