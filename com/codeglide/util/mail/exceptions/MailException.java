package com.codeglide.util.mail.exceptions;

import javax.mail.MessagingException;

@SuppressWarnings("serial")
public class MailException extends Exception {
	public MailException(String message) {
		super(message);
	}

	public MailException(MessagingException e) {
		super(e);
	}
}
