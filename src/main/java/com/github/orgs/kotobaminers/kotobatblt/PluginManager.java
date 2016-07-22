package com.github.orgs.kotobaminers.kotobatblt;

import com.github.orgs.kotobaminers.userinterface.HologramsManager;

public class PluginManager {
	private static KotobaTBLT plugin = null;
	
	private PluginManager() {
	}

	public static void initialize(KotobaTBLT plugin) {
		PluginManager.plugin = plugin;
		HologramsManager.removeAllHologram();
	}
	
	public static KotobaTBLT getPlugin() {
		return plugin;
	}

}
