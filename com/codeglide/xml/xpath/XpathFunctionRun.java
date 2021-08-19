package com.codeglide.xml.xpath;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.DOMException;

import com.codeglide.core.rte.commands.Function;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.render.Action;

public class XpathFunctionRun extends XpathFunction {

	public XPathFunction getFunction(String name) {
		Context context = Context.getCurrent();
		Function function = context.getApplication().getFunction(getString(name).toLowerCase());
		if( function == null )
			return null;
		
		return new RunFunction(function, name);
	}

	public class RunFunction implements XPathFunction {
		private Function function;
		private String name;
		
		public RunFunction(Function function, String name) {
			this.function = function;
			this.name = name;
		}
		
		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			// Add input parameters
			Vector<Object> inputArgs = new Vector<Object>();
			for( int i = 0; i < args.size(); i++ ) 
				inputArgs.add(args.get(i));
			
			try {
				return function.run(Context.getCurrent(), new LinkedList<Action>(), inputArgs);
			} catch (Exception e) {
				DOMException e1 = new DOMException(DOMException.INVALID_ACCESS_ERR,"@function-err,"+name);
				e1.initCause(e);
				throw e1;
			}
		}
	}
	
}
