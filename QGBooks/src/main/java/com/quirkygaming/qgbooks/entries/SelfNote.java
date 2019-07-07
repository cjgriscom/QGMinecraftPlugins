package com.quirkygaming.qgbooks.entries;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SelfNote extends GenericEntry implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public SelfNote(long ID, String body, UUID sender, TimeData timeData) {
		super(ID, body, sender, timeData);
	}

	@Override
	public List<UUID> recipients() {
		return Collections.singletonList(senderID);
	}

	@Override
	public List<UUID> senderAndRecipients() {
		return recipients();
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
		page = page.concat("§r§l" + "Note to self: " + lineFeed);
		page = page.concat("§r" + body());
		return page;
	}

	@Override
	public void replaceUUID(UUID oldU, UUID newU) {
		if (senderID.equals(oldU)) senderID = newU;
	}
	
}
