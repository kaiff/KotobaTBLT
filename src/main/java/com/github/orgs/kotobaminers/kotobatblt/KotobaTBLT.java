package com.github.orgs.kotobaminers.kotobatblt;

import java.util.stream.Stream;

import org.bukkit.plugin.java.JavaPlugin;

public final class KotobaTBLT extends JavaPlugin {
    @Override
    public void onEnable() {
    	Stream.of(PluginCommandExecutor.Commands.values())
    		.forEach(command -> this.getCommand(command.name()).setExecutor(new PluginCommandExecutor(this)));
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
