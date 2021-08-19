package com.codeglide.util.spell;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.core.Expression;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;
import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;

/*
 * 
 * Spellchecker Subnodes
 * 
 * Replace
 * ReplaceAll
 * Ignore
 * IgnoreAll
 * Add
 * NextSuggestion
 * 
 * Word node attributes
 *  @Value
 *  
 * 
 */

/** This class is used for spell checking. */
public class SpellChecker extends DynamicElement implements SpellCheckListener {
	/** A helper for spell checking. */
	com.swabunga.spell.event.SpellChecker spellChecker;
	/** The language used. */
	String language;
	/** The tokenizer. */
	PreviousWordTokenizer input;
	/** The user's dictionary. */
	SpellDictionary userDictionary;
	
	/** A reference to the child NextSuggestion. */
	Node nextSuggestionNode;
	
	/**	Indicates if an action can be taken. */
	boolean doAction;
	
	/** An action that will be taken. */
	private short action;
	/** A new word for replacement or to add to a user dictionary. */
	private String newWord;
	
	// The Command pattern could be considered for the following commands.
	/** Field indicating that the incorrect word should be ignored. */
	private static final short IGNORE = 0;
	/** Field indicating that the incorrect word should be ignored forever. */
	private static final short IGNOREALL = 1;
	/** Field indicating that the incorrect word should be replaced. */
	private static final short REPLACE = 2;
	/** Field indicating that the incorrect word should be replaced always. */
	private static final short REPLACEALL = 3;
	/** Field indicating that the incorrect word should be added to the dictionary. */
	private static final short ADDTODICT = 4;

	/**
	* Creates a new SpellChecker object.
	* @param parentDoc The parent document.
	*/
	public SpellChecker(Document parentDoc) {
		super(parentDoc, "SpellChecker");
		
		spellChecker = new com.swabunga.spell.event.SpellChecker();
		spellChecker.addSpellCheckListener(this);
		
		// Children.
		appendChild(new Replace(parentDoc));
		appendChild(new ReplaceAll(parentDoc));
		appendChild(new Ignore(parentDoc));
		appendChild(new IgnoreAll(parentDoc));
		appendChild(new Add(parentDoc));
		nextSuggestionNode = new DynamicElement(parentDoc, "NextSuggestion");
		appendChild(nextSuggestionNode);
	}
	
	/**
	 * Sets the language of the input text. The default language is American English.
	 * @param lang The language.
	 */
	public void setLanguage(String lang) {
		if (lang == null)
			lang = "us_en";
		
		try {
			SpellDictionary dictionary = new SpellDictionaryHashMap(new File("resources/dict/" + lang + ".dic"));
			this.setDictionary(dictionary);
			this.language = lang;
		} catch (Exception _) {}
	}
	
	/**
	 * Sets a dictionary for the spell check where each word is in a separate line.
	 * @param wordList The dictionary.
	 */
	private void setDictionary(SpellDictionary wordList) {
		this.getSpellChecker().addDictionary(wordList);
	}
	
	private void setUserDictionary(SpellDictionary dictionary) {
		this.getSpellChecker().setUserDictionary(dictionary);
		this.userDictionary = dictionary;
	}
	
	/**
	 * Sets the user dictionary for the added words. The words are separated by commas.
	 * @param wordList A word list.
	 */
	public void setUserDictionary(String wordList) {
		Reader reader = new StringReader(wordList.replace(',', '\n'));

		try {
			SpellDictionary dict = new SpellDictionaryHashMap(reader);
			this.setUserDictionary(dict);
		} catch (IOException _) {}		
	}

	/**
	 * Sets the user dictionary for the added words. The words are separated by commas.
	 * @param wordList A word list.
	 */
	public void setUserDictionary(DynamicAttr wordList) {
		try {
			this.setUserDictionary(new DynamicAttrDictionary(wordList));
		} catch (IOException _) {}
	}

	/**
	 * Sets the text for check spelling.
	 * @param input The text.
	 */
	public void setInput(DynamicAttr input) {
		boolean isHtml = false;
		
		// Verify if the input is HTML.
		try {
			
			Expression expContentType = (Expression)input.getFieldDefinition().getSetting(ObjectField.E_CONTENT_TYPE);
			isHtml = expContentType != null && expContentType.evaluate(null, input.getParentNode()).equalsIgnoreCase("text/html");
		} catch (Exception _) {
		}

		setInput(input.getValue(), isHtml);
	}

	/**
	 * Sets the text for check spelling indicating if the text is HTML.
	 * @param input An attribute with the text.
	 * @param isHtml A boolean indicating if the text is HTML.
	 */
	public void setInput(DynamicAttr input, boolean isHtml) {
		this.setInput(input.getValue(), isHtml);
	}
	
	/**
	 * Sets the text for check spelling indicating if the text is HTML.
	 * @param input The text.
	 * @param isHtml A boolean indicating if the text is HTML.
	 */
	public void setInput(String input, boolean isHtml) {
		PreviousWordFinder finder;
		
		if (isHtml)
			finder = new PreviousHtmlWordFinder(input);
		else
			finder = new PreviousDefaultWordFinder(input);
		
		this.input = new PreviousStringWordTokenizer(input, finder);
	}
	
	/**
	 * Handles an event received when a misspelled word is encountered.
	 * @param event The event.
	 */
	public void spellingError(SpellCheckEvent event) {
		if (this.isDoAction()) {
			switch (this.getAction()) {
			  case SpellCheckEvent.IGNORE: event.ignoreWord(false); break;		      
		      case SpellCheckEvent.IGNOREALL: event.ignoreWord(true); break;
		      case SpellCheckEvent.REPLACE: event.replaceWord(this.getNewWord(), false); break;
		      case SpellCheckEvent.REPLACEALL: event.replaceWord(this.getNewWord(), true); break;
		      case SpellCheckEvent.ADDTODICT: event.addToDictionary(this.getNewWord()); break;
		    }
			this.setDoAction(false);
		} else {
			this.loadNextSuggestion(event.getInvalidWord(), event.getSuggestions());
			event.cancel();
			this.getInput().previousWord();
			this.setDoAction(true);
		}
	}

	/** Starts checking and gives suggestions for a misspelled word. */
	public void startCheckSpelling() {
		this.reset();
		this.checkSpelling();
	}
	
	/** Resets the spell checker. */
	private void reset() {
		this.getSpellChecker().reset();
		this.setDoAction(false);
		this.setAttributeNode(new CorrectedTextAttr(this));
	}
	
	/** Gives suggestions for a misspelled word. */
	private void loadNextSuggestion(String invalidWord, List suggestions) {
		// Remove last suggestion.
		Node nextSuggestionNode = this.getNextSuggestionNode();
		if (nextSuggestionNode.hasChildNodes()) {
			nextSuggestionNode.removeChild(nextSuggestionNode.getFirstChild());
		}
				
		// Add invalid word.
		Word result = new Word(parentDoc);
		result.setAttribute("value", invalidWord);
		nextSuggestionNode.appendChild(result);
		
		// Add suggestions.
		if (!suggestions.isEmpty()) {
			Suggestions suggestionsNode = new Suggestions(parentDoc);
			Word word;
			for (Iterator i = suggestions.iterator(); i.hasNext();) {
				word = new Word(parentDoc);
				word.setAttribute("value", i.next().toString());
				suggestionsNode.appendChild(word);
			}

			result.appendChild(suggestionsNode);
		}
	}
	
	/**
	 * Sets an action and does a check spelling so the action is done.
	 * @param action The action.
	 */
	private void doAction(short action) {
		this.setAction(action);
		this.checkSpelling();
	}
	
	/**
	 * Checks the spelling of the input till it finds the first mispelled word.
	 * If the checking finishes no suggestion remains.
	 */
	private void checkSpelling() {
		if (this.getSpellChecker().checkSpelling(this.getInput()) != com.swabunga.spell.event.SpellChecker.SPELLCHECK_CANCEL) {
			// Remove the last suggestion when the checking ends.
			Node nextSuggestionNode = this.getNextSuggestionNode();
			if (nextSuggestionNode.hasChildNodes())
				nextSuggestionNode.removeChild(nextSuggestionNode.getFirstChild());
		}
	}
	
	/**
	 * Does the replace action.
	 * @param word The word to be replaced.
	 */
	public void replace(Word word) {
		this.setNewWord(word.getAttribute("value"));
		this.doAction(REPLACE);
	}
	
	/**
	 * Does the replaceAll action.
	 * @param word The word to be replaced.
	 */
	public void replaceAll(Word word) {
		this.setNewWord(word.getAttribute("value"));
		this.doAction(REPLACEALL);
	}
	
	/** Does the ignore action. */
	public void ignore() {
		this.doAction(IGNORE);
	}

	/** Does the ignoreAll action. */
	public void ignoreAll() {
		this.doAction(IGNOREALL);
	}
	
	/**
	 * Does the add action.
	 * @param word The word to be added.
	 */
	public void add(Word word) {
		this.setNewWord(word.getAttribute("value"));
		this.doAction(ADDTODICT);
	}
	
	/** Returns the corrected text after a check spelling. */
	public String getCorrectedText() {
		return this.getInput().getContext();
	}
	
	private Node getNextSuggestionNode() {
		return nextSuggestionNode;
	}

	private com.swabunga.spell.event.SpellChecker getSpellChecker() {
		return spellChecker;
	}

	private PreviousWordTokenizer getInput() {
		return input;
	}

	private boolean isDoAction() {
		return doAction;
	}

	private void setDoAction(boolean doAction) {
		this.doAction = doAction;
	}

	private short getAction() {
		return action;
	}

	private void setAction(short action) {
		this.action = action;
	}

	private String getNewWord() {
		return newWord;
	}

	private void setNewWord(String newWord) {
		this.newWord = newWord;
	}

	/*
	// TODO [Test] Remove test method.
	public static void main(String[] args) throws IOException, XPathExpressionException {
		testSpellChecker();
		// testHtmlTokenizer();
		
				
	}
	*/

	/* Examples:
	* <tag> good morning in spanish is: buen d&iacute;a </tag> other text
	* helo bye bai helo welcome
	* <tag> helo bye </tag> hi <e> bai helo welcome </e> testing
	*/
	
	/*
	// TODO [Test] Remove test method.
	private static void testSpellChecker() throws IOException, XPathExpressionException {
		SpellChecker spellChecker = createSpellChecker();
					
	    boolean isHtml;
	    
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("Enter line to spell check (return to exit): ");
			String line = in.readLine();
			System.out.println();

			if (line.length() == 0) {
				break;
			}
			
			// Set input.
			isHtml = true;
			spellChecker.setInput(line, isHtml);
			
			// Spell checking.
			spellChecker.startCheckSpelling();
			spellChecker.interact();
						
			// The result.
			System.out.println("  Result: " + spellChecker.getAttributeNode("CorrectedText").getValue());
			System.out.println();
		}
	}

	// TODO [Test] Remove test method.
	private static SpellChecker createSpellChecker() {
		SpellChecker spellChecker = new SpellChecker(new DummyDocument());

		// Set language.
		spellChecker.setLanguage(null);		

	    // Set user dictionary.
		DynamicElement node = new DynamicElement(null, null);
		node.setAttribute("WordList", "hola,chau");
		DynamicAttr wordList = (DynamicAttr) node.getAttributeNode("WordList");
		spellChecker.setUserDictionary(wordList);

		return spellChecker;
	}
	
	// TODO [Test] Remove this test method.
	private void interact() throws IOException {
		boolean corrected = false;
		DynamicElement word = null;
		
		while (!corrected) {
			// Check spelling.
			List<Node> children = ((DynamicElement) nextSuggestionNode).getChildren();
			if (children == null || children.isEmpty())
				corrected = true;
			else
				word = (DynamicElement) children.get(0);
			
			if (!corrected) {
				// Prompt for user action.
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				
				System.out.print("> The invalid word is: " + word.getAttribute("value") + ". ");
				this.showSuggestions(word);
				System.out.print("  Enter action (0=Ig, 1=IgAll, 2=Re, 3=ReAll, 4=Add, 5=CorrectedText): ");
				
				String line;
				line = in.readLine();
				
				switch (line.charAt(0)) {
				case '0': 	this.getChildNode("Ignore").appendChild(new Word(parentDoc)); break; // Ignore.
				case '1':	this.getChildNode("IgnoreAll").appendChild(new Word(parentDoc)); break; // Ignore all.
				case '2':	System.out.print("  Enter word to replace: "); // Replace.
							line = in.readLine();
							word = new Word(parentDoc); word.setAttribute("value", line);
							this.getChildNode("Replace").appendChild((Word) word); break;				
				case '3':	System.out.print("  Enter word to replace all: "); // Replace all.
							line = in.readLine();
							word = new Word(parentDoc); word.setAttribute("value", line);
							this.getChildNode("ReplaceAll").appendChild((Word) word); break;
				case '4':	System.out.print("  Enter word to add: "); // Add.
							line = in.readLine();
							word = new Word(parentDoc); word.setAttribute("value", line);
							this.getChildNode("Add").appendChild((Word) word); break;
				case '5':	System.out.println("  Corrected text till now: " + this.getCorrectedText()); break; // Corrected Text.
				}
				
				System.out.println();				
			}
		}
    }

	// TODO [Test] Remove this test method.
	private void showSuggestions(DynamicElement word) {
		if (word.hasChildNodes()) {
			DynamicElement suggestionsNode = (DynamicElement) word.getChildren().get(0);
			
			System.out.print("Suggestions: ");

	        for (Iterator i = suggestionsNode.getChildren().iterator(); i.hasNext();) {
	          System.out.print(((DynamicElement) i.next()).getAttribute("value"));
	          if (i.hasNext()) {
	            System.out.print(", ");
	          }
	        }
	        System.out.println();			
		} else {
			System.out.println("No suggestions found.");
		}
	}
	
	// TODO [Test] Remove this test method.
	private static void testHtmlTokenizer() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		PreviousWordTokenizer tokenizer;
		StringBuffer buffer;
		String word;
		
		while (true) {
			System.out.print("Enter line to tokenize (return to exit): ");
			String line = in.readLine();
			System.out.println();

			if (line.length() == 0) {
				break;
			}
			
			tokenizer = new PreviousStringWordTokenizer(line, new PreviousHtmlWordFinder(line));
			
			buffer = new StringBuffer();
		    while (tokenizer.hasMoreWords()) {
		    	word = tokenizer.nextWord();
		    	buffer.append(word);
		    	if (tokenizer.hasMoreWords())
		    		buffer.append(", ");
		    }
		        
			// The result.
			System.out.println("  Result: " + buffer.toString());
			System.out.println();
		}
	}
	*/
}
