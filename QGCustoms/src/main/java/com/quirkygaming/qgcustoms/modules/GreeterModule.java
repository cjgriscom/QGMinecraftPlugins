package com.quirkygaming.qgcustoms.modules;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.quirkygaming.commons.listeners.GreeterListener;
import com.quirkygaming.qgbooks.QGBooks;
import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;
import com.quirkygaming.qgplayermanager.QGPlayerManager;

public class GreeterModule extends CustomModule implements GreeterListener {
	
	private Set<String> knownPlayers = new HashSet<String>();
	
	public GreeterModule(QGCustomsPlugin plugin) {
		super("Greeter", new Listener[] {}, new String[] {}, plugin);
	}
	
	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		
		return false;
	}
	
	@Override
	public boolean onEnable() {
		com.quirkygaming.commons.CommonsAPI.subscribeToGreetingMessages(this);
		setKnownPlayers();
		return true;
	}
	
	@Override
	public boolean onDisable() {
		return true;
	}

	
	void addKnownPlayer(String player) {
		knownPlayers.add(player);
	}
	
	private void setKnownPlayers() {
		
		for (OfflinePlayer p : Bukkit.getServer().getOfflinePlayers()) {
			knownPlayers.add(p.getName());
		}
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			knownPlayers.add(p.getName());
		}
	}

	public void sendGreetingMessage(Player player) {
		QGPlayerManager.sendOnlinePlayerInfo(player);
	}

	
	public void processPossibleNewPlayer(Player player) {
		if (! knownPlayers.contains(player.getName())) {
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				if (! player.getName().equals(p.getName())) {
					p.sendMessage(ChatColor.BLUE + "Please welcome " + ChatColor.GOLD + player.getName() + ChatColor.BLUE + " to the server!");
				}
			}
			player.getInventory().addItem(QGBooks.INSTANCE.setupBookItem(new ItemStack(Material.WRITTEN_BOOK), player));
			player.sendMessage(ChatColor.BLUE + "Welcome! Please check your your inventory for your message book.");
			player.sendMessage(ChatColor.GOLD + "After reading the rules and commands, select a free region to enter the world.");
			knownPlayers.add(player.getName());
		}
	}
	
	@Override
	public void sendGreeting(Player p) {
		sendGreetingMessage(p);
		processPossibleNewPlayer(p);
	}
	
	@Override
	public String getName() {
		return "QGCustoms_Greeter";
	}
	
}
