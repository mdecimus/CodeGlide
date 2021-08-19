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

/**
 * @author lziliani
 *
 */
public class DailyRecurrence extends Recurrence {

//	 type:  0 = every day, 1 = every week day, 2 = every weekend day
	/*
	 * Type = 0:
	 * 	RRULE:FREQ=DAILY
	 * Type = 1: 
	 *	@see WeeklyRecurrence
	 * Type = 2:
	 * 	@see WeeklyRecurrence
	 */
	public DailyRecurrence( Calendar startdate, Calendar enddate, int count, int type, int interval ) {
		this.startdate = startdate;
		this.enddate = enddate;
		this.type = type;
		this.count = count;
		this.interval = interval;
	}

	/* (non-Javadoc)
	 * @see com.intersoft.recurrence.Recurrence#contains(java.util.Calendar)
	 */
	public boolean contains(Calendar thisdate) {
		long daysStart, daysEnd, daysThis;
		int dow, dowst;
		
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
		
		switch( type ) {
			case 0:
				if( interval == 0)
					return false;
				if( enddate == null && count != 0) {
					if( ( daysThis - daysStart ) / interval > count-1 )
						return false;
				}
				if( ( daysThis - daysStart ) % interval == 0 )
					return true;
				else
					return false;
				
			case 1:
				dow = dayOfWeek( thisdate.get(Calendar.YEAR), thisdate.get(Calendar.MONTH)+1, thisdate.get(Calendar.DAY_OF_MONTH) );
				if( dow >= 1 && dow <= 5 ) {
					if( enddate == null && count != 0) {
						dowst = dayOfWeek( startdate.get(Calendar.YEAR), startdate.get(Calendar.MONTH)+1, startdate.get(Calendar.DAY_OF_MONTH) );
						if( ( ( ( ( daysThis - daysStart - (7-dowst) - dow ) / 7 ) * 5 ) + dow + 5 - ((dowst>5)?6:dowst) )  > count - 1 )
							return false;
					}
					return true;
				}
				return false;

			case 2:
				dow = dayOfWeek( thisdate.get(Calendar.YEAR), thisdate.get(Calendar.MONTH)+1, thisdate.get(Calendar.DAY_OF_MONTH) );
				if( dow >= 6 && dow <= 7 ) {
					if( enddate == null && count != 0) {
						dowst = dayOfWeek( startdate.get(Calendar.YEAR), startdate.get(Calendar.MONTH)+1, startdate.get(Calendar.DAY_OF_MONTH) );
						if( ( ( ( ( daysThis - daysStart - (7-dowst) - dow ) / 7 ) * 2 ) + ((dow==7)?2:1) + 7 - ((dowst>=6)?dowst:6) )  > count - 1 )
							return false;
					}
					return true;
				}
				return false;
			
			default:
				return false;
		}
	}

	protected int type;
	protected int count;
	protected int interval;
}
