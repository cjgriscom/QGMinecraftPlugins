package com.quirkygaming.commons.playerinterface;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@Deprecated 
public class GenericPlayer {
	
	public static boolean useConsoleColor = true;
	public static final HashMap<String, String> consoleColorMap = new HashMap<String,String>();
	
	Player player;
	public static final GenericPlayer CONSOLE = new GenericPlayer();
	
	static {
		consoleColorMap.put("§k", "[8m");  //Obfuscated
		consoleColorMap.put("§l", "[1m");  //Bold
		consoleColorMap.put("§m", "[9m");  //Strike
		consoleColorMap.put("§n", "[4m");  //Strike
		consoleColorMap.put("§o", "[3m");  //Italic
		consoleColorMap.put("§r", "[0m");  //Reset
		consoleColorMap.put("§0", "[30m"); //Black
		consoleColorMap.put("§1", "[34m"); //Dark Blue
		consoleColorMap.put("§2", "[32m"); //Dark Green
		consoleColorMap.put("§3", "[36m"); //Dark Aqua
		consoleColorMap.put("§4", "[31m"); //Dark Red
		consoleColorMap.put("§5", "[35m"); //Dark Purple
		consoleColorMap.put("§6", "[33m"); //Gold
		consoleColorMap.put("§7", "[37m"); //Grey
		consoleColorMap.put("§8", "[30;1m"); //Dark Gray
		consoleColorMap.put("§9", "[34;1m"); //Blue
		consoleColorMap.put("§a", "[32;1m"); //Green
		consoleColorMap.put("§b", "[36;1m"); //Aqua
		consoleColorMap.put("§c", "[31;1m"); //Red
		consoleColorMap.put("§d", "[35;1m"); //Light Purple
		consoleColorMap.put("§e", "[33;1m"); //Yellow
		consoleColorMap.put("§f", "[37;1m"); //White

	}
	
	public GenericPlayer(Player player) {
		this.player = player;
	}
	
	private GenericPlayer() {}
	
	public String getName() {
		if (player == null) {
			return "CONSOLE";
		} else {
			return player.getName();
		}
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void sendMessage(String message) {
		if (player == null) {
			System.out.println("[QGCommons] " + stripChatColor(message));
		} else {
			player.sendMessage(message);
		}
	}
	
	public void sendMessage(String[] messages) {
		if (player == null) {
			for (String message : messages) {
				System.out.println("[QGCommons] " + stripChatColor(message));
			}
		} else {
			player.sendMessage(messages);
		}
	}
	
	public void sendMessage(String[] messages, Plugin plugin) {
		if (player == null) {
			for (String message : messages) {
				System.out.println("[" + plugin.getName() + "] " + stripChatColor(message));
			}
		} else {
			player.sendMessage(messages);
		}
	}
	
	private String stripChatColor(String message) {
		
		for (String chatColor : consoleColorMap.keySet()) {
			message = message.replaceAll(chatColor, consoleColorMap.get(chatColor));
		}
		return message + consoleColorMap.get(ChatColor.RESET.toString());
	}

}
