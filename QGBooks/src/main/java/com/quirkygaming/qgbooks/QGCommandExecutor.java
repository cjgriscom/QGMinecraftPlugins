package com.quirkygaming.qgbooks;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.quirkygaming.qgbooks.config.Database;
import com.quirkygaming.qgbooks.entries.CompatableEntry;

public class QGCommandExecutor implements CommandExecutor {
	 
	private QGBooks plugin;
 
	public QGCommandExecutor(QGBooks plugin) {
		this.plugin = plugin;
		this.plugin.getCommand("qgbooks").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("qgbooks")){
			boolean success = cmdSub(sender, cmd, label, args);
			if (sender instanceof Player && success) {
				plugin.processEvent((Player) sender, ((Player) sender).getInventory().getItemInMainHand()); 
				// Process messagebook events;
			}
			return success;
		}
		return false;
	}
	
	private boolean cmdSub(CommandSender p, Command cmd, String label, String[] args) {
		if (args.length < 1) {
			p.sendMessage("Too few arguments");
			return false;
		}
		if (args[0].equalsIgnoreCase("hide")) {
			return showOrHideCommand(args,p, true);
		} else if (args[0].equalsIgnoreCase("show")) {
			return showOrHideCommand(args,p, false);
		} else if (args[0].equalsIgnoreCase("help")) {
			p.sendMessage(ChatColor.GOLD + "--QGBooks Commands--\n");
			p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgbooks hide <bookID>\nOR /qgbooks hide <firstID> <lastID>");
			p.sendMessage("-Hides an entry or range of entries from your own message book.\n");
			p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgbooks show <bookID>\nOR /qgbooks show <firstID> <lastID>");
			p.sendMessage("-Un-hides a previously hidden entry or range of entries.\n");
			p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/qgbooks delete <bookID>");
			p.sendMessage("-Deletes a message from all recipient's books IF you were the sender or have delete permission.");
			return true;
		} else if (args[0].equalsIgnoreCase("delete")) {
			return deleteCommand(args,p);
		}
		return false;
	}
	
	
	private long setRangeHidden(String startS, String endS, CommandSender p, boolean hide) {
		long count = 0; // # of successfully hidden entries
		
		try {
			long startPos = new Long(startS);
			long endPos = new Long(endS);
			
			for (long i = startPos; i <= endPos; i++) {
				CompatableEntry e = Database.getPageByID(i);
				if (e==null || !e.hasSenderOrRecipient(p)) {
					// fail
				} else {
					if (hide) {
						Database.removePageUser(e, Database.senderID(p));
					} else {
						Database.addPageUser(e, Database.senderID(p));
					}
					count++;
				}
			}
		} catch (Exception e) {
			p.sendMessage(ChatColor.RED + "Error: Check the command documentation and try again.");
		}
		
		return count;
	}
	
	private boolean showOrHideCommand(String[] args, CommandSender p, boolean hide) {
		if (!(p instanceof Player)) {
			p.sendMessage("This command can only be executed by a player."); 
			return false;
		}
		if (args.length < 2) {
			p.sendMessage("Too few arguments");
			return false;
		} else if (args.length == 2) {
			if (setRangeHidden(args[1], args[1], p, hide) == 1) {
				if (hide) {
					p.sendMessage(ChatColor.GREEN + "Message " + args[1] + " is now hidden.");
				} else {
					p.sendMessage(ChatColor.GREEN + "Message " + args[1] + " is now visible.");
				}
				return true;
			} else {
				p.sendMessage(ChatColor.RED + "Could not find message " + args[1]);
			}
			return true;
		} else if (args.length == 3) {
			long count = setRangeHidden(args[1], args[2], p, hide);
			
			p.sendMessage(ChatColor.GREEN + "Successfully processed " + count + " messages.");
			return true;
		} else {
			p.sendMessage("Too many arguments");
			return false;
		}
		//return true;
	}
	
	private boolean deleteCommand(String[] args, CommandSender p) {
		boolean op = p.isOp();
		if (!(p instanceof Player) || p.hasPermission("QuirkyGaming.books.admin")) {
			op = true;
		}
		if (args.length < 2) {
			p.sendMessage("Too few arguments");
			return false;
		} else if (args.length == 2) {
			CompatableEntry e = Database.getPageByID(args[1]);
			if (e==null) {
				p.sendMessage(ChatColor.RED + "Could not find message " + args[1]);
				return true;
			}
			if (!op && !e.senderID().equals(Database.senderID(p))) {
				p.sendMessage(ChatColor.RED + "You do not own message " + args[1] + ". \n"
						+ "Only the sender of a message may delete it. Use \"/qgbooks hide <id>\" instead.");
				return true;
			}
			Database.deleteEntry(e);
			p.sendMessage(ChatColor.GREEN + "Deleted message " + args[1] + ".");
			return true;
		} else {
			p.sendMessage("Too many arguments");
			return false;
		}
	}
}
