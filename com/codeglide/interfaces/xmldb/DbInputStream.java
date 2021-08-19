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
package com.codeglide.interfaces.xmldb;

import java.io.IOException;
import java.io.InputStream;

public abstract class DbInputStream extends InputStream {
	public final static int TYPE_SQL = 0;
	public final static int TYPE_FLATFILE = 1;
	
	protected InputStream stream = null;
	protected long streamId = -1, nodeId = -1;
	protected String appName = null;
	
	public DbInputStream() {
	}
	
	public DbInputStream( String appName, long nodeId, long streamId ) {
		this.appName = appName;
		this.nodeId = nodeId;
		this.streamId = streamId;
	}
	
	public abstract void insert( InputStream stream );
	public abstract void delete() throws IOException;
	public abstract void open() throws IOException;
	public abstract void setSize(long size);
	public abstract long getSize();
	
	public static DbInputStream getDbInputStreamInstance( int type, String appName, long nodeId, long streamId ) {
		if( type == TYPE_SQL )
			return new SqlDbInputStream(appName, nodeId, streamId);
		else if( type == TYPE_FLATFILE )
			return new FileDbInputStream(appName, nodeId, streamId);
		else
			return null;
	}
	
	public long getId() {
		return streamId;
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
