package com.quirkygaming.commons.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import com.quirkygaming.commons.InternalListener.Dispatch;

public final class ItemFocusEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	private ItemStack item;
	private Player player;
	private Dispatch d;
	
	public ItemFocusEvent(Player player, ItemStack item, Dispatch d) {
		this.player = player;
		this.item = item;
		this.d = d;
	}
	
	public Player getPlayer() {
		return player;
	}
	public ItemStack getItem() {
		return item;
	}
	public void setItem(ItemStack i) {
		d.setItem(i);
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
