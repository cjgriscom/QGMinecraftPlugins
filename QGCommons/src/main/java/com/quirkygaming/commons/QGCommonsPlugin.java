package com.quirkygaming.commons;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.quirkygaming.commons.GreeterRegister.GreeterPriority;
import com.quirkygaming.commons.heavyloader.HeavyLoading;
import com.quirkygaming.propertydb.InitializationToken;
import com.quirkygaming.propertydb.PropertyDB;

public final class QGCommonsPlugin extends JavaPlugin implements Listener {
	

	private static final int PDB_SAVE_PERIOD = 30;
	public static Map<String, PromptDeclaration> waitingPrompts = new ConcurrentHashMap<String, PromptDeclaration>();
	public static HashMap<String, Long> logouts = new HashMap<String, Long>();
	static QGCommonsPlugin instance = null;
	private HeavyLoading heavy_loading;
	
	private static InitializationToken token;
	
	
	
	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(this, this);
		new InternalListener(this);
		token = PropertyDB.initializeDB(PDB_SAVE_PERIOD*1000);
		// Start HeavyLoader listening
		heavy_loading = new HeavyLoading();
		getServer().getPluginManager().registerEvents(heavy_loading, this);
	}
	
	@Override
	public void onDisable() {
		// Stop HeavyLoader listening
		heavy_loading.onPluginUnload();
		PropertyDB.closeDatabase(token);
		saveDefaultConfig();
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String label, final String[] args) {
		if (cmd.getName().equalsIgnoreCase("qgreload")) {
			return handleReload(sender, args);
		} else if (cmd.getName().equalsIgnoreCase("loadworld")) {
			if (args.length != 2) {
				return false;
			} else {
				new Thread(new Runnable() {public void run() {
				try {
					World w = Bukkit.getServer().createWorld(WorldCreator.name(args[0]).environment(Environment.valueOf(args[1].toUpperCase().trim())));
					sender.sendMessage(ChatColor.GREEN + w.getName() + " created.");
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Caught exception: " + e.getClass());
					sender.sendMessage(ChatColor.RED + "   " + e.getMessage());
				}
				}}).run();
			}
			
		} else if (cmd.getName().equalsIgnoreCase("tpworld")) {
			if (args.length != 5) {
				return false;
			} else {
				Player p = getServer().getPlayer(args[0]);
				World w = getServer().getWorld(args[4]);
				if (p == null) {
					sender.sendMessage("Player not found.");
					return true;
				}
				if (w == null) {
					sender.sendMessage("World not found.");
					return true;
				}
				p.teleport(new Location(w, 
						Double.parseDouble(args[1]),
						Double.parseDouble(args[2]),
						Double.parseDouble(args[3])));
				sender.sendMessage("Teleported.");
			}
		}
		return true;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		outputMessage(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		logouts.put(event.getPlayer().getDisplayName(), new Date().getTime());
	}

	private boolean handleReload(CommandSender sender, String[] args) {
		reloadConfig();
		sender.sendMessage("Configuration Reloaded!");
		return true;
	}

	@SuppressWarnings("unchecked")
	private void outputMessage(final Player player) {
		//long current = new Date().getTime();
		long lastLogin = player.getLastPlayed();
		Long lastLogoutObj = logouts.get(player.getDisplayName());
		long lastLogout = lastLogoutObj == null ? 0 : lastLogoutObj;
		long lastPlayed = lastLogout > lastLogin ? lastLogout : lastLogin;
		Date d = new Date(lastPlayed);
		
		/*if (current - lastPlayed > 5*60*1000) { // If player hasn't been on for 5 minutes
			class TitleDispatch implements Runnable {
				public void run() {
					getServer().dispatchCommand(getServer().getConsoleSender(),
							"title " + player.getDisplayName() + " times 20 40 20 ");
					getServer().dispatchCommand(
							getServer().getConsoleSender(),
							"title " 
									+ player.getDisplayName() 
									+ " title {text:\""
									+ getConfig().getString("splash")
									+ "\",color:gold,bold:true}");
				}
			}
			
			getServer().getScheduler().scheduleSyncDelayedTask(this, new TitleDispatch(), 12);
		}*/
		
		GreeterRegister.send(GreeterPriority.TOP, player);
		
		List<String> list = (List<String>) getConfig().getList("header"); // Header List
		for (int i = 0; list != null && i < list.size(); i++) {
			char[] character = new String("&").toCharArray();

			player.sendMessage(ChatColor.translateAlternateColorCodes(character[0],
					(String) list.get(i)));
		}
		
		//Handle mid-subscribers
		GreeterRegister.send(GreeterPriority.MIDDLE, player);
		
		list = (List<String>) getConfig().getList("footer"); // Switch Lists
		for (int i = 0; list != null && i < list.size(); i++) {
			String str = (String) list.get(i);
			str = str.replaceAll("LASTLOGIN", d.toString());
			
			char[] character = new String("&").toCharArray();

			player.sendMessage(ChatColor.translateAlternateColorCodes(character[0], str));
		}
		GreeterRegister.send(GreeterPriority.BOTTOM, player);
	}
	
}
