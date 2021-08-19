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

/**
 * @author jwajnerman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SelectQuery extends Query
{
	SelectQuery(SqlDialect dialect)
	{
		super(dialect);
	}

	public static final int INNER_JOIN = 1;
	public static final int LEFT_OUTER_JOIN = 2;
	public static final int RIGHT_OUTER_JOIN = 3;
	public static final int FULL_OUTER_JOIN = 4;
	
	private ArrayList tableSources = new ArrayList();
	private ArrayList columns = new ArrayList();
	private ArrayList filters;
	private ArrayList sortings;
	private int top = 0, offset = -1;
	
	public ArrayList getTableSources()
	{
		return tableSources;
	}
	
	public ArrayList getColumns()
	{
		return columns;
	}
	
	public ArrayList getFilters()
	{
		return filters;
	}
	
	public ArrayList getSortings()
	{
		return sortings;
	}
	
	public int getTop()
	{
		return top;
	}
	
	public int getOffset()
	{
		return offset;
	}
	
	public void addTable(Table table)
	{
		addTable(table, INNER_JOIN);
	}
	
	public void addTable(String tableName)
	{
		addTable(new Table(tableName));
	}
	
	public void addTable(Table table, int joinType)
	{
		addTable(table, joinType, null);
	}
	
	public void addTable(String tableName, int joinType)
	{
		addTable(new Table(tableName), joinType);
	}
	
	public void addTable(Table table, int joinType, BooleanExpression joinCondition)
	{
		tableSources.add(new TableSource(table, joinType, joinCondition));
	}
	
	public void addTable(String tableName, int joinType, BooleanExpression joinCondition)
	{
		addTable(new Table(tableName), joinType, joinCondition);
	}
	
	public void addColumn(Expression expr)
	{
		addColumn(expr, null);
	}
	
	public void addColumn(Expression expr, String alias)
	{
		columns.add(new SelectedColumn(expr, alias));
	}
	
	public void addColumn(Table table, String colName)
	{
		addColumn(table, colName, null);
	}
	
	public void addColumn(Table table, String colName, String alias)
	{
		addColumn(new Column(table, colName), alias);
	}
	
	public void addColumn(String colName)
	{
		if (tableSources.size() != 1)
			throw new IllegalStateException("This overload can only be used with single table defined");
		
		addColumn(((TableSource) tableSources.get(0)).getTable(), colName);
	}
	
	public void addColumn(String tableName, String colName)
	{
		addColumn(new Column(tableName, colName));
	}
	
	public void addColumns(String tableName, String[] colNames)
	{
		for (int i = 0; i < colNames.length; i++)
			addColumn(tableName, colNames[i]);
	}
	
	public void addColumns(Table table, String[] colNames)
	{
		for (int i = 0; i < colNames.length; i++)
			addColumn(table, colNames[i]);
	}
	
	public void addFilter(BooleanExpression filter)
	{
		if (filters == null)
			 filters = new ArrayList();
		filters.add(filter);
	}
	
	public void addSorting(Expression order)
	{
		addSorting(order, false);
	}
	
	public void addSorting(Expression order, boolean desc)
	{
		if (sortings == null)
			sortings = new ArrayList();
		sortings.add(new Sorting(order, desc));
	}
	
	public void setTop(int n)
	{
		this.top = n;
	}
	
	public void setOffset(int n)
	{
		this.offset = n;
	}
	
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.Expression#accept(com.mintersoft.sql.ExpressionVisitor)
	 */
	public void accept(ExpressionVisitor v)
	{
		v.visit(this);
	}
	
	public class SelectedColumn
	{
		private final Expression expr;
		private final String alias;

		public SelectedColumn(Expression expr, String alias)
		{
			this.expr = expr;
			this.alias = alias;
		}
		
		public Expression getExpression()
		{
			return expr;
		}
		
		public String getAlias()
		{
			return alias;
		}
	}
	
	public class TableSource
	{
		private Table table;
		private int joinType;
		private BooleanExpression joinCondition;
		
		public TableSource(Table table, int joinType, BooleanExpression joinCondition)
		{
			this.table = table;
			this.joinType = joinType;
			this.joinCondition = joinCondition;
		}
		
		public Table getTable()
		{
			return table;
		}
		
		public int getJoinType()
		{
			return joinType;
		}
		
		public BooleanExpression getJoinCondition()
		{
			return joinCondition;
		}
	}

	public class Sorting
	{
		private final Expression expr;
		private final boolean desc;

		public Sorting(Expression expr, boolean desc)
		{
			this.expr = expr;
			this.desc = desc;
		}

		
		/**
		 * @return Returns the desc.
		 */
		public boolean isDesc()
		{
			return desc;
		}
		/**
		 * @return Returns the expr.
		 */
		public Expression getExpr()
		{
			return expr;
		}
	}
}
