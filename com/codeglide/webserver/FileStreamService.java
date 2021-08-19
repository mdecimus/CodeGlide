package com.codeglide.webserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codeglide.core.rte.engines.FileStreamEngine;

public class FileStreamService extends HttpServlet {

	private static final long serialVersionUID = -3562712052973418730L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		FileStreamEngine engine = new FileStreamEngine();
		engine.handleRequest(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	
}
