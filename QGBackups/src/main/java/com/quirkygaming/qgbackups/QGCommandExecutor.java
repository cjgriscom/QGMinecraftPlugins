package com.quirkygaming.qgbackups;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.quirkygaming.commons.Prompt;
import com.quirkygaming.commons.PromptQueue;
import com.quirkygaming.qgbackups.Task.TaskType;
import com.quirkygaming.qgbackups.Task.Timing;

public class QGCommandExecutor implements CommandExecutor {
	 
	private QGBackups plugin;
 
	public QGCommandExecutor(QGBackups plugin) {
		this.plugin = plugin;
	}
	
	private <T extends Enum<T>> T getEnum(String name, Class<T> enumclass) {
		try {
			return Enum.valueOf(enumclass, name);
		} catch (java.lang.IllegalArgumentException e) {
			return null;
		}
	}
	
	public void listTimeSlots(CommandSender sender) {
		for (TimeSlot t : plugin.timeslots.get().values()) {
			sender.sendMessage(ChatColor.GOLD +""+ ChatColor.BOLD + t.name + ":");
			sender.sendMessage("  " + ChatColor.BLUE + "Hour: " + t.hour + " GMT");
			sender.sendMessage("  " + ChatColor.BLUE + "Hour Period: " + t.hour_period);
		}
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("queuetask")) {
			PromptQueue prompts = new PromptQueue("CANCEL");
			
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Enter name of task: (type CANCEL at any time to cancel)");
					return true;
				} public void onResponse(String msg) {
					boolean success = plugin.tasks.get().containsKey(msg.trim());
					if (!success) {
						sender.sendMessage(ChatColor.RED + "Could not find \"" + msg.trim() + "\".");
					} else {
						plugin.queueTask(plugin.tasks.get().get(msg.trim()));
						sender.sendMessage(ChatColor.GOLD + "Scheduled.");
					}
				}
			};
			prompts.startChain();
			return true;
		} else if (cmd.getName().equalsIgnoreCase("definetask")) {
			PromptQueue prompts = new PromptQueue("CANCEL");
			final Task t = new Task();
			
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Enter name of task: (type CANCEL at any time to cancel)");
					return true;
				} public void onResponse(String msg) {
					t.name.set(msg.trim());
				}
			};
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Enter timing (PERIODIC, REQUEST, or ALWAYS)");
					return true;
				} public void onResponse(String msg) {
					Timing time = getEnum(msg.trim().toUpperCase(), Timing.class);
					if (time == null) {
						cancel(ChatColor.RED + "Invalid entry; canceling."); return;
					}
					t.timingType.set(time);
				}
			};
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					if (t.timingType.equals(Timing.PERIODIC)) {
						sender.sendMessage(ChatColor.GOLD + "Enter name of time slot:");
						listTimeSlots(sender);
						return true;
					}
					return false;
				} public void onResponse(String msg) {
					TimeSlot time = plugin.timeslots.get().get(msg.trim());
					if (time == null) {
						cancel(ChatColor.RED + "Invalid entry; canceling."); return;
					}
					t.time.set(time);
				}
			};
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Enter action type (SCRIPT, CLONE_DIR, or RESTART)");
					return true;
				} public void onResponse(String msg) {
					TaskType type = getEnum(msg.trim().toUpperCase(), TaskType.class);
					if (type == null) {
						cancel(ChatColor.RED + "Invalid entry; canceling."); return;
					}
					t.task.set(type);
				}
			};
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					if (t.task.equals(TaskType.SCRIPT)) {
						sender.sendMessage(ChatColor.GOLD + "Enter full script command:");
						return true;
					} else if (t.task.equals(TaskType.CLONE_DIR)) {
						sender.sendMessage(ChatColor.GOLD + "Enter source directory:");
						return true;
					}
					return false;
				} public void onResponse(String msg) {
					t.item1.set(msg.trim());
				}
			};
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					if (t.task.equals(TaskType.CLONE_DIR)) {
						sender.sendMessage(ChatColor.GOLD + "Enter destination directory:");
						return true;
					}
					return false;
				} public void onResponse(String msg) {
					t.item2.set(msg.trim());
				}
			};
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Added " + t.name);
					plugin.tasks.get().put(t.name.get(), t);
					plugin.tasks.update();
					return false;
				} public void onResponse(String msg) {}
			};
			
			prompts.startChain();
			
			
			return true;
			
			
		} else if (cmd.getName().equalsIgnoreCase("definetimeslot")) {
			PromptQueue prompts = new PromptQueue("CANCEL");
			final TimeSlot t = new TimeSlot();
			
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Enter name of time slot: (type CANCEL at any time to cancel)");
					return true;
				} public void onResponse(String msg) {
					t.name.set(msg.trim());
				}
			};
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Enter the base hour in GMT: (type CANCEL at any time to cancel)");
					return true;
				} public void onResponse(String msg) {
					try {
						t.hour.set(Integer.parseInt(msg));
					} catch (NumberFormatException e) {
						cancel(ChatColor.RED + "Could not parse integer");
					}
				}
			};
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Enter the hour interval: (type CANCEL at any time to cancel)");
					return true;
				} public void onResponse(String msg) {
					try {
						t.hour_period.set(Integer.parseInt(msg));
					} catch (NumberFormatException e) {
						cancel(ChatColor.RED + "Could not parse integer");
					}
				}
			};
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Enter the number of minutes before notification: (type CANCEL at any time to cancel)");
					return true;
				} public void onResponse(String msg) {
					try {
						t.warning_start_minutes.set(Integer.parseInt(msg));
					} catch (NumberFormatException e) {
						cancel(ChatColor.RED + "Could not parse integer");
					}
				}
			};
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Enter the warning notification period in seconds: (type CANCEL at any time to cancel)");
					return true;
				} public void onResponse(String msg) {
					try {
						t.warning_period_seconds.set(Integer.parseInt(msg));
					} catch (NumberFormatException e) {
						cancel(ChatColor.RED + "Could not parse integer");
					}
				}
			};
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Enter prefix to the warning message: (type CANCEL at any time to cancel)");
					return true;
				} public void onResponse(String msg) {
					t.warning_message_prefix.set(msg.trim());
				}
			};
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Added " + t.name);
					plugin.timeslots.get().put(t.name.get(), t);
					plugin.timeslots.update();
					return false;
				} public void onResponse(String msg) {}
			};
			
			prompts.startChain();
			return true;
		} else if (cmd.getName().equalsIgnoreCase("deletetask")) {
			PromptQueue prompts = new PromptQueue("CANCEL");
			
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Enter name of task: (type CANCEL at any time to cancel)");
					return true;
				} public void onResponse(String msg) {
					Object success = plugin.tasks.get().remove(msg.trim());
					plugin.tasks.update();
					if (success == null) {
						sender.sendMessage(ChatColor.RED + "Could not find \"" + msg.trim() + "\".");
					} else {
						sender.sendMessage(ChatColor.GOLD + "Deleted.");
					}
				}
			};
			
			prompts.startChain();
			return true;
		} else if (cmd.getName().equalsIgnoreCase("deletetimeslot")) {
			PromptQueue prompts = new PromptQueue("CANCEL");
			
			new Prompt(prompts, sender) {
				public boolean conditionAndMessage() {
					sender.sendMessage(ChatColor.GOLD + "Enter name of slot: (type CANCEL at any time to cancel)");
					return true;
				} public void onResponse(String msg) {
					Object success = plugin.timeslots.get().remove(msg.trim());
					plugin.timeslots.update();
					if (success == null) {
						sender.sendMessage(ChatColor.RED + "Could not find \"" + msg.trim() + "\".");
					} else {
						sender.sendMessage(ChatColor.GOLD + "Deleted.");
					}
				}
			};
			
			prompts.startChain();
			return true;
		} else if (cmd.getName().equalsIgnoreCase("listtasks")) {
			for (Task t : plugin.tasks.get().values()) {
				sender.sendMessage(ChatColor.GOLD +""+ ChatColor.BOLD + t.name + ":");
				sender.sendMessage("  " + ChatColor.BLUE + "Type: " + t.timingType);
				if (t.time.get() != null) sender.sendMessage("  " + ChatColor.GREEN + "TimeSlot: " + t.time);
				sender.sendMessage("  " + ChatColor.BLUE + "Script: " + t.scriptEntry());
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("listtimeslots")) {
			listTimeSlots(sender);
			return true;
		}
		
		return false;
	}
	
}
