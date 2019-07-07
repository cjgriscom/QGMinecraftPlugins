package com.quirkygaming.qgbooks.config;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.quirkygaming.commons.PlayersToUpdate;
import com.quirkygaming.commons.playerinterface.PlayerInfo;
import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.propertydb.PropertyDB;
import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgbooks.QGBooks;
import com.quirkygaming.qgbooks.entries.CompatableEntry;

public class Database {
	
	public static UUID CONSOLE = UUID.fromString("f350bbbd-9643-435b-a029-681395b28692");
	
	private static MutableProperty<TreeMap<Long, CompatableEntry>> pageMap;
	private static MutableProperty<HashMap<UUID, Set<Long>>> pageUsers;
	
	public static void loadConfig(JavaPlugin p) {
		ErrorHandler<RuntimeException> erh = ErrorHandler.logAll(p.getLogger(), true);
		
		p.getDataFolder().mkdir();
		
		pageMap = PropertyDB.initiateProperty(p.getDataFolder(),
				"page_map", 1, new TreeMap<Long, CompatableEntry>(), erh);
		pageUsers = PropertyDB.initiateProperty(p.getDataFolder(),
				"page_users", 1, new HashMap<UUID, Set<Long>>(), erh);
		
		p.getLogger().info("Loaded " + pageMap.get().size() + " book entries.");
		
		for (Entry<UUID,UUID> upd : PlayersToUpdate.list.entrySet()) {
			if (pageUsers.get().containsKey(upd.getKey())) {
				pageUsers.get().put(upd.getValue(), pageUsers.get().remove(upd.getKey()));
				pageUsers.update();
			}
			
			for (CompatableEntry entry : pageMap.get().values()) {
				entry.replaceUUID(upd.getKey(), upd.getValue());
			}
			pageMap.update();
		}
	}
	
	public static UUID senderID(CommandSender p) {
		if (p instanceof Player) {
			return ((Player)p).getUniqueId();
		} else {
			return Database.CONSOLE;
		}
	}
	
	public static CompatableEntry getPageByID(long ID) {
		return pageMap.get().get(ID);
	}
	public static CompatableEntry getPageByID(String ID) {
		try {
			return pageMap.get().get(new Long(ID));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Set<Long> getPlayerPageIDs(UUID player) {
		
		Set<Long> s = pageUsers.get().get(player);
		if (s == null) {
			s = new TreeSet<Long>();
			pageUsers.get().put(player, s); // Generate if not found
			pageUsers.update();
		}
		return s;
		
	}

	public static long nextID() {
		if (pageMap.get().isEmpty()) return 1;
		else return pageMap.get().lastKey() + 1;
		
	}
	
	static void loadEntry(CompatableEntry e) {
		pageMap.get().put(e.ID(), e);
		pageMap.update();
	}
	
	public static void addEntry(CompatableEntry e) {
		loadEntry(e);
		// Add new users
		for (UUID p : e.senderAndRecipients()) { // For each affected player...
			if (p.equals(CONSOLE)) continue;
			addPageUser(e,p);
		}
	}
	
	public static void deleteEntry(CompatableEntry e) {
		pageMap.get().remove(e.ID());
		pageMap.update();
		// Look through sender and recipients and remove all pageUser references
		for (UUID p : e.senderAndRecipients()) { // For each affected player...
			if (p.equals(CONSOLE)) continue;
			removePageUser(e,p);
		}
	}
	
	public static void addPageUser(CompatableEntry e, UUID player) {
		modifyPageUser(e, player, false);
	}
	public static void removePageUser(CompatableEntry e, UUID player) {
		modifyPageUser(e, player, true);
	}
	
	private static void modifyPageUser(CompatableEntry e, UUID player, boolean remove) {
		Set<Long> l;
		if (pageUsers.get().containsKey(player)) {
			l=pageUsers.get().get(player);
		} else {
			l=new TreeSet<Long>();
		}
		if (remove) {l.remove(e.ID());} else {l.add(e.ID());}

		pageUsers.get().put(player, l);
		pageUsers.update();
		
	}
	
	public static String nameOf(UUID id) {
		if (id.equals(CONSOLE)) return "CONSOLE";
		return Bukkit.getServer().getOfflinePlayer(id).getName();
	}

	public static UUID lookupID(String player) {
		if (player.equals("carltonlieber")) player = "The_PoToTo_123";
		if (player.equalsIgnoreCase("CONSOLE")) {
			return Database.CONSOLE;
		} else {
			PlayerInfo p = new PlayerInfo(player);
			if (p.lastLogin() == null) {
				QGBooks.INSTANCE.getLogger().info("Did not recognize " + player);
				return null;
			} else {
				return p.uniqueID();
			}
		}
	}
}
