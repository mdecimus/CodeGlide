package com.codeglide.xml.xpath;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Node;

import com.codeglide.core.Logger;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.interfaces.root.RootNode;
import com.codeglide.util.ISO8601;
import com.codeglide.util.recurrence.Recurrence;
import com.codeglide.xml.dom.DummyNodeList;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.dom.util.CalendarElement;

public class XpathFunctionCal extends XpathFunction {
	public XpathFunctionCal() {
		addFunction("new-calendar", new NewCalendar());
		addFunction("expand-rrule", new ExpandRrule());
		addFunction("time-as-seconds", new GetTimeAsSeconds());
	}
	
	public class NewCalendar implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			return new CalendarElement(Context.getCurrent().getDocumentNode());
		}
	}
	
	public class ExpandRrule implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			List<Node> result = new LinkedList<Node>();
			try {
				RootNode rootNode = Context.getCurrent().getRootNode();
				String rule = getString(args.get(0));

				// Look for a node attribute
				/*int i = 0;
				while( rootNode == null && i < args.size() ) {
					Node node = getNode(args.get(i));
					if( node != null )
						rootNode = (RootNode)node.getOwnerDocument().getDocumentElement();
				}*/
				
				// Set from and to dates
				GregorianCalendar startDate = new GregorianCalendar(rootNode.getTimezone());
				startDate.setTime(ISO8601.parseDate(getString(args.get(1))));
				GregorianCalendar fromDate = new GregorianCalendar(rootNode.getTimezone());
				fromDate.setTime(ISO8601.parseDate(getString(args.get(2))));
				GregorianCalendar toDate = new GregorianCalendar(rootNode.getTimezone());
				toDate.setTime(ISO8601.parseDate(getString(args.get(3))));
				
				//System.out.println(rule+", "+getString(args.get(1))+", "+getString(args.get(2))+","+getString(args.get(3)));
				
				Recurrence recur = Recurrence.parse(startDate, rule);
				List<Date> occurrences = recur.expand(fromDate, toDate);
				for( Date occurrence : occurrences ) {
					DynamicElement date = new DynamicElement(rootNode.getDocumentNode(), "Occurrence");
					date.setAttribute("Date", ISO8601.formatUtc(occurrence));
					result.add(date);
					//System.out.println("Expanded to " + date.getAttribute("Date"));
				}
			} catch (Exception e) {
				Logger.debug(e);
			}
			
			return new DummyNodeList(result);
		}
	}
	
	public class GetTimeAsSeconds implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			try {
				return new Double(ISO8601.parseDate(getString(args.get(0))).getTime()/1000);
			} catch (Exception e) {
				return new Double(0);
			}
		}
	}

}
