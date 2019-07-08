package com.quirkygaming.qgsigns;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.material.Sign;

public class SignsListener implements Listener {
	
	private QGSigns plugin;
	
	public static HashMap<String,Long> timeSinceLastClick = new HashMap<String,Long>();
	
	public SignsListener(QGSigns plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	
	/**@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.getPlayer() != null) {
			try {
				Block block = event.getPlayer().getTargetBlock(null, 100);
				
				if (block != null) {
					final Material mat = block.getType();
					if (mat == Material.SIGN_POST || mat == Material.WALL_SIGN) {
						plugin.doSignUpdateEvent(block, event.getPlayer());
					}
				}
			} catch (IllegalStateException e) {
				//There's a random bug (as of 1.4.7) in Bukkit with the iterator/getTargetBlock, so catch the exps.
			}
		}
	}**/
	
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		
		final Block block = event.getClickedBlock();
		if (block == null) {
			return;
		}
		
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			timeSinceLastClick.put(event.getPlayer().getName(), System.currentTimeMillis());
		} else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			final Material mat = block.getType();
			if (mat == Material.SIGN || mat == Material.WALL_SIGN) {
				plugin.doSignClickEvent(block, event.getPlayer(), event);
			}
		}
		
		
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockDestroy(BlockBreakEvent event) {
		Block block = event.getBlock();
		
		final Material mat = block.getType();
		if (mat == Material.SIGN || mat == Material.WALL_SIGN) {
			plugin.doSignDestroyEvent(block, event.getPlayer(), event);
		} else {
			BlockFace face;
			Block testBlock;
			for (int i = 1; i <= 5; i++) {
				if (!event.isCancelled()) {
					
					switch (i) {
						case 1: face = BlockFace.NORTH; break;
						case 2: face = BlockFace.SOUTH; break;
						case 3: face = BlockFace.UP;    break;
						case 4: face = BlockFace.EAST;  break;
						case 5: face = BlockFace.WEST;  break;
						default: face = BlockFace.UP;   break;
					}
					testBlock = block.getRelative(face);
					Material testMat = testBlock.getType();
					if (testMat == Material.SIGN || testMat == Material.WALL_SIGN) {
						Sign sign = (Sign) testBlock.getState().getData();
						if (testBlock.getRelative(sign.getAttachedFace()).getLocation().equals(block.getLocation())){
							plugin.doSignDestroyEvent(testBlock, event.getPlayer(), event);
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onSignCreation(SignChangeEvent event) {
		if (plugin.doSignCreateEvent(event.getLines(), event.getBlock(), event.getPlayer(), event)) {
			event.setCancelled(true);
		}
	}
	
}
