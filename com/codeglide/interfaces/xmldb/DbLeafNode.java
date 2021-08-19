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

import java.sql.SQLException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import com.codeglide.core.acl.AclToken;
import com.codeglide.interfaces.xmldb.SqlConnectionPool.SqlConnection;
import com.codeglide.xml.dom.DynamicElement;

public class DbLeafNode extends DbNode {

	public DbLeafNode(Document document, long id, long parentId, int nodeId) {
		super(document, id, parentId, nodeId);
	}

	public DbLeafNode(Document document, long id) {
		super(document, id);
	}

	public DbLeafNode(Document document, String nodeName) {
		super(document, nodeName);
	}

	public DynamicElement move(DynamicElement source) {
		DynamicElement moved = (DynamicElement)this.appendChild(source);
		source.getParentNode().removeChild(source);
		return moved;
	}
	
	protected void delete(SqlConnection conn, boolean checkAcl) throws DOMException, SQLException {
		if( checkAcl && !hasPermission(AclToken.ACL_DELETE) )
			throw new DOMException(DOMException.VALIDATION_ERR, "@acl-violation-delete");

		delete(conn);
	}


}
