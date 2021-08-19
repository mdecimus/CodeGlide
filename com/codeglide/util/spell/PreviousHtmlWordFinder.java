package com.codeglide.util.spell;

import com.codeglide.util.converter.HtmlCoder;
import com.swabunga.spell.event.AbstractWordFinder;
import com.swabunga.spell.event.Word;
import com.swabunga.spell.event.WordNotFoundException;

/**
 * This finder reads HTML documents avoiding words in tags and converting special characters.
 * It only allows going back one word.
 */
public class PreviousHtmlWordFinder extends AbstractWordFinder implements PreviousWordFinder {
	/** Contains the previous word. */
	private Word previousWord = new Word("", 0);
	/** Contains how many characters should be skiped after an HTML entity has been processed. */
	private int remainingCode = 0;
	/** Comtains the previous value of remainingCode. This is to allow doing previous() in the presence of HTML entities. */
	private int lastRemainingCode = 0;
	
	/** Creates a new PreviousHtmlWordFinder object. */
	public PreviousHtmlWordFinder() {}
	
	/**
	 * Creates a new PreviousHtmlWordFinder object.
	 * @param inText The text to process.
	 */
	public PreviousHtmlWordFinder(String inText) {
		super(inText);
	}

	/**
	 * Returns the next word keeping track of the previous word and goes forward one word in the sequence.
	 * It also avoids words in tags and converts special characters.
	 */
	public Word next() {
		if (this.getPreviousWord() != null) // This is because on object construction this method is checked.
			this.getPreviousWord().copy(currentWord);

	    if (nextWord == null) {
	      throw new WordNotFoundException("No more words found.");
	    }
	    currentWord.copy(nextWord);
	    setSentenceIterator(currentWord);

	    int i = currentWord.getEnd() + this.getRemainingCode();
	    this.setLastRemainingCode(this.getRemainingCode());
	    int remaining = 0;

	    boolean finished = false;
	    
	    StringBuffer word = null;
	    StringBuffer code;
	    char c;
	    boolean doRead, zero;

	    while (i < text.length() && !finished) {
	    	c = text.charAt(i);
	    	
	    	if (c == '<') {
	    		// Ignore things inside tags.
				int i2 = ignore(i, '<', '>');
				i = (i2 == i ? i + 1 : i2);
	    	
	    	} else { 
	    		if (c == '&') {
	    			// Decode entity.
		    		code = new StringBuffer();
		    		doRead = true;
		    		zero = false;
		    		
		    		while(doRead && i < text.length() && !zero) {
		    			c = text.charAt(++i); 			
		    			if (c == ';')
		    				doRead = false;
		    			else if(!Character.isLetterOrDigit(c) && c != '#')
		    				zero = true;
		    			else {
		    				code.append(c);
		    			}
		    			
		    			remaining++;
		    		}
		    		
		    		if (zero)
		    			c = (char) 0;
		    		else
		    			c = (char) HtmlCoder.decodeHtmlEntity(code.toString());
	    	   	}
	    		
				if (Character.isLetter(c)) {
					if (word == null) {
						word = new StringBuffer();
						nextWord.setStart(i);
					}
					word.append(c);
				} else if (word != null)
					finished = true;
				
				i++;
			}
	    }
	    
	    if (word == null)
	    	nextWord = null;
	    else
	    	nextWord.setText(word.toString());
	    
	    this.setRemainingCode(remaining);

	    return currentWord;
	}
	
	/** Returns the previous word and goes back one word in the sequence. */
	public Word previous() {
		if (nextWord != null)
			nextWord.copy(currentWord);
		else
			nextWord = new Word(currentWord);
		
		currentWord.copy(this.getPreviousWord());
		
		this.setRemainingCode(this.getLastRemainingCode());
		
		return currentWord;
	}

	private Word getPreviousWord() {
		return previousWord;
	}

	private int getRemainingCode() {
		return remainingCode;
	}

	private void setRemainingCode(int remainingCode) {
		this.remainingCode = remainingCode;
	}

	private int getLastRemainingCode() {
		return lastRemainingCode;
	}

	private void setLastRemainingCode(int lastRemainingCode) {
		this.lastRemainingCode = lastRemainingCode;
	}
}
