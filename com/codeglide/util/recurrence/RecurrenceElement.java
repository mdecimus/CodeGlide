package com.codeglide.util.recurrence;

import org.w3c.dom.Document;

import com.codeglide.xml.dom.VirtualElement;

public class RecurrenceElement extends VirtualElement {

	public RecurrenceElement(Document parentDoc, String nodeName) {
		super(parentDoc, nodeName);
	}

	public final static int PATTERN = 0; // "day", "week", "month", "year"
	public final static int END = 1; // "never", "count", "date"
	public final static int END_COUNT = 2; // integer
	public final static int END_DATE = 3; // date
	public final static int DAY_EVERY = 4; // "day", "weekday"
	public final static int DAY_INTERVAL = 5; // integer (count)
	public final static int WEEK_INTERVAL = 6; // integer
	public final static int WEEK_MONDAY = 7; // boolean
	public final static int WEEK_TUESDAY = 8; // boolean
	public final static int WEEK_WEDNESDAY = 9; // boolean
	public final static int WEEK_THURSDAY = 10; // boolean
	public final static int WEEK_FRIDAY = 11; // boolean
	public final static int WEEK_SATURDAY = 12; // boolean
	public final static int WEEK_SUNDAY = 13; // boolean
	public final static int MONTH_WHEN = 14; // "date", "relative"
	public final static int MONTH_DAYNO = 15; // integer
	public final static int MONTH_INTERVAL = 16; // integer
	public final static int MONTH_POS = 17; // "first", "second", "third", "fourth", "last"
	public final static int MONTH_DAY = 18; // "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "day", "weekday", "weekendday"
	public final static int YEAR_WHEN = 19; // "date", "relative"
	public final static int YEAR_MONTH = 20; // integer
	public final static int YEAR_POS = 21; // "first", "second", "third", "fourth", "last"
	public final static int YEAR_DAY = 22; // "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "day", "weekday", "weekendday"
	public final static int YEAR_DAYNO = 23; // integer
	
	public void addParameter( int type, String value ) {
		
	}
	
	public String getParameter( int type ) {
		return null;
	}
	
	public void setRecurrenceRule( String rule ) {
		
	}
	
	public String toString() {
		return null;
	}
	
	
}
