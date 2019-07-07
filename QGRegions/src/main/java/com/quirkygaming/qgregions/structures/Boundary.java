package com.quirkygaming.qgregions.structures;

import java.io.Serializable;

import org.bukkit.Location;

public abstract class Boundary implements Serializable, Comparable<Boundary> {
	
	private static final long serialVersionUID = 1L;
	
	public abstract String stringRep();
	public abstract boolean contains(Location l);
	public abstract BoundaryType type();
	public void delete(Region parent) {}
	
	@Override
	public int compareTo(Boundary other) {
		return type().ordinal() - other.type().ordinal();
	}
	
	@Override
	public String toString() {
		return stringRep();
	}
}
