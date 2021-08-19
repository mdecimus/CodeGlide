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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.ServerSettings;
import com.codeglide.core.acl.AclToken;
import com.codeglide.core.acl.UidToken;
import com.codeglide.core.objects.ObjectDefinition;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.ReceivedFile;
import com.codeglide.interfaces.root.RootNode;
import com.codeglide.interfaces.xmldb.SqlConnectionPool.SqlConnection;
import com.codeglide.interfaces.xmldb.sql.Argument;
import com.codeglide.interfaces.xmldb.sql.BooleanExpression;
import com.codeglide.interfaces.xmldb.sql.Column;
import com.codeglide.interfaces.xmldb.sql.DeleteQuery;
import com.codeglide.interfaces.xmldb.sql.Equals;
import com.codeglide.interfaces.xmldb.sql.InsertQuery;
import com.codeglide.interfaces.xmldb.sql.Like;
import com.codeglide.interfaces.xmldb.sql.Or;
import com.codeglide.interfaces.xmldb.sql.SelectQuery;
import com.codeglide.interfaces.xmldb.sql.SqlDialect;
import com.codeglide.interfaces.xmldb.sql.Table;
import com.codeglide.interfaces.xmldb.sql.UpdateQuery;
import com.codeglide.util.ISO8601;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicAttrDate;
import com.codeglide.xml.dom.DynamicAttrStream;
import com.codeglide.xml.dom.DynamicAttrString;
import com.codeglide.xml.dom.DynamicAttrVirtual;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.dom.ExtendedElement;
import com.codeglide.xml.dom.VirtualElement;

public abstract class DbNode extends DynamicElement implements ExtendedElement {
	protected long id = -1;
	protected long parentId = -1;
	protected long size = 0;
	protected RootNode rootNode;
	protected List<AclToken> aclList;
	
	/*
	 * Creates a new DbNode
	 * 
	 */

	public DbNode( Document document, String nodeName ) {
		super(document, nodeName);
		rootNode = (RootNode)parentDoc.getDocumentElement();
	}
	
	/*
	 * Creates a node using the information from the table HIERARCHY
	 * 
	 */
	public DbNode(Document document, long id, long parentId, int nodeId) {
		this( document, null );
		this.id = id;
		this.parentId = parentId;
		this.nodeName = rootNode.getApplication().getString(nodeId);
		
		_setAttributeNode(new AttrId(this));
		// We need to initialize before using this DbNode
		nodeFlags |= NEEDS_ATTR_INIT;
	}
	
	public DbNode(Document document, long id ) {
		// Load settings
		this( document, null );
		this.id = id;
		this.parentId = -1;
		this.nodeName = null;
		
		_setAttributeNode(new AttrId(this));
		// We need to initialize before using this DbNode
		nodeFlags |= NEEDS_ATTR_INIT;
	}
	
	public long getId() {
		return id;
	}

	public RootNode getRootNode() {
		return rootNode;
	}
	
	public long getParentId() {
		return parentId;
	}
	
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	
	/** BEGIN ADDITION
	 *		BY Hugo, 2008-01-18
	 *	Remove these comments if modifications are approved
	 */
	public void setParent(DynamicElement parentNode) {
		super.setParent(parentNode);
		if (parentNode != null  && parentNode instanceof DbContainerNode && this.parentId != -1)
			//Condition "this.parentId != -1" put because parentId is used like a flag and otherwise it will be set on start up
			this.parentId = ((DbContainerNode)parentNode).id;
	}
	/** END ADDITION */

	protected void initAttributes() {
		// If the node is a container or has an invalid ID, return.
		if( (this instanceof DbContainerNode && nodeName != null ) || id == -1 || (parentId == -1 && nodeName != null) )
			return;
		disableTracking();
		
		DbInterface db = (DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF);
		// Unpack node
		SqlConnection conn = null;
		try {
			SqlDialect dialect = db.getDialect();
			conn = db.getConnection();

			// We need extra information from HIERARCHY
			if( parentId == -1 && nodeName == null ) {
				SelectQuery select = dialect.createSelect();
				Table th = new Table(rootNode.getApplication().getName()+"_HIERARCHY");
				select.addTable(th);
				select.addColumn(th.getColumn("PARENTID"));
				select.addColumn(th.getColumn("NODENAME"));
				select.addColumn(th.getColumn("SIZE"));
				select.addColumn(th.getColumn("METADATA"));
				select.addFilter(new Equals(th.getColumn("ID"),new Argument(id)));

				PreparedStatement stmt = select.prepareStatement(conn.getConnection());
				ResultSet rs = stmt.executeQuery();
				
				if( rs.next() ) {
					parentId = rs.getLong(1);
					nodeName = rootNode.getApplication().getString(rs.getInt(2));
					setSize(rs.getLong(3));
					deserialize(rs.getString(4), 0, this, this.id, rootNode.getApplication().getObject(nodeName));
					setAcl(getAttribute("_Acl"));
				}
				db.closeResultSet(rs);
			}
		} catch (SQLException e) {
			Logger.debug(e);
		} finally {
			if( conn != null )
				db.releaseConnection(conn);
		}

		enableTracking();
	}
	
	public void trackChange() {
		if( (nodeFlags & HAS_CHANGED) == 0 ) {
			nodeFlags |= HAS_CHANGED;
			/** BEGIN MODIFICATION
			 *		BY Hugo, 2008-01-18
			 *	Remove these comments if modifications are approved
			 */
			/*
			if( this.parentNode != null && !(((DbNode)this.parentNode) instanceof DbContainerNode) )
			*/		
			if( this.parentNode != null && !(this.parentNode instanceof DbContainerNode) )
			/** END MODIFICATION */
				this.parentNode.trackChange();
		}
	}
	
	public void setAcl(String acl) {
		this.aclList = AclToken.getTokens(acl);
	}
	
	public void setAcl(AclToken acl) {
		this.aclList = new ArrayList<AclToken>();
		this.aclList.add(acl);
	}
	
	public void setAcl(List<AclToken> acl) {
		this.aclList = acl;
	}
	
	//TODO handle removeChild() for changelogs and STREAM deletion
	//TODO remove unused images

	public void update() throws DOMException {
		if( !hasChanged() )
			return;
		if( id == -1 || parentId == -1 )
			return;
		if( !hasPermission(AclToken.ACL_UPDATE) )
			throw new DOMException(DOMException.VALIDATION_ERR, "@acl-violation-update");
		String newAcl = null;
		if( ((DynamicAttr) getAttributeNode("_Acl")).hasChanged() ) {
			if( !isItemOwner() )
				throw new DOMException(DOMException.VALIDATION_ERR, "@acl-violation-owner");
			newAcl = getAttribute("_Acl");
			List<AclToken> newAclList = AclToken.getTokens(newAcl);
			if( aclList.get(0).getUid() != newAclList.get(0).getUid() )
				throw new DOMException(DOMException.VALIDATION_ERR, "@acl-violation-owner");
			aclList = newAclList;
		}
		
		// Run events
		if( objectDefinition != null ) {
			if( !objectDefinition.handleEvent(ObjectDefinition.ON_UPDATE, this) )
				return;
			if( !objectDefinition.handleEvent(ObjectDefinition.ON_SAVE, this) )
				return;
		}

		boolean doCommit = false;
		try {
			// Create or use existing transaction
			Transaction transaction = Transaction.getActive();
			if( doCommit = (transaction == null) ) 
				transaction = Transaction.create();

			DbInterface db = (DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF);

			SqlConnection conn = transaction.getConnection();

			// Array for deleted streams
			List<InputStream> streams = new LinkedList<InputStream>();

			// Insert changelog
			Application application = rootNode.getApplication();
			Collection<ObjectField> schema = application.getAllObjectFields(nodeName, ObjectField.T_CHANGELOG);
			if( schema != null && schema.size() > 0 ) {
				boolean nodeChanged = true;
				DynamicElement log = new DynamicElement(parentDoc,"ChangeLog");
				for( ObjectField item : schema ) {
					boolean attrChanged = false;
					String attrValue = null;
					
					try {
						DynamicAttr attr = (DynamicAttr)item.getBind().evaluate(null, this, Expression.NODE);
						if( attr != null ) {
							if( (attrChanged = attr.hasChanged()) ) {
								if( attr instanceof DynamicAttrStream ) 
									attrValue = ((DynamicAttrStream)attr).peekValue(255);
								else {
									attrValue = attr.getValue();
									if( attrValue != null && attrValue.length() > 255 )
										attrValue = attrValue.substring(0, 254);
								}
							}
						}
					} catch( Exception _ ) {}
					
					if( attrChanged ) {
						DynamicElement entry = (DynamicElement) log.appendChild("Field");
						entry.setAttribute("Name", Integer.toString(application.getId(item.getId()), 36) );
						entry.setAttribute("Value", attrValue);
						if( !nodeChanged )
							nodeChanged = true;
					}
				}
				if( nodeChanged ) {
					StringBuffer buf = new StringBuffer();
					serialize(buf, conn, log, -1, true );

					InsertQuery insert = db.getDialect().createInsert(application.getName()+"_CHANGELOG");
					insert.setValue("ID", id);
					insert.setValue("CHANGEDON", ISO8601.formatUtc(new Date()));
					insert.setValue("CHANGEDBY", ((RootNode)parentDoc.getDocumentElement()).getUserName());
					insert.setValue("METADATA", new Argument(buf.toString()));
					db.executeInsert(conn.getConnection(), insert);
				}
			}

			// Insert new binaries
			long nodeSize = insertStreams(conn, rootNode.getApplication().getName(), id, streams, this, false);
			
			// Update node data
			setAttribute("_ModifiedBy", rootNode.getUserId());
			setAttribute("_ModifiedDate", ISO8601.formatUtc(new Date()));
			StringBuffer buf = new StringBuffer(2048);
			serialize(buf, conn, this, id, !(this instanceof DbContainerNode) );

			// Update hierarchy table
			UpdateQuery update = db.getDialect().createUpdate(rootNode.getApplication().getName()+"_HIERARCHY");
			if( newAcl != null )
				update.setValue("ACLREAD", getAclReaders());
			update.setValue("SIZE", new Argument(nodeSize + (long)buf.length()));
			update.setValue("METADATA", new Argument(buf.toString()));
			update.addFilter(new Equals(new Column("ID"), new Argument(id)));
			db.executeUpdate(conn.getConnection(), update);

			// Insert indexes
			schema = rootNode.getApplication().getAllObjectFields(nodeName, ObjectField.T_INDEX);
			if( schema != null ) {
				boolean nodeChanged = false;
				update = db.getDialect().createUpdate(rootNode.getApplication().getName()+"_IDX_"+nodeName.toUpperCase());
				for( ObjectField item : schema ) {
					DynamicAttr attr = (DynamicAttr)item.getBind().evaluate(null,this, Expression.NODE);
					if( attr != null && attr.hasChanged() ) {
						String value = attr.getValue();
						if( item.getFormat() == ObjectField.F_STRING && value.length() > db.getIndexSize() )
							value = value.substring(0, db.getIndexSize()-1);
						update.setValue(rootNode.getApplication().getColumnName(item), value );
						if( !nodeChanged )
							nodeChanged = true;
					}
					/*boolean attrChanged = false;
					NodeList list = (NodeList)item.getBind().evaluate(null,this, Expression.NODELIST);
					buf = new StringBuffer();
					for( int i = 0; i < list.getLength() - 1; i++ ) {
						DynamicAttr attr = (DynamicAttr) list.item(i);
						if( attr.hasChanged() && !attrChanged )
							attrChanged = true;
						if( attr.getValue() != null ) {
							if( buf.length() > 0 )
								buf.append(" ");
							buf.append(attr.getValue());
						}
					}

					if( attrChanged ) {
						String value = buf.toString();
						update.setValue(rootNode.getApplication().getColumnName(item), (db.getIndexSize()>value.length())?value.substring(0, db.getIndexSize()-1):value );
						if( !nodeChanged )
							nodeChanged = true;
					}*/
				}
				update.addFilter(new Equals(new Column("ID"), new Argument(id)));
				if( nodeChanged ) {
					db.executeUpdate(conn.getConnection(), update);
				}
			}
			
			// Delete previous streams
			if( streams.size() > 0 ) {
				Vector<BooleanExpression> vector = new Vector<BooleanExpression>();
				for( InputStream stream : streams )  {
					if( stream instanceof DbInputStream )
						vector.add(new Equals(new Column("STREAMID"),new Argument(((DbInputStream)stream).getId())));
				}
				if( vector.size() > 0 ) {
					DeleteQuery delete = db.getDialect().createDelete(rootNode.getApplication().getName()+"_NODE_STREAMS");
					delete.addFilter(new Or((BooleanExpression[]) vector.toArray(new BooleanExpression[vector.size()])));
					db.executeDelete(conn.getConnection(), delete);
				}
			}
			
			resetChanged();

			// Commit transaction
			if( doCommit )
				transaction.commit();

			// Delete received files
			if( streams.size() > 0 ) {
				for( InputStream stream : streams )  {
					if( stream instanceof ReceivedFile )
						((ReceivedFile)stream).delete();
				}
			}
		} catch(Throwable e) {
			// Rollback transaction
			if( doCommit )
				Transaction.getActive().rollback();

			// Set error
			DOMException error;
			if( e instanceof DOMException )
				error = (DOMException)e;
			else {
				error = new DOMException(DOMException.VALIDATION_ERR, "@update-failed");
				error.initCause(e);
			}
			throw error;
		}
	}
	
	private long insertStreams(SqlConnection conn, String appName, long nodeId, List<InputStream> previousStreams, DynamicElement node, boolean isInsert ) throws SQLException {
		long totalSize = 0;
		DbInterface db = (DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF);
		Iterator<Attr> attrIt = node.getAttributesCollection().iterator();
		while(attrIt.hasNext()) {
			DynamicAttr attr = (DynamicAttr)attrIt.next();
			if( !(attr instanceof DynamicAttrStream) )
				continue;
			InputStream inputStream = ((DynamicAttrStream)attr).getInputStream();
			if( inputStream != null ) {
				if( isInsert || attr.hasChanged() || !(inputStream instanceof DbInputStream)) {
					DbInputStream dis = DbInputStream.getDbInputStreamInstance(DbInputStream.TYPE_SQL, appName, nodeId, db.getUniqueId());
					SqlDbInputStream.InputStreamCounter isc = ((SqlDbInputStream)dis).new InputStreamCounter(inputStream);
					
					// Insert node and obtain id
					InsertQuery insert = db.getDialect().createInsert(appName+"_NODE_STREAMS");
					insert.setValue("STREAMID",dis.getId());
					insert.setValue("NODEID", nodeId);
					insert.setValue("CONTENTS", isc);
					db.executeInsert(conn.getConnection(), insert);
					dis.setSize(isc.getSize());
					((DynamicAttrStream)attr)._setInputStream(dis);
					totalSize += isc.getSize();

					// Store old streams if requested
					if( previousStreams != null && ((DynamicAttrStream)attr).getPreviousInputStream() != null )
						previousStreams.add((((DynamicAttrStream)attr).getPreviousInputStream()));
				
				} else if( inputStream instanceof DbInputStream )
					totalSize += ((DbInputStream)inputStream).getSize();
			}
				
		}
		
		if( node.getChildren() != null ) {
	 		Iterator<Node> it = node.getChildren().iterator();
			while(it.hasNext())
				totalSize += insertStreams(conn, appName, nodeId, previousStreams, (DynamicElement)it.next(), isInsert);
		}
		return totalSize;
	}
	
	
	protected long insert(SqlConnection conn, DynamicElement node, long parentId, List<InputStream> streams ) throws DOMException {
		DbInterface db = (DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF);
		long nodeId = -1;
		
		try {
			boolean isContainer = (node instanceof DbContainerNode) || node.getAttribute("_Container") != null;
			StringBuffer buf = new StringBuffer(2048);
			
			// Create internal vars
			node.setAttribute("_CreatedBy", rootNode.getUserId());
			node.setAttribute("_CreationDate", ISO8601.formatUtc(new Date()));
			node.setAttribute("_Acl", getAcl());
		
			// Insert streams
			long nodeSize = insertStreams(conn, rootNode.getApplication().getName(), id, streams, node, true);
			
			// Insert node and obtain id
			nodeId = db.getUniqueId();
			String nodeName = node.getNodeName();
			serialize(buf, conn, node, nodeId, !isContainer );

			// Update hierarchy table
			InsertQuery insert = db.getDialect().createInsert(rootNode.getApplication().getName()+"_HIERARCHY");
			insert.setValue("ID", nodeId);
			insert.setValue("PARENTID", parentId);
			insert.setValue("NODENAME", rootNode.getApplication().getId(node.getNodeName()));
			insert.setValue("ACLREAD", getAclReaders());
			insert.setValue("SIZE", nodeSize + (long)buf.length() );
			insert.setValue("ISCONTAINER", (isContainer)?"1":"0");
			insert.setValue("METADATA", new Argument(buf.toString()));
			db.executeInsert(conn.getConnection(), insert);
			
			// Insert changelog
			Collection<ObjectField> schema = rootNode.getApplication().getAllObjectFields(nodeName, ObjectField.T_CHANGELOG);
			if( schema != null ) {
				DynamicElement log = new DynamicElement(parentDoc,"ChangeLog");
				node.setAttribute("_Revision", 1);
				Application application = rootNode.getApplication();
				for( ObjectField item : schema ) {
					String attrValue = null;
					
					try {
						DynamicAttr attr = (DynamicAttr)item.getBind().evaluate(null, node, Expression.NODE);
						if( attr != null ) {
							if( attr instanceof DynamicAttrStream ) 
								attrValue = ((DynamicAttrStream)attr).peekValue(255);
							else {
								attrValue = attr.getExpandedValue();
								if( attrValue != null && attrValue.length() > 255 )
									attrValue = attrValue.substring(0, 254);
							}
						}
					} catch( Exception e ) {
						Logger.debug(e);
					}

					// If the value is null, skip it
					if( attrValue == null || attrValue.isEmpty() )
						continue;
					
					DynamicElement entry = (DynamicElement) log.appendChild("Field");
					entry.setAttribute("Name", Integer.toString(application.getId(item.getId()), 36));
					entry.setAttribute("Value", attrValue);
				}
				StringBuffer lbuf = new StringBuffer(1024);
				serialize(lbuf, conn, log, -1, true );

				insert = db.getDialect().createInsert(application.getName()+"_CHANGELOG");
				insert.setValue("ID", nodeId);
				insert.setValue("CHANGEDON", ISO8601.formatUtc(new Date()));
				insert.setValue("CHANGEDBY", ((RootNode)parentDoc.getDocumentElement()).getUserName());
				insert.setValue("METADATA", new Argument(lbuf.toString()));
				db.executeInsert(conn.getConnection(), insert);
			}
			
			// Update quota
			if( db.getTotalQuota(rootNode, getOwnerId()) > 0 )
				db.updateUsedQuota(getOwnerId(), nodeSize);
			
			// Insert indexes
			schema = rootNode.getApplication().getAllObjectFields(nodeName, ObjectField.T_INDEX);
			if( schema != null ) {
				insert = db.getDialect().createInsert(rootNode.getApplication().getName()+"_IDX_"+nodeName.toUpperCase());
				insert.setValue("ID", nodeId);
				for( ObjectField item : schema ) {
					try {
						String val = item.getBind().evaluate(null,node);
						if( val.length() > db.getIndexSize() )
							val = val.substring(0, db.getIndexSize() - 1);
						if( val.isEmpty() )
							val = null;
						insert.setValue(rootNode.getApplication().getColumnName(item), val);
					} catch( Exception _ ) {}
				}
				db.executeInsert(conn.getConnection(), insert);
			}
			
			// If it is a container, insert its children
			if( isContainer && node.hasChildNodes() ) {
				for( Node item : node.getChildren() ) {
					if( !item.getNodeName().startsWith("_") )
						insert( conn, (DynamicElement)item, nodeId, streams );
				}
			}
			
		} catch (Exception e) {
			DOMException error;
			if( e instanceof DOMException )
				error = (DOMException)e;
			else {
				error = new DOMException(DOMException.VALIDATION_ERR, "@insert-failed");
				error.initCause(e);
			}
			throw error;
		}
		return nodeId;
	}
	
	public List<Node> getChangeLog() {
		ArrayList<Node> result = new ArrayList<Node>();

		// Check if changelogs are enabled for this node
		Collection<ObjectField> schema = rootNode.getApplication().getAllObjectFields(nodeName, ObjectField.T_CHANGELOG);
		if( schema == null && schema.size() > 0 )
			return result;
		
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlConnection conn = null;
		
		try {
			SqlDialect dialect = db.getDialect();
			conn = db.getConnection();

			Application application = rootNode.getApplication();
			SelectQuery select = dialect.createSelect();
			Table th = new Table(application.getName()+"_CHANGELOG");
			select.addTable(th);
			select.addColumn(th.getColumn("METADATA"));
			select.addColumn(th.getColumn("CHANGEDBY"));
			select.addColumn(th.getColumn("CHANGEDON"));
			select.addFilter(new Equals(th.getColumn("ID"),new Argument(this.id)));
			select.addSorting(th.getColumn("CHANGEDON"), true);
			PreparedStatement stmt = select.prepareStatement(conn.getConnection());
			ResultSet rs = stmt.executeQuery();
			
			while( rs.next() ) {
				DynamicElement changesNode = new DynamicElement(this.parentDoc, "Changes");
				deserialize(rs.getString(1), 0, changesNode, -1, null);
				String changedBy = rs.getString(2), changedOn = rs.getString(3);
				if( changesNode.getChildren() != null ) {
					for( Node node : changesNode.getChildren() ) {
						String changedField = application.getString(((DynamicElement)node).getAttribute("Name"));
						String newValue = ((DynamicElement)node).getAttribute("Value");
						
						DynamicElement changeLog = new DynamicElement(this.parentDoc, "ChangeLog");
						
						changeLog.setAttribute("ChangedBy", changedBy);
						changeLog.setAttributeNode(new DynamicAttrDate(changeLog, "ChangedOn", changedOn));
						for( ObjectField field : schema ) {
							if( field.getId().equals(changedField) ) {
								try {
									changeLog.setAttribute("Name", field.getName().evaluate(null, this.parentDoc));
									changeLog.setAttribute("Value", this.objectDefinition.buildField(this, field, newValue).getExpandedValue());
								} catch (Exception e) {
									Logger.debug(e);
								}
							}
						}
						result.add(changeLog);
					}
				}
			}
			
			db.closeResultSet(rs);
			db.releaseConnection(conn);
			conn = null;
			
		} catch (SQLException e) {
			Logger.debug(e);
		} finally {
			if( conn != null )
				db.releaseConnection(conn);
		}
		return result;
	}
	
	protected boolean isContainer(Node node) {
		return( ((node instanceof DbContainerNode) ) || ((DynamicElement)node).getAttribute("_Container") != null );
	}
	
	public void appendChild(List<Node> list) throws DOMException {
		Iterator<Node> it = list.iterator();
		while( it.hasNext() )
			appendChild(it.next());
	}
	
	public void delete() throws DOMException {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}

		// Run events
		if( objectDefinition != null ) {
			if( !objectDefinition.handleEvent(ObjectDefinition.ON_DELETE, this) )
				return;
		}

		boolean doCommit = false;
		try {
			// Create or use existing transaction
			Transaction transaction = Transaction.getActive();
			if( doCommit = (transaction == null) ) 
				transaction = Transaction.create();

			delete(transaction.getConnection(), true);
			DbInterface db = (DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF);
			if( db.getTotalQuota(rootNode, getOwnerId()) > 0 )
				db.updateUsedQuota(getOwnerId(), -1);
			
			// Commit transaction
			if( doCommit )
				transaction.commit();
		} catch(Throwable e) {
			// Rollback transaction
			if( doCommit )
				Transaction.getActive().rollback();

			// Set error
			DOMException error;
			if( e instanceof DOMException )
				error = (DOMException)e;
			else {
				error = new DOMException(DOMException.VALIDATION_ERR, "@delete-failed");
				error.initCause(e);
			}
			throw error;
		}
	}
	
	protected abstract void delete(SqlConnection conn, boolean checkAcl) throws DOMException, SQLException;

	protected void delete(SqlConnection conn) throws SQLException {
		DeleteQuery delete;
		DbInterface db = (DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF);
		
		// Delete hierarchy
		delete = db.getDialect().createDelete(rootNode.getApplication().getName()+"_HIERARCHY");
		delete.addFilter(new Equals(new Column("ID"), new Argument(id)));
		db.executeDelete(conn.getConnection(), delete);

		// Delete indexes
		Collection<ObjectField> schema = rootNode.getApplication().getAllObjectFields(nodeName, ObjectField.T_INDEX);
		if( schema != null ) {
			delete = db.getDialect().createDelete(rootNode.getApplication().getName()+"_IDX_"+nodeName.toUpperCase());
			delete.addFilter(new Equals(new Column("ID"), new Argument(id)));
			db.executeDelete(conn.getConnection(), delete);
		}
		
		// Delete changelog
		schema = rootNode.getApplication().getAllObjectFields(nodeName, ObjectField.T_CHANGELOG);
		if( schema != null ) {
			delete = db.getDialect().createDelete(rootNode.getApplication().getName()+"_CHANGELOG");
			delete.addFilter(new Equals(new Column("ID"), new Argument(id)));
			db.executeDelete(conn.getConnection(), delete);
		}
		
		// Delete streams
		delete = db.getDialect().createDelete(rootNode.getApplication().getName()+"_NODE_STREAMS");
		delete.addFilter(new Equals(new Column("NODEID"), new Argument(id)));
		db.executeDelete(conn.getConnection(), delete);

		// Delete links
		delete = db.getDialect().createDelete(rootNode.getApplication().getName()+"_LINK");
		delete.addFilter(new Or(new BooleanExpression[] {new Equals(new Column("SOURCEID"), new Argument(id)), new Equals(new Column("TARGETID"), new Argument(id))}) );
		db.executeDelete(conn.getConnection(), delete);
	}
	
	
	
	// Returns true is the current user or his/her group owns the item
	public boolean isItemOwner() {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		return AclToken.checkAcl(AclToken.ACL_OWNER, rootNode.getUidTokens(), aclList);
	}

	public boolean hasPermission(int operation) {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		return AclToken.checkAcl(operation, rootNode.getUidTokens(), aclList);
	}
	
	//public boolean hasResursivePermission(int operation) {
	//	return( isItemOwner() || hasPermission(operation));
	//}
	
	public long getOwnerId() {
		return aclList.get(0).getUid();
	}
	
	public AclToken getOwnerToken() {
		return aclList.get(0);
	}

	public String getAclReaders() {
		StringBuffer result = new StringBuffer();
		Iterator<AclToken> it = aclList.iterator();
		while( it.hasNext() ) {
			AclToken item = it.next();
			if( item.getLevel() == AclToken.ACL_OWNER || item.getLevel() == AclToken.ACL_READ )
				result.append("_").append(item.getUidString());
		}
		result.append("_");
		
		return result.toString();
	}
	
	public String getAcl() {
		return AclToken.getAclString(aclList);
	}
	
	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
	protected void serialize(StringBuffer result, SqlConnection conn, DynamicElement node, long nodeId, boolean includeChildren ) throws Exception {
		Collection<Attr> attributes = node.getAttributesCollection();
		Iterator<Attr> it = attributes.iterator();
		boolean isRootNode = result.length() == 0;
		boolean firstEntry = !it.hasNext();
		Application dbif = rootNode.getApplication();
		
		if( !isRootNode )
			result.append("{");
		while( it.hasNext() ) {
			DynamicAttr item = (DynamicAttr)it.next();
			if( item instanceof DynamicAttrVirtual )
				continue;
			Object value = item.getObjectValue();
			result.append(Integer.toString(dbif.getId(item.getName()), 36));
			result.append(":");
			if( value == null ) {
				result.append("n");
			} else if( value instanceof DbInputStream ) {
				result.append("b");
				result.append(Long.toString(((DbInputStream)value).getId(), 36));
				result.append(".");
				result.append(Long.toString(((DbInputStream)value).getSize(), 36));
			} else if( value instanceof String ){
				boolean isDigit = true;
				for( int i = 0; i < ((String)value).length() && isDigit; i++ ) {
					if( !Character.isDigit(((String)value).charAt(i)) )
						isDigit = false;
				}
				if( ((String)value).isEmpty() || !isDigit ) {
					result.append("\"");
					for( int i = 0; i < ((String)value).length(); i++ ) {
						char c = ((String)value).charAt(i);
						if( c == '"' || c == '\\' )
							result.append("\\");
						result.append(c);
					}
					result.append("\"");
				} else
					result.append(String.valueOf(Long.parseLong(((String)value))));
			} else {
				result.append("n");
				Logger.warn("Unknown type [" + value + "].");
			}
			if( it.hasNext() )
				result.append(",");
		}
		List<Node> children = node.getChildren();
		if( children != null ) {
			for( Node child : children ) {
				//if( child instanceof DbLinknNode )
				//	((DbLinknNode)child).flush(conn, nodeId);
				//else 
				if( !(child instanceof VirtualElement) ){
					String childName = child.getNodeName();
					if( includeChildren || childName.startsWith("_") ) {
						if( firstEntry )
							firstEntry = false;
						else
							result.append(",");
						result.append(Integer.toString(dbif.getId(childName), 36));
						result.append(":");
						serialize(result, conn, (DynamicElement)child, nodeId, true );
					}
				}
			}
		}
		
		if( !isRootNode )
			result.append("}");
	}
	
	private DynamicAttr deserializeAttribute(ObjectDefinition objDef, DynamicElement node, String attrName, Object attrValue ) {
		if( objDef != null )
			return objDef.buildField(node, attrName, attrValue);
		else if( attrValue == null || attrValue instanceof String )
			return new DynamicAttrString(node, attrName, (String)attrValue);
		else if( attrValue instanceof InputStream )
			return new DynamicAttrStream(node, attrName, (InputStream)attrValue);
		else
			return null;
	}
	
	public int deserialize(String data, int startIdx, DynamicElement node, long nodeId, ObjectDefinition objDef ) {
		final int IN_KEY = 0, IN_QUOTE = 1, IN_DATA = 2, NEED_VALUE = 3, GOT_FIELD = 4;
		int i = startIdx, status = IN_KEY, endIdx = data.length();
		StringBuffer buf = new StringBuffer();
		String nodeName = null;
		Application dbif = rootNode.getApplication();
		
		while( i < endIdx ) {
			char c = data.charAt(i++);
			
			if( status == IN_KEY ) {
				if( c == ':' ) {
					nodeName = dbif.getString(Integer.parseInt(buf.toString(), 36));
					buf = new StringBuffer();
					status = NEED_VALUE;
				} else if( c == '}' )
					break;
				else
					buf.append(c);
			} else if( status == IN_QUOTE ) {
				if( c == '"' ) {
					node.setAttributeNode(deserializeAttribute(objDef, node, nodeName, buf.toString()));
					status = GOT_FIELD;
				} else {
					if( c == '\\' )
						c = data.charAt(i++);
					buf.append(c);
				}
			} else if( status == IN_DATA ) {
				if( c == ',' || c == '}' || i == endIdx ) {
					String value = buf.toString();
					if( value.charAt(0) == 'b' ) {
						String[] parts = value.substring(1).split("\\.");
						DbInputStream dbis = DbInputStream.getDbInputStreamInstance(DbInputStream.TYPE_SQL, dbif.getName(), nodeId, Long.parseLong(parts[0], 36));
						dbis.setSize(Long.parseLong(parts[1], 36));
						node.setAttributeNode(deserializeAttribute(objDef, node, nodeName, dbis));
					} else
						node.setAttributeNode(deserializeAttribute(objDef, node, nodeName, buf.toString()));
					status = GOT_FIELD;
				} else
					buf.append(c);
			} else if( status == NEED_VALUE ) {
				if( c == '"' ) {
					status = IN_QUOTE;
				} else if (c == '{' ) {
					DynamicElement child = (DynamicElement)node._appendChild(nodeName);
					child.disableTracking();
					i += deserialize(data, i, child, nodeId, (objDef!=null)?objDef.getObject(nodeName):dbif.getObject(nodeName));
					child.enableTracking();
					status = GOT_FIELD;
				} else if( c == 'n' ) {
					node.setAttributeNode(deserializeAttribute(objDef, node, nodeName, null));
					status = GOT_FIELD;
				} else {
					status = IN_DATA;
					buf.append(c);
					if( i == endIdx )
						node.setAttributeNode(deserializeAttribute(objDef, node, nodeName, buf.toString()));
				}
			}
			
			if( status == GOT_FIELD && i < endIdx ) {
				if( c != '}' && c != ',' )
					c = data.charAt(i++);
				if( c == '}' )
					break;
					//return i - startIdx;
				else if( c == ',' ) {
					nodeName = null;
					buf = new StringBuffer();
					status = IN_KEY;
				}
			}
		}
		
		// Add missing fields
		if( objDef != null ) {
			objDef.addFields(node, true);
			node.setObjectDefinition(objDef);
		}
		
		return i - startIdx;
		
	}

	
	public BooleanExpression getAclRead(Column aclColumn) {
		List<UidToken> tokens = ((RootNode)parentDoc.getDocumentElement()).getUidTokens();
		Iterator<UidToken> it = tokens.iterator();
		ArrayList<BooleanExpression> exp = new ArrayList<BooleanExpression>(tokens.size());
		while(it.hasNext()) {
			UidToken uid = it.next();
			exp.add(new Like(aclColumn, "%_"+uid.getUidString()+"_%"));
		}
		return new Or(exp.toArray(new BooleanExpression[exp.size()]));
	}

	public boolean equals(Object obj) {
		return (obj != null && obj instanceof DbNode && ((DbNode)obj).getId() == this.id );
	}
	
	protected class AttrId extends DynamicAttrVirtual {

		public AttrId(DynamicElement parentNode) {
			super(parentNode, "_Id");
		}

		public String getExpandedValue() {
			return getValue();
		}

		public String getValue() {
			return (((DbNode)parentNode).getId() > -1) ? Long.toString(((DbNode)parentNode).getId(), 36) : "";
		}

		public void setValue(String value) throws DOMException {
			// Ignored
		}
		
	}

}
