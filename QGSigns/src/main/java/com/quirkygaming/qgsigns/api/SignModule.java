package com.quirkygaming.qgsigns.api;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public abstract class SignModule {
	
	private final String header;
	
	public SignModule(String signHeader) {
		this.header = signHeader;
	}
	
	public String ID() {
		return header;
	}
	
	public boolean matchesHeader(String match) {
		return match.equals("["+header+"]");
	}
	
	public abstract void onSignCreation(List<String> lines, Location l, Player player, Cancellable cancellable);
	
	public abstract void onSignClick(List<String> lines, Location l, Player player);
	
	public abstract void onSignDestroy(List<String> lines, Location l, Player player, Cancellable cancellable);
	
}
