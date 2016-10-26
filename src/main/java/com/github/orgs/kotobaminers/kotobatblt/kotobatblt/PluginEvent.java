package com.github.orgs.kotobaminers.kotobatblt.kotobatblt;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.github.orgs.kotobaminers.kotobatblt.database.PlayerData;
import com.github.orgs.kotobaminers.kotobatblt.database.PlayerManager;
import com.github.orgs.kotobaminers.kotobatblt.database.Sentence;
import com.github.orgs.kotobaminers.kotobatblt.database.SentenceManager;
import com.github.orgs.kotobaminers.kotobatblt.database.Sentence.Expression;
import com.github.orgs.kotobaminers.kotobatblt.userinterface.GUIIcon;
import com.github.orgs.kotobaminers.kotobatblt.userinterface.Holograms;
import com.github.orgs.kotobaminers.kotobatblt.userinterface.HologramsManager;
import com.github.orgs.kotobaminers.kotobatblt.userinterface.NPCManager;
import com.github.orgs.kotobaminers.kotobatblt.userinterface.PluginGUI;
import com.github.orgs.kotobaminers.kotobatblt.userinterface.PluginGUI.GUITitle;
import com.github.orgs.kotobaminers.kotobatblt.utility.PluginSound;
import com.github.orgs.kotobaminers.kotobatblt.utility.Utility;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class PluginEvent implements Listener {

	@EventHandler
	public void onRightClickNPC(NPCRightClickEvent event) {
		NPC npc = event.getNPC();
		Player player = event.getClicker();

		PlayerData data = PlayerManager.getOrDefault(player.getUniqueId()).npc(npc.getId());
		List<Expression> expressions = data.getExpressions();

		PlayerManager.updateDisplay(data, npc.getId())
			.ifPresent(d ->SentenceManager.find(d.getDisplay())
			.ifPresent(s ->NPCManager.findNPC(s.getNPC()).map(n -> n.getStoredLocation())
				.ifPresent(loc -> {
					List<String> lines = s.getLines(expressions);
					SentenceManager.findSentencesByConversation(s.getConversation())
						.ifPresent(sentences -> lines.add(0, Utility.patternProgress(" ", "¡", sentences.size(), sentences.stream().map(Sentence::getId).collect(Collectors.toList()).indexOf(s.getId()), ChatColor.GREEN)));
					HologramsManager.updateHologram(Holograms.create(loc), s.getConversation(), lines, 20 * 10);
					Utility.lookAt(player, loc.add(0, -1, 0));
					PluginSound.ATTENTION.play(player);
				})));
	}

	@EventHandler
	public void onLeftClickNPC(NPCLeftClickEvent event) {
		NPC npc = event.getNPC();
		Player player = event.getClicker();

		PlayerManager.update(PlayerManager.getOrDefault(player.getUniqueId()).npc(npc.getId()));
		SentenceManager.findSentencesByNPCId(npc.getId())
			.map(sentences -> 
				PluginGUI.createInventory(gui ->
					gui.title(GUITitle.SENTENCE.getTitle())
					.icons(GUIIcon.createConversationSet(sentences))
					.addLastRowIcons(GUIIcon.IconSet.CONVERSATION_SETTING.getIcons().stream().map(icon -> icon.createItemStack()).collect(Collectors.toList()))
			))
			.ifPresent(inv -> {
				player.openInventory(inv);
				PluginSound.CLICK.play(player);
			});
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!PluginGUI.isPluginGUI(event.getInventory())) {
			return;
		}
		
		if(!PluginGUI.isValidSlot(event) || event.getAction() == InventoryAction.NOTHING) {
			event.setCancelled(true);
			return;
		} 

		GUIIcon.findGUIIcon(event.getCurrentItem())
			.ifPresent(icon -> 	icon.executeClickEvent(event));
	}
}
