package com.quirkygaming.qgbooks.entries;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.quirkygaming.qgbooks.config.Database;

public abstract class GenericEntry implements CompatableEntry, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected long ID;
	protected UUID senderID = null;
	protected String body;
	protected TimeData timeData;
	
	protected static final String lineFeed = "\n";
	
	public GenericEntry(long ID, String body, UUID sender, TimeData timeData) {
		this.senderID = sender;
		this.body = body;
		this.timeData = timeData;
		this.ID = ID;
	}
	
	public String date() {
		return timeData.date();
	}
	public String time() {
		return timeData.time();
	}
	public long epochFormat() {
		return timeData.epochFormat();
	}
	public long ID() {
		return ID;
	}
	public String body() {
		return body;
	}
	public UUID senderID() {
		return senderID;
	}
	public String senderName() {
		return Database.nameOf(senderID());
	}
	@Override
	public boolean hasSenderOrRecipient(CommandSender p) {
		return this.senderAndRecipients().contains(Database.senderID(p));
	}
}
