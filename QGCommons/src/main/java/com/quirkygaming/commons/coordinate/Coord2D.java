package com.quirkygaming.commons.coordinate;

import java.io.Serializable;

public class Coord2D implements Serializable, Comparable<Coord2D> {
	private static final long serialVersionUID = 5187310506858037520L;
	
	public int x, z;
	
	public Coord2D(int x, int z) {this.x = x; this.z = z;}
	
	public Coord2D west() {return new Coord2D(x-1, z);}
	public Coord2D east() {return new Coord2D(x+1, z);}
	public Coord2D north() {return new Coord2D(x, z-1);}
	public Coord2D south() {return new Coord2D(x, z+1);}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Coord2D) return this.compareTo((Coord2D)o) == 0;
		return false;
	}
	
	@Override
	public int compareTo(Coord2D o) {
		int res = Integer.compare(x, o.x);
		if (res != 0) return res;
		res = Integer.compare(z, o.z);
		if (this instanceof Coord3D && o instanceof Coord3D) {
			if (res != 0) return res;
			return Integer.compare(((Coord3D)this).y, ((Coord3D)o).y);
		} else {
			return res;
		}
	}
	
	@Override
	public String toString() {
		return "[Coord2D: " + x + ", " + z + "]";
	}
}