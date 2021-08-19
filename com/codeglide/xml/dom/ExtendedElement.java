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
package com.codeglide.xml.dom;

import java.util.List;

import org.w3c.dom.DOMException;

import com.codeglide.core.acl.AclToken;

public interface ExtendedElement {
	public long getId();
	public long getParentId();
	
	public void setAcl(String acl);	
	public void setAcl(AclToken acl);
	public void setAcl(List<AclToken> acl);
	public boolean hasPermission(int operation);
	public long getOwnerId();
	public AclToken getOwnerToken();
	public String getAcl();
	
	public long getSize();
	
	public void update() throws DOMException;
	public DynamicElement move(DynamicElement source);
	public void delete() throws DOMException;
	
}
