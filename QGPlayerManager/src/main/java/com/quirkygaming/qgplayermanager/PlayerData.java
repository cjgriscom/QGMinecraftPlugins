package com.quirkygaming.qgplayermanager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.quirkygaming.propertylib.Mutator;
import com.quirkygaming.propertylib.Property;
import com.quirkygaming.qgplayermanager.QGPlayerManager.Rank;

public class PlayerData implements Comparable<PlayerData>, Serializable {
	
	private static final long serialVersionUID = -2039676234949764781L;

	Mutator m = new Mutator();
	
	private transient OfflinePlayer o_player;
	
	public Property<UUID> player = Property.newProperty(m, null);
	public Property<String> realName = Property.newProperty(m, "");
	public Property<Boolean> moderator = Property.newProperty(m, false);
	public Property<Boolean> inmate = Property.newProperty(m, false);
	public Property<Integer> rankID = Property.newProperty(m, Rank.DEFAULT.id);
	
	public PlayerData(UUID player, String realName) {
		m.set(this.player, player);
		m.set(this.realName, realName);
	}
	
	// Called by the Serialization API when deserialized
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		if (player == null) player = Property.newProperty(m, null);
		if (realName == null) realName = Property.newProperty(m, "");
		if (moderator == null) moderator = Property.newProperty(m, false);
		if (inmate == null) inmate = Property.newProperty(m, false);
		if (rankID == null) rankID = Property.newProperty(m, Rank.DEFAULT.id);
	}
	
	public OfflinePlayer getPlayer() {
		if (o_player == null) {
			o_player = Bukkit.getOfflinePlayer(player.get());
		}
		return o_player;
	}
	
	public String getName() {
		return getPlayer().getName();
	}
	
	public boolean canConnect() {
		return getPlayer().isWhitelisted() && !getPlayer().isBanned();
	}
	
	public long getFirstPlayed() {
		return getPlayer().getFirstPlayed();
	}

	public long getLastPlayed() {
		return getPlayer().getLastPlayed();
	}
	
	@Override
	public int compareTo(PlayerData o) {
		return Long.compare(o.getLastPlayed(), this.getLastPlayed());
		//return Long.compare(this.getFirstPlayed(), o.getFirstPlayed());
	}
}
