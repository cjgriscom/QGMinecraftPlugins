package com.quirkygaming.qgwarps;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpsCommandExecutor implements CommandExecutor {
	 
	private QGWarps plugin;
 
	public WarpsCommandExecutor(QGWarps plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("set")){
			// The /set command will set a temporary warp point for a player.
			
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (args.length > 1) {
					sender.sendMessage("Too many arguments!");
					return false;
				} else if (args.length == 1) {
					String warpName = args[0];
					if (warpName.equalsIgnoreCase("home")) {
						sender.sendMessage("Use the /sethome command to set your home."); //Accidental /set home
					} else if (warpName.equalsIgnoreCase("spawn")) {
						sender.sendMessage("Use the /setspawn command to set your spawnpoint."); //Accidental /set spawn
					} else {
						plugin.setTemporaryWarp(player, warpName);
					}
					return true;
				} else if (args.length == 0) {
					plugin.setTemporaryWarp(player);
				}
				return true;
			} else {
				plugin.getLogger().info("This command cannot be executed by the console.");
				return false;
			}
		} else if (cmd.getName().equalsIgnoreCase("return")){
			// The /return command will return the player to the point where he last used the /set command.
			
			if (sender instanceof Player) {
				Player player = (Player) sender;
				plugin.returnPlayer(player, true);
				return true;
			} else {
				plugin.getLogger().info("This command cannot be executed by the console.");
				return false;
			}
		} else if (cmd.getName().equalsIgnoreCase("recall")){
			// The /recall command outputs the location and name of the point set with the /set command.
			
			if (sender instanceof Player) {
				Player player = (Player) sender;
				plugin.recallWarpName(player);
				return true;
			} else {
				plugin.getLogger().info("This command cannot be executed by the console.");
				return false;
			}
		} else if (cmd.getName().equalsIgnoreCase("sethome")){
			// The /sethome command will set a home point for a player.
			
			if (sender instanceof Player) {
				Player player = (Player) sender;
				plugin.setHome(player);
				return true;
			} else {
				plugin.getLogger().info("This command cannot be executed by the console.");
				return false;
			}
		} else if (cmd.getName().equalsIgnoreCase("home")){
			// The /home command will return the player to the point where he last used the /sethome command.
			
			if (sender instanceof Player) {
				Player player = (Player) sender;
				plugin.returnPlayerHome(player, true, true);
				return true;
			} else {
				plugin.getLogger().info("This command cannot be executed by the console.");
				return false;
			}
		} else if (cmd.getName().equalsIgnoreCase("setspawn")){
			// The /respawn allows a player to set his death spawnpoint.
			if (sender instanceof Player) {
				if (args.length > 1) {
					sender.sendMessage("Too many arguments!");
					return false;
				} else if (args.length == 1) {
					plugin.setSpawn((Player) sender, args[0].trim().toLowerCase(), true);
					return true;
				} else if (args.length == 0) {
					plugin.setSpawn((Player) sender, "no location specified", true); // TODO this could set up the selector
					return false;
				}
				return true;
			} else {
				plugin.getLogger().info("This command cannot be executed by the console.");
				return false;
			}
		} else if (cmd.getName().equalsIgnoreCase("warpsreload")){
			plugin.c = Configuration.LoadConfiguration(plugin);
			plugin.getServer().broadcastMessage("Warps Manually Reloaded");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("warpssave")){
			plugin.c.save();
			plugin.getServer().broadcastMessage("Warps Manually Saved");
			return true;
		}
		
		return false;
	}
}
