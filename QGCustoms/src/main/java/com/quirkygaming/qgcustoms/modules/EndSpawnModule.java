package com.quirkygaming.qgcustoms.modules;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import com.quirkygaming.commons.Util;
import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class EndSpawnModule extends CustomModule {

	public EndSpawnModule(QGCustomsPlugin plugin) {
		super("EndSpawn", new Listener[]{new ESListener()}, new String[]{}, plugin);
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
	
	public static class ESListener implements Listener {

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void onPlayerPortal(PlayerPortalEvent event) {

			Player player = event.getPlayer();
			World world = event.getTo().getWorld();
			if (world.getName().endsWith("the_end")
					&& !event.getFrom().getWorld().getName().endsWith("the_end")) {
				// Cause players to observe the end spawn that I set
				event.setCancelled(true);
				player.teleport(world.getSpawnLocation());
			}
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void onEntityPortal(EntityPortalEvent event) {

			// Cause entities to observe the end spawn that I set
			// Waiting a tick is required for some reason; entities disappear
			// otherwise

			final Entity entity = event.getEntity();
			if (!entity.getType().equals(EntityType.PLAYER)) {
				if (event.getTo() == null) return;
				final World world = event.getTo().getWorld();
				if (world == null) return;
				if (world.getName().endsWith("the_end") && !event.getFrom().getWorld().getName().endsWith("the_end")) {
					plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							Util.loadChunks(world.getSpawnLocation(), 10);
							entity.teleport(world.getSpawnLocation());
						}
					}, 1);
				}
			}

		}

	}
	
}
