package com.codeglide.core.rte.widgets;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.Function;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.interfaces.CloseHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.variables.Variable;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.core.rte.windowmanager.PanelInstance;

public class RootPanel extends Panel implements CloseHandler {
	protected List<Variable> variableList;
	protected Function onLoadFunction, onCloseFunction;

	public RootPanel(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		for( Element containers : getChildrenElements(element) ) {
			if( containers.getNodeName().equalsIgnoreCase("variables") ) {
				variableList = new LinkedList<Variable>();
				for( Element child : getChildrenElements(containers) ) {
					variableList.add(new Variable(this, child));
				}
			} else if( containers.getNodeName().equalsIgnoreCase("functions") ) {
				for( Element function : getChildrenElements(containers) ) {
					if( function.getNodeName().equals("function") ) {
						if( function.getAttribute("name").equalsIgnoreCase("onload") )
							onLoadFunction = new Function(this, function);
						else if( function.getAttribute("name").equalsIgnoreCase("onclose") )
							onCloseFunction = new Function(this, function);
					}
				}
			}
		}
	}

	public PanelInstance newInstance(ContextUi context, Vector<Expression> inputExpressions) throws CodeGlideException {
		// Create an instance for this panel and a new variable holder
		VariableHolder panelVariables = new VariableHolder();
		PanelInstance pInstance = new PanelInstance();
		pInstance.setVariables(panelVariables);
		pInstance.setPanel(this);
		
		// Set variables
		if( variableList != null  ) {
			int inputVarCount = 0;
			for( Variable var : variableList ) {
				panelVariables.defineVariable(var.getVariableName(), var.getVariableType());

				if( var.getVariableContext() == Variable.INPUT && inputExpressions != null ) {
					short type;
					switch( var.getVariableType() ) {
						case VariableHolder.BOOLEAN:
							type = Expression.BOOLEAN;
							break;
						case VariableHolder.OBJECT:
							type = Expression.NODE;
							break;
						case VariableHolder.STRING:
							type = Expression.STRING;
							break;
						case VariableHolder.NUMBER:
							type = Expression.NUMBER;
							break;
						case VariableHolder.OBJECTARRAY:
						default:
							type = Expression.NODELIST;
							break;
					}
					Expression inputExpression = null;
					if( inputVarCount < inputExpressions.size() )
						inputExpression = inputExpressions.get(inputVarCount);
					if( inputExpression != null )
						panelVariables.setVariable(var.getVariableName(), inputExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), type));
					inputVarCount++;
				}
			}
		}

		return pInstance;
	}
	
	public void handleLoad(ContextUi context, List<Action> result ) throws CodeGlideException {
		if( onLoadFunction != null )
			onLoadFunction.run(context, result);
	}
	
	public void handleClose(ContextUi context, List<Action> result) throws CodeGlideException {
		if( onCloseFunction != null )
			onCloseFunction.run(context, result);
	}

}
