package com.quirkygaming.commons;

import org.bukkit.Location;

import com.quirkygaming.reflectionlib.AnonymousClass;

public class Util {
	
	public static String plural(int quantity, String singular, String plural) {
		return (quantity==1 ? singular : plural);
	}
	
	public static String nth(int number) {
		int workingNumber = number;
		if (workingNumber < 0) workingNumber = -workingNumber;
		
		if (workingNumber >= 11 && workingNumber <= 13) return number + "th"; // 11-13 are exceptions
		switch (workingNumber % 10) {
			case 1: return number + "st";
			case 2: return number + "nd";
			case 3: return number + "rd";
		default: return number + "th";
		}
		
	}

	private static String bk_v;
	public static String getBukkitVersion() {
		if (bk_v == null) {
			String result = ""; // Empty string if no package versioning is used
			for (Package p : Package.getPackages()) {
				if (p.getName().startsWith("org.bukkit.craftbukkit.v")) {
					result = p.getName().replace("org.bukkit.craftbukkit.", "").replaceFirst("\\..*", "");
					break;
				}
			}
			System.out.println("[QGCommons] Bukkit package version: " + result);
			bk_v = result;
		}
		return bk_v;
	}
	
	private static final String nms = "net.minecraft.server.";
	public static String getNMSPackagePrefix() {
		String version = getBukkitVersion();
		if (!version.isEmpty()) version += ".";
		return nms + version;
	}
	public static AnonymousClass loadNMSClass(String classNameAfterVersion) {
		return AnonymousClass.get(getNMSPackagePrefix() + classNameAfterVersion);
	}
	
	private static final String cb = "org.bukkit.craftbukkit.";
	public static String getCraftBukkitPackagePrefix() {
		String version = getBukkitVersion();
		if (!version.isEmpty()) version += ".";
		return cb + version;
	}
	public static AnonymousClass loadCraftBukkitClass(String classNameAfterVersion) {
		return AnonymousClass.get(getCraftBukkitPackagePrefix() + classNameAfterVersion);
	}
	
	// The following is from BKCommonLib
	public static void loadChunks(Location location, final int radius) {
		loadChunks(location.getWorld(), location.getX(), location.getZ(), radius);
	}

	public static void loadChunks(org.bukkit.World world, double xmid, double zmid, final int radius) {
		loadChunks(world, toChunk(xmid), toChunk(zmid), radius);
	}

	public static void loadChunks(org.bukkit.World world, final int xmid, final int zmid, final int radius) {
		for (int cx = xmid - radius; cx <= xmid + radius; cx++) {
			for (int cz = zmid - radius; cz <= zmid + radius; cz++) {
				world.getChunkAt(cx, cz);
			}
		}
	}
	public static int toChunk(double loc) {
		return floor(loc / 16.0);
	}
	public static int floor(double value) {
		int i = (int) value;
		return value < (double) i ? i - 1 : i;
	}
	
	
}
