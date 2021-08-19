/*
 * 	Copyright (C) 2007, CodeGlide - Entwickler, S.A.
 *	All rights reserved.
 *
 *	You may not distribute this software, in whole or in part, without
 *	the express consent of the author.
 *
 *	There is no warranty or other guarantee of fitness of this software
 *	for any purpose.  It is provided solely "as is".
 *
 */

package com.codeglide.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import com.codeglide.core.rte.engines.Response;

public class DavResponse extends Response {
	//Class for representing all aspects of status codes
	static public class Status {
		//Constants
		public static final Status OK = new Status(200,"OK");
		public static final Status CREATED = new Status(201,"Created");
		public static final Status NO_CONTENT = new Status(204,"No Content");
		public static final Status MULTI_STATUS = new Status(207,"Multi-Status");
		public static final Status UNAUTHORIZED = new Status(401,"Unauthorized");
		public static final Status FORBIDDEN = new Status(403,"Forbidden");
		public static final Status NOT_FOUND = new Status(404,"Not Found");
		public static final Status METHOD_NOT_ALLOWED = new Status(405,"Method Not Allowed");
		public static final Status CONFLICT = new Status(409,"Conflict");
		public static final Status PRECONDITION_FAILED = new Status(412,"Precondition Failed");
		public static final Status LOCKED = new Status(423,"Locked");
		public static final Status INTERNAL_SERVER_ERROR = new Status(500,"Internal Server Error");
		//Attributes
		final private int code;
		final private String description;
		//Constructor
		public Status(int code,String description) {
			this.code = code;
			this.description = description;
		}
		//Getters
		public int getCode() {
			return code;
		}
		public String getDescription() {
			return description;
		}
		public String getTag() {
			return "HTTP/1.1 " + code + " " + description;
		}
	}
	
	//Adapters for sources
	static private abstract class Source {
		abstract public void printOn(HttpServletResponse response) throws IOException;
	}
	
	static private class StringSource extends Source {
		String string;
		public StringSource(String string) {
			this.string = string;
		}
		public void printOn(HttpServletResponse response) throws IOException {
			response.getWriter().print(string);
		}
	}
	
	static private class StreamSource extends Source {
		InputStream stream;
		public StreamSource(InputStream stream) {
			this.stream = stream;
		}
		public void printOn(HttpServletResponse response) throws IOException {
			//Transfer all data from file's stream to response's stream
			if (stream != null) {
				byte[] buffer = new byte[1000];
				while (true) {
					int size = stream.read(buffer);
					if (size <= 0)
						return;
					else
						response.getOutputStream().write(buffer);
				}
			}
		}
	}
	
	//Attributes
	final private Status status;
	final private List<Source> contents = new LinkedList<Source>();
	final private Map<String,String> headers = new HashMap<String,String>();
	
	//Constructor
	public DavResponse(Status status) {
		this.status = status;
		this.initialize();
	}
	
	public DavResponse(Status status,InputStream stream) {
		this.status = status;
		this.addContent(stream);
		this.initialize();
	}
	
	public DavResponse(Status status,String string) {
		this.status = status;
		this.addContent(string);
		this.initialize();
	}
	
	private void initialize() {
		headers.put("MS-Author-Via","DAV");
		headers.put("DAV","2");
		headers.put("Content-type","text/xml; charset=\"utf-8\"");
	}
	
	//Content management
	public void addContent(InputStream content) {
		contents.add(new StreamSource(content));
	}
	
	public void addContent(String content) {
		contents.add(new StringSource(content));
	}
	
	//Header management
	public void addHeader(String header,String value) {
		headers.put(header,value);
	}
	
	public void send(HttpServletResponse response) throws IOException {
		response.setStatus(status.getCode(),status.getDescription());
		for(Source content : contents)
			content.printOn(response);
		for(Entry<String,String> header : headers.entrySet())
			response.addHeader(header.getKey(),header.getValue());
	}
}
