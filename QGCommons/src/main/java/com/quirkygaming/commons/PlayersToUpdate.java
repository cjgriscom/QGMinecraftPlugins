package com.quirkygaming.commons;

import java.util.TreeMap;
import java.util.UUID;

public class PlayersToUpdate {

	public static final TreeMap<UUID, UUID> list = new TreeMap<>();
	
	// Vallamont
	static {list.put(UUID.fromString("ce292582-f783-4eca-8237-b5ae3015bd1e"), 
			UUID.fromString("243c2827-140a-40f9-9c09-005abf47626e"));}
}
