package com.quirkygaming.qgregions;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.quirkygaming.qgsigns.QGSigns;
import com.quirkygaming.qgsigns.api.SignModule;

public class RegionSignModule extends SignModule {
	
	QGRegions plugin;
	
	public RegionSignModule(QGRegions plugin) {
		super("region");
		QGSigns.registerModule(this);
		this.plugin = plugin;
	}

	@Override
	public void onSignCreation(List<String> lines, Location loc, Player player, Cancellable cancellable) {
		
		String name = (lines.get(1) + lines.get(2)).trim();
		if (name.isEmpty()) {
			cancellable.setCancelled(true);
			player.sendMessage(ChatColor.GOLD + "[QGRegions] Enter the region name on the first two lines of the sign.");
		} else {
			if (!plugin.beginAddingRegion(name, loc.getWorld().getName(), player)) {
				// Something's wrong; cancel sign creation
				cancellable.setCancelled(true);
			} else {
				plugin.addBoundary(player, loc);
			}
		}
		
	}

	@Override
	public void onSignClick(List<String> lines, Location l, Player player) {
		
	}

	@Override
	public void onSignDestroy(List<String> lines, Location l, Player player, Cancellable cancellable) {
		
	}
}


