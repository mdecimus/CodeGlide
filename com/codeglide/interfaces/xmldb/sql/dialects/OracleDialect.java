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
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class OracleDialect extends SqlDialect
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mintersoft.sql.SqlDialect#createGenerator()
	 */
	public SqlGenerator createGenerator()
	{
		return new OracleSqlGenerator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mintersoft.sql.SqlDialect#generateTableStatements(com.mintersoft.sql.schema.TableDefinition)
	 */
	public String[] generateTableStatements(TableDefinition table)
	{
		ArrayList statements = new ArrayList();
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("CREATE TABLE ").append(table.getName()).append(" (");
		boolean hasAutonumeric = false;
		
		for (Iterator colIt = table.getColumns().iterator(); colIt.hasNext();)
		{
			ColumnDefinition col = (ColumnDefinition) colIt.next();
			buffer.append(col.getName()).append(' ');
			
			switch (col.getType())
			{
				case ColumnDefinition.TYPE_INT:
					buffer.append("INTEGER");
					break;
				
				case ColumnDefinition.TYPE_BIGINT:
					buffer.append("NUMBER");
					break;
				
				case ColumnDefinition.TYPE_VARCHAR:
				case ColumnDefinition.TYPE_NVARCHAR:
					buffer.append("VARCHAR2(").append(col.getSize()).append(")");
					break;
				
				case ColumnDefinition.TYPE_TEXT:
				case ColumnDefinition.TYPE_NTEXT:
					buffer.append("VARCHAR2(4000)");
					break;
				
				case ColumnDefinition.TYPE_BINARY:
					buffer.append("BLOB");
					break;
			}
			
			buffer.append(" ");
			
			if (col.isPrimaryKey())
				buffer.append("PRIMARY KEY");
			else
				buffer.append(col.isNull() ? "NULL" : "NOT NULL");
			
			if (col.isAutonumeric())
				hasAutonumeric = true;
			
			if (colIt.hasNext())
				buffer.append(", ");
		}
		
		buffer.append(")");
		statements.add(buffer.toString());
		
		if (hasAutonumeric)
			statements.add("CREATE SEQUENCE " + table.getName() + "_ID INCREMENT BY 1 START WITH 1");
		
		int indexId = 0;
		for (Iterator indexIt = table.getIndexes().iterator(); indexIt.hasNext();)
		{
			IndexDefinition index = (IndexDefinition) indexIt.next();
			String[] columns = index.getColumns();
			buffer = new StringBuffer();
			buffer.append("CREATE INDEX " + table.getName() + "_IX" + (++indexId) + " ON " + table.getName() + "(");
			
			for (int i = 0; i < columns.length; i++)
			{
				buffer.append(columns[i]);
				if (i < columns.length - 1)
					buffer.append(", ");
			}
			
			buffer.append(")");
			statements.add(buffer.toString());
		}
		
		return (String[]) statements.toArray(new String[statements.size()]);
	}

	private class OracleSqlGenerator extends SqlGenerator
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.TextToInt)
		 */
		public void visit(TextToInt textToInt)
		{
			sql.append("CAST (");
			textToInt.getTextExpression().accept(this);
			sql.append(" AS NUMBER)");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.TextToVarchar)
		 */
		public void visit(TextToVarchar textToVarchar)
		{
			textToVarchar.getTextExpression().accept(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.VarcharToInt)
		 */
		public void visit(VarcharToInt varcharToInt)
		{
			sql.append("CAST (");
			varcharToInt.getVarcharExpr().accept(this);
			sql.append(" AS NUMBER)");
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

		/**
		 * @param join
		 */
		public void visit(OuterJoin join)
		{
			join.getLeft().accept(this);
			if (join.isLeft()) sql.append("(+)");
			sql.append("=");
			join.getRight().accept(this);
			if (!join.isLeft()) sql.append("(+)");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.mintersoft.sql.ExpressionVisitor#visit(com.mintersoft.sql.SelectQuery)
		 */
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
					sql.append(' ').append(alias);

				if (colIt.hasNext())
					sql.append(", ");
			}
		}

		private void appendTables(SelectQuery query)
		{
			for (Iterator tableIt = query.getTableSources().iterator(); tableIt.hasNext();)
			{
				TableSource tableSrc = (TableSource) tableIt.next();

				Table table = tableSrc.getTable();
				String alias = tables.getAlias(table);
				sql.append(table.getName() + " " + alias);

				if (tableIt.hasNext())
					sql.append(", ");

				BooleanExpression joinCondition = tableSrc.getJoinCondition();
				boolean isLeft = false;
				if (joinCondition != null)
				{
					switch (tableSrc.getJoinType())
					{
						case SelectQuery.INNER_JOIN:
							query.addFilter(joinCondition);
							break;

						case SelectQuery.LEFT_OUTER_JOIN:
							isLeft = true;
						case SelectQuery.RIGHT_OUTER_JOIN:
							if (!(joinCondition instanceof Equals))
								throw new UnsupportedOperationException("Outer joins only supported for equal condition");

							Equals equal = (Equals) joinCondition;
							query.addFilter(new OuterJoin(equal.getLeft(), equal.getRight(), isLeft));
							break;
						
						case SelectQuery.FULL_OUTER_JOIN:
							throw new UnsupportedOperationException("Full outer joins not supported");
					}
				}
			}
		}
	}

	private class OuterJoin extends BooleanExpression
	{
		private final Expression left;
		private final Expression right;
		private final boolean isLeft;

		public OuterJoin(Expression left, Expression right, boolean isLeft)
		{
			this.left = left;
			this.right = right;
			this.isLeft = isLeft;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.mintersoft.sql.Expression#accept(com.mintersoft.sql.ExpressionVisitor)
		 */
		public void accept(ExpressionVisitor v)
		{
			if (!(v instanceof OracleSqlGenerator))
			{
				throw new Error("This expression is only valid for oracle");
			}

			((OracleSqlGenerator) v).visit(this);
		}

		public boolean isLeft()
		{
			return isLeft;
		}

		public Expression getLeft()
		{
			return left;
		}

		public Expression getRight()
		{
			return right;
		}
	}
}