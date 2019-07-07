package com.quirkygaming.qgbooks.entries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.quirkygaming.qgbooks.config.Database;

public class PrivateMessage extends GenericEntry implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private UUID recipientID = null;
	
	public PrivateMessage(long ID, String body, UUID sender, TimeData timeData, UUID recipient) {
		super(ID, body, sender, timeData);
		this.recipientID = recipient;
	}
	
	public String recipientName() {
		return Database.nameOf(recipientID);
	}

	@Override
	public List<UUID> recipients() {
		return Collections.singletonList(recipientID);
	}

	@Override
	public List<UUID> senderAndRecipients() {
		List<UUID> l = new ArrayList<UUID>();
		l.add(senderID);
		l.add(recipientID);
		return l;
	}

	@Override
	public boolean isBulletin() {
		return false;
	}

	@Override
	public String getPage(UUID bookOwner) {
		String page;
		
		page = "Message ID: " + ID() + lineFeed;
		page = page.concat("§o" + date() + ", " + time() + lineFeed);
		if (senderID().equals(bookOwner)) {
			page = page.concat("§r§l" + "PM from you to " + recipientName() + lineFeed);	
		} else if (recipientID.equals(bookOwner)) {
			page = page.concat("§r§l" + "PM from " + senderName() + lineFeed);
		}
		page = page.concat("§r" + body());
		
		return page;
	}

	@Override
	public void replaceUUID(UUID oldU, UUID newU) {
		if (senderID.equals(oldU)) senderID = newU;
		if (recipientID.equals(oldU)) recipientID = newU;
	}
	
}
