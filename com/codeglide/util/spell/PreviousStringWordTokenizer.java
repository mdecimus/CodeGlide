package com.codeglide.util.spell;

import com.swabunga.spell.event.StringWordTokenizer;

/** This is a string tokenizer that uses a PreviousWordFinder finder. */
public class PreviousStringWordTokenizer extends StringWordTokenizer implements PreviousWordTokenizer {
	/**
	 * Creates a new PreviousStringWordTokenizer object.
	 * @param s The string to be tokenized.
	 */
	public PreviousStringWordTokenizer(String s, PreviousWordFinder wf) {
		super(s, wf);
	}
	
	/** Returns the previous word and goes back one word in the sequence. */
	public String previousWord() {
	    return ((PreviousWordFinder) finder).previous().getText();
	}
}
