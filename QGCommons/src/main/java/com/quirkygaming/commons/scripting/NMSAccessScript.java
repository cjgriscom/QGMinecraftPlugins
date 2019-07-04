package com.quirkygaming.commons.scripting;

import java.io.InputStream;

import com.quirkygaming.commons.Util;
import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.scripting.SimpleScript;

public class NMSAccessScript<E> extends SimpleScript<E> {
	
	public static NMSAccessScript<Void> loadScript(String functionName, int nArgs, Class<?> jarHost, String filename) {
		return new NMSAccessScript<Void>(functionName, nArgs, jarHost.getClassLoader().getResourceAsStream(filename));
	}
	
	public static <T> NMSAccessScript<T> loadScript(String functionName, int nArgs, Class<T> returnType, Class<?> jarHost, String filename) {
		return new NMSAccessScript<T>(functionName, nArgs, jarHost.getClassLoader().getResourceAsStream(filename));
	}
	
	public static NMSAccessScript<Void> loadScript(String code, String functionName, int nArgs) {
		return new NMSAccessScript<Void>(functionName, nArgs, code);
	}
	
	public static <T> NMSAccessScript<T> loadScript(String code, String functionName, int nArgs, Class<T> returnType) {
		return new NMSAccessScript<T>(functionName, nArgs, code);
	}
	
	public NMSAccessScript(String functionName, int nArgs, InputStream jsFile) {
		this(functionName, nArgs, getCode(jsFile));
	}
	
	public NMSAccessScript(String functionName, int nArgs, String jsFile) {
		super(functionName, nArgs, jsFile);
		code = code.replaceAll("NMS.", "Packages." + Util.getNMSPackagePrefix());
		code = code.replaceAll("CraftBukkit.", "Packages." + Util.getCraftBukkitPackagePrefix());
		code = code.replaceAll("Bukkit.", "Packages.org.bukkit.");
	}
	
	private static String getCode(InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is);
		s.useDelimiter("\\A");
		String out = s.hasNext() ? s.next() : "";
		s.close();
		return out;
	}
	
	public E run(Object... args) {
		return run(ErrorHandler.throwAll(), args);
	}
	
}
