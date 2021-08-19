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
 * @author admin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RawExpression extends BooleanExpression {
	private String query;
	
	public RawExpression( String query ) {
		this.query = query;
	}
	
	public void accept(ExpressionVisitor v) {
		v.visit(this);
	}

	public String getQuery() {
		return query;
	}
}
