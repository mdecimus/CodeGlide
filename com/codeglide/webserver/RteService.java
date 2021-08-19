package com.codeglide.webserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codeglide.core.rte.engines.WebServiceRenderer;

public class RteService extends HttpServlet {
	private static final long serialVersionUID = -3562712052975418930L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		WebServiceRenderer engine = new WebServiceRenderer();
		engine.handleRequest(req, resp);
	}
}
