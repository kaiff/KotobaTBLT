package com.github.orgs.kotobaminers.kotobatblt.kotobatblt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.orgs.kotobaminers.kotobatblt.database.DatabaseManager;
import com.github.orgs.kotobaminers.kotobatblt.database.ExternalQuery;
import com.github.orgs.kotobaminers.kotobatblt.database.PlayerData;
import com.github.orgs.kotobaminers.kotobatblt.database.PlayerManager;
import com.github.orgs.kotobaminers.kotobatblt.database.SentenceManager;
import com.github.orgs.kotobaminers.kotobatblt.database.PlayerData.EditMode;
import com.github.orgs.kotobaminers.kotobatblt.database.PlayerData.PluginPermission;
import com.github.orgs.kotobaminers.kotobatblt.database.Sentence.Expression;
import com.github.orgs.kotobaminers.kotobatblt.userinterface.GUIIcon;
import com.github.orgs.kotobaminers.kotobatblt.userinterface.PluginGUI;
import com.github.orgs.kotobaminers.kotobatblt.userinterface.PluginGUI.GUITitle;
import com.github.orgs.kotobaminers.kotobatblt.userinterface.PluginMessage.Message;
import com.github.orgs.kotobaminers.kotobatblt.utility.PluginSound;

import net.md_5.bungee.api.ChatColor;

public class PluginCommandExecutor implements CommandExecutor {
	private final KotobaTBLT plugin;
	
	public PluginCommandExecutor (KotobaTBLT plugin) {
		this.plugin = plugin;
	}

	public enum PluginCommand {
		TBLT(Arrays.asList("TBLT"), Arrays.asList(), null, PluginPermission.PLAYER, "Root Commands"),
		CONFIG(Arrays.asList("CONFIG"), Arrays.asList(), TBLT, PluginPermission.PLAYER, "Config Commands"),
		ENGLISH(Arrays.asList("ENGLISH"), Arrays.asList(), CONFIG, PluginPermission.PLAYER, "Toggle Display English"),
		KANJI(Arrays.asList("KANJI"), Arrays.asList(), CONFIG, PluginPermission.PLAYER, "Toggle Display Kanji"),
		
		EDIT(Arrays.asList("EDIT", "E"), Arrays.asList("<sentence>"), TBLT, PluginPermission.PLAYER, "Edit a Sentence"),
		CHANGE_SPEAKER(Arrays.asList("SPEAKER", "S"), Arrays.asList("<NPC ID>"), TBLT, PluginPermission.OP, "Change a Speaker"),

		QUIZ(Arrays.asList("QUIZ", "QUESTION", "Q"), Arrays.asList(), TBLT, PluginPermission.PLAYER, "Quiz Commands"),
		SORT_QUIZ(Arrays.asList("SORT", "S"), Arrays.asList("Conversation ID"), QUIZ, PluginPermission.PLAYER, "Sort Quiz"),

		OP(Arrays.asList("OP"), Arrays.asList(), TBLT, PluginPermission.OP, "Operator's Commands"),
//		LOAD(Arrays.asList("LOAD", "L"), Arrays.asList("<file>"), OP, PluginPermission.OP, "Load A Sentence File"),
		RELOAD(Arrays.asList("RELOAD", "R"), Arrays.asList(), OP, PluginPermission.OP, "Reload KotobaTBLT Plugin"),

		TASK(Arrays.asList("TASK", "T"), Arrays.asList(), TBLT, PluginPermission.EXAMINEE, "Task Commands"),
		CREATE_TASK(Arrays.asList("CREATE", "C"), Arrays.asList("<name>", "<NPC ID>"), TASK, PluginPermission.OP, "Create a Task"),
		LIST_TASK(Arrays.asList("LIST", "L"), Arrays.asList(), TASK, PluginPermission.OP, "Show a Task's List"),

		CONVERSATION(Arrays.asList("CONV", "C", "CONVERSATION"), Arrays.asList(), TBLT, PluginPermission.OP, "Conversation Commands"),
		CREATE_CONVERSATION(Arrays.asList("CREATE", "C"), Arrays.asList("<task>", "<NPC ID>"), CONVERSATION, PluginPermission.OP, "Create a Conversation"),

		DEVELOP(Arrays.asList("DEVELOP", "DEV"), Arrays.asList(), TBLT, PluginPermission.DEVELOPER, "Developper's Commands"),
		TEST(Arrays.asList("TEST"), Arrays.asList("args"), DEVELOP, PluginPermission.DEVELOPER, "Tests for Development"),
		DATABASE(Arrays.asList("DATABASE", "DB"), Arrays.asList(), DEVELOP, PluginPermission.DEVELOPER, "Database Commands"),
		UPDATE(Arrays.asList("UPDATE", "U"), Arrays.asList("<file>"), DATABASE, PluginPermission.DEVELOPER, "Execute Query for MySQL");
		
		private List<String> names;
		private List<String> examples;
		private PluginCommand parent;
		private PluginPermission permission;
		private String usage;

		private PluginCommand(List<String> names, List<String> examples, PluginCommand parent, PluginPermission permission, String usage) {
			this.names = names.stream().map(String::toUpperCase).collect(Collectors.toList());
			this.examples = examples;
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
			return PluginPermission.hasPermission(permission, player.getUniqueId());
		}
		
		private static List<PluginCommand> findCommands(Command command, String[] args) {
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
		
		public String getUsage() {
			List<String> names = this.getCommandTree().stream()
				.map(com -> com.names.get(0).toLowerCase())
				.collect(Collectors.toList());
			names.set(0, "/" + ChatColor.GOLD + ChatColor.BOLD + names.get(0));
			if(1 < names.size()) {
				names.set(1, ChatColor.YELLOW + names.get(1));
			}
			if(2 < names.size()) {
				names.set(2, ChatColor.RESET + names.get(2));
			} else {
				names.set(names.size() - 1, names.get(names.size() - 1) + ChatColor.RESET);
			}
			names.addAll(examples);
			return String.join(" ", names) + ChatColor.GOLD + " : " + ChatColor.RESET + usage;
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
			player.sendMessage(Message.INVALID.getMessageWithPrefix(null));
			return true;
		} else if(1 < commands.size()) {
			for (PluginCommand com : commands) {
				player.sendMessage(com.getUsage());
			}
			return true;
		}

		PluginCommand selected = commands.get(0);
		if(!selected.hasPermission(player)) {
			player.sendMessage(Message.NO_PERMISSION.getMessageWithPrefix(Arrays.asList(selected.name())));
			return true;
		}
		List<String> opts = selected.takeArguments(args);

		switch(selected) {
		case ENGLISH:
			{
				PlayerData data = PlayerManager.getOrDefault(player.getUniqueId());
				PlayerManager.update(data.english(!data.getEnglish()));
			}
			return true;
		case KANJI:
			{
				PlayerData data = PlayerManager.getOrDefault(player.getUniqueId());
				PlayerManager.update(data.kanji(!data.getKanji()));
			}
			return true;
		case SORT_QUIZ:
			if(0 < opts.size()) {
				try {
					int npc = Integer.valueOf(opts.get(0));
					Player player2 = player;
					PlayerManager.update(PlayerManager.getOrDefault(player.getUniqueId()).npc(npc));
					SentenceManager.findSentencesByNPCId(npc)
						.flatMap(GUIIcon::tryCreateSortQuizSet)
						.map(icons ->
							PluginGUI.createInventory(gui ->
								gui.title(GUITitle.SORT_QUIZ.getTitle())
									.icons(icons)))
						.ifPresent(inv -> player2.openInventory(inv));
					return true;
				} catch(NumberFormatException e) {
					e.printStackTrace();
				}
			}
			break;
		
		case LIST_TASK:
			player.sendMessage(Message.NONE.getMessageWithPrefix(Arrays.asList("Task: " + String.join(", ", SentenceManager.getAllTask()))));
			return true;
		case CREATE_TASK:
			if(1 < opts.size()) {
				try {
					int npc = Integer.valueOf(opts.get(1));
					boolean success = SentenceManager.tryCreateTask(opts.get(0), npc);
					if(success) {
						PluginSound.FORGE.play(player);
						player.sendMessage(Message.SUCCESS.getMessageWithPrefix(Arrays.asList(opts.get(0) + ", NPC: " + npc)));
					} else {
						PluginSound.FAILED.play(player);
						player.sendMessage(Message.INVALID.getMessageWithPrefix(Arrays.asList(opts.get(0) + ", NPC: " + npc)));
					}
					return true;
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			break;
		case CREATE_CONVERSATION:
			if(1 < opts.size()) {
				try {
					int npc = Integer.valueOf(opts.get(1));
					boolean success = SentenceManager.tryCreateConversation(opts.get(0), npc);
					if(success) {
						PluginSound.FORGE.play(player);
						player.sendMessage(Message.SUCCESS.getMessageWithPrefix(Arrays.asList(opts.get(0) + ", NPC: " + npc)));
					} else {
						PluginSound.FAILED.play(player);
						player.sendMessage(Message.INVALID.getMessageWithPrefix(Arrays.asList(opts.get(0) + ", NPC: " + npc)));
					}
					return true;
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			break;
		
		case EDIT:
			if(0 < opts.size()) {
				final Player p = player;
				String edit = String.join(" ", opts);
				PlayerData data = PlayerManager.getOrDefault(player.getUniqueId());
				data.findEdit()
					.ifPresent(id -> SentenceManager.find(id)
						.ifPresent(sentence -> {
							SentenceManager.update(sentence.edit(edit, data.getEditMode()));
							p.sendMessage(Message.NONE.getMessageWithPrefix(Arrays.asList("Edited")));
							p.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + " EN: " + ChatColor.RESET + Message.NONE.getMessage(sentence.getLines(Arrays.asList(Expression.ENGLISH))));
							p.sendMessage("" + ChatColor.RED + ChatColor.BOLD + " JP: " + ChatColor.RESET + Message.NONE.getMessage(sentence.getLines(Arrays.asList(Expression.KANJI))));
							PluginSound.FORGE.play(p);
						}));
				return true;
			}
			break;
			
		case CHANGE_SPEAKER:
			if(0 < opts.size()) {
				try {
					int npc = Integer.valueOf(opts.get(0));
					PlayerData data = PlayerManager.getOrDefault(player.getUniqueId());
					if(!data.getEditMode().equals(EditMode.SPEAKER)) break;
					boolean success = data.findEdit()
						.flatMap(SentenceManager::find)
						.map(sentence -> SentenceManager.tryChangeSpeaker(sentence, npc))
						.orElse(false);
					if(success) {
						player.sendMessage(Message.SUCCESS.getMessageWithPrefix(Arrays.asList()));
						PluginSound.FORGE.play(player);
					} else {
						player.sendMessage(Message.INVALID.getMessageWithPrefix(Arrays.asList()));
						PluginSound.FAILED.play(player);
					}
					return true;
				} catch(NumberFormatException e) {
					e.printStackTrace();
				}
			}
			break;
//		case LOAD:
//			if(0 < opts.size()) {
//				SentenceManager.importSentence(opts.get(0));
//				return true;
//			}
//			break;
		case RELOAD:
			PluginManager.initialize(plugin);
			return true;
		case UPDATE:
			if(0 < opts.size()) {
				DatabaseManager.executeUpdate(ExternalQuery.loadQuery(opts.get(0)));
			}
			return true;
		case TEST:
			KotobaTBLTTest.testPlayer(player);
			return true;
		default:
			break;
		}
		player.sendMessage(selected.getUsage());
		return true;
	}

}
