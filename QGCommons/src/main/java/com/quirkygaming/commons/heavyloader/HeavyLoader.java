package com.quirkygaming.commons.heavyloader;

public interface HeavyLoader {
	/**
	 * This method could be called from another thread, make sure proper thread
	 * safety is in place!
	 * 
	 * Implementors should never call this method; could cause thread overlap!!
	 * Call HeavyLoading.forceAndWaitForLoad(this) instead.
	 */
	public void doAsyncLoading();
}
