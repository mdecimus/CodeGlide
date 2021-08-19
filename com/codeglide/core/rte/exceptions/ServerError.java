package com.codeglide.core.rte.exceptions;

public class ServerError extends CodeGlideException {
	private String title = null;
	private static final long serialVersionUID = 1L;

	public ServerError(String title, String message ) {
		this.message = message;
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
}
