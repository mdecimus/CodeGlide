package com.codeglide.util.spell;

import java.io.IOException;
import java.io.StringReader;

import com.codeglide.xml.dom.DynamicAttr;
import com.swabunga.spell.engine.SpellDictionaryHashMap;

/** A dictionary that uses the word list in a DynamicAttr object. */
public class DynamicAttrDictionary extends SpellDictionaryHashMap {
	DynamicAttr wordList;

	/**
	* DynamicAttrDictionary constructor.
	* @throws java.io.IOException indicates a problem with the file system.
	*/
	public DynamicAttrDictionary(DynamicAttr wordList) throws IOException {
		super(new StringReader(wordList.getValue().replace(',', '\n')));
		this.setWordList(wordList);
	}
	
	/**
	 * Adds a word to the user dictionary used for spell checking and to the one in the DynamicAttr object.
	 * @param word The word to add.
	 */
	public void addWord(String word) {
		DynamicAttr wordList;
		
		wordList = this.getWordList();
		wordList.setValue(wordList.getValue() + "," + word);
		
		super.addWord(word);
	}

	private DynamicAttr getWordList() {
		return wordList;
	}

	private void setWordList(DynamicAttr wordList) {
		this.wordList = wordList;
	}	
}
