package com.github.orgs.kotobaminers.kotobatblt;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.orgs.kotobaminers.kotobatblt.Sentence.Expression;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class Event implements Listener {
	
	@EventHandler
	public void onClickNPCRight(NPCRightClickEvent event) {
		NPC npc = event.getNPC();
		Player player = event.getClicker();

		List<Expression> expressions = Arrays.asList(Expression.KANJI, Expression.ENGLISH);
		DatabaseManager.updataPlayerData(player.getUniqueId(), player.getName(), DatabaseManager.getConversation(npc.getId()));

		Optional<Sentence> sentence = DatabaseManager.findSentence(npc.getId(), player.getUniqueId());
		sentence.ifPresent(s -> {
			NPCManager.findNPC(s.getNPC()).map(n -> n.getStoredLocation())
				.ifPresent(loc -> HologramsManager.updateHologram(Holograms.create(loc), s.getConversation(), s.getLines(expressions), 20));
			});
		
	}

}
