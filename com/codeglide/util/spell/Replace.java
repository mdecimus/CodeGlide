package com.codeglide.util.spell;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.xml.dom.DynamicElement;

/** This class replaces a misspelled word. */
public class Replace extends DynamicElement {

	/**
	 * Creates a new Replace object.
	 * @param parentDoc The parent document.
	 */
	public Replace(Document parentDoc) {
		super(parentDoc, "Replace");
	}

	/**
	 * Replaces a misspelled word.
	 * @param word The new word.
	 * @return The new word.
	 */
	public Node _appendChild(Node word) throws DOMException {
		if (word instanceof Word)
			((SpellChecker)parentNode).replace((Word) word);
		return word;
	}
}
