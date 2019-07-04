package com.quirkygaming.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.bukkit.plugin.Plugin;

public class SerialAPI {
	
	@SuppressWarnings("unchecked")
	public static <T> T loadSerializable(Plugin p, String path, T defaultIfMissing) {
		try {
			File location = new File(path);
			if (location.exists()) {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(location));
				Object obj = ois.readObject();
				ois.close();
				return (T) obj;
			} else {
				p.getLogger().info("File not found; creating " + path);
				saveSerializable(p, path, defaultIfMissing);
				return defaultIfMissing;
			}
			
		} catch (FileNotFoundException e) {
			p.getLogger().warning(e.getMessage());
			p.getLogger().warning("Caught FileNotFoundException while reading " + path);
		} catch (IOException e) {
			p.getLogger().warning(e.getMessage());
			p.getLogger().warning("Caught IOException while reading " + path);
		} catch (ClassNotFoundException e) {
			p.getLogger().warning(e.getMessage());
			p.getLogger().warning("Caught ClassNotFoundException while reading " + path);
			e.printStackTrace();
		}
		return null;
	}
	
	public static <T> boolean saveSerializable(Plugin p, String path, T object) {
		try {
			File location = new File(path);
			if (!location.exists()) {
				new File(location.getParent()).mkdirs();
			}
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(location));
			oos.writeObject(object);
			oos.close();
			return true;
		} catch (FileNotFoundException e) {
			p.getLogger().warning(e.getMessage());
			p.getLogger().warning("Could not write object; path invalid: " + path);
		} catch (IOException e) {
			p.getLogger().warning(e.getMessage());
			p.getLogger().warning("Caught IOException while writing " + path);
		}
		return false;
	}
	
}
