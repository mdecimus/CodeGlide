/*
 * 	Copyright (C) 2007, CodeGlide - Entwickler, S.A.
 *	All rights reserved.
 *
 *	You may not distribute this software, in whole or in part, without
 *	the express consent of the author.
 *
 *	There is no warranty or other guarantee of fitness of this software
 *	for any purpose.  It is provided solely "as is".
 *
 */
package com.codeglide.util.recurrence;

import java.util.Calendar;

public class Test {

	/**
	 * Prints all the matching dates from 1990 to 2050
	 * @param recu: recurrence to expand
	 * @param legend: title for the recurrence
	 */
	private static void printAllDates(String legend, Recurrence recu) {
		Object[] c;
		Calendar from = Calendar.getInstance();
		from.set(1990, 01, 01, 0, 0, 0);
		Calendar to = Calendar.getInstance();
		to.set(2050, 01, 01, 0, 0, 0);
		/*c = recu.expand(from, to);
		
		System.out.println(legend);
		for (int i = 0; i < c.length; i++) {
			System.out.println(((Calendar)c[i]).getTime());
		}*/
		System.out.println("----------------------------------");

	}
	
	private static void yearlyRecurrenceTest() {
		Calendar start = Calendar.getInstance();
		start.set(2001, Calendar.OCTOBER, 7, 0, 0, 0);
		Calendar end = Calendar.getInstance();
		end.set(2006, Calendar.JANUARY, 01, 0, 0, 0);
		
		YearlyRecurrence yearly;
		
		yearly = new YearlyRecurrence(start, end, 0, 0, 7, 0, 10, 1);
		printAllDates("every October 7, from 2001 to 2005", yearly);
		
		yearly = new YearlyRecurrence(start, null, 10, 0, 7, 0, 10, 1);
		printAllDates("every October 7, from 2001, 10 times", yearly);
		
		yearly = new YearlyRecurrence(start, null, 10, 0, 7, 0, 10, 2);
		printAllDates("every October 7, from 2001, interval 2, 10 times", yearly);
		
		yearly = new YearlyRecurrence(start, end, 0, 1, 1, 1, 2, 1);
		printAllDates("every first monday of February, from 2001 to 2005", yearly);
		
		yearly = new YearlyRecurrence(start, end, 0, 1, 2, 3, 2, 1);
		printAllDates("every second wednsday of February, from 2001 to 2005", yearly);
		
		yearly = new YearlyRecurrence(start, null, 10, 1, -1, 1, 2, 2);
		printAllDates("every last monday of February, from 2001, interval 2, 10 times", yearly);
		
		yearly = new YearlyRecurrence(start, null, 10, 1, 3, 9, 12, 1);
		printAllDates("every third weekday of December, from 2001, 10 times", yearly);
		
		yearly = new YearlyRecurrence(start, null, 10, 1, 3, 10, 1, 1);
		printAllDates("every third weekend day of January, from 2001, 10 times", yearly);
		
	}
	
	private static void yearlyRRULETest() {
		Calendar start = Calendar.getInstance();
		start.set(2001, Calendar.OCTOBER, 7, 0, 0, 0);
		
		String until = "2006-01-01T00:00:00Z";
		
		Recurrence yearly;
		
		yearly = Recurrence.parse(start, "RRULE:FREQ=YEARLY;BYMONTH=10;BYMONTHDAY=7;UNTIL="+until);
		printAllDates("every October 7, from 2001 to 2005", yearly);
		
		yearly = Recurrence.parse(start, "RRULE:FREQ=YEARLY;BYMONTH=10;BYMONTHDAY=7;COUNT=10");
		printAllDates("every October 7, from 2001, 10 times", yearly);
		
		yearly = Recurrence.parse(start, "RRULE:FREQ=YEARLY;BYMONTH=10;BYMONTHDAY=7;COUNT=10;INTERVAL=2");
		printAllDates("every October 7, from 2001, interval 2, 10 times", yearly);
		
		yearly = Recurrence.parse(start, "RRULE:FREQ=YEARLY;BYMONTH=2;BYDAY=1MO;UNTIL="+until);
		printAllDates("every first monday of February, from 2001 to 2005", yearly);
		
		yearly = Recurrence.parse(start, "RRULE:FREQ=YEARLY;BYMONTH=2;BYDAY=2WE;UNTIL="+until);
		printAllDates("every second wednsday of February, from 2001 to 2005", yearly);
		
		yearly = Recurrence.parse(start, "RRULE:FREQ=YEARLY;BYMONTH=2;BYDAY=-1MO;COUNT=10;INTERVAL=2");
		printAllDates("every last monday of February, from 2001, interval 2, 10 times", yearly);
		
		yearly = Recurrence.parse(start, "RRULE:FREQ=YEARLY;BYMONTH=12;BYSETPOS=3;BYDAY=MO,TU,WE,FR,TH;COUNT=10");
		printAllDates("every third weekday of December, from 2001, 10 times", yearly);
		
		yearly = Recurrence.parse(start, "RRULE:FREQ=YEARLY;BYMONTH=1;BYSETPOS=3;BYDAY=SA,SU;COUNT=10");
		printAllDates("every third weekend day of January, from 2001, 10 times", yearly);
		
	}
	
	private static void monthlyRRULETest() {
		Calendar start = Calendar.getInstance();
		start.set(2001, Calendar.OCTOBER, 15);
		String until = "2010-01-01T09:00:00Z";
		
		Recurrence monthly;
		
		monthly = Recurrence.parse(start, "RRULE:FREQ=MONTHLY;BYMONTHDAY=1;UNTIL="+until);
		printAllDates("every 1th day of month, from 2001 to 2010", monthly);
		
		monthly = Recurrence.parse(start, "RRULE:FREQ=MONTHLY;BYMONTHDAY=15;COUNT=10;INTERVAL=2");
		printAllDates("every 15th, 2 months each, from 2001, 10 times", monthly);
		
		monthly = Recurrence.parse(start, "RRULE:FREQ=MONTHLY;BYMONTHDAY=30;INTERVAL=5;UNTIL="+until);
		printAllDates("every 30th, 5 months each, from 2001 to 2010", monthly);
		
		monthly = Recurrence.parse(start, "RRULE:FREQ=MONTHLY;BYDAY=3MO;INTERVAL=2;UNTIL="+until);
		printAllDates("every 3th monday, every 2 months, from 2001 to 2010", monthly);

		monthly = Recurrence.parse(start, "RRULE:FREQ=MONTHLY;BYDAY=-1WE;COUNT=10");
		printAllDates("every last wednesday, from 2001, 10 times", monthly);
		
	}

	private static void monthlyRecurrenceTest() {
		Calendar start = Calendar.getInstance();
		start.set(2001, Calendar.OCTOBER, 15);
		Calendar end = Calendar.getInstance();
		end.set(2010, Calendar.JANUARY, 01);
		
		MonthlyRecurrence monthly;
		
		monthly = new MonthlyRecurrence(start, end, 0, 0, 1, 0, 1);
		printAllDates("every 1th day of month, from 2001 to 2010", monthly);
		
		monthly = new MonthlyRecurrence(start, null, 10, 0, 15, 0, 2);
		printAllDates("every 15th, 2 months each, from 2001, 10 times", monthly);
		
		monthly = new MonthlyRecurrence(start, end, 10, 0, 30, 0, 5);
		printAllDates("every 30th, 5 months each, from 2001 to 2010", monthly);
		
		monthly = new MonthlyRecurrence(start, end, 0, 1, 3, 1, 2);
		printAllDates("every 3th monday, every 2 months, from 2001 to 2010", monthly);

		monthly = new MonthlyRecurrence(start, null, 10, 1, -1, 3, 1);
		printAllDates("every last wednesday, from 2001, 10 times", monthly);
		
	}

	private static void weeklyRecurrenceTest() {
		Calendar start = Calendar.getInstance();
		start.set(2004, Calendar.JANUARY, 15);
		Calendar end = Calendar.getInstance();
		end.set(2004, Calendar.DECEMBER, 01);
		
		WeeklyRecurrence weekly;
		
		weekly = new WeeklyRecurrence(start, end, 0, 0x01 | 0x04 | 0x10 | 0x40, 1);
		printAllDates("Every Monday, Wednday, Friday, Sunday, from January 15th, 2004 to December 1th, 2004", weekly);
		
		weekly = new WeeklyRecurrence(start, null, 10, 0x20 | 0x40, 1);
		printAllDates("Every Saturday and Sunday, from January 15th, 10 times", weekly);
		
		weekly = new WeeklyRecurrence(start, end, 0, 0x01 | 0x02 | 0x04 | 0x08 | 0x10, 5);
		printAllDates("Every weekday from January 15th, 2004 to December 1th, 2004, every 5 weeks", weekly);
		
		weekly = new WeeklyRecurrence(start, null, 10, 0x20 | 0x40, 2);
		printAllDates("Every weekend, from January 15th, 10 times, every 2 weeks", weekly);
	}

	private static void weeklyRRULETest() {
		Calendar start = Calendar.getInstance();
		start.set(2004, Calendar.JANUARY, 15);
		String until = "2004-12-01T09:00:00Z";
		
		Recurrence weekly;
		
		weekly = Recurrence.parse(start, "FREQ=WEEKLY;BYDAY=MO,WE,FR,SU;UNTIL="+until);
		printAllDates("Every Monday, Wednday, Friday, Sunday, from January 15th, 2004 to December 1th, 2004", weekly);
		
		weekly = Recurrence.parse(start, "FREQ=WEEKLY;BYDAY=SA,SU;COUNT=10");
		printAllDates("Every Saturday and Sunday, from January 15th, 10 times", weekly);
		
		weekly = Recurrence.parse(start, "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;INTERVAL=5;UNTIL="+until);
		printAllDates("Every weekday from January 15th, 2004 to December 1th, 2004, every 5 weeks", weekly);
		
		weekly = Recurrence.parse(start, "FREQ=WEEKLY;BYDAY=SA,SU;INTERVAL=2;COUNT=10");
		printAllDates("Every weekend, from January 15th, 10 times, every 2 weeks", weekly);
	}

	private static void dailyRecurrenceTest() {
		Calendar start = Calendar.getInstance();
		start.set(2004, Calendar.JANUARY, 15, 0, 0, 0);
		Calendar end = Calendar.getInstance();
		end.set(2004, Calendar.DECEMBER, 01, 0, 0, 0);
		
		DailyRecurrence daily;

		daily = new DailyRecurrence(start, end, 0, 0, 1);
		printAllDates("Every day between January, 15, 2004 and December 1th, 2004", daily);
	
		daily = new DailyRecurrence(start, null, 10, 0, 2);
		printAllDates("From January, 15, 2004, ten days every 2 days", daily);
	
		}
	
	private static void dailyRRULETest() {
		Calendar start = Calendar.getInstance();
		start.set(2004, Calendar.JANUARY, 15, 0, 0, 0);
		String until = "2004-12-01T00:00:00Z";
		
		Recurrence daily;

		daily = Recurrence.parse(start, "FREQ=DAILY;UNTIL="+until);
		printAllDates("Every day between January, 15, 2004 and December 1th, 2004", daily);
	
		daily = Recurrence.parse(start, "FREQ=DAILY;COUNT=10;INTERVAL=2");
		printAllDates("From January, 15, 2004, ten days every 2 days", daily);
	
	}

	
	public static void main(String[] args) {
		yearlyRecurrenceTest();
		System.out.println("**************************");
		yearlyRRULETest();
	}
}
