package com.quirkygaming.commons.playerinterface;

import org.bukkit.event.Cancellable;
import org.bukkit.event.server.ServerCommandEvent;

public class ServerCommandEventCanceller implements Cancellable {
	
	ServerCommandEvent e;
	
	public ServerCommandEventCanceller(ServerCommandEvent e) {
		this.e = e;
	}
	
	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void setCancelled(boolean arg0) {
		e.setCommand("qg cancel");
	}

}
