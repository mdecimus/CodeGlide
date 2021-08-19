package com.codeglide.core.rte.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.Exit;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;
import com.codeglide.core.rte.variables.Variable;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.core.rte.variables.VariableInput;
import com.codeglide.core.rte.variables.VariableResolver;
import com.codeglide.core.rte.widgets.Button;
import com.codeglide.xml.dom.DummyNodeList;

public class Function extends Command implements SequenceBucketizable {
	private CommandGroup contentsBlock;
	private LinkedList<Variable> variables;
	
	public Function(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		for( Element containers : getChildrenElements(element) ) {
			if( containers.getNodeName().equalsIgnoreCase("variables") ) {
				variables = new LinkedList<Variable>();
				for( Element child : getChildrenElements(containers) ) {
					Variable variable = null;
					if( hasAttribute(child, "inputList") )
						variable = new VariableInput(this, child);
					else
						variable = new Variable(this, child);
					variables.add(variable);
				}
			} else if( containers.getNodeName().equalsIgnoreCase("contents") ) {
				contentsBlock = new CommandGroup(this, containers);
			}
		}
		seqId = -1;
		application.getWidgetBucket().registerObject(this);
		
		if( parent instanceof Button && parent.getAncestor(For.class) != null ) {
			// If this function is called within FOR, we need to persist the variables
			HashSet<String> forVariables = new HashSet<String>();
			Item runner = parent;
			while( runner != null && !(runner instanceof Function) ) {
				if( runner instanceof For ) 
					forVariables.add(((For)runner).getVariableName());
				runner = runner.getParent();
			}
			if( forVariables.size() > 0 ) {
				if( variables == null )
					variables = new LinkedList<Variable>();
				for( String varName : forVariables ) //TODO only add variables if they are really used
					variables.add(new VariableInput(this, varName, VariableHolder.OBJECT, Variable.INPUT, varName));
			}
		}
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		this.run(context, result, null, false);
	}
	
	public Collection<Variable> getVariables() {
		return (variables!=null)?variables : null;
		/*Variable result = null;
		if( variables != null ) {
			for( Variable var : variables.values() ) {
				if( var.getVariableContext() == Variable.INPUT )
					return var;
			}
		}
		return result;*/
	}

	public Object run(Context context, List<Action> result, Object input) throws CodeGlideException {
		return run(context, result, input, false);
	}
	
	public Object run(Context context, List<Action> result, Object input, boolean noExitCatch ) throws CodeGlideException {
		// Obtain variables
		VariableResolver variables = context.getVariables();

		// Output variable
		Variable outputVar = null;

		// Create a new Variable holder
		VariableHolder varHolder = null, previousCallVariables = null;
		String varHolderName = "FNC" + String.valueOf(getSequenceId());
		
		if( this.variables != null && this.variables.size() > 0 ) {
			varHolder = new VariableHolder();
			
			// Find out if the input is a Collection
			Vector<?> inputCollection = null;
			if( input instanceof Collection )
				inputCollection = (Vector<?>)input;
			int inputVarCount = 0;

			for( Variable var : this.variables ) {
				// Define the Variable
				varHolder.defineVariable(var.getVariableName(), var.getVariableType());
				
				if( var.getVariableContext() == Variable.INPUT && input != null ) {

					Object value = null;

					// If the in
					if( inputCollection != null ) {
						if( inputVarCount < inputCollection.size() )
							value = inputCollection.get(inputVarCount);
					} else if( inputVarCount == 0 )
						value = input;
						
					if( value != null ) {
						// Calculate expression
						if( value instanceof Expression ) {
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
							
							value = ((Expression)value).evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), type);
						} else {
							switch( var.getVariableType() ) {
								case VariableHolder.OBJECT:
									if( value instanceof NodeList ) {
										if ( ((NodeList)value).getLength() > 0 )
											value = ((NodeList)value).item(0);
										else 
											value = null;
									} else if ( !(value instanceof Node) )
										value = null;
									break;
								case VariableHolder.BOOLEAN:
								case VariableHolder.NUMBER:
								case VariableHolder.STRING:
									if ( value instanceof NodeList && ((NodeList)value).getLength() > 0 )
										value = ((NodeList)value).item(0);
									if( value instanceof Node )
										value = ((Node)value).getNodeValue();
									if( value != null && !((String)value).isEmpty() && var.getVariableType() != VariableHolder.STRING) {
										try {
											if( var.getVariableType() == VariableHolder.NUMBER ) {
												value = Double.valueOf((String)value);
											} else {
												value = Boolean.valueOf(((String)value).equals("1") || ((String)value).equalsIgnoreCase("true"));
											}
										} catch (NumberFormatException _) {
										}
									}
									break;
								case VariableHolder.OBJECTARRAY:
									if( value instanceof Node ) {
										LinkedList<Node> list = new LinkedList<Node>();
										list.add((Node)value);
										value = new DummyNodeList(list);
									} else if (!(value instanceof NodeList) )
										value = null;
									break;
							}
							if( value == null )
								Logger.debug("Invalid parameter passed to variable '" + var.getVariableName() + "'.");
						}
						
						varHolder.setVariable(var.getVariableName(), value);
					}
					inputVarCount++;
				} else if( var.getVariableContext() == Variable.OUTPUT ) {
					//if( var.getVariableType() != variables.getVariableType(outputVariable) )
					//	throw new RuntimeError("@runtime-output-mismatch," + fncName);
					outputVar = var;
				}
			}
			
			// Retrieve variables of a previous call of this function (when used recursively)
			previousCallVariables = variables.getVariables(varHolderName);
			
			// Add variables
			variables.addVariables(varHolderName, varHolder);
		}
		
		boolean exitThrown = false;
		try {
			if( contentsBlock != null )
				contentsBlock.run(context, result);
		} catch( com.codeglide.core.rte.exceptions.Break _ ) {
			
		} catch( com.codeglide.core.rte.exceptions.Exit _ ) {
			exitThrown = true;
		} finally {
			if( varHolder != null ) {
				if( previousCallVariables != null )
					variables.addVariables(varHolderName, previousCallVariables);
				else
					variables.removeVariables(varHolderName);
			}
		}
		
		if( noExitCatch && exitThrown )
			throw new Exit();
		
		return (outputVar != null) ? varHolder.resolveVariable(outputVar.getVariableName()) : null;
	}
	
	public static Vector<Expression> parseInputParameters(Element element) {
		NamedNodeMap attributes = element.getAttributes();
		Vector<Expression> result = null;
		if( attributes.getLength() > 0 ) {
			result = new Vector<Expression>();
			for( int i = 0; i < attributes.getLength(); i++ ) {
				Node attribute = attributes.item(i);
				String attrName = attribute.getNodeName().toLowerCase();
				if( attribute.getNodeName().startsWith("input") ) {
					int idx = 1;
					if( attrName.startsWith("inputarg") ) 
						idx = Integer.parseInt(attrName.substring(8));
					if( idx > result.size() )
						result.setSize(idx);
					result.set(idx-1, new Expression(attribute.getNodeValue()));
				}
			}
		}
		
		return result;
	}
	
	
	// Sequence ID
	private int seqId;
	
	public int getSequenceId() {
		return seqId;
	}

	public void setSequenceId(int id) {
		this.seqId = id;
	}

}
