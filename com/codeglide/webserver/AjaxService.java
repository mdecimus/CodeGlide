package com.codeglide.webserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codeglide.core.rte.engines.AjaxRenderer;

public class AjaxService extends HttpServlet {

	private static final long serialVersionUID = -3562712052973418930L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		AjaxRenderer engine = new AjaxRenderer();
		engine.handleRequest(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	
}
