package com.github.orgs.kotobaminers.kotobatblt.kotobatblt;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.orgs.kotobaminers.kotobatblt.database.DatabaseManager;
import com.github.orgs.kotobaminers.kotobatblt.kotobatblt.PluginCommandExecutor.PluginCommand;
import com.github.orgs.kotobaminers.kotobatblt.userinterface.HologramsManager;

public final class KotobaTBLT extends JavaPlugin {
	@Override
	public void onEnable() {
		this.getCommand(PluginCommand.getRoot().name()).setExecutor(new PluginCommandExecutor(this));
		getServer().getPluginManager().registerEvents(new PluginEvent(), this);
		
		PluginManager.initialize(this);
		DatabaseManager.loadConfig();
		DatabaseManager.openConnection();
		
		HologramsManager.removeAllHologram();
	}
	
	@Override
	public void onDisable() {
		DatabaseManager.closeConnection();
	}
}
