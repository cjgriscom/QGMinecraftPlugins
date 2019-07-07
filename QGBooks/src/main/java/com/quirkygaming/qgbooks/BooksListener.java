package com.quirkygaming.qgbooks;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ShapelessRecipe;

import com.quirkygaming.commons.events.ItemFocusEvent;

public class BooksListener implements Listener {
	
	private QGBooks plugin;
	
	public BooksListener(QGBooks plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onCraftEvent(CraftItemEvent event) {
		if (event.getRecipe().getResult().getType() == Material.WRITTEN_BOOK) {
			try {
				ShapelessRecipe r = (ShapelessRecipe) event.getRecipe();
				if (r.getIngredientList().containsAll(plugin.messageBookRecipe.getIngredientList())) {
					plugin.handleCraftingEvent(event);
				}
			} catch (Exception e) {}
		}
		
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onItemFocus(final ItemFocusEvent event) {
		if (event.getItem() != null) {
			if (event.getItem().getAmount() == 1) {
				plugin.processEvent(event.getPlayer(), event.getItem());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		if (event.getMessage().startsWith("@") || event.getMessage().startsWith("#")) {
			event.setCancelled(true);
			Bukkit.getScheduler().runTask(plugin, new Runnable() {
				public void run() {
					if (plugin.processChatEvent(event)) {
						for (final Player player : plugin.getServer().getOnlinePlayers()) {
							plugin.processEvent(player, player.getInventory().getItemInMainHand());
							// Update in-hand message books that may not otherwise get updated
						}
					}
				}
			});
		}
		
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onConsoleCommand(final ServerCommandEvent event) {
		if (plugin.processCommandEvent(event)) {
			for (Player player : plugin.getServer().getOnlinePlayers()) { 
				plugin.processEvent(player, player.getInventory().getItemInMainHand()); // Update in-hand message books that may not otherwise get updated
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void onPlayerLogout(final PlayerQuitEvent event) {
		plugin.lastPlayed.put(event.getPlayer().getName(), new Date().getTime());
	}

}
