package com.quirkygaming.qgplayermanager;

import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.quirkygaming.commons.playerinterface.PlayerInfo;
import com.quirkygaming.qgplayermanager.QGPlayerManager.Rank;

public class QGCommandExecutor implements CommandExecutor {
	 
	private QGPlayerManager plugin;
 
	public QGCommandExecutor(QGPlayerManager plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("who")) {
			
			// The /who command displays all players that are online.
			
			if (sender instanceof Player) {
				Player player = (Player) sender;
				QGPlayerManager.sendOnlinePlayerInfo(player);
				return true;
			} else {
				plugin.sendOnlinePlayerInfo();
				return true;
		    }
		} else if (cmd.getName().equalsIgnoreCase("whois")) {
			if (args.length != 1) return false;
			
			PlayerInfo usernameSearch = new PlayerInfo(args[0]);
			if (usernameSearch.lastLogin() == null) {
				sender.sendMessage(ChatColor.RED + "Could not find player: " + args[0]);
				return true;
			}
			
			PlayerData player = QGPlayerManager.playerData_.get(usernameSearch.uniqueID());
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "Could not find player: " + args[0] + "; run /playerrefresh");
				return true;
			}
			
			sender.sendMessage(ChatColor.GOLD + "--- " + ChatColor.WHITE + player.getName() + ChatColor.GOLD + " ---");
			sender.sendMessage(ChatColor.GOLD + "First Played: " + ChatColor.WHITE + new Date(player.getFirstPlayed()));
			sender.sendMessage(ChatColor.GOLD + "Last Played:  " + ChatColor.WHITE + new Date(player.getLastPlayed()));
			sender.sendMessage(ChatColor.GOLD + "UUID:         " + ChatColor.WHITE + player.player);
			sender.sendMessage(ChatColor.GOLD + "Real Name:    " + ChatColor.WHITE + player.realName);
			if (! (sender instanceof Player) || (QGPlayerManager.playerData_.get(((Player)sender).getUniqueId())).moderator.get()) {
				sender.sendMessage(ChatColor.GOLD + "Rank:         " + ChatColor.WHITE + QGPlayerManager.rankID.get(player.rankID.get()));
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("playeredit")) {
			if (args.length != 4) return false;
			PlayerInfo usernameSearch = new PlayerInfo(args[0]);
			if (usernameSearch.lastLogin() == null) {
				sender.sendMessage(ChatColor.RED + "Could not find player: " + args[0]);
				return true;
			}
			PlayerData player = QGPlayerManager.playerData_.get(usernameSearch.uniqueID());
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "Could not find player: " + args[0] + "; run /playerrefresh");
				return true;
			}
			
			player.m.set(player.realName, args[1] + " " + args[2]);
			
			Rank rank = QGPlayerManager.rankName.get(args[3].toLowerCase());
			if (rank == null) {
				sender.sendMessage("Invalid rank. Possible values:");
				for (Rank r : QGPlayerManager.rankName.values()) {
					sender.sendMessage("   " + r.title + ": " + r.description);
				}
				return true;
			}
			
			player.m.set(player.moderator, rank.moderator);
			player.m.set(player.inmate, rank.inmate);
			player.m.set(player.rankID, rank.id);
			
			sender.sendMessage(ChatColor.GOLD + "Player " + player.getName() + " updated.");
			plugin.saveConf();
			return true;
		} else if (cmd.getName().equalsIgnoreCase("playerrefresh")) {
			plugin.refresh();
			return true;
		}
		
		return false;
	}
}
