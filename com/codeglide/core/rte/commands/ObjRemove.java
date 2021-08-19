package com.codeglide.core.rte.commands;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.dom.ExtendedElement;

public class ObjRemove extends Command {
	private Expression targetExpression, sourceExpression;
	
	public ObjRemove(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		targetExpression = getExpression(element, "target");
		sourceExpression = getExpression(element, "source");
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		
		if( sourceExpression != null ) {
			DynamicElement targetNode = (DynamicElement)targetExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODE);
			NodeList sourceNodes = (NodeList)targetExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODELIST);
			
			if( targetNode != null && sourceNodes != null && sourceNodes.getLength() > 0 ) {
				for( int i = 0; i < sourceNodes.getLength(); i++ ) 
					targetNode.removeChild(sourceNodes.item(i));
			}
			
		} else {
			DynamicElement targetNode = ((DynamicElement)targetExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODE));
			if( targetNode.getParentNode() != null )
				targetNode.getParentNode().removeChild(targetNode);
			else if( targetNode instanceof ExtendedElement )
				((ExtendedElement)targetNode).delete();
			
			//FIXME fix this
			/*NodeList targetNodes = (NodeList)targetExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODELIST);
			if( targetNodes != null && targetNodes.getLength() > 0 ) {
				for( int i = 0; i < targetNodes.getLength(); i++ ) {
					DynamicElement targetNode = (DynamicElement)targetNodes.item(i);
					if( targetNode.getParentNode() != null )
						targetNode.getParentNode().removeChild(targetNode);
					else if( targetNode instanceof ExtendedElement )
						((ExtendedElement)targetNode).delete();
					//else
					//	throw new CodeglideRuntimeException("@orphan-node-deletion");
				}
			}*/
		}

	}

}
