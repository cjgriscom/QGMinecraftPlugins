package com.quirkygaming.qgbooks.entries;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;

public interface CompatableEntry {
	public String date();
	public String time();
	public long epochFormat();
	public String body();
	public long ID();
	public List<UUID> recipients();
	public List<UUID> senderAndRecipients();
	public UUID senderID();
	public String senderName();
	public boolean isBulletin();
	public String getPage(UUID bookOwner);
	
	public void replaceUUID(UUID oldU, UUID newU);
	
	public boolean hasSenderOrRecipient(CommandSender player);
	//public List<String> pageUsers();
}
