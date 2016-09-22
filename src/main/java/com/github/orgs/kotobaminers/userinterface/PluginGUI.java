package com.github.orgs.kotobaminers.userinterface;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class PluginGUI {
	public enum GUITitle {
		SENTENCE(ChatColor.BOLD + "Sentence"),
		EDIT_SENTENCE(ChatColor.BOLD + "Edit Sentence"),
		SORT_QUIZ(ChatColor.BOLD + "Sort Quiz"),
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
	static int calculateColumn(int slot) {
		return slot - MAX_WIDTH * (slot / MAX_WIDTH);
	}
}
