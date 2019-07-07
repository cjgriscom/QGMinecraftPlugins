package com.quirkygaming.qgcustoms.modules;

import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class AutoloadModule extends CustomModule {
	
	MutableProperty<TreeMap<String, Environment>> worlds;
	
	public AutoloadModule(QGCustomsPlugin plugin) {
		super("Autoload", new Listener[]{new ModuleListener()}, new String[]{"autoload"}, plugin);
		worlds = QGCustomsPlugin.customsConf.getOrInitiateProperty("autoload", 1, new TreeMap<String, Environment>());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args) {
		if (name.equalsIgnoreCase("autoload")) {
			if (args.length != 2) return false;
			World w = Bukkit.getWorld(args[1]);
			if (w == null) {
				sender.sendMessage(ChatColor.RED + "World " + args[1] + " does not exist or is not loaded.");
			} else {
				if (args[0].equalsIgnoreCase("add")) {
					worlds.get().put(w.getName(), w.getEnvironment());
					worlds.update();
					sender.sendMessage(ChatColor.GREEN + "Added autoload: " + w.getName());
				} else if (args[0].equalsIgnoreCase("remove")) {
					worlds.get().remove(w.getName());
					worlds.update();
					sender.sendMessage(ChatColor.GREEN + "Removed autoload: " + w.getName());
				} else {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onEnable() {
		for (String world : worlds.get().keySet()) {
			plugin.getLogger().info("Autoloading world " + world);
			if (Bukkit.getWorld(world) == null) {
				Bukkit.getServer().createWorld(WorldCreator.name(world).environment(worlds.get().get(world)));
			} else {
				plugin.getLogger().info("Already loaded: " + world);
			}
		}
		return true;
	}

	@Override
	public boolean onDisable() {
		return true;
	}
	
	public static class ModuleListener implements Listener {

		//@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
		

	}
	
}
