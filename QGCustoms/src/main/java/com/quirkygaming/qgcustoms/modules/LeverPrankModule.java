package com.quirkygaming.qgcustoms.modules;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Lever;

import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class LeverPrankModule extends CustomModule {
	
	static String PLAYER = "sup3";
	
	static int[] flip1 = new int[]{10};
	static int[] flip2 = new int[]{10,20};
	static int[] flip3 = new int[]{15,17,19,21,23,25};
	static int[] flip4 = new int[]{60};
	static int[] flip5 = new int[]{};
	static int[] flip6 = new int[]{};
	static int[] flip7 = new int[]{};
	
	static ArrayList<int[]> flips = new ArrayList<int[]>();
	static {
		flips.add(flip1); flips.add(flip2); flips.add(flip3); flips.add(flip4); flips.add(flip5); flips.add(flip6); flips.add(flip7);
	}
	
	static int stage = 0;

	public LeverPrankModule(QGCustomsPlugin plugin) {
		super("LeverPrank", new Listener[]{new ModuleListener()}, new String[]{}, plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args) {
		return false;
	}

	@Override
	public boolean onEnable() {
		stage = 0;
		return true;
	}

	@Override
	public boolean onDisable() {
		return true;
	}
	
	public static class ModuleListener implements Listener {

		@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
		public void onPlayerInteract(PlayerInteractEvent e) {
			if (e.getPlayer().getName().toLowerCase().startsWith(PLAYER.toLowerCase()) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Block b = e.getClickedBlock();
				if (b.getType() == Material.LEVER) {
					int[] flipList = null;
					if (stage >= flips.size()) {
						flipList = flips.get((int) (Math.random() * flips.size()));
					} else {
						flipList = flips.get(stage++);
					}
					for (int i : flipList) sendLeverChange(b, e, i);
				}
			}
		}

		private void sendLeverChange(Block b, PlayerInteractEvent e, int delay) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
				b.getWorld().playSound(b.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
				Lever l = (Lever) b.getState().getData();
				l.setPowered(!l.isPowered());
				b.getState().setData(l);
				b.getState().update();
				b.setData(l.getData(), true);
			}, delay);
		}
	}
	
}
