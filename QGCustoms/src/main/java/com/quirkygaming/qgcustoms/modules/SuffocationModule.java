package com.quirkygaming.qgcustoms.modules;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.quirkygaming.commons.Util;
import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class SuffocationModule extends CustomModule {

	public SuffocationModule(QGCustomsPlugin plugin) {
		super("HorseSuffocation", new Listener[]{new HListener()}, new String[]{}, plugin);
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
	
	public static class HListener implements Listener {

		private static final long horseWarningInterval = 5000;
		
		private HashMap<Integer, Long> timeSinceLastHorseWarning = new HashMap<Integer, Long>();
		private HashMap<Integer, Integer> warningSums = new HashMap<Integer, Integer>();
		
		private boolean doHorseWarnings = true;
		
		//Cancel horse suffocation
		@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
		public void onSuffocateDamage(EntityDamageEvent event) {
			if (event.getCause().equals(DamageCause.SUFFOCATION) && doHorseWarnings) {
				if (event.getEntityType().equals(EntityType.HORSE)) {
					final LivingEntity horse = (LivingEntity) event.getEntity();
					final double health = horse.getHealth();
					
					//Instead of canceling just fix the damage to get sound and correct intervals and stuff
					plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							horse.setHealth(health);
						}
					}, 1);
					
					ArrayList<Player> players = new ArrayList<Player>();
					for (Entity entity : horse.getNearbyEntities(75, 1000, 75)) {
						if (entity.getType().equals(EntityType.PLAYER)) {
							players.add((Player) entity);
						}
					}
					sendHorseWarning(players, horse);
				} 
			}
		}
		
		private void sendHorseWarning(ArrayList<Player> players, LivingEntity horse) {
			//Send a message to nearby players now and then
			int horseID = horse.getEntityId();
			
			Location loc = horse.getLocation();
			
			Long currentTime = System.currentTimeMillis();
			
			Long timeSincePrev = timeSinceLastHorseWarning.get(horseID);
			if (timeSincePrev == null) timeSincePrev = 0 - horseWarningInterval;
			
			Integer warningsSincePrev = warningSums.get(horseID);
			
			if (warningsSincePrev == null) {
				warningsSincePrev = 0;
			}
			
			warningsSincePrev++;
			warningSums.put(horseID, warningsSincePrev);
			
			if (currentTime - timeSincePrev >= horseWarningInterval) {
				for (Player player : players) {
					
					player.sendMessage(ChatColor.GOLD +
							"[QGCustoms] " +
							ChatColor.RED.toString() +
							warningsSincePrev + 
							" horse suffocation " + 
							Util.plural(warningsSincePrev, "warning", "warnings") + 
							" at X" + loc.getBlockX() + 
							" Y" + loc.getBlockY() + 
							" Z" + loc.getBlockZ() +
							".");
				}
				timeSinceLastHorseWarning.put(horseID, currentTime);
				warningSums.put(horseID, 0);
			}
		}

	}
	
}


