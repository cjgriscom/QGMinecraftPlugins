package com.quirkygaming.qgwarps;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class Configuration_V4 extends Configuration {
	
	static final String HFILE = "plugins/QGWarps/homes.obj";
	static final String TWFILE = "plugins/QGWarps/tempwarps.obj";
	
	HashMap<String, Warp> homes;
	HashMap<String, Warp> tempWarps;
	
	protected Configuration_V4(boolean constructOnFailure) {
		super(constructOnFailure);
		homes = new HashMap<String, Warp>();
		tempWarps = new HashMap<String, Warp>();
		
		boolean save = !confExists();
		String defaultWorld = Bukkit.getWorlds().get(0).getName();
		
		homes = constructConf(HFILE, defaultWorld);
		tempWarps = constructConf(TWFILE, defaultWorld);
		
		if (save && constructOnFailure) {
			save();
		}
	}
	
	@SuppressWarnings("unchecked")
	private HashMap<String, Warp> constructConf(String fileName, String defaultWorld) {
		File file = new File(fileName);
		HashMap<String, Warp> map;
		
		if (file.exists()) {
			ObjectInputStream ois;
			try {
				ois = new ObjectInputStream(new FileInputStream(file));
				Object obj = ois.readObject();
				if (obj instanceof HashMap) {
					map = (HashMap<String, Warp>) obj;
					ois.close();
					return map;
				}
				ois.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			return new HashMap<String,Warp>();
		}
		
		throw new RuntimeException("Configuration error");
	}
	
	@Override
	protected boolean confExists() {
		return (homes.size()+tempWarps.size()) > 0;
	}
	
	@SuppressWarnings("deprecation")
	private HashMap<UUID, Warp> convert(HashMap<String, Warp> old) {
		HashMap<UUID, Warp> nw = new HashMap<UUID, Warp>();
		
		for (Entry<String, Warp> entry : old.entrySet()) {
			try {
				OfflinePlayer p = Bukkit.getServer().getOfflinePlayer(entry.getKey());
				nw.put(p.getUniqueId(), entry.getValue());
			} catch (Exception e) {
				System.out.println("[QGWarps] Error converting player " + entry.getKey() + ": ");
				e.printStackTrace();
			}
			
		}
		return nw;
	}
	
	@Override
	public HashMap<UUID, Warp> tempWarps() {
		return convert(tempWarps);
	}
	
	@Override
	public HashMap<UUID, Warp> homes() {
		return convert(homes);
	}
	
	@Override
	public HashMap<UUID, String> spawns() {
		return new HashMap<UUID, String>();
	}
	
	@Override
	public void saveHomes() {
		//saveV4Conf(HFILE, homes);
	}
	
	@Override
	public void saveTempWarps() {
		//saveV4Conf(TWFILE, tempWarps);
	}
	
	@Override
	public void saveSpawns() {}
	
	/*private void saveV4Conf(String file, HashMap<String, Warp> map) {
		try {
			ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(new File(file)));
			ois.writeObject(map);
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
}
