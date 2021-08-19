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

public class MonthlyRecurrence extends Recurrence {
	
	//	Monthly:  0 = Day DAY_NUMBER of every INTERVAL months,  1 = the [FIRST..LAST] [DAY/WEEKDAY/WEEKDAYNAME] of every INTERVAL months
	/*
	 *	Type 0:
	 *		RRULE:FREQ=MONTHLY;BYMONTHDAY=dd
	 *	Type 1:
	 * 		RRULE:FREQ=MONTHLY;BYDAY=nDD
	 *	where n stands for: -1 for last, 1 for first, ... 
	 * 	  and DD is: SU,MO,TU,WE,TH,FR,SA
	 */
	public MonthlyRecurrence( Calendar startdate, Calendar enddate, int count, int type, int day1, int day2, int interval ) {
		this.startdate = startdate;
		this.enddate = enddate;
		this.count = count;
		this.type = type;
		this.day1 = day1;
		this.day2 = day2;
		this.interval = interval;
	}
	
	/* (non-Javadoc)
	 * @see com.intersoft.recurrence.Recurrence#contains(java.util.Calendar)
	 */
	public boolean contains(Calendar thisdate) {
		Calendar tmpdate = Calendar.getInstance(tz);
		long daysStart, daysEnd, daysThis, months = 0;
		int dow, i, c;
		
		daysStart = dateToDays( startdate.get(Calendar.YEAR), startdate.get(Calendar.MONTH)+1, startdate.get(Calendar.DAY_OF_MONTH) );
		daysThis = dateToDays( thisdate.get(Calendar.YEAR), thisdate.get(Calendar.MONTH)+1, thisdate.get(Calendar.DAY_OF_MONTH) );
		
		if( daysStart == daysThis )
			return true;
		else if( daysStart > daysThis )
			return false;
		
		if( enddate != null ) {
			daysEnd = dateToDays( enddate.get(Calendar.YEAR), enddate.get(Calendar.MONTH)+1, enddate.get(Calendar.DAY_OF_MONTH) );
			if( daysThis > daysEnd )
				return false;
		}
		
		if( ( thisdate.get(Calendar.YEAR) - startdate.get(Calendar.YEAR) ) >= 2 )
			months += ( thisdate.get(Calendar.YEAR) - startdate.get(Calendar.YEAR) - 1 ) * 12;
		if( thisdate.get(Calendar.YEAR) == startdate.get(Calendar.YEAR) )
			months += thisdate.get(Calendar.MONTH) - startdate.get(Calendar.MONTH);
		else
			months += thisdate.get(Calendar.MONTH) + 12 - startdate.get(Calendar.MONTH); 
		if( (count != 0 && enddate == null) && ( (months/interval) > count-1 ) )
			return false;
		if( ( months % interval ) != 0 )
			return false;
		
		switch( type ) {
		case 0:
			if( thisdate.get(Calendar.DAY_OF_MONTH) != day1 )
				return false;
			break;
		case 1:
			tmpdate.set(thisdate.get(Calendar.YEAR), thisdate.get(Calendar.MONTH), thisdate.get(Calendar.DAY_OF_MONTH));
			
			if( day2 == 8 ) {
				if( day1 >= 1 && day1 <= 4 ) {
					if( thisdate.get(Calendar.DAY_OF_MONTH) != day1 )
						return false;
					else
						return true;
				} else {
					if( thisdate.get(Calendar.DAY_OF_MONTH) != daysInMonth( thisdate.get(Calendar.MONTH)+1, thisdate.get(Calendar.YEAR) ) )
						return false;
					else
						return true;
				}
			} else if( day2 == 9 ) {
				if( day1 >= 1 && day1 <= 4 ) {
					c = 1;
					i = 0;
					while( i != day1 ) {
						dow = dayOfWeek( thisdate.get(Calendar.YEAR), thisdate.get(Calendar.MONTH)+1, c );
						if( dow >= 1 && dow <= 5 )
							i++;
						c++;
					}
					if( thisdate.get(Calendar.DAY_OF_MONTH) != c-1 )
						return false;
					else
						return true;
				} else {
					tmpdate.set( Calendar.DAY_OF_MONTH, daysInMonth( tmpdate.get(Calendar.MONTH)+1, tmpdate.get(Calendar.YEAR) ) );
					dow = dayOfWeek( tmpdate.get(Calendar.YEAR), tmpdate.get(Calendar.MONTH)+1, tmpdate.get(Calendar.DAY_OF_MONTH) );
					if( dow > 5 )
						addDeltaDays( tmpdate, 5-dow );
					if( dateToDays( tmpdate.get(Calendar.YEAR), tmpdate.get(Calendar.MONTH)+1, tmpdate.get(Calendar.DAY_OF_MONTH) ) != daysThis )
						return false;
					else
						return true;
				}
			} else if( day2 == 10 ) {
				if( day1 >= 1 && day1 <= 4 ) {
					c = 1;
					i = 0;
					while( i != day1 ) {
						dow = dayOfWeek( thisdate.get(Calendar.YEAR), thisdate.get(Calendar.MONTH)+1, c );
						if( dow >= 6 && dow <= 7 )
							i++;
						c++;
					}
					if( thisdate.get(Calendar.DAY_OF_MONTH) != c-1 )
						return false;
					else
						return true;
				} else {
					tmpdate.set( Calendar.DAY_OF_MONTH, daysInMonth( tmpdate.get(Calendar.MONTH)+1, tmpdate.get(Calendar.YEAR) ) );
					dow = dayOfWeek( tmpdate.get(Calendar.YEAR), tmpdate.get(Calendar.MONTH)+1, tmpdate.get(Calendar.DAY_OF_MONTH) );
					if( dow < 6 )
						addDeltaDays( tmpdate, -dow );
					if( dateToDays( tmpdate.get(Calendar.YEAR), tmpdate.get(Calendar.MONTH)+1, tmpdate.get(Calendar.DAY_OF_MONTH) ) != daysThis )
						return false;
					else
						return true;
				}
			} else {
				if( day1 >= 1 && day1 <= 4 ) {
					nthWeekday( tmpdate, day2, day1 );
					if( dateToDays( tmpdate.get(Calendar.YEAR), tmpdate.get(Calendar.MONTH)+1, tmpdate.get(Calendar.DAY_OF_MONTH) ) != daysThis )
						return false;
					else
						return true;
				} else {
					tmpdate.set(Calendar.DAY_OF_MONTH, 1);
					tmpdate.add(Calendar.MONTH, 1);
/* this is a std Calendar work
					if( tmpdate.get(Calendar.MONTH) > 12 ) {
						tmpdate.set(Calendar.MONTH, 1);
						tmpdate.add(Calendar.YEAR, 1);
					}
*/
					dow = dayOfWeek( tmpdate.get(Calendar.YEAR), tmpdate.get(Calendar.MONTH)+1, 8 - day2 );
					
					if( ( dateToDays( tmpdate.get(Calendar.YEAR), tmpdate.get(Calendar.MONTH)+1, 1 ) - dow ) != daysThis )
						return false;
					else
						return true;
				}
			}

		default:
			return false;
		}
		return true;
	}
	
	protected int count;
	protected int type;
	protected int day1;
	protected int day2;
	protected int interval;
	
}
