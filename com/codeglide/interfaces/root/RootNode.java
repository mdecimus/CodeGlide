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
package com.codeglide.interfaces.root;

import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.core.Expression;
import com.codeglide.core.acl.UidToken;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.xml.dom.DummyDocument;
import com.codeglide.xml.dom.DynamicElement;

public class RootNode extends DynamicElement {
	private List<UidToken>uid = null;
	private String siteId = null;
	private Application application = null;
	private TimeZone tz;
	private String lang;
	
	// Global variables
	private VariableHolder globalVariables = null;
	
	public RootNode(Document doc, Application application) {
		super(doc, "CG");
		((DummyDocument)this.parentDoc).setDocumentElement(this);
		this.application = application;
		this.tz = TimeZone.getDefault();
	}

	public VariableHolder getGlobalVariables() {
		return globalVariables;
	}

	public void setGlobalVariables(VariableHolder globalVariables) {
		this.globalVariables = globalVariables;
	}

	public List<UidToken> getUidTokens() {
		return uid;
	}
	
	public boolean isMember(long uid) {
		for( UidToken token : this.uid ) {
			if( token.getUid() == uid )
				return true;
		}
		return false;
	}
	
	public Application getApplication() {
		return application;
	}
	
	public TimeZone getTimezone() {
		return tz;
	}
	
	public String getLanguage() {
		return lang;
	}
	
	public void setLanguage(String lang) {
		if( lang == null )
			return;
		this.lang = lang;
	}
	
	public void setTimezone(String tz) {
		if( tz == null )
			return;
		this.tz = TimeZone.getTimeZone(tz);
	}

	public void setUid(String uid, String groupUid) {
		if( uid == null )
			return;
		this.uid = UidToken.getUidTokens(uid);
		if( groupUid != null )
			this.uid.addAll(UidToken.getUidTokens(groupUid));
	}
	
	public long getUserId() {
		return uid.get(0).getUid();
	}
	
	public String getUserName() {
		//TODO implement
		return "admin";
	}
	
	public boolean hasExpired() {
		//TODO implement
		return false;
	}
	
	public void setExpireTimeout(String minutes) {
		if( minutes == null )
			return;
		//TODO implement
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

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	
	// Cached user Expressions
	
	private List<Expression> expressions = null;
	
	public Expression getExpression(String xpath) {
		if( expressions == null )
			expressions = new LinkedList<Expression>();
		for( Expression exp : expressions ) {
			if( exp.getXpathQuery().equals(xpath) )
				return exp;
		}
		Expression exp = new Expression("!"+xpath);
		expressions.add(exp);
		return exp;
	}

}
