package com.quirkygaming.qgcustoms.modules;

import static org.bukkit.DyeColor.BLUE;
import static org.bukkit.DyeColor.GREEN;
import static org.bukkit.DyeColor.ORANGE;
import static org.bukkit.DyeColor.RED;
import static org.bukkit.DyeColor.WHITE;
import static org.bukkit.DyeColor.YELLOW;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Wool;

import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;
import com.quirkygaming.qgheads.QGHeads;

public class CubeModule extends CustomModule {

	public static ItemStack cube;
	public static ItemStack cube_scrambled;
	
	public CubeModule(QGCustomsPlugin plugin) {
		super("Rubiks", new Listener[]{new CubeListener()}, new String[]{}, plugin);
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		return false;
	}

	@Override
	public boolean onEnable() {

		setCustomBlocks();
		return true;
	}

	@Override
	public boolean onDisable() {
		return true;
	}
	
	void setCustomBlocks() {
		/*Rubik's Cube*/ {
			cube = QGHeads.initCustomHead("§rRubik's Cube", "23cbe66f-5e4f-4a8f-bd00-0444059c9b57", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDRhOGVkZjNkMTk3NDJkZTlhY2Y0MDhmZjRkNjE3MjYxNjFjMzZkNDUzZjcyZWY3MjliZGQ5OTQzZDZhZiJ9fX0=");
			cube_scrambled = QGHeads.initCustomHead("§rRubik's Cube (scrambled)", "68f87a44-275f-4a5d-986a-6f41d405c72e", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWQ4NmU3YmQyOGMxNDZmNzE1MTRjNzgyY2FjMDU1ODYwZDFmMzcyYjRhOWJlM2ZlNjVjZmUxMTA0NzMzYmEifX19");
			
			ShapelessRecipe cubeRecipe = new ShapelessRecipe(cube);
			
			DyeColor[] colors = new DyeColor[]{WHITE, BLUE, RED, GREEN, ORANGE, YELLOW};
			for (DyeColor c : colors) cubeRecipe.addIngredient(new Wool(c));
			cubeRecipe.addIngredient(Material.OBSIDIAN);
			
			plugin.getServer().addRecipe(cubeRecipe);
			
		}
	}
	
	private static long cooldown = 0;
	public static class CubeListener implements Listener {
		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void onPlayerInteract(final PlayerInteractEvent event) {
			final Block block = event.getClickedBlock();
			if (block == null) {
				return;
			}
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				final Material mat = block.getType();
				if (mat == Material.SKULL) {
					String thisUUID = QGHeads.getUUIDFromPlayerHead(block);
							
					if (thisUUID != null) {
						boolean break_ = false;
						if (System.currentTimeMillis() - cooldown < 50) break_ = true;
						cooldown = System.currentTimeMillis();
						if (break_) return;
						if (thisUUID.equals(QGHeads.getUUIDFromPlayerHead(cube))) {
							event.setCancelled(true);
							QGHeads.replaceHead(block, cube_scrambled);
						} else if (thisUUID.equals(QGHeads.getUUIDFromPlayerHead(cube_scrambled))) {
							event.setCancelled(true);
							QGHeads.replaceHead(block, cube);
						} else if (QGHeads.localNames.containsKey(thisUUID)) {
							event.setCancelled(true);
							QGHeads.replaceHead(block, QGHeads.registeredHeads.get(thisUUID));
						}
					}
				}
			}
		}
	}
	
}
