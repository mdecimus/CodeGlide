package com.codeglide.webdav.exceptions;

public class WebdavTreeNoPermissionException extends WebdavTreeException {
	public WebdavTreeNoPermissionException(String message) {
		super(message);
	}

	public WebdavTreeNoPermissionException(Throwable e) {
		super(e);
	}
}
