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
public class WebdavTreeUnsupportedOperationException extends WebdavTreeException {
	public WebdavTreeUnsupportedOperationException() {
		super();
	}
	public WebdavTreeUnsupportedOperationException(String message) {
		super(message);
	}
	public WebdavTreeUnsupportedOperationException(Throwable e) {
		super(e);
	}
}
