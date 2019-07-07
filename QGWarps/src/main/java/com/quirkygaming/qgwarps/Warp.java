package com.quirkygaming.qgwarps;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Warp implements Serializable {

	private static final long serialVersionUID = -7364033493677408646L;

	String name;

	double x, y, z;
	float pitch = 0, yaw = 0;

	String world;

	public Warp(String name, String world, double x, double y, double z, float yaw, float pitch) {
		this.name = name;
		this.world = world;
		this.x = x; this.y = y; this.z = z;
		this.pitch = pitch; this.yaw = yaw;
	}
	
	public Warp(Location loc) {
		name = "UNNAMED";
		decodeLocation(loc);
	}

	public Warp(String warpName, Location loc) {
		name = warpName;
		decodeLocation(loc);
	}
	
	public boolean isNamed() {
		return !name.equals("UNNAMED");
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	private boolean decodeLocation(Location loc) {
		x = loc.getX();
		y = loc.getY();
		z = loc.getZ();
		pitch = loc.getPitch();
		yaw = loc.getYaw();
		try {
			world = loc.getWorld().getName();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getName() {
		return name;
	}

	public Location getLocation() {
		World w = Bukkit.getWorld(world);
		if (w == null) {
			return null;
		}
		Location nativeLoc = new Location(w, x, y, z);
		if (pitch != 0) nativeLoc.setPitch(pitch);
		if (yaw != 0) nativeLoc.setYaw(yaw);
		return nativeLoc;
	}

}
