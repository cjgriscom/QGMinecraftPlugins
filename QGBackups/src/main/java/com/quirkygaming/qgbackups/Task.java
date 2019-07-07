package com.quirkygaming.qgbackups;

import java.io.Serializable;

import com.quirkygaming.propertylib.MutableProperty;

public class Task implements Serializable {
	private static final long serialVersionUID = 1;
	
	MutableProperty<String> name = MutableProperty.newProperty(null);
	MutableProperty<Timing> timingType = MutableProperty.newProperty(null);
	MutableProperty<TaskType> task = MutableProperty.newProperty(null);
	MutableProperty<String> item1 = MutableProperty.newProperty(null);
	MutableProperty<String> item2 = MutableProperty.newProperty(null);
	MutableProperty<TimeSlot> time = MutableProperty.newProperty(null); // TODO issue: syncing the timeslot duplicates the object
	
	public String scriptEntry() {
		if (task.get() == TaskType.CLONE_DIR) {
			return "cp -vR " + item1 + " " + item2 + "\n";
		} else if (task.get() == TaskType.SCRIPT) {
			return item1 + "\n";
		}
		return "";
	}
	
	public static enum TaskType {
		SCRIPT, CLONE_DIR, RESTART
	}
	
	public static enum Timing {
		PERIODIC, ALWAYS, REQUEST
	}
}
