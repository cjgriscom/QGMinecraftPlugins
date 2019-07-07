package com.quirkygaming.qginventory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.quirkygaming.commons.Prompt;
import com.quirkygaming.commons.PromptQueue;
import com.quirkygaming.commons.playerinterface.PlayerInfo;
import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.propertydb.sublayer.SubDB;
import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgplayermanager.QGPlayerManager;

public class QGInventory extends JavaPlugin implements CommandExecutor, Listener {
	
	SubDB<RuntimeException> database;
	
	static QGInventory instance;
	
	@Override
	public void onEnable() {
		instance = this;
		getCommand("qginventory").setExecutor(this);
		getServer().getPluginManager().registerEvents(this, this);
		getDataFolder().mkdir();
		database  = new SubDB<>("cache", getDataFolder(), ErrorHandler.logAll(getLogger(), true));
	}
	
	@Override
	public void onDisable(){
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("qginventory")) {
			
			if (QGPlayerManager.isOpConsoleOrModerator(sender)) {
				
				PromptQueue queue = new PromptQueue("CANCEL");
				final MutableProperty<UUID> uuid = MutableProperty.newProperty(null);
				
				new Prompt(queue, sender) {
					public boolean conditionAndMessage() {
						sender.sendMessage(ChatColor.GOLD + "Enter the name of a player to see a list of restore points: (type CANCEL to cancel)");
						return true;
					} public void onResponse(String msg) {
						PlayerInfo pd = new PlayerInfo(msg);
						uuid.set(pd.uniqueID());
						if (uuid.get() == null) {
							sender.sendMessage(ChatColor.RED + "Player not found.");
							queue.cancel();
						} else {
							sender.sendMessage(ChatColor.BLUE +""+ ChatColor.BOLD + pd.username() + ":");
							MutableProperty<PlayerCache> pc = getPlayerCache(uuid.get());
							pc.get().listRestorePoints(sender);
							closePlayerCache(pc);
						}
					}
				};
				
				new Prompt(queue, sender) {
					public boolean conditionAndMessage() {
						sender.sendMessage(ChatColor.GOLD + "Enter the inventory to restore: (or CANCEL to cancel)");
						return true;
					} public void onResponse(String msg) {
						try {
							int i = Integer.parseInt(msg);
							MutableProperty<PlayerCache> pc = getPlayerCache(uuid.get());
							pc.get().restoreIndex(sender, uuid.get(), i);
							closePlayerCache(pc);
						} catch (NumberFormatException e) {
							sender.sendMessage(ChatColor.RED + "Could not understand response: " + msg);
						}
					}
				};
				
				queue.startChain();
				
			} else {
				sender.sendMessage(ChatColor.RED + "This command must be executed by a server moderator!");
			}
		}
		
		return true;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		cache(p, e.getDeathMessage(), e.getEntity().getLocation());
	}

	private void cache(Player p, String reason, Location l) {
		Date d = new Date();
		MutableProperty<PlayerCache> pc = getPlayerCache(p.getUniqueId());
		pc.get().cache.put(d, new InventoryCache(p.getInventory(), p.getExp(), reason, l));
		closePlayerCache(pc);
	}
	
	private MutableProperty<PlayerCache> getPlayerCache(UUID id) {
		MutableProperty<PlayerCache> pc = database.getOrInitiateProperty(id.toString(), 1, null);
		if (pc.get() == null) pc.set(new PlayerCache());
		return pc;
	}
	
	private void closePlayerCache(MutableProperty<PlayerCache> cache) {
		cache.update();
		database.unloadProperty(cache);
	}
}

class PlayerCache implements Serializable {
	private static final long serialVersionUID = -4743642222690213775L;
	
	TreeMap<Date, InventoryCache> cache = new TreeMap<Date, InventoryCache>();

	public void listRestorePoints(CommandSender sender) {
		ChatColor color = ChatColor.GOLD;
		int i = 1;
		for (Date d : cache.keySet()) {
			String reason = cache.get(d).deathReason;
			String world = cache.get(d).world;
			sender.sendMessage(ChatColor.BLUE + (i++ + ". ") + color + formatDate(d) + " (" + world + "): " + reason + "\n");
			
			if (color == ChatColor.GOLD) color = ChatColor.WHITE;
			else color = ChatColor.GOLD;
		}
	}

	private String formatDate(Date d) {
		return DateFormat.getDateInstance().format(d) + " " + DateFormat.getTimeInstance().format(d);
	}

	public void restoreIndex(CommandSender sender, UUID player, int desired) {
		int i = 1;
		Date found = null;
		for (Date d : cache.keySet()) {
			if (i++ != desired) continue;
			found = d;
			break;
		}
		
		if (found != null) {
			// This routine must be performed outside the loop to avoid ConcurrentModificationException
			if (Bukkit.getPlayer(player) != null) {
				restoreInventory(found,desired, Bukkit.getPlayer(player), sender);
				sender.sendMessage(ChatColor.GOLD + "Restored inventory " + desired + ".");
			} else sender.sendMessage(ChatColor.RED + "Players must be online to restore an inventory!");
		} else {
			sender.sendMessage(ChatColor.RED + "Could not restore inventory " + desired + ".");
		}
	}

	private void restoreInventory(Date d, int id, Player player, CommandSender sender) {
		//Run backup
		cache.put(new Date(), new InventoryCache(player.getInventory(), player.getExp(), "Backup (restored " + id + " by "+sender.getName()+")", player.getLocation()));
		cache.get(d).populateInventory(player);
		player.sendMessage(ChatColor.GOLD + "Your inventory has been restored to " + formatDate(d));
		
	}
}

class InventoryCache implements Serializable {
	private static final long serialVersionUID = 7465863031032195578L;
	
	String deathReason;
	float exp;
	Serializable[] itemSer;
	Serializable helmet;
	Serializable chestplate;
	Serializable pants;
	Serializable boots;
	Serializable shield;
	String world;
	
	public InventoryCache(PlayerInventory pi, float exp, String reason, Location l) {
		this.exp = exp;
		this.deathReason = reason;
		this.world = l.getWorld().getName();
		ItemStack[] stacks = pi.getContents();
		itemSer = new Serializable[stacks.length];
		for (int i = 0; i < stacks.length; i++) {
			itemSer[i] = serialize(stacks[i]);
		}
		helmet = serialize(pi.getHelmet());
		chestplate = serialize(pi.getChestplate());
		pants = serialize(pi.getLeggings());
		boots = serialize(pi.getBoots());
		shield = serialize(pi.getItemInOffHand());
	}
	
	public void populateInventory(Player p) {
		PlayerInventory pi = p.getInventory();
		p.setExp(exp);
		for (int i = 0; i < itemSer.length; i++) {
			pi.setItem(i, deserialize(itemSer[i]));
		}
		pi.setHelmet(deserialize(helmet));
		pi.setChestplate(deserialize(chestplate));
		pi.setLeggings(deserialize(pants));
		pi.setBoots(deserialize(boots));
		pi.setItemInOffHand(deserialize(shield));
	}
	
	public float getExp() {
		return exp;
	}
	
	static Serializable serialize(ItemStack src) {
		if (src == null) return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BukkitObjectOutputStream boos;
		try {
			boos = new BukkitObjectOutputStream(baos);
			boos.writeObject(src);
			boos.close();
			
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static ItemStack deserialize(Object src) {
		if (src == null) return null;
		ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) src);
		BukkitObjectInputStream bois;
		try {
			bois = new BukkitObjectInputStream(bais);
			Object o = null;
			try {
				o = bois.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			bois.close();
			
			return (ItemStack) o;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}