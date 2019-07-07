package com.quirkygaming.qgcustoms.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;
import com.quirkygaming.qgwarps.QGWarpsAPI;
import com.quirkygaming.qgwarps.WarpListener;

public class OldEndModule extends CustomModule implements WarpListener {
	
	public OldEndModule(QGCustomsPlugin plugin) {
		super("OldEnd", new Listener[]{}, new String[]{}, plugin);
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		return false;
	}

	@Override
	public boolean onEnable() {
		QGWarpsAPI.registerWarpListener(this);
		
		Bukkit.getServer().createWorld(WorldCreator.name("oldend").environment(Environment.THE_END));
		return true;
	}

	@Override
	public boolean onDisable() {
		return true;
	}

	@Override
	public boolean canWarp(Player p, WarpType type, MutableProperty<String> reason) {
		if (p.getWorld().getName().equalsIgnoreCase("oldend") && (type == WarpType.SET || type == WarpType.SETHOME)) {
			reason.set(ChatColor.RED + "Setting warps in the old end is prohibited.");
			return false;
		}
		return true;
	}

}
