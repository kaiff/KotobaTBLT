package com.github.orgs.kotobaminers.kotobatblt.utility;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MetadataStore;
import net.citizensnpcs.api.npc.NPC;

public class Utility {
	public static void shootFirework(World world, Location location) {
		Firework fw = (Firework) world.spawn(location, Firework.class);
		FireworkMeta fwm = fw.getFireworkMeta();
		Random random = new Random();
		FireworkEffect effect = FireworkEffect.builder().flicker(random.nextBoolean()).withColor(Color.GREEN).withFade(Color.AQUA).with(Type.BALL_LARGE).trail(random.nextBoolean()).build();
		fwm.addEffect(effect);
		fwm.setPower(0);
		fw.setFireworkMeta(fwm);
	}

	private static final List<String> skinMeta = Arrays.asList(
		"cached-skin-uuid-name",
		"player-skin-textures",
		"cached-skin-uuid",
		"player-skin-name",
		"player-skin-signature");
	
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
	
	public static Stream<NPC> getAllNPCs() {
		Iterator<NPC> iterator = CitizensAPI.getNPCRegistry().iterator();
		Iterable<NPC> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), false);
	}

//TODO This doesn't work
//	private static void refreshSkinMeta(NPC npc) {
//		MetadataStore data = npc.data();
//		skinMeta.stream()
//			.filter(meta -> data.has(meta))
//			.forEach(data::remove);
//	}

	public static void renameNPCAsPlayer(NPC npc, String name, UUID uuid) {
//		refreshSkinMeta(npc);
		MetadataStore data = npc.data();
//		data.set("cached-skin-uuid-name", name);
//		data.set("cached-skin-uuid", uuid.toString());
		npc.despawn();
		npc.setBukkitEntityType(EntityType.PLAYER);
		npc.setName(name);
		npc.spawn(npc.getStoredLocation());
	}

//TODO: NPC skin system is to do later	
//	public static Optional<String> findSkinName(NPC npc) {
//		if (npc.data().has(Skin.CACHED_SKIN_UUID_NAME_METADATA)) {
//			return Optional.ofNullable(npc.data().get(Skin.CACHED_SKIN_UUID_NAME_METADATA));
//		}
//		return Optional.empty();
//	}
	
	public static String patternProgress(String base, String unique, int length, int index, ChatColor color) {
		String progress = "" + color;
		for(int i = 0; i < length; i++) {
			if(i == index) {
				progress += unique;
			} else {
				progress += base;
			}
		}
		progress += ChatColor.RESET;
		return progress;
	}
	
	public static void lookAt(Player player, Location lookat) {
		//Clone the loc to prevent applied changes to the input loc
		 Location loc = player.getLocation().clone();
		// Values of change in distance (make it relative)
		 double dx = lookat.getX() - loc.getX();
		 double dy = lookat.getY() - loc.getY();
	     double dz = lookat.getZ() - loc.getZ();
		 // Set yaw
		 if (dx != 0) {
			   // Set yaw start value based on dx
			   if (dx < 0) {
				 loc.setYaw((float) (1.5 * Math.PI));
				   } else {
					 loc.setYaw((float) (0.5 * Math.PI));
					   }
			   loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
			 } else if (dz < 0) {
				   loc.setYaw((float) Math.PI);
			}
		// Get the distance from dx/dz
		double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
		// Set pitch
		loc.setPitch((float) -Math.atan(dy / dxz));
		// Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
		loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
		loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);
		player.teleport(loc);
	}
}
