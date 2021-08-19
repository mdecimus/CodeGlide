package com.codeglide.xml.dom;

import java.io.InputStream;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class NullElement extends VirtualElement {

	public NullElement(Document parentDoc) {
		super(parentDoc, "__NULL");
	}

	public Node _appendChild(Node arg0) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public Node _appendChild(String name) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public Node _removeChild(String name) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public Node appendChild(Node arg0) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public Node appendChild(String name) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public void removeAttribute(String arg0) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public Attr removeAttributeNode(Attr arg0) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public void removeAttributeNS(String arg0, String arg1) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public Node removeChild(Node arg0) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public Node removeChild(String name) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public Node replaceChild(Node arg0, Node arg1) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public void setAttribute(String arg0, InputStream arg1) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public void setAttribute(String arg0, String arg1) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public Attr setAttributeNode(Attr arg0) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public Attr setAttributeNodeNS(Attr arg0) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public void setAttributeNS(String arg0, String arg1, String arg2) {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

	public void setNodeValue(String arg0) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@null-object-access");
	}

}
