package com.quirkygaming.qgcustoms.modules;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.quirkygaming.commons.playerinterface.PlayerInfo;
import com.quirkygaming.qgcustoms.CustomModule;
import com.quirkygaming.qgcustoms.QGCustomsPlugin;

public class BuzzerModule extends CustomModule {

	public BuzzerModule(QGCustomsPlugin plugin) {
		super("Buzzer", new Listener[]{new BListener()}, new String[]{}, plugin);
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
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
	
	public static class BListener implements Listener {

		@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
		public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
			if (!event.getMessage().startsWith("/")) {
				new Buzzer(event.getMessage()).runTaskLater(plugin, 1);
			}
		}

		@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
		public void onServerMessage(ServerCommandEvent event) {
			if (event.getCommand().toLowerCase().startsWith("say ")) {
				new Buzzer(event.getCommand().substring(3)).runTaskLater(plugin, 1);
			}
		}
	}
	

	public static class Buzzer extends BukkitRunnable {

		String[] msgtext;

		public Buzzer(String msgtext) {
			this.msgtext = msgtext.split("\\s+");
		}

		public void run() {
			for (String m : msgtext) {
				m = m.replaceAll("[:,.^?!/]", "");
				PlayerInfo pInfo = new PlayerInfo(m);	// strip out punctuation
														// from each keyword
				Player p = pInfo.player();

				if (p != null) {
					if ((m.length() > 4 && m.length() < 17)
							|| p.getName().equalsIgnoreCase(m)) {
						p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1000,
								.5f);
					}
				}
			}

		}

	}

	
}
