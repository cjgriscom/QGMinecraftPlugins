package com.quirkygaming.qgcustoms.modules;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.quirkygaming.commons.CommonsAPI;
import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

import net.md_5.bungee.api.ChatColor;

public class PromptTestModule extends CustomModule {

	public PromptTestModule(QGCustomsPlugin plugin) {
		super("Prompts", new Listener[]{}, new String[]{"qgtest"}, plugin);
	}

	@Override
	public boolean onEnable() {
		return true;
	}

	@Override
	public boolean onDisable() {
		return true;
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String name, String[] args) {
		if (name.equalsIgnoreCase("qgtest")) {
			final MutableProperty<String> response1 = MutableProperty.newProperty("");
			final MutableProperty<Boolean> cancel1 = MutableProperty.newProperty(false);
			
			sender.sendMessage(ChatColor.GOLD + "Select a command:");
			sender.sendMessage(ChatColor.GREEN + "  WarpToLobby " + ChatColor.WHITE + "(operator permission)");
			sender.sendMessage(ChatColor.BLUE +  "  LobbyDebug");
			
			CommonsAPI.chatPrompt(sender, new Runnable() {
				@Override
				public void run() {
					if (response1.get().equalsIgnoreCase("WarpToLobby")) simulateRespawn(sender);
					else if (response1.get().equalsIgnoreCase("LobbyDebug")) LobbyModule.lobbyDebug(sender);
					else sender.sendMessage("QGTest timeout for " + sender.getName());
				}
				
			}, response1, cancel1, 20*15);
			return true;
		}
		return false;
	}
	
	public void simulateRespawn(CommandSender sender) {
		if (sender instanceof Player && sender.isOp()) {
			((Player) sender).teleport(LobbyModule.LOBBY);
		} else {
			sender.sendMessage(ChatColor.RED + "You don't have permission");
		}
	}
}
