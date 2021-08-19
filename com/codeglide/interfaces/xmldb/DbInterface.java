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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.core.Logger;
import com.codeglide.core.objects.ObjectDefinition;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.Application;
import com.codeglide.interfaces.Interface;
import com.codeglide.interfaces.root.RootNode;
import com.codeglide.interfaces.xmldb.SqlConnectionPool.SqlConnection;
import com.codeglide.interfaces.xmldb.sql.Argument;
import com.codeglide.interfaces.xmldb.sql.DeleteQuery;
import com.codeglide.interfaces.xmldb.sql.Equals;
import com.codeglide.interfaces.xmldb.sql.InsertQuery;
import com.codeglide.interfaces.xmldb.sql.Like;
import com.codeglide.interfaces.xmldb.sql.SelectQuery;
import com.codeglide.interfaces.xmldb.sql.SqlDialect;
import com.codeglide.interfaces.xmldb.sql.Sum;
import com.codeglide.interfaces.xmldb.sql.Table;
import com.codeglide.interfaces.xmldb.sql.UpdateQuery;
import com.codeglide.interfaces.xmldb.sql.dialects.HSQLDialect;
import com.codeglide.interfaces.xmldb.sql.dialects.MySqlDialect;
import com.codeglide.interfaces.xmldb.sql.dialects.OracleDialect;
import com.codeglide.interfaces.xmldb.sql.dialects.PostgresDialect;
import com.codeglide.interfaces.xmldb.sql.dialects.SqlServerDialect;
import com.codeglide.interfaces.xmldb.sql.schema.ColumnDefinition;
import com.codeglide.interfaces.xmldb.sql.schema.IndexDefinition;
import com.codeglide.interfaces.xmldb.sql.schema.TableDefinition;
import com.codeglide.xml.dom.DynamicElement;

/*
 * Parameters:
 * 
 * connstring      = Connection String to SQL
 * user            = SQL User
 * pass            = SQL Pass
 * type            = SQL Server type
 * poolsize        = SQL Connection pool size
 * driverclass     = SQL Driver Class
 * binarystore     = Shared directory where binaries are stored
 * 
 * serverid        = Numeric ID of the current server
 * quota           = <IdxName>,<UidColumnName>,<QuotaColumnName>
 * 
 */

public class DbInterface extends Interface {
	private static Vector<SqlConnectionPool> poolVector = new Vector<SqlConnectionPool>();
	private SqlConnectionPool pool = null;
	private SqlDialect dialect;
	
	/*
	 * Unique Id format
	 * 
	 * 1  bit  = 0 (to make sure it is a positive Long value)
	 * 32 bits = seconds since August 1st, 2007
	 * 15 bits = server id, 0 to 32,767
	 * 16 bits = counter, 0 to 65,535
	 * 
	 */
	
	private final static long epoch = 61146673200L;
	
	private int idCounter = 0;
	private int idServer = 0;
	
	private int idxSize = 20;
	
	public synchronized long getUniqueId() {
		long id = 0L;
		
		idCounter = (idCounter + 1) % 65535;
		
		id |= ((System.currentTimeMillis()/1000) - epoch);
		id <<= 15;
		id |= idServer;
		id <<= 16;
		id |= idCounter;
		
		return id;
	}
	
	public void init() throws Exception {
		synchronized(poolVector) {
			Iterator<SqlConnectionPool> it = poolVector.iterator();
			String poolSize = params.get("poolsize");
			if(poolSize==null)
				poolSize="10";
			while( it.hasNext() && pool == null ) {
				pool = it.next();
				if( !pool.equalsSettings(params.get("connstring"), params.get("user"), params.get("pass")) )
					pool = null;
			}
			if( pool == null ) {
				pool = new SqlConnectionPool(params.get("connstring"), params.get("user"), params.get("pass"), Integer.parseInt(poolSize));
				poolVector.add(pool);
			}
		}
		String dbType = params.get("type");
		if( dbType.equals("mssql") )
			dialect = new SqlServerDialect();
		else if( dbType.equals("mysql") )
			dialect = new MySqlDialect();
		else if( dbType.equals("oracle") )
			dialect = new OracleDialect();
		else if( dbType.equals("postgres") )
			dialect = new PostgresDialect();
		else if( dbType.equals("hsql") ) 
			dialect = new HSQLDialect();
		try {
			Class.forName(params.get("driverclass"));
		} catch (Exception e) {
			Logger.error(e.getMessage());
			Logger.debug(e);
		}
		
		idServer = Integer.parseInt(params.get("serverid"));

		try {
			idxSize = Integer.parseInt(params.get("indexsize"));
		} catch (Exception _) {
			idxSize = 20;
		}

	}
	
	public void initApplication(Application application) throws Exception {
		String tablePrefix = application.getName();

		// Are there any tables?
		if( hasTable(tablePrefix + "_HIERARCHY") )
			return;
		initBinaryStore(tablePrefix);
		
		// The application is deployed for the first time
		application.setFlag(Application.FIRST_DEPLOYMENT);
		
		//TODO migrate tables if schema is different

		// Create hierarchy table
		TableDefinition table = new TableDefinition(tablePrefix+"_HIERARCHY");
		table.addColumn(new ColumnDefinition("ID", ColumnDefinition.TYPE_BIGINT, 0, false, true, false));
		table.addColumn(new ColumnDefinition("PARENTID", ColumnDefinition.TYPE_BIGINT, 0, false, false, false));
		table.addColumn(new ColumnDefinition("NODENAME", ColumnDefinition.TYPE_INT, 0, false, false, false));
		table.addColumn(new ColumnDefinition("ACLREAD", ColumnDefinition.TYPE_VARCHAR, 200, false, false, false));
		table.addColumn(new ColumnDefinition("ISCONTAINER", ColumnDefinition.TYPE_INT, 0, false, false, false));
		table.addColumn(new ColumnDefinition("SIZE", ColumnDefinition.TYPE_BIGINT, 0, false, false, false));
		table.addColumn(new ColumnDefinition("METADATA", ColumnDefinition.TYPE_TEXT, 0, false, false, false));
		table.addIndex(new IndexDefinition(new String[] { "PARENTID", "NODENAME", "ACLREAD", "ISCONTAINER", "SIZE" }));
		createTable(table);
		
		// Create Changelog table
		table = new TableDefinition(tablePrefix+"_CHANGELOG");
		table.addColumn(new ColumnDefinition("ID", ColumnDefinition.TYPE_BIGINT, 0, false, false, false));
		table.addColumn(new ColumnDefinition("CHANGEDBY", ColumnDefinition.TYPE_VARCHAR, 20, false, false, false));
		table.addColumn(new ColumnDefinition("CHANGEDON", ColumnDefinition.TYPE_VARCHAR, 20, false, false, false));
		table.addColumn(new ColumnDefinition("METADATA", ColumnDefinition.TYPE_TEXT, 0, false, false, false));
		table.addIndex(new IndexDefinition(new String[] { "ID", "CHANGEDON" }));
		createTable(table);
		
		// Create Streams table
		table = new TableDefinition(tablePrefix+"_NODE_STREAMS");
		table.addColumn(new ColumnDefinition("STREAMID", ColumnDefinition.TYPE_BIGINT, 0, false, true, false));
		table.addColumn(new ColumnDefinition("NODEID", ColumnDefinition.TYPE_BIGINT, 0, false, false, false));
		table.addColumn(new ColumnDefinition("CONTENTS", ColumnDefinition.TYPE_BINARY, 0, false, false, false));
		table.addIndex(new IndexDefinition(new String[] { "NODEID" }));
		createTable(table);
		
		// Create Streams table
		table = new TableDefinition(tablePrefix+"_LINK");
		table.addColumn(new ColumnDefinition("SOURCEID", ColumnDefinition.TYPE_BIGINT, 0, false, false, false));
		table.addColumn(new ColumnDefinition("TARGETID", ColumnDefinition.TYPE_BIGINT, 0, false, false, false));
		table.addColumn(new ColumnDefinition("TYPE", ColumnDefinition.TYPE_INT, 0, false, false, false));
		table.addIndex(new IndexDefinition(new String[] { "SOURCEID", "TARGETID", "TYPE" }));
		createTable(table);
		
		// Create Streams table
		table = new TableDefinition(tablePrefix+"_QUOTA");
		table.addColumn(new ColumnDefinition("ID", ColumnDefinition.TYPE_BIGINT, 0, false, true, false));
		table.addColumn(new ColumnDefinition("QUOTA", ColumnDefinition.TYPE_BIGINT, 0, false, false, false));
		createTable(table);
		
		// Create Messages table
		table = new TableDefinition(tablePrefix+"_MESSAGES");
		table.addColumn(new ColumnDefinition("ID", ColumnDefinition.TYPE_BIGINT, 0, false, true, false));
		table.addColumn(new ColumnDefinition("METADATA", ColumnDefinition.TYPE_TEXT, 0, false, false, false));
		createTable(table);
		
		// Create indexes
		Iterator<ObjectDefinition> it = application.getObjects().iterator();
		while( it.hasNext() ) {
			ObjectDefinition obj = it.next();
			Collection<ObjectField> schema = application.getAllObjectFields(obj.getId(), ObjectField.T_INDEX);
			if( schema != null ) {
				ArrayList<String> indexFields = new ArrayList<String>();
				table = new TableDefinition(tablePrefix+"_IDX_" + obj.getId().toUpperCase());
				table.addColumn(new ColumnDefinition("ID", ColumnDefinition.TYPE_BIGINT, 0, false, true, false));
				
				for( ObjectField item : schema ) {
					//TODO handle boolean, double
					String fieldName = application.getColumnName(item);
					ColumnDefinition colDef = null;
					switch( item.getFormat() ) {
						case ObjectField.F_INTEGER:
						case ObjectField.F_BOOLEAN:
						case ObjectField.F_DOUBLE:
							colDef = new ColumnDefinition(fieldName, ColumnDefinition.TYPE_INT, 0, true, false, false);
							break;
						case ObjectField.F_LINK:
							colDef = new ColumnDefinition(fieldName, ColumnDefinition.TYPE_BIGINT, 0, true, false, false);
							break;
						case ObjectField.F_STRING:
						case ObjectField.F_STRING_TOKENIZER:
						case ObjectField.F_DATE:
						case ObjectField.F_ENUM:
							colDef = new ColumnDefinition(fieldName, ColumnDefinition.TYPE_VARCHAR, idxSize, true, false, false);
							break;
						default:
							break;
					}
					if( colDef != null ) {
						table.addColumn(colDef);
						indexFields.add(fieldName);
						if( indexFields.size() >= 10 ) {
							table.addIndex(new IndexDefinition(indexFields.toArray( new String[indexFields.size()] ) ));
							indexFields = new ArrayList<String>();
						}
					}
 				}
				if( indexFields.size() > 0 )
					table.addIndex(new IndexDefinition(indexFields.toArray( new String[indexFields.size()] ) ));
				createTable(table);
			}
		}
		
	}
	
	private void initBinaryStore(String appName) {
		String binaryRoot = getParameter("binarystore");
		File f = new File(binaryRoot);
		if( !f.exists() ) 
			f.mkdir();
		binaryRoot += "/" + appName;
		f = new File(binaryRoot);
		if( !f.exists() ) {
			f.mkdir();
			for( int i = 0; i < 64; i++ ) {
				(new File(binaryRoot+"/"+i)).mkdir();
				for( int j = 0; j < 64; j++ ) {
					(new File(binaryRoot+"/"+i+"/"+j)).mkdir();
				}
			}
		}
		
	}

	public void createTable(TableDefinition table) throws SQLException {
		SqlConnection conn = pool.getConnection();
		String[] sqls = dialect.generateTableStatements(table);

		for (int i = 0; i < sqls.length; i++) {
			Statement stmt = conn.getConnection().createStatement();
			//TODO remove logger
			Logger.debug(sqls[i]);
			stmt.execute(sqls[i]);
			stmt.close();
		}
		pool.returnConnection(conn);
	}
	
	public boolean hasTable(String tableName) throws SQLException
	{
		String sql = "SELECT COUNT(*) FROM " + tableName;
		ResultSet rs;
		
		SqlConnection conn = pool.getConnection();
		try
		{
			PreparedStatement stmt = conn.getConnection().prepareStatement(sql);
			rs = stmt.executeQuery();
		}
		catch (SQLException e)
		{
			pool.returnConnection(conn);
			return false;
		}
		
		closeResultSet(rs);
		pool.returnConnection(conn);
		return true;
	}

	public void closeResultSet(ResultSet rs) {
		try {
			PreparedStatement stmt = (PreparedStatement) rs.getStatement();
			rs.close();
			stmt.close();
		} catch (SQLException e) {
		}
	}

	public SqlConnection getConnection() throws SQLException {
		return pool.getConnection();
	}
	
	public void releaseConnection(SqlConnection conn) {
		pool.returnConnection(conn);
	}
	
	public SqlDialect getDialect() {
		return dialect;
	}
	
	public long executeInsert(Connection conn, InsertQuery query) throws SQLException
	{
		PreparedStatement stmt = query.prepareStatement(conn, !(dialect instanceof HSQLDialect));
		stmt.executeUpdate();
		long id = -1;
		if( !(dialect instanceof HSQLDialect) ) {
			ResultSet keys = stmt.getGeneratedKeys();
			if( keys.next() )
				id = keys.getLong(1);
			closeResultSet(keys);
			stmt.close();
		} else {
			// For HSQLDB we have to do this...
			stmt.close();
			stmt = conn.prepareStatement("CALL IDENTITY()");
			ResultSet rs = stmt.executeQuery();
			if( rs.next() )
				id = rs.getLong(1);
			closeResultSet(rs);
		}
		return id;
	}

	protected void executeUpdate(Connection conn, UpdateQuery query) throws SQLException
	{
		PreparedStatement stmt = query.prepareStatement(conn, !(dialect instanceof HSQLDialect));
		stmt.executeUpdate();
		stmt.close();
	}

	public void executeDelete(Connection conn, DeleteQuery query) throws SQLException
	{
		PreparedStatement stmt = query.prepareStatement(conn, !(dialect instanceof HSQLDialect));
		stmt.executeUpdate();
		stmt.close();
	}
	
	public int getIndexSize() {
		return idxSize;
	}

	public DynamicElement createRootElement(Document document) {
		return new DbRootNode(document);
	}

	
	/*
	 * 
	 * Quota Management functions
	 * 
	 */
	
	private class QuotedUid {
		public long totalQuota = 0;
		public long usedQuota = 0;
		public boolean hasDeleted = false;
	}
	
	private HashMap<Long, QuotedUid> quotaCache = new HashMap<Long, QuotedUid>();
	
	
	//TODO implement quotas
	public long getTotalQuota(RootNode rootNode, long uid) {
		Long lUid = new Long(uid);
		QuotedUid qUid = quotaCache.get(lUid);
		if( qUid == null ) {
			qUid = new QuotedUid();
			SqlConnection conn = null;
			try {
				SqlDialect dialect = getDialect();
				conn = getConnection();

				SelectQuery select = dialect.createSelect();
				Table tn = new Table(rootNode.getApplication().getName()+"_QUOTA");
				select.addTable(tn);
				select.addColumn(tn.getColumn("QUOTA"));
				select.addFilter(new Equals(tn.getColumn("ID"),new Argument(uid)));
				PreparedStatement stmt = select.prepareStatement(conn.getConnection());
				ResultSet rs = stmt.executeQuery();
				
				if( rs.next() )
					qUid.totalQuota = rs.getLong(1);
				
				closeResultSet(rs);
			} catch (SQLException e) {
				Logger.debug(e);
			} finally {
				if( conn != null )
					releaseConnection(conn);
			}
			qUid.usedQuota = getUsedQuota(rootNode, uid);
			quotaCache.put(lUid, qUid);
		}
		return qUid.totalQuota;
	}
	
	//TODO call this method when total quota is changed or user logs off
	public void removeCachedQuota(long uid) {
		quotaCache.remove(new Long(uid));
	}

	public void updateUsedQuota(long uid, long bytes) {
		QuotedUid qUid = quotaCache.get(new Long(uid));
		if( qUid != null && qUid.totalQuota > 0 ) {
			qUid.usedQuota += bytes;
			if( bytes < 0 && !qUid.hasDeleted )
				qUid.hasDeleted = true;
		}
	}
	
	public long getUsedQuota(RootNode rootNode, long uid) {
		// Calculate Quota
		long usedQuota = 0;
		SqlConnection conn = null;
		try {
			SqlDialect dialect = getDialect();
			conn = getConnection();

			SelectQuery select = dialect.createSelect();
			Table tn = new Table(rootNode.getApplication().getName()+"_HIERARCHY");
			select.addTable(tn);
			select.addColumn(new Sum(tn.getColumn("SIZE")));
			select.addFilter(new Like(tn.getColumn("ACLREAD"),new Argument("_"+Long.toString(uid, 36)+"_%")));
			PreparedStatement stmt = select.prepareStatement(conn.getConnection());
			ResultSet rs = stmt.executeQuery();
			
			if( rs.next() )
				usedQuota = rs.getLong(1);
			
			closeResultSet(rs);
		} catch (SQLException e) {
			Logger.debug(e);
		} finally {
			if( conn != null )
				releaseConnection(conn);
		}
		return usedQuota;
	}
	
	public boolean isOverQuota(RootNode rootNode, long uid) {
		long totalQuota = getTotalQuota(rootNode, uid);
		
		if( totalQuota <= 0 )
			return false;
		
		QuotedUid qUid = quotaCache.get(uid);
		if( qUid.usedQuota > totalQuota ) {
			if( qUid.hasDeleted ) {
				qUid.usedQuota = getUsedQuota(rootNode, uid);
				qUid.hasDeleted = false;
				if( qUid.usedQuota <= totalQuota )
					return false;
			}
			return true;
		}
		return false;
	}
	
	public List<Node> getLeaves(SelectQuery select, Table hierarchyTable, Table indexTable, ObjectDefinition objDef, DbNode parentNode, String sortBy, int rowStart, int rowLimit ) {
		List<Node> result = new LinkedList<Node>();

		SqlConnection conn = null;
		try {
			conn = getConnection();
			Document parentDoc = parentNode.getDocumentNode();
			RootNode rootNode = (RootNode)parentDoc.getDocumentElement();

			select.addColumn(hierarchyTable.getColumn("ID"));
			select.addColumn(hierarchyTable.getColumn("PARENTID"));
			select.addColumn(hierarchyTable.getColumn("NODENAME"));
			select.addColumn(hierarchyTable.getColumn("SIZE"));
			select.addColumn(hierarchyTable.getColumn("METADATA"));
			select.addFilter(parentNode.getAclRead(hierarchyTable.getColumn("ACLREAD")));
			select.addFilter(new Equals(hierarchyTable.getColumn("ISCONTAINER"),new Argument(0)));

			// Set limits
			if( rowLimit > 0 )
				select.setTop(rowLimit);
			if( rowStart > 0 )
				select.setOffset(rowStart-1);
			
			// Add sorting fields
			if( indexTable != null && sortBy != null && !sortBy.isEmpty() ) {
				String[] sortTerms = sortBy.split(",");
				for( int i = 0; i < sortTerms.length; i++ ) {
					boolean isDesc = (sortTerms[i].startsWith("@"));
					if( isDesc )
						sortTerms[i] = sortTerms[i].substring(1);
					ObjectField field = objDef.getField(sortTerms[i]);
					if( field != null )
						select.addSorting(indexTable.getColumn(rootNode.getApplication().getColumnName(field)), isDesc);
				}
				select.addFilter(new Equals(hierarchyTable.getColumn("ID"), indexTable.getColumn("ID")));
			}
			
			PreparedStatement stmt = select.prepareStatement(conn.getConnection());
			ResultSet rs = stmt.executeQuery();
			
			while( rs.next() ) {
				DbNode node = new DbLeafNode(parentDoc, rs.getLong(1), rs.getLong(2), rs.getInt(3));
				node.setSize(rs.getLong(4));
				node.deserialize(rs.getString(5), 0, node, rs.getLong(1), rootNode.getApplication().getObject(node.getNodeName()));
				node.setAcl(node.getAttribute("_Acl"));
				node.removeFlag(DynamicElement.NEEDS_ATTR_INIT);
				node.enableTracking();
				result.add(node);
			}
			closeResultSet(rs);
		} catch (SQLException e) {
			Logger.debug(e);
		} finally {
			if( conn != null )
				releaseConnection(conn);
		}
		
		return result;
	}
	
	
	
}
