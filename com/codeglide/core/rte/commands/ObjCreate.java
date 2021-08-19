package com.codeglide.core.rte.commands;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.objects.ObjectDefinition;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.ExpressionException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.core.rte.variables.VariableResolver;
import com.codeglide.xml.dom.DummyNodeList;
import com.codeglide.xml.dom.DynamicAttrCalculated;
import com.codeglide.xml.dom.DynamicAttrString;
import com.codeglide.xml.dom.DynamicElement;

public class ObjCreate extends Command {
	private Expression targetExpression, nameExpression, sourceExpression;
	private List<RawObject> rawObjects;
	
	public ObjCreate(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		targetExpression = getExpression(element, "target");
		nameExpression = getExpression(element, "name");
		sourceExpression = getExpression(element, "source");
		if( nameExpression == null && sourceExpression == null ) {
			rawObjects = new LinkedList<RawObject>();
			for( Element child : getChildrenElements(element) )
				rawObjects.add(new RawObject(this, child));
		} else
			rawObjects = null;
	}

	public void run(Context context, List<Action> result) throws CodeGlideException {
		List<Node> items = new LinkedList<Node>();
		
		if( rawObjects != null ) {
			for( RawObject obj : rawObjects )
				items.add(obj.buildObject(context.getDocumentNode()));
		} else {
			if( nameExpression != null ) {
				String nodeName = nameExpression.evaluate(context.getVariables(), context.getDocumentNode());
				ObjectDefinition obj = context.getApplication().getObject(nodeName);
				if( obj != null )
					items.add(obj.buildObject(context.getDocumentNode()));
				else
					items.add(new DynamicElement(context.getDocumentNode(), nodeName));
			} else {
				// Clone an existing object
				DynamicElement sourceNode = (DynamicElement)sourceExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODE);
				
				if( sourceNode == null )
					return;
				items.add((DynamicElement)sourceNode.cloneNode(true));
			}
		}
		
		//TODO find a nicer way to express setting information on variables i.e. var:target, var:bind, etc.
		if( targetExpression.isType(Expression.T_STRING) ) {
			// Add to variable
			
			VariableResolver variables = context.getVariables();
			String varName = targetExpression.toString();
			
			if( variables.getVariableType(varName) == VariableHolder.OBJECTARRAY ) {
				NodeList nodes = (NodeList) variables.resolveVariable(varName);
				if( nodes instanceof DummyNodeList ) {
					boolean isEmpty = nodes.getLength() < 1;
					for( Node item : items)
						((DummyNodeList)nodes).add(item);
					if( isEmpty)
						variables.setVariable(varName, nodes);
				} else {
					DummyNodeList resultNodes = new DummyNodeList(new LinkedList<Node>());
					for( int i = 0; i < nodes.getLength(); i++ )
						resultNodes.add(nodes.item(i));
					for( Node item : items)
						resultNodes.add(item);
					variables.setVariable(varName, resultNodes);
				}
			} else if( variables.getVariableType(varName) == VariableHolder.OBJECT )
				variables.setVariable(varName, items.get(0));
			else
				Logger.debug("Variable '"+varName+"' has to be an object or object array to be used as an createObject target.");
		} else {
			// Add to node
			DynamicElement target = ((DynamicElement)targetExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODE));
			for( Node item : items)
				target.appendChild(item);
		}
		
	}

	protected class RawObject extends Item {
		private int nameId;
		private List<KeyPair> attributes, calculated;
		private List<RawObject> children;
		private Application app;
		
		public RawObject(Item parent, Element element) {
			super(parent, element);
		}

		public String getName() {
			return app.getString(nameId);
		}
		
		public void addAttribute( String name, Expression value ) {
			if( attributes == null )
				attributes = new LinkedList<KeyPair>();
			attributes.add(new KeyPair(app.getId(name), value));
		}

		public void addCalculated( String name, Expression value ) {
			if( calculated == null )
				calculated = new LinkedList<KeyPair>();
			calculated.add(new KeyPair(app.getId(name), value));
		}

		public DynamicElement buildObject(Document doc) {
			DynamicElement obj = new DynamicElement(doc, app.getString(nameId));
			
			// Set attributes
			if( attributes != null ) {
				Iterator<KeyPair> it = attributes.iterator();
				while( it.hasNext() ) {
					KeyPair item = it.next();
					String value = null;
					try {
						if( item.getValue() != null )
							value = item.getValue().evaluate(null, doc);
					} catch (ExpressionException _) {
					}
					obj.setAttributeNode(new DynamicAttrString(obj, app.getString(item.getNameId()), value));
				}
			}
			
			// Set calculated fields
			if( calculated != null ) {
				Iterator<KeyPair> it = calculated.iterator();
				while( it.hasNext() ) {
					KeyPair item = it.next();
					obj.setAttributeNode(new DynamicAttrCalculated(obj, app.getString(item.getNameId()), item.getValue()));
				}
			}
			
			// Add children
			if( children != null ) {
				Iterator<RawObject> it = children.iterator();
				while( it.hasNext() )
					obj.appendChild(it.next().buildObject(doc));
			}
			
			return obj;
		}
		
		public void addChild(RawObject tpl) {
			if( children == null )
				children = new LinkedList<RawObject>();
			children.add(tpl);
		}
		
		private class KeyPair {
			int nameId;
			Expression value;
			
			public KeyPair(int nameId, Expression value) {
				this.nameId = nameId;
				this.value = value;
			}

			public int getNameId() {
				return nameId;
			}
			
			public void setNameId(int nameId) {
				this.nameId = nameId;
			}
			
			public Expression getValue() {
				return value;
			}
			
			public void setValue(Expression value) {
				this.value = value;
			}
			
		}

		protected void parseElement(Element element, Application application) {
			this.app = application;
			this.nameId = application.getId(element.getNodeName());

			if (element.hasAttributes()) {
				NamedNodeMap attributes = element.getAttributes();

				for (int j = 0; j < attributes.getLength(); j++) {

					Node attribute = attributes.item(j);

					String name = attribute.getNodeName();
					String value = attribute.getNodeValue();

					Expression expression;

					if (value.isEmpty())
						expression = null;
					else
						expression = new Expression(value);

					if (name.startsWith("x:")) {
						addCalculated(name.substring(2),
								expression);
					} else
						addAttribute(name, expression);
				}
			}

			for( Element child : getChildrenElements(element) )
				addChild(new RawObject(this, child));

		}
		
	}
}
