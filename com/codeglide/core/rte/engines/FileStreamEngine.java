package com.codeglide.core.rte.engines;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.session.Session;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;

public class FileStreamEngine extends Engine {
	public final static String FILESTREAM_URL = "/FileStream/";
	
	private String fileName = null, fileType = null;
	private int fileSize = -1;
	private Object fileBin = null;

	public FileStreamEngine() {
		this.response = new FileResponse();
	}
	
	protected void handleRequest() throws Exception {
		
		// Obtain fileNode
		String requestPath[] = servletRequest.getServletPath().split("\\/");
		DynamicElement fileNode = getElementById(requestPath[requestPath.length-1]);
		
		// Set file name
		fileName = fileNode.getAttribute("Name");
		
		// Set file type
		fileType = fileNode.getAttribute("Type");
		
		// Set file size
		try {
			fileSize = Integer.parseInt(fileNode.getAttribute("Size"));
		} catch (Exception _) {fileSize = -1;}
		
		// Set binary object
		try {
			fileBin = ((DynamicAttr)fileNode.getAttributeNode("Bin")).getObjectValue();
		} catch (Exception _) {}

		if( fileName == null || fileName.isEmpty() )
			fileName = "file.bin";
		if( fileType == null || fileType.indexOf("/") == -1 )
			fileType = "application/octet-stream";
	}

	protected DynamicElement getElementById( String id ) {
		return (DynamicElement)((ContextUi)context).getWindowManager().getObject(Integer.parseInt(id, 36));
	}
	
	protected String getSessionId() {
		// Obtain session ID
		String sessionId = null;
		Cookie[] cookies = servletRequest.getCookies();
		if( cookies != null && cookies.length > 0 ) {
			for( int i = 0; i < cookies.length; i++ ) {
				if( cookies[i].getName().equals("ID") )
					sessionId = cookies[i].getValue();
			}
		}
		return sessionId;
	}
	
	protected void setSessionId(String sessionId) {
	}

	protected class FileResponse extends Response {

		public void send(HttpServletResponse response) throws IOException {
			response.setContentType(fileType);
			response.setHeader("Content-Disposition","attachment; filename="+fileName);
			if( fileBin != null ) {
				PrintWriter out = response.getWriter();
				if( fileBin instanceof InputStream ) {
					if( fileSize > 0 )
						response.setContentLength(fileSize);
					int c;
					while( (c = ((InputStream)fileBin).read()) != -1 )
						out.write(c);
					out.close();
				} else {
					response.setContentLength(((String)fileBin).length());
					out.write((String)fileBin);
				}
				out.close();
			} else
				response.setContentLength(0);
		}
		
	}

	protected void handleError(Throwable e) {
		
	}

	protected Session handleSessionNotFound(Application application)
			throws Exception {
		return null;
	}
	
}
