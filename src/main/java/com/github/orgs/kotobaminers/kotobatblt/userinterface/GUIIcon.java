package com.github.orgs.kotobaminers.kotobatblt.userinterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.orgs.kotobaminers.kotobatblt.database.PlayerData;
import com.github.orgs.kotobaminers.kotobatblt.database.PlayerManager;
import com.github.orgs.kotobaminers.kotobatblt.database.Sentence;
import com.github.orgs.kotobaminers.kotobatblt.database.SentenceManager;
import com.github.orgs.kotobaminers.kotobatblt.database.PlayerData.EditMode;
import com.github.orgs.kotobaminers.kotobatblt.database.Sentence.Expression;
import com.github.orgs.kotobaminers.kotobatblt.kotobatblt.PluginCommandExecutor.PluginCommand;
import com.github.orgs.kotobaminers.kotobatblt.userinterface.PluginMessage.Message;
import com.github.orgs.kotobaminers.kotobatblt.utility.PluginSound;
import com.github.orgs.kotobaminers.kotobatblt.utility.Utility;

import net.citizensnpcs.api.npc.NPC;

public enum GUIIcon {
			ENGLISH(Material.WOOL, 3, "Enter English", null, ClickType.CLOSE),
			JAPANESE(Material.WOOL, 14, "Enter Japanese", null, ClickType.CLOSE),
			FREE_UP(Material.GLASS, 0, "Free up this Conversation", null, ClickType.CLOSE),
			CLAIM(Material.STAINED_GLASS, 1, "Claim this Conversation", null, ClickType.CLOSE),
			MY_SKIN(Material.GOLDEN_APPLE, 0, "Change the Skin to Me", null, ClickType.CLOSE),

			ABOBE_QUESTION(Material.COAL, 0,  "", null, ClickType.CANCEL),
			SWAPPABLE(Material.STICK, 0,  "", null, ClickType.MOVE),
			ANSWER_ABOBE_QUESTION(Material.WORKBENCH, 0, "Answer", null, ClickType.CLOSE),

			SENTENCE_SKELETON(Material.SKULL_ITEM, 0, "", null, ClickType.CANCEL),
			SENTENCE_WITHER(Material.SKULL_ITEM, 1, "", null, ClickType.CANCEL),
			SENTENCE_ZOMBIE(Material.SKULL_ITEM, 2, "", null, ClickType.CANCEL),
			SENTENCE_PLAYER(Material.SKULL_ITEM, 3, "", null, ClickType.CANCEL),
			SENTENCE_CREEPER(Material.SKULL_ITEM, 4, "", null, ClickType.CANCEL),
			CHANGE_SPEAKER(Material.EYE_OF_ENDER, 0, "Change the Speaker", null, ClickType.CLOSE),
			PREPEND(Material.IRON_INGOT, 0, "Prepend a Sentence", null, ClickType.CLOSE),
			APPEND(Material.GOLD_INGOT, 0, "Append a Sentence", null, ClickType.CLOSE),
			DELETE(Material.GLASS_BOTTLE, 0, "Delete a Sentence", null, ClickType.CLOSE),

			NONE(Material.AIR, 0, "Dummy", null, ClickType.CANCEL),
			;
	
			private enum ClickType {
				CANCEL, MOVE, CLOSE
			}

			private final Material material;
			private final short data;
			private String displayName;
			private Optional<List<String>> lore;
			private ClickType clickType;

			private GUIIcon(Material material, int data, String displayName, List<String> lore, ClickType clickType) {
				this.material = material;
				this.data = (short) data;
				this.displayName = displayName;
				this.lore = Optional.ofNullable(lore);
				this.clickType = clickType;
			}

			public GUIIcon displayName(String displayName) {
				this.displayName = displayName;
				return this;
			}
			public GUIIcon lore(List<String> lore) {
				this.lore = Optional.ofNullable(lore);
				return this;
			}

			public static Optional<GUIIcon> findGUIIcon(ItemStack item) {
				return Stream.of(values())
					.filter(icon -> item.getType().equals(icon.material) && item.getDurability() == icon.data)
					.findFirst();
			}

			public ItemStack createItemStack() {
				ItemStack item = new ItemStack(this.material, 1, data);
				if(item.getType() != Material.AIR) {
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(displayName);
					lore.ifPresent(l -> meta.setLore(l));
					if(!lore.isPresent()) {
						meta.setLore(new ArrayList<String>());
					}
					item.setItemMeta(meta);
				}
				return item;			
			}

			public static Optional<List<ItemStack>> tryCreateSortQuizSet(List<Sentence> sentences) {
				if(sentences.size() < 2) return Optional.empty();
				List<Integer> questionOrder = new ArrayList<>();
				for(int i = 0; i < sentences.size(); i++) {
					questionOrder.add(i);
				}
				Collections.shuffle(questionOrder);
				List<ItemStack> icons = new ArrayList<>();
				for(int i = 0; i < sentences.size(); i++) {
					icons.add(ABOBE_QUESTION.displayName(sentences.get(i).getLines(Arrays.asList(Expression.ENGLISH)).get(0)).createItemStack());
				}
				for(int i = sentences.size(); i < PluginGUI.MAX_WIDTH; i++) {
					icons.add(NONE.createItemStack());
				}
				for(int i = 0; i < sentences.size(); i++) {
					icons.add(SWAPPABLE.displayName(sentences.get(questionOrder.get(i)).getLines(Arrays.asList(Expression.KANJI)).get(0)).createItemStack());
				}
				for(int i = sentences.size(); i < PluginGUI.MAX_WIDTH; i++) {
					icons.add(NONE.createItemStack());
				}
				icons.add(ANSWER_ABOBE_QUESTION.createItemStack());

				return Optional.of(icons);
			}
			
			public static List<ItemStack> createConversationSet(List<Sentence> sentences) {
				List<ItemStack> main = new ArrayList<>();
				List<ItemStack> list1 = new ArrayList<>();
				List<ItemStack> list2 = new ArrayList<>();
				for(int i = 0; i < PluginGUI.MAX_WIDTH ; i++) {
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
	
						ItemStack english = ENGLISH.createItemStack();
						ItemMeta englishMeta = english.getItemMeta();
						english.setAmount(i + 1);
						englishMeta.setDisplayName(sentences.get(i).getLines(Arrays.asList(Expression.ENGLISH)).get(0));
						english.setItemMeta(englishMeta);
						list1.add(english);
	
						ItemStack japanese = JAPANESE.createItemStack();
						ItemMeta japaneseMeta = japanese.getItemMeta();
						japanese.setAmount(i + 1);
						japaneseMeta.setDisplayName(sentences.get(i).getLines(Arrays.asList(Expression.KANJI)).get(0));
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
			
			private void executeClickType(InventoryClickEvent event) {
				switch(clickType) {
				case CANCEL:
					event.setCancelled(true);
					break;
				case CLOSE:
					event.setCancelled(true);
					event.getWhoClicked().closeInventory();
					break;
				case MOVE:
					break;
				default:
					break;
				}
			}
			
			public void executeClickEvent(InventoryClickEvent event) {
				if(event.getWhoClicked() instanceof Player) {
					executeClickType(event);
					Player player = (Player) event.getWhoClicked();
					switch(this) {
					case ANSWER_ABOBE_QUESTION:
						{
							SentenceManager.findSentencesByNPCId(PlayerManager.getOrDefault(player.getUniqueId()).getNPC())
								.ifPresent(sentences ->  {
									Inventory inventory = event.getInventory();
									List<Integer> index = new ArrayList<>();
									for(int i = PluginGUI.MAX_WIDTH; i < PluginGUI.MAX_WIDTH + sentences.size(); i++) {
										index.add(i);
									}
									List<ItemStack> items = index.stream()
										.map(i -> inventory.getItem(i))
										.collect(Collectors.toList());
									if(items.contains(null)) {
										player.sendMessage(Message.INVALID.getMessageWithPrefix(null));
										PluginSound.FAILED.play(player);
										return;	
									}
									if(items.stream().allMatch(item -> item.getType().equals(SWAPPABLE.material))) {
										for(int i = 0; i < sentences.size(); i++) {
											if(!sentences.get(i).getLines(Arrays.asList(Expression.ENGLISH, Expression.KANJI)).contains(items.get(i).getItemMeta().getDisplayName())) {
												player.sendMessage(Message.WRONG.getMessageWithPrefix(null));
												PluginSound.FAILED.play(player);
												return;
											}
										}
										player.sendMessage(Message.CORRECT.getMessageWithPrefix(null));
										Utility.shootFirework(player.getWorld(), player.getLocation());
										PluginSound.GOOD.play(player);
										return;
									}
								});
						}
						break;
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
							PlayerManager.updateSentenceByInventory(data, PluginGUI.calculateColumn(event.getRawSlot()))
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
							PlayerManager.updateSentenceByInventory(data, PluginGUI.calculateColumn(event.getRawSlot()))
								.ifPresent(d ->
									SentenceManager.find(d.getSentence())
										.ifPresent(s -> {
											if(s.canEdit(player.getUniqueId())) {
												PluginSound.CLICK.play(player);
												player.sendMessage(Message.NONE.getMessageWithPrefix(Arrays.asList(PluginCommand.EDIT.getUsage())));
												player.sendMessage(Message.NONE.getMessage(Arrays.asList(s.getLines(Arrays.asList(Expression.KANJI)).get(0) + " => ")));
												PlayerManager.update(data.edit(s.getId()).editMode(EditMode.KANJI));
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
							PluginGUI.calculateColumn(event.getRawSlot())
						);
						player.openInventory(
							PluginGUI.createInventory(gui -> 
								gui.title(PluginGUI.GUITitle.EDIT_SENTENCE.getTitle())
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
