/*
 * 	Copyright (C) 2008, CodeGlide - Entwickler, S.A.
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

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.core.Logger;
import com.codeglide.core.ServerSettings;
import com.codeglide.core.acl.AclToken;
import com.codeglide.core.objects.ObjectDefinition;
import com.codeglide.core.rte.ReceivedFile;
import com.codeglide.interfaces.LeavesNode;
import com.codeglide.interfaces.SearchableNode;
import com.codeglide.interfaces.xmldb.SqlConnectionPool.SqlConnection;
import com.codeglide.interfaces.xmldb.sql.Argument;
import com.codeglide.interfaces.xmldb.sql.Column;
import com.codeglide.interfaces.xmldb.sql.Equals;
import com.codeglide.interfaces.xmldb.sql.SelectQuery;
import com.codeglide.interfaces.xmldb.sql.SqlDialect;
import com.codeglide.interfaces.xmldb.sql.Table;
import com.codeglide.interfaces.xmldb.sql.UpdateQuery;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.dom.ExtendedElement;

public class DbContainerNode extends DbNode implements SearchableNode, LeavesNode {

	public DbContainerNode(Document document, long id, long parentId, int nodeId) {
		super(document, id, parentId, nodeId);
		nodeFlags |= NEEDS_CHILD_INIT;
	}

	public DbContainerNode(Document document, long id) {
		super(document, id);
		nodeFlags |= NEEDS_CHILD_INIT;
	}

	public DbContainerNode(Document document, String nodeName) {
		super(document, nodeName);
	}
	
	protected void initChildren() {
		if( id == -1 || (parentId == -1 && nodeName != null) )
			return;
	
		DbInterface db = (DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF);

		// Add attributes
		if( (nodeFlags & NEEDS_ATTR_INIT ) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		
		// Get children
		SqlConnection conn = null;
		try {
			SqlDialect dialect = db.getDialect();
			conn = db.getConnection();

			SelectQuery select = dialect.createSelect();
			Table th = new Table(rootNode.getApplication().getName()+"_HIERARCHY");
			select.addTable(th);
			select.addColumn(th.getColumn("ID"));
			select.addColumn(th.getColumn("NODENAME"));
			select.addColumn(th.getColumn("SIZE"));
			select.addColumn(th.getColumn("METADATA"));
			// If we own the folder there is no need to check ACLs
			if( !isItemOwner() )
				select.addFilter(((DbRootNode)rootNode.getChildNode("Db")).getAclRead(th.getColumn("ACLREAD")));
			//select.addFilter(new Equals(th.getColumn("ISCONTAINER"),new Argument( ((nodeFlags & EXPAND_ONLY_CONTAINERS) != 0) ? 1 : 0 )));
			select.addFilter(new Equals(th.getColumn("ISCONTAINER"),new Argument( 1 )));
			select.addFilter(new Equals(th.getColumn("PARENTID"),new Argument(id)));
			PreparedStatement stmt = select.prepareStatement(conn.getConnection());
			ResultSet rs = stmt.executeQuery();
			
			while( rs.next() ) {
				try {
					DbNode node = new DbContainerNode(parentDoc, rs.getLong(1), id, rs.getInt(2));
					node.disableTracking();
					deserialize(rs.getString(4), 0, node, node.getId(), rootNode.getApplication().getObject(node.getNodeName()));
					node.setSize(rs.getLong(3));
					node.setAcl(node.getAttribute("_Acl"));
					node.enableTracking();
					_appendChild(node);
				} catch (Exception e) {
					Logger.debug(e);
				}
			}
			db.closeResultSet(rs);
		} catch (SQLException e) {
			Logger.debug(e);
		} finally {
			if( conn != null )
				db.releaseConnection(conn);
		}
	}
	
	public Node removeChild(Node arg0) throws DOMException {
		DbNode node = (DbNode)arg0;
		node.delete();
		children.remove(node);
		return arg0;
	}
	
	public Node removeChild(String name) throws DOMException {
		DbNode node = (DbNode) getChildNode(name);
		if( node == null )
			return null;
		return this.removeChild(node);
	}

	public DynamicElement move(DynamicElement source) {
		boolean doCommit = false;
		try {
			// Create or use existing transaction
			Transaction transaction = Transaction.getActive();
			if( doCommit = (transaction == null) ) 
				transaction = Transaction.create();

			DbNode moved;
			if( !hasPermission(AclToken.ACL_INSERT) )
				throw new DOMException(DOMException.VALIDATION_ERR, "@acl-violation-insert");
			else if( source instanceof ExtendedElement && !((ExtendedElement)source).hasPermission(AclToken.ACL_DELETE) )
				throw new DOMException(DOMException.VALIDATION_ERR, "@acl-violation-delete");
				
			if( source instanceof DbNode ) {
				DbNode node = (DbNode)source;
				DbInterface db = (DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF);
				try {
					SqlConnection conn = transaction.getConnection();
					UpdateQuery update = db.getDialect().createUpdate(rootNode.getApplication().getName()+"_HIERARCHY");
					update.setValue("PARENTID", new Argument(id));
					update.addFilter(new Equals(new Column("ID"), new Argument(node.getId())));
					db.executeUpdate(conn.getConnection(), update);
					/** BEGIN MODIFICATION
					 *		BY Hugo, 2008-01-18
					 *	Remove these comments if modifications are approved
					 */
					node.setParent(this);
					//node.setParentId(id);		INCLUDED IN DbNode >> setParent(...)
					/** END MODIFICATION */
				} catch (Exception e) {
					DOMException error = new DOMException(DOMException.VALIDATION_ERR, "@insert-failed");
					error.initCause(e);
					throw error;
				}
				moved = node;
			} else {
				List<InputStream> streams = new LinkedList<InputStream>();
				long nodeId = insert(transaction.getConnection(), source, id, streams);	

				// Delete received files
				if( streams.size() > 0 ) {
					for( InputStream stream : streams )  {
						if( stream instanceof ReceivedFile )
							((ReceivedFile)stream).delete();
					}
				}
				
				if( isContainer(source) ) {
					moved = new DbContainerNode(parentDoc, nodeId);
					if( (nodeFlags & EXPAND_ONLY_CONTAINERS) != 0 )
						moved.setFlag(EXPAND_ONLY_CONTAINERS);
				} else
					moved = new DbLeafNode(parentDoc, nodeId);
			}

			source.getParentNode().removeChild(source);
			this._appendChild(moved);
			
			// Commit transaction
			if( doCommit )
				transaction.commit();

			return moved;
		} catch(Throwable e) {
			// Rollback transaction
			if( doCommit )
				Transaction.getActive().rollback();

			// Set error
			DOMException error;
			if( e instanceof DOMException )
				error = (DOMException)e;
			else {
				error = new DOMException(DOMException.VALIDATION_ERR, "@move-failed");
				error.initCause(e);
			}
			throw error;
		}
	}
	
	public Node appendChild(Node arg0) throws DOMException {
		if( arg0.getNodeName().startsWith("_") )
			return super.appendChild(arg0);
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		if( !hasPermission(AclToken.ACL_INSERT) )
			throw new DOMException(DOMException.VALIDATION_ERR, "@acl-violation-insert");
		if( ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF)).isOverQuota(rootNode, aclList.get(0).getUid()) )
			throw new DOMException(DOMException.VALIDATION_ERR, "@quota-exceeded");

		ObjectDefinition builder = rootNode.getApplication().getObject(arg0.getNodeName());
		if( builder != null ) {
			if( !((ObjectDefinition)builder).handleEvent(ObjectDefinition.ON_INSERT, (DynamicElement)arg0) )
				return arg0;
			if( !((ObjectDefinition)builder).handleEvent(ObjectDefinition.ON_SAVE, (DynamicElement)arg0) )
				return arg0;
		}
		
		boolean doCommit = false;
		try {
			// Create or use existing transaction
			Transaction transaction = Transaction.getActive();
			if( doCommit = (transaction == null) ) 
				transaction = Transaction.create();

			List<InputStream> streams = new LinkedList<InputStream>();
			long nodeId = insert(transaction.getConnection(), (DynamicElement)arg0,id,streams);

			// Commit transaction
			if( doCommit )
				transaction.commit();

			if( nodeId > -1 ) {

				// Delete received files
				if( streams.size() > 0 ) {
					for( InputStream stream : streams )  {
						if( stream instanceof ReceivedFile )
							((ReceivedFile)stream).delete();
					}
				}

				if( isContainer(arg0) ) {
					DbNode newNode = new DbContainerNode(parentDoc, nodeId);
					if( (nodeFlags & EXPAND_ONLY_CONTAINERS) != 0 )
						newNode.setFlag(EXPAND_ONLY_CONTAINERS);
					super.appendChild(newNode);
					return newNode;
				} else
					return new DbLeafNode(parentDoc, nodeId);
			} else
				return arg0;
		} catch(Throwable e) {
			// Rollback transaction
			if( doCommit )
				Transaction.getActive().rollback();

			// Set error
			DOMException error;
			if( e instanceof DOMException )
				error = (DOMException)e;
			else {
				error = new DOMException(DOMException.VALIDATION_ERR, "@insert-failed");
				error.initCause(e);
			}
			throw error;
		}
	}
	
	public Node appendChild(String name) throws DOMException {
		if( name.startsWith("_"))
			return super.appendChild(name);
		throw new DOMException(DOMException.VALIDATION_ERR, "@not-supported");
	}
	
	public boolean hasResursivePermission(int operation) {
		if( isItemOwner() )
			return true;
		else if( !hasPermission(operation) )
			return false;
		else if( children != null ) {
			Iterator<Node> it = children.iterator();
			while(it.hasNext()) {
				Node node = (Node)it.next();
				if( node instanceof DbContainerNode ) {
					if( !((DbContainerNode)node).hasResursivePermission(operation) )
						return false;
				} else if( node instanceof DbLeafNode && !((DbLeafNode)node).hasPermission(operation) )
					return false;
			}
		}
		return true;
	}
	
	protected void delete(SqlConnection conn, boolean checkAcl) throws DOMException, SQLException {
		if( checkAcl && !hasResursivePermission(AclToken.ACL_DELETE) )
			throw new DOMException(DOMException.VALIDATION_ERR, "@acl-violation-delete");

		if( children != null ) {
			Iterator<Node> it = children.iterator();
			while(it.hasNext())
				((DbNode)it.next()).delete(conn, false);
		}

		delete(conn);
	}

	public List<Node> getLeaves() {
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlDialect dialect = db.getDialect();
		SelectQuery select = dialect.createSelect();
		Table th = new Table(rootNode.getApplication().getName()+"_HIERARCHY");
		select.addTable(th);
		select.addFilter(new Equals(th.getColumn("PARENTID"),new Argument(this.id)));
		return db.getLeaves(select, th, null, null, this, null, 0, 0);
	}

	public List<Node> getLeaves(String nodeName, String sortBy, int rowStart, int rowLimit ) {
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlDialect dialect = db.getDialect();
		SelectQuery select = dialect.createSelect();
		Table th = new Table(rootNode.getApplication().getName()+"_HIERARCHY");
		select.addTable(th);
		Table ts = null;
		ObjectDefinition objDef = null;
		if( nodeName != null && !nodeName.isEmpty() ) {
			objDef = rootNode.getApplication().getObject(nodeName);
			if( objDef != null && sortBy != null && !sortBy.isEmpty()) {
				ts = new Table(rootNode.getApplication().getName()+"_IDX_"+nodeName.toUpperCase());
				select.addTable(ts);
			} else
				select.addFilter(new Equals(th.getColumn("NODENAME"), new Argument(rootNode.getApplication().getId(nodeName))));
		}
		select.addFilter(new Equals(th.getColumn("PARENTID"),new Argument(this.id)));
		return db.getLeaves(select, th, ts, objDef, this, sortBy, rowStart, rowLimit);
	}

	public List<Node> search(DynamicElement searchObject, String sortBy, int rowStart, int rowLimit) {
		searchObject.setAttribute("Folder", Long.toString(this.id, 36));
		return ((DbRootNode)rootNode.getChildNode("Db")).search(searchObject, sortBy, rowStart, rowLimit);
	}

}
