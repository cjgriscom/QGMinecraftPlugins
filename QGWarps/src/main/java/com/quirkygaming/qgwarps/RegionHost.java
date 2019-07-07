package com.quirkygaming.qgwarps;

import org.bukkit.Location;

public interface RegionHost {
	public boolean inRegion(Location l);
	public String  getRegion(Location l);
}
