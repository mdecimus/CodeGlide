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

import java.util.ArrayList;
import java.util.Iterator;

import com.codeglide.interfaces.xmldb.sql.BooleanExpression;
import com.codeglide.interfaces.xmldb.sql.Length;
import com.codeglide.interfaces.xmldb.sql.SelectQuery;
import com.codeglide.interfaces.xmldb.sql.SqlDialect;
import com.codeglide.interfaces.xmldb.sql.SqlGenerator;
import com.codeglide.interfaces.xmldb.sql.Table;
import com.codeglide.interfaces.xmldb.sql.TextToInt;
import com.codeglide.interfaces.xmldb.sql.TextToVarchar;
import com.codeglide.interfaces.xmldb.sql.VarcharToInt;
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
public class SqlServerDialect extends SqlDialect
{

	/* (non-Javadoc)
	 * @see com.mintersoft.sql.SqlDialect#createGenerator()
	 */
	public SqlGenerator createGenerator()
	{
		return new SqlServerSqlGenerator();
	}

	/* (non-Javadoc)
	 * @see com.mintersoft.sql.SqlDialect#generateTableStatements(com.mintersoft.sql.schema.TableDefinition)
	 */
	public String[] generateTableStatements(TableDefinition table)
	{
		ArrayList statements = new ArrayList();
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("CREATE TABLE " + table.getName() + " (");
		
		for (Iterator colIt = table.getColumns().iterator(); colIt.hasNext();)
		{
			ColumnDefinition col = (ColumnDefinition) colIt.next();
			buffer.append(col.getName()).append(' ');
			
			switch (col.getType())
			{
				case ColumnDefinition.TYPE_INT:
					buffer.append("int");
					break;
				
				case ColumnDefinition.TYPE_BIGINT:
					buffer.append("bigint");
					break;
				
				case ColumnDefinition.TYPE_VARCHAR:
					buffer.append("varchar(").append(col.getSize()).append(")");
					break;
				
				case ColumnDefinition.TYPE_NVARCHAR:
					buffer.append("nvarchar(").append(col.getSize()).append(")");
					break;
				
				case ColumnDefinition.TYPE_TEXT:
					buffer.append("text");
					break;
					
				case ColumnDefinition.TYPE_NTEXT:
					buffer.append("ntext");
					break;
				
				case ColumnDefinition.TYPE_BINARY:
					if (col.getSize() <= 8000 && col.getSize() > 0)
						buffer.append("varbinary(").append(col.getSize()).append(")");
					else
						buffer.append("image");
					break;
			}
			
			buffer.append(' ');
			
			if (col.isAutonumeric())
				buffer.append("IDENTITY(1,1) ");
			
			if (col.isPrimaryKey())
				buffer.append("PRIMARY KEY");
			else
				buffer.append(col.isNull() ? "NULL" : "NOT NULL");
			
			if (colIt.hasNext())
				buffer.append(", ");
		}
		
		buffer.append(")");
		statements.add(buffer.toString());
		
		int indexId = 0;
		for (Iterator indexIt = table.getIndexes().iterator(); indexIt.hasNext();)
		{
			IndexDefinition index = (IndexDefinition) indexIt.next();
			String[] columns = index.getColumns();
			buffer = new StringBuffer();
			buffer.append("CREATE INDEX " + table.getName() + "_IX" + (++indexId) + " ON " + table.getName() + "(");
			
			boolean hasColumns = false;
			
			for (int i = 0; i < columns.length; i++)
			{
				ColumnDefinition column = table.getColumn(columns[i]);
				if (column != null && column.getType() != ColumnDefinition.TYPE_TEXT && column.getType() != ColumnDefinition.TYPE_NTEXT ) {
					if (hasColumns)
						buffer.append(", ");
					buffer.append(columns[i]);
					hasColumns = true;
				}
			}
			
			buffer.append(")");
			if( hasColumns )
				statements.add(buffer.toString());
		}
		
		return (String[]) statements.toArray(new String[statements.size()]);
	}
	
	private class SqlServerSqlGenerator extends SqlGenerator
	{
		public void visit(TextToInt textToInt)
		{
			sql.append("CONVERT(INT, CONVERT(VARCHAR, ");
			textToInt.getTextExpression().accept(this);
			sql.append("))");
		}

		public void visit(TextToVarchar textToVarchar)
		{
			sql.append("CONVERT(VARCHAR, ");
			textToVarchar.getTextExpression().accept(this);
			sql.append(')');
		}

		public void visit(VarcharToInt varcharToInt)
		{
			sql.append("CONVERT(INT, ");
			varcharToInt.getVarcharExpr().accept(this);
			sql.append(')');
		}
		
		/* (non-Javadoc)
		 * @see com.mintersoft.sql.SqlGenerator#visit(com.mintersoft.sql.Length)
		 */
		public void visit(Length length)
		{
			sql.append("LEN(");
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
			
			if (query.getTop() > 0)
				sql.append("TOP " + query.getTop() + " ");
			
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
					sql.append(' ').append(alias);
				
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
