package com.quirkygaming.qgsigns.modules.spy;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.quirkygaming.qgsigns.PlayerInfoE;
import com.quirkygaming.qgsigns.api.SignModule;
import com.quirkygaming.qgwarps.QGWarpsAPI;

public class Spy extends SignModule {

	public Spy() {
		super("spy");
	}
	
	@Override
	public boolean matchesHeader(String match) {
		return match.equals("["+ID()+"]") || match.startsWith("~");
	}
	
	private void eventDispatch(List<String> lines, Location l, Player player) {
		if (lines.get(0).equalsIgnoreCase("[spy]")) {
			doSpyEvent(lines, lines.get(1).concat(lines.get(2)).concat(lines.get(3)));
		} else if (lines.get(0).startsWith("~")) {
			doSpyEvent(lines, lines.get(0).substring(1));
		}
	}
	
	@Override
	public void onSignCreation(List<String> lines, Location l, Player player,
			Cancellable cancellable) {
		eventDispatch(lines, l, player);
	}

	@Override
	public void onSignClick(List<String> lines, Location l, Player player) {
		eventDispatch(lines, l, player);
	}

	@Override
	public void onSignDestroy(List<String> lines, Location l, Player player,
			Cancellable cancellable) {
		//
	}
	
	private void doSpyEvent(List<String> lines, String username) {
		
		if (username == null || username.trim().equals("")) return;
		
		PlayerInfoE playerInfo = new PlayerInfoE(username);
		String playerStatus = "Idle";
		if (playerInfo.sneaking()) {
			playerStatus = "Crouching";
		} else if (playerInfo.sprinting()) {
			playerStatus = "Sprinting";
		} else if (playerInfo.digging()) {
			playerStatus = "Digging";
		} else if (playerInfo.traveling()) {
			playerStatus = "Traveling";
		} else if (playerInfo.sleeping()) {
			playerStatus = "Sleeping";
		} else if (playerInfo.dead()) {
			playerStatus = "Dead";
		} else if (playerInfo.banned()) {
			playerStatus = "Banned";
		} else if (playerInfo.offline()) {
			playerStatus = "Offline";
		}
		
		String signName = playerInfo.username();
		if (signName.length() >= 15) {
			signName = signName.substring(0, signName.length()-2).concat("*");
		}
		lines.set(0, "~" + signName);
		lines.set(1, playerStatus);
		
		if (!playerInfo.offline()){
			//lines.set(2, "L " + (playerInfo.level() + (float)playerInfo.expPercentage()/100));
			
			String region = QGWarpsAPI.getRegion(playerInfo.location());
			if (region != null) lines.set(2, region);
			else {
				switch(playerInfo.location().getWorld().getEnvironment()) {
					case NORMAL:  lines.set(2, "Overworld"); break;
					case NETHER:  lines.set(2, "Nether"); break;
					case THE_END: lines.set(2, "The End"); break;
				}
			}
			lines.set(3, "L " + playerInfo.level() + ", " + playerInfo.health() + " HP, " + playerInfo.food() + " F");
		} else {
			Date playerLastLogin = playerInfo.lastLogin();
			if (playerLastLogin != null) {
				DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
				DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
				dateFormatter.setTimeZone(Calendar.getInstance().getTimeZone());
				timeFormatter.setTimeZone(Calendar.getInstance().getTimeZone());
				String dateOut = dateFormatter.format(playerLastLogin);
				String timeOut = timeFormatter.format(playerLastLogin);
				lines.set(2, "Seen: " + dateOut);
				lines.set(3, timeOut);
			} else {
				lines.set(2, "Seen: Never");
				lines.set(3, "");
			}	
		}
	}

}
