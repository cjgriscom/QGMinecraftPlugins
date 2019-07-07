package com.quirkygaming.qgregions.structures;

/**
 * The order of these entries is critical; earlier ordinals will be checked first in
 * the theoretical logical AND expression that evaluates whether a location is within
 * a region or not. Outline must be last because it is resource-intensive and may 
 * often be unneeded.
 *    
 * @author chandler
 *
 */
public enum BoundaryType {
	WORLD(WorldBoundary.class), HEIGHT(HeightBoundary.class), OUTLINE(OutlineBoundary.class);
	
	Class<? extends Boundary> clazz;
	
	BoundaryType(Class<? extends Boundary> clazz) {
		this.clazz = clazz;
	}
}
