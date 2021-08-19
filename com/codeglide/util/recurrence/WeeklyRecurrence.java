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

public class WeeklyRecurrence extends Recurrence {

	/*
	 * RRULE:FREQ=WEEKLY;BYDAY=DD1,DD2,DD3...
	 * 	where DDn is one of the set { SU, MO, TU, WE, TH, FR, SA }
	 */
	public WeeklyRecurrence( Calendar startdate, Calendar enddate, int count, int wdays, int interval ) {
		this.startdate = startdate;
		this.enddate = enddate;
		this.count = count;
		this.wdays = wdays;
		this.interval = interval;
	}
	
	/* (non-Javadoc)
	 * @see com.intersoft.recurrence.Recurrence#contains(java.util.Calendar)
	 */
	public boolean contains(Calendar thisdate) {
		long daysStart, daysEnd, daysThis;
		int dow, dayHex[] = { 0, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40 };
		
		daysStart = dateToDays( startdate.get(Calendar.YEAR), startdate.get(Calendar.MONTH)+1, startdate.get(Calendar.DAY_OF_MONTH) );
		daysThis = dateToDays( thisdate.get(Calendar.YEAR), thisdate.get(Calendar.MONTH)+1, thisdate.get(Calendar.DAY_OF_MONTH) );
		
		if( daysStart == daysThis )
			return true;
		else if( daysStart > daysThis )
			return false;
		
		if( enddate != null) {
			daysEnd = dateToDays( enddate.get(Calendar.YEAR), enddate.get(Calendar.MONTH)+1, enddate.get(Calendar.DAY_OF_MONTH) );
			if( daysThis > daysEnd )
				return false;
		}

		dow = dayOfWeek( thisdate.get(Calendar.YEAR), thisdate.get(Calendar.MONTH)+1, thisdate.get(Calendar.DAY_OF_MONTH) );

		if( ( dayHex[ dow ] & wdays ) != 0 ) {
			int dowst;

			dowst = dayOfWeek( startdate.get(Calendar.YEAR), startdate.get(Calendar.MONTH)+1, startdate.get(Calendar.DAY_OF_MONTH) );

			if( ( (daysThis/7L) - (daysStart/7L) ) % interval > 0 )
				return false;

			if( enddate == null && count != 0 ) {
				int i = 0, offset = 0, dayperweek = 0;
				
				if( ( ( daysThis - daysStart ) < 7 ) && dow >= dowst ) {
					for( i = dowst; i <= dow; i ++ ) {
						if( ( dayHex[ i ] & wdays ) != 0 ) {
							offset++;
						}
					}
				
				} else {
					for( i = dowst; i <= 7; i++ ) {
						if( ( dayHex[ i ] & wdays ) != 0 ) {
							offset++;
						}
					}
					for( i = 1; i <= dow; i++ ) {
						if( ( dayHex[ i ] & wdays ) != 0 ) {
							offset++;
						}
					}
				}
				
				for( i = 1; i <= 7; i++ ) {
					if( ( dayHex[ i ] & wdays ) != 0 ) {
						dayperweek++;
					}
				}

				if( ( ( ( weeksBetween( daysStart, daysThis, dowst, dow ) / (7*interval) ) * dayperweek ) + offset )  > count )
					return false;
			}

			return true;

		}
		return false;
	}

	protected int count;
	protected int wdays;
	protected int interval;

}
