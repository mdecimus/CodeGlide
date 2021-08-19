package com.codeglide.util.spell;

import com.swabunga.spell.event.WordTokenizer;

public interface PreviousWordTokenizer extends WordTokenizer {
	public String previousWord();
}
