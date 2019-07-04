package com.quirkygaming.commons;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/*
 * A convenient API layer for constructing a chain of prompts
 */
public abstract class Prompt implements Runnable {
	public final CommandSender sender;
	private int timeoutTicks;
	public final PromptQueue queue;
	
	public Prompt(final PromptQueue queue, final CommandSender sender) {
		this(queue, sender, queue.defaultTimeoutTicks);
	}
	public Prompt(final PromptQueue queue, final CommandSender sender, int timeoutTicks) {
		this.sender = sender; this.queue = queue;
		queue.response.set(queue.cancelString);
		queue.canceled.set(false);
		this.timeoutTicks = timeoutTicks;
		queue.q.add(this);
	}
	
	public void cancel(String msg) {
		sender.sendMessage(msg);
		queue.canceled.set(true);
	}
	
	void link() {
		if (queue.canceled.get()) {
			queue.q.clear();
			return;
		}
		
		if (!conditionAndMessage()) { // If this doesn't want to run, continue.
			if (!queue.q.isEmpty()) queue.q.remove().link();
		} else {
			CommonsAPI.chatPromptLater(sender, this, queue.response, queue.canceled, timeoutTicks);
		}
	}
	
	public abstract boolean conditionAndMessage();
	public abstract void onResponse(String response);
	
	public void run() {
		if (queue.canceled.get() || queue.response.get().equalsIgnoreCase(queue.cancelString)) {
			sender.sendMessage(ChatColor.GOLD +"Canceled.");
			return;
		}
		onResponse(queue.response.get());
		
		// Run next
		if (!queue.q.isEmpty()) queue.q.remove().link();
	}
}
