package com.codeglide.interfaces.messages;

import org.w3c.dom.Document;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.interfaces.Interface;
import com.codeglide.xml.dom.DynamicElement;

public class MessagesInterface extends Interface {

	public DynamicElement createRootElement(Document document)
			throws CodeGlideException {
		return new MessagesNode(document);
	}

	public void init() throws Exception {

	}

	public void initApplication(Application application) throws Exception {

	}

}
