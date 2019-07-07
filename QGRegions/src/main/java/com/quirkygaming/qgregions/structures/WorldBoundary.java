package com.quirkygaming.qgregions.structures;

import org.bukkit.Location;


public class WorldBoundary extends Boundary {
	
	private static final long serialVersionUID = -4795667758504740822L;
	
	private String world;
	
	public WorldBoundary(String world) {this.world = world;}
	
	public String worldName() {return world;};

	@Override
	public boolean contains(Location l) {
		return l.getWorld().getName().equalsIgnoreCase(world);
	}

	@Override
	public BoundaryType type() {
		return BoundaryType.WORLD;
	}

	@Override
	public String stringRep() {
		return worldName();
	}
	
}
