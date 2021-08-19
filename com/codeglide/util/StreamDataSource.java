package com.codeglide.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public class StreamDataSource implements DataSource {
	
	final private String name;
	final private InputStream stream;
	final private String contentType;

	//Constructors
	//-All arguments
	public StreamDataSource(String name,InputStream stream,String contentType) {
		this.name = name;
		this.stream = stream;
		this.contentType = contentType;
	}
	
	//-Just one stream and no name
	public StreamDataSource(InputStream stream,String contentType) {
		this("-unknown-",stream,contentType);
	}

	//Methods defined by interface
	public String getContentType() {
		return contentType;
	}

	public InputStream getInputStream() throws IOException {
		stream.reset();
		return stream;
	}

	public String getName() {
		return name;
	}

	public OutputStream getOutputStream() throws IOException {
		throw new IOException("OutputStream not defined");
	}
}
