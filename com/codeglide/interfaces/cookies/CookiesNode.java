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
package com.codeglide.interfaces.cookies;

import java.text.ParseException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;

import com.codeglide.core.Logger;
import com.codeglide.util.Base64Util;
import com.codeglide.util.json.Json2DynamicElement;
import com.codeglide.xml.dom.DynamicElement;

public class CookiesNode extends DynamicElement {
	private Cookie cookie = null;
	private String cookieValue = null;
	//private String hostname = null;
	
	public CookiesNode(Document parentDoc) {
		super(parentDoc, "Cookies");
		enableTracking();
	}
	
	public void getCookies(String cookieValue) {
		nodeFlags &= ~HAS_CHANGED;
		nodeFlags |= NEEDS_CHILD_INIT;

		this.cookieValue = cookieValue;
	}
	
	public void getCookies(HttpServletRequest request) {
		// Reset flags
		this.cookie = null;
		nodeFlags &= ~HAS_CHANGED;
		nodeFlags |= NEEDS_CHILD_INIT;
		
		// Set hostname
		//this.hostname = request.getServletRequest().getHeader("host");
		
		// Obtain the cookies object
		Cookie[] cookies = request.getCookies();
		if( cookies != null && cookies.length > 0 ) {
			for( int i = 0; i < cookies.length; i++ ) {
				//TODO add UID, sitename, packagename to CGOBJECTS name
				if( cookies[i].getName().equals("CGOBJECTS") ) {
					cookie = cookies[i];
					cookieValue = cookie.getValue();
				}
			}
		}
	}
	
	public void setCookies(HttpServletResponse response) {
		if( hasChanged() ) {
			if( children == null || children.size() < 1 ) {
				// If we have no cookies, remove them from the client
				if( cookie != null ) {
					cookie.setMaxAge(0);
					response.addCookie(cookie);
				}
			} else {
				cookie = new Cookie("CGOBJECTS", Base64Util.encode(Json2DynamicElement.exportNodes(children)));
				cookie.setMaxAge(60*60*24*365);
				//cookie.setPath("/cgrte/");
				//cookie.setDomain(hostname);
				response.addCookie(cookie);
			}
		}
	}

	public String getCookiesString() {
		if( hasChanged() ) {
			if( children == null || children.size() < 1 ) {
				return "";
			} else {
				return Base64Util.encode(Json2DynamicElement.exportNodes(children));
			}
		}
		return null;
	}
	
	protected void initChildren() {
		children = null;
		try {
			if( cookieValue != null )
				Json2DynamicElement.importNodes(this, Base64Util.decode(cookieValue));
		} catch (ParseException e) {
			Logger.debug(e);
		}
	}

}
