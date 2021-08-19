package com.codeglide.core.rte.commands;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.core.rte.variables.VariableResolver;
import com.codeglide.xml.dom.DummyNodeList;
import com.codeglide.xml.dom.DynamicElement;

public class ObjAdd extends Command {
	private Expression targetExpression, sourceExpression, resultExpression;
	
	public ObjAdd(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		sourceExpression = getExpression(element, "source");
		targetExpression = getExpression(element, "target");
		resultExpression = getExpression(element, "result");
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		DynamicElement sourceNode = (DynamicElement)sourceExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODE);
		
		if( sourceNode == null )
			return;
		
		//TODO find a nicer way to express setting information on variables i.e. var:target, var:bind, etc.
		if( targetExpression.isType(Expression.T_STRING) ) {
			// Add to variable
			
			VariableResolver variables = context.getVariables();
			String varName = targetExpression.toString();
			
			if( variables.getVariableType(varName) == VariableHolder.OBJECTARRAY ) {
				NodeList nodes = (NodeList) variables.resolveVariable(varName);
				if( nodes instanceof DummyNodeList ) {
					boolean isEmpty = nodes.getLength() < 1;
					((DummyNodeList)nodes).add(sourceNode);
					if( isEmpty)
						variables.setVariable(varName, nodes);
				} else {
					DummyNodeList resultNodes = new DummyNodeList(new LinkedList<Node>());
					for( int i = 0; i < nodes.getLength(); i++ )
						resultNodes.add(nodes.item(i));
					resultNodes.add(sourceNode);
					variables.setVariable(varName, resultNodes);
				}
			} else
				Logger.debug("Variable '"+varName+"' has to be an object array to be used as an addObject target.");
		} else {
			// Add to node
			DynamicElement target = ((DynamicElement)targetExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODE));
			Node resultNode = target.appendChild(sourceNode);
			if( resultExpression != null ) {
				VariableResolver variables = context.getVariables();
				String resultVar = resultExpression.evaluate(context.getVariables(), context.getDocumentNode());
				if( variables.getVariableType(resultVar) != -1 )
					variables.setVariable(resultVar, resultNode);
			}
		}
	}

}
