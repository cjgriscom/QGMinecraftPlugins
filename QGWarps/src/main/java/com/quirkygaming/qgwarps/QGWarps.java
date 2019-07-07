package com.quirkygaming.qgwarps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

import com.quirkygaming.commons.CommonsAPI;
import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgwarps.WarpListener.WarpType;

public class QGWarps extends JavaPlugin implements Listener {
	
	public static boolean WARP_HORSE = true;
	
	private static double Y_ADD = 0.1; // Add a small value to Y to prevent falling thru floor
	
	HashSet<WarpListener> cancels = new HashSet<WarpListener>();
	HashSet<RegionHost> regioners = new HashSet<RegionHost>();
	HashMap<String, SpawnpointManager> customSpawns = new HashMap<String, SpawnpointManager>();
	
	WarpsCommandExecutor executor;
	
	Configuration c;
	
	String defaultSpawn = "bed";
	
	@Override
	public void onEnable() {
		c = Configuration.LoadConfiguration(this);
		
		if (c!=null) {
			executor = new WarpsCommandExecutor(this);
			
			getCommand("set").setExecutor(executor);
			getCommand("return").setExecutor(executor);
			getCommand("recall").setExecutor(executor);
			getCommand("sethome").setExecutor(executor);
			getCommand("home").setExecutor(executor);
			getCommand("warpssave").setExecutor(executor);
			getCommand("warpsreload").setExecutor(executor);
			getCommand("setspawn").setExecutor(executor);
			
		} else {
			this.getLogger().severe("Fatal configuration error. Disabling....");
			this.setEnabled(false);
		}
		
		getServer().getPluginManager().registerEvents(this, this);
		QGWarpsAPI.plugin = this;
	}
	
	@Override
	public void onDisable(){ 
		if (c!=null) c.save();
		this.getLogger().info("Disabled");
	}
	
	@Nullable String getRegion(Location l) {
		for (RegionHost h : regioners) {
			if (h.inRegion(l)) {
				return h.getRegion(l);
			}
		}
		return null;
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEndPortal(final PlayerPortalEvent e) {
		if (e.getCause() == TeleportCause.END_PORTAL && e.getTo().getWorld().getEnvironment() == Environment.NORMAL) {
			onGenericRespawn(e, (res)->{
				e.setTo(res);
			});
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onRespawn(final PlayerRespawnEvent e) {
		onGenericRespawn(e, (res)->{e.setRespawnLocation(res);});
	}
	
	public void onGenericRespawn(PlayerEvent e,
			Consumer<Location> setRespawnLocation) {
		
		Player p = e.getPlayer();
		String destination = defaultSpawn;
		if (c.spawns().containsKey(p.getUniqueId())) {
			destination = c.spawns().get(p.getUniqueId());
		} else {
			e.getPlayer().sendMessage(ChatColor.GOLD + "You do not have a spawnpoint. Use /setspawn to set it.");
			setSpawn(p, destination, false); // Set to default so no more messages
		}
		
		if (destination.equals("bed")) return;
		
		if (destination.equals("return")) {
			Location res = returnPlayer(p, false);
			if (res == null) {
				p.sendMessage(ChatColor.RED + "You do not have a temporary warp. Warping to bed instead.");
			} else {
				setRespawnLocation.accept(res);
			}
		} else if (destination.equals("home")) {
			Location res = returnPlayerHome(p, false, false);
			if (res == null) {
				p.sendMessage(ChatColor.RED + "You do not have a home (use /sethome). Warping to bed instead.");
			} else {
				setRespawnLocation.accept(res);
			}
		} else {
			// Search list
			for (String custom : customSpawns.keySet()) {
				if (custom.equalsIgnoreCase(destination)) {
					setRespawnLocation.accept(customSpawns.get(custom).getSpawnpointLocation(p));
					return;
				}
			}
			p.sendMessage(ChatColor.RED + "Spawnpoint '" + destination + "' does not exist! Please use /setspawn to reset.");
		}
	}
	
	private boolean canWarp(Player player, WarpType type) {
		MutableProperty<String> message = 
				MutableProperty.newProperty(ChatColor.RED + "Warping is prohibited in this circumstance.");
		for (WarpListener l : cancels) {
			if (!l.canWarp(player, type, message)) {
				player.sendMessage(message.get());
				return false;
			}
		}
		return true;
	}
	
	public void setTemporaryWarp(Player player, String name) {
		if (canWarp(player, WarpType.SET)) {
			Warp Warp = new Warp(name, getPlayerLocation(player));
			c.tempWarps().put(player.getUniqueId(), Warp);
			player.sendMessage("Set temporary warp with the name '" + name + "'. Use /recall to remind yourself of the name and coordinates.");
			c.saveTempWarps();
		}
	}
	
	public void setTemporaryWarp(Player player) {
		if (canWarp(player, WarpType.SET)) {
			Warp Warp = new Warp(getPlayerLocation(player));
			c.tempWarps().put(player.getUniqueId(), Warp);
			player.sendMessage("Set unnamed temporary warp. Use /recall to remind yourself of the location.");
			c.saveTempWarps();
		}
	}
	
	public Location returnPlayer(Player player, boolean doWarp) {
		if (canWarp(player, WarpType.RETURN)) {
			if (c.tempWarps().containsKey(player.getUniqueId())) {
				Warp returnPoint = c.tempWarps().get(player.getUniqueId());
				if (doWarp) warpPlayer(player,returnPoint);
				return returnPoint.getLocation().add(0, Y_ADD, 0);
			} else {
				player.sendMessage(ChatColor.RED + "No return point set!");
				return null;
			}
		}
		return null;
	}
	
	public void recallWarpName(Player player) {
		String respawn = defaultSpawn.toUpperCase();
		if (c.spawns().containsKey(player.getUniqueId())) {
			respawn = c.spawns().get(player.getUniqueId()).toUpperCase();
		}
		player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Respawn Point: " + ChatColor.WHITE +""+ ChatColor.RESET + respawn);
		
		if (c.tempWarps().containsKey(player.getUniqueId())) {
			Warp returnPoint = c.tempWarps().get(player.getUniqueId());
			player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Temporary Warp:");
			if (returnPoint.isNamed()) 
				player.sendMessage("  Name: " + returnPoint.getName());
			player.sendMessage("  Coords: " + returnPoint.world + "{" + (int)returnPoint.x + ", " + (int)returnPoint.y + ", " + (int)returnPoint.z + "}");
			String region = getRegion(returnPoint.getLocation());
			if (region != null) player.sendMessage("  Region: " + region);
		} else {
			player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "No temporary warp set.");
		}
		
		if (c.homes().containsKey(player.getUniqueId())) {
			Warp returnPoint = c.homes().get(player.getUniqueId());
			player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Home:");
			player.sendMessage("  Coords: " + returnPoint.world + "{" + (int)returnPoint.x + ", " + (int)returnPoint.y + ", " + (int)returnPoint.z + "}");
			String region = getRegion(returnPoint.getLocation());
			if (region != null) player.sendMessage("  Region: " + region);
		} else {
			player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "No home set.");
		}
	}
	
	public void setHome(Player player) {
		if (canWarp(player, WarpType.SETHOME)) {
			Warp Warp = new Warp(getPlayerLocation(player));
			c.homes().put(player.getUniqueId(), Warp);
			String[] messages = {"Home location set.",ChatColor.RED + "WARNING: Using /home will reset any warps created with /set!"};
			player.sendMessage(messages);
			c.saveHomes();
		}
	}
	
	public Location returnPlayerHome(Player player, boolean doWarp, boolean clearSet) {
		if (canWarp(player, WarpType.HOME)) {
			if (c.homes().containsKey(player.getUniqueId())) {
				Warp returnPoint = c.homes().get(player.getUniqueId());
				if (doWarp) warpPlayer(player,returnPoint);
				
				//Clear entry from c.tempWarps()
				if (clearSet) {
					c.tempWarps().remove(player.getUniqueId());
					c.saveTempWarps();
				}
				return returnPoint.getLocation();
			} else {
				player.sendMessage(ChatColor.RED + "No home set!");
				return null;
			}
		}
		return null;
	}
	
	class VehicleSlave implements Runnable {
		
		Player player;
		Entity vehicle;
		
		VehicleSlave(Player player, Entity vehicle) {	
			this.player = player;
			this.vehicle = vehicle;
		}
		
		@Override
		public void run() {
			vehicle.setPassenger(player);
		}
	}
	
	private void warpPlayer(Player player, Warp destination) {
		CommonsAPI.teleportWithMount(player, destination.getLocation().add(0, Y_ADD, 0));
		player.sendMessage("Warping...");
	}
	
	private Location getPlayerLocation(Player player) {
		return player.getLocation();
	}

	public boolean setSpawn(Player p, String destination, boolean messages) {
		if (destination.equals("home") || 
			destination.equals("return") || 
			destination.equals("bed") || 
			customSpawns.containsKey(destination)) {
			c.spawns().put(p.getUniqueId(), destination);
			if (messages) p.sendMessage(ChatColor.GOLD + "Your respawn destination is now '" + destination + "'");
			if (messages) p.sendMessage("Use /recall to check your spawnpoint.");
			c.saveSpawns();
			return true;
		} else {
			String customs = "";
			for (String custom : customSpawns.keySet()) {
				customs += custom + ", ";
			}
			p.sendMessage(ChatColor.RED + "Please enter one of the following destinations: "+customs+"home, return (temporary warp), or bed.");
			return false;
		}
		
	}
}
