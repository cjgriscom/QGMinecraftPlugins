package com.quirkygaming.qgcustoms;

import java.io.IOException;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.command.CommandExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.quirkygaming.qgcustoms.CustomModule.QGCustomsModule;

@QGCustomsModule
public abstract class CustomModule implements CommandExecutor {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	public static @interface QGCustomsModule {}
	
	protected String name;
	private static HashMap<String, CustomModule> modules = new HashMap<String, CustomModule>();
	public static QGCustomsPlugin plugin;
	
	boolean loaded = false;
	boolean failed = false;
	
	static void unloadModules() {
		modules.clear();
	}
	
	static HashMap<String, CustomModule> loadModules(QGCustomsPlugin plugin) {
		modules.clear();
		//ManagerModule mm = new ManagerModule(plugin);
		
		URL jar = QGCustomsPlugin.class.getProtectionDomain().getCodeSource().getLocation();
		try {
			JarFile jarF = new JarFile(jar.getFile());
			Enumeration<JarEntry> entries = jarF.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.endsWith("class") && !name.contains("$") ) {
					try {
						Class<?> clazz = Class.forName(name.replace(".class", "").replace("/", "."));
						if (clazz.getAnnotation(QGCustomsModule.class) != null && 
								!Modifier.isAbstract(clazz.getModifiers())) {
							CustomModule module = (CustomModule) clazz.getConstructors()[0].newInstance(plugin);
							modules.put(module.name(), module);
						}
					} catch (ClassCastException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
						plugin.getLogger().severe("Construction or reflection error for " + name);
						e.printStackTrace();
					}
				}
			}
			
			jarF.close();
		} catch (IOException e) {
			plugin.getLogger().warning("Loading modules failed; " + e.getMessage());
		}
		plugin.getLogger().info("Discovered " + modules.size() + " modules.");
		
		// Make sure database is initialized
		((ManagerModule)modules.get("Manager")).setKnownModules(modules);
		
		return modules;
	}
	
	Listener[] listeners; String[] commands;
	public CustomModule(String name, Listener[] listeners, String[] commands, QGCustomsPlugin plugin) {
		this.name = name;
		this.listeners = listeners;
		this.commands = commands;
		CustomModule.plugin = plugin;
	}
	
	void enableEvents() {
		for (String s : commands) plugin.getCommand(s).setExecutor(this);
		for (Listener l : listeners) plugin.getServer().getPluginManager().registerEvents(l, plugin);
	}
	
	void disableEvents() {
		for (String s : commands) plugin.getCommand(s).setExecutor(null);
		for (Listener l : listeners) HandlerList.unregisterAll(l);
	}
	
	public abstract boolean onEnable();
	public abstract boolean onDisable();
	
	public String name() {
		return name;
	}
	
	public boolean loaded()  {return loaded; }
	public boolean enabled() {return ManagerModule.isEnabled(name());}
	public boolean failed()  {return failed; }
}
