package com.quirkygaming.commons.heavyloader;

import java.util.LinkedHashSet;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.world.ChunkLoadEvent;

/**
 * This class slowly loads a list of "HeavyLoaders", classes that need to load
 * large files asynchronously without interrupting the server thread.
 * 
 * This API is intended for plugins like QGRegions that need several large files to
 * be loaded into memory, but can do without them until they are needed. Implement
 * HeavyLoader in a class that needs such files, then call hook(...) to queue the slow
 * loading, and call forceAndWaitForLoad(...) if the file is needed immediately.
 * 
 * @author chandler
 *
 */
public class HeavyLoading implements Listener {
	
	// Listener is registered by QGCommons
	
	private static int PAUSE_MS = 1000;
	
	private static boolean startHeavyLoading = false;
	private static boolean forceQuit = false;
	
	private static LinkedHashSet<HeavyLoader> hooked = new LinkedHashSet<HeavyLoader>();
	
	private static final int PROCESS = 0;
	private static final int HOOK = 1;
	
	private static Thread loaderThread;
	
	private static synchronized void process(int action, HeavyLoader item) {
		if (action == PROCESS) {
			if (!hooked.isEmpty()) {
				if (item == null) item = hooked.iterator().next();
				if (!hooked.contains(item)) return; // Force and wait might try to redo items
				item.doAsyncLoading();
				hooked.remove(item);
				//System.out.println("DEBUG: processed " + item.toString());
			}
		} else if (action == HOOK) {
			hooked.add(item);
		}
	}
	
	/**
	 * This method adds a HeavyLoader to the queue
	 * @param item
	 */
	public static void hook(HeavyLoader item) {
		process(HOOK, item);
	}
	
	/**
	 * Run this method if an item needs to be processed immediately. 
	 * It returns silently in all other cases (i.e. already processes, never hooked).
	 * @param item
	 */
	public static void forceAndWaitForLoad(HeavyLoader item) {
		//System.out.println("DEBUG: Force and Wait waiting for: " + item);
		process(PROCESS, item);
		//System.out.println("DEBUG: Force and Wait completed for: " + item);
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		startHeavyLoading();
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) startHeavyLoading();
	}
	
	// This method is called by the plugin to kill the loader thread
	public void onPluginUnload() {
		if (!forceQuit && loaderThread != null) {
			forceQuit = true;
			loaderThread.interrupt();
			try {
				loaderThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void startHeavyLoading() {
		if (!startHeavyLoading) {
			startHeavyLoading = true;
			loaderThread = new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println("[QGCommons] Began processing HeavyLoading hooks");
					while (! forceQuit) {
						process(PROCESS, null);
						try {
							Thread.sleep(PAUSE_MS);
						} catch (InterruptedException e) {
							System.out.println("[QGCommons] HeavyLoading thread interrupted");
						}
					}
				}
			});
			loaderThread.start();
		}
	}
}