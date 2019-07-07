package com.quirkygaming.qgcustoms;

import java.util.HashMap;

import org.bukkit.plugin.java.JavaPlugin;

import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.propertydb.sublayer.SubDB;


public class QGCustomsPlugin extends JavaPlugin {
	
	static HashMap<String, CustomModule> modules;
	
	public static SubDB<RuntimeException> customsConf;
	
	public void onEnable() {
		getDataFolder().mkdir();
		customsConf = new SubDB<>("customsDB", getDataFolder(), ErrorHandler.logAll(getLogger()));
		modules = CustomModule.loadModules(this);
		
		for (CustomModule m : modules.values()) {
			if (m.enabled()) getLogger().info(loadModule(m));
		}
	}
	
	String loadModule(CustomModule m) {
		if (m.loaded) return m.name() + " already loaded!";
		try {
			m.onEnable();
			m.enableEvents();
			m.loaded = true;
			return "Loaded " + m.name();
		} catch (Exception e) {
			e.printStackTrace();
			m.loaded = false;
			m.failed = true;
			return "Error loading " + m.name() + "!";
		}
	}
	
	String unloadModule(CustomModule m) {
		if (!m.loaded) return m.name() + " already unloaded!";
		try {
			m.disableEvents();
			m.onDisable();
			m.loaded = false;
			m.failed = false;
			return "Unloaded " + m.name();
		} catch (Exception e) {
			e.printStackTrace();
			m.loaded = true;
			m.failed = true;
			return "Error unloading " + m.name() + "!";
		}
	}
	
	public void onDisable(){ 
		for (CustomModule m : modules.values()) {
			if (m.loaded) getLogger().info(unloadModule(m));
		}
		CustomModule.unloadModules();
	}
	
	
}
