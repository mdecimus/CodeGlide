package com.codeglide.util.spell;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.xml.dom.DynamicElement;

/** This class ignores all attempts of correction of a misspelled word. */
public class IgnoreAll extends DynamicElement {

	/**
	 * Creates a new IgnoreAll object.
	 * @param parentDoc The parent document.
	 */
	public IgnoreAll(Document parentDoc) {
		super(parentDoc, "IgnoreAll");
	}

	/**
	 * Ignores all attempts of correction of a misspelled word.
	 * @param word A Word object.
	 * @return The argument.
	 */
	public Node _appendChild(Node word) throws DOMException {
		if (word instanceof Word)
			((SpellChecker)parentNode).ignoreAll();
		return word;
	}
}