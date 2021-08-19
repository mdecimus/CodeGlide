package com.codeglide.util.spell;

import com.swabunga.spell.event.DefaultWordFinder;
import com.swabunga.spell.event.Word;

/** This finder allows going back one word. */
public class PreviousDefaultWordFinder extends DefaultWordFinder implements PreviousWordFinder {
	/** Contains the previous word. */
	private Word previousWord = new Word("", 0);
	
	/** Creates a new PreviousDefaultWordFinder object. */
	public PreviousDefaultWordFinder() {}
	
	/**
	 * Creates a new PreviousDefaultWordFinder object.
	 * @param inText The text to process.
	 */
	public PreviousDefaultWordFinder(String inText) {
		super(inText);
	}
	
	/** Returns the next word keeping track of the previous word and goes forward one word in the sequence. */
	public Word next() {
		if (this.getPreviousWord() != null)
			this.getPreviousWord().copy(currentWord);
		return super.next();
	}
	
	/** Returns the previous word and goes back one word in the sequence. */
	public Word previous() {
		if (nextWord != null)  // This is because on object construction this method is checked.
			nextWord.copy(currentWord);
		else
			nextWord = new Word(currentWord);
		
		currentWord.copy(this.getPreviousWord());
		return currentWord;
	}

	private Word getPreviousWord() {
		return previousWord;
	}
}
