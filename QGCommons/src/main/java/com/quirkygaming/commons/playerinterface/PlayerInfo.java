package com.quirkygaming.commons.playerinterface;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

// PlayerInfo v2.0

public class PlayerInfo {
	
	protected Player player;
	protected OfflinePlayer offlinePlayer;
	protected String username;

	public PlayerInfo(String username) {
		this.player = getPlayerByUsername(username.trim(), Bukkit.getServer().getOnlinePlayers());
		this.offlinePlayer = getPlayerByUsername(username.trim(), Bukkit.getServer().getOfflinePlayers());
		if (this.player != null) {
			this.username = player.getName();
			
		} else if (this.offlinePlayer != null) {
			this.username = offlinePlayer.getName();
			
		} else {
			this.username = username;
		}
	}
	
	public PlayerInfo(Player player) {
		this.player = player;
		this.username = player.getName();
	}
	
	public World getWorld() {
		if (player == null) return null;
		return player.getWorld();
	}
	
	public String username() {
		return username;
	}
	
	public UUID uniqueID() {
		if (offlinePlayer == null) return null;
		return offlinePlayer.getUniqueId();
	}
	
	public Player player() {
		return player;
	}
	
	public boolean offline() { //TODO i think OfflinePlayer is capable of returning an online response
		if (player == null) return true; // in which case I would want to switch to an online player
		return !player.isOnline();
	}
	
	public boolean banned() {
		if (player == null) {
			if (offlinePlayer!=null) return offlinePlayer.isBanned();
			return false;
		}
		return player.isBanned();
	}
	
	public boolean traveling() {
		if (player == null) return false;
		return (player.getVehicle() != null);
	}
	
	public boolean sprinting() {
		if (player == null) return false;
		return player.isSprinting();
	}
	
	public boolean sleeping() {
		if (player == null) return false;
		return player.isSleeping();
	}
	
	public boolean sneaking() {
		if (player == null) return false;
		return player.isSneaking();
	}
	
	public boolean dead() {
		if (player == null) return false;
		return player.isDead();
	}
	
	public int expPercentage() {
		if (player == null) return 0;
		double exp = player.getExp();
		double percent = exp * 100;
		return (int) percent;
	}
	
	public int level() {
		if (player == null) return 0;
		return player.getLevel();
	}
	
	public int health() {
		if (player == null) return 0;
		return (int) player.getHealth() / 2;
	}
	
	public int food() {
		if (player == null) return 0;
		return (int) player.getFoodLevel() / 2;
	}
	
	public Date lastLogin() {
		
		if (offlinePlayer != null) {
			long lastLoginEpochFormat = offlinePlayer.getLastPlayed();
			Date lastLogin = new Date(lastLoginEpochFormat);
		
			return lastLogin;
		}
		return null;
		
	}
	
	// Because Bukkit can't decide on a return type
	private <P extends OfflinePlayer> P getPlayerByUsername(String username, P[] players) {
		List<P> list = Arrays.asList(players); return getPlayerByUsername(username, list);
	}
	private <P extends OfflinePlayer> P getPlayerByUsername(String username, Collection<P> players) {
		List<P> list = new ArrayList<P>(players); return getPlayerByUsername(username, list);
	}
	private <P extends OfflinePlayer> P getPlayerByUsername(String username, List<P> players) {

		java.util.Collections.sort(players, PlayerComp.getInstance());
		
		for (P p : players) {
			if (p.getName().equalsIgnoreCase(username)) {
				return p;
			}
		}
		for (P p : players) {
			if (wildCardMatch(p.getName().toLowerCase(), username.toLowerCase())) {
				return p;
			}
		}
		return null;
	}
	
	private boolean wildCardMatch(String text, String pattern) {
		// Source: http://www.adarshr.com/papers/wildcard
		// Create the cards by splitting using a RegEx. If more speed 
		// is desired, a simpler character based splitting can be done.
		String [] cards = pattern.split("\\*");
		// Iterate over the cards.
		for (String card : cards) {
			int idx = text.indexOf(card);
			
			// Card not detected in the text.
			if(idx == -1) {
				return false;
			}
			
			// Move ahead, towards the right of the text.
			text = text.substring(idx + card.length());
		}
		
		return true;
	}
	
	static class PlayerComp implements Comparator<OfflinePlayer> {
		static PlayerComp c;
		@Override
		public int compare(OfflinePlayer p1, OfflinePlayer p2) {
			return Collator.getInstance().
					compare(p1.getName().toLowerCase(), p2.getName().toLowerCase());
		}
		public static PlayerComp getInstance() {
			if (c==null) c = new PlayerComp();
			return c;
		}
	}

}
