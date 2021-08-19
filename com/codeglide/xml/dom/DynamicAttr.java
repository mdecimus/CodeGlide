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
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;

public abstract class DynamicAttr implements Attr, TrackeableNode, SequenceBucketizable {
	protected String name;
	protected Object value, oldValue;
	private boolean hasChanged = false;
	protected DynamicElement parentNode;
	protected ObjectField fieldDefinition = null;
	
	public Object getPreviousValue() {
		return this.oldValue;
	}

	public boolean hasChanged() {
		return this.hasChanged;
	}

	public void trackChange() {
		if( !this.hasChanged ) {
			this.hasChanged = true;
			if( this.parentNode.isTracking() )
				this.parentNode.trackChange();
		}
	}
	
	public void resetChanged() {
		this.hasChanged = false;
		this.oldValue = this.value;
	}
	
	public boolean setIfChanged(Object value) {
		if( value == null && this.value == null )
			return false;
		else if( value == null || this.value == null || !this.value.equals(value) ) {
			this.value = value;
			trackChange();
			return true;
		} else
			return false;
	}
	
	public DynamicAttr(DynamicElement parentNode, ObjectField fieldDefinition ) {
		this.parentNode = parentNode;
		this.fieldDefinition = fieldDefinition;
		this.name = fieldDefinition.getLocalId();
	}
	
	protected DynamicAttr(DynamicElement parentNode, ObjectField fieldDefinition, Object value ) {
		this.parentNode = parentNode;
		this.fieldDefinition = fieldDefinition;
		this.name = fieldDefinition.getLocalId();
		this.value = this.oldValue = value;
	}
	
	public DynamicAttr(DynamicElement parentNode, String name) {
		this.parentNode = parentNode;
		this.name = name;
	}
	
	protected DynamicAttr(DynamicElement parentNode, String name, Object value) {
		this.parentNode = parentNode;
		this.name = name;
		this.value = this.oldValue = value;
	}
	
	public ObjectField getFieldDefinition() {
		return fieldDefinition;
	}

	public String getName() {
		return this.name;
	}

	public Object getObjectValue() {
		return value;
	}

	public String getLocalName() {
		return this.name;
	}

	public String getNodeName() {
		return this.name;
	}

	public short getNodeType() {
		return Node.ATTRIBUTE_NODE;
	}

	public Document getOwnerDocument() {
		return parentNode.getOwnerDocument();
	}

	public Node getParentNode() {
		return parentNode;
	}
	
	public abstract String getExpandedValue();

	public Element getOwnerElement() {
		return parentNode;
	}

	// Non-implemented functions
	
	public TypeInfo getSchemaTypeInfo() {
		return null;
	}

	public boolean getSpecified() {
		return false;
	}

	public boolean isId() {
		return false;
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

	public Node getFirstChild() {
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
		return getValue();
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

	public boolean hasChildNodes() {
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
		setValue(nodeValue);
	}

	public void setPrefix(String prefix) throws DOMException {
	}

	public void setTextContent(String textContent) throws DOMException {
	}

	public Object setUserData(String key, Object data, UserDataHandler handler) {
		return null;
	}

	// Sequence bucket
	
	private int seqId = -1;
	
	public int getSequenceId() {
		return this.seqId;
	}

	public void setSequenceId(int id) {
		this.seqId = id;
	}

}
