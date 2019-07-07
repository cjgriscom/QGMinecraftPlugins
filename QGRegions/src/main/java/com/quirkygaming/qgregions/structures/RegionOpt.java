package com.quirkygaming.qgregions.structures;

import java.io.Serializable;

public enum RegionOpt implements Serializable {
	adventure,				// Enforce adventure mode
	creeper_protected,		// Disable creeper spawns and explosions
	disable_warp_in,		// Prevent QGWarps from setting warps
	disable_warp_out,		// Prevent QGWarps from warping out
	unlisted,				// Omit from lists such as QGWebSocket (external plugins only)
	disable_chorus,			// Prevent chorus fruit teleports
	disable_pearl_within,	// Prevent enderpearling within the region
	disable_pearl_in,		// Prevent enderpearling into the region
	disable_pearl_out,		// Prevent enderpearling out of the region
	nochat,					// Don't display the bracketed region name in chat
	block_sleep				// Prevent the player from sleeping in this region
}
