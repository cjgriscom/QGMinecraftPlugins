package com.quirkygaming.qgcustoms.modules;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class ChatColorModule extends CustomModule {

	public ChatColorModule(QGCustomsPlugin plugin) {
		super("ChatColor", new Listener[]{new CCListener()}, new String[]{}, plugin);
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
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
	
	public static class CCListener implements Listener {

		@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
		public void onPlayerDeath(PlayerDeathEvent event) {
			event.setDeathMessage(ChatColor.RED + event.getDeathMessage());

		}

	}
	
}
