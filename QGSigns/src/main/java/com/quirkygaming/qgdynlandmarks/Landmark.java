package com.quirkygaming.qgdynlandmarks;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Landmark implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String type;
	private String title;
	private String world;
	private double x;
	private double y;
	private double z;
	
	Landmark(){}
	public Landmark(String type, String title, Location location) {
		this.type = type;
		this.title = title;
		this.world = location.getWorld().getName();
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();	}
	
	public String getType() {
		return type;
	}
	
	public String getTitle() {
		return title;
	}
	
	public Location getLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}
	
}
