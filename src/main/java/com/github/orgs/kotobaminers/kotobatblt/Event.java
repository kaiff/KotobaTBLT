package com.github.orgs.kotobaminers.kotobatblt;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.orgs.kotobaminers.database.DatabaseManager;
import com.github.orgs.kotobaminers.kotobatblt.Sentence.Expression;
import com.github.orgs.kotobaminers.userinterface.PluginGUI;
import com.github.orgs.kotobaminers.userinterface.PluginGUI.GUIIcon;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class Event implements Listener {
	
	@EventHandler
	public void onRightClickNPC(NPCRightClickEvent event) {
		NPC npc = event.getNPC();
		Player player = event.getClicker();

		DatabaseManager.updataPlayerData(player.getUniqueId(), player.getName(), DatabaseManager.getConversation(npc.getId()));

		List<Expression> expressions = Arrays.asList(Expression.JAPANESE, Expression.ENGLISH);
		Optional<Sentence> sentence = DatabaseManager.findSentence(npc.getId(), player.getUniqueId());
		sentence.ifPresent(s -> {
			NPCManager.findNPC(s.getNPC()).map(n -> n.getStoredLocation())
				.ifPresent(loc -> HologramsManager.updateHologram(Holograms.create(loc), s.getConversation(), s.getLines(expressions), 20));
		});
	}

	@EventHandler
	public void onLeftClickNPC(NPCLeftClickEvent event) {
		NPC npc = event.getNPC();
		Player player = event.getClicker();

		DatabaseManager.findSentencesByNPCId(npc.getId())
			.ifPresent(sentences -> 
				player.openInventory(
					PluginGUI.createInventory(gui ->
						gui.title("TEST")
						.icons(GUIIcon.create(sentences)))));
	}
}
