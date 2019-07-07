package com.quirkygaming.qgcustoms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.propertydb.PropertyDB;
import com.quirkygaming.propertylib.MutableProperty;

public class ManagerModule extends CustomModule {
	private static MutableProperty<HashSet<String>> enabledPlugins;
	
	public ManagerModule(QGCustomsPlugin plugin) {
		super("Manager", new Listener[]{}, new String[]{"qgcustoms"}, plugin);
		plugin.getDataFolder().mkdir();
		enabledPlugins = PropertyDB.initiateProperty(plugin.getDataFolder(),
				"ManagerModule_enabledPlugins", 1, new HashSet<String>(), ErrorHandler.logAll(plugin.getLogger(), true));
	}
	
	static Set<String> getEnabledModules() {
		return enabledPlugins.get();
	}
	
	public static boolean isEnabled(String mod) {return getEnabledModules().contains(mod);}

	@Override
	public boolean onEnable() {
		return true;
	}

	@Override
	public boolean onDisable() {
		return true;
	}
	
	public void setKnownModules(HashMap<String, CustomModule> modules) {
		if (enabledPlugins.get().size() == 0) {
			// Fresh
			enabledPlugins.get().add("Manager");
			enabledPlugins.get().addAll(modules.keySet());
			enabledPlugins.update();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args) {
		if (args.length < 1) return false;
		
		switch(args[0].toLowerCase()) {
			case "list": {
				String list = "";
				for (CustomModule m : QGCustomsPlugin.modules.values()) {
					if (!list.isEmpty()) list += ", ";
					if (!m.enabled()) list += ChatColor.STRIKETHROUGH;
					if (m.failed()) list += ChatColor.RED;
					else if (m.loaded()) list += ChatColor.GREEN;
					list += m.name();
					list += ChatColor.RESET + "" + ChatColor.WHITE;
					
				}
				sender.sendMessage(list);
				return true;
			}
			case "unload": {
				String modname = "";
				if (args.length == 2) modname = args[1];
				CustomModule m = QGCustomsPlugin.modules.get(modname);
				if (m == null || m == this) {
					sender.sendMessage(ChatColor.RED + "Please enter a valid module name");
					return true;
				}
				sender.sendMessage(ChatColor.GOLD + plugin.unloadModule(m));
				return true;
			}
			case "load": {
				String modname = "";
				if (args.length == 2) modname = args[1];
				CustomModule m = QGCustomsPlugin.modules.get(modname);
				if (m == null || m == this) {
					sender.sendMessage(ChatColor.RED + "Please enter a valid module name");
					return true;
				}
				sender.sendMessage(ChatColor.GOLD + plugin.loadModule(m));
				return true;
			}
			case "enable": {
				String modname = "";
				if (args.length == 2) modname = args[1];
				CustomModule m = QGCustomsPlugin.modules.get(modname);
				if (m == null || m == this) {
					sender.sendMessage(ChatColor.RED + "Please enter a valid module name");
					return true;
				}
				enabledPlugins.get().add(modname);
				enabledPlugins.update();
				sender.sendMessage(ChatColor.GOLD + "Enabled " + modname);
				return true;
			}
			case "disable": {
				String modname = "";
				if (args.length == 2) modname = args[1];
				CustomModule m = QGCustomsPlugin.modules.get(modname);
				if (m == null || m == this) {
					sender.sendMessage(ChatColor.RED + "Please enter a valid module name");
					return true;
				}
				enabledPlugins.get().remove(modname);
				enabledPlugins.update();
				sender.sendMessage(ChatColor.GOLD + "Disabled " + modname);
				return true;
			}
			default: return false;
		}
		
	}
}
