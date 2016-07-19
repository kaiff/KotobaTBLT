package com.github.orgs.kotobaminers.kotobatblt;

public class PluginManager {
	private static KotobaTBLT plugin = null;
	
	private PluginManager() {
	}

	public static void initialize(KotobaTBLT plugin) {
		PluginManager.plugin = plugin;
	}
	
	public static KotobaTBLT getPlugin() {
		return plugin;
	}

}
