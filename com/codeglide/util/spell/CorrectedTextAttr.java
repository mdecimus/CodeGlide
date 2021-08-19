package com.codeglide.util.spell;

import org.w3c.dom.DOMException;

import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;

/** Dynamic attribute that contains the corrected text after a spell checking. */
public class CorrectedTextAttr extends DynamicAttr {

	/**
	 * Creates a new CorrectedTextAttr object.
	 * @param parentDoc The parent document.
	 */
	public CorrectedTextAttr(DynamicElement parentNode ) {
		super(parentNode, "CorrectedText");
	}

	/** Returns the corrected text after a spell checking. */
	public String getExpandedValue() {
		return getValue();
	}

	/** Returns the corrected text after a spell checking. */
	public String getValue() {
		if (value != null)
			return (String) value;
		else
			return (String)(value = ((SpellChecker) parentNode).getCorrectedText());
	}

	/** The value can only be set in the method getValue(). */
	public void setValue(String arg0) throws DOMException {}
}
