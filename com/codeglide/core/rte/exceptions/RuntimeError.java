package com.codeglide.core.rte.exceptions;

public class RuntimeError extends CodeGlideException {

	private static final long serialVersionUID = 1L;

	public RuntimeError(String message) {
		this.message = message;
	}
	
}
