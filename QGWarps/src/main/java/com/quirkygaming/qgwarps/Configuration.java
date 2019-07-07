package com.quirkygaming.qgwarps;

import java.util.HashMap;
import java.util.UUID;

public abstract class Configuration {
	
	protected static QGWarps plugin;
	
	public static Configuration LoadConfiguration(QGWarps plugin) {
		Configuration.plugin = plugin;
		
		Configuration conf;

		conf = new Configuration_V5(false);
		
		if (conf.confExists()) {
			plugin.getLogger().info("Loaded V5 Configuration");
			return conf;
		} else {
			Configuration_V4 conf4 = new Configuration_V4(false);
			if (conf4.confExists()) {
				plugin.getLogger().info("Converting V4 Configuration");
				conf.cloneFromOtherCopy(conf4);
				conf.save();
			} else {
				plugin.getLogger().info("Configuration blank or not found");
			}
		}
		
		if (!conf.confExists()) {
			plugin.getLogger().info("Generating Blank Configuration");
			conf.save();
		}
		
		return conf;
	}
	
	protected Configuration(boolean constructOnFailure) {
		
	}
	
	protected void cloneFromOtherCopy(Configuration otherCopy) {
		homes().clear();
		homes().putAll(otherCopy.homes());
		tempWarps().clear();
		tempWarps().putAll(otherCopy.tempWarps());
		spawns().clear();
		spawns().putAll(otherCopy.spawns());
	}

	protected abstract boolean confExists();
	
	public abstract HashMap<UUID, Warp> tempWarps();
	public abstract HashMap<UUID, Warp> homes();
	public abstract HashMap<UUID, String> spawns();
	
	public void save() {
		saveHomes();
		saveTempWarps();
		saveSpawns();
	}
	
	public abstract void saveHomes();
	public abstract void saveTempWarps();
	public abstract void saveSpawns();
	
}
