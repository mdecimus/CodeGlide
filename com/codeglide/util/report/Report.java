package com.codeglide.util.report;

import java.util.Iterator;

public class Report {
	public final static int SUMMARY_COUNT = 0;
	public final static int SUMMARY_AVERAGE = 1;
	public final static int SUMMARY_SUM = 2;
	public final static int SUMMARY_MIN = 3;
	public final static int SUMMARY_MAX = 4;
	
	public final static int CHART_PIE = 0;
	// add more
	
	public final static int FIELD_STRING = 0;
	public final static int FIELD_INTEGER = 0;
	public final static int FIELD_DOUBLE = 0;
	
	public void addDisplayField(String fieldId, int type) {
		
	}
	
	public void addDisplayField(String fieldId, int type, HighlightSettings highlight ) {
		
	}
	
	public void addGrouping(String fieldId, boolean isDescendant ) {
		
	}
	
	public void addSummary(int type, String fieldId ) {
		
	}
	
	public void addSummary(int type, String fieldId, HighlightSettings highlight ) {
		
	}
	
	public void addHighlightField(String fieldId, String rgbColor, int highlightCondition, double value ) {
		
	}
	
	public void setChartTitle(String chartTitle) {
		
	}
	
	public void setChartType(int chartType) {
		
	}

	public void setChartXAxis(String summaryFieldId) {
		
	}
	
	public void setChartYAxis(String groupFieldId) {
		
	}
	
	public void setChartGroupings(String groupFieldId) {
		// This is for Grouped, Stacked, Stacked to 100% charts
	}
	
	public void setRecordIterator(Iterator<Record> recordIterator ) {
		
	}
	
	public String getReportDefinition() {
		return null;
	}
	
	public void setReportDefinition(String reportDefinition) {
		
	}
	
	public class HighlightSettings {
		public final static int COND_EQUALS = 0;
		public final static int COND_LT = 1;
		public final static int COND_GT = 2;
		
		String rgbColor;
		int condition;
		double value;
	}
	
}
