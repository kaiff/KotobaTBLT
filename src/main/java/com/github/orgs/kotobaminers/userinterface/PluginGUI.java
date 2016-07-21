package com.github.orgs.kotobaminers.userinterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.orgs.kotobaminers.kotobatblt.Sentence;
import com.github.orgs.kotobaminers.kotobatblt.Sentence.Expression;

import net.citizensnpcs.api.npc.NPC;


public class PluginGUI {
	public enum GUIIcon {
		ENGLISH(Material.WOOL, 3, "Enter English", null),
		JAPANESE(Material.WOOL, 14, "Enter Japanese", null),
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
		
		public static List<ItemStack> create(List<Sentence> sentences) {
			List<ItemStack> main = new ArrayList<>();
			List<ItemStack> list1 = new ArrayList<>();
			List<ItemStack> list2 = new ArrayList<>();
			for(int i = 0; i < INVENTORY_MAX_WIDTH ; i++) {
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
	}
	
	private static final int INVENTORY_MAX_WIDTH = 9;
	
	private PluginGUI() {
	}

	private String title;
	private List<ItemStack> icons;
	private int size = 54;

	public PluginGUI title(String title) {
		this.title = title;
		return this;
	}
	public PluginGUI icons(List<ItemStack> icons) {
		this.icons = icons;
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
}
