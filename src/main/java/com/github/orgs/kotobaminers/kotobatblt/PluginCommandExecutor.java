package com.github.orgs.kotobaminers.kotobatblt;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PluginCommandExecutor implements CommandExecutor {
	private final KotobaTBLT plugin;
	
	public PluginCommandExecutor (KotobaTBLT plugin) {
		this.plugin = plugin;
	}
	
	enum Commands {
		TBLT,
		TEST
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		KotobaTBLTTest.testAll();
		return false;
	}

}
