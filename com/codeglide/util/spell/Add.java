package com.codeglide.util.spell;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.xml.dom.DynamicElement;

/** This class adds words to a user dictionary. */
public class Add extends DynamicElement {

	/**
	 * Creates a new Add object.
	 * @param parentDoc The parent document.
	 */
	public Add(Document parentDoc) {
		super(parentDoc, "Add");
	}

	/**
	 * Adds a word to a user dictionary.
	 * @param word A word to add.
	 * @return The added word.
	 */
	public Node _appendChild(Node word) throws DOMException {
		if (word instanceof Word)
			((SpellChecker)parentNode).add((Word) word);
		return word;
	}
}