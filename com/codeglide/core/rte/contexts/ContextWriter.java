package com.codeglide.core.rte.contexts;

import java.io.Writer;

import com.codeglide.core.rte.session.Session;

public class ContextWriter extends ContextUi {
	private Writer writer;

	public ContextWriter(Session session) {
		super(session);
	}

	public Writer getWriter() {
		return writer;
	}

	public void setWriter(Writer writer) {
		this.writer = writer;
	}
	
}
