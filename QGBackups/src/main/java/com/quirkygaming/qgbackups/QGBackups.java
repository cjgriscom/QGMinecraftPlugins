package com.quirkygaming.qgbackups;

import static com.quirkygaming.qgbackups.Task.Timing.ALWAYS;
import static com.quirkygaming.qgbackups.Task.Timing.PERIODIC;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.propertydb.PropertyDB;
import com.quirkygaming.propertylib.MutableProperty;

public class QGBackups extends JavaPlugin {
	
	private static boolean countdown_running = false;
	
	MutableProperty<HashMap<String, Task>> tasks;
	MutableProperty<HashMap<String, TimeSlot>> timeslots;
	
	private HashSet<Task> taskQueue = new HashSet<>();
	
	void queueTask(Task t) {
		taskQueue.add(t);
		setTaskListAndDefaults(taskQueue);
	}
	
	private void setDefaultTaskFile() {
		HashSet<Task> slatedRuns = new HashSet<>();
		
		for (Task t : tasks.get().values()) {
			if (t.timingType.get() == ALWAYS) {
				slatedRuns.add(t);
			}
		}
		setTaskFile(slatedRuns);
	}
	
	private void setTaskFile(HashSet<Task> slatedRuns) {
		File file = new File(getDataFolder(), "slatedcommands.sh");
		if (file.exists()) file.delete();
		try {
			FileOutputStream fos = new FileOutputStream(file);
			PrintStream ps = new PrintStream(fos);
			for (Task t : slatedRuns) {
				ps.println("echo 'Executing " + t.name + "'");
				ps.println(t.scriptEntry());
			}
			ps.close();
			fos.close();
		} catch (IOException e) {
			getLogger().warning("Error writing slatedcommands.sh");
			e.printStackTrace();
		}
		
		try {
			final Process p = Runtime.getRuntime().exec("chmod +x " + file.getAbsolutePath());
			
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				getLogger().warning("Error setting permissions for slatedcommands.sh");
				e.printStackTrace();
			}
			getLogger().info("Finished writing slatedcommands.sh");
			p.destroy();
		} catch (IOException e) {
			getLogger().warning("Error setting permissions for slatedcommands.sh");
			e.printStackTrace();
		}
	}
	
	private void setTaskListAndDefaults(HashSet<Task> slatedRuns) {
		slatedRuns.addAll(taskQueue);
		
		for (Task t : tasks.get().values()) {
			if (t.timingType.get() == ALWAYS) {
				slatedRuns.add(t);
			}
		}
		setTaskFile(slatedRuns);
	}
	
	private void restart() {
		Bukkit.spigot().restart();
	}
	
	private void broadcast(String msg) {
		getServer().broadcastMessage(msg);
	}
	
	@Override
	public void onEnable() {
		
		getDataFolder().mkdir();
		
		ErrorHandler<RuntimeException> erh = ErrorHandler.logAll(getLogger(),true);
		
		tasks = PropertyDB.initiateProperty(getDataFolder(),
				"tasks", 1, new HashMap<String, Task>(), erh);
		timeslots = PropertyDB.initiateProperty(getDataFolder(),
				"timeslots", 1, new HashMap<String, TimeSlot>(), erh);
		
		// Initialize timeslots
		for (TimeSlot t : timeslots.get().values()) t.init();
		
		QGCommandExecutor executor = new QGCommandExecutor(this);
		getCommand("queuetask").setExecutor(executor);
		getCommand("definetask").setExecutor(executor);
		getCommand("definetimeslot").setExecutor(executor);
		getCommand("deletetask").setExecutor(executor);
		getCommand("deletetimeslot").setExecutor(executor);
		getCommand("listtasks").setExecutor(executor);
		getCommand("listtimeslots").setExecutor(executor);
		
		setDefaultTaskFile();
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				final HashSet<Task> slatedRuns = new HashSet<Task>();
				TimeSlot ts = null;
				Calendar rightNow = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
				int minute = rightNow.get(Calendar.MINUTE);
				
				if (minute > 2) return; // Past ideal time
				
				int hour = rightNow.get(Calendar.HOUR_OF_DAY);
				
				for (Task t : tasks.get().values()) {
					if (t.timingType.get() == PERIODIC) {
						//if (t.time.get().satisfied_hour == hour) continue; // Already done
						if (countdown_running) continue;
						
						if (Math.abs(hour - t.time.get().hour.get()) % t.time.get().hour_period.get() == 0) {
							slatedRuns.add(t);
							ts = t.time.get();
						}
					}
				}
				
				if (ts != null && slatedRuns.size() > 0) {
					final TimeSlot timeslot = ts;
					//timeslot.satisfied_hour = hour;
					countdown_running = true;
					final long doomsday = timeslot.warning_start_minutes.get() * 60 + (System.currentTimeMillis()/1000);
					
					Bukkit.getScheduler().runTaskLater(QGBackups.this, new Runnable() {
						public void run() {broadcast(ChatColor.GOLD + "Restarting server..."); 
											setTaskListAndDefaults(slatedRuns); restart();}
						
					}, timeslot.warning_start_minutes.get()*60*20);
					
					Bukkit.getScheduler().scheduleSyncRepeatingTask(QGBackups.this, new Runnable() {
						public void run() {
							long time = (doomsday - System.currentTimeMillis()/1000);
							if (time <= 0) return;
							broadcast(ChatColor.RED + timeslot.warning_message_prefix.get().trim() + " " + 
									time + " seconds");
						}
						
					}, 2, timeslot.warning_period_seconds.get()*20);
				}
				
			}
		}, 20*10, 20*60);
			
		
	}
	
	@Override
	public void onDisable(){ 
		
	}
	
	
}
