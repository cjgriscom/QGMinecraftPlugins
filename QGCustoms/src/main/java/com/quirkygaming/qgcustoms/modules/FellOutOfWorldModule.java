package com.quirkygaming.qgcustoms.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class FellOutOfWorldModule extends CustomModule {
	
	// Prevent players from falling out of world on spawn
	public FellOutOfWorldModule(QGCustomsPlugin plugin) {
		super("FellOutOfWorld", new Listener[]{new ModuleListener()}, new String[]{}, plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args) {
		return false;
	}

	@Override
	public boolean onEnable() {
		return true;
	}

	@Override
	public boolean onDisable() {
		return true;
	}
	
	public static class ModuleListener implements Listener {

		@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
		public void onTele(PlayerTeleportEvent e) {
			final Location to = e.getTo();
			if (	e.getCause() == TeleportCause.COMMAND || 
					e.getCause() == TeleportCause.NETHER_PORTAL || 
					e.getCause() == TeleportCause.END_PORTAL || 
					e.getCause() == TeleportCause.END_GATEWAY || 
					e.getCause() == TeleportCause.PLUGIN) {
				
				Player p = e.getPlayer();
				
				correctTraj(p, to.add(0, .1, 0), to);
			}
			
		}
		
		private void correctTraj(final Player p, final Location backup, final Location oldLocation) {
			if (!p.isOnline()) return;
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
				if (!p.isOnline()) return;
				Location currentLocation = p.getLocation();
				if (!currentLocation.getWorld().getName().equals(oldLocation.getWorld().getName())) return; // If player has already left world don't mess up y coords
				//p.sendMessage(ChatColor.GOLD + "" +System.currentTimeMillis());
				//p.sendMessage(ChatColor.GOLD + "Y: " + p.getLocation().getY());
				//p.sendMessage(ChatColor.GOLD + "Y disp: " + (oldLocation.getBlockY() - currentLocation.getBlockY()));
				if (p.getLocation().getY() < 0) {
					p.sendMessage(ChatColor.GOLD + "Correcting trajectory...");
					p.setVelocity(new Vector(0, 0, 0));
					p.setFallDistance(0);
					p.teleport(backup);
				} else if (oldLocation.getBlockY() - currentLocation.getBlockY() >= 1) {
					correctTraj(p, backup, currentLocation);
				}
			}, 20);
			
		}

	}
	
}
