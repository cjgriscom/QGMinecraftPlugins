package com.quirkygaming.qgcustoms.modules;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

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
		ShapedRecipe r = new ShapedRecipe(new NamespacedKey(plugin, "smooth_stone"), new ItemStack(Material.SMOOTH_STONE));
		r.shape("x", "x").setIngredient('x', Material.STONE_SLAB);
		
		Bukkit.getServer().addRecipe(r);
		return true;
	}

	@Override
	public boolean onDisable() {
		return true;
	}
	
	public static class CCListener implements Listener {
		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void onPlace(BlockPlaceEvent event) {
			if (event.getItemInHand() != null && 
					event.getItemInHand().hasItemMeta() && 
					event.getItemInHand().getItemMeta().hasDisplayName() && 
					event.getItemInHand().getItemMeta().getDisplayName().equals("Smooth Stone")) {
				event.getBlockPlaced().setType(Material.SMOOTH_STONE);
			}
			
		}
		
	}
	
}
