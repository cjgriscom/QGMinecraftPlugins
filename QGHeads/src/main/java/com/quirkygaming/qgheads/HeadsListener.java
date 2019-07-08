package com.quirkygaming.qgheads;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;

public class HeadsListener implements Listener {
	
	private QGHeads plugin;
	private Random random;
	
	public HeadsListener(QGHeads plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		registerProtocolLibListener();
		random = new Random();
	}
	
	private boolean getLootCondition(LivingEntity deadMob) {
		
		if (deadMob != null) {
			Player killer = deadMob.getKiller();
			if (killer != null) {
				ItemStack item = killer.getInventory().getItemInMainHand();
				if (item != null) {
					if (item.containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
						int lev = item.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
						switch (lev) {
							case 1: return random.nextInt(166) < 5;
							case 2: return random.nextInt(143) < 5;
							case 3: return random.nextInt(125) < 5;
						}
					}
				}
			}
		}
		return random.nextInt(200) < 5;
	}
	
	/*/ Remove display name when inside item frames
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onItemFrameInsert(final HangingEvent event) {
		System.out.println("Caought inser5t ecenet");
		if (event.getEntity().getType() == EntityType.ITEM_FRAME) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					ItemFrame frame = (ItemFrame) event.getEntity();
					if (frame.getItem().getType() != Material.SKULL_ITEM) return;
					SkullMeta meta = (SkullMeta)frame.getItem().getItemMeta();
					if (meta == null) return;
					if (meta.hasDisplayName() && QGHeads.mobMap.containsKey(meta.getOwner())) {
						frame.setItem(QGHeads.mobMap_nodisplay.get(meta.getOwner()));
					}
				}
			});
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onHeadBreak(final BlockBreakEvent event) {
		Block b = event.getBlock();
		if (b.getType().equals(Material.SKULL)) {
			ItemStack item = b.getDrops().iterator().next();
			if (b.getDrops().iterator().next().hasItemMeta()) {
				SkullMeta data = (SkullMeta)item.getItemMeta();
				if (data.hasOwner()) {
					if (data.getOwner().startsWith("MHF_")) {
						event.setCancelled(true);
						b.setType(Material.AIR);
						b.getWorld().dropItemNaturally(b.getLocation(), QGHeads.getMobHeadByName(data.getOwner()).clone());
					}
				}
			}
		}
	}
	
	// Make sure pickups have display names
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onItemPickup(final InventoryPickupItemEvent event) {
		if (event.getItem().getItemStack().getType().equals(Material.SKULL_ITEM)) {
			System.out.println("Caught skull pickup eevent");
			SkullMeta meta = (SkullMeta)event.getItem().getItemStack().getItemMeta();
			if (meta == null) return;
			if (QGHeads.mobMap.containsKey(meta.getOwner())) {
				event.getItem().setItemStack(QGHeads.mobMap.get(meta.getOwner()));
			}
		}
	}*/
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onMobDeath(final EntityDeathEvent event) {
		
		if (getLootCondition(event.getEntity())) {  
			if (event.getEntityType().equals(EntityType.SKELETON)) {
				
				if (event.getEntity() instanceof org.bukkit.entity.WitherSkeleton) {
					event.getDrops().add(QGHeads.skeletonHead);
				}
				
			} else if (event.getEntityType().equals(EntityType.CREEPER)) {
				event.getDrops().add(QGHeads.creeperHead);
			} else if (event.getEntityType().equals(EntityType.ZOMBIE)) {
				event.getDrops().add(QGHeads.zombieHead);
			} else if (event.getEntityType().equals(EntityType.ENDERMAN)) {
				event.getDrops().add(QGHeads.enderHead);
			} else if (event.getEntityType().equals(EntityType.PIG_ZOMBIE)) {
				event.getDrops().add(QGHeads.pigZombieHead);
			} else if (event.getEntityType().equals(EntityType.SPIDER)) {
				event.getDrops().add(QGHeads.spiderHead);
			} else if (event.getEntityType().equals(EntityType.CAVE_SPIDER)) {
				event.getDrops().add(QGHeads.caveSpiderHead);
			}
		}
		
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerDeath(final PlayerDeathEvent event) {
		
		if (event.getEntityType().equals(EntityType.PLAYER) && event.getEntity().getKiller() != null) {
			
			DamageCause causeOfDeath = event.getEntity().getLastDamageCause().getCause();
			boolean attackerEqualsVictim = event.getEntity().getKiller().getName().equals(event.getEntity().getPlayer().getName());
			if (causeOfDeath.equals(DamageCause.ENTITY_ATTACK) && !attackerEqualsVictim) {
				if (event.getEntity().getKiller().getInventory().getItemInMainHand().getType().equals(Material.WOODEN_AXE)) {
					event.getDrops().add(QGHeads.getPlayerHead(event.getEntity().getUniqueId()));
				}
			}
		}
	}
	
	public ItemStack rename(ItemStack item) {
		if (item == null) return item;
		String uuid = QGHeads.getUUIDFromPlayerHead(item);
		if (uuid == null) return item;
		
		String rename = QGHeads.localNames.get(uuid);
		
		if (rename != null) {
			item = item.clone();
			ItemMeta data = item.getItemMeta();
			data.setDisplayName(rename);
			item.setItemMeta(data);
		}
		return item;
	}
	
	public void registerProtocolLibListener() {
		ProtocolLibrary.getProtocolManager().addPacketListener(
				new PacketAdapter(plugin, ListenerPriority.HIGH, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
					@Override
					public void onPacketSending(PacketEvent event) {
						PacketContainer packet = event.getPacket();
						if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
							packet.getItemModifier().write(0, rename(packet.getItemModifier().read(0)));
						} else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
							try {
								ItemStack[] elements = packet.getItemArrayModifier().read(0);
								for (int i = 0; i < elements.length; i++) {
									if (elements[i] != null) {
										elements[i] = rename(elements[i]);
									}
								}
								packet.getItemArrayModifier().write(0, elements);
							} catch (FieldAccessException e) {
								// Thrown when the event does not support itemstacks
								//plugin.getLogger().warning(e.getClass().getName() + " thrown for " + event.getPacketType().name());
							}
						}
					}
				});
	}
}
