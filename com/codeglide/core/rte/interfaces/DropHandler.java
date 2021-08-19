package com.codeglide.core.rte.interfaces;

import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;

public interface DropHandler {
	public void handleDrop(ContextUi context, List<Action> result, Node targetNode, NodeList sourceNodes ) throws CodeGlideException;
}
