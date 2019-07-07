package com.quirkygaming.qgbooks.entries;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class Bulletin extends GroupMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public Bulletin(long ID, String body, UUID sender, TimeData timeData) {
		super(ID, body, sender, timeData, new HashSet<UUID>());
		
		for (OfflinePlayer p : Bukkit.getServer().getOfflinePlayers()) {
			super.recipientIDs.add(p.getUniqueId());
		}
	}
	
	public Bulletin(long ID, String body, UUID sender, TimeData timeData, Collection<UUID> recipients) {
		super(ID, body, sender, timeData, recipients);
	}

	@Override
	public String getPage(UUID bookOwner) {
		String page;
		
		page = "Message ID: " + ID() + lineFeed;
		page = page.concat("§o" + date() + ", " + time() + lineFeed);
		if (senderName().equals(bookOwner)) {
			page = page.concat("§r§l" + "Bulletin from you" + lineFeed);	
		} else {
			page = page.concat("§r§l" + "Bulletin from " + senderName() + lineFeed);
		}
		page = page.concat("§r" + body());
		
		return page;
	}
	
	@Override
	public boolean isBulletin() {
		return true;
	}

}
