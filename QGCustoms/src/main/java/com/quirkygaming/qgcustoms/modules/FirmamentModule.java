package com.quirkygaming.qgcustoms.modules;

import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class FirmamentModule extends CustomModule {
	
	public FirmamentModule(QGCustomsPlugin plugin) {
		super("Firmament", new Listener[]{new FListener()}, new String[]{}, plugin);
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
	
	public static class FListener implements Listener {

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void onEnderPearl(final PlayerTeleportEvent event) { // Prevent players from getting above the nether
			if (event.getCause() == TeleportCause.ENDER_PEARL) {
				if (event.getFrom().getWorld().getEnvironment() == Environment.NETHER) {
					if (event.getFrom().getBlockY() < 128) {
						if (event.getTo().getBlockY() >= 126) {
							event.setCancelled(true);
							Bukkit.getLogger().warning("Blocked attempt to exceed bedrock height by " + event.getPlayer().getName());
						}
					}
				}
			}
		}
	}
	
}
