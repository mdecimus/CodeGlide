package com.codeglide.util.spell;

import com.swabunga.spell.event.Word;
import com.swabunga.spell.event.WordFinder;

/** Interface for a word finder that can return the previous word. */
public interface PreviousWordFinder extends WordFinder {
	/** Returns the previous word. */
	public Word previous();
}
