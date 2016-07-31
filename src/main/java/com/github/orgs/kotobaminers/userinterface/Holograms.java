package com.github.orgs.kotobaminers.userinterface;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;

import com.github.orgs.kotobaminers.kotobatblt.PluginManager;

public class Holograms {
	double height = 1.4;
	Location loc = null;
	List<ArmorStand> holos = new ArrayList<ArmorStand>();
	public BukkitTask task = null;
	
	private Holograms() {
	}
	
	public static Holograms create(Location loc) {
		Holograms holograms = new Holograms();
		holograms.setLocation(loc);
		return holograms;
	}
	
	public void displayTemporarily(List<String> lines, Integer duration){
		if(0 < lines.size()){
			cancelTask();
			this.loc.setY((this.loc.getY() + this.height) - 1.25);
			for(int i = lines.size(); 0 < i; i--) {
				for(int j = 0; j < 3; j++) {//TODO
				final ArmorStand hologram =
						(ArmorStand) this.loc.getWorld().spawnEntity(this.loc, EntityType.ARMOR_STAND);
				hologram.setVisible(false);
				hologram.setGravity(false);
				hologram.setCustomName(new String(lines.get(i-1)));
				hologram.setCustomNameVisible(true);
				holos.add(hologram);
				}

				this.loc.setY(this.loc.getY() + 0.25);
			}
			task = Bukkit.getScheduler().runTaskLater(PluginManager.getPlugin(), new Runnable() {
				@Override
				public void run() {
					remove();
				}
			}, duration);
		}
	}
	
	public void remove() {
		cancelTask();
		holos.forEach(e -> e.remove());
		holos = new ArrayList<>();
	}
	private void cancelTask() {
		if (task != null) {
			task.cancel();
		}
	}
	private void setLocation(Location loc) {
		this.loc = loc;
	}
}
