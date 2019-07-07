package com.quirkygaming.qgsigns;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.quirkygaming.qgdynlandmarks.LandmarkSignModule;
import com.quirkygaming.qgsigns.api.SignModule;
import com.quirkygaming.qgsigns.modules.spy.Disposal;
import com.quirkygaming.qgsigns.modules.spy.Spy;

public class QGSigns extends JavaPlugin {
	
	private static TreeMap<String, SignModule> modules = new TreeMap<String, SignModule>();
	SignsListener playerListener;
	
	LandmarkSignModule landmarks;
	
	private static QGSigns INSTANCE;
	
	public void onEnable() {
		INSTANCE = this;
		
		playerListener = new SignsListener(this);
		registerModule(new Disposal());
		registerModule(new Spy());
		//registerModule(new RegionSignModule(this));
		
		if (getServer().getPluginManager().isPluginEnabled("dynmap")) {
			getLogger().info("Loading DynLandmarks module");
			landmarks = new LandmarkSignModule(this);
			registerModule(landmarks);
		} else {
			getLogger().info("Could not load DynLandmarks module; Dynmap not found.");
		}
		
	}
	 
	public void onDisable(){ 
		if (landmarks != null) landmarks.onDisable(); // Save landmarks conf
		
		modules.clear();
	}
	
	public static void registerModule(SignModule module) {
		INSTANCE.getLogger().info("Registered SignModule " + module.ID());
		modules.put(module.ID(), module);
	}
	
	public static void unregisterModule(SignModule module) {
		INSTANCE.getLogger().info("Unregistered SignModule " + module.ID());
		modules.remove(module.ID());
	}
	
	public void doSignClickEvent(Block block, Player player, Cancellable event) {
		Sign sign = (Sign) block.getState();

		List<String> lines = cleanLines(sign.getLines());
		
		if (lines.size() >= 1) {
			boolean modified = false;
			
			for (SignModule m : modules.values()) {
				if (m.matchesHeader(lines.get(0))) {
					try {
						event.setCancelled(true);
						m.onSignClick(lines, block.getLocation(), player);
						modified = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			if (modified) {
				sign.setLine(0, "§1"+lines.get(0)); sign.setLine(1, lines.get(1)); sign.setLine(2, lines.get(2)); sign.setLine(3, lines.get(3));
				sign.update();
			}
		}
	}
	
	public boolean doSignCreateEvent(String[] lines_e, Block block, Player player, SignChangeEvent event) {
		List<String> lines = cleanLines(lines_e);
		//Return true will cancel Sign Change - intended
		
		if (lines.size() >= 1) {
			boolean modified = false;
			
			for (SignModule m : modules.values()) {
				
				if (m.matchesHeader(lines.get(0))) {
					try {
						m.onSignCreation(lines, block.getLocation(), player, event);
						modified = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (event.isCancelled()) { // Creation canceled
						modified = false;
						block.breakNaturally(); break;
					}
				}
			}
			
			if (modified) {
				Sign sign = (Sign) block.getState();
				sign.setLine(0, "§1"+lines.get(0)); sign.setLine(1, lines.get(1)); sign.setLine(2, lines.get(2)); sign.setLine(3, lines.get(3));
				sign.update();
				return true;
			}
		}
		
		return false;
	}
	
	public void doSignDestroyEvent(Block block, Player player, BlockBreakEvent event) {
		Sign sign = (Sign) block.getState();
		List<String> lines = cleanLines(sign.getLines());

		if (lines.size() >= 1) {
			boolean modified = false;
			
			for (SignModule m : modules.values()) {
				if (m.matchesHeader(lines.get(0))) {
					try {
						m.onSignDestroy(lines, block.getLocation(), player, event);
						if (event.isCancelled()) {modified = true; break;} // Destruction cancelled
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			if (modified) { // Only reaches here if destruction is cancelled
				sign.setLine(0, "§1"+lines.get(0)); sign.setLine(1, lines.get(1)); sign.setLine(2, lines.get(2)); sign.setLine(3, lines.get(3));
				sign.update();
			}
		}
	}
	
	public List<String> cleanLines(String[] lines) { // Strip color tokens
		String regex = "(§)[0-9,a-z,A-Z]";
		ArrayList<String> l = new ArrayList<String>();
		for (int i = 0; i < 4; i++) {
			l.add(lines[i].replaceAll(regex, ""));
		}
		return l;
	}
	
}
