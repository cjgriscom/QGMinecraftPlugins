package com.quirkygaming.qgbackups;

import java.io.Serializable;

import com.quirkygaming.propertylib.MutableProperty;

public class TimeSlot implements Serializable {
	private static final long serialVersionUID = 1;
	
	void init() {
		//satisfied_hour = -1;
	}
	
	//transient int satisfied_hour = -1;
	
	MutableProperty<String> name  = MutableProperty.newProperty(null);
	MutableProperty<Integer> hour = MutableProperty.newProperty(null); // GMT
	MutableProperty<Integer> hour_period = MutableProperty.newProperty(null);
	MutableProperty<Integer> warning_start_minutes = MutableProperty.newProperty(null);
	MutableProperty<Integer> warning_period_seconds = MutableProperty.newProperty(null);
	MutableProperty<String> warning_message_prefix = MutableProperty.newProperty(null);
	
	public String toString() {
		return name.get();
	}
	
}
