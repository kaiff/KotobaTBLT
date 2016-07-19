package com.github.orgs.kotobaminers.kotobatblt;

import java.util.UUID;

public class PlayerData {
	private UUID uuid = null;
	private String name = "";
	private int line = 0;
	private int conversation = 0;
	
	private PlayerData() {
	}
	public static PlayerData create(UUID uuid, String name, int line, int conversation) {
		PlayerData data = new PlayerData();
		data.uuid = uuid;
		data.name = name;
		data.line = line;
		data.conversation = conversation;
		return data;
	}

	public UUID getUuid() {
		return uuid;
	}
	public String getName() {
		return name;
	}
	public int getLine() {
		return line;
	}
	public int getConversation() {
		return conversation;
	}
	
	@Override
	public String toString() {
		return "PlayerData: " + uuid+ ", " + name  + ", " + line + ", " + conversation;
	}
}
