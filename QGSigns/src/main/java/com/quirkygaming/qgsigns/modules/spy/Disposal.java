package com.quirkygaming.qgsigns.modules.spy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.quirkygaming.qgsigns.api.SignModule;

public class Disposal extends SignModule {

	public Disposal() {
		super("disposal");
	}

	@Override
	public void onSignCreation(List<String> lines, Location l, Player player, Cancellable cancellable) {
		// nothing
		
	}

	@Override
	public void onSignClick(List<String> lines, Location l, Player player) {
		player.openInventory(Bukkit.getServer().createInventory(player, 36, "Disposal"));
	}

	@Override
	public void onSignDestroy(List<String> lines, Location l, Player player, Cancellable cancellable) {
		// nothing
		
	}

}
