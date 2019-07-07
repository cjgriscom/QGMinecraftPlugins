package com.quirkygaming.qgcustoms.modules;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class SurvivalGamesModule extends CustomModule {
	
	public SurvivalGamesModule(QGCustomsPlugin plugin) {
		super("SGManager", new Listener[]{new ModuleListener()}, new String[]{}, plugin);
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
		
		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
			public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
			String[] msg = event.getMessage().toLowerCase().split("\\s+");
			if (msg.length < 2) return;
			if (msg[1].equals("join")) {
				if (msg[0].equals("/sg") || 
						msg[0].equals("/hg") ||
						msg[0].equals("/hungergames") ||
						msg[0].equals("/survivalgames")) {
					event.setCancelled(true);
					event.getPlayer().sendMessage(ChatColor.RED + "No permission to remotely join arenas.");
				}
			}
			
			
		}
	}
	
}
