package com.codeglide.core.rte;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.codeglide.core.Logger;

public class ReceivedFile extends InputStream {
	InputStream stream = null;
	String name;
	long size;
	String contentType;
	File binPath;

	public ReceivedFile(File binPath) {
		this.binPath = binPath;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public File getFile() {
		return binPath;
	}
	public void setFile(File file) {
		this.binPath = file;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public void open() throws IOException {
		stream = new BufferedInputStream(new FileInputStream(binPath));
	}
	
	public void delete() {
		try {
			if( stream != null )
				stream.close();
			binPath.delete();
		} catch (IOException e) {
			Logger.debug(e);
		}
	}

	public int read() throws IOException {
		if( stream == null )
			open();
		int c = stream.read();
		if( c == -1 ) {
			stream.close();
			stream = null;
		}
		return c;
	}
	
	public synchronized int read(byte b[], int off, int len) throws IOException {
		if( stream == null )
			open();
		return stream.read(b,off,len);
	}

	public synchronized long skip(long n) throws IOException {
		return stream.skip(n);
	}

	public synchronized int available() throws IOException {
		return stream.available();
	}

	public synchronized void reset() throws IOException {
		if( stream != null ) {
			stream.close();
			stream = null;
		}
	}

	public synchronized void close() throws IOException {
		reset();
	}
	
	public boolean markSupported() {
		return true;
	}

	
	
}
