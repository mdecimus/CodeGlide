package com.codeglide.interfaces;

import java.util.List;

import org.w3c.dom.Node;

public interface LeavesNode {

	public List<Node> getLeaves();
	public List<Node> getLeaves(String nodeName, String sortBy, int rangeStart, int rangeEnd );
	
}
