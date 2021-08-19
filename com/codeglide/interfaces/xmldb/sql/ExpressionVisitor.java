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

/**
 * TODO Enter class description
 */
public interface ExpressionVisitor
{
	void visit(Argument argument);
	void visit(Column column);
	void visit(Count count);
	void visit(Length length);
	void visit(Equals equals);
	void visit(Exists exists);
	void visit(GreaterOrLess gtl);
	void visit(In in);
	void visit(IsNull null1);
	void visit(Like like);
	void visit(Literal literal);
	void visit(Sum sum);
	void visit(TextToInt textToInt);
	void visit(TextToVarchar textToVarchar);
	void visit(VarcharToInt varcharToInt);
	void visit(SelectQuery query);
	void visit(InsertQuery query);
	void visit(BatchQuery query);
	void visit(UpdateQuery query);
	void visit(DeleteQuery query);
	void visit(Or or);
	void visit(And and);
	void visit(Not not);
	void visit(RawExpression raw);
}
