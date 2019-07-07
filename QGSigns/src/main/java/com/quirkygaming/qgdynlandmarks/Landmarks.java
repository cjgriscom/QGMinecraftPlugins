package com.quirkygaming.qgdynlandmarks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import com.quirkygaming.qgsigns.QGSigns;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Landmarks {

	private final String ICON_ID_DEFAULT = "qglandmarks.default";
	private final String ICON_DEFAULT = "default.png";
	private final String ICON_ID_STATION = "qglandmarks.station";
	private final String ICON_STATION = "station.png";
	private final String ICON_ID_HOME = "qglandmarks.home";
	private final String ICON_HOME = "home.png";

	private final String MARKER_SET = "qglandmarks.landmarks";
	private final String LABEL = "Landmarks";
	
	private Plugin p;
	private LandmarkSignModule lModule;
	private boolean stop;
	private int task;

	private boolean enable;
	private int updateSeconds;
	private String label;
	private int layerPriority;
	private boolean hideByDefault;
	private int minZoom;

	private MarkerSet markerSet;
	private MarkerIcon iconDefault;
	private MarkerIcon iconStation;
	private MarkerIcon iconHome;
	private Map<String, Marker> markers = new HashMap<String, Marker>();

	public Landmarks(Plugin p, LandmarkSignModule module) {

		this.lModule = module;
		this.p=p;
		config();

		if (enable) {
			initMarkerSet();
			initIcons();

			// Initially update in 10 seconds
			scheduleNextUpdate(10);
		}
	}

	private void config() {
		enable = true;
		updateSeconds = 5;
		layerPriority = 1;
		hideByDefault = false;
		minZoom = 0;
		label = LABEL;
	}

	private void initMarkerSet() {

		markerSet = lModule.getMarkerApi().getMarkerSet(MARKER_SET);

		if (markerSet == null) {
			markerSet = lModule.getMarkerApi().createMarkerSet(MARKER_SET,
					label, null, false);
		} else {
			markerSet.setMarkerSetLabel(label);
		}

		if (markerSet == null) {
			p.getLogger()
					.severe("Error creating " + LABEL + " marker set");
			return;
		}

		markerSet.setLayerPriority(layerPriority);
		markerSet.setHideByDefault(hideByDefault);
		markerSet.setMinZoom(minZoom);
	}

	private void initIcons() {
		iconDefault = lModule.getMarkerApi().getMarkerIcon(ICON_ID_DEFAULT);
		iconStation = lModule.getMarkerApi().getMarkerIcon(ICON_ID_STATION);
		iconHome = lModule.getMarkerApi().getMarkerIcon(ICON_ID_HOME);
		
		if (iconDefault == null) {
			InputStream stream = QGSigns.class
					.getResourceAsStream("/images/" + ICON_DEFAULT);
			iconDefault = lModule.getMarkerApi().createMarkerIcon(
					ICON_ID_DEFAULT, ICON_ID_DEFAULT, stream);
		}
		
		if (iconStation == null) {
			InputStream stream = QGSigns.class
					.getResourceAsStream("/images/" + ICON_STATION);
			iconStation = lModule.getMarkerApi().createMarkerIcon(
					ICON_ID_STATION, ICON_ID_STATION, stream);
		}

		if (iconHome == null) {
			InputStream stream = QGSigns.class
					.getResourceAsStream("/images/" + ICON_HOME);
			iconHome = lModule.getMarkerApi().createMarkerIcon(
					ICON_ID_HOME, ICON_ID_HOME, stream);
		}

		if (iconDefault == null || iconStation == null || iconHome == null) {
			p.getLogger().severe("Error creating icons");
		}

	}

	private void scheduleNextUpdate(int seconds) {
		p.getServer().getScheduler().cancelTask(task);
		task = p.getServer().getScheduler().scheduleSyncDelayedTask(
				p, new Update(), seconds * 20);
	}

	private class Update implements Runnable {
		public void run() {
			if (!stop) {
				updateMarkerSet();
				scheduleNextUpdate(updateSeconds);
			}
		}
	}

	public void cleanup() {
		if (markerSet != null) {
			markerSet.deleteMarkerSet();
			markerSet = null;
		}
		markers.clear();
		stop = true;
	}

	private void updateMarkerSet() {
		Map<String, Marker> newMarkers = new HashMap<String, Marker>();

		for (World world : p.getServer().getWorlds()) {
			for (Landmark landmark : lModule.landmarks.get().values()) {

				Location loc = landmark.getLocation();
				String label = landmark.getTitle();
				String id = Landmarks.createID(loc);
				
				// one world at a time
				if (loc.getWorld() != world) {
					continue;
				}

				// pull out the markers from the old set to reuse them
				Marker m = markers.remove(id);
				
				// set icon
				MarkerIcon icon = iconDefault;
				if (landmark.getType().equalsIgnoreCase("station")) {
					icon = iconStation;
				}
				if (landmark.getType().equalsIgnoreCase("home")) {
					icon = iconHome;
				}
				
				if (m == null) {
					m = markerSet.createMarker(id, label, true,
							world.getName(), loc.getX(), loc.getY(),
							loc.getZ(), icon, false);
				} else {
					m.setLocation(world.getName(), loc.getX(), loc.getY(), loc
							.getZ());
					m.setLabel(label, true);
					m.setMarkerIcon(icon);
				}

				newMarkers.put(id, m);
			}
		}

		// delete all markers that we will no longer use

		for (Marker oldMarker : markers.values()) {
			oldMarker.deleteMarker();
		}

		// clean and replace the marker set

		markers.clear();
		markers = newMarkers;
	}
	
	public static String createID(Location loc) {
		return loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
	}
}