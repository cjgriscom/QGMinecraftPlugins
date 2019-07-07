package com.quirkygaming.qgsigns.api;

import com.quirkygaming.qgsigns.QGSigns;

public class SignsAPI {
	
	public static void registerSignModule(SignModule module) {
		QGSigns.registerModule(module);
	}
	
	public static void unregisterSignModule(SignModule module) {
		QGSigns.unregisterModule(module);
	}
}
