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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

public abstract class Recurrence {
	protected TimeZone tz;
	
	protected Calendar getCalendarIntance() {
		return Calendar.getInstance(tz);
	}
	
	public abstract boolean contains(Calendar date);

	/**
	 * @param from: start date of the requiered expansion
	 * @param to: end date of the requiered expansion
	 * @return array of matching dates between <i>from</i> and <i>to</i> 
	 */
	public List<Date> expand(Calendar from, Calendar to) {
		ArrayList<Date> dates = new ArrayList<Date>();
		Calendar fromCopy = (Calendar)from.clone(); // we don't change the function parameters
		
		fromCopy.set(Calendar.HOUR_OF_DAY, startdate.get(Calendar.HOUR_OF_DAY));
		fromCopy.set(Calendar.MINUTE, startdate.get(Calendar.MINUTE));
		fromCopy.set(Calendar.SECOND, startdate.get(Calendar.SECOND));
		fromCopy.set(Calendar.MILLISECOND, 0);

		while (!fromCopy.after(to)) {
			if (this.contains(fromCopy)) {
				dates.add(fromCopy.getTime());
			}
			fromCopy.add(Calendar.DAY_OF_MONTH, 1);
		}
		return dates;
	}
	
	protected static final int daysInMonthArray[][] = {
		{ 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 },
		{ 0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 }
	};

	protected static final int daysInYearArray[][] = {
		{ 0, 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365 },
		{ 0, 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366 }
	};

	protected boolean isLeap( int year ) {
		int yy;

		return( ((year & 0x03) == 0) &&
		         ( (((yy = (int) (year / 100)) * 100) != year) ||
		           ((yy & 0x03) == 0) ) );
	}

	protected int daysInMonth( int month, int year ) {
		return( daysInMonthArray[isLeap(year)? 1 : 0][ month ] );
	}

	protected Calendar mkDate( int day, int month, int year, int hour, int minute ) {
		Calendar tmpDate = getCalendarIntance();

		tmpDate.set(Calendar.YEAR, year);
		tmpDate.set(Calendar.MONTH, month-1);
		tmpDate.set(Calendar.DAY_OF_MONTH, day);
		tmpDate.set(Calendar.HOUR, hour);
		tmpDate.set(Calendar.MINUTE, minute);
		tmpDate.set(Calendar.SECOND, 0);

		return( tmpDate );
	}

	protected long dateYearToDays( int year ) {
		long days;
		
		days = year * 365L;
		days += year >>= 2;
		days -= year /= 25;
		days += year >>  2;

		return(days);
	}

	protected long dateToDays( int year, int month, int day ) {
		return( dateYearToDays( --year ) + daysInYearArray[ isLeap( year+1 )? 1 : 0 ][ month ] + day );
	}

	protected long deltaDates( Calendar date1, Calendar date2 ) {
		long deltaDays, deltaSeconds;
		
		if( date1 == null || date2 == null )
			return 0L;

		deltaSeconds = ((((date2.get(Calendar.HOUR) * 60L) + date2.get(Calendar.MINUTE)) * 60L) + date2.get(Calendar.SECOND)) - 
		        ((((date1.get(Calendar.HOUR) * 60L) + date1.get(Calendar.MINUTE)) * 60L) + date1.get(Calendar.SECOND));
		deltaDays = dateToDays( date2.get(Calendar.YEAR), date2.get(Calendar.MONTH)+1, date2.get(Calendar.DAY_OF_MONTH) ) -
		            dateToDays( date1.get(Calendar.YEAR), date1.get(Calendar.MONTH)+1, date1.get(Calendar.DAY_OF_MONTH) );

		return( ( deltaDays * 86400L ) + deltaSeconds );
	}
	
	protected boolean addDeltaDays( Calendar date, long deltaDays ) {
		long days;
		boolean leap;
		int iYear = date.get(Calendar.YEAR);
		int iMonth = date.get(Calendar.MONTH)+1;
		int iDay = date.get(Calendar.DAY_OF_MONTH);
		
		if (((days = dateToDays(iYear,iMonth,iDay)) > 0L) &&
		    ((days += deltaDays) > 0L)) {
		
		   iYear = (int)(days / 365.2425);
		   iDay  = (int)(days - dateYearToDays(iYear));
		   if (iDay < 1)
				iDay = (int)(days - dateYearToDays(iYear-1));		    
		   else
		   		iYear++;
		   leap = isLeap(iYear);
		   if (iDay > daysInYearArray[leap? 1 : 0][13]) {
		   		iDay -= daysInYearArray[leap? 1 : 0][13];
		   		leap  = isLeap(++iYear);
		   }
		   for ( iMonth = 12; iMonth >= 1; (iMonth)-- ) {
		   	if (iDay > daysInYearArray[leap? 1 : 0][iMonth]) {
		   		iDay -= daysInYearArray[leap? 1 : 0][iMonth];
		   		break;
		   	}
		   }
		   date.set(iYear, iMonth-1, iDay);
		   return true;
		} 
		else 
			return false;
	}

	protected boolean addDeltaDHMS( Calendar date, long deltaDays, long deltaHour, long deltaMinute, long deltaSecond ) {
		long  sum;
		long  quot;
		boolean ret;
		
		quot = (long) (deltaHour / 24);
		deltaHour  -= quot * 24L;
		deltaDays  += quot;
		quot = (long) (deltaMinute / 60);
		deltaMinute  -= quot * 60L;
		deltaHour  += quot;
		quot = (long) (deltaSecond / 60);
		deltaSecond  -= quot * 60L;
		deltaMinute  += quot;
		quot = (long) (deltaMinute / 60);
		deltaMinute  -= quot * 60L;
		deltaHour  += quot;
		quot = (long) (deltaHour / 24);
		deltaHour  -= quot * 24L;
		deltaDays  += quot;
		sum = ((((date.get(Calendar.HOUR) * 60L) + date.get(Calendar.MINUTE)) * 60L) + date.get(Calendar.SECOND)) +
		      (((( deltaHour   * 60L) +  deltaMinute)  * 60L) +  deltaSecond);
		if (sum < 0L) {
			quot = (long) (sum / 86400L);
			sum -= quot * 86400L;
			deltaDays += quot;
			if (sum < 0L) {
				sum += 86400L;
				deltaDays--;
			}
		}
		if (sum > 0L) {
			quot  = (long) (sum / 60);
			date.set(Calendar.SECOND, (int)  (sum - quot * 60L));
			sum   = quot;
			quot  = (long) (sum / 60);
			date.set(Calendar.MINUTE, (int)  (sum - quot * 60L));
			sum   = quot;
			quot  = (long) (sum / 24);
			date.set(Calendar.HOUR, (int)  (sum - quot * 24L));
			deltaDays   += quot;
		} else {
			date.set(Calendar.HOUR, 0);
			date.set(Calendar.MINUTE, 0);
			date.set(Calendar.SECOND, 0);
		}

		ret = addDeltaDays(date, deltaDays);
		return(ret);
	}

	protected int dayOfWeek( int year, int month, int day ) {
		long days;
		
		days = dateToDays(year,month,day);
		if (days > 0L) {
			days--;
			days %= 7L;
			days++;
		} 
		return (int)days;
	}

	protected boolean nthWeekday( Calendar date, int dow, int n ) {
		int  mm = date.get(Calendar.MONTH)+1;
		int  first;
		long delta;
		
		date.set(Calendar.DAY_OF_MONTH, 1);
		first = dayOfWeek(date.get(Calendar.YEAR), mm, 1);
		if (dow < first) dow += 7;
		delta = (long) (dow - first);
		delta += (n-1) * 7L;
		if( addDeltaDays( date, delta ) && (date.get(Calendar.MONTH)+1 == mm))
			return true;
		else
			return false;
	}

	protected static String utcPrintf( Calendar date ) {
		//String strDate;

		SimpleDateFormat frmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		return( frmt.format(date.getTime()).concat("T") );
	}

	protected static Calendar utcScanf( String strDate, TimeZone tz ) throws ParseException {
		Calendar date = Calendar.getInstance(tz);
		
		SimpleDateFormat frmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		date.setTime(frmt.parse(strDate));
		
		return( date );
	}

	protected Calendar nowDate() {
		Calendar date = getCalendarIntance();
		return date;
	}

	protected long weeksBetween( long start, long end, int dowstart, int dowend ) {
		long rval;
		
		rval = end - start - ( 7 - dowstart ) - dowend;

		if( rval < 0L )
			return 0L;
		else
			return rval;
	}

	protected int weekNumber( int year, int month, int day, int weekstart) {
		int offset = 0, i, c, first;
		
		first = dayOfWeek(year,1,1);
		for( i = 1, c = weekstart; i<= 7 ; i++, c++ ) {
			if( c > 7 ) c = 1;
			if( c == first ) i = 8; else offset++;
		}

		return( (int) ( (dateToDays(year,month,day) - dateToDays(year,1,1) + offset) / 7L ) + 1 );

	}

	protected int getCalendarDays( int year, int month, int weekstart,
            Integer monthdays, Integer pmonthdays, Integer firstdow, Integer boffset, Integer eoffset ) {
		int i, c;
		int iboffset, ieoffset;
		
		firstdow = new Integer( dayOfWeek( year, month, 1) );
		monthdays = new Integer( daysInMonth( month, year ) );
		pmonthdays = new Integer( daysInMonth( (month==1)?12:month-1, (month==1)?year-1:year ) );
		for( i = 1, c = weekstart, iboffset = 0; i<= 7 ; i++, c++ ) {
			if( c > 7 ) c = 1;
			if( c == firstdow.intValue() ) i = 8; else (iboffset)++;
		}
		if ( (ieoffset = (( iboffset + monthdays.intValue() ) % 7)) != 0 )
			ieoffset = 7 - ieoffset;
		
		boffset = new Integer(iboffset);
		eoffset = new Integer(ieoffset);
		return 1;
	}

	protected boolean firstDayOfWeek( int week, int weekstart, Calendar date) {
		int first, offset = 0, i, c;

		first = dayOfWeek(date.get(Calendar.YEAR),1,1);
		date.set(Calendar.MONTH, Calendar.JANUARY);
		date.set(Calendar.DAY_OF_MONTH, 1);
		
		for( i = 1, c = weekstart; i<= 7 ; i++, c++ ) {
			if( c > 7 ) c = 1;
			if( c == first ) i = 8; else offset++;
		}

		return( addDeltaDays(date, (((week-1) * 7L) - offset)) );
	}

	/**
	 * Parse a RRULE like string, and returns the correspondent recurrence object.
	 * @param startDate: Start day of the recurrence
	 * @param rule: RRULE like string. @see RFC2447 for details
	 * @return A Yearly, Monthly, Weekly, or Daily recurrence object. 
	 */
	public static Recurrence parse(Calendar startDate, String rule) {
    	Recurrence recurrence;
    	StringTokenizer stk;
    	int count = 0;
    	Calendar endDate = null;
    	int type = 0;
    	int day1 = 0, day2 = 0;
    	int month = 0;
    	int interval = 1;
    	int freq = RECUR_UNKNOWN;
    	
		//System.out.println("GOT: " + rule);
		if (rule.startsWith("RRULE:"))
			stk = new StringTokenizer(rule.substring("RRULE:".length()), ";");
		else
			stk = new StringTokenizer(rule, ";");
		while (stk.hasMoreTokens())	{
			String curRule = stk.nextToken();
			String key = curRule.substring(0, curRule.indexOf("=")).trim();
			String value = curRule.substring(curRule.indexOf("=") + 1).trim();

			if ("FREQ".equals(key))	{
				freq = getFrequencyFromString(value);
				if (freq == RECUR_UNKNOWN)
					throw new IllegalArgumentException("Invalid or unsupported FREQ value; " + rule);
			}
			else if ("COUNT".equals(key)) {
				count = Integer.parseInt(value);
				if (count < 1)
					throw new IllegalArgumentException("COUNT < 1");
			}
			else if ("UNTIL".equals(key)) {
				try	{
					endDate = Recurrence.utcScanf(value,startDate.getTimeZone()); 
				}
				catch (ParseException e1) {
					throw new IllegalArgumentException("Invalid date format");
				}
			}
			else if ("INTERVAL".equals(key)) {
				interval = Integer.parseInt(value);
				if (interval < 1)
					throw new IllegalArgumentException("INTERVAL < 1");
			}
			else if ("BYDAY".equals(key)) {
				if( freq == RECUR_DAILY ) {
					if( value.equals("MO,TU,WE,TH,FR"))
						type = 1;
					else if( value.equals("SA,SU") )
						type = 2;
				} else {
					String elem;
					StringTokenizer days = new StringTokenizer(value, ",");
					type = 1;
					int day = 0;
				
					while (days.hasMoreTokens()) {
						elem = days.nextToken();
						String dayOfWeek = elem.substring(elem.length()-2);
						if (elem.length() > 2) {
							if (freq == RECUR_WEEKLY)
								new IllegalArgumentException("BYDAY: Illegal or unsupported format in this frequency type");
						
							day1 = Integer.parseInt(elem.substring(0, elem.length()-2));
						}
						// we get a bitstream with the days
						day |= (1 << (getWeekdayFromString(dayOfWeek)-1));
					}
				
					if (freq == RECUR_WEEKLY)
						day1 = day;
					else {
						if (day == 0x7F) // 1111111 : day
							day2 = 8;
						else if (day == 0x1F) // 11111 : weekdays
							day2 = 9;
						else if (day == 0x60) // 1100000 : weekends
							day2 = 10;
						else {
							day2 = 1;
							while ((day & 1) == 0) {
								day2++;
								day >>= 1;
							}
						}
					}
				}
			}
			else if ("BYMONTHDAY".equals(key))	{
				if (freq != RECUR_YEARLY && freq != RECUR_MONTHLY)
					throw new IllegalArgumentException("BYMONTHDAY: Illegal or unsupported in this frequency type");
				day1 = Integer.parseInt(value);
			}
			else if ("BYMONTH".equals(key))	{
				if (freq != RECUR_YEARLY && freq != RECUR_MONTHLY)
					throw new IllegalArgumentException("BYMONTHDAY: Illegal or unsupported in this frequency type");
				
				month = Integer.parseInt(value);
			}
			else if ("BYSETPOS".equals(key))	{
				if (freq != RECUR_YEARLY && freq != RECUR_MONTHLY)
					throw new IllegalArgumentException("BYSETPOS: Illegal or unsupported in this frequency type");
				
				day1 = Integer.parseInt(value);
			}
			else {
				throw new IllegalArgumentException("Illegal or unsupported key: " + key);
			}

		}

		switch (freq) {
		case RECUR_DAILY:
			recurrence = new DailyRecurrence(startDate, endDate, count, type, interval);
			break;
		case RECUR_WEEKLY:
			recurrence = new WeeklyRecurrence(startDate, endDate, count, day1, interval);
			break;
		case RECUR_MONTHLY:
			recurrence = new MonthlyRecurrence(startDate, endDate, count, type, day1, day2, interval);
			break;
		case RECUR_YEARLY:
			recurrence = new YearlyRecurrence(startDate, endDate, count, type, day1, day2, month, interval);
			break;
		default:
			throw new IllegalArgumentException("Invalid RRULE");
			
		}
		recurrence.tz = startDate.getTimeZone();
		return recurrence;

	}
	private static int getFrequencyFromString(String freq) {
		int value = RECUR_UNKNOWN;
		
		if ("DAILY".equals(freq)) {
			value = RECUR_DAILY;
		}
		else if ("WEEKLY".equals(freq)) {
			value = RECUR_WEEKLY;
		}
		else if ("MONTHLY".equals(freq)) {
			value = RECUR_MONTHLY;
		}
		else if ("YEARLY".equals(freq)) {
			value = RECUR_YEARLY;
		}

		return value;
	}

	private static int getWeekdayFromString(String day) {
		int value = 0;
		
		if ("MO".equals(day)) {
			value = 1;
		}
		else if ("TU".equals(day)) {
			value = 2;
		}
		else if ("WE".equals(day)) {
			value = 3;
		}
		else if ("TH".equals(day)) {
			value = 4;
		}
		else if ("FR".equals(day)) {
			value = 5;
		}
		else if ("SA".equals(day)) {
			value = 6;
		}
		else if ("SU".equals(day)) {
			value = 7;
		}

		return value;
	}

	private static final int RECUR_YEARLY = 1;
	private static final int RECUR_MONTHLY = 2;
	private static final int RECUR_WEEKLY = 3;
	private static final int RECUR_DAILY = 4;
	private static final int RECUR_UNKNOWN = 0;

	protected Calendar startdate;
	protected Calendar enddate;
	
}

