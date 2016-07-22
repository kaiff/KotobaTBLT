package com.github.orgs.kotobaminers.kotobatblt;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.github.orgs.kotobaminers.database.PlayerData;
import com.github.orgs.kotobaminers.database.PlayerManager;
import com.github.orgs.kotobaminers.database.Sentence;
import com.github.orgs.kotobaminers.database.Sentence.Expression;
import com.github.orgs.kotobaminers.database.SentenceManager;
import com.github.orgs.kotobaminers.userinterface.Holograms;
import com.github.orgs.kotobaminers.userinterface.HologramsManager;
import com.github.orgs.kotobaminers.userinterface.NPCManager;
import com.github.orgs.kotobaminers.userinterface.PluginGUI;
import com.github.orgs.kotobaminers.userinterface.PluginGUI.GUIIcon;
import com.github.orgs.kotobaminers.userinterface.PluginGUI.GUITitle;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class PluginEvent implements Listener {
	
	@EventHandler
	public void onRightClickNPC(NPCRightClickEvent event) {
		NPC npc = event.getNPC();
		Player player = event.getClicker();

		List<Expression> expressions = Arrays.asList(Expression.JAPANESE, Expression.ENGLISH);

		PlayerData data = PlayerManager.getOrDefault(player.getUniqueId()).npc(npc.getId());
		PlayerManager.updataHologram(data, npc.getId())
			.flatMap(d -> PlayerManager.updataSentenceByHologram(d, d.getLine()))
			.ifPresent(d -> {
				Optional<Sentence> sentence = SentenceManager.find(d.getSentence());
				PlayerManager.update(data);
				sentence.ifPresent(s ->
					NPCManager.findNPC(s.getNPC()).map(n -> n.getStoredLocation())
						.ifPresent(loc -> HologramsManager.updateHologram(Holograms.create(loc), s.getConversation(), s.getLines(expressions), 20)));
			});
	}

	@EventHandler
	public void onLeftClickNPC(NPCLeftClickEvent event) {
		NPC npc = event.getNPC();
		Player player = event.getClicker();

		PlayerManager.update(PlayerManager.getOrDefault(player.getUniqueId()).npc(npc.getId()));
		SentenceManager.findSentencesByNPCId(npc.getId())
			.ifPresent(sentences -> 
				player.openInventory(
					PluginGUI.createInventory(gui ->
						gui.title(GUITitle.SENTENCE.getTitle())
						.icons(GUIIcon.createItemStack(sentences)))));
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!PluginGUI.isPluginGUI(event.getInventory()) || !PluginGUI.isValidSlot(event)) {
			return;
		}
		event.setCancelled(true);
		event.getWhoClicked().closeInventory();

		GUIIcon.findGUIIcon(event.getCurrentItem())
			.ifPresent(icon -> icon.executeClickEvent(event));
	}
}
