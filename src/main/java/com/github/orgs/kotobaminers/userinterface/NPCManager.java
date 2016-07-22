package com.github.orgs.kotobaminers.userinterface;

import java.util.Optional;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class NPCManager {
	public static Optional<NPC> findNPC(Integer id) {
		if(id < 0) return Optional.empty();
		return Optional.ofNullable(CitizensAPI.getNPCRegistry().getById(id));
	}
}
