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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;

class ConnectionReaper extends Thread {

    private SqlConnectionPool pool;
    private final long delay=300000;

    ConnectionReaper(SqlConnectionPool pool) {
        this.pool=pool;
    }

    public void run() {
        while(true) {
           try {
              sleep(delay);
           } catch( InterruptedException e) { }
           pool.reapConnections();
        }
    }
}

//TODO stop connection reaper

public class SqlConnectionPool {

   private Vector<SqlConnection> connections;
   private String url, user, password;
   final private long timeout=60000;
   private ConnectionReaper reaper;
   //final private int poolsize=10;

   public SqlConnectionPool(String url, String user, String password, int poolSize) {
      this.url = url;
      this.user = user;
      this.password = password;
      connections = new Vector<SqlConnection>(poolSize);
      reaper = new ConnectionReaper(this);
      reaper.start();
   }

   public boolean equalsSettings(String url, String user, String password) {
	   return url.equals(this.url) && password.equals(this.password) && user.equals(this.user);
   }
   
   public synchronized void reapConnections() {

      long stale = System.currentTimeMillis() - timeout;
      Enumeration<SqlConnection> connlist = connections.elements();
    
      while((connlist != null) && (connlist.hasMoreElements())) {
          SqlConnection conn = (SqlConnection)connlist.nextElement();

          if((conn.inUse()) && (stale >conn.getLastUse()) && 
                                            (!conn.validate())) {
 	      removeConnection(conn);
         }
      }
   }

   public synchronized void closeConnections() {
        
      Enumeration<SqlConnection> connlist = connections.elements();

      while((connlist != null) && (connlist.hasMoreElements())) {
          SqlConnection conn = (SqlConnection)connlist.nextElement();
          removeConnection(conn);
      }
   }

   private synchronized void removeConnection(SqlConnection conn) {
	   try {
		   conn.getConnection().close();
	   } catch (SQLException _) {}
       connections.removeElement(conn);
   }


   public synchronized SqlConnection getConnection() throws SQLException {

       SqlConnection c;
       for(int i = 0; i < connections.size(); i++) {
           c = (SqlConnection)connections.elementAt(i);
           if (c.lease()) {
              return c;
           }
       }

       System.out.println("Connections " + connections.size());
       
       Connection conn = DriverManager.getConnection(url, user, password);
       conn.setAutoCommit(false);
       conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
       c = new SqlConnection(conn, this);
       c.lease();
       connections.addElement(c);
       return c;
  } 

   public synchronized void returnConnection(SqlConnection conn) {
      conn.expireLease();
   }
   
   public class SqlConnection {

	    private SqlConnectionPool pool;
	    private Connection conn;
	    private boolean inuse;
	    private long timestamp;


	    public SqlConnection(Connection conn, SqlConnectionPool pool) {
	        this.conn=conn;
	        this.pool=pool;
	        this.inuse=false;
	        this.timestamp=0;
	    }

	    public synchronized boolean lease() {
	       if(inuse)  {
	           return false;
	       } else {
	          inuse=true;
	          timestamp=System.currentTimeMillis();
	          return true;
	       }
	    }
	    public boolean validate() {
			try {
		            conn.getMetaData();
		        } catch (Exception e) {
		        	return false;
			}
			return true;
	    }

	    public boolean inUse() {
	        return inuse;
	    }

	    public long getLastUse() {
	        return timestamp;
	    }

	    public void close() throws SQLException {
	        pool.returnConnection(this);
	    }

	    protected void expireLease() {
	        inuse=false;
	    }

	    public Connection getConnection() {
	        return conn;
	    }
	    
	    public void commit() throws SQLException {
	    	conn.commit();
	    }
	    
	    public void rollback() throws SQLException {
	    	conn.rollback();
	    }
	    

/*	    public PreparedStatement prepareStatement(String sql) throws SQLException {
	        return conn.prepareStatement(sql);
	    }

	    public CallableStatement prepareCall(String sql) throws SQLException {
	        return conn.prepareCall(sql);
	    }

	    public Statement createStatement() throws SQLException {
	        return conn.createStatement();
	    }

	    public String nativeSQL(String sql) throws SQLException {
	        return conn.nativeSQL(sql);
	    }

	    public void setAutoCommit(boolean autoCommit) throws SQLException {
	        conn.setAutoCommit(autoCommit);
	    }

	    public boolean getAutoCommit() throws SQLException {
	        return conn.getAutoCommit();
	    }

	    public void commit() throws SQLException {
	        conn.commit();
	    }

	    public void rollback() throws SQLException {
	        conn.rollback();
	    }

	    public boolean isClosed() throws SQLException {
	        return conn.isClosed();
	    }

	    public DatabaseMetaData getMetaData() throws SQLException {
	        return conn.getMetaData();
	    }

	    public void setReadOnly(boolean readOnly) throws SQLException {
	        conn.setReadOnly(readOnly);
	    }
	  
	    public boolean isReadOnly() throws SQLException {
	        return conn.isReadOnly();
	    }

	    public void setCatalog(String catalog) throws SQLException {
	        conn.setCatalog(catalog);
	    }

	    public String getCatalog() throws SQLException {
	        return conn.getCatalog();
	    }

	    public void setTransactionIsolation(int level) throws SQLException {
	        conn.setTransactionIsolation(level);
	    }

	    public int getTransactionIsolation() throws SQLException {
	        return conn.getTransactionIsolation();
	    }

	    public SQLWarning getWarnings() throws SQLException {
	        return conn.getWarnings();
	    }

	    public void clearWarnings() throws SQLException {
	        conn.clearWarnings();
	    }

		public Array createArrayOf(String typeName, Object[] elements)
				throws SQLException {
			return conn.createArrayOf(typeName, elements);
		}

		public Blob createBlob() throws SQLException {
			return conn.createBlob();
		}

		public Clob createClob() throws SQLException {
			return conn.createClob();
		}

		public NClob createNClob() throws SQLException {
			return conn.createNClob();
		}

		public SQLXML createSQLXML() throws SQLException {
			return conn.createSQLXML();
		}

		public Statement createStatement(int resultSetType, int resultSetConcurrency)
				throws SQLException {
			return conn.createStatement(resultSetType, resultSetConcurrency);
		}

		public Statement createStatement(int resultSetType,
				int resultSetConcurrency, int resultSetHoldability)
				throws SQLException {
			return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		public Struct createStruct(String typeName, Object[] attributes)
				throws SQLException {
			return conn.createStruct(typeName, attributes);
		}

		public Properties getClientInfo() throws SQLException {
			return conn.getClientInfo();
		}

		public String getClientInfo(String name) throws SQLException {
			return conn.getClientInfo(name);
		}

		public int getHoldability() throws SQLException {
			return conn.getHoldability();
		}

		public Map<String, Class<?>> getTypeMap() throws SQLException {
			return conn.getTypeMap();
		}

		public boolean isValid(int timeout) throws SQLException {
			return conn.isValid(timeout);
		}

		public CallableStatement prepareCall(String sql, int resultSetType,
				int resultSetConcurrency) throws SQLException {
			return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
		}

		public CallableStatement prepareCall(String sql, int resultSetType,
				int resultSetConcurrency, int resultSetHoldability)
				throws SQLException {
			return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
				throws SQLException {
			return conn.prepareStatement(sql, autoGeneratedKeys);
		}

		public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
				throws SQLException {
			return conn.prepareStatement(sql, columnIndexes);
		}

		public PreparedStatement prepareStatement(String sql, String[] columnNames)
				throws SQLException {
			return conn.prepareStatement(sql, columnNames);
		}

		public PreparedStatement prepareStatement(String sql, int resultSetType,
				int resultSetConcurrency) throws SQLException {
			return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
		}

		public PreparedStatement prepareStatement(String sql, int resultSetType,
				int resultSetConcurrency, int resultSetHoldability)
				throws SQLException {
			return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		public void releaseSavepoint(Savepoint savepoint) throws SQLException {
			conn.releaseSavepoint(savepoint);
		}

		public void rollback(Savepoint savepoint) throws SQLException {
			conn.rollback();
		}

		public void setClientInfo(Properties properties)
				throws SQLClientInfoException {
			conn.setClientInfo(properties);
		}

		public void setClientInfo(String name, String value)
				throws SQLClientInfoException {
			conn.setClientInfo(name, value);
		}

		public void setHoldability(int holdability) throws SQLException {
			conn.setHoldability(holdability);
		}

		public Savepoint setSavepoint() throws SQLException {
			return conn.setSavepoint();
		}

		public Savepoint setSavepoint(String name) throws SQLException {
			return conn.setSavepoint(name);
		}

		public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
			conn.setTypeMap(map);
		}

		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return conn.isWrapperFor(iface);
		}

		public <T> T unwrap(Class<T> iface) throws SQLException {
			return conn.unwrap(iface);
		}*/
	}

}
