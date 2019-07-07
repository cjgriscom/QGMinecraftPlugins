package com.quirkygaming.qgregions;

import static com.quirkygaming.qgregions.QGRegions.getAssocRegion;
import static com.quirkygaming.qgregions.structures.RegionField.name;
import static com.quirkygaming.qgregions.structures.RegionOpt.disable_warp_in;
import static com.quirkygaming.qgregions.structures.RegionOpt.disable_warp_out;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgregions.structures.Region;
import com.quirkygaming.qgregions.structures.RegionField;
import com.quirkygaming.qgregions.structures.RegionOpt;
import com.quirkygaming.qgwarps.WarpListener;

public class OptionListeners implements Listener, WarpListener {
	
	// Monitor events execute last (even after highest).  If something has been changed by Warps we will have picked it up.
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent e) {
		setMode(e.getPlayer(), e.getTo());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPortal(PlayerPortalEvent e) {
		setMode(e.getPlayer(), e.getTo());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSpawn(PlayerRespawnEvent e) {
		setMode(e.getPlayer(), e.getRespawnLocation());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent e) {
		setMode(e.getPlayer(), e.getPlayer().getLocation());
	}
	
	private void setMode(Player p, Location to_l) {
		Region to = getAssocRegion(to_l);
		if (to != null && to.isOptionSet(RegionOpt.adventure)) {
			p.setGameMode(GameMode.ADVENTURE);
		} else {
			if (p.getGameMode().equals(GameMode.ADVENTURE)) p.setGameMode(GameMode.SURVIVAL);
		}
	}
	
	private void tryCancel(Region r, Cancellable e, RegionOpt ifThis, Player p, String message) {
		if (r != null) {
			if (r.isOptionSet(ifThis)) {
				e.setCancelled(true);
				if (p != null && !message.isEmpty()) {
					p.sendMessage(message.replaceAll("<NAME>", r.getField(RegionField.name)));
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onCreeperSpawn(CreatureSpawnEvent e) {
		if (!e.getEntityType().equals(EntityType.CREEPER)) return;
		tryCancel(getAssocRegion(e.getLocation()), e, RegionOpt.creeper_protected, null, "");
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onCreeperExplode(EntityExplodeEvent e) {
		if (!e.getEntityType().equals(EntityType.CREEPER)) return;
		tryCancel(getAssocRegion(e.getLocation()), e, RegionOpt.creeper_protected, null, "");
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (e.getCause().equals(TeleportCause.CHORUS_FRUIT)) {
			// Check from
			tryCancel(getAssocRegion(e.getFrom()), e, RegionOpt.disable_chorus, null, "");
			// Check to
			tryCancel(getAssocRegion(e.getTo()), e, RegionOpt.disable_chorus, null, "");
		} else if (e.getCause().equals(TeleportCause.ENDER_PEARL)) {
			Region to = getAssocRegion(e.getTo());
			Region from = getAssocRegion(e.getFrom());
			
			if (to == null && from == null) {
				return; // No luck
			} else if (to == from && to != null) { // Teleport within
				tryCancel(to, e, RegionOpt.disable_pearl_within, e.getPlayer(), "");
			} else {
				// So they are not the same
				
				// Cancel teleport in if 'to' indicates disable_in
				tryCancel(to, e, RegionOpt.disable_pearl_in, null, "");
				
				// Cancel teleport out if 'from' indicates disable_out
				tryCancel(from, e, RegionOpt.disable_pearl_out, null, "");
			}
		}
	}
	
	@Override
	public boolean canWarp(Player p, WarpType type, MutableProperty<String> reason) {
		
		Region r = getAssocRegion(p.getLocation());
		if (r != null) {
			if (type == WarpType.HOME || type == WarpType.RETURN) {
				if (r.isOptionSet(disable_warp_out)) {
					reason.set(ChatColor.RED + "Warping out of " + r.getField(name) + " is prohibited.");
					return false;
				}
			} else if (type == WarpType.SETHOME || type == WarpType.SET) {
				if (r.isOptionSet(disable_warp_in)) {
					reason.set(ChatColor.RED + "Warping into " + r.getField(name) + " is prohibited.");
					return false;
				}
			}
		}
		
		return true;
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBed(PlayerInteractEvent e) {
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().equals(Material.BED_BLOCK)) {
			Region r = getAssocRegion(e.getClickedBlock().getLocation());
			
			tryCancel(r, e, RegionOpt.block_sleep, e.getPlayer(), 
					ChatColor.RED+"Sleeping is prohibited in this region.");
			
		}
	}
	
}
