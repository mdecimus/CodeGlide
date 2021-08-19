package com.codeglide.util.spell;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.xml.dom.DynamicElement;

/** This class indicates a replacement for all occurrences of a misspelled word. */
public class ReplaceAll extends DynamicElement {

	/**
	 * Creates a new ReplaceAll object.
	 * @param parentDoc The parent document.
	 */
	public ReplaceAll(Document parentDoc) {
		super(parentDoc, "ReplaceAll");
	}

	/**
	 * Indicates a replacement for all occurrences of a misspelled word.
	 * @param word The replacement word.
	 * @return The replacement word.
	 */
	public Node _appendChild(Node arg0) throws DOMException {
		if (arg0 instanceof Word)
			((SpellChecker)parentNode).replaceAll((Word) arg0);
		return arg0;
	}
}