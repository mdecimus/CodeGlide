package com.codeglide.xml.dom;

public abstract class DynamicAttrVirtual extends DynamicAttr {

	public DynamicAttrVirtual(DynamicElement parentNode, String name, Object value) {
		super(parentNode, name, value);
	}

	public DynamicAttrVirtual(DynamicElement parentNode, String name) {
		super(parentNode, name);
	}

}
