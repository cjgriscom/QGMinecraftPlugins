package com.quirkygaming.qgcustoms.modules;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class ZombieModule extends CustomModule {

	public ZombieModule(QGCustomsPlugin plugin) {
		super("BabyZombie", new Listener[]{new ZListener()}, new String[]{}, plugin);
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
	
	public static class ZListener implements Listener {

		// Fix baby zombies not dropping anything
		@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
		public void onZombieDeath(org.bukkit.event.entity.EntityDeathEvent event) {
			if (event.getEntityType().equals(EntityType.ZOMBIE)) {
				Zombie zombie = (Zombie)event.getEntity();
				if (zombie.isBaby()) {
					EntityEquipment inv = zombie.getEquipment();
					List<ItemStack> items = event.getDrops();
					
					if (Math.random() <= inv.getHelmetDropChance()) items.add(inv.getHelmet());
					if (Math.random() <= inv.getChestplateDropChance()) items.add(inv.getChestplate());
					if (Math.random() <= inv.getLeggingsDropChance()) items.add(inv.getLeggings());
					if (Math.random() <= inv.getBootsDropChance()) items.add(inv.getBoots());
					if (Math.random() <= inv.getItemInMainHandDropChance()) items.add(inv.getItemInMainHand());
				}
			}
		}

	}
	
}
