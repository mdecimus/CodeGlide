package com.codeglide.xml.dom.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.exceptions.ExpressionException;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.dom.VirtualElement;

/*
  			<field id="To" name="To" type="index" format="stringtokenizer">
				<define name="inputRegex" value="\"([^"]*)\"\\s*<([^@]*@[^@]*)>" group="Name,Addr"/>
				<define name="inputRegex" value="$Name <$Addr>"/>
				<define name="inputRegex" value="<$Addr>"/>
				<define name="inputRegex" value="$Addr ($Name)"/>
				<define name="inputRegex" value="$Addr"/>
				<define name="outputExpression" value=""$Name" <$Addr>"/>
				<define name="quotechars" value="&amp;"/>
				<define name="separatorchars" value=",;"/>
				<define name="escapechars" value="\"/>
				<define name="childname" value="Address"/>
			</field>
 * 
 *    
		String example =
		 "<hola@hola>, \"Juan\" <a@a>, <prueba@prueba.com>, \\\"; \"Pepe <pepe@pepe.com>, P\\\"epe2 <pepe2@pepe2.com>\" <test@test.com>, Arbusto\\, Jorge <user@domain.com>"
		 +
		 ", <user@domain.com>, test@test.com; Ms. Petra Waldburger <pwaldburger@testdomain.com>;;; name.last.name@domain.com ,, j+o-e=l?i.s_t@hardaddresses.com ; happycamper@domain.cc (Camper\\, Happy)"
		 +
		 ", \"Doe, John\" <jdoe@company.com>,pepe@pepe.com;Prueba Test<test@tes.com>,\\,\\,\\,User\\,\\,\\,\\, <user@user.com>;    jaja@jaja.com (\\;Tester\\(\\)) ;;;;;;;;;;      <jejeje@ejjeej.com>;  invalido; invalido (invalido); invalido <invalido>; valido valido\\; valido <valido@valido.com>  ;  , , , , ; , \"Testing@Testing\\\" testing\\\" testing\" <testing@moretests.com>;addr@addr.com";
 
		parse(example);
 * 
 * Address
 *   @Name = Jorge "George" Arbusto
 *   @Addr = jarbusto@casablanca.gov
 *   
 * Address
 *   @Addr = user@domain.com
 * 
 */

public class StringTokenizer extends VirtualElement {
	private Collection<Character> quoteChars;
	private Collection<Character> escapeChars;
	private List<Character> separatorChars;
	private String childName;
	private List<String> outputExpressions;
	private List<Expression> matchExpressions;
	private String defaultOutputExpression;
	private Collection<GroupRegex> inputRegexes;
	private DynamicAttr stringAttr;
	
	public StringTokenizer(Document parentDoc, String name) {
		super(parentDoc, name);
		this.quoteChars = new ArrayList<Character>();
		this.escapeChars = new ArrayList<Character>();
		this.separatorChars = new ArrayList<Character>();
		this.inputRegexes = new ArrayList<GroupRegex>();
		this.outputExpressions = new ArrayList<String>();
		this.matchExpressions = new ArrayList<Expression>();
	}
	
	public void setAttribute(DynamicAttr stringAttr) {
		this.stringAttr = stringAttr;
	}
	
	public void addQuoteChar(Character quoteChar) {
		this.quoteChars.add(quoteChar);
	}
	
	public void addEscapeChar(Character escapeChar) {
		this.escapeChars.add(escapeChar);
	}
	
	public void addSeparatorChar(Character separatorChar) {
		this.separatorChars.add(separatorChar);
	}
	
	public void setChildName(String childName) {
		this.childName = childName; 
	}
	
	public void addInputRegex(GroupRegex inputRegex) {
		this.getInputRegexes().add(inputRegex);
	}
	
	public void addOutputExpression(String outputExpression, Expression matchExpression) {
		if (matchExpression != null) {
			this.getOutputExpressions().add(outputExpression);
			this.getMatchExpressions().add(matchExpression);
		} else
			this.setDefaultOutputExpression(outputExpression);
	}
	
	public Node appendChild(String name) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@not-supported");
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		
		if (this.getChildren() != null && !this.getChildren().isEmpty()) {
			// Use first separator.
			char separator = this.getSeparatorChars().get(0);
			
			List<Node> children = this.getChildren();
			
			Node child;
			String outputExpression;
			
			// Append the first child.
			child = children.get(0);
			outputExpression = outputExpressionForChild(child);
			if (outputExpression != null)
			  result.append(this.outputStringForChild(child, outputExpression));
		
			// Append the rest.
			for (int i = 1; i < children.size(); i++) {				
				result.append(separator);
				child = children.get(i);
				outputExpression = outputExpressionForChild(child);
				if (outputExpression != null)
				  result.append(this.outputStringForChild(child, outputExpression));
			}
		}
		
		return result.toString();
	}

	public void parse(String inputString) {		
		this.children = null;
		if( inputString != null ) {
			Collection<String> tokenList = this.tokenize(inputString); 
			if (tokenList != null) {
				DynamicElement childNode;
				
				for (String token : tokenList) {				
					for (GroupRegex groupRegex : this.getInputRegexes()) {					
						if (groupRegex.matches(token)) {						
							childNode = new DynamicElement(this.parentDoc, this.childName);
							for (String group : groupRegex.getGroupMatches().keySet()) {
								childNode.setAttribute(group, groupRegex.getMatch(group));					
							}
							this.appendChild(childNode);
							
							break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Tokenizes a string using the information of quote chars, escape chars and separator chars.
	 * 
	 * @param inputString String to be tokenized.
	 * @return Tokens obtained. 
	 */
	private Collection<String> tokenize(String inputString) {
		StringBuffer token = new StringBuffer();
		char c, quoteChar;
		Collection<String> tokenList = new ArrayList<String>();
		
		int i = 0;
		int length = inputString.length();
		
		while (i < length) {
			c = inputString.charAt(i);
			while (i < length && !this.getQuoteChars().contains(c) && !this.getEscapeChars().contains(c) && !this.getSeparatorChars().contains(c)) {
				token.append(c); if (++i < length) c = inputString.charAt(i);
			}
			if (i < length) {		
				// Quote char.
				if (this.getQuoteChars().contains(c)) {
					quoteChar = c;
					token.append(c); if (++i < length) c = inputString.charAt(i);
					do {
						while (i < length && c != quoteChar && !this.getEscapeChars().contains(c)) {
							token.append(c); if (++i < length) c = inputString.charAt(i);
						}
						if (i < length && this.getEscapeChars().contains(c)) {
							if (++i < length) c = inputString.charAt(i);
							if (i < length)	{
								token.append(c); if (++i < length) c = inputString.charAt(i);
							}
						}
					} while (i < length && c != quoteChar);
					if (c == quoteChar) {
						token.append(c); i++;
						if (i == length) {
							tokenList.add(token.toString());
							token = new StringBuffer();
						}
					}
						
				// Escape char.
				} else if (this.getEscapeChars().contains(c)) {
					if (++i < length ) c = inputString.charAt(i);
					if (i < length) {
						token.append(c); i++;
						if (i == length) {
							tokenList.add(token.toString());
							token = new StringBuffer();
						}
					}
				
				// Separator char.
				} else {
					tokenList.add(token.toString());
					token = new StringBuffer();
					i++;
				}
			} else {
				tokenList.add(token.toString());
				token = new StringBuffer();
			}
		}
		
		return tokenList;
	}
	
	/**
	 * Obtains a string with information of a node in the format of a given output expression.
	 * 
	 * @param child The node.
	 * @param child The output expression.
	 * @return A string in the format of the output expression.
	 */
	private String outputStringForChild(Node child, String outputFormat) {
		StringBuffer output = new StringBuffer();
		StringBuffer attribute;
		String value;
		
		char c;
		int i = 0;
		int length = outputFormat.length();
		
		while (i < length) {
			c = outputFormat.charAt(i);
			while (i < length && c != '$') {
				output.append(c); if (++i < length) c = outputFormat.charAt(i);
			}
			// Attribute.
			if (c == '$' && ++i < length) {
				c = outputFormat.charAt(i);
				if (Character.isLetterOrDigit(c)) {
					attribute = new StringBuffer();
					while (i < length && Character.isLetterOrDigit(c)) {
						attribute.append(c); if (++i < length) c = outputFormat.charAt(i);
					}
					value = ((DynamicElement) child).getAttribute(attribute.toString());
					if (value != null) {
						output.append(this.escapeSpecialChars(value));
					}
				}
			}
		}
		
		return output.toString();
	}

	/**
	 * Escapes quote, separator and escape chars from a given string.
	 * @param value The string.
	 * @return The string with the special chars escaped.
	 */
	private StringBuffer escapeSpecialChars(String value) {
		StringBuffer result = new StringBuffer();
		char c;
		
		for (int i = 0; i < value.length(); i++) {
			c = value.charAt(i);
			if (this.getQuoteChars().contains(c) || this.getEscapeChars().contains(c) || this.getSeparatorChars().contains(c))
				result.append('\\');
			result.append(c);
		}
		
		return result;
	}

	/**
	 * Obtains an output expression for a node.
	 * It looks for a match expressions.
	 * If there is not a match expression for this node it returns the default output expression.
	 * 
	 * @param child The node.
	 * @return The output expression for the node.
	 */
	private String outputExpressionForChild(Node child) {
		List<Expression> matchExpressions = this.getMatchExpressions();
		List<String> outputExpressions = this.getOutputExpressions();

		for (int i = 0; i < matchExpressions.size(); i++) {
			try {
				if((Boolean) matchExpressions.get(i).evaluate(null, child, Expression.BOOLEAN))
					return outputExpressions.get(i); 
			} catch (ExpressionException _) {}
			
		}

		return this.getDefaultOutputExpression();
	}

	public String getChildName() {
		return childName;
	}

	private Collection<Character> getEscapeChars() {
		return escapeChars;
	}

	private List<String> getOutputExpressions() {
		return outputExpressions;
	}

	private Collection<Character> getQuoteChars() {
		return quoteChars;
	}

	private List<Character> getSeparatorChars() {
		return separatorChars;
	}

	private Collection<GroupRegex> getInputRegexes() {
		return inputRegexes;
	}

	private String getDefaultOutputExpression() {
		return defaultOutputExpression;
	}

	private void setDefaultOutputExpression(String defaultOutputExpression) {
		this.defaultOutputExpression = defaultOutputExpression;
	}

	private List<Expression> getMatchExpressions() {
		return matchExpressions;
	}

	/*
	// TODO Remove this test method.
	public static void main(String[] args) {
		StringTokenizer strTok = new StringTokenizer(new DummyDocument(), "Token");
		
		// Test initialization.
		strTok.addQuoteChar('"');
		strTok.addEscapeChar('\\');
		strTok.addSeparatorChar(','); strTok.addSeparatorChar(';');
		strTok.setChildName("Address");
		strTok.addOutputExpression("\"$Name\" <$Addr>", "!contains(@Name,',')");
		strTok.addOutputExpression("$Name <$Addr>", "!string-length(@Name)>0");
		strTok.addOutputExpression("$Addr", null);
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		map.put("Name", 1);	map.put("Addr", 2);
		strTok.addInputRegex(new GroupRegex("\\s*\"(.*)\"\\s*<([^@\\s]+@[^@\\s]+)>\\s*", map));
		
		map = new HashMap<String, Integer>();
		map.put("Name", 1);	map.put("Addr", 2);
		strTok.addInputRegex(new GroupRegex("\\s*([^\\s].*[^\\s])\\s*<([^@\\s]+@[^@\\s]+)>\\s*", map));
		
		map = new HashMap<String, Integer>();
		map.put("Addr", 1);
		strTok.addInputRegex(new GroupRegex("\\s*<([^@\\s]+@[^@\\s]+)>\\s*", map));
	
		map = new HashMap<String, Integer>();
		map.put("Addr", 1); map.put("Name", 2);
		strTok.addInputRegex(new GroupRegex("\\s*([^@\\s]+@[^@\\s]+)\\s*\\((.*)\\)\\s*", map));
		
		map = new HashMap<String, Integer>();
		map.put("Addr", 1);
		strTok.addInputRegex(new GroupRegex("\\s*([^@\\s]+@[^@\\s]+)\\s*", map));
		
		// Parse.		
		String prueba
		 = "<hola@hola>, \"Juan\" <a@a>, <prueba@prueba.com>, \\\"; \"Pepe <pepe@pepe.com>, P\\\"epe2 <pepe2@pepe2.com>\" <test@test.com>, Arbusto\\, Jorge <user@domain.com>"
		 +
		 ", <user@domain.com>, test@test.com; Ms. Petra Waldburger <pwaldburger@testdomain.com>;;; name.last.name@domain.com ,, j+o-e=l?i.s_t@hardaddresses.com ; happycamper@domain.cc (Camper\\, Happy)"
		 +
		 ", \"Doe, John\" <jdoe@company.com>,pepe@pepe.com;Prueba Test<test@tes.com>,\\,\\,\\,User\\,\\,\\,\\, <user@user.com>;    jaja@jaja.com (\\;Tester\\(\\)) ;;;;;;;;;;      <jejeje@ejjeej.com>;  invalido; invalido (invalido); invalido <invalido>; valido valido\\; valido <valido@valido.com>  ;  , , , , ; , \"Testing@Testing\\\" testing\\\" testing\" <testing@moretests.com>;addr@addr.com";
		
		strTok.parse(prueba);
		
		DynamicElement childNode = new DynamicElement(strTok.parentDoc, strTok.childName);
		childNode.setAttribute("Name", "Juan");
		childNode.setAttribute("Addr", "juan@mail.com");
		strTok.appendChild(childNode);
		
		childNode = new DynamicElement(strTok.parentDoc, strTok.childName);
		childNode.setAttribute("Addr", "gaston@mail.com");
		strTok.appendChild(childNode);
		
		childNode = new DynamicElement(strTok.parentDoc, strTok.childName);
		childNode.setAttribute("Addr", "al@gmail.com");
		childNode.setAttribute("Name", "Augusto Lopez");
		strTok.appendChild(childNode);
		
		childNode = new DynamicElement(strTok.parentDoc, strTok.childName);
		childNode.setAttribute("Addr", "test@test.com");
		childNode.setAttribute("Name", "Pepe <pepe@pepe.com>, P\"epe2 <pepe2@pepe2.com>");
		strTok.appendChild(childNode);
		
		childNode = new DynamicElement(strTok.parentDoc, strTok.childName);
		childNode.setAttribute("Addr", "test@test.com");
		childNode.setAttribute("Name", ";Tester()");
		strTok.appendChild(childNode);
		
		
		
		
		System.out.println(strTok.toString());
	}*/
}
