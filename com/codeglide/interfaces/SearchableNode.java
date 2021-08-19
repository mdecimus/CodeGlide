package com.codeglide.interfaces;

import java.util.List;

import org.w3c.dom.Node;

import com.codeglide.xml.dom.DynamicElement;

/*
 * Search object definition
 * 
 * SearchObject
 *   @Type = Object Type (null, search all types)
 *   @Folder = List of folder IDs where search has to be performed
 *   @Text = Peform full text search
 *   
 *   SearchField
 *     @Name = Fields to search for string
 *     @Type = contains, equals, greaterThan, lowerThan, startsWith, endsWith
 *     @Value = Value to search for
 *     
 *   SearchGroup
 *    @Type = And, Or, Not
 * 
 */

public interface SearchableNode {

	public List<Node> search( DynamicElement searchObject, String sortBy, int rangeStart, int rangeEnd );
	
}
