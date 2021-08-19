package com.codeglide.interfaces.xmldb;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.ServerSettings;
import com.codeglide.core.objects.ObjectDefinition;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.Application;
import com.codeglide.interfaces.LeavesNode;
import com.codeglide.interfaces.SearchableNode;
import com.codeglide.interfaces.root.RootNode;
import com.codeglide.interfaces.xmldb.sql.And;
import com.codeglide.interfaces.xmldb.sql.Argument;
import com.codeglide.interfaces.xmldb.sql.BooleanExpression;
import com.codeglide.interfaces.xmldb.sql.Column;
import com.codeglide.interfaces.xmldb.sql.DeleteQuery;
import com.codeglide.interfaces.xmldb.sql.Equals;
import com.codeglide.interfaces.xmldb.sql.InsertQuery;
import com.codeglide.interfaces.xmldb.sql.Or;
import com.codeglide.interfaces.xmldb.sql.SelectQuery;
import com.codeglide.interfaces.xmldb.sql.SqlDialect;
import com.codeglide.interfaces.xmldb.sql.Table;
import com.codeglide.xml.dom.DynamicAttrLink;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.dom.VirtualElement;

public class DbLinknNode extends VirtualElement implements SearchableNode, LeavesNode {
	private ObjectField targetField = null, fieldDef = null;
	private ObjectDefinition targetObject = null;
	private long parentId = -1;
	RootNode rootNode;
	Application application;

	public DbLinknNode(Document parentDoc, String nodeName, ObjectField fieldDef ) {
		super(parentDoc, nodeName);
		this.targetObject = (ObjectDefinition) fieldDef.getSetting(ObjectField.E_LINK_OBJECT);
		this.targetField = (ObjectField) fieldDef.getSetting(ObjectField.E_LINK_FIELD);
		this.fieldDef = fieldDef;
		
		rootNode = (RootNode)parentDoc.getDocumentElement();
		application = rootNode.getApplication();

		// We need to initialize before using this DbLinknNode
		nodeFlags |= NEEDS_CHILD_INIT;
	}
	
	protected void initChildren() {
		DynamicElement parentNode = (DynamicElement)this.getParentNode();
		while( parentNode != null && !(parentNode instanceof DbLeafNode) )
			parentNode = (DynamicElement)parentNode.getParentNode();
		if( parentNode != null )
			parentId = ((DbLeafNode)parentNode).getId();
	}
	
	public Node appendChild(Node arg0) throws DOMException {
		if( (nodeFlags & NEEDS_CHILD_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_CHILD_INIT;
			initChildren();
		}
		if( arg0 == null || parentId == -1 )
			return arg0;
		else if( arg0 instanceof DbLeafNode ) {
			DbInterface db = (DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF);
			RootNode rootNode = (RootNode)parentDoc.getDocumentElement();
			Application application = rootNode.getApplication();

			if( targetField.getFormat() == ObjectField.F_LINK_N ) {
				InsertQuery insert = db.getDialect().createInsert(application.getName()+"_LINK");
				insert.setValue("SOURCEID", parentId);
				insert.setValue("TARGETID", ((DbNode)arg0).getId());
				insert.setValue("TYPE", new Argument(application.getId(fieldDef.getId())) );
				Transaction transaction = null;
				try {
					transaction = new Transaction();
					db.executeInsert(transaction.getConnection().getConnection(), insert);
					transaction.commit();
				} catch (Throwable e) {
					Logger.debug(e);
					try {
						transaction.rollback();
					} catch (Exception _) {
					}
				}
			} else if( targetField.getFormat() == ObjectField.F_LINK ) {
				try {
					((DynamicAttrLink)targetField.getBind().evaluate(null, ((DbNode)arg0), Expression.NODE)).setValue(String.valueOf(parentId));
					((DbNode)arg0).update();
				} catch (Exception e) {
					Logger.debug(e);
				}
			}
			return arg0;
		} else if( arg0.getNodeName().startsWith("_") )
			return super.appendChild(arg0);
		else
			throw new DOMException(DOMException.VALIDATION_ERR, "@not-supported");
	}
	
	public Node removeChild(Node arg0) throws DOMException {
		if( (nodeFlags & NEEDS_CHILD_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_CHILD_INIT;
			initChildren();
		}
		if( arg0 instanceof DbLeafNode && parentId != -1 ) {
			DbInterface db = (DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF);
			RootNode rootNode = (RootNode)parentDoc.getDocumentElement();
			Application application = rootNode.getApplication();

			if( targetField.getFormat() == ObjectField.F_LINK_N ) {
				DeleteQuery delete = db.getDialect().createDelete(application.getName()+"_LINK");
				And term1 = null, term2 = null;
				ArrayList<BooleanExpression> terms = new ArrayList<BooleanExpression>();
				terms.add(new Equals(new Column("TYPE"), new Argument(application.getId(fieldDef.getId()))));
				terms.add(new Equals(new Column("SOURCEID"), new Argument(parentId)));
				terms.add(new Equals(new Column("TARGETID"), new Argument(((DbNode)arg0).getId())));
				term1 = new And((BooleanExpression[]) terms.toArray(new BooleanExpression[terms.size()]));

				terms = new ArrayList<BooleanExpression>();
				terms.add(new Equals(new Column("TYPE"), new Argument(application.getId(targetField.getId()))));
				terms.add(new Equals(new Column("TARGETID"), new Argument(parentId)));
				terms.add(new Equals(new Column("SOURCEID"), new Argument(((DbNode)arg0).getId())));
				term2 = new And((BooleanExpression[]) terms.toArray(new BooleanExpression[terms.size()]));

				delete.addFilter(new Or(new BooleanExpression[] {term1, term2}) );
				Transaction transaction = null;
				try {
					transaction = new Transaction();
					db.executeDelete(transaction.getConnection().getConnection(), delete);
					transaction.commit();
				} catch (Throwable e) {
					Logger.debug(e);
					try {
						transaction.rollback();
					} catch (Exception _) {
					}
				}
			} else if( targetField.getFormat() == ObjectField.F_LINK ) {
				try {
					((DynamicAttrLink)targetField.getBind().evaluate(null, ((DbNode)arg0), Expression.NODE)).setValue("");
					((DbNode)arg0).update();
				} catch (Exception e) {
					Logger.debug(e);
				}
			}			
		}
		return arg0;
	}
	
	public Node removeChild(String name) throws DOMException {
		throw new DOMException(DOMException.VALIDATION_ERR, "@not-supported");
	}
	
	protected void addLinkFilter(SelectQuery select, Table th, Table ti) {
		if( targetField.getFormat() == ObjectField.F_LINK_N ) {
			Table tl = new Table(application.getName()+"_LINK");
			select.addTable(tl);
			And term1 = null, term2 = null;
			ArrayList<BooleanExpression> terms = new ArrayList<BooleanExpression>();
			terms.add(new Equals(tl.getColumn("TYPE"), new Argument(application.getId(fieldDef.getId()))));
			terms.add(new Equals(tl.getColumn("SOURCEID"), new Argument(parentId)));
			terms.add(new Equals(tl.getColumn("TARGETID"), th.getColumn("ID")));
			term1 = new And((BooleanExpression[]) terms.toArray(new BooleanExpression[terms.size()]));

			terms = new ArrayList<BooleanExpression>();
			terms.add(new Equals(tl.getColumn("TYPE"), new Argument(application.getId(targetField.getId()))));
			terms.add(new Equals(tl.getColumn("TARGETID"), new Argument(parentId)));
			terms.add(new Equals(tl.getColumn("SOURCEID"), th.getColumn("ID")));
			term2 = new And((BooleanExpression[]) terms.toArray(new BooleanExpression[terms.size()]));
			
			select.addFilter(new Or(new BooleanExpression[]{term1, term2}));
		} else if( targetField.getFormat() == ObjectField.F_LINK ) {
			Table tl = ti;
			if( tl == null ) {
				tl = new Table(rootNode.getApplication().getName()+"_IDX_"+targetObject.getId().toUpperCase());
				select.addTable(tl);
			}
			select.addFilter(new Equals(tl.getColumn(rootNode.getApplication().getColumnName(targetField)), new Argument(parentId)));
			select.addFilter(new Equals(th.getColumn("ID"), tl.getColumn("ID")));
		}
	}

	public List<Node> getLeaves() {
		if( (nodeFlags & NEEDS_CHILD_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_CHILD_INIT;
			initChildren();
		}
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlDialect dialect = db.getDialect();
		SelectQuery select = dialect.createSelect();
		Table th = new Table(rootNode.getApplication().getName()+"_HIERARCHY");
		select.addTable(th);
		addLinkFilter(select, th, null);
		return db.getLeaves(select, th, null, null, (DbNode)parentNode, null, 0, 0);
	}

	public List<Node> getLeaves(String nodeName, String sortBy, int rangeStart, int rangeEnd) {
		if( (nodeFlags & NEEDS_CHILD_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_CHILD_INIT;
			initChildren();
		}
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlDialect dialect = db.getDialect();
		SelectQuery select = dialect.createSelect();
		Table th = new Table(rootNode.getApplication().getName()+"_HIERARCHY");
		Table ts = new Table(rootNode.getApplication().getName()+"_IDX_"+targetObject.getId().toUpperCase());
		select.addTable(th);
		select.addTable(ts);
		
		// Add link filter
		addLinkFilter(select, th, ts);
		
		return db.getLeaves(select, th, ts, targetObject, (DbNode)parentNode, sortBy, rangeStart, rangeEnd);
	}

	public List<Node> search(DynamicElement searchObject, String sortBy, int rangeStart, int rangeEnd) {
		if( (nodeFlags & NEEDS_CHILD_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_CHILD_INIT;
			initChildren();
		}
		
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlDialect dialect = db.getDialect();
		SelectQuery select = dialect.createSelect();
		Table th = new Table(rootNode.getApplication().getName()+"_HIERARCHY");
		Table ts = new Table(rootNode.getApplication().getName()+"_IDX_"+targetObject.getId().toUpperCase());
		select.addTable(th);
		select.addTable(ts);
		
		// Add link filter
		addLinkFilter(select, th, ts);
		
		// Add search filter
		((DbRootNode)rootNode.getChildNode("Db")).addSearchFilter(select, th, ts, targetObject, searchObject);
		
		return db.getLeaves(select, th, ts, targetObject, (DbNode)parentNode, sortBy, rangeStart, rangeEnd);
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	


}
