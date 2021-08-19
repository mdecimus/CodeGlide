package com.codeglide.xml.dom.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import com.codeglide.interfaces.root.RootNode;
import com.codeglide.util.ISO8601;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.dom.VirtualElement;

public class CalendarElement extends VirtualElement {
	private GregorianCalendar calendar;
	
	public CalendarElement(Document parentDoc) {
		super(parentDoc, "Calendar");
		calendar = new GregorianCalendar(((RootNode)parentDoc.getDocumentElement()).getTimezone());
		setAttributeNode(new TimeAttr(this, "Date"));
		setAttributeNode(new TzAttr(this,"Timezone"));
		setAttributeNode(new DowAttr(this, "FirstDayOfWeek"));
		setAttributeNode(new FieldAttr(this, "AmPm", Calendar.AM_PM));
		setAttributeNode(new FieldAttr(this, "Day", Calendar.DATE));
		setAttributeNode(new FieldAttr(this, "DayOfMonth", Calendar.DAY_OF_MONTH));
		setAttributeNode(new FieldDayAttr(this, "DayOfWeek", Calendar.DAY_OF_WEEK));
		setAttributeNode(new FieldAttr(this, "DayOfWeekInMonth", Calendar.DAY_OF_WEEK_IN_MONTH));
		setAttributeNode(new FieldAttr(this, "DayOfYear", Calendar.DAY_OF_YEAR));
		setAttributeNode(new FieldAttr(this, "DstOffset", Calendar.DST_OFFSET));
		setAttributeNode(new FieldAttr(this, "Era", Calendar.ERA));
		setAttributeNode(new FieldAttr(this, "Hour", Calendar.HOUR));
		setAttributeNode(new FieldAttr(this, "HourOfDay", Calendar.HOUR_OF_DAY));
		setAttributeNode(new FieldAttr(this, "Millisecond", Calendar.MILLISECOND));
		setAttributeNode(new FieldAttr(this, "Minute", Calendar.MINUTE));
		setAttributeNode(new FieldMonthAttr(this, "Month", Calendar.MONTH));
		setAttributeNode(new FieldAttr(this, "Second", Calendar.SECOND));
		setAttributeNode(new FieldAttr(this, "WeekOfMonth", Calendar.WEEK_OF_MONTH));
		setAttributeNode(new FieldAttr(this, "WeekOfYear", Calendar.WEEK_OF_YEAR));
		setAttributeNode(new FieldAttr(this, "Year", Calendar.YEAR));
		setAttributeNode(new FieldAttr(this, "ZoneOffset", Calendar.ZONE_OFFSET));
	}
	
	protected void add(int field, int amount) {
		calendar.add(field, amount);
	}
	
	protected void set(int field, int value) {
		calendar.set(field, value);
	}
	
	protected int get(int field ) {
		return calendar.get(field);
	}
	
	protected void setTime(Date date) {
		calendar.setTime(date);
	}
	
	protected Date getTime() {
		return calendar.getTime();
	}
	
	protected TimeZone getTimeZone() {
		return calendar.getTimeZone();
	}
	
	protected void setTimeZone( TimeZone tz ) {
		calendar.setTimeZone(tz);
	}
	
	protected int getFirstDayOfWeek() {
		return calendar.getFirstDayOfWeek();
	}
	
	protected void setFirstDayOfWeek(int value) {
		calendar.setFirstDayOfWeek(value);
	}
	
	private class TimeAttr extends DynamicAttr {
		private Date cachedDate;
		private String cachedDateString = null;
		
		public TimeAttr(DynamicElement parentNode, String name) {
			super(parentNode, name);
		}

		public String getExpandedValue() {
			return getValue();
		}

		public String getValue() {
			Date calDate = ((CalendarElement)parentNode).getTime();
			if( cachedDate == null || !calDate.equals(cachedDate) ) {
				cachedDate = calDate;
				cachedDateString = ISO8601.formatUtc(calDate);
				System.out.println(cachedDateString);
			}
			
			return cachedDateString;
		}

		public void setValue(String value) throws DOMException {
			if( value != null && !value.isEmpty() ) {
				try {
					((CalendarElement)parentNode).setTime(ISO8601.parseDate(value));
				} catch (ParseException _) {
				}
			}
		}
	}
	
	private class TzAttr extends DynamicAttr {

		public TzAttr(DynamicElement parentNode, String name) {
			super(parentNode, name);
		}

		public String getExpandedValue() {
			return getValue();
		}

		public String getValue() {
			return ((CalendarElement)parentNode).getTimeZone().getID();
		}

		public void setValue(String value) throws DOMException {
			if( value != null && !value.isEmpty() ) {
				TimeZone tz = TimeZone.getTimeZone(value);
				if( tz != null )
					((CalendarElement)parentNode).setTimeZone(tz);
			}
		}
	}
	
	private class DowAttr extends DynamicAttr {

		public DowAttr(DynamicElement parentNode, String name) {
			super(parentNode, name);
		}

		public String getExpandedValue() {
			return getValue();
		}

		public String getValue() {
			return String.valueOf(((CalendarElement)parentNode).getFirstDayOfWeek());
		}

		public void setValue(String value) throws DOMException {
			if( value != null && !value.isEmpty() ) {
				int dow;
				if( value.equalsIgnoreCase("monday") ) {
					dow = Calendar.MONDAY;
				} else if( value.equalsIgnoreCase("tuesday") ) {
					dow = Calendar.TUESDAY;
				} else if( value.equalsIgnoreCase("wednesday") ) {
					dow = Calendar.WEDNESDAY;
				} else if( value.equalsIgnoreCase("thursday") ) {
					dow = Calendar.THURSDAY;
				} else if( value.equalsIgnoreCase("friday") ) {
					dow = Calendar.FRIDAY;
				} else if( value.equalsIgnoreCase("saturday") ) {
					dow = Calendar.SATURDAY;
				} else if( value.equalsIgnoreCase("sunday") ) {
					dow = Calendar.SUNDAY;
				} else
					dow = Integer.parseInt(value);
				((CalendarElement)parentNode).setFirstDayOfWeek(dow);
			}
		}
	}
	
	private class FieldAttr extends DynamicAttr {
		private int field;
		
		public FieldAttr(DynamicElement parentNode, String name, int field ) {
			super(parentNode, name);
			this.field = field;
		}

		public String getExpandedValue() {
			return getValue();
		}

		public String getValue() {
			return String.valueOf(((CalendarElement)parentNode).get(field));
		}

		public void setValue(String arg0) throws DOMException {
			if( arg0 != null && !arg0.isEmpty() ) {
				if( arg0.startsWith("+") || arg0.startsWith("-") ) {
					if( arg0.startsWith("+") )
						arg0 = arg0.substring(1);
					((CalendarElement)parentNode).add(field, Integer.parseInt(arg0));
				} else
					((CalendarElement)parentNode).set(field, Integer.parseInt(arg0));
			}
			
		}
	}
	
	private class FieldDayAttr extends FieldAttr {

		public FieldDayAttr(DynamicElement parentNode, String name, int field) {
			super(parentNode, name, field);
		}
		
		public void setValue(String arg0) throws DOMException {
			if( arg0 != null && !arg0.isEmpty() ) {
				if( !arg0.startsWith("+") && !arg0.startsWith("-") && !Character.isDigit(arg0.charAt(0)) ) {
					if( arg0.equalsIgnoreCase("monday") ) {
						arg0 = Integer.toString(Calendar.MONDAY);
					} else if( arg0.equalsIgnoreCase("tuesday") ) {
						arg0 = Integer.toString(Calendar.TUESDAY);
					} else if( arg0.equalsIgnoreCase("wednesday") ) {
						arg0 = Integer.toString(Calendar.WEDNESDAY);
					} else if( arg0.equalsIgnoreCase("thursday") ) {
						arg0 = Integer.toString(Calendar.THURSDAY);
					} else if( arg0.equalsIgnoreCase("friday") ) {
						arg0 = Integer.toString(Calendar.FRIDAY);
					} else if( arg0.equalsIgnoreCase("saturday") ) {
						arg0 = Integer.toString(Calendar.SATURDAY);
					} else if( arg0.equalsIgnoreCase("sunday") ) {
						arg0 = Integer.toString(Calendar.SUNDAY);
					}
				}
				super.setValue(arg0);
			}
		}
	}

	private class FieldMonthAttr extends FieldAttr {

		public FieldMonthAttr(DynamicElement parentNode, String name, int field) {
			super(parentNode, name, field);
		}
		
		public void setValue(String arg0) throws DOMException {
			if( arg0 != null && !arg0.isEmpty() ) {
				if( !arg0.startsWith("+") && !arg0.startsWith("-") && !Character.isDigit(arg0.charAt(0)) ) {
					if( arg0.equalsIgnoreCase("january") ) {
						arg0 = Integer.toString(Calendar.JANUARY);
					} else if( arg0.equalsIgnoreCase("february") ) {
						arg0 = Integer.toString(Calendar.FEBRUARY);
					} else if( arg0.equalsIgnoreCase("march") ) {
						arg0 = Integer.toString(Calendar.MARCH);
					} else if( arg0.equalsIgnoreCase("april") ) {
						arg0 = Integer.toString(Calendar.APRIL);
					} else if( arg0.equalsIgnoreCase("may") ) {
						arg0 = Integer.toString(Calendar.MAY);
					} else if( arg0.equalsIgnoreCase("june") ) {
						arg0 = Integer.toString(Calendar.JUNE);
					} else if( arg0.equalsIgnoreCase("july") ) {
						arg0 = Integer.toString(Calendar.JULY);
					} else if( arg0.equalsIgnoreCase("august") ) {
						arg0 = Integer.toString(Calendar.AUGUST);
					} else if( arg0.equalsIgnoreCase("september") ) {
						arg0 = Integer.toString(Calendar.SEPTEMBER);
					} else if( arg0.equalsIgnoreCase("october") ) {
						arg0 = Integer.toString(Calendar.OCTOBER);
					} else if( arg0.equalsIgnoreCase("november") ) {
						arg0 = Integer.toString(Calendar.NOVEMBER);
					} else if( arg0.equalsIgnoreCase("december") ) {
						arg0 = Integer.toString(Calendar.DECEMBER);
					}
				}
				super.setValue(arg0);
			}
		}
	}

}
