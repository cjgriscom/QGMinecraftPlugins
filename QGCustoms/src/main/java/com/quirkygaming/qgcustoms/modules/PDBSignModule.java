package com.quirkygaming.qgcustoms.modules;

import static com.quirkygaming.qgcustoms.QGCustomsPlugin.customsConf;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;

import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;
import com.quirkygaming.qgsigns.QGSigns;
import com.quirkygaming.qgsigns.api.SignModule;

public class PDBSignModule extends CustomModule {
	private PDBSignModule_Adaptor stats_adaptor;
	private PDBSignModule_Adaptor editor_adaptor;
	
	public PDBSignModule(QGCustomsPlugin plugin) {
		super("CustomDB_Editor", new Listener[]{}, new String[]{}, plugin);
		
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		return false;
	}

	@Override
	public boolean onEnable() {
		QGSigns.registerModule(stats_adaptor = new PDBSignModule_Adaptor(true));
		QGSigns.registerModule(editor_adaptor = new PDBSignModule_Adaptor(false));
		return true;
	}

	@Override
	public boolean onDisable() {
		QGSigns.unregisterModule(stats_adaptor);
		QGSigns.unregisterModule(editor_adaptor);
		return true;
	}
	
	static boolean exists(String property) {
		return customsConf.propertyExists(property);
	}
	
	static String getAsString(String property) {
		long version = customsConf.propertyVersion(property);
		MutableProperty<?> mp = customsConf.getOrInitiateProperty(property, version, null);
		Object value = mp.get();
		if (value instanceof Integer || value instanceof String || value instanceof Boolean) {
			return value.toString();
		} else {
			return "type error";
		}
	}
	
	static class PDBSignModule_Adaptor extends SignModule {
		
		private final static String STAT_ID = "stats";
		private final static String EDITOR_ID = "pdb";
		
		public PDBSignModule_Adaptor(boolean statsOnly) {
			super(statsOnly ? STAT_ID : EDITOR_ID);
		}

		@Override
		public void onSignCreation(List<String> lines, Location l, Player player, Cancellable cancellable) {
			if (this.ID().equals(EDITOR_ID) && lines.get(2).trim().equalsIgnoreCase("set")) {
				player.sendMessage("Not implemented (yet)");
				cancellable.setCancelled(true);
			} else {
				onSignClick(lines, l, player);
			}
		}

		@Override
		public void onSignClick(List<String> lines, Location l, Player player) {
			String property = lines.get(1);
			if (exists(property)) {
				lines.set(3, getAsString(property));
			} else {
				lines.set(3, "d.n.e.");
			}
			
		}

		@Override
		public void onSignDestroy(List<String> lines, Location l, Player player, Cancellable cancellable) {
			
		}
		
	}
}

