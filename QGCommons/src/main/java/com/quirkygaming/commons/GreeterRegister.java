package com.quirkygaming.commons;

import java.util.HashMap;

import org.bukkit.entity.Player;

import com.quirkygaming.commons.listeners.GreeterListener;

public class GreeterRegister {
	
	static HashMap<String, GreeterListener> top = new HashMap<String, GreeterListener>();
	static HashMap<String, GreeterListener> middle = new HashMap<String, GreeterListener>();
	static HashMap<String, GreeterListener> bottom = new HashMap<String, GreeterListener>();

	static void addListener(GreeterListener l, GreeterPriority p) {
		map(p).put(l.getName(), l);
	}
	
	public enum GreeterPriority {
		TOP,
		MIDDLE,
		BOTTOM
	}
	
	static void send(GreeterPriority p, Player player) {
		for (GreeterListener l : map(p).values()) {
			l.sendGreeting(player);
		}
	}
	
	private static HashMap<String, GreeterListener> map(GreeterPriority p) {
		switch (p) {
			case TOP:return top;
			case MIDDLE:return middle;
			default:return bottom;
		}
	}
}
