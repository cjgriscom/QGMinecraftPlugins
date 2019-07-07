package com.quirkygaming.qgsigns;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerInfoE extends com.quirkygaming.commons.playerinterface.PlayerInfo {
	
	public PlayerInfoE(String username) {
		super(username);
		
	}
	
	public PlayerInfoE(Player player) {
		super(player);
	}
	
	public Location location() {
		if (player != null) {
			return player.getLocation();
		}
		return null;
	}
	
	public boolean digging() {
		if (player != null) {
			Long lastClickTime = SignsListener.timeSinceLastClick.get(player.getName());
			if (lastClickTime != null){
				if (lastClickTime >= System.currentTimeMillis() - 1500) {
					return true;
				}
			}
		}
		return false;
	}

}
