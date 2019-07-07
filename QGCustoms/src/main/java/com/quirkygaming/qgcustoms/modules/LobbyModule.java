package com.quirkygaming.qgcustoms.modules;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.quirkygaming.commons.CommonsAPI;
import com.quirkygaming.commons.playerinterface.PlayerInfo;
import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;
import com.quirkygaming.qgwarps.QGWarpsAPI;
import com.quirkygaming.qgwarps.SpawnpointManager;
import com.quirkygaming.qgwarps.Warp;
import com.quirkygaming.qgwarps.WarpListener;

public class LobbyModule extends CustomModule implements SpawnpointManager, WarpListener {
	
	public static Location LOBBY = new Location(plugin.getServer().getWorld("QuirkyGaming"), -199.5, 28.5, 0.5);
	
	private static Path warpfile =  FileSystems.getDefault().getPath("lobby_warps.txt");
	private static TreeMap<String, Warp> warps = new TreeMap<>();
	// This file goes in root server directory
	// Line-by-line syntax: typeID x_button y_button z_button x_dest y_dest z_dest pitch_dest yaw_dest world_dest
	// Type ID prefixes: free, primary, secondary
	// Type ID suffixes: playerteleport
	// Example: secondary_playerteleport
	// Remove an entire comment with // anywhere on the line
	
	public LobbyModule(QGCustomsPlugin plugin) {
		super("Lobby", new Listener[]{new CCListener()}, new String[]{}, plugin);
	}
	
	public static void updateProfit(Material mat, int plusMinusAmount) {
		String pname = "";
		if (mat.equals(Material.EMERALD)) {
			pname = "KingFredFnd_Emrld";
		} else if (mat.equals(Material.GOLD_INGOT)) {
			pname = "KingFredFnd_Gold";
		} else if (mat.equals(Material.IRON_INGOT)) {
			pname = "KingFredFnd_Iron";
		} else {
			System.out.println("[QGCustoms:Lobby] Invalid payment type processed...?");
			return;
		}
		
		MutableProperty<Integer> kfred = QGCustomsPlugin.customsConf.getOrInitiateProperty(pname, 1, 0);
		
		kfred.set(kfred.get() + plusMinusAmount);
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		return false;
	}

	@Override
	public boolean onEnable() {
		QGWarpsAPI.registerWarpListener(this);
		QGWarpsAPI.addCustomSpawnpointManager("lobby", this);
		QGWarpsAPI.setDefaultRespawnLocation("lobby");
		
		Bukkit.getServer().createWorld(WorldCreator.name("jailworld").environment(Environment.NETHER)); // Jail
		loadWarps(); // make sure to load after jailworld is existent
		return true;
	}

	@Override
	public boolean onDisable() {
		return true;
	}
	
	// Called through QGTest
	public static void lobbyDebug(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			loadWarps();
			Location l = p.getLocation();
			p.sendMessage(l.getX() + " " + l.getY() + " " + l.getZ() + " " + l.getPitch() + " " + l.getYaw() + " " + l.getWorld());
		}
	}
	
	private static void loadWarps() {
		warps.clear();
		try {
			List<String> lines = Files.readAllLines(warpfile, Charset.defaultCharset() );
			for (String entry : lines) {
				if (entry.isEmpty() || entry.contains("//")) continue;
				String[] parts = entry.trim().split(" ");
				int bx = Integer.parseInt(parts[1]);
				int by = Integer.parseInt(parts[2]);
				int bz = Integer.parseInt(parts[3]);
				double x = Double.parseDouble(parts[4]);
				double y = Double.parseDouble(parts[5]);
				double z = Double.parseDouble(parts[6]);
				float pitch = (float)Double.parseDouble(parts[7]);
				float yaw = (float)Double.parseDouble(parts[8]);
				String w = parts[9];
				Warp warp = new Warp(parts[0], w, x,y,z,yaw,pitch);
				warps.put(bx + " " + by + " " + bz, warp);
			}
			plugin.getLogger().info("Loaded "+warps.size()+" warps from lobby_warps.txt.");
		} catch (IOException e) {
			plugin.getLogger().warning("Could not read lobby_warps.txt; please construct it in the server root.");
		}
	}
	
	public static class CCListener implements Listener {
		@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
		public void onInteract(final PlayerInteractEvent e) {
			if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)
				&& (e.getClickedBlock().getType().equals(Material.STONE_BUTTON) ||
				    e.getClickedBlock().getType().equals(Material.WOOD_BUTTON))
				&& (e.getPlayer().getGameMode().equals(GameMode.ADVENTURE) || 
				    e.getPlayer().getGameMode().equals(GameMode.CREATIVE))) {
				
				if (e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) e.getPlayer().sendMessage("You pressed " + e.getClickedBlock().getLocation());
				
				Location l = e.getClickedBlock().getLocation();
				String key = l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ();
				if (warps.containsKey(key)) {
					Warp warp = warps.get(key);
					boolean quantity = true;
					int deduction = 0;
					ItemStack is = null;
					if (warp.getName().startsWith("free")) {
						// That's great, just warp
					} else if (warp.getName().startsWith("primary")) {
						// That'll be expensive
						is = e.getPlayer().getInventory().getItemInMainHand();
						if (is.getType().equals(Material.IRON_INGOT)) {
							if (is.getAmount() < 3) quantity=false;
							else deduction = 3;
						} else if (is.getType().equals(Material.GOLD_INGOT)) {
							if (is.getAmount() < 2) quantity=false;
							else deduction = 2;
						} else if (is.getType().equals(Material.EMERALD)) {
							if (is.getAmount() < 1) quantity=false;
							else deduction = 1;
						} else {
							e.getPlayer().sendMessage(ChatColor.RED + "Please hold the payment in your right hand while pressing the button.");
							return;
						}
						
					} else if (warp.getName().startsWith("secondary")) {
						// That'll be more expensive
						is = e.getPlayer().getInventory().getItemInMainHand();
						if (is.getType().equals(Material.IRON_INGOT)) {
							if (is.getAmount() < 6) quantity=false;
							else deduction = 6;
						} else if (is.getType().equals(Material.GOLD_INGOT)) {
							if (is.getAmount() < 4) quantity=false;
							else deduction = 4;
						} else if (is.getType().equals(Material.EMERALD)) {
							if (is.getAmount() < 2) quantity=false;
							else deduction = 2;
						} else {
							e.getPlayer().sendMessage(ChatColor.RED + "Please hold the payment in your right hand while pressing the button.");
							return;
						}
						
					} else {
						e.getPlayer().sendMessage(ChatColor.RED + "This warp is not supported yet.");
						return;
					}
					
					if (!quantity) {
						e.getPlayer().sendMessage(ChatColor.RED + "Not enough payment!");
						return;
					} 
					
					if (warp.getName().endsWith("playerteleport")) {
						boolean canPrompt = prompt1(e, ((is == null) ? Material.AIR : is.getType()), deduction);
						
						if (canPrompt) {
							e.getPlayer().sendMessage(ChatColor.GOLD + "Please enter the name of the player to whom you wish to teleport:");
							updateProfit(is.getType(), deduction);
							is.setAmount(is.getAmount() - deduction);
							e.getPlayer().getInventory().setItemInMainHand(is);
						} else {
							e.getPlayer().sendMessage(ChatColor.RED + "Transaction already in progress!");
						}
					} else {
						if (is != null) {
							updateProfit(is.getType(), deduction);
							is.setAmount(is.getAmount() - deduction);
							e.getPlayer().getInventory().setItemInMainHand(is);
						}
						e.getPlayer().teleport(warp.getLocation());
					}
				}
			}
		}
		
		public boolean prompt1(final PlayerInteractEvent e, final Material refundMaterial, final int refundAmount) {
			final MutableProperty<String> response = MutableProperty.newProperty("");
			final MutableProperty<Boolean> canceled = MutableProperty.newProperty(false);
			return CommonsAPI.chatPrompt(e.getPlayer(), new Runnable(){
				public void run() {
					if (canceled.get()) {
						e.getPlayer().sendMessage(ChatColor.RED + "No response recieved; canceling transaction.");
						refund(e, refundMaterial, refundAmount);
					} else {
						final PlayerInfo target = new PlayerInfo(response.get());
						if (target != null && !target.offline()) {
							e.getPlayer().sendMessage(ChatColor.GREEN + "You selected: " + target.username());
							e.getPlayer().sendMessage(ChatColor.GOLD + "Waiting for " + target.username() + " to respond...");
							target.player().sendMessage(ChatColor.GOLD + e.getPlayer().getName() + " wants to teleport to you. Type CONFIRM to allow or CANCEL to disallow.");
							Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){public void run() {
								prompt2(e, refundMaterial, refundAmount, target);
							}}, 1);
						} else {
							e.getPlayer().sendMessage(ChatColor.RED + "Target player not found!");
							refund(e, refundMaterial, refundAmount);
						}
						
					}
				}
				
				
			}, response, canceled, 20*20);
		}
		
		public void refund(final PlayerInteractEvent e, final Material refundMaterial, final int refundAmount) {
			if (refundMaterial != Material.AIR) {
				updateProfit(refundMaterial, -refundAmount);
				e.getPlayer().getInventory().addItem(new ItemStack(refundMaterial, refundAmount));
				e.getPlayer().sendMessage(ChatColor.GREEN + "Refunded transaction.");
			}
		}
		
		public boolean prompt2(final PlayerInteractEvent e, final Material refundMaterial, final int refundAmount, final PlayerInfo target) {
			final MutableProperty<String> response = MutableProperty.newProperty("");
			final MutableProperty<Boolean> canceled = MutableProperty.newProperty(false);
			//Second prompt
			return CommonsAPI.chatPrompt(target.player(), new Runnable(){
				public void run() {
					if (canceled.get()) {
						e.getPlayer().sendMessage(ChatColor.RED + "No response recieved; canceling transaction.");
						target.player().sendMessage(ChatColor.RED + "Request timed out.");
						refund(e, refundMaterial, refundAmount);
					} else {
						if (response.get().equalsIgnoreCase("CONFIRM")) {
							e.getPlayer().sendMessage("Teleporting...");
							target.player().sendMessage("Teleporting " + e.getPlayer().getName() + " you...");
							e.getPlayer().teleport(target.player());
						} else {
							e.getPlayer().sendMessage(ChatColor.RED + "Denied; canceling transaction.");
							refund(e, refundMaterial, refundAmount);
						}
						
					}
				}
			}, response, canceled, 20*20);
		}
		
		@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
		public void onBed(PlayerInteractEvent e) { // Lobby bed
			if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().equals(Material.BED_BLOCK)) {
				String region = QGWarpsAPI.getRegion(e.getClickedBlock().getLocation());
				if (region != null && region.equalsIgnoreCase("Lobby")) {
					e.setCancelled(true);
					if (e.getPlayer().getBedSpawnLocation() != null) {
						e.getPlayer().teleport(e.getPlayer().getBedSpawnLocation().add(0, .1, 0));
					} else {
						e.getPlayer().sendMessage(ChatColor.RED + "Your home bed was missing or obstructed");
					}
					
				}
			}
		}
	}

	@Override
	public boolean canWarp(Player p, WarpType type, MutableProperty<String> reason) {
		return true;
	}

	@Override
	public Location getSpawnpointLocation(Player p) {
		return LOBBY;
	}

}
