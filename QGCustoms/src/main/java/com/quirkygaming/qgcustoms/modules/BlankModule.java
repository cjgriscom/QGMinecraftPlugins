package com.quirkygaming.qgcustoms.modules;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class BlankModule extends CustomModule {

	public BlankModule(QGCustomsPlugin plugin) {
		super("Blank", new Listener[]{new ModuleListener()}, new String[]{}, plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args) {
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
	
	public static class ModuleListener implements Listener {

		//@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
		

	}
	
}
