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
package com.codeglide.interfaces.mail;

import org.w3c.dom.Document;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.interfaces.Interface;
import com.codeglide.util.mail.folder.RootFolderElement;
import com.codeglide.xml.dom.DynamicElement;

public class MailInterface extends Interface {

	public DynamicElement createRootElement(Document document) throws CodeGlideException {
		DynamicElement root = (DynamicElement)document.getDocumentElement();
		DynamicElement dbRoot = (DynamicElement)root.getChildNode("Db");
		DynamicElement userFolder = (DynamicElement)dbRoot.getChildNode("UserFolder");
		DynamicElement mailCache = (DynamicElement)userFolder.getChildNode("MailCache");
		if (mailCache == null) {
			mailCache = new DynamicElement(document,"MailCache");
			mailCache = (DynamicElement)userFolder.appendChild(mailCache);
		}
		RootFolderElement rootFolder = new RootFolderElement(mailCache);
		/*************************************************************/
		/*********  HARDWIRED!  ***  Only for test purposes  *********/
		/*************************************************************/
		rootFolder.setAttribute("Host","imap.gmail.com");
		rootFolder.setAttribute("Protocol","imap");
		rootFolder.setAttribute("SSL",true);
		rootFolder.setAttribute("User","the.tattered.king");
		rootFolder.setAttribute("Pass","ali baba");
		/*************************************************************/
		return rootFolder;
	}

	public void init() throws Exception {
		//NO-OP
	}

	public void initApplication(Application application) throws Exception {
		//NO-OP
	}
}
