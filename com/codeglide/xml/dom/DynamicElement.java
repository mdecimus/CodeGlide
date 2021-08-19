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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

import com.codeglide.core.objects.ObjectDefinition;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;
import com.codeglide.util.ISO8601;

public class DynamicElement implements Element, TrackeableNode, SequenceBucketizable {
	protected String nodeName = null;
	protected DynamicElement parentNode = null;
	protected Document parentDoc = null;
	protected HashMap<String, Attr> attributes = null;
	protected List<Node> children = null;
	protected int nodeFlags = NO_TRACKING;
	
	protected ObjectDefinition objectDefinition = null;
	
	public final static short NEEDS_ATTR_INIT = 0x001;
	public final static short NEEDS_CHILD_INIT = 0x002;
	public final static short HAS_CHANGED = 0x004;
	public final static short NO_TRACKING = 0x008;
	public final static short EXPAND_ONLY_CONTAINERS = 0x010;

	// Object Constructor
	public DynamicElement( Document parentDoc, String nodeName ) {
		this.nodeName = nodeName;
		this.parentDoc = parentDoc;
	}
	
	// Sets Parent Node
	public void setParent(DynamicElement parentNode) {
		this.parentNode = parentNode;
	}

	//TODO make sure this function is only called when needed
	protected void initAttributes() {
	}
	
	protected void initChildren() {
	}
	
	public void setFlag( int flag ) {
		nodeFlags |= flag;
	}
	
	public void removeFlag( int flag ) {
		nodeFlags &= ~flag;
	}
	
	public void disableTracking() {
		nodeFlags |= NO_TRACKING;
	}
	
	public void enableTracking() {
		nodeFlags &= ~NO_TRACKING;
	}
	
	public boolean isTracking() {
		return (nodeFlags & NO_TRACKING) == 0;
	}
	
	public void requiresAttrInit() {
		nodeFlags |= NEEDS_ATTR_INIT;
	}
	
	public void requiresChildInit() {
		nodeFlags |= NEEDS_CHILD_INIT;
	}
	
	// Inherited Members
	public String getAttribute(String arg0) {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		if( attributes == null )
			return null;
		Attr result = attributes.get(arg0);
		return (result!=null)?result.getValue():null;
	}
	
	public Attr setAttributeNode(Attr arg0) throws DOMException {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		return _setAttributeNode(arg0);
	}

	public Attr _setAttributeNode(Attr arg0) throws DOMException {
		if( attributes == null )
			attributes = new HashMap<String, Attr>();
		attributes.put(arg0.getName(), arg0);
		return arg0;
	}

	public void setAttribute(String arg0, InputStream arg1) throws DOMException {
		if( arg0.startsWith("_") )
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Internal attributes cannot contain binary values.");
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		Attr item = getAttributeNode(arg0);
		if( item == null ) {
			if( objectDefinition != null )
				item = objectDefinition.buildField(this, arg0, null);
			else
				item = new DynamicAttrStream(this, arg0, null);
			attributes.put(arg0, item);
		}
		if( item instanceof DynamicAttrStream )
			((DynamicAttrStream)item).setInputStream(arg1);
	}

	public void setAttribute(String arg0, String arg1) throws DOMException {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		Attr item = getAttributeNode(arg0);
		if( item == null ) {
			if( objectDefinition != null )
				item = objectDefinition.buildField(this, arg0, null);
			else
				item = new DynamicAttrString(this, arg0, null);
			attributes.put(arg0, item);
		} 
		item.setValue(arg1);
	}

	public void setAttribute(String arg0, boolean arg1) throws DOMException {
		setAttribute(arg0,(arg1)?"1":"0");
	}

	public void setAttribute(String arg0, long arg1) throws DOMException {
		setAttribute(arg0,String.valueOf(arg1));
	}

	public void setAttribute(String arg0, double arg1) throws DOMException {
		setAttribute(arg0,String.valueOf(arg1));
	}

	public void setAttribute(String arg0, int arg1) throws DOMException {
		setAttribute(arg0,String.valueOf(arg1));
	}

	public void setAttribute(String arg0, Date arg1) throws DOMException {
		setAttribute(arg0,ISO8601.formatUtc(arg1));
	}

	public Node appendChild(Node arg0) throws DOMException {
		if( (nodeFlags & NEEDS_CHILD_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_CHILD_INIT;
			initChildren();
		}
		_appendChild(arg0);
		if( (nodeFlags & NO_TRACKING) == 0 )
			trackChange();
		return arg0;
	}
	
	public Node appendChild(String name) throws DOMException {
		if( (nodeFlags & NEEDS_CHILD_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_CHILD_INIT;
			initChildren();
		}
		return _appendChild(name);
	}

	public Node _appendChild(Node arg0) throws DOMException {
		if( children == null )
			children = new ArrayList<Node>();
		children.add((DynamicElement)arg0);
		if( isTracking() )
			((DynamicElement)arg0).enableTracking();
		((DynamicElement)arg0).setParent(this);
		return arg0;
	}
	
	public Node _appendChild(String name) throws DOMException {
		DynamicElement result = null;
		if( objectDefinition != null && objectDefinition.getObject(name) != null ) {
			ObjectDefinition childObjDef = objectDefinition.getObject(name);
			result = childObjDef.buildObject(this.parentDoc);
			result.setObjectDefinition(childObjDef);
			return result;
		} else
			result = new DynamicElement(this.parentDoc,name);
		return _appendChild(result);
	}
	
	/** BEGIN MODIFICATION
	 *		BY Hugo, 2008-01-18
	 *	Remove these comments if modifications are approved
	 */
	/*
	 	method:
	 	
	public Node removeChild(Node arg0) throws DOMException {
		if( (nodeFlags & NEEDS_CHILD_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_CHILD_INIT;
			initChildren();
		}
		children.remove(arg0);
		if( (nodeFlags & NO_TRACKING) == 0 )
			trackChange();
		return arg0;
	}
	
	 	splitted into removeChild() and _removeChild(). _removeChild(..) provides a way to remove a
	 	child from children list, despite subclasses possibly redefinition of removeChild(..)      
	 */
	public Node _removeChild(Node arg0) throws DOMException {
		if( (nodeFlags & NEEDS_CHILD_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_CHILD_INIT;
			initChildren();
		}
		children.remove(arg0);
		if( (nodeFlags & NO_TRACKING) == 0 )
			trackChange();
		return arg0;
	}
	
	public Node removeChild(Node arg0) throws DOMException {
		return this._removeChild(arg0);
	}
	/** END MODIFICATION */
	
	public Node _removeChild(String name) throws DOMException {
		if( children == null )
			return null;
		Iterator<Node> it = children.iterator();
		while( it.hasNext() ) {
			Node node = it.next();
			if( node.getNodeName().equalsIgnoreCase(name) ) {
				children.remove(node);
				return node;
			}
		}
		return null;
	}
	
	public Node removeChild(String name) throws DOMException {
		if( (nodeFlags & NEEDS_CHILD_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_CHILD_INIT;
			initChildren();
		}
		if( children == null )
			return null;
		Iterator<Node> it = children.iterator();
		while( it.hasNext() ) {
			Node node = it.next();
			if( node.getNodeName().equalsIgnoreCase(name) ) {
				children.remove(node);
				if( (nodeFlags & NO_TRACKING) == 0 )
					trackChange();
				return node;
			}
		}
		return null;
	}
	
	public Node getChildNode(String name) {
		if( (nodeFlags & NEEDS_CHILD_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_CHILD_INIT;
			initChildren();
		}
		if( children == null )
			return null;
		Iterator<Node> it = children.iterator();
		while( it.hasNext() ) {
			Node node = it.next();
			if( node.getNodeName().equalsIgnoreCase(name) )
				return node;
		}
		return null;
	}

	public NamedNodeMap getAttributes() {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		return new DummyNamedNodeMap((attributes==null)?(new HashMap<String, Attr>()):attributes);
	}
	
	public Collection<Attr> getAttributesCollection() {
		return ( attributes == null ) ? new Vector<Attr>() : attributes.values();
	}

	public NodeList getChildNodes() {
		List<Node> children = this.getChildren();
		return new DummyNodeList((children==null)?(new ArrayList<Node>()):children);
	}

	public List<Node> getChildren() {
		if( (nodeFlags & NEEDS_CHILD_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_CHILD_INIT;
			initChildren();
		}
		return children;
	}
	
	public List<Node> _getChildren() {
		return children;
	}
	
	public void _setChildren(List<Node> children) {
		this.children = children;
	}
	
	public Node getFirstChild() {
		List<Node> children = this.getChildren();
		return (children!=null&&children.size()>0)?children.get(0):null;
	}

	public Node getLastChild() {
		List<Node> children = this.getChildren();
		return (children!=null&&children.size()>0)?children.get(children.size()-1):null;
	}

	public String getLocalName() {
		return this.nodeName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public short getNodeType() {
		return Node.ELEMENT_NODE;
	}

	public Document getOwnerDocument() {
		return this.parentDoc;
	}

	public Node getParentNode() {
		return this.parentNode;
	}

	public Document getDocumentNode() {
		return this.parentDoc;
	}

	public Node getNextSibling() {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		if( parentNode == null )
			return null;
		List<Node> children = this.parentNode._getChildren();
		int index = children.indexOf(this) + 1;
		return (index < children.size()) ? children.get(index) : null;
	}

	public Node getPreviousSibling() {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		if( parentNode == null )
			return null;
		List<Node> children = this.parentNode._getChildren();
		int index = children.indexOf(this);
		return (index <= 0) ? null : children.get(index-1);
	}

	public boolean hasAttributes() {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		return (attributes!=null && attributes.size()>0);
	}

	public boolean hasChildNodes() {
		List<Node> children = this.getChildren();
		return (children!=null && children.size()>0);
	}

	public Attr getAttributeNode(String arg0) {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		if( attributes == null )
			attributes = new HashMap<String, Attr>();
		return attributes.get(arg0);
	}

	public void removeAttribute(String arg0) throws DOMException {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		if( attributes != null && attributes.containsKey(arg0))
			setAttribute(arg0, (String)null);
	}
	
	// Change tracking functions
	
	public boolean hasChanged() {
		return (nodeFlags & HAS_CHANGED) != 0;
	}
	
	public void resetChanged() {
		nodeFlags &= ~HAS_CHANGED;
		if( attributes != null ) {
			for( Attr attr : attributes.values() )
				((TrackeableNode)attr).resetChanged();
		}
		if( children != null ) {
			for( Node node : children )
				((TrackeableNode)node).resetChanged();
		}
	}

	public void trackChange() {
		if( (nodeFlags & HAS_CHANGED) == 0 ) {
			nodeFlags |= HAS_CHANGED;
			if( this.parentNode != null )
				this.parentNode.trackChange();
		}
	}

	public Node cloneNode(boolean deep) {
		return cloneNode(parentDoc, deep);
	}
	
	public Node cloneNode(Document parentDoc, boolean deep) {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}

		DynamicElement node = new DynamicElement(parentDoc, nodeName);
		if( attributes != null ) {
			for( Attr attr : attributes.values() ) {
				Object value = ((DynamicAttr)attr).getObjectValue();
				if( value == null || value instanceof String )
					node.setAttribute(attr.getName(), (String)value );
				else if( value instanceof InputStream )
					node.setAttribute(attr.getName(), (InputStream)value);
			}
		}
		
		if( deep ) {
			if( (nodeFlags & NEEDS_CHILD_INIT) != 0 ) {
				nodeFlags &= ~NEEDS_CHILD_INIT;
				initChildren();
			}
			if( children != null ) {
				for( Node child : children ) {
					node.appendChild(child.cloneNode(true));
				}
			}
		}
		
		return node;
	}
	
	// Non-implemented Methods
	
	public String getAttributeNS(String arg0, String arg1) throws DOMException {
		return null;
	}

	public Attr getAttributeNodeNS(String arg0, String arg1)
			throws DOMException {
		return null;
	}

	public NodeList getElementsByTagName(String arg0) {
		return null;
	}

	public NodeList getElementsByTagNameNS(String arg0, String arg1)
			throws DOMException {
		//TODO: Should we throw a DOMException if the method is not supported? - Find Out
		//throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
		return null;
	}

	public TypeInfo getSchemaTypeInfo() {
		return null;
	}

	public String getTagName() {
		return null;
	}

	public boolean hasAttribute(String arg0) {
		if( (nodeFlags & NEEDS_ATTR_INIT) != 0 ) {
			nodeFlags &= ~NEEDS_ATTR_INIT;
			initAttributes();
		}
		return attributes.containsKey(arg0);
	}

	public boolean hasAttributeNS(String arg0, String arg1) throws DOMException {
		return hasAttribute(arg0);
	}

	public void removeAttributeNS(String arg0, String arg1) throws DOMException {
	}

	public Attr removeAttributeNode(Attr arg0) throws DOMException {
		return null;
	}

	public void setAttributeNS(String arg0, String arg1, String arg2)
			throws DOMException {
	}

	public Attr setAttributeNodeNS(Attr arg0) throws DOMException {
		return null;
	}

	public void setIdAttribute(String arg0, boolean arg1) throws DOMException {
	}

	public void setIdAttributeNS(String arg0, String arg1, boolean arg2)
			throws DOMException {
	}

	public void setIdAttributeNode(Attr arg0, boolean arg1) throws DOMException {
	}

	public short compareDocumentPosition(Node arg0) throws DOMException {
		return 0;
	}

	public String getBaseURI() {
		return null;
	}

	public Object getFeature(String arg0, String arg1) {
		return null;
	}

	public String getNamespaceURI() {
		return null;
	}

	public String getNodeValue() throws DOMException {
		return null;
	}

	public String getPrefix() {
		return null;
	}

	public String getTextContent() throws DOMException {
		return null;
	}

	public Object getUserData(String arg0) {
		return null;
	}

	public Node insertBefore(Node arg0, Node arg1) throws DOMException {
		return null;
	}

	public boolean isDefaultNamespace(String arg0) {
		return false;
	}

	public boolean isEqualNode(Node arg0) {
		return false;
	}

	public boolean isSameNode(Node arg0) {
		return false;
	}

	public boolean isSupported(String arg0, String arg1) {
		return false;
	}

	public String lookupNamespaceURI(String arg0) {
		return null;
	}

	public String lookupPrefix(String arg0) {
		return null;
	}

	public void normalize() {
	}

	public Node replaceChild(Node arg0, Node arg1) throws DOMException {
		return null;
	}

	public void setNodeValue(String arg0) throws DOMException {
	}

	public void setPrefix(String arg0) throws DOMException {
	}

	public void setTextContent(String arg0) throws DOMException {
	}

	public Object setUserData(String arg0, Object arg1, UserDataHandler arg2) {
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

	public ObjectDefinition getObjectDefinition() {
		return objectDefinition;
	}

	public void setObjectDefinition(ObjectDefinition objectDefinition) {
		this.objectDefinition = objectDefinition;
	}

}
