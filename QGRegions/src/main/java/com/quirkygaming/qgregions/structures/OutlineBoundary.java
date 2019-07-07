package com.quirkygaming.qgregions.structures;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

import org.bukkit.Location;

import com.quirkygaming.commons.coordinate.Coord2D;
import com.quirkygaming.commons.coordinate.Coord3D;
import com.quirkygaming.commons.heavyloader.HeavyLoader;
import com.quirkygaming.commons.heavyloader.HeavyLoading;
import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgregions.QGRegions;

public class OutlineBoundary extends Boundary implements HeavyLoader {
	
	private static final boolean LOAD_IMMEDIATELY = false;
	
	private static final long serialVersionUID = -7170853789290904964L;
	
	private Coord2D origin;
	private int minZ, maxZ, minX, maxX;
	private transient TreeSet<Coord2D> fill_runtime = new TreeSet<Coord2D>();
	private String uuid;
	
	private transient MutableProperty<TreeSet<Coord2D>> bounds;
	
	// Must run floodFill() after construction to validate bounds and origin
	public OutlineBoundary(Coord2D origin, TreeSet<Coord2D> boundarySet, Region parent) {
		uuid = parent.getField(RegionField.UUID);
		
		delete(parent); // Delete if already exists
		bounds = QGRegions.region_DB.initiateProperty(boundsProperty(uuid), 1, boundarySet);
		
		this.origin = origin;
		
		populateBoundaries();
	}
	
	// Called by the Serialization API upon deserializaton
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		if (LOAD_IMMEDIATELY) {
			doAsyncLoading();
		} else {
			HeavyLoading.hook(this);
		}
	}

	@Override
	public void doAsyncLoading() {
		if (fill_runtime != null && !fill_runtime.isEmpty()) return;
		if (bounds == null) bounds = QGRegions.region_DB.getOrInitiateProperty(boundsProperty(uuid), 1, null);
		fill_runtime = new TreeSet<Coord2D>();
		floodFill();
	}
	
	@Override 
	public void delete(Region parent) {
		String prop = boundsProperty(parent.getField(RegionField.UUID));
		if (QGRegions.region_DB.propertyExists(prop)) {
			QGRegions.region_DB.deleteProperty(prop);
		}
	}
	
	private String boundsProperty(String uuid) {
		return uuid + "_outlinebounds";
	}
	
	@Override
	public boolean contains(Location coord) {
		if (coord.getBlockX() >= minX && coord.getBlockX() <= maxX && 
				coord.getBlockZ() >= minZ && coord.getBlockZ() <= maxZ) {
			
			HeavyLoading.forceAndWaitForLoad(this);
			
			return fill_runtime.contains(new Coord3D(coord));
		}
		return false;
	}

	private void populateBoundaries() {
		minX = minZ = Integer.MAX_VALUE;
		maxX = maxZ = Integer.MIN_VALUE;
		for (Coord2D testCoord : bounds.get()) {
			if (testCoord.x < minX) minX = testCoord.x;
			if (testCoord.x > maxX) maxX = testCoord.x;
			if (testCoord.z < minZ) minZ = testCoord.z;
			if (testCoord.z > maxZ) maxZ = testCoord.z;
		}
	
	}
	
	// Returns false on failure
	public boolean floodFill() {
		Queue<Coord2D> q = new LinkedList<Coord2D>();
		if (bounds.get().contains(origin)) {
			System.out.println("[QGRegions] Flood fill failed; bounds contains origin: " + origin);
			return false; // On top of boundary
		}
		q.add(origin);
		fill_runtime.clear();
		fill_runtime.addAll(bounds.get());
		while (!q.isEmpty()) {
			Coord2D w = q.remove();
			Coord2D e = w;
			while (!fill_runtime.contains(w.west())) {
				w = w.west();
				if (e.x - w.x > bounds.get().size()) {
					System.out.println("[QGRegions] Flood fill failed; out of bounds 1; origin: " + origin);
					return false; // Outside of boundaries
				}
			}
			while (!fill_runtime.contains(e)) {
				e = e.east();
				if (e.x - w.x > bounds.get().size()) {
					System.out.println("[QGRegions] Flood fill failed; out of bounds 2; origin: " + origin);
					return false; // Outside of boundaries
				}
			}
			while (!w.equals(e)) {
				fill_runtime.add(w);
				if (!fill_runtime.contains(w.north())) q.add(w.north());
				if (!fill_runtime.contains(w.south())) q.add(w.south());
				w = w.east();
			}
			
		}
		
		return true;
	}

	@Override
	public BoundaryType type() {
		return BoundaryType.OUTLINE;
	}

	@Override
	public String stringRep() {
		String boundExp = "unloaded";
		String fill = "unloaded";
		if (bounds != null) boundExp = bounds.get().size() + "";
		if (fill_runtime != null) fill = fill_runtime.size() + "";
		return boundExp + " block boundary, " + fill + " block fill";
	}
}
