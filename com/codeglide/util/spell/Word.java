package com.codeglide.util.spell;

import org.w3c.dom.Document;

import com.codeglide.xml.dom.DynamicElement;

/** This class contains a word. */
public class Word extends DynamicElement {

	/**
	 * Creates a new Word object.
	 * @param parentDoc The parent document.
	 */
	public Word(Document parentDoc) {
		super(parentDoc, "Word");
	}

}
