package com.codeglide.core.rte.engines;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public abstract class Response {
	
	public abstract void send(HttpServletResponse response) throws IOException;

}
