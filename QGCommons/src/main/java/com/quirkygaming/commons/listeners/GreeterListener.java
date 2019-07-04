package com.quirkygaming.commons.listeners;

import org.bukkit.entity.Player;

public interface GreeterListener extends CommonsExtension {
	public void sendGreeting(Player p);

}
