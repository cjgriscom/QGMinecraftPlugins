package com.quirkygaming.qgwarps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

import com.quirkygaming.commons.PlayersToUpdate;

public class Configuration_V5 extends Configuration {
	
	static final String HFILE = "plugins/QGWarps/homes_uuid.obj";
	static final String TWFILE = "plugins/QGWarps/tempwarps_uuid.obj";
	static final String SPFILE = "plugins/QGWarps/spawnpoints_uuid.obj";
	
	HashMap<UUID, Warp> homes;
	HashMap<UUID, Warp> tempWarps;
	HashMap<UUID, String> spawns;
	
	public static void main(String[] args) {
		HashMap<UUID, Warp> homes = constructConf("/home/chandler/minecraft/minecraft/"+HFILE, null);
		System.out.println(homes.size());
		Warp w = homes.get(UUID.fromString("64bfa4c3-ec9b-4334-a8d9-1cf5b750265a"));
		System.out.println(w.world);
		System.out.println(w.x);
		System.out.println(w.y);
		System.out.println(w.z);
	}
	
	protected Configuration_V5(boolean constructOnFailure) {
		super(constructOnFailure);
		homes = new HashMap<UUID, Warp>();
		tempWarps = new HashMap<UUID, Warp>();
		spawns = new HashMap<UUID, String>();
		
		homes = constructConf(HFILE, homes);
		tempWarps = constructConf(TWFILE, tempWarps);
		spawns = constructConf(SPFILE, spawns);
		
		boolean replaced = false;

		for (Entry<UUID,UUID> upd : PlayersToUpdate.list.entrySet()) {
			if (homes.containsKey(upd.getKey())) {
				homes.put(upd.getValue(), homes.remove(upd.getKey()));
				replaced = true;
			}
			if (tempWarps.containsKey(upd.getKey())) {
				tempWarps.put(upd.getValue(), tempWarps.remove(upd.getKey()));
				replaced = true;
			}
			if (spawns.containsKey(upd.getKey())) {
				spawns.put(upd.getValue(), spawns.remove(upd.getKey()));
				replaced = true;
			}
		}
		
		if ((!confExists() && constructOnFailure) || replaced) {
			save();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T constructConf(String fileName, T _default) {
		File file = new File(fileName);
		
		if (file.exists()) {
			ObjectInputStream ois;
			try {
				ois = new ObjectInputStream(new FileInputStream(file));
				Object obj = ois.readObject();
				if (obj instanceof HashMap) {
					ois.close();
					return (T) obj;
				}
				ois.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return _default;
		}
		
		throw new RuntimeException("Configuration error");
	}
	
	@Override
	protected boolean confExists() {
		return (homes.size()+tempWarps.size()+spawns.size()) > 0;
	}
	
	@Override
	public HashMap<UUID, Warp> tempWarps() {
		return tempWarps;
	}
	
	@Override
	public HashMap<UUID, Warp> homes() {
		return homes;
	}
	
	@Override
	public HashMap<UUID, String> spawns() {
		return spawns;
	}
	
	
	@Override
	public void saveHomes() {
		saveV5Conf(HFILE, homes);
	}
	
	@Override
	public void saveTempWarps() {
		saveV5Conf(TWFILE, tempWarps);
	}
	
	@Override
	public void saveSpawns() {
		saveV5Conf(SPFILE, spawns);
	}
	
	private void saveV5Conf(String file, Object map) {
		try {
			ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(new File(file)));
			ois.writeObject(map);
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
