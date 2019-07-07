package com.quirkygaming.qgwarps;

import org.bukkit.entity.Player;

import com.quirkygaming.propertylib.MutableProperty;

public interface WarpListener {
	
	public enum WarpType{
		SETHOME, HOME, SET, RETURN
	}
	
	// Extend and register this interface via the QGWarps class to decide if a player can warp or not.
	// Overwrite the property to change the reason 
	public boolean canWarp(Player p, WarpType type, MutableProperty<String> reason);
}
