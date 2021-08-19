package com.codeglide.interfaces.messages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.core.Logger;
import com.codeglide.core.ServerSettings;
import com.codeglide.core.rte.session.SessionHook;
import com.codeglide.interfaces.root.RootNode;
import com.codeglide.interfaces.xmldb.DbInterface;
import com.codeglide.interfaces.xmldb.SqlConnectionPool.SqlConnection;
import com.codeglide.interfaces.xmldb.sql.Argument;
import com.codeglide.interfaces.xmldb.sql.Column;
import com.codeglide.interfaces.xmldb.sql.DeleteQuery;
import com.codeglide.interfaces.xmldb.sql.Equals;
import com.codeglide.interfaces.xmldb.sql.InsertQuery;
import com.codeglide.interfaces.xmldb.sql.SelectQuery;
import com.codeglide.interfaces.xmldb.sql.SqlDialect;
import com.codeglide.interfaces.xmldb.sql.Table;
import com.codeglide.util.json.Json2DynamicElement;
import com.codeglide.xml.dom.DynamicElement;

public class MessagesNode extends DynamicElement implements SessionHook {
	private long currentUid = -1;
	
	private static HashMap<String, MessagesNode> messagePipe = new HashMap<String, MessagesNode>();
	
	public MessagesNode(Document parentDoc) {
		super(parentDoc, "MessageChannel");
		this.nodeFlags |= NEEDS_CHILD_INIT;
	}
	
	protected void initChildren() {
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlConnection conn = null;
		try {
			// Retrieve offline messages
			SqlDialect dialect = db.getDialect();
			conn = db.getConnection();
			RootNode rootNode = (RootNode)parentDoc.getDocumentElement();

			SelectQuery select = dialect.createSelect();
			Table tn = new Table(rootNode.getApplication().getName()+"_MESSAGES");
			select.addTable(tn);
			select.addColumn(tn.getColumn("METADATA"));
			select.addFilter(new Equals(tn.getColumn("ID"),new Argument(currentUid)));
			PreparedStatement stmt = select.prepareStatement(conn.getConnection());
			ResultSet rs = stmt.executeQuery();
			
			if( rs.next() ) {
				try {
					Json2DynamicElement.importNodes(this, rs.getString(1));
				} catch (Exception _) {
				}
			}
			
			db.closeResultSet(rs);
			
			// Delete received offline messages
			DeleteQuery delete = db.getDialect().createDelete(rootNode.getApplication().getName()+"_NODE_STREAMS");
			delete.addFilter(new Equals(new Column("ID"), new Argument(currentUid)));
			db.executeDelete(conn.getConnection(), delete);

		} catch (SQLException e) {
			Logger.debug(e);
		} finally {
			if( conn != null )
				db.releaseConnection(conn);
		}
	}

	public Node appendChild(Node arg0) throws DOMException {
		DynamicElement message = (DynamicElement)arg0;

		String recipient;
		long recipientId;
		
		if( message == null || (recipient = message.getAttribute("To")) == null || recipient.isEmpty() || (recipientId = Long.parseLong(recipient, 36)) < 0 )
			throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@invalid-recipient");
		
		// Obtain full recipient address
		recipient = getRecipientId(recipientId);
		
		// Check first if the user is logged on
		synchronized (messagePipe) {
			if( messagePipe.containsKey(recipient) ) {
				MessagesNode messagesNode = messagePipe.get(recipient);
				messagesNode._appendChild(((DynamicElement)arg0).cloneNode(messagesNode.getDocumentNode(), true));
				return arg0;
			}
		}
		
		List<Node> messages = new LinkedList<Node>();
		messages.add(message);
		
		saveOfflineMessages(recipientId, messages);
		
		return arg0;
	}


	public Node appendChild(String name) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "@not-supported");
	}
	
	private void saveOfflineMessages(long recipientUid, List<Node> messages ) {
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlConnection conn = null;
		try {
			conn = db.getConnection();

			// Update hierarchy table
			InsertQuery insert = db.getDialect().createInsert(((RootNode)parentDoc.getDocumentElement()).getApplication().getName()+"_MESSAGES");
			insert.setValue("ID", recipientUid);
			insert.setValue("METADATA", Json2DynamicElement.exportNodes(messages));
			db.executeInsert(conn.getConnection(), insert);
			conn.commit();
		} catch (SQLException e) {
			if( conn != null ) {
				try {
					conn.rollback();
				} catch (SQLException _) {}
			}
		} finally {
			if( conn != null )
				db.releaseConnection(conn);
		}
		
	}
	
	private String getRecipientId(long id) {
		RootNode rootNode = (RootNode) parentDoc.getDocumentElement();
		return rootNode.getApplication().getName() + rootNode.getSiteId() + Long.toString(id, 36);
	}

	public void onSessionStart() {
		currentUid = ((RootNode)parentDoc.getDocumentElement()).getUserId();
		synchronized( messagePipe ) {
			messagePipe.put(getRecipientId(currentUid), this);
		}
	}
	
	public void onSessionEnd() {
		synchronized (messagePipe) {
			messagePipe.remove(getRecipientId(currentUid));
		}
		if( children != null && children.size() > 0 ) 
			saveOfflineMessages(currentUid, children);
	}
	
}
