package com.quirkygaming.qgheads;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.quirkygaming.commons.Util;
import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.reflectionlib.AnonymousClass;
import com.quirkygaming.reflectionlib.AnonymousObject;

public class QGHeads extends JavaPlugin {
	
	HeadsListener listener;
	private static ErrorHandler<RuntimeException> handler;
	
	public static HashMap<String, String> localNames = new HashMap<String, String>();
	public static HashMap<String, ItemStack> registeredHeads = new HashMap<String, ItemStack>();
	
	public static final ItemStack skeletonHead = new ItemStack(Material.SKELETON_SKULL, 1);
	public static final ItemStack zombieHead = new ItemStack(Material.ZOMBIE_HEAD);
	public static final ItemStack steveHead = new ItemStack(Material.PLAYER_HEAD);
	public static final ItemStack creeperHead = new ItemStack(Material.CREEPER_HEAD);
	
	public static final ItemStack spiderHead = initCustomHead("§rSpider Head", "5ad55f34-41b6-4bd2-9c32-18983c635936", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ==");
	public static final ItemStack caveSpiderHead = initCustomHead("§rCave Spider Head", "cab28771-f0cd-4fe7-b129-02c69eba79a5", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDE2NDVkZmQ3N2QwOTkyMzEwN2IzNDk2ZTk0ZWViNWMzMDMyOWY5N2VmYzk2ZWQ3NmUyMjZlOTgyMjQifX19");
	public static final ItemStack enderHead = initCustomHead("§rEnderman Head", "40ffb372-12f6-4678-b3f2-2176bf56dd4b", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2E1OWJiMGE3YTMyOTY1YjNkOTBkOGVhZmE4OTlkMTgzNWY0MjQ1MDllYWRkNGU2YjcwOWFkYTUwYjljZiJ9fX0=");
	public static final ItemStack pigZombieHead = initCustomHead("§rZombie Pigman Head", "18a2bb50-334a-4084-9184-2c380251a24b", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRlOWM2ZTk4NTgyZmZkOGZmOGZlYjMzMjJjZDE4NDljNDNmYjE2YjE1OGFiYjExY2E3YjQyZWRhNzc0M2ViIn19fQ==");
	
	@Override
	public void onEnable() {
		handler = ErrorHandler.logAll(getLogger());
		listener = new HeadsListener(this);
	}
	
	@Override
	public void onDisable(){ 
		
	}
	
	static final String prefix = "{SkullOwner:{Id:\"";
	
	public static ItemStack initCustomHead(String displayName, String uuid, String textureEncoded) {
		String NBTJSON = prefix+uuid+ "\",Properties:{textures:[{"
						+ "Value:\"" + textureEncoded + "\"}]}}}";
		AnonymousClass jsonToNbt = Util.loadNMSClass("MojangsonParser");
		AnonymousObject nbt = jsonToNbt.invokeAnon(handler, "parse", NBTJSON);
		localNames.put(uuid, displayName);
		ItemStack head = getPlayerHead(nbt);
		registeredHeads.put(uuid, head);
		return head;
	}
	
	public static void replaceHead(Block head, ItemStack newHead) {
		AnonymousObject craftBlockState = AnonymousObject.fromObject(head.getState());
		AnonymousObject tileEntitySkull = craftBlockState.invokeAnon(handler, "getTileEntity");
		
		Object nbtOwner = AnonymousObject.fromObject(getNBT(newHead)).invoke(handler, "get", "SkullOwner");
		
		Object gameProfile = Util.loadNMSClass("GameProfileSerializer").invoke(handler, "deserialize", nbtOwner);
		
		tileEntitySkull.invoke(handler, "setGameProfile", gameProfile);
		
		head.getState().update(true);
	}
	
	public static ItemStack getPlayerHead(UUID playerUUID) {
		ItemStack playerHead = steveHead.clone();
		SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
		skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerUUID));
		playerHead.setItemMeta(skullMeta);
		return playerHead;
	}
	
	public static ItemStack getPlayerHead(AnonymousObject nbt) {
		AnonymousClass craftItemStack = Util.loadCraftBukkitClass("inventory.CraftItemStack");
		AnonymousObject nmsItem = craftItemStack.invokeAnon(handler, "asNMSCopy", steveHead);
		nmsItem.invoke(handler, "setTag", nbt);
		return craftItemStack.invoke(handler, "asBukkitCopy", nmsItem);
	}
	
	public static Object getNBT(ItemStack head) {
		if (head.getType() != Material.PLAYER_HEAD) return null;
		
		AnonymousClass craftItemStack = Util.loadCraftBukkitClass("inventory.CraftItemStack");
		AnonymousObject nmsItem = craftItemStack.invokeAnon(handler, "asNMSCopy", head);
		return nmsItem.invoke(handler, "getTag");
	}
	
	public static String getUUIDFromPlayerHead(ItemStack head) {
		Object nbt = getNBT(head);
		if (nbt == null) return null;
		
		String uuid = nbt.toString().replace(QGHeads.prefix, "");
		return uuid.substring(0, uuid.indexOf("\""));
	}
	
	public static String getUUIDFromPlayerHead(Block head) {
		return getUUIDFromPlayerHead(head.getDrops().iterator().next());
	}
}
