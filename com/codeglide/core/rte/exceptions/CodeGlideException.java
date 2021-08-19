package com.codeglide.core.rte.exceptions;

import com.codeglide.core.rte.interfaces.Runnable;

public abstract class CodeGlideException extends Exception {
	protected String message = null;
	protected Runnable causedBy = null;
	
	private static final long serialVersionUID = 6496927444867307022L;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Runnable getCausedBy() {
		return causedBy;
	}

	public void setCausedBy(Runnable causedBy) {
		this.causedBy = causedBy;
	}
	
}
