package com.quirkygaming.qgcustoms.modules;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import com.quirkygaming.commons.scripting.NMSAccessScript;
import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class CooldownModule extends CustomModule {
	private static String cooldown_js = "function cancelCooldown(vehicle, entered, cooldownMessage) {\n" + 
										"	nmsVehicle = vehicle.getHandle();\n" + 
										"	currValue = nmsVehicle.portalCooldown;\n" + 
										"	if (currValue > 0) {\n" + 
										"		if (entered instanceof Bukkit.entity.Player) {\n" + 
										"			entered.sendMessage(cooldownMessage);\n" + 
										"		}\n" + 
										"		nmsVehicle.portalCooldown = 0;\n" + 
										"	}\n" + 
										"}";
	private static NMSAccessScript<Void> cancelCooldown;
	
	public CooldownModule(QGCustomsPlugin plugin) {
		super("HorseCooldown", new Listener[]{new CCListener()}, new String[]{}, plugin);
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		return false;
	}

	@Override
	public boolean onEnable() {
		cancelCooldown = NMSAccessScript.loadScript(cooldown_js, "cancelCooldown", 3);

		return true;
	}

	@Override
	public boolean onDisable() {
		return true;
	}
	
	public static class CCListener implements Listener {

		// Hack in a smaller cooldown
		@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
		public void onVehicleEnter(VehicleEnterEvent e) {
			cancelCooldown.run(
					e.getVehicle(),
					e.getEntered(),
					ChatColor.GOLD + "[QGCustoms]" + ChatColor.RED + " Canceled portal cooldown time for vehicle (you can now reenter).");
		}
	}
	
}
