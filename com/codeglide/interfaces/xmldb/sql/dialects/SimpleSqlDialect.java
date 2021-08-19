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
package com.codeglide.interfaces.xmldb.sql.dialects;

import java.util.Iterator;

import com.codeglide.interfaces.xmldb.sql.*;
import com.codeglide.interfaces.xmldb.sql.SelectQuery.SelectedColumn;
import com.codeglide.interfaces.xmldb.sql.SelectQuery.Sorting;
import com.codeglide.interfaces.xmldb.sql.SelectQuery.TableSource;
import com.codeglide.interfaces.xmldb.sql.schema.ColumnDefinition;
import com.codeglide.interfaces.xmldb.sql.schema.IndexDefinition;
import com.codeglide.interfaces.xmldb.sql.schema.TableDefinition;


/**
 * @author jwajnerman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SimpleSqlDialect extends SqlDialect
{
	/* (non-Javadoc)
	 * @see com.mintersoft.sql.SqlDialect#createGenerator()
	 */
	public SqlGenerator createGenerator()
	{
		return new SimpleSqlGenerator();
	}

	/* (non-Javadoc)
	 * @see com.mintersoft.sql.SqlDialect#generateTableStatements(com.mintersoft.sql.schema.TableDefinition)
	 */
	public String[] generateTableStatements(TableDefinition table)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("CREATE TABLE " + table.getName() + "(");
		
		for (Iterator colIt = table.getColumns().iterator(); colIt.hasNext();)
		{
			ColumnDefinition col = (ColumnDefinition) colIt.next();
			buffer.append(col.getName());
			buffer.append(' ');
			switch (col.getType())
			{
				case ColumnDefinition.TYPE_INT:
					buffer.append("integer");
					break;
				
				case ColumnDefinition.TYPE_BIGINT:
					buffer.append("bigint");
					break;
				
				case ColumnDefinition.TYPE_VARCHAR:
				case ColumnDefinition.TYPE_NVARCHAR:
					buffer.append("varchar(" + col.getSize() + ")");
					break;
				
				case ColumnDefinition.TYPE_BINARY:
					int size = col.getSize();
					if (size == 0)
						size = 64 * 1024;
					buffer.append("binary(" + size + ")");
					break;
				
				case ColumnDefinition.TYPE_TEXT:
				case ColumnDefinition.TYPE_NTEXT:
					buffer.append("varchar(65536)");
					break;
			}
			
			buffer.append(' ');
			buffer.append(col.isNull() ? "NULL" : "NOT NULL");
			
			if (col.isPrimaryKey())
				buffer.append(" PRIMARY KEY");
			
			if (colIt.hasNext())
				buffer.append(", ");
		}
		
		
		int indexId = 0;
		for (Iterator indexIt = table.getIndexes().iterator(); indexIt.hasNext();)
		{
			IndexDefinition	index = (IndexDefinition) indexIt.next();
			buffer.append(", ");
			buffer.append("CONSTRAINT " + table.getName() + "_IX" + (++indexId) + " INDEX (");
			
			String[] columns = index.getColumns();
			for (int i = 0; i < columns.length; i++)
			{
				buffer.append(columns[i]);
				if (i < columns.length - 1)
					buffer.append(", ");
			}
			
			buffer.append(")");
		}
		
		buffer.append(")");
		return new String[] { buffer.toString() };
	}

	private class SimpleSqlGenerator extends SqlGenerator
	{
		/* (non-Javadoc)
		 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.TextToInt)
		 */
		public void visit(TextToInt textToInt)
		{
			throw new UnsupportedOperationException();
		}

		/* (non-Javadoc)
		 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.TextToVarchar)
		 */
		public void visit(TextToVarchar textToVarchar)
		{
			throw new UnsupportedOperationException();
		}

		/* (non-Javadoc)
		 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.VarcharToInt)
		 */
		public void visit(VarcharToInt varcharToInt)
		{
			throw new UnsupportedOperationException();
		}
		
		/* (non-Javadoc)
		 * @see com.mintersoft.sql.SqlGenerator#visit(com.mintersoft.sql.Length)
		 */
		public void visit(Length length)
		{
			sql.append("LENGTH(");
			length.getExpression().accept(this);
			sql.append(")");
		}

		public void visit(SelectQuery query)
		{
			for (Iterator tablesIt = query.getTableSources().iterator(); tablesIt.hasNext();)
			{
				TableSource tableSrc = (TableSource) tablesIt.next();
				tables.setAlias(tableSrc.getTable());
			}
			
			sql.append("SELECT ");
			
			appendColumns(query);
			sql.append(' ');
			sql.append("FROM ");
			appendTables(query);
			
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
			
			if (query.getSortings() != null)
			{
				sql.append(" ORDER BY ");
				for (Iterator sortingIt = query.getSortings().iterator(); sortingIt.hasNext();)
				{
					Sorting sort = (Sorting) sortingIt.next();
					if (sort.getExpr() instanceof SelectQuery)
						sql.append("(");
					sort.getExpr().accept(this);
					if (sort.getExpr() instanceof SelectQuery)
						sql.append(")");
					if (sort.isDesc())
						sql.append(" DESC");
					if (sortingIt.hasNext())
						sql.append(", ");
				}
			}
		}
		
		private void appendColumns(SelectQuery query)
		{
			for (Iterator colIt = query.getColumns().iterator(); colIt.hasNext();)
			{
				SelectedColumn col = (SelectedColumn) colIt.next();
				col.getExpression().accept(this);
				
				String alias = col.getAlias();
				if (alias != null)
					sql.append(" AS ").append(alias);
				else if (col.getExpression() instanceof Column)
					sql.append(" AS ").append(((Column) col.getExpression()).getName());
				
				if (colIt.hasNext())
					sql.append(", ");
			}
		}
		
		private void appendTables(SelectQuery query)
		{
			for (int i = 0; i < query.getTableSources().size(); i++)
			{
				TableSource tableSrc = (TableSource) query.getTableSources().get(i);
				BooleanExpression joinCondition = tableSrc.getJoinCondition();
				
				if (i > 0)
				{
					switch (tableSrc.getJoinType())
					{
						case SelectQuery.INNER_JOIN:
							if (joinCondition == null)
								sql.append(", ");
							else
								sql.append(" INNER JOIN ");
							break;
						
						case SelectQuery.LEFT_OUTER_JOIN:
							sql.append(" LEFT OUTER JOIN ");
							break;
						
						case SelectQuery.RIGHT_OUTER_JOIN:
							sql.append(" RIGHT OUTER JOIN ");
							break;
						
						case SelectQuery.FULL_OUTER_JOIN:
							sql.append(" FULL OUTER JOIN ");
							break;
					}
				}
				
				Table table = tableSrc.getTable();
				String alias = tables.getAlias(table);
				sql.append(table.getName() + " AS " + alias);
				
				
				
				if (i > 0 && joinCondition != null)
				{
					sql.append(" ON ");
					joinCondition.accept(this);
				}
			}
		}
	}
}
