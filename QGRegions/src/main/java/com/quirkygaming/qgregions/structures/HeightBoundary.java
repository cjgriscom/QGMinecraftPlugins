package com.quirkygaming.qgregions.structures;

import org.bukkit.Location;


public class HeightBoundary extends Boundary {
	
	private static final long serialVersionUID = 2350066038575906065L;
	
	int upper,lower;
	
	public HeightBoundary(int lower, int upper) {
		this.lower = lower; this.upper = upper;
	}
	
	@Override
	public boolean contains(Location l) {
		return l.getBlockY() >= lower && l.getBlockY() <= upper;
	}

	@Override
	public BoundaryType type() {
		return BoundaryType.HEIGHT;
	}

	@Override
	public String stringRep() {
		return "Y " + lower + " through " + upper;
	}
	
}
