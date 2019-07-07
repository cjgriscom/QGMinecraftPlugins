package com.quirkygaming.qgcustoms.modules.externalization;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class JukeEvent extends Event {
	
	public static final HandlerList hl = new HandlerList();
	
	Material disc;Block b;
	
	public JukeEvent(Material disc, Block b) {
		this.disc=disc;this.b=b;
	}
	
	@Override
	public HandlerList getHandlers() {
		return hl;
	}
	public static HandlerList getHandlerList() {
		return hl;
	}
	
	public Block getBlock() {return b;}
	public Material getDisc() {return disc;}
	
}
