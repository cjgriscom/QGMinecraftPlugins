package com.quirkygaming.commons;

import java.util.LinkedList;
import java.util.Queue;

import com.quirkygaming.propertylib.MutableProperty;

public class PromptQueue {
	Queue<Prompt> q = new LinkedList<Prompt>();
	
	final MutableProperty<String> response = MutableProperty.newProperty("CANCEL");
	final MutableProperty<Boolean> canceled = MutableProperty.newProperty(false);
	
	int defaultTimeoutTicks = 20*45;
	
	String cancelString;
	
	public PromptQueue(String cancelString) {
		this.cancelString = cancelString;
	}
	
	public PromptQueue(String cancelString, int defaultTimeoutTicks) {
		this.cancelString = cancelString;
		this.defaultTimeoutTicks = defaultTimeoutTicks;
	}
	
	public void startChain() {
		if (!q.isEmpty()) q.remove().link();
	}
	
	public void cancel() {
		canceled.set(true);
	}
}
