package com.quirkygaming.qgregions;

import static com.quirkygaming.qgregions.structures.RegionField.name;
import static com.quirkygaming.qgregions.structures.RegionField.note;
import static com.quirkygaming.qgregions.structures.RegionField.short_name;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.quirkygaming.commons.CommonsAPI;
import com.quirkygaming.commons.coordinate.Coord2D;
import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.propertydb.sublayer.SubDB;
import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgregions.structures.BoundaryType;
import com.quirkygaming.qgregions.structures.HeightBoundary;
import com.quirkygaming.qgregions.structures.OutlineBoundary;
import com.quirkygaming.qgregions.structures.Region;
import com.quirkygaming.qgregions.structures.RegionField;
import com.quirkygaming.qgregions.structures.RegionOpt;
import com.quirkygaming.qgwarps.QGWarpsAPI;
import com.quirkygaming.qgwarps.RegionHost;

public class QGRegions extends JavaPlugin implements Listener, CommandExecutor {
	
	
	public static SubDB<RuntimeException> region_DB;
	
	private static MutableProperty<TreeSet<String>> uuidIndex;
	static HashMap<String, Region> regions = new HashMap<String, Region>();
	
	private static File confDir = new File("plugins/QGRegions/");
	
	public static Collection<Region> getRegions() {
		return regions.values();
	}
	
	public static boolean hasPermission(CommandSender p) {
		return !(p instanceof Player) || p.isOp() || p.hasPermission("QuirkyGaming.regions");
	}

	private void adjustChat(AsyncPlayerChatEvent event) {
		Region r = getAssocRegion(event.getPlayer().getLocation());
		if (r != null) {
			if (r.isOptionSet(RegionOpt.nochat)) return;
			String current = event.getFormat();
			event.setFormat(ChatColor.BLUE + "[" + r.getField(short_name) + "] " + ChatColor.RESET + ChatColor.WHITE + current);
		}
	}
	
	@Override
	public void onEnable() {
		confDir.mkdirs();
		region_DB = new SubDB<>("QGRegions", confDir, ErrorHandler.logAll(getLogger(), true));
		
		boolean existed = region_DB.propertyExists("index");
		uuidIndex = region_DB.initiateProperty("index", 1, new TreeSet<String>());
		
		if (existed) {
			Region.loadFromIndex(region_DB, this, uuidIndex, regions);
		}
		
		OptionListeners optionListeners = new OptionListeners();
		
		getServer().getPluginManager().registerEvents(optionListeners, this);
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("qgregions").setExecutor(this);
		
		if (Bukkit.getPluginManager().isPluginEnabled("QGSigns")) {
			// If Signs is being used, register a module
			// Placing a sign entitled [region] with a region name as the subtext
			//    allows creating a region with an outline boundary in one go.
			new RegionSignModule(this);
		}
		
		QGWarpsAPI.registerWarpListener(optionListeners);
		
		QGWarpsAPI.registerRegionHost(new RegionHost(){

			@Override
			public boolean inRegion(Location l) {
				Region r = getAssocRegion(l);
				return r != null && !r.isOptionSet(RegionOpt.nochat);
			}

			@Override
			public String getRegion(Location l) {
				Region r = getAssocRegion(l);
				if (!r.isOptionSet(RegionOpt.nochat)) {
					return r.getField(short_name);
				}
				return "";
			}
			
		});
	}
	
	public static @Nullable Region getAssocRegion(Location l) {
		for (Region r : regions.values()) {
			if (r.contains(l)) return r;
		}
		return null;
	}
	
	UUID currentPlayer = null;
	Coord2D firstBound;
	TreeSet<Coord2D> bounds;
	Location signLoc;
	boolean complete = false;
	
	public boolean beginAddingRegion(String name, String world, CommandSender p) {
		if (hasPermission(p)) {
			if (regions.containsKey(name)) {
				p.sendMessage(ChatColor.RED + "That region already exists!");
				return false;
			}
			new Region(name, world, uuidIndex, regions); // Constructor handles all caches and such
			setSelection(p, name);
			p.sendMessage(ChatColor.GOLD + "Added and selected region " + name + ".");
			return true;
		} else {
			p.sendMessage(ChatColor.RED + "You do not have permission to set regions.");
			return false;
		}
	}
	
	public void addBoundary(Player player, Location loc) {
		if (currentPlayer != null) {
			player.sendMessage(ChatColor.RED + "[QGRegions] Another player is currently creating a region.");
			return;
		}
		if (regions.containsKey(name)) {
			player.sendMessage(ChatColor.RED + "[QGRegions] Region " + name + " already exists.");
			return;
		}
		currentPlayer = player.getUniqueId();
		signLoc = loc;
		firstBound = null;
		complete = false;
		bounds = new TreeSet<Coord2D>();
		player.sendMessage(ChatColor.RED + "[QGRegions] Walk around the entire region boundary and return to your starting point. Disconnect to cancel.");
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDisconnect(PlayerQuitEvent event) {
		if (event.getPlayer().getUniqueId() == currentPlayer) currentPlayer = null;
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!complete && event.getPlayer() != null && event.getPlayer().getUniqueId() == currentPlayer) {
			
			int curSize = bounds.size();
			Coord2D lastCoord = new Coord2D(
					event.getPlayer().getLocation().getBlockX(),
					event.getPlayer().getLocation().getBlockZ());
			bounds.add(lastCoord);
			if (firstBound == null) firstBound = lastCoord;
			if (bounds.size() != curSize && bounds.size() % 10 == 0) event.getPlayer().sendMessage(ChatColor.GOLD + "[QGRegions] " + bounds.size());
			if (bounds.size() > 4 && lastCoord.equals(firstBound)) {
				complete = true;
				event.getPlayer().sendMessage(ChatColor.GOLD + "[QGRegions] Completed perimeter. Stand inside the boundary and type FINISH.");
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onChat(final AsyncPlayerChatEvent event) {
		if (complete && event.getPlayer().getUniqueId() == currentPlayer) {
			if (event.getMessage().equalsIgnoreCase("finish")) {
				event.setCancelled(true);
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
					@Override
					public void run() {
						OutlineBoundary b = new OutlineBoundary(
								new Coord2D(event.getPlayer().getLocation().getBlockX(), event.getPlayer().getLocation().getBlockZ()), 
								bounds,
								selection(event.getPlayer()));
						
						boolean success = b.floodFill();
						if (!success) {
							event.getPlayer().sendMessage(ChatColor.RED + "[QGRegions] Could not fill region.  Make sure you are standing inside the border (not on it) and try again.");
							event.getPlayer().sendMessage(ChatColor.BLUE + "[QGRegions] Sometimes moving too fast (i.e. beacon + sprint + fly) causes boundary blocks to be skipped.");
							selection(event.getPlayer()).delBoundary(BoundaryType.OUTLINE);
							return;
						}
						selection(event.getPlayer()).setBoundary(b);
						currentPlayer = null;
						event.getPlayer().sendMessage(ChatColor.GOLD + "[QGRegions] Boundary added.");
					}
				});
				
			}
		} else {
			adjustChat(event);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("qgregions")) {
			if (hasPermission(sender)) {
				boolean success = cmdSub(sender, cmd, label, args);
				if (success) return true;
				else {
					sender.sendMessage(ChatColor.GOLD + "--QGRegions Commands--");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions add <world> <regionname>");
					sender.sendMessage("-Creates a new region.");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions select <name>");
					sender.sendMessage("-Selects a region for editing (required!)");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions rename <name>");
					sender.sendMessage("-Rename and reselect a region");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions list");
					sender.sendMessage("-Lists all defined regions.");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions info");
					sender.sendMessage("-Lists information about the selected region.");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions delete");
					sender.sendMessage("-Deletes the currently selected region.");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions options");
					sender.sendMessage("-Lists the possible boolean options for a region (block_creepers, etc).");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions enable <option>");
					sender.sendMessage("-Enable a boolean option (choices can be listed with /qgregions options).");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions disable <option>");
					sender.sendMessage("-Disable a boolean option (choices can be listed with /qgregions options).");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions set_height_bounds <lower> <upper>");
					sender.sendMessage("-Add Y coordinate boundaries to the selected region.");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions del_height_bounds");
					sender.sendMessage("-Remove the Y coordinate boundaries for the selected region.");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions set_outline_bounds");
					sender.sendMessage("-Begins the prompting system for adding an outline boundary.");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions del_height_bounds");
					sender.sendMessage("-Remove the outline boundary for the selected region.");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions set_short_name <name>");
					sender.sendMessage("-Sets the optional chat name of the region.");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgregions note <note>");
					sender.sendMessage("-Sets ownership note for the QG website.");
					return false;
				}
			} else {
				sender.sendMessage("You do not have permission to manage regions.");
				return true;
			}
		} else {
			return false;
		}
	}
	
	private String currSel = "";
	
	private @Nullable Region selection(CommandSender p) {
		if (currSel != null && !currSel.isEmpty()) {
			return regions.get(currSel);
		}
		return null;
	}
	private void setSelection(CommandSender p, String r) {
		currSel = r;
	}
	
	private boolean cmdSub(final CommandSender p, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		if (args[0].equalsIgnoreCase("list")) {
			p.sendMessage(ChatColor.GOLD + "[QGRegions] Region List: ");
			for (String name : regions.keySet()) {
				p.sendMessage("  " + name);
			}
			return true;
		} else if (args[0].equalsIgnoreCase("options")) {
			p.sendMessage(ChatColor.GOLD + "[QGRegions] Option List: ");
			for (RegionOpt opt : RegionOpt.values()) {
				p.sendMessage("  " + opt.name());
			}
			return true;
		} else if (args[0].equalsIgnoreCase("add")) {
			if (args.length < 3) {
				p.sendMessage("Use the syntax: /qgregions add <world> <regionname>.");
				return true;
			} else {
				String n = "";
				for (int i = 2; i < args.length; i++) {
					n += " " + args[i];
				}
				
				if (Bukkit.getWorld(args[1]) != null) {
					String sel = n.trim();
					beginAddingRegion(sel, args[1], p);
				} else {
					p.sendMessage(ChatColor.RED + "World does not exist.");
				}
				
				return true;
			}
		} else if (args[0].equalsIgnoreCase("select")) {
			if (args.length < 2) {
				p.sendMessage("Enter the region name after the select statement. Use /qgregions list to see a list of the regions.");
				return true;
			} else {
				String n = "";
				for (int i = 1; i < args.length; i++) {
					n += " " + args[i];
				}
				setSelection(p, n.trim());
				Region r = selection(p);
				if (r != null) {
					sendInfo(p, r);
				} else {
					p.sendMessage(ChatColor.RED + "Could not find region \"" + n + "\"; try again (case sensitive!).");
					setSelection(p, "");
				}
				return true;
			}
		} else {
			if (selection(p) == null) {
				p.sendMessage("First use /qgregions select <name> to set a region to modify.");
				return true;
			}
			
			if (args[0].equalsIgnoreCase("rename")) {
				if (args.length < 2) {
					p.sendMessage("Enter the region name after the select statement. Use /qgregions list to see a list of the regions.");
					return true;
				} else {
					String n = "";
					for (int i = 1; i < args.length; i++) {
						n += " " + args[i];
					}
					n = n.trim();
					selection(p).rename(n, uuidIndex, regions);
					setSelection(p, n);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("delete")) {
				selection(p).delete(uuidIndex, regions); // Delete SubDB properties, this method handles all
				setSelection(p, "");
				p.sendMessage("Deleted.");
				return true;
			} else if (args[0].equalsIgnoreCase("info")) {
				sendInfo(p, selection(p));
				return true;
			} else if (args[0].equalsIgnoreCase("disable") || args[0].equalsIgnoreCase("enable")) {
				if (args.length != 2) {
					p.sendMessage("Syntax: /qgregions "+args[0].toLowerCase()+
							" <option>. Use /qgregions options to see a list of boolean options.");
					return true;
				}
				try {
					selection(p).setOption(RegionOpt.valueOf(args[1]), args[0].equalsIgnoreCase("enable"));
				} catch (Exception e) {p.sendMessage(ChatColor.RED + "That option does not exist.");}
				p.sendMessage("Updated.");
				return true;
			} else if (args[0].equalsIgnoreCase("set_short_name")) {
				if (args.length < 2) {
					p.sendMessage("Enter the region name after the command.");
					return true;
				} else {
					String n = "";
					for (int i = 1; i < args.length; i++) {
						n += " " + args[i];
					}
					selection(p).setField(short_name, n.trim());
				}
				p.sendMessage("Updated.");
				return true;
			} else if (args[0].equalsIgnoreCase("set_height_bounds")) {
				if (args.length != 3) {
					p.sendMessage("Use the syntax: /qgregions set_height_bounds <lower> <upper>");
					return true;
				} else {
					int lower = Integer.parseInt(args[1]);
					int upper = Integer.parseInt(args[2]);
					if (lower >= upper) {
						p.sendMessage("Invalid boundaries (lower >= upper).");
						return true;
					}
					selection(p).setBoundary(new HeightBoundary(lower, upper));
				}
				p.sendMessage("Updated.");
				return true;
			} else if (args[0].equalsIgnoreCase("del_height_bounds")) {
				selection(p).delBoundary(BoundaryType.HEIGHT);
				p.sendMessage("Updated.");
				return true;
			} else if (args[0].equalsIgnoreCase("set_outline_bounds")) {
				if (!(p instanceof Player)) {
					p.sendMessage("This command can only be executed by a player.");
					return true;
				}
				final MutableProperty<String> response = MutableProperty.newProperty("");
				final MutableProperty<Boolean> canceled = MutableProperty.newProperty(false);
				
				p.sendMessage(ChatColor.GOLD + "Please stand in the location where the outline will begin. Type BEGIN when ready (30 second timeout).");
				
				CommonsAPI.chatPrompt(p, new Runnable() {public void run() {
					if (canceled.get() || !response.get().equalsIgnoreCase("begin")) {
						p.sendMessage("Canceled.");
					} else {
						addBoundary((Player) p, ((Player) p).getLocation());
					}
				}}, response, canceled, 20*30);
				return true;
			} else if (args[0].equalsIgnoreCase("del_outline_bounds")) {
				selection(p).delBoundary(BoundaryType.OUTLINE);
				p.sendMessage("Updated.");
				return true;
			} else if (args[0].equalsIgnoreCase("note")) {
				if (args.length < 2) {
					p.sendMessage("Enter the note after the command.");
					return true;
				} else {
					String n = "";
					for (int i = 1; i < args.length; i++) {
						n += " " + args[i];
					}
					selection(p).setField(note, n);
				}
				p.sendMessage("Updated.");
				return true;
			} else {
				return false;
			}
		}
	}

	private void sendInfo(CommandSender p, Region r) {
		p.sendMessage(ChatColor.GOLD + "Selected region: " + r.getField(name));
		
		String opts = "";
		for (RegionOpt o : r.options) opts += o.name() + " ";
		p.sendMessage("  UUID: " + r.getField(RegionField.UUID));
		p.sendMessage("  Note: " + r.getField(note));
		p.sendMessage("  Short Name: " + r.getField(short_name));
		p.sendMessage("  Options: " + opts);
		p.sendMessage("  Boundary Criteria: ");
		for (BoundaryType t : r.boundaries.keySet()) {
			p.sendMessage("    " + t.name() + ": " + r.boundaries.get(t));
		}
	}
}
