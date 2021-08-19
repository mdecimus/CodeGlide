package com.codeglide.interfaces.xmldb;

import java.sql.SQLException;
import java.util.HashMap;

import com.codeglide.core.Logger;
import com.codeglide.core.ServerSettings;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.RuntimeError;
import com.codeglide.interfaces.xmldb.SqlConnectionPool.SqlConnection;

public class Transaction {
	private static HashMap<Thread, Transaction> transactionMap = new HashMap<Thread, Transaction>();
	protected DbInterface db = (DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF);
	protected SqlConnection connection = null;
	
	public SqlConnection getConnection() throws SQLException {
		if( connection == null )
			this.connection = db.getConnection();
		return connection;
	}
	
	public void commit() throws CodeGlideException {
		synchronized( transactionMap ) {
			transactionMap.remove(Thread.currentThread());
		}
		try {
			if( connection != null )
				try {
					connection.commit();
				} finally {
					db.releaseConnection(connection);
					connection = null;
				}
		} catch (SQLException e) {
			CodeGlideException error = new RuntimeError("@commit-failed");
			error.initCause(e);
			throw error;
		}
	}
	
	public void rollback() {
		synchronized( transactionMap ) {
			transactionMap.remove(Thread.currentThread());
		}
		try {
			if( connection != null )
				try {
					connection.rollback();
				} finally {
					db.releaseConnection(connection);
					connection = null;
				}
		} catch (SQLException e) {
			Logger.debug(e);
		}
	}
	
	protected void finalize() throws Throwable {
		super.finalize();
		if( connection != null )
			db.releaseConnection(connection);
	}

	public static Transaction create() {
		Transaction result = transactionMap.get(Thread.currentThread());
		if( result == null ) {
			result = new Transaction();
			synchronized( transactionMap ) {
				transactionMap.put(Thread.currentThread(), result);
			}
		}
		return result;
	}
	
	public static Transaction getActive() {
		return transactionMap.get(Thread.currentThread());
	}
	
}
