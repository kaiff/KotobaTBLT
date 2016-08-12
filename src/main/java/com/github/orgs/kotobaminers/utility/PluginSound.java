package com.github.orgs.kotobaminers.utility;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public enum PluginSound {
	GOOD(Sound.LEVEL_UP, 1, 1),
	FAILED(Sound.ITEM_BREAK, 1, 1),
	ATTENTION(Sound.NOTE_PIANO, 1, 1),
	POP_UP(Sound.CHICKEN_EGG_POP, 1, 1),
	FORGE(Sound.ANVIL_USE, 1, 1),
	CLICK(Sound.CLICK, 1, 1),
	;

	private float volume;
	private float pitch;
	private Sound sound;
	
	private PluginSound(Sound sound, float volume, float pitch) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}
	
	public void play(Player player) {
		player.playSound(player.getLocation(), sound, volume, pitch);
	}
}
