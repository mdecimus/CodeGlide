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

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

public class DummyDocument implements Document {
	private Element mainNode;
	
	public short getNodeType() {
		return Element.DOCUMENT_NODE;
	}

	public Element getDocumentElement() {
		return mainNode;
	}

	public void setDocumentElement(Element mainNode) {
		this.mainNode = mainNode;
	}
	
	public boolean hasChildNodes() {
		return true;
	}
	
	public String getLocalName() {
		return "root";
	}

	public String getNodeName() {
		return getLocalName();
	}
	
	public Node getFirstChild() {
		return mainNode;
	}

	// Non-implemented functions
	
	public Node adoptNode(Node arg0) throws DOMException {
		return null;
	}

	public Attr createAttribute(String arg0) throws DOMException {
		return null;
	}

	public Attr createAttributeNS(String arg0, String arg1) throws DOMException {
		return null;
	}

	public CDATASection createCDATASection(String arg0) throws DOMException {
		return null;
	}

	public Comment createComment(String arg0) {
		return null;
	}

	public DocumentFragment createDocumentFragment() {
		return null;
	}

	public Element createElement(String arg0) throws DOMException {
		return null;
	}

	public Element createElementNS(String arg0, String arg1)
			throws DOMException {
		return null;
	}

	public EntityReference createEntityReference(String arg0)
			throws DOMException {
		return null;
	}

	public ProcessingInstruction createProcessingInstruction(String arg0,
			String arg1) throws DOMException {
		return null;
	}

	public Text createTextNode(String arg0) {
		return null;
	}

	public DocumentType getDoctype() {
		return null;
	}

	public String getDocumentURI() {
		return null;
	}

	public DOMConfiguration getDomConfig() {
		return null;
	}

	public Element getElementById(String arg0) {
		return null;
	}

	public NodeList getElementsByTagName(String arg0) {
		return null;
	}

	public NodeList getElementsByTagNameNS(String arg0, String arg1) {
		return null;
	}

	public DOMImplementation getImplementation() {
		return null;
	}

	public String getInputEncoding() {
		return null;
	}

	public boolean getStrictErrorChecking() {
		return false;
	}

	public String getXmlEncoding() {
		return null;
	}

	public boolean getXmlStandalone() {
		return false;
	}

	public String getXmlVersion() {
		return null;
	}

	public Node importNode(Node arg0, boolean arg1) throws DOMException {
		return null;
	}

	public void normalizeDocument() {
	}

	public Node renameNode(Node arg0, String arg1, String arg2)
			throws DOMException {
		return null;
	}

	public void setDocumentURI(String arg0) {
	}

	public void setStrictErrorChecking(boolean arg0) {
	}

	public void setXmlStandalone(boolean arg0) throws DOMException {
	}

	public void setXmlVersion(String arg0) throws DOMException {
	}

	public Node appendChild(Node newChild) throws DOMException {
		return null;
	}

	public Node cloneNode(boolean deep) {
		return null;
	}

	public short compareDocumentPosition(Node other) throws DOMException {
		return 0;
	}

	public NamedNodeMap getAttributes() {
		return null;
	}

	public String getBaseURI() {
		return null;
	}

	public NodeList getChildNodes() {
		return null;
	}

	public Object getFeature(String feature, String version) {
		return null;
	}

	public Node getLastChild() {
		return null;
	}

	public String getNamespaceURI() {
		return null;
	}

	public Node getNextSibling() {
		return null;
	}

	public String getNodeValue() throws DOMException {
		return null;
	}

	public Document getOwnerDocument() {
		return null;
	}

	public Node getParentNode() {
		return null;
	}

	public String getPrefix() {
		return null;
	}

	public Node getPreviousSibling() {
		return null;
	}

	public String getTextContent() throws DOMException {
		return null;
	}

	public Object getUserData(String key) {
		return null;
	}

	public boolean hasAttributes() {
		return false;
	}

	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
		return null;
	}

	public boolean isDefaultNamespace(String namespaceURI) {
		return false;
	}

	public boolean isEqualNode(Node arg) {
		return false;
	}

	public boolean isSameNode(Node other) {
		return false;
	}

	public boolean isSupported(String feature, String version) {
		return false;
	}

	public String lookupNamespaceURI(String prefix) {
		return null;
	}

	public String lookupPrefix(String namespaceURI) {
		return null;
	}

	public void normalize() {
	}

	public Node removeChild(Node oldChild) throws DOMException {
		return null;
	}

	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
		return null;
	}

	public void setNodeValue(String nodeValue) throws DOMException {
	}

	public void setPrefix(String prefix) throws DOMException {
	}

	public void setTextContent(String textContent) throws DOMException {
	}

	public Object setUserData(String key, Object data, UserDataHandler handler) {
		return null;
	}
}
