package com.codeglide.xml.dom;

import org.w3c.dom.DOMException;

import com.codeglide.core.Logger;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.interfaces.xmldb.DbLeafNode;
import com.codeglide.interfaces.xmldb.DbNode;

public class DynamicAttrLink extends DynamicAttr {
	private String linkedObjectName = null;
	
	public DynamicAttrLink(DynamicElement parentNode, ObjectField fieldDefinition, String value) {
		super(parentNode, fieldDefinition, value);
		/*if( value != null && !value.isEmpty() ) {
			VirtualElement vElement = new VirtualElement(this.parentNode.getDocumentNode(), "__" + name);
			vElement._appendChild(new DbLeafNode(this.parentNode.getDocumentNode(), Long.parseLong(value)));
			parentNode._appendChild(vElement);
		}*/
	}

	public String getValue() {
		return (String) value;
	}
	
	public void setValue(long value) throws DOMException {
		if( value > 0 ) {
			if( setIfChanged(String.valueOf(value)) )
				linkedObjectName = null;
			/*String linkedNodeName = "__" + name;
			parentNode._removeChild(linkedNodeName);
			VirtualElement vElement = new VirtualElement(this.parentNode.getDocumentNode(), linkedNodeName);
			vElement._appendChild(new DbLeafNode(this.parentNode.getDocumentNode(), value));
			parentNode._appendChild(vElement);*/
		} else
			setIfChanged(null);
	}

	public void setValue(String value) throws DOMException {
		if( value != null && !value.isEmpty() ) {
			try {
				setValue(Long.parseLong(value));
			} catch (NumberFormatException _) {
				Logger.debug("Invalid value '" + value + "' specified for field '" + fieldDefinition.getId() + "'.");
			}
		} else
			setIfChanged(null);
	}

	public String getExpandedValue() {
		if( linkedObjectName == null && value != null && !((String)value).isEmpty() && Long.parseLong((String)value) > -1) {
			try {
				linkedObjectName = ((ObjectField)fieldDefinition.getSetting(ObjectField.E_LINK_FIELD)).getBind().evaluate(null, getLinkedNode());
				if( linkedObjectName == null )
					linkedObjectName = "";
			} catch (Exception _) {
				//Logger.debug(e);
			}
		}
		return linkedObjectName;
	}
	
	public void setExpandedValue(String value) {
		this.linkedObjectName = value;
	}
	
	public DbNode getLinkedNode() {
		try {
			return (DbNode) new DbLeafNode(this.parentNode.getDocumentNode(), Long.parseLong((String)value));
		} catch (Exception e) {
			return null;
		}
	}

}
