package com.quirkygaming.qgcustoms.modules;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.Lightable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;
import com.quirkygaming.qgcustoms.modules.externalization.JukeEvent;

public class JukeboxModule extends CustomModule {

	public JukeboxModule(QGCustomsPlugin plugin) {
		super("Jukebox", new Listener[]{new JListener()}, new String[]{}, plugin);
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
	
	public static class JListener implements Listener {

		//Code adapted from original code by carapace7940 on MinecraftForums

		private final BlockFace adjFaces[] = { BlockFace.NORTH, BlockFace.EAST,
				BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP,
				BlockFace.DOWN };
		private final BlockFace nsewFaces[] = { BlockFace.NORTH, BlockFace.EAST,
				BlockFace.SOUTH, BlockFace.WEST };

		private HashSet<Block> blocksWaitingForPhysicsEvent = new HashSet<Block>();
		
		
		@EventHandler(priority = EventPriority.LOWEST)
		public void onRedstoneTorchCreation(BlockPlaceEvent e) { 
			//Should trigger when a redstone torch is placed below a jukebox
			
			Block block = e.getBlock();
			
			if ((block.getType() == Material.REDSTONE_TORCH || block.getType() == Material.REDSTONE_WALL_TORCH) 
					&& ((Lightable)block.getBlockData()).isLit()) {
				
				Block adjBlock = block.getRelative(BlockFace.UP);
				if (adjBlock.getType() == Material.JUKEBOX) {
					if (adjBlock.isBlockPowered()) {
						//For some reason the block gets powered before the event executes...
						doPowerup(adjBlock); 
					}
				}
			}
		}
		

		@EventHandler(priority = EventPriority.LOWEST)
		public void onBlockRedstone(BlockRedstoneEvent e) {
			Block triggeringBlock = e.getBlock();

			if (e.getOldCurrent() == 0 && e.getNewCurrent() > 0) {

				for (BlockFace bf : adjFaces) {
					Block adjBlock = triggeringBlock.getRelative(bf);
					if (adjBlock.getType() == Material.JUKEBOX) {
						if (adjBlock.isBlockPowered())
							break; // its *already* powered, so it can't be
									// powered-on!
						blocksWaitingForPhysicsEvent.add(adjBlock);
					}
				}
			}
		}

		@EventHandler(priority = EventPriority.LOWEST)
		public void onBlockPhysics(BlockPhysicsEvent e) {
			Block b = e.getBlock();
			if (b.getType() == Material.JUKEBOX) {
				if (blocksWaitingForPhysicsEvent.contains(b)) {
					blocksWaitingForPhysicsEvent.remove(b);
					if (b.isBlockPowered())
						doPowerup(b); 
				}
			}
			if (b.getType() == Material.REPEATER && ((Lightable)b.getBlockData()).isLit()) {
				for (BlockFace bf : nsewFaces) {
					if (b.isBlockFacePowered(bf)) {
						Block rc = b.getRelative(bf);
						if (rc.getType() == Material.JUKEBOX)
							blocksWaitingForPhysicsEvent.add(rc);
					}
				}
			}
		}

		private void doPowerup(Block block) {
			Jukebox box = (Jukebox) block.getState();
			Material disk = box.getPlaying();
			
			if (! disk.equals(Material.AIR)) {
				box.setPlaying(disk);
				Bukkit.getPluginManager().callEvent(new JukeEvent(box.getPlaying(), block));
			}
		}
	}
	
}
