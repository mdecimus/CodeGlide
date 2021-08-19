package com.codeglide.util.spell;

import org.w3c.dom.Document;

import com.codeglide.xml.dom.DynamicElement;

/** This class contains the suggestions for a misspelled word. */
public class Suggestions extends DynamicElement {

	/**
	 * Creates a new Suggestions object.
	 * @param parentDoc The parent document.
	 */
	public Suggestions(Document parentDoc) {
		super(parentDoc, "Suggestions");
	}
}