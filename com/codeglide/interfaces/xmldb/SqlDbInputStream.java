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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.codeglide.core.ServerSettings;
import com.codeglide.core.Logger;
import com.codeglide.interfaces.xmldb.SqlConnectionPool.SqlConnection;
import com.codeglide.interfaces.xmldb.sql.Argument;
import com.codeglide.interfaces.xmldb.sql.Column;
import com.codeglide.interfaces.xmldb.sql.DeleteQuery;
import com.codeglide.interfaces.xmldb.sql.Equals;
import com.codeglide.interfaces.xmldb.sql.InsertQuery;
import com.codeglide.interfaces.xmldb.sql.SelectQuery;
import com.codeglide.interfaces.xmldb.sql.Table;

public class SqlDbInputStream extends DbInputStream {
	private long size = -1;

	public SqlDbInputStream(String appName, long nodeId, long streamId) {
		super(appName, nodeId, streamId);
	}
	
	public void insert( InputStream stream ) {
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlConnection conn = null;
		try {
			InputStreamCounter isc = new InputStreamCounter(stream);
			conn = db.getConnection();
			
			// Insert node and obtain id
			InsertQuery insert = db.getDialect().createInsert(appName+"_NODE_STREAMS");
			insert.setValue("STREAMID",streamId);
			insert.setValue("NODEID", nodeId);
			insert.setValue("CONTENTS", isc);
			db.executeInsert(conn.getConnection(), insert);
			size = isc.getSize();
		} catch (SQLException e) {
			Logger.debug(e);
		} finally {
			if( conn != null )
				db.releaseConnection(conn);
		}
	}
	
	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
	}
	
	public void open() throws IOException {
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlConnection conn = null;

		try {
			conn = db.getConnection();
			SelectQuery select = db.getDialect().createSelect();
			Table tn = new Table(appName+"_NODE_STREAMS");
			select.addTable(tn);
			select.addColumn(tn.getColumn("CONTENTS"));
			select.addFilter(new Equals(tn.getColumn("STREAMID"),new Argument(streamId)));
			PreparedStatement stmt = select.prepareStatement(conn.getConnection());
			ResultSet rs = stmt.executeQuery();
			if( rs.next() ) 
				stream = new BufferedInputStream(rs.getBinaryStream(1));
			db.closeResultSet(rs);
		} catch (SQLException e) {
			Logger.debug(e);
		} finally {
			if( conn != null )
				db.releaseConnection(conn);
		}
	}
	
	public long getId() {
		return streamId;
	}
	
	public void delete() throws IOException {
		if( stream != null )
			stream.close();
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlConnection conn = null;

		try {
			// Delete node streams
			conn = db.getConnection();
			DeleteQuery delete;
			delete = db.getDialect().createDelete(appName+"_NODE_STREAMS");
			delete.addFilter(new Equals(new Column("STREAMID"), new Argument(streamId)));
			//delete.addFilter(new Equals(new Column("SITEID"),new Argument(siteId)));
			db.executeDelete(conn.getConnection(), delete);
		} catch (SQLException e) {
			Logger.debug(e);
		} finally {
			if( conn != null )
				db.releaseConnection(conn);
		}

		
	}

	public class InputStreamCounter extends InputStream {
		private InputStream stream;
		private long size = 0;
		
		public InputStreamCounter(InputStream stream) {
			this.stream = stream;
		}
		public int read() throws IOException {
			size++;
			return stream.read();
		}
		public synchronized int read(byte b[], int off, int len) throws IOException {
			int read = stream.read(b,off,len);
			size += read;
			return read;
		}

		public synchronized long skip(long n) throws IOException {
			return stream.skip(n);
		}

		public synchronized int available() throws IOException {
			return stream.available();
		}

		public synchronized void reset() throws IOException {
			stream.reset();
		}

		public synchronized void close() throws IOException {
			stream.close();
		}
		
		public boolean markSupported() {
			return stream.markSupported();
		}
		
		public long getSize() {
			return size;
		}
		
	}
	
}
