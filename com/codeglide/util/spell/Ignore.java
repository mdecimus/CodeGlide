package com.codeglide.util.spell;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.xml.dom.DynamicElement;

/** This class ignores the correction of a misspelled word. */
public class Ignore extends DynamicElement {

	/**
	 * Creates a new Ignore object.
	 * @param parentDoc The parent document.
	 */
	public Ignore(Document parentDoc) {
		super(parentDoc, "Ignore");
	}

	/**
	 * Ignores a misspelled word.
	 * @param word A Word object.
	 * @return The argument.
	 */
	public Node _appendChild(Node word) throws DOMException {
		if (word instanceof Word)
			((SpellChecker)parentNode).ignore();
		return word;
	}
}