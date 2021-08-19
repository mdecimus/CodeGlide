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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.core.Logger;
import com.codeglide.core.ServerSettings;
import com.codeglide.core.acl.AclToken;
import com.codeglide.core.acl.UidToken;
import com.codeglide.core.objects.ObjectDefinition;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.interfaces.xmldb.SqlConnectionPool.SqlConnection;
import com.codeglide.interfaces.xmldb.sql.And;
import com.codeglide.interfaces.xmldb.sql.Argument;
import com.codeglide.interfaces.xmldb.sql.BooleanExpression;
import com.codeglide.interfaces.xmldb.sql.Equals;
import com.codeglide.interfaces.xmldb.sql.GreaterOrLess;
import com.codeglide.interfaces.xmldb.sql.Like;
import com.codeglide.interfaces.xmldb.sql.Not;
import com.codeglide.interfaces.xmldb.sql.Or;
import com.codeglide.interfaces.xmldb.sql.SelectQuery;
import com.codeglide.interfaces.xmldb.sql.SqlDialect;
import com.codeglide.interfaces.xmldb.sql.Table;
import com.codeglide.util.StringUtil;
import com.codeglide.xml.dom.DynamicElement;


public class DbRootNode extends DbContainerNode {
	public DbRootNode(Document document) {
		super(document, "Db");
		this.id = -1;
		this.parentId = -1;
		nodeFlags |= NEEDS_CHILD_INIT;
	}
	
	protected void initChildren() {
		//TODO refresh folder structure every X minutes
		this.children = getFolders(false);
	}

	public List<Node> getFolders( boolean autoExpand ) {
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlConnection conn = null;
		ArrayList<Node> result = null;
		
		try {
			SqlDialect dialect = db.getDialect();
			conn = db.getConnection();

			SelectQuery select = dialect.createSelect();
			Table th = new Table(rootNode.getApplication().getName()+"_HIERARCHY");
			select.addTable(th);
			select.addColumn(th.getColumn("ID"));
			select.addColumn(th.getColumn("PARENTID"));
			select.addColumn(th.getColumn("NODENAME"));
			select.addColumn(th.getColumn("METADATA"));
			select.addFilter(getAclRead(th.getColumn("ACLREAD")));
			select.addFilter(new Equals(th.getColumn("ISCONTAINER"),new Argument(1)));
			PreparedStatement stmt = select.prepareStatement(conn.getConnection());
			ResultSet rs = stmt.executeQuery();
			
			// Count number of records
			rs.last();
			HashMap<Long, DbNode> nodeMap = new HashMap<Long, DbNode>(rs.getRow());
			rs.beforeFirst();

			while( rs.next() ) {
				DbNode node = new DbContainerNode(parentDoc, rs.getLong(1), rs.getLong(2), rs.getInt(3));
				if( !autoExpand ) {
					node.removeFlag(DynamicElement.NEEDS_CHILD_INIT);
					node.setFlag(DynamicElement.EXPAND_ONLY_CONTAINERS);
				}
				node.removeFlag(DynamicElement.NEEDS_ATTR_INIT);
				node.disableTracking();
				deserialize(rs.getString(4), 0, node, node.getId(), rootNode.getApplication().getObject(node.getNodeName()));
				node.setAcl(node.getAttribute("_Acl"));
				node.enableTracking();
				nodeMap.put(node.getId(), node);
			}
			db.closeResultSet(rs);
			db.releaseConnection(conn);
			conn = null;

			// Create tree using a NodeMap helper
			Iterator<DbNode> nodeIt = nodeMap.values().iterator();
			int userFolderId = rootNode.getApplication().getId("UserFolder"), 
				groupFolderId = rootNode.getApplication().getId("GroupFolder"),
				sharedFolderId = rootNode.getApplication().getId("SharedFolder");
			result = new ArrayList<Node>();
			while( nodeIt.hasNext() ) {
				DbNode node = nodeIt.next();
				DbNode parentNode = nodeMap.get(node.getParentId());

				// If it is an orphan node, it belongs to a holder node.
				if( parentNode == null ) {
					// First look if the node is already there
					Iterator<Node> it = result.iterator();
					while( it.hasNext() && parentNode == null ) {
						parentNode = (DbNode)it.next();
						if( parentNode.getId() != node.getOwnerId() )
							parentNode = null;
					}
					if( parentNode == null ) {
						int folderNameId;
						if( node.getOwnerId() == rootNode.getUserId() )
							folderNameId = userFolderId;
						else if( rootNode.isMember( node.getOwnerId() ) )
							folderNameId = groupFolderId;
						else
							folderNameId = sharedFolderId;
						parentNode = new DbContainerNode(parentDoc, node.getOwnerId(), -1, folderNameId);
						parentNode.setAcl(node.getOwnerToken());
						result.add(parentNode);
						parentNode.setParent(this);
					}
				}
				parentNode._appendChild(node);
			}
			
			// Help garbage collector (does this really help?)
			nodeMap = null;

			// Create empty root folders for each group the user belongs to in case they don't exist
			Iterator<UidToken> uidIt = rootNode.getUidTokens().iterator();
			while( uidIt.hasNext() ) {
				UidToken uid = uidIt.next();
				Iterator<Node> it = result.iterator();
				DbNode parentNode = null;
				while( it.hasNext() && parentNode == null ) {
					parentNode = (DbNode)it.next();
					if( parentNode.getId() != uid.getUid() )
						parentNode = null;
				}
				if( parentNode == null ) {
					int folderNameId;
					if( uid.getUid() == rootNode.getUserId() )
						folderNameId = userFolderId;
					else 
						folderNameId = groupFolderId;
					parentNode = new DbContainerNode(parentDoc, uid.getUid(), -1, folderNameId);
					parentNode.setAcl(new AclToken(AclToken.ACL_OWNER, uid.getUid()));
					result.add(parentNode);
					parentNode.setParent(this);
				}
			}
			
		} catch (SQLException e) {
			Logger.debug(e);
		} finally {
			if( conn != null )
				db.releaseConnection(conn);
		}
		//printNodes(result, 0);
		return result;
	}
	
	private void printNodes(List<Node> nodes, int depth) {
		if( nodes == null )
			return;
		StringBuffer spacer = new StringBuffer();
		for( int i = 0; i < depth; i++ )
			spacer.append(" ");
		for( Node nodeX : nodes ) {
			DynamicElement node = (DynamicElement)nodeX;
			System.out.print(spacer.toString()+"<"+node.getNodeName());
			
			if( node.getChildren() != null && node.getChildren().size() > 0 ) {
				System.out.println(">");
				printNodes(node.getChildren(), depth+1);
				System.out.println(spacer.toString()+"</"+node.getNodeName()+">");
			} else
				System.out.println("/>");
		}
	}
	
	public VariableHolder setSearchVariables( String sortBy, String filterBy, int pageStart, int pageEnd ) {
		VariableHolder result = new VariableHolder();
		result.defineVariable("_SortBy", VariableHolder.STRING);
		result.defineVariable("_SearchString", VariableHolder.STRING);
		result.defineVariable("_RowStart", VariableHolder.NUMBER);
		result.defineVariable("_RowLimit", VariableHolder.NUMBER);
		result.setVariable("_RowStart", new Double(pageStart));
		result.setVariable("_RowLimit", new Double(pageEnd));
		if( filterBy != null )
			result.setVariable("_SearchString", filterBy);
		if( sortBy != null )
			result.setVariable("_SortBy", sortBy);

		return result;
	}

	// System folders cannot be deleted, new items cannot be inserted.
	public Node removeChild(Node arg0) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "@err-system-folder" );
	}
	
	public Node removeChild(String name) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "@err-system-folder" );
	}

	public Node appendChild(Node arg0) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "@err-system-folder" );
	}
	
	public Node appendChild(String name) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "@err-system-folder" );
	}
	
	public void update() {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "@err-system-folder" );
	}

	public void delete() {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "@err-system-folder" );
	}
	
	public enum SearchOpType {
		CONTAINS, EQUALS, GREATERTHAN, LESSTHAN, LESSTHANEQUAL, GREATERTHANEQUAL, STARTSWITH, ENDSWITH, AND, OR, NOT
	}
	
	protected BooleanExpression buildQuery( DynamicElement searchObject, Table hierarchyTable, Table indexTable, ObjectDefinition objDef, SearchOpType operation ) {
		Application app = rootNode.getApplication();
		Vector<BooleanExpression> terms = new Vector<BooleanExpression>();
		for( Node node : searchObject.getChildren() ) {
			DynamicElement op = (DynamicElement)node;
			String opName = op.getAttribute("Name"), opValue = op.getAttribute("Value");
			
			// Get OpType
			SearchOpType opType = null;
			try {
				opType = Enum.valueOf(SearchOpType.class, op.getAttribute("Type").toUpperCase());;
			} catch( Exception _ ) {}
			
			if( opType == null )
				continue;
			if( opType == SearchOpType.AND || opType == SearchOpType.OR || opType == SearchOpType.NOT ) {
				BooleanExpression result = buildQuery(op, hierarchyTable, indexTable, objDef, opType);
				if( result != null )
					terms.add(result);
			} else {
				if( opValue == null || opValue.isEmpty() )
					continue;
				if( opName == null || opName.isEmpty() )
					opName = op.getNodeName();
				
				// Handle inline Or queries, i.e. Name="From,To,Cc" Value="@codeglide.com"
				Vector<BooleanExpression> combined = new Vector<BooleanExpression>();
				String[] nameParts = opName.split(",");
				for( int i = 0; i < nameParts.length; i++ ) {
					Vector<Object> arguments = new Vector<Object>();
					
					ObjectField field = null;
					if( objDef != null && (field = objDef.getField(nameParts[i])) != null && (field.getFormat() == ObjectField.F_BOOLEAN || field.getFormat() == ObjectField.F_DOUBLE || field.getFormat() == ObjectField.F_INTEGER || field.getFormat() == ObjectField.F_LINK || field.getFormat() == ObjectField.F_ENUM)) {
						String[] argumentParts = opValue.split(",");
						
						for( int j = 0; j < argumentParts.length; j++ ) {
							switch( field.getFormat() ) {
								case ObjectField.F_BOOLEAN:
									arguments.add(new Integer( (argumentParts[j].equals("1") || argumentParts[j].equalsIgnoreCase("true")) ? 1 : 0));
									break;
								case ObjectField.F_DOUBLE:
									arguments.add(new Double(Double.parseDouble(argumentParts[j])));
									break;
								case ObjectField.F_INTEGER:
									arguments.add(new Integer(Integer.parseInt(argumentParts[j])));
									break;
								case ObjectField.F_LINK:
									arguments.add(new Long(Long.parseLong(argumentParts[j], 36)));
									break;
								default:
									arguments.add(argumentParts[j]);
							}
						}
					} else {
						String[] argumentParts = StringUtil.splitWithQuotes(opValue, ' ', '"', true);
						for( int j = 0; j < argumentParts.length; j++ ) 
							arguments.add(argumentParts[j]);
					}
					
					// Handle combined arguments, i.e. Name="ReportsTo" Value="4238472384,998398923,121323746"
					Vector<BooleanExpression> combinedArguments = new Vector<BooleanExpression>();
					
					if( field != null && field.isType(ObjectField.T_INDEX) && field.getFormat() != ObjectField.F_STRING ) {
						// The field is indexed
						for( Object argument : arguments ) {
							switch( opType ) {
								case GREATERTHAN:
									combinedArguments.add(new GreaterOrLess(indexTable.getColumn(app.getColumnName(field)), new Argument(argument.toString()), GreaterOrLess.GREATERTHAN));
									break;
								case GREATERTHANEQUAL:
									combinedArguments.add(new GreaterOrLess(indexTable.getColumn(app.getColumnName(field)), new Argument(argument.toString()), GreaterOrLess.GREATERTHANEQUAL));
									break;
								case LESSTHAN:
									combinedArguments.add(new GreaterOrLess(indexTable.getColumn(app.getColumnName(field)), new Argument(argument.toString()), GreaterOrLess.LESSTHAN));
									break;
								case LESSTHANEQUAL:
									combinedArguments.add(new GreaterOrLess(indexTable.getColumn(app.getColumnName(field)), new Argument(argument.toString()), GreaterOrLess.LESSTHANEQUAL));
									break;
								case EQUALS:
								default:
									combinedArguments.add(new Equals(indexTable.getColumn(app.getColumnName(field)), new Argument(argument.toString())));
									break;
							}
						}
						
					} else {
						// The field is not indexed or it is a string, thus search within METADATA
						int fieldId = app._getId(nameParts[i]);
						
						// If fieldId is -1, it doesn't exist in the database, skip it.
						if( fieldId < 0 )
							continue;

						for( Object argument : arguments ) {
							boolean isStringField = argument instanceof String;
							StringBuffer fieldQuery = new StringBuffer();
							fieldQuery.append("%").append(Integer.toString(fieldId, 36)).append(":");
							if( isStringField ) {
								fieldQuery.append("\"");
								if( opType == SearchOpType.CONTAINS || opType == SearchOpType.ENDSWITH ) 
									fieldQuery.append("%");
							}
							fieldQuery.append(argument.toString());
							if( isStringField ) {
								if( opType == SearchOpType.CONTAINS || opType == SearchOpType.STARTSWITH )
									fieldQuery.append("%");
								fieldQuery.append("\"");
							}
							fieldQuery.append("%");
							Logger.debug(fieldQuery.toString());
							combinedArguments.add(new Like(hierarchyTable.getColumn("METADATA"), new Argument(fieldQuery.toString())));
						}
					}
					if( combinedArguments.size() > 1 )
						combined.add(new Or((BooleanExpression[]) combinedArguments.toArray(new BooleanExpression[combinedArguments.size()])));
					else if( combinedArguments.size() > 0)
						combined.add(combinedArguments.get(0));
					
				}
				if( combined.size() > 1 )
					terms.add(new Or((BooleanExpression[]) combined.toArray(new BooleanExpression[combined.size()])));
				else if( combined.size() > 0)
					terms.add(combined.get(0));
			}
		}
		if( terms.size() > 0 ) {
			if( operation == SearchOpType.AND )
				return new And((BooleanExpression[]) terms.toArray(new BooleanExpression[terms.size()]));
			else if( operation == SearchOpType.OR )
				return new Or((BooleanExpression[]) terms.toArray(new BooleanExpression[terms.size()]));
			else if( operation == SearchOpType.NOT )
				return new Not(terms.get(0));
		}
		return null;
	}
	
	public void addSearchFilter(SelectQuery select, Table th, Table ts, ObjectDefinition objDef, DynamicElement searchObject ) {
		// Add query
		if( searchObject.getChildren() != null ) {
			BooleanExpression queryFilter = buildQuery(searchObject, th, ts, objDef, SearchOpType.AND);
			if( queryFilter != null )
				select.addFilter(queryFilter);
		}

		// Add full text search
		String fullText = searchObject.getAttribute("Text");
		if( fullText != null && !fullText.isEmpty() ) {
			String[] searchTerms = StringUtil.splitWithQuotes(fullText, ' ', '"', true);
			Vector<BooleanExpression> textList = new Vector<BooleanExpression>();
			//TODO use Sphinx indexes
			for( int i = 0; i < searchTerms.length; i++ ) 
				textList.add(new Like(th.getColumn("METADATA"), new Argument("%"+searchTerms[i]+"%")));
			if( textList.size() > 1 )
				select.addFilter(new Or((BooleanExpression[]) textList.toArray(new BooleanExpression[textList.size()])));
			else
				select.addFilter(textList.get(0));
		}
		
		// Add folders
		String folders = searchObject.getAttribute("Folder");
		if( folders != null && !folders.isEmpty() ) {
			String[] folderTerms = folders.split(",");
			Vector<BooleanExpression> foldersList = new Vector<BooleanExpression>();
			for( int i = 0; i < folderTerms.length; i++ )
				foldersList.add(new Equals(th.getColumn("PARENTID"),new Argument(Long.parseLong(folderTerms[i], 36))));
			if( foldersList.size() > 1 )
				select.addFilter(new Or((BooleanExpression[]) foldersList.toArray(new BooleanExpression[foldersList.size()])));
			else
				select.addFilter(foldersList.get(0));
		}

	}

	public List<Node> search(DynamicElement searchObject, String sortBy, int rowStart, int rowLimit ) {
		String nodeName = searchObject.getAttribute("Type");
		
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlDialect dialect = db.getDialect();
		SelectQuery select = dialect.createSelect();
		Table th = new Table(rootNode.getApplication().getName()+"_HIERARCHY");
		Table ts = null;

		// Obtain object definition, if any
		ObjectDefinition objDef = null;
		if( nodeName != null && !nodeName.isEmpty() ) {
			objDef = rootNode.getApplication().getObject(nodeName);
			if( objDef != null ) {
				ts = new Table(rootNode.getApplication().getName()+"_IDX_"+nodeName.toUpperCase());
				select.addTable(ts);
			}
		}
		select.addTable(th);
		
		addSearchFilter(select, th, ts, objDef, searchObject);
		
		return db.getLeaves(select, th, ts, objDef, this, sortBy, rowStart, rowLimit);
	}

	
}
