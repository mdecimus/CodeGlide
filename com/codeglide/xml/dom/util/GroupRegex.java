package com.codeglide.xml.dom.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupRegex {
	Pattern pattern;
	Map<String, Integer> groupNumbers;
	Map<String, String> groupMatches;
	
	public GroupRegex(String regex, Map<String, Integer> groupNumbers) {
		this.setPattern(Pattern.compile(regex));
		this.setGroupNumbers(groupNumbers);
		this.setGroupMatches(new HashMap<String, String>());
	}

	/**
	 * Tests if an input string matches the pattern.
	 * Also saves the matching groups if there is a match.
	 * 
	 * @param inputString String to be matched.
	 * @return True if the pattern matches the input string.
	 */
	public boolean matches(String inputString) {
		Matcher matcher = this.getPattern().matcher(inputString);
		boolean matchFound = matcher.matches();
		
		if (matchFound) {
			Integer number;
			String groupStr;
			for (String group : this.getGroupNumbers().keySet()) {
				number = this.getGroupNumbers().get(group);
				groupStr = matcher.group(number);
				this.getGroupMatches().put(group, groupStr);
			}
		}
		
		return matchFound;
	}
	
	public String getMatch(String group) {
		return this.getGroupMatches().get(group);
	}

	public Map<String, Integer> getGroupNumbers() {
		return groupNumbers;
	}

	public void setGroupNumbers(Map<String, Integer> groupNumbers) {
		this.groupNumbers = groupNumbers;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public Map<String, String> getGroupMatches() {
		return groupMatches;
	}

	public void setGroupMatches(Map<String, String> groupMatches) {
		this.groupMatches = groupMatches;
	}
}
