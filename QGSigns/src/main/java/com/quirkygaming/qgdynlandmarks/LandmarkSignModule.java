package com.quirkygaming.qgdynlandmarks;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerAPI;

import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.propertydb.PropertyDB;
import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgsigns.api.SignModule;

public class LandmarkSignModule extends SignModule implements CommandExecutor {
	
	private JavaPlugin p;
	private Plugin dynmap;
	private DynmapAPI dynmapAPI;
	private MarkerAPI markerAPI;
	
	MutableProperty<HashMap<String, Landmark>> landmarks;
	private Landmarks landmarkLayer;
	
	public LandmarkSignModule(JavaPlugin p) {
		super("landmark");
		this.p = p;
		
		p.getDataFolder().mkdir();
		
		landmarks = PropertyDB.initiateProperty(p.getDataFolder(), 
				"landmarks", 1, new HashMap<String, Landmark>(), ErrorHandler.logAll(p.getLogger(), true));

		p.getLogger().info("Loaded " + landmarks.get().size() + " landmarks");
		
		p.getCommand("qglandmarks").setExecutor(this);
		
		loadDynmap();
		
		landmarkLayer = new Landmarks(p, this);
	}
	
	public void onDisable() {}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args) {
		if (name.equalsIgnoreCase("qglandmarks")) {
			if (args.length < 1) return false;
			if (args[0].equalsIgnoreCase("list")) {
				TreeSet<String> nameAlpha = new TreeSet<>();
				for (String lname : landmarks.get().keySet()) {
					nameAlpha.add(lname);
				}
				for (String lname : nameAlpha) {
					sender.sendMessage(ChatColor.GOLD + lname); // List alphabetically
				}
				return true;
			} else if (args[0].equalsIgnoreCase("delete")) {
				if (args.length < 2) return false;
				String lname = args[1];
				
				if (!landmarks.get().containsKey(lname)) {
					sender.sendMessage(ChatColor.RED + "Landmark not found (case sensitive!!)");
				} else {
					landmarks.get().get(lname).getLocation().getBlock().breakNaturally();
					landmarks.get().remove(lname);
					landmarks.update();
				}
				return true;
			}
		}
		return false;
	}
	
	public void loadDynmap() {
		
		// Load Dynmap
		PluginManager pm = p.getServer().getPluginManager();
		dynmap = pm.getPlugin("dynmap");
		
		if (dynmap == null) {
			p.getLogger().severe("Cannot find dynmap!");
			return;
		} else {
			p.getLogger().info("Loaded Dynmap.");
		}
		
		dynmapAPI = (DynmapAPI) dynmap;
		
		markerAPI = dynmapAPI.getMarkerAPI();
		if (markerAPI == null) {
			p.getLogger().severe("Error loading Dynmap marker API!");
			return;
		} else {
			p.getLogger().info("Loaded Dynmap marker API.");
		}
	}
	
	public MarkerAPI getMarkerApi() {
		return markerAPI;
	}
	
	public HashMap<String, Landmark> getLandmarks_() {
		return landmarks.get();
	}
	
	public Landmarks getLayer() {
		return landmarkLayer;
	}
	

	@Override
	public void onSignCreation(List<String> lines, Location loc, Player player,
			Cancellable cancellable) {
		if (player.hasPermission("QuirkyGaming.landmarks")) {
			
			String type = "sign";
			if (lines.get(3).trim().equalsIgnoreCase("home")) {
				type = "home";
			} else if (lines.get(3).trim().equalsIgnoreCase("station")) {
				type = "station";
			}
			
			landmarks.get().put(Landmarks.createID(loc), new Landmark(type, lines.get(1).concat(lines.get(2)), loc));
			landmarks.update();
		} else {
			player.sendMessage(ChatColor.RED + "You do not have permission to place landmark signs.");
			cancellable.setCancelled(true);
			
		}
		
	}

	@Override
	public void onSignClick(List<String> lines, Location l, Player player) {}

	@Override
	public void onSignDestroy(List<String> lines, Location loc, Player player, Cancellable cancellable) {
		
		if (player.hasPermission("QuirkyGaming.landmarks")) {
			String ID = Landmarks.createID(loc);
			
			if (landmarks.get().containsKey(ID)) {
				landmarks.get().remove(ID);
				landmarks.update();
			}
		} else {
			player.sendMessage(ChatColor.RED + "You do not have permission to break landmark signs.");
			cancellable.setCancelled(true);
			
		}
		
	}

}
