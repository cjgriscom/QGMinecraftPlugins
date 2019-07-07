package com.quirkygaming.qgwebsocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.TreeSet;

import org.bukkit.plugin.java.JavaPlugin;

import com.quirkygaming.qgplayermanager.PlayerData;
import com.quirkygaming.qgplayermanager.QGPlayerManager;
import com.quirkygaming.qgregions.QGRegions;
import com.quirkygaming.qgregions.structures.Region;
import com.quirkygaming.qgregions.structures.RegionField;
import com.quirkygaming.qgregions.structures.RegionOpt;

public class QGSocket extends JavaPlugin {
	
	ServerSocket serverSocket;
	Thread dispatcher;
	static final int port = 25567;
	
	@Override
	public void onEnable() {
		try {
			
			serverSocket = new ServerSocket(port);
			getLogger().info("Started socket on port " + port);
			dispatcher = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while(QGSocket.this.isEnabled()) {
							final Socket clientSocket = serverSocket.accept(); // Wait for connection then fork
							new Thread(new Runnable() {public void run(){dispatch(clientSocket);}}).start();
						}
					} catch (SocketException e) {
						return; // Server is closing
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			dispatcher.start();

		} catch (IOException e) {
			e.printStackTrace();
			getLogger().info("Failed to initiate socket");
			getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	@Override
	public void onDisable(){try {serverSocket.close();} catch (IOException e) {}}
	
	// Runs on its own thread
	void dispatch(Socket clientSocket) {
		try {
			
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String request = null;
			while (true) {
				long maxTime = System.currentTimeMillis() + 500;
				while ((request = in.readLine()) == null) { // Terminate on newline
					if (System.currentTimeMillis() > maxTime) break; // Break after 500ms
				}
				if (request == null) {
					break;
				} else if (request.equals("regions_governed")) {
					for (Region r : QGRegions.getRegions()) {
						if (r.isOptionSet(RegionOpt.unlisted)) continue;
						if (r.getField(RegionField.note).isEmpty()) continue;
						out.print("<li><strong>" + r.getField(RegionField.name) + ": </strong>");
						out.print(r.getField(RegionField.note));
						//out.print(" - " + r.fill.size() + " m&sup2 ");
						if (r.isOptionSet(RegionOpt.creeper_protected)) out.print(" - Creeper Protection");
						out.print("</li>");
					}
					out.println();
				} else if (request.equals("regions_open")) {
					for (Region r : QGRegions.getRegions()) {
						if (r.isOptionSet(RegionOpt.unlisted)) continue;
						if (!r.getField(RegionField.note).isEmpty()) continue;
						out.print("<li><strong>" + r.getField(RegionField.name) + "</strong></li>");
					}
					out.println();
				} else if (request.equals("players")) {
					///Sort
					TreeSet<PlayerData> dataSet = new TreeSet<PlayerData>();
					dataSet.addAll(QGPlayerManager.getAllPlayers());
					for (PlayerData p : dataSet) {
						if (!p.canConnect()) continue; // Banned or not whitelisted
						String boldS = p.moderator.get() ? "<strong>" : "";
						String boldE = p.moderator.get() ? "</strong>" : "";
						String redS = p.getPlayer().isOnline() ? "<span style='color:red'>" : "";
						String redE = p.getPlayer().isOnline() ? "</span>" : "";
						String realName = p.realName.equals("Unknown Name") ? "" : " (" + p.realName.get() + ")";
						out.print("<li>" + redS + boldS + p.getName() + boldE + redE+ realName + "</li>");
					}
					out.println();
				} else if (request.equals("connected")) {
					out.println(getServer().getOnlinePlayers().size() + "/" + getServer().getMaxPlayers());
				} else if (request.equals("version")) {
					out.println(getServer().getVersion());
				} else if (request.equals("stop") || request.equals("reload") || request.equals("restart")) {
					out.println("Executing...");
					getServer().dispatchCommand(getServer().getConsoleSender(), request);
					break;
				} else if (request.startsWith("say_")) {
					out.println("Executed.");
					getServer().dispatchCommand(getServer().getConsoleSender(), request.replaceAll("_"," "));
				} else if (request.startsWith("kick_") || request.startsWith("whitelist_") || request.startsWith("ban_") || request.startsWith("pardon_") || request.startsWith("ban_")) {
					out.println("Executed.");
					getServer().dispatchCommand(getServer().getConsoleSender(), request.replaceAll("_"," "));
				} else if (request.equals("end")) {
					break;
				} else {
					break;
				}
			}
			
			out.close();
			in.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			getLogger().info("Caught IOException while dispatching socket thread");
		}
		
	}
	
}
