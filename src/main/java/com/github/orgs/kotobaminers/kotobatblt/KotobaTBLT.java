package com.github.orgs.kotobaminers.kotobatblt;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.orgs.kotobaminers.database.DatabaseManager;
import com.github.orgs.kotobaminers.kotobatblt.PluginCommandExecutor.PluginCommand;

public final class KotobaTBLT extends JavaPlugin {
    @Override
    public void onEnable() {
    	this.getCommand(PluginCommand.getRoot().name()).setExecutor(new PluginCommandExecutor(this));
    	getServer().getPluginManager().registerEvents(new Event(), this);
    	
    	PluginManager.initialize(this);
    	DatabaseManager.openConnection();
    	DatabaseManager.importSentence();

		HologramsManager.initializeAllHologram();

    }
    
    @Override
    public void onDisable() {
    	DatabaseManager.closeConnection();
    }
}
