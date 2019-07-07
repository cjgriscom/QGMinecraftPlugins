package com.quirkygaming.qgcustoms.modules;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class SlabRecipesModule extends CustomModule {

	public SlabRecipesModule(QGCustomsPlugin plugin) {
		super("SlabRecipes", new Listener[]{new CCListener()}, new String[]{}, plugin);
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		return false;
	}

	
	@Override
	public boolean onEnable() {
		ItemMeta sm = Bukkit.getItemFactory().getItemMeta(Material.STEP);
		sm.setDisplayName("Smooth Stone");
		smooth_stone.setItemMeta(sm);
		ShapedRecipe r = new ShapedRecipe(smooth_stone);
		r.shape("x", "x").setIngredient('x', Material.STEP);
		
		Bukkit.getServer().addRecipe(r);
		return true;
	}

	@Override
	public boolean onDisable() {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	static ItemStack smooth_stone = new ItemStack(Material.LEAVES, 1, (byte)5, (byte)5);
	static {
		ItemMeta IM = Bukkit.getItemFactory().getItemMeta(Material.LEAVES);
		IM.setDisplayName("Smooth Stone");;
		smooth_stone.setItemMeta(IM);
	}
	
	public static class CCListener implements Listener {
		@SuppressWarnings("deprecation")
		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void onPlace(BlockPlaceEvent event) {
			if (event.getItemInHand() != null && 
					event.getItemInHand().hasItemMeta() && 
					event.getItemInHand().getItemMeta().hasDisplayName() && 
					event.getItemInHand().getItemMeta().getDisplayName().equals("Smooth Stone")) {
				event.getBlockPlaced().setType(Material.DOUBLE_STEP);
				event.getBlockPlaced().setData((byte)(8));
			}
			
		}
		
	}
	
}
