package com.quirkygaming.commons.coordinate;

import java.io.Serializable;

import org.bukkit.Location;

public class Coord3D extends Coord2D implements Serializable {
	
	private static final long serialVersionUID = -6208654659250626515L;
	
	public int y;
	
	public Coord3D(Location l) {this(l.getBlockX(), l.getBlockY(), l.getBlockZ());}
	public Coord3D(int x, int y, int z) {super(x, z); this.y = y;}
	
	public Coord3D up() {return new Coord3D(x, y+1, z);}
	public Coord3D down() {return new Coord3D(x, y-1, z);}
	
	@Override
	public String toString() {
		return "[Coord3D: " + x + ", " + y + ", " + z + "]";
	}
}