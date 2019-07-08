package com.quirkygaming.qgcustoms.modules;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.quirkygaming.commons.coordinate.Coord3D;
import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.propertydb.PropertyDB;
import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgbooks.QGBooks;
import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;
import com.quirkygaming.qgplayermanager.QGPlayerManager;

public class ModeratorModule extends CustomModule {
	private static final File confDir = new File("plugins/QGCustoms/ModeratorModule/");
	
	private static TreeMap<Coord3D, ItemList> chestCache = new TreeMap<Coord3D, ItemList>();
	private static HashMap<String, Integer> lockMode = new HashMap<String, Integer>();
	
	private static MutableProperty<TreeMap<Coord3D, UUID>> chestLock;

	public ModeratorModule(QGCustomsPlugin plugin) {
		super("Moderator", new Listener[]{new ChestLockListener()}, new String[]{"qgmoderator"}, plugin);
		
		
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (cmd.getName().equalsIgnoreCase("qgmoderator")) {
			if (QGPlayerManager.isOpConsoleOrModerator(sender)) {
				boolean success = cmdSub(sender, cmd, label, args);
				if (success) return true;
				else {
					sender.sendMessage(ChatColor.GOLD + "--QGModerator Commands--\n");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgmoderator chestlock");
					sender.sendMessage("-Toggles chest protection mode (rightclick on chests with a wood axe)\n");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgmoderator chestunlock");
					sender.sendMessage("-Toggles chest un-proctection mode (rightclick on chests with a wood axe)\n");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgmoderator release");
					sender.sendMessage("-Stop chest protection mode\n");
					return false;
				}
			} else {
				sender.sendMessage("You do not have moderator permission.");
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean onEnable() {
		confDir.mkdirs();
		
		chestLock = PropertyDB.initiateProperty(
				confDir, "chestLock", 2,
				new TreeMap<Coord3D, UUID>(), 
				ErrorHandler.logAll(plugin.getLogger(), true));
		
		plugin.getLogger().info("Loaded " + chestLock.get().size() + " V2 ChestLock entries.");
		return true;
	}

	@Override
	public boolean onDisable() {
		chestLock.update();
		return true;
	}
	
	private boolean cmdSub(CommandSender p, Command cmd, String label, String[] args) {
		if (args.length < 1) {
			p.sendMessage("Too few arguments");
			return false;
		}
		if (args[0].equalsIgnoreCase("chestlock")) {
			p.sendMessage(ChatColor.GOLD + "Entered chest protection mode...");
			lockMode.put(p.getName(), 1);
			return true;
		} else if (args[0].equalsIgnoreCase("chestunlock")) {
			p.sendMessage(ChatColor.GOLD + "Entered chest unlocking mode...");
			lockMode.put(p.getName(), 2);
			return true;
		} else if (args[0].equalsIgnoreCase("release")) {
			p.sendMessage(ChatColor.GOLD + "Exited chest protection.");
			lockMode.remove(p.getName());
			return true;
		} else {
			return false;
		}
	}
	
	private static class ItemList {
		HashMap<Material, Integer> container = new HashMap<Material, Integer>();
		
		ItemList(Inventory i) {
			for (ItemStack s : i.getContents()) {
				if (s == null) continue;
				inc(s.getType(), s.getAmount());
			}
		}
		
		String getChangesAndUpdateThis(ItemList newList, String delimiter) {
			syncKeysWith(newList); // Make sure all keys accounted for
			newList.syncKeysWith(this);
			String list = "";
			for (Material m : container.keySet()) {
				int old = container.get(m);
				int neww = newList.container.get(m);
				if (old > neww) {
					list += (neww-old) + ":" + m.toString() + delimiter;
				} else if (old < neww) {
					list += "+" + (neww-old) + ":" + m.toString() + delimiter;
				}
			}
			return list;
		}
		
		private void syncKeysWith(ItemList other) {
			for (Material m : other.container.keySet()) {
				initMatr(m);
			}
		}
		
		private void inc(Material m, int amount) {
			initMatr(m);
			container.put(m, container.get(m) + amount);
		}
		
		private void initMatr(Material m) {
			if (!container.containsKey(m)) container.put(m, 0);
		}
		
	}

	private static void handleChestOpen(InventoryOpenEvent e, Player p, Inventory iv, Location l) {
		Coord3D coord = new Coord3D(l);
		if (lockMode.containsKey(p.getName())) {
			if (!p.getInventory().getItemInMainHand().getType().equals(Material.WOODEN_AXE)) return;
			e.setCancelled(true);
			int mode = lockMode.get(p.getName());
			if (mode == 1) {
				if (chestLock.get().containsKey(coord)) {
					p.sendMessage(ChatColor.GOLD + "Chest protection already exists at " + coord);
					return;
				}
				chestLock.get().put(coord, p.getUniqueId());
				p.sendMessage(ChatColor.GOLD + "Protecting chest at " + coord);
			} else if (mode == 2) {
				if (!chestLock.get().containsKey(coord)) {
					p.sendMessage(ChatColor.GOLD + "No chest protection exists at " + coord);
					return;
				}
				chestLock.get().remove(coord);
				p.sendMessage(ChatColor.GOLD + "Removing chest protection at " + coord);
			}
			chestLock.update();
		}
		if (chestLock.get().containsKey(coord)) 
			chestCache.put(coord, new ItemList(iv));
	}
	
	private static void handleChestClose(InventoryCloseEvent e, Player p, Inventory iv, Location l) {
		Coord3D coord = new Coord3D(l);
		
		if (!lockMode.containsKey(p.getName()) && chestLock.get().containsKey(coord) && chestCache.containsKey(coord)) {
			UUID owner = chestLock.get().get(coord);
			if (p.getUniqueId().equals(owner)) return;
			
			// Intruder
			String diffList = chestCache.get(coord).getChangesAndUpdateThis(
					new ItemList(iv), ", ");
			
			if (diffList.isEmpty()) return;
			
			String recipName = Bukkit.getOfflinePlayer(owner).getName();
			String message = "@" + recipName + " " + ChatColor.RED +
					"QGModerator Chestlock: " + p.getName() + " modified " + recipName+  "'s chest at " + coord + ":\n" +
					ChatColor.RESET + diffList;
			QGBooks.INSTANCE.sendMessageViaString(message, Bukkit.getConsoleSender());
		}
	}
	
	static class ChestLockListener implements Listener {
		@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
		public void onChestOpen(InventoryOpenEvent e) {
			if (!(e.getPlayer() instanceof Player)) return;
			Player p = (Player) e.getPlayer();
			
			if (e.getInventory().getType().equals(InventoryType.CHEST)) {
				if (e.getInventory().getHolder() instanceof DoubleChest) {
					DoubleChest dc = (DoubleChest) e.getInventory().getHolder();
					Chest left = (Chest)(dc.getLeftSide());
					Chest right = (Chest)(dc.getRightSide());
					handleChestOpen(e, p, left.getBlockInventory(), left.getLocation());
					handleChestOpen(e, p, right.getBlockInventory(), right.getLocation());
				} else if (e.getInventory().getHolder() instanceof Chest) {
					
					Chest c = (Chest) e.getInventory().getHolder();
					handleChestOpen(e, p, c.getBlockInventory(), c.getLocation());
				} else if (e.getInventory().getHolder() instanceof ShulkerBox) {
					
					ShulkerBox c = (ShulkerBox) e.getInventory().getHolder();
					handleChestOpen(e, p, c.getInventory(), c.getLocation());
				}
				
			}
		}
		
		@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
		public void onChestClose(InventoryCloseEvent e) {
			if (!(e.getPlayer() instanceof Player)) return;
			Player p = (Player) e.getPlayer();
			
			if (e.getInventory().getType().equals(InventoryType.CHEST)) {
				if (e.getInventory().getHolder() instanceof DoubleChest) {
					DoubleChest dc = (DoubleChest) e.getInventory().getHolder();
					Chest left = (Chest)(dc.getLeftSide());
					Chest right = (Chest)(dc.getRightSide());
					handleChestClose(e, p, left.getBlockInventory(), left.getLocation());
					handleChestClose(e, p, right.getBlockInventory(), right.getLocation());
				} else if (e.getInventory().getHolder() instanceof Chest) {
					Chest c = (Chest) e.getInventory().getHolder();
					handleChestClose(e, p, c.getBlockInventory(), c.getLocation());
				} else if (e.getInventory().getHolder() instanceof ShulkerBox) {
					ShulkerBox c = (ShulkerBox) e.getInventory().getHolder();
					handleChestClose(e, p, c.getInventory(), c.getLocation());
				}
			}
		}
		
		
		@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
		public void onBlockBreak(BlockBreakEvent e) {
			Coord3D loc = new Coord3D(e.getBlock().getLocation());
			if (chestLock.get().containsKey(loc)) {
				UUID owner = chestLock.get().get(loc);
				Player p = e.getPlayer();
				
				chestLock.get().remove(loc);
				chestLock.update();
				
				chestCache.remove(loc);
				
				if (p.getUniqueId().equals(owner)) {
					p.sendMessage(ChatColor.GOLD + "Removing chest protection at " + loc);
					return;
				}
				String recipName = Bukkit.getOfflinePlayer(owner).getName();
				String message = "@" + recipName + " " + ChatColor.RED + "QGModerator Chestlock: \n" +
						p.getName() + " broke "+recipName+"'s chest at " + loc + ".";
				QGBooks.INSTANCE.sendMessageViaString(message, Bukkit.getConsoleSender());
			}
		}
	}
	
}


