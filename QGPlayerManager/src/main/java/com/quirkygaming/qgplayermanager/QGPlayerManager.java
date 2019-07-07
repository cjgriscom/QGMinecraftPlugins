package com.quirkygaming.qgplayermanager;

import java.util.Collection;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.quirkygaming.commons.PlayersToUpdate;
import com.quirkygaming.commons.SerialAPI;

public class QGPlayerManager extends JavaPlugin {
	
	static final String DEFAULT_REALNAME = "Unknown Name";
	private static final String DATAFILE = "plugins/QGPlayerManager/playerdatamap.obj";
	
	
	// TODO static MutableProperty<TreeMap<UUID, PlayerData>> playerData;
	
	static TreeMap<UUID, PlayerData> playerData_ = new TreeMap<UUID, PlayerData>();
	
	static TreeMap<Integer, Rank> rankID = new TreeMap<>();
	static TreeMap<String, Rank> rankName = new TreeMap<>();
	
	public enum Rank {
		MODERATOR("moderator", "", 1, true, false), // TODO add trial, spectator, n00b
		DEFAULT("default", "", 0, false, false),
		INMATE("inmate", "", 2, false, true),
		;
		
		public String description, title;
		public boolean moderator, inmate;
		public int id;
		
		private Rank(String title, String description, int id, boolean moderator, boolean inmate) {
			this.title = title; this.description = description; this.id = id;
			this.moderator = moderator; this.inmate = inmate;
			rankID.put(id, this);
			rankName.put(title, this);
		}
	}
	
	@Override
	public void onEnable() {
		
		playerData_ = SerialAPI.loadSerializable(this, DATAFILE, playerData_);
		
		if (playerData_ != null) {
			getLogger().info("Loaded " + playerData_.size() + " player datasets.");

			for (Entry<UUID,UUID> upd : PlayersToUpdate.list.entrySet()) {
				if (playerData_.containsKey(upd.getKey())) {
					playerData_.put(upd.getValue(), playerData_.remove(upd.getKey()));
				}
			}
			saveConf();
		} else {
			getLogger().severe("Could not load player datasets; disabling.");
			getPluginLoader().disablePlugin(this);
		}
		refresh();
		
		QGCommandExecutor executor = new QGCommandExecutor(this);
		getCommand("who").setExecutor(executor);
		getCommand("whois").setExecutor(executor);
		getCommand("playeredit").setExecutor(executor);
		getCommand("playerrefresh").setExecutor(executor);
		Rank.values(); // Initialize enum
	}
	
	@Override
	public void onDisable(){ 
		if (playerData_ != null && saveConf()) {
			getLogger().info("Wrote " + playerData_.size() + " player datasets.");
		}
	}
	
	public static Collection<PlayerData> getAllPlayers() {
		return playerData_.values();
	}
	
	void refresh() {
		for (OfflinePlayer p : getServer().getOfflinePlayers()) {
			if (!playerData_.containsKey(p.getUniqueId())) {
				playerData_.put(p.getUniqueId(), new PlayerData(p.getUniqueId(), DEFAULT_REALNAME));
			}
			PlayerData data = playerData_.get(p.getUniqueId());
			if (data.realName.get().equals(DEFAULT_REALNAME)) {
				getLogger().info(data.getName() + " does not have a registered name; please set it!");
			}
		}
		saveConf();
	}
	
	boolean saveConf() {
		return SerialAPI.saveSerializable(this, DATAFILE, playerData_);
	}
	
	public static boolean isModerator(CommandSender player) {
		return playerData_.get(((Player)player).getUniqueId()).moderator.get();
	}
	
	public static boolean isOpOrModerator(CommandSender player) {
		return player.isOp() || isModerator(player);
	}
	
	public static boolean isOpConsoleOrModerator(CommandSender player) {
		return player.isOp() || !(player instanceof Player) || isModerator(player);
	}
	
	public static String getOnlinePlayerInfo(Boolean useColors) {
		
		String playerNameString = "";
		
		String color1 = ChatColor.WHITE.toString();
		String color2 = ChatColor.YELLOW.toString();
		
		int i=0;
		for (Player p : Bukkit.getOnlinePlayers()) {
			i++;
			String currentColor = "";
			if (useColors) {
				if (i % 2 == 0) {
					currentColor = color1;
				} else {
					currentColor = color2;
				}
			}
			playerNameString = playerNameString.concat(" " + currentColor + p.getName());
		}
		
		if (useColors) {
			return ChatColor.GOLD + "Players Online:" + playerNameString;
		} else {
			return "Players Online:" + playerNameString;
		}
	}
	
	public static void sendOnlinePlayerInfo(Player player) {
		player.sendMessage(getOnlinePlayerInfo(true));
	}
	
	public void sendOnlinePlayerInfo() {
		getLogger().info(getOnlinePlayerInfo(false));
	}
	
}
