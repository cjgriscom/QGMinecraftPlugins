package com.quirkygaming.commons;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.quirkygaming.commons.events.ItemFocusEvent;
import com.quirkygaming.commons.playerinterface.ServerCommandEventCanceller;

public class InternalListener implements Listener {
	
	public static String SERVER_NAME = "CONSOLE";
	
	public interface Dispatch {
		public void setItem(ItemStack i);
	}
	
	
	public InternalListener(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	
	private void doIFEvent(Player p, ItemStack i, Dispatch d) {
		Bukkit.getServer().getPluginManager().callEvent(new ItemFocusEvent(p,i,d));
	}
	
	
	class PlayerInteractDispatch implements Dispatch {
		PlayerInteractEvent event;
		PlayerInteractDispatch(PlayerInteractEvent event) {this.event=event;}
		public void setItem(ItemStack i) {event.getPlayer().setItemOnCursor(i);}
	}
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			if (event.hasItem()) {
				doIFEvent(event.getPlayer(), event.getItem(), new PlayerInteractDispatch(event));
			}
		}
	}
	
	class CursorDispatch implements Dispatch {
		InventoryClickEvent event;
		CursorDispatch(InventoryClickEvent event) {this.event=event;}
		public void setItem(ItemStack i) {event.getView().setCursor(i);} //TODO find alternative / safety check
	}
	class CurrentDispatch implements Dispatch {
		InventoryClickEvent event;
		CurrentDispatch(InventoryClickEvent event) {this.event=event;}
		public void setItem(ItemStack i) {event.setCurrentItem(i);}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		
		try {
		if (event.getCurrentItem() != null) {
			doIFEvent(p, event.getCurrentItem(), new CurrentDispatch(event));
		}}catch(Exception e) {} // TODO getCurrentItem() throws random exceptions
		if (event.getCursor() != null) {
			doIFEvent(p, event.getCursor(), new CursorDispatch(event));
		}
	}
	
	class HeldDispatch implements Dispatch {
		PlayerItemHeldEvent event;
		HeldDispatch(PlayerItemHeldEvent event) {this.event=event;}
		public void setItem(ItemStack i) {event.getPlayer().getInventory().setItem(event.getNewSlot(),i);}
	}
	class PickupDispatch implements Dispatch {
		PlayerPickupItemEvent event;
		PickupDispatch(PlayerPickupItemEvent event) {this.event=event;}
		public void setItem(ItemStack i) {event.getItem().setItemStack(i);}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onItemHeld(final PlayerItemHeldEvent event) {
		ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
		if (item != null) {
			doIFEvent(event.getPlayer(), item, new HeldDispatch(event));
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onItemPickup(final PlayerPickupItemEvent event) {
		doIFEvent(event.getPlayer(), event.getItem().getItemStack(), new PickupDispatch(event));
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onConsoleCommand(final ServerCommandEvent event) {
		String sender = event.getSender().getName();
		if (QGCommonsPlugin.waitingPrompts.containsKey(sender)) {
			PromptDeclaration p = QGCommonsPlugin.waitingPrompts.get(sender);
			p.response.set(event.getCommand());
			p.callback.run();
			p.fulfilled = true;
			QGCommonsPlugin.waitingPrompts.remove(sender);
			new ServerCommandEventCanceller(event).setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerChat(final AsyncPlayerChatEvent e) {
		if (QGCommonsPlugin.waitingPrompts.containsKey(e.getPlayer().getName())) {
			e.setCancelled(true);
			Bukkit.getScheduler().scheduleSyncDelayedTask(QGCommonsPlugin.instance, new Runnable() {
				public void run() {
					PromptDeclaration p = QGCommonsPlugin.waitingPrompts.get(e.getPlayer().getName());
					p.response.set(e.getMessage());
					p.callback.run();
					p.fulfilled = true;
					QGCommonsPlugin.waitingPrompts.remove(e.getPlayer().getName());
				}
			});
		}
	}
}
