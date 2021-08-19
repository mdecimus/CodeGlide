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
package com.codeglide.util;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class ISO8601
{
	private static DateFormat ISO8601UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static DateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private static DateFormat ISO8601NonSepUTC = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
	private static DateFormat ISO8601NonSep = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	private static DateFormat ShortDate = new SimpleDateFormat("yyyy-MM-dd");
	private static DateFormat ShortDateNonSep = new SimpleDateFormat("yyyyMMdd");
	
	private static Pattern iso8601utcPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");
	private static Pattern iso8601localPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
	private static Pattern iso8601utcNonSepWithoutTimezone = Pattern.compile("\\d{8}T\\d{6}");
	private static Pattern iso8601tzPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[\\+-]\\d{2}:\\d{2}");
	private static Pattern iso8601utcNonSepPattern = Pattern.compile("\\d{8}T\\d{6}Z");
	//private static Pattern iso8601tzNonSepPattern = Pattern.compile("\\d{8}T\\d{6}[\\+-]\\d{4}");
	private static Pattern shortDatePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
	private static Pattern shortDateNonSepPattern = Pattern.compile("\\d{4}\\d{2}\\d{2}");
	
	static
	{
		ISO8601UTC.setTimeZone(TimeZone.getTimeZone("GMT"));
		ISO8601NonSepUTC.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	public static Date parseDate(String date) throws ParseException
	{
		if (iso8601utcPattern.matcher(date).matches())
		{
			return ISO8601UTC.parse(date);
		}
		else if (iso8601utcNonSepPattern.matcher(date).matches())
		{
			return ISO8601NonSepUTC.parse(date);
		}
		else if (iso8601utcNonSepWithoutTimezone.matcher(date).matches())
		{
			return ISO8601NonSep.parse(date);
		}
		else if (shortDateNonSepPattern.matcher(date).matches())
		{
			return ShortDateNonSep.parse(date);
		}
		else if (shortDatePattern.matcher(date).matches())
		{
			return ShortDate.parse(date);
		}
		else if (iso8601localPattern.matcher(date).matches())
		{
			return ISO8601.parse(date);
		}
		else if (iso8601tzPattern.matcher(date).matches())
		{
			Date baseDate;

			baseDate = ISO8601.parse(date.substring(0, 19));

			int minutes = parseOffsetMinutes(date.substring(19));
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(baseDate);
			cal.add(Calendar.MINUTE, -minutes);
			return cal.getTime();
		}
		else
			throw new ParseException("Invalid dateTime format", 0);

	}
	
	public static int parseOffsetMinutes(String offset) throws ParseException
	{
		if (offset.length() != 6 || offset.charAt(3) != ':')
			throw new ParseException("Invalid time offset format", 0);
		
		int minutes = Integer.parseInt(offset.substring(1, 3)) * 60 + Integer.parseInt(offset.substring(4, 6));
		char sign = offset.charAt(0);
		if (sign == '-')
			minutes *= -1;
		else if (sign != '+')
			throw new ParseException("Invalid time offset sign", 0);
		
		return minutes;
	}
	
	public static String formatUtc(Date date)
	{
		return ISO8601UTC.format(date);
	}
	
	public static String formatUtcWithoutSeparators(Date date)
	{
		return ISO8601NonSepUTC.format(date);
	}
	
	public static Date toLocal(Date date)
	{
		return toLocal(date, getLocalOffsetMinutes(date));
	}
	
	public static Date toLocal(Date date, int offsetMinutes)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MINUTE, offsetMinutes);
		return cal.getTime();
	}
	
	public static int getLocalOffsetMinutes(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int offsetMinutes = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 1000);
		return offsetMinutes;
	}
	
	public static String formatLocal(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int offsetMinutes = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 1000);
		return formatLocal(date, offsetMinutes);
	}
	
	public static String formatLocal(Date date, int offsetMinutes)
	{
		String dateStr = ISO8601.format(date);
		int offMinutes = Math.abs(offsetMinutes % 60);
		int offHours = Math.abs((offsetMinutes - offMinutes) / 60);
		
		StringWriter dateWriter = new StringWriter();
		dateWriter.write(dateStr);
		
		NumberFormat nf = new DecimalFormat("00");
		
		dateWriter.write(offsetMinutes > 0 ? "+" : "-");
		dateWriter.write(nf.format(offHours));
		dateWriter.write(":");
		dateWriter.write(nf.format(offMinutes));
		
		return dateWriter.toString();
	}
}
