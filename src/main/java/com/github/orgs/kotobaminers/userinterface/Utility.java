package com.github.orgs.kotobaminers.userinterface;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class Utility {
	public static ItemStack createPlayerSkull(String owner) {
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		SkullMeta itemMeta = (SkullMeta) skull.getItemMeta();
		itemMeta.setDisplayName(owner);
		itemMeta.setOwner(owner);
		skull.setItemMeta(itemMeta);
		return skull;	}

	public static Optional<NPC> findNPC(Integer id) {
		if (id < 0) {
			return Optional.empty();
		}
		return Optional.ofNullable(CitizensAPI.getNPCRegistry().getById(id));
	}
//TODO: NPC skin system is to do later	
//	public static Optional<String> findSkinName(NPC npc) {
//		if (npc.data().has(Skin.CACHED_SKIN_UUID_NAME_METADATA)) {
//			return Optional.ofNullable(npc.data().get(Skin.CACHED_SKIN_UUID_NAME_METADATA));
//		}
//		return Optional.empty();
//	}
}
