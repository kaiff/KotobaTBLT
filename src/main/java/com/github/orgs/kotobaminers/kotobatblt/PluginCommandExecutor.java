package com.github.orgs.kotobaminers.kotobatblt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.orgs.kotobaminers.database.DatabaseManager;
import com.github.orgs.kotobaminers.database.ExternalQuery;
import com.github.orgs.kotobaminers.kotobatblt.PluginMessage.Message;

public class PluginCommandExecutor implements CommandExecutor {
	private final KotobaTBLT plugin;
	
	public PluginCommandExecutor (KotobaTBLT plugin) {
		this.plugin = plugin;
	}
	
	enum PluginPermission{
		PLAYER,
		OP,
		DEVELOPER;
		
		private static List<UUID> developers = Arrays.asList(UUID.fromString("de7bd32b-48a9-4aae-9afa-ef1de55f5bad"), UUID.fromString("ae6898a3-63f9-45b0-ac17-c8be45210d18"));

		static boolean hasPermission(PluginPermission permission, Player player) {
			switch (permission) {
			case DEVELOPER:
				if(developers.contains(player.getUniqueId())) return true;
				break;
			case OP:
				if (player.isOp()) return true;
				break;
			case PLAYER:
				return true;
			default:
			}

			return false;
		}
	}
	
	enum PluginCommand {
		TBLT(Arrays.asList("TBLT"), null, PluginPermission.PLAYER, "Root Commands"),
		DEVELOP(Arrays.asList("DEVELOP", "DEV"), TBLT, PluginPermission.DEVELOPER, "Developper's Commands"),
		TEST(Arrays.asList("TEST"), DEVELOP, PluginPermission.DEVELOPER, "Tests for Development"),
		DATABASE(Arrays.asList("DATABASE, DB"), DEVELOP, PluginPermission.DEVELOPER, "Database Commands"),
		UPDATE(Arrays.asList("UPDATE", "U"), DATABASE, PluginPermission.DEVELOPER, "Execute Query for MySQL");
		
		private List<String> names;
		private PluginCommand parent;
		private PluginPermission permission;
		private String usage;

		private PluginCommand(List<String> names, PluginCommand parent, PluginPermission permission, String usage) {
			this.names = names.stream().map(String::toUpperCase).collect(Collectors.toList());
			this.parent = parent;
			this.permission = permission;
			this.usage = usage;
		}
		
		public static PluginCommand getRoot() {
			return TBLT;
		}
		
		private List<PluginCommand> getCommandTree() {
			List<PluginCommand> list = new ArrayList<PluginCommand>();
			PluginCommand current = this;
			while(!current.equals(getRoot())) {
				list.add(current);
				current = current.parent;
			}
			list.add(getRoot());
			Collections.reverse(list);
			return list;
		}
		
		public List<String> takeArguments(String[] args) {
			List<String> arguments = new ArrayList<String>();
			int drop = getCommandTree().size() - 1;
			if(drop < args.length) {
				for(int i = drop; i < args.length; i++) {
					arguments.add(args[i]);
				}
			}
			return arguments;
		}

		private boolean hasPermission(Player player) {
			return PluginPermission.hasPermission(permission, player);
		}
		
		static List<PluginCommand> findCommands(Command command, String[] args) {
			List<String> path = new ArrayList<String>();
			path.add(command.getName().toUpperCase());
			path.addAll(Stream.of(args).map(String::toUpperCase).collect(Collectors.toList()));
			
			List<List<PluginCommand>> tree = Stream.of(PluginCommand.values())
				.map(com -> com.getCommandTree())
				.collect(Collectors.toList());
			
			for(int i = 0; i < path.size(); i++) {
				List<List<PluginCommand>> remain = new ArrayList<List<PluginCommand>>();
				for (List<PluginCommand> branch : tree) {
					if(i < branch.size()) {
						if(branch.get(i).names.contains(path.get(i))) {
							remain.add(branch);
						}
					}
				}
				if(0 < remain.size()) {
					tree = new ArrayList<List<PluginCommand>>();
					tree.addAll(remain);
				}
			}
			return tree.stream()
				.map(list -> {
					Collections.reverse(list);	
					return list.get(0);})
				.collect(Collectors.toList());
		}
		
		private String getUsage() {
			String title = String.join(" ", this.getCommandTree().stream()
				.map(com -> com.names.get(0).toLowerCase())
				.collect(Collectors.toList()));
			return "/" + title + " : " + this.usage;
		}
		
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			return true;
		}
		List<PluginCommand> commands = PluginCommand.findCommands(command, args);
		if(commands.size() == 0) {
			player.sendMessage(Message.INVALID.getMessage(null));
			return true;
		} else if(1 < commands.size()) {
			for (PluginCommand com : commands) {
				player.sendMessage(com.getUsage());
			}
			return true;
		}

		PluginCommand selected = commands.get(0);
		if(!selected.hasPermission(player)) {
			player.sendMessage(Message.NO_PERMISSION.getMessage(Arrays.asList(selected.name())));
			return true;
		}
		List<String> opts = selected.takeArguments(args);
		
		switch(selected) {
		case TBLT:
		case DEVELOP:
			return true;
		case UPDATE:
			if(0 < opts.size()) {
				DatabaseManager.executeUpdate(ExternalQuery.loadQuery(opts.get(0)));
			}
			return true;
		case TEST:
			KotobaTBLTTest.test();
			return true;
		default:
			break;
		}
		player.sendMessage(selected.getUsage());
		return true;
	}

}
