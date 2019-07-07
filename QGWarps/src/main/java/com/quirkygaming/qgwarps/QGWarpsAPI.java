package com.quirkygaming.qgwarps;

import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Location;

public class QGWarpsAPI {
	static QGWarps plugin;

	public static void registerWarpListener(WarpListener l) {
		plugin.cancels.add(l);
		plugin.getLogger().info("Registered Warp Listener " + l.getClass());
	}
	
	public static void registerRegionHost(RegionHost h) {
		plugin.regioners.add(h);
		plugin.getLogger().info("Registered Region Host " + h.getClass());
	}
	
	public static @Nullable String getRegion(Location loc) {
		return plugin.getRegion(loc);
	}
	
	public static void setDefaultRespawnLocation(String defaultSpawn) {
		plugin.defaultSpawn = defaultSpawn;
		plugin.getLogger().info("Default Spawn externally set to '" + defaultSpawn + "'.");
	}
	
	public static void addCustomSpawnpointManager(String name, SpawnpointManager m) {
		plugin.customSpawns.put(name.toLowerCase(), m);
		plugin.getLogger().info("Registered Spawnpoint Manager for '" + name + "': " + m.getClass());
	}
	
	public static boolean playerHasSetSpawn(UUID player) {
		return plugin.c.spawns().containsKey(player);
	}
	
}
