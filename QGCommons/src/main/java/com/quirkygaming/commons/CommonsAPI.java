package com.quirkygaming.commons;

import static com.quirkygaming.reflectionlib.AnonymousClass.classExceptionHandler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.quirkygaming.commons.GreeterRegister.GreeterPriority;
import com.quirkygaming.commons.listeners.GreeterListener;
import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.reflectionlib.AnonymousClass;
import com.quirkygaming.reflectionlib.AnonymousObject;

public class CommonsAPI {
	
	public static void chatPromptLater(final CommandSender player, final Runnable callback, final MutableProperty<String> response, final MutableProperty<Boolean> canceled, final long timeoutTicks) {
		Bukkit.getScheduler().runTask(QGCommonsPlugin.instance, new Runnable() {
			public void run() {
				chatPrompt(player, callback, response, canceled, timeoutTicks);
			}
		});
	}
	
	// Returns false if player is already waiting
	public static boolean chatPrompt(final CommandSender player, final Runnable callback,final MutableProperty<String> response, final MutableProperty<Boolean> canceled, long timeoutTicks) {
		if (QGCommonsPlugin.waitingPrompts.containsKey(player.getName())) {
			return false;
		}
		
		final PromptDeclaration p = new PromptDeclaration(callback, response, canceled, player);
		
		QGCommonsPlugin.waitingPrompts.put(player.getName(),p);
		
		Bukkit.getScheduler().runTaskLater(QGCommonsPlugin.instance, new Runnable() {public void run() {
			if (!p.fulfilled) {
				canceled.set(true);
				p.callback.run();
				QGCommonsPlugin.waitingPrompts.remove(player.getName());
			}
		}}, timeoutTicks);
		
		return true;
	}
	
	public static void subscribeToGreetingMessages(GreeterListener listener, GreeterPriority priority) {
		GreeterRegister.addListener(listener, priority);
	}

	public static void subscribeToGreetingMessages(GreeterListener listener) {
		GreeterRegister.addListener(listener, GreeterPriority.MIDDLE);
	}
	
	public static void teleportWithMount(final Player p, final Location l) {
		if (p.isInsideVehicle()) {
			final Entity vehicle = p.getVehicle();
			vehicle.eject();
			Util.loadChunks(l, 3);
			vehicle.teleport(l);
			
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
					QGCommonsPlugin.instance, 
						new Runnable() {
							public void run() {
								p.teleport(l);
							}
						}, 
					2);
			
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
					QGCommonsPlugin.instance, 
						new Runnable() {
							public void run() {
								vehicle.setPassenger(p);
							}
						}, 
					4);
			
			
		} else {
			p.teleport(l);
		}
	}

	// Code adapted from BKCommonLib
	public static Location findSpawnLocation(Entity dummyEntity, Location startLocation, boolean createPortals) {
		AnonymousClass agentClass = Util.loadCraftBukkitClass("CraftTravelAgent");
		
		AnonymousObject nmsWorld = AnonymousObject.fromObject(startLocation.getWorld()).
				invoke("getHandle");
		
		AnonymousObject agent = agentClass.instantiate(nmsWorld);
		
		agent.invoke("setCanCreatePortal", createPortals);
		
		Location exit = agent.invoke("findOrCreate", startLocation);
		
		AnonymousObject nmsDummyEntity = AnonymousObject.fromObject(dummyEntity).
				invoke("getHandle");
		
		try {
			agent.invoke(classExceptionHandler(NullPointerException.class), "adjustExit", 
					nmsDummyEntity,
					exit,
					new Vector(0, 0, 0));
		} catch (Exception npe) {
			// NullPointerException; ignore
		}
		
		
		/*CraftTravelAgent travelAgent = new CraftTravelAgent(((CraftWorld)startLocation.getWorld()).getHandle());
		travelAgent.setCanCreatePortal(createPortals);
		Location exit = travelAgent.findOrCreate(startLocation);
		try {
			travelAgent.adjustExit(
					((CraftEntity) dummyEntity).getHandle(),
					exit, 
					new Vector(0, 0, 0)
					);
		} catch (NullPointerException ex) {} //Ignore */

		return exit;
	}

}

class PromptDeclaration {
	Runnable callback;
	MutableProperty<String> response;
	MutableProperty<Boolean> canceled;
	CommandSender sender;
	boolean fulfilled = false;
	public PromptDeclaration(Runnable callback, MutableProperty<String> response, MutableProperty<Boolean> canceled, CommandSender sender) {
		this.callback = callback;
		this.response = response;
		this.canceled = canceled;
		this.sender = sender;
	}
}
