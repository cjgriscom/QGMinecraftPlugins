package com.quirkygaming.qgcustoms.modules;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class PrankModule extends CustomModule {

	public static final String banningName = "n/a"; //Use to prank and ban a player on login.
	
	public PrankModule(QGCustomsPlugin plugin) {
		super("PrankAndBan", new Listener[]{new PrankListener()}, new String[]{}, plugin);
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		return false;
	}

	@Override
	public boolean onEnable() {
		return true;
	}

	@Override
	public boolean onDisable() {
		return true;
	}
	
	public static class PrankListener implements Listener {
		@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
		public void onPlayerJoin(PlayerJoinEvent event) {
			if (event.getPlayer().getName().equalsIgnoreCase(banningName)) {
				new PrankEvent("scaretodeath", plugin.getServer(), event.getPlayer(), plugin).runTaskLater(plugin, 1);
			}
		}

		@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
		public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
			
			if (event.getPlayer().getName().equalsIgnoreCase(banningName)) event.setMessage("HEELLLLPPP MEEEEEE!!!!");
		}
		
		@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
		public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
			if (event.getPlayer().getName().equalsIgnoreCase(banningName)) event.setMessage("/me is really scared!");
		}
		
	}
	
}

class PrankEvent extends BukkitRunnable {
	
	Player player;
	Server server;
	Plugin plugin;
	String prankName;
	
	public PrankEvent(String prankName, Server server, Player player, Plugin plugin) {
		this.player = player;
		this.server = server;
		this.plugin = plugin;
		this.prankName = prankName;
	}
	
	private void runCommand(String command) {
		server.dispatchCommand(server.getConsoleSender(), command);
	}
	
	private void haunt() {
		runCommand("hb-ai haunt " + player.getName());
	}
	
	private void zombiehorse() {
		Location l = player.getLocation();
		runCommand("summon EntityHorse " +
				l.getBlockX() +
				" " +
				(l.getBlockY()+2) +
				" " +
				l.getBlockZ() +
				" {Type:3,CustomName:+"+player.getName()+"+,CustomNameVisible:1}");
		runCommand("summon EntityHorse " +
				(l.getBlockX()-2) +
				" " +
				(l.getBlockY()+2) +
				" " +
				l.getBlockZ() +
				" {Type:4,CustomName:"+player.getName()+",CustomNameVisible:1}");
	}

	private void bury() {
		runCommand("hb-ai bury " + player.getName());
	}
	
	private void graveyard() {
		runCommand("hb-ai graveyard " + player.getName());
	}
	
	private void ban() {
		runCommand("ban " + player.getDisplayName());
	}
	
	private void executeLater(String prank, int interval) {
		new PrankEvent(prank, server, player, plugin).runTaskLater(plugin, interval);
	}

	@Override
	public void run() {
		if (prankName.equalsIgnoreCase("scaretodeath")) {
			player.getWorld().setTime(18000);
			zombiehorse();
			//executeLater("bury", 50);
			executeLater("haunt", 202);
			executeLater("graveyard", 400);
			executeLater("ban", 880);
			
		} else if (prankName.equalsIgnoreCase("ban")) {
			ban();
		} else if (prankName.equalsIgnoreCase("haunt")) {
			haunt();
		} else if (prankName.equalsIgnoreCase("bury")) {
			bury();
		} else if (prankName.equalsIgnoreCase("graveyard")) {
			graveyard();
		}
	}
	
	public void executePrank() {
		run();
	}
}

