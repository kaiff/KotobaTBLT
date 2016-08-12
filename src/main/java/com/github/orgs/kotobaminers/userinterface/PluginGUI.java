package com.github.orgs.kotobaminers.userinterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.orgs.kotobaminers.database.PlayerData;
import com.github.orgs.kotobaminers.database.PlayerData.EditMode;
import com.github.orgs.kotobaminers.database.PlayerManager;
import com.github.orgs.kotobaminers.database.Sentence;
import com.github.orgs.kotobaminers.database.Sentence.Expression;
import com.github.orgs.kotobaminers.database.SentenceManager;
import com.github.orgs.kotobaminers.kotobatblt.PluginCommandExecutor.PluginCommand;
import com.github.orgs.kotobaminers.userinterface.PluginMessage.Message;
import com.github.orgs.kotobaminers.utility.PluginSound;
import com.github.orgs.kotobaminers.utility.Utility;

import net.citizensnpcs.api.npc.NPC;


public class PluginGUI {
	public enum GUIIcon {
		ENGLISH(Material.WOOL, 3, "Enter English", null),
		JAPANESE(Material.WOOL, 14, "Enter Japanese", null),
		FREE_UP(Material.GLASS, 0, "Free up this Conversation", null),
		CLAIM(Material.STAINED_GLASS, 1, "Claim this Conversation", null),
		MY_SKIN(Material.GOLDEN_APPLE, 0, "Change the Skin to Me", null),

		SENTENCE_SKELETON(Material.SKULL_ITEM, 0, "", null),
		SENTENCE_WITHER(Material.SKULL_ITEM, 1, "", null),
		SENTENCE_ZOMBIE(Material.SKULL_ITEM, 2, "", null),
		SENTENCE_PLAYER(Material.SKULL_ITEM, 3, "", null),
		SENTENCE_CREEPER(Material.SKULL_ITEM, 4, "", null),
		CHANGE_SPEAKER(Material.EYE_OF_ENDER, 0, "Change the Speaker", null),
		PREPEND(Material.IRON_INGOT, 0, "Prepend a Sentence", null),
		APPEND(Material.GOLD_INGOT, 0, "Append a Sentence", null),
		DELETE(Material.GLASS_BOTTLE, 0, "Delete a Sentence", null),
		
		NONE(Material.AIR, 0, "Dummy", null),
		;

		private final Material material;
		private final short data;
		private String displayName;
		private Optional<List<String>> lore;
		
		private GUIIcon(Material material, int data, String displayName, List<String> lore) {
			this.material = material;
			this.data = (short) data;
			this.displayName = displayName;
			this.lore = Optional.ofNullable(lore);
		}
		
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
		public void setLore(List<String> lore) {
			this.lore = Optional.ofNullable(lore);
		}
		
		public static Optional<GUIIcon> findGUIIcon(ItemStack item) {
			return Stream.of(GUIIcon.values())
				.filter(icon -> item.getType().equals(icon.material) && item.getDurability() == icon.data)
				.findFirst();
		}
		
		public ItemStack createItemStack() {
			ItemStack item = new ItemStack(this.material, 1, data);
			if(item.getType() != Material.AIR) {
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(displayName);
				lore.ifPresent(l -> meta.setLore(l));
				item.setItemMeta(meta);
			}
			return item;			
		}
		
		public static List<ItemStack> createItemStack(List<Sentence> sentences) {
			List<ItemStack> main = new ArrayList<>();
			List<ItemStack> list1 = new ArrayList<>();
			List<ItemStack> list2 = new ArrayList<>();
			for(int i = 0; i < MAX_WIDTH ; i++) {
				if(i < sentences.size()) {
					ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
					NPC npc = Utility.findNPC(sentences.get(i).getNPC()).orElse(null);
					if(npc != null) {
						switch(npc.getEntity().getType()) {
						case PLAYER:
//							skull = Utility.findSkinName(npc).map(Utility::createPlayerSkull).orElse(skull); TODO:
							break;
						case CREEPER:
							skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.CREEPER.ordinal());
							break;
						case SKELETON:
							skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.SKELETON.ordinal());
							break;
						case WITHER:
						case WITHER_SKULL:
							skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.WITHER.ordinal());
							break;
						case GIANT:
						case ZOMBIE:
							skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.ZOMBIE.ordinal());
							break;
						default:
							break;
						}
					}
					skull.setAmount(i + 1);
					ItemMeta skullMeta = skull.getItemMeta();
					skullMeta.setDisplayName(npc.getName());
					skull.setItemMeta(skullMeta);
					main.add(skull);

					ItemStack english = GUIIcon.ENGLISH.createItemStack();
					ItemMeta englishMeta = english.getItemMeta();
					english.setAmount(i + 1);
					englishMeta.setDisplayName(sentences.get(i).getLines(Arrays.asList(Expression.ENGLISH)).get(0));
					english.setItemMeta(englishMeta);
					list1.add(english);

					ItemStack japanese = GUIIcon.JAPANESE.createItemStack();
					ItemMeta japaneseMeta = japanese.getItemMeta();
					japanese.setAmount(i + 1);
					japaneseMeta.setDisplayName(sentences.get(i).getLines(Arrays.asList(Expression.JAPANESE)).get(0));
					japanese.setItemMeta(japaneseMeta);
					list2.add(japanese);

				} else {
					main.add(NONE.createItemStack());
					list1.add(NONE.createItemStack());
					list2.add(NONE.createItemStack());
				}
			}
			main.addAll(list1);
			main.addAll(list2);
			return main;
		}
		
		public void executeClickEvent(InventoryClickEvent event) {
			if(event.getWhoClicked() instanceof Player) {
				Player player = (Player) event.getWhoClicked();
				switch(this) {
				case FREE_UP:
					SentenceManager.findSentencesByNPCId(PlayerManager.getOrDefault(player.getUniqueId()).getNPC())
						.ifPresent(sentences -> {
							if(sentences.stream().allMatch(s -> s.canEdit(player.getUniqueId()))) {
								sentences.stream().forEach(s -> SentenceManager.update(s.owner(Optional.empty())));
								PluginSound.FORGE.play(player);
							} else {
								player.sendMessage(Message.NO_PERMISSION.getMessageWithPrefix(null));
								PluginSound.FAILED.play(player);
							}}
					);
					break;
				case CLAIM:
					SentenceManager.findSentencesByNPCId(PlayerManager.getOrDefault(player.getUniqueId()).getNPC())
						.ifPresent(sentences -> {
							if(sentences.stream().allMatch(s -> s.canEdit(player.getUniqueId()))) {
								sentences.stream().forEach(s -> SentenceManager.update(s.owner(Optional.of(player.getUniqueId()))));
								PluginSound.FORGE.play(player);
							} else {
								player.sendMessage(Message.NO_PERMISSION.getMessageWithPrefix(null));
								PluginSound.FAILED.play(player);
							}}
					);
					break;
				case MY_SKIN:
					Utility.findNPC(PlayerManager.getOrDefault(player.getUniqueId()).getNPC())
						.ifPresent(npc -> Utility.renameNPCAsPlayer(npc, player.getName(), player.getUniqueId()));
					PluginSound.POP_UP.play(player);
					break;

				case CHANGE_SPEAKER:
					{
						PlayerData data = PlayerManager.getOrDefault(player.getUniqueId());
						PlayerManager.update(data.editMode(EditMode.SPEAKER).edit(data.getSentence()));
						PluginSound.CLICK.play(player);
						player.sendMessage(Message.NONE.getMessageWithPrefix(Arrays.asList()) + "/tblt speaker <NPC ID>");
					}
					break;
				case APPEND:
					PluginSound.CLICK.play(player);
					SentenceManager.find(PlayerManager.getOrDefault(player.getUniqueId()).getSentence())
						.ifPresent(sentence -> SentenceManager.insertEmptySentence(sentence, 1));
					break;
				case PREPEND:
					PluginSound.CLICK.play(player);
					SentenceManager.find(PlayerManager.getOrDefault(player.getUniqueId()).getSentence())
						.ifPresent(sentence -> SentenceManager.insertEmptySentence(sentence, 0));
					break;
				case DELETE:
					PluginSound.CLICK.play(player);
					SentenceManager.find(PlayerManager.getOrDefault(player.getUniqueId()).getSentence())
						.ifPresent(sentence -> SentenceManager.delete(sentence));
					break;

				case ENGLISH: 
					{
						PlayerData data = PlayerManager.getOrDefault(player.getUniqueId());
						PlayerManager.updateSentenceByInventory(data, calculateColumn(event.getRawSlot()))
							.ifPresent(d ->
								SentenceManager.find(d.getSentence())
									.ifPresent(s -> {
										if(s.canEdit(player.getUniqueId())) {
											PluginSound.CLICK.play(player);
											player.sendMessage(Message.NONE.getMessageWithPrefix(Arrays.asList(PluginCommand.EDIT.getUsage())));
											player.sendMessage(Message.NONE.getMessage(Arrays.asList(s.getLines(Arrays.asList(Expression.ENGLISH)).get(0) + " => ")));
											PlayerManager.update(data.edit(s.getId()).editMode(EditMode.ENGLISH));
											return;
										}
										PluginSound.FAILED.play(player);
										player.sendMessage(Message.NO_PERMISSION.getMessageWithPrefix(null));
							}));
					}
					break;
				case JAPANESE:
					{
						PlayerData data = PlayerManager.getOrDefault(player.getUniqueId());
						PlayerManager.updateSentenceByInventory(data, calculateColumn(event.getRawSlot()))
							.ifPresent(d ->
								SentenceManager.find(d.getSentence())
									.ifPresent(s -> {
										if(s.canEdit(player.getUniqueId())) {
											PluginSound.CLICK.play(player);
											player.sendMessage(Message.NONE.getMessageWithPrefix(Arrays.asList(PluginCommand.EDIT.getUsage())));
											player.sendMessage(Message.NONE.getMessage(Arrays.asList(s.getLines(Arrays.asList(Expression.JAPANESE)).get(0) + " => ")));
											PlayerManager.update(data.edit(s.getId()).editMode(EditMode.JAPANESE));
											return;
										}
										PluginSound.FAILED.play(player);
										player.sendMessage(Message.NO_PERMISSION.getMessageWithPrefix(null));
							}));
					}
					break;
				case SENTENCE_PLAYER:
				case SENTENCE_ZOMBIE:
				case SENTENCE_WITHER:
				case SENTENCE_SKELETON:
				case SENTENCE_CREEPER:
					PluginSound.CLICK.play(player);
					PlayerManager.updateSentenceByInventory(
						PlayerManager.getOrDefault(player.getUniqueId()),
						calculateColumn(event.getRawSlot())
					);
					player.openInventory(
						PluginGUI.createInventory(gui -> 
							gui.title(GUITitle.EDIT_SENTENCE.getTitle())
								.icons(IconSet.EDIT_CONVERSATION.getIcons().stream().map(icon -> icon.createItemStack()).collect(Collectors.toList()))));
					break;
				case NONE:
					break;
				default:
					break;
				}
			}
		}
		
		public enum IconSet {
			EDIT_CONVERSATION(Arrays.asList(CHANGE_SPEAKER, PREPEND, APPEND, DELETE)),
			CONVERSATION_SETTING(Arrays.asList(FREE_UP, CLAIM, MY_SKIN)),
			;
			private final List<GUIIcon> set;
			private IconSet(List<GUIIcon> set) {
				this.set = set;
			}
			public List<GUIIcon> getIcons() {
				return set;
			}
		}
	}
	
	public enum GUITitle {
		SENTENCE(ChatColor.BOLD + "Sentence"),
		EDIT_SENTENCE(ChatColor.BOLD + "Edit Sentence"),
		;
		
		private final String title;
		private GUITitle(String title) {
			this.title = title;
		}
		public String getTitle() {
			return title;
		}
	}
	
	private PluginGUI() {
	}

	private String title;
	private List<ItemStack> icons;
	private int size = 45;
	public static final int MAX_WIDTH = 9;
	private static final int MAX_SIZE = 45;

	public PluginGUI title(String title) {
		this.title = title;
		return this;
	}
	public PluginGUI icons(List<ItemStack> icons) {
		this.icons = icons;
		return this;
	}
	public PluginGUI addLastRowIcons(List<ItemStack> last) {
		if(icons.size() < MAX_SIZE - MAX_WIDTH) {
			for(int i = icons.size(); i < MAX_SIZE - MAX_WIDTH; i++) {
				icons.add(GUIIcon.NONE.createItemStack());
			}
			for(int i = 0; i < MAX_WIDTH; i++) {
				if(last.size() <= i) {
					break;
				}
				icons.add(last.get(i));
			}
		}
		return this;
	}

	public static Inventory createInventory(final Consumer<PluginGUI> builder) {
		PluginGUI gui = new PluginGUI();
		builder.accept(gui);
		Inventory inventory = Bukkit.createInventory(null, gui.size, gui.title);
		for(int i = 0; i < gui.icons.size(); i++) {
			if(!(gui.icons.get(i).getType().equals(Material.AIR))) {
				inventory.setItem(i, gui.icons.get(i));
			}
		}
		return inventory;
	}
	
	public static boolean isPluginGUI(Inventory inventory) {
		return Stream.of(GUITitle.values())
			.anyMatch(title -> inventory.getTitle().equalsIgnoreCase(title.getTitle()));
	}
	public static boolean isValidSlot(InventoryClickEvent event) {
		if(event.getRawSlot() < MAX_SIZE) {
			return true;
		}
		return false;
	}
	private static int calculateColumn(int slot) {
		return slot - MAX_WIDTH * (slot / MAX_WIDTH);
	}
}
