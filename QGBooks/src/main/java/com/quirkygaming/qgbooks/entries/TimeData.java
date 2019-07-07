package com.quirkygaming.qgbooks.entries;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String date;
	private String time;
	private long epochDate;
	
	public TimeData(){}
	
	public TimeData(String date, String time, long epochDate) {
		this.epochDate = epochDate;
		this.time = time;
		this.date = date;
	}
	
	public TimeData(long epochDate) {
		this.epochDate = epochDate;
		
		DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
		dateFormatter.setTimeZone(Calendar.getInstance().getTimeZone());
		timeFormatter.setTimeZone(Calendar.getInstance().getTimeZone());
	
		this.time = timeFormatter.format(epochDate); 
		this.date = dateFormatter.format(epochDate); 
	}
	
	public static TimeData newCurrentTime() {
		return new TimeData((new Date()).getTime());
	}

	public String date() {
		return date;
	}
	public String time() {
		return time;
	}
	public long epochFormat() {
		return epochDate;
	}

}
