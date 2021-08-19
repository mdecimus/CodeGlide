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
package com.codeglide.core;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;


public class Logger {

	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("com.codeglide");

	public static final int  ALL = 0;
	public static final int  DEBUG = 1;
	public static final int  INFO = 2;
	public static final int  WARN = 3;
	public static final int  ERROR = 4;
	public static final int  FATAL = 5;
	public static final int  OFF = 6;

	/*	

	 * Substitute symbol

	%c Logger, %c{2 } last 2 partial names
	%C Class name (full agony), %C{2 } last 2 partial names
	%d{dd MMM yyyy HH:MM:ss } Date, format see java.text.SimpleDateFormat
	%F File name
	%l Location (caution: compiler-option-dependently)
	%L Line number
	%m user-defined message
	%n newLine
	%M Method name
	%p Level
	%r Milliseconds since program start
	%t Threadname
	%x, %X see Doku
	%% individual percentage sign
	Caution: %C, %F, %l, %L, %M SLOW DOWN program run!

	 * Logging Levels:

	DEBUG < INFO < WARN < ERROR < FATAL

	ALL: the lowest possible rank, intended to turn on all logging.
	OFF: the highest possible rank, intended to turn off logging.

	 */

	public static void init(int level, String path){

		switch (level) {

		case ALL:
			logger.setLevel(Level.ALL);
			break;
		case DEBUG:
			logger.setLevel(Level.DEBUG);
			break;
		case INFO:
			logger.setLevel(Level.INFO);
			break;
		case WARN:
			logger.setLevel(Level.WARN);
			break;
		case ERROR:
			logger.setLevel(Level.ERROR);
			break;
		case FATAL:
			logger.setLevel(Level.FATAL);
			break;
		case OFF:
			logger.setLevel(Level.OFF);
			break;
		default:
			logger.setLevel(Level.INFO);
		}
		
		// Date - Time - Level - Message
		//String pattern = "%d{ISO8601} - %p %m %n";
		PatternLayout patternLayout = null;
		
		try {
			patternLayout = new PatternLayout("%d{dd MMM yyyy HH:MM:ss} - %p %m %n");
		} catch (Throwable _) {
		}
		
		if(path != null && !path.isEmpty()){
			try {
				FileAppender fileAppender = new FileAppender(patternLayout, path, true);
				logger.addAppender(fileAppender);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else
			logger.addAppender(new ConsoleAppender(patternLayout));
		 
		// Separator
		logger.info("= = = = = = = = = = = = = = = = = = = = = = =");
	}

	public static void debug(Throwable e) {
		// Write debug
		StringWriter stackWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stackWriter));
		logger.debug(stackWriter.toString());
	}

	public static void debug(Object o) {
		logger.debug(o);
	}

	public static void error(Object o) {
		logger.error(o);
	}

	public static void warn(Object o) {
		logger.warn(o);
	}

	public static void info(Object o) {
		logger.info(o);
	}

	public static void fatal(Object o) {
		logger.fatal(o);
	}

}
