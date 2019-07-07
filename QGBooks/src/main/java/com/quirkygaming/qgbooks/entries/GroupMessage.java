package com.quirkygaming.qgbooks.entries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GroupMessage extends GenericEntry implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected Set<UUID> recipientIDs = null;
	
	public GroupMessage(long ID, String body, UUID sender, TimeData timeData, Set<UUID> recipients) {
		super(ID, body, sender, timeData);
		this.recipientIDs = recipients;
	}
	
	public GroupMessage(long ID, String body, UUID sender, TimeData timeData, Collection<UUID> recipients) {
		super(ID, body, sender, timeData);
		this.recipientIDs = new HashSet<UUID>(recipients);
	}

	@Override
	public List<UUID> recipients() {
		return new ArrayList<UUID>(recipientIDs);
	}

	@Override
	public List<UUID> senderAndRecipients() {
		ArrayList<UUID> newList = new ArrayList<UUID>(recipientIDs);
		newList.add(0, senderID);
		return newList;
	}

	@Override
	public boolean isBulletin() {
		return false;
	}

	@Override
	public String getPage(UUID bookOwner) {
		return "GroupMessage page generator stub";
	}

	@Override
	public void replaceUUID(UUID oldU, UUID newU) {
		if (senderID.equals(oldU)) senderID = newU;
		if (recipientIDs.contains(oldU)) {
			recipientIDs.remove(oldU);
			recipientIDs.add(newU);
		}
	}

}
