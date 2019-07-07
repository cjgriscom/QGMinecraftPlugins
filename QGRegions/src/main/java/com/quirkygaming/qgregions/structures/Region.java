package com.quirkygaming.qgregions.structures;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Location;

import com.quirkygaming.propertydb.sublayer.SubDB;
import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgregions.QGRegions;

public class Region implements Serializable {
	
	private static final long serialVersionUID = 1660259665707088951L;

	private HashMap<RegionField, String> fields = new HashMap<RegionField, String>();
	
	public HashSet<RegionOpt> options = new HashSet<RegionOpt>();
	
	public TreeMap<BoundaryType, Boundary> boundaries = new TreeMap<>();
	
	private transient MutableProperty<Region> thisRegionProperty;
	
	public static void loadFromIndex(SubDB<RuntimeException> db, QGRegions p, MutableProperty<TreeSet<String>> uuidIndex, HashMap<String, Region> regions) {
		for (String uuid : uuidIndex.get()) {
			MutableProperty<Region> region = db.initiateProperty(uuid, 1, null);
			if (region.get() == null) {
				p.getLogger().warning("Could not find region UUID " + uuid);
				uuidIndex.get().remove(uuid);
			} else {
				regions.put(region.get().getField(RegionField.name), region.get());
				region.get().initialize(region);
			}
		}
		p.getLogger().info("Loaded " + uuidIndex.get().size() + " regions.");
	}
	
	public Region(String name, String world, MutableProperty<TreeSet<String>> uuidIndex, HashMap<String, Region> regions) {
		String uuid = Long.toHexString(UUID.randomUUID().getLeastSignificantBits());
		thisRegionProperty = QGRegions.region_DB.initiateProperty(uuid, 1, null);
		
		setField(RegionField.name, name);
		setField(RegionField.short_name, name);
		setField(RegionField.UUID, uuid);
		boundaries.put(BoundaryType.WORLD, new WorldBoundary(world));
		
		uuidIndex.get().add(uuid);
		uuidIndex.update();
		
		regions.put(name, this);
		
		thisRegionProperty.set(this);
	}
	
	public void rename(String name, MutableProperty<TreeSet<String>> uuidIndex, HashMap<String, Region> regions) {
		regions.put(name, regions.remove(getField(RegionField.name)));
		this.setField(RegionField.name, name);
		thisRegionProperty.update();
	}
	
	public void setBoundary(Boundary b) {
		boundaries.put(b.type(), b);
		thisRegionProperty.update();
	}
	
	public void delBoundary(BoundaryType b) {
		boundaries.remove(b).delete(this);
		thisRegionProperty.update();
	}
	
	public void initialize(MutableProperty<Region> thisRegionProperty) {
		this.thisRegionProperty = thisRegionProperty;
	}
	
	public void delete(MutableProperty<TreeSet<String>> uuidIndex, HashMap<String, Region> regions) {
		for (Boundary b : boundaries.values()) b.delete(this);
		String uuid = getField(RegionField.UUID);
		uuidIndex.get().remove(uuid);
		uuidIndex.update();
		regions.remove(getField(RegionField.name));
		QGRegions.region_DB.deleteProperty(uuid);
	}
	
	public String getField(RegionField field) {
		if (fields.containsKey(field)) return fields.get(field);
		else return "";
	}
	
	public void setField(RegionField field, String value) {
		fields.put(field, value);
		thisRegionProperty.update();
	}
	
	public boolean isOptionSet(RegionOpt option) {
		return options.contains(option);
	}
	
	public void setOption(RegionOpt option, boolean state) {
		if (state) {
			options.add(option);
		} else {
			options.remove(option);
		}
		thisRegionProperty.update();
	}
	
	public boolean contains(Location l) {
		for (Boundary b : boundaries.values()) {
			if (!b.contains(l)) return false;
		}
		return true;
	}
	
	public String getWorld() {
		return boundaries.get(BoundaryType.WORLD).toString();
	}
}
