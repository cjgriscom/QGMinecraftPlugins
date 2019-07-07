package com.quirkygaming.qgcustoms.modules;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class RulesModule extends CustomModule {

	public RulesModule(QGCustomsPlugin plugin) {
		super("Rules", new Listener[]{}, new String[]{"rules"}, plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] arg3) {
		if (cmd.getName().equalsIgnoreCase("rules")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				sendRules(player);
				return true;
			} else {
				sendRules();
				return true;
			}
		}
		
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
	

	public String[] getRules(boolean useColor) {
		String miscColor = "";
		String QGColor = "";
		String ruleColor = "";
		if (useColor) {
			miscColor = ChatColor.GREEN.toString();
			QGColor = ChatColor.GOLD.toString();
			ruleColor = ChatColor.RED.toString();
		}
		
		String[] messages = {QGColor + "--QuirkyGaming Rules--", 
				miscColor + "The leadership of QG strives to emphasize virtues rather then specific rules.",
				miscColor + "These virtues include Respect, Integrity, Honor, and Professionalism.", 
				ruleColor + "Visit http://minecraft.quirkygaming.com for a complete rule list that details what is expected of followers of these virtues."};
		
		return messages;
	}
	
	public void sendRules() {
		for (String msg : getRules(false)) plugin.getLogger().info(msg);
	}
	
	public void sendRules(Player player) {
		player.sendMessage(getRules(true));
	}
	
}
