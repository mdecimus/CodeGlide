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

package com.codeglide.webdav.exceptions;

@SuppressWarnings("serial")
public class WebdavTreeNodeNotFoundException extends WebdavTreeException {
	public WebdavTreeNodeNotFoundException(String message) {
		super(message);
	};
	public WebdavTreeNodeNotFoundException(Throwable e) {
		super(e);
	};
}
