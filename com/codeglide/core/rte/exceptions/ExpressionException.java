package com.codeglide.core.rte.exceptions;

import com.codeglide.core.Expression;

public class ExpressionException extends CodeGlideException {
	private Expression expression = null;
	
	private static final long serialVersionUID = 1L;

	public ExpressionException(String message, Expression expression) {
		this.message = message;
		this.expression = expression;
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	

}
