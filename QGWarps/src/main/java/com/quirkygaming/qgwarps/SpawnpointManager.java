package com.quirkygaming.qgwarps;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface SpawnpointManager {
	
	// Extend and register this interface via the QGWarps class to warp a player to the spawnpoint (i.e. Lobby)
	public Location getSpawnpointLocation(Player p);
	
}
