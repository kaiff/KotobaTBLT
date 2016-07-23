package com.github.orgs.kotobaminers.database;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerData {
	private UUID uuid = null;
	private int npc = 0;
	private int conversation = 0;
	private int sentence = 0;
	private int line = 0;
	private Optional<Integer> edit = Optional.empty();
	private EditMode mode = EditMode.NONE;
	
	private PlayerData() {
	}
	public PlayerData uuid(UUID uuid) {
		this.uuid = uuid;
		return this;
	}
	public PlayerData npc(int npc) {
		this.npc = npc;
		return this;
	}
	public PlayerData conversation(int conversation) {
		this.conversation = conversation;
		return this;
	}
	public PlayerData sentence(int sentence) {
		this.sentence = sentence;
		return this;
	}
	public PlayerData line(int line) {
		this.line = line;
		return this;
	}
	public PlayerData edit(int edit) {
		this.edit = Optional.of(edit);
		return this;
	}
	public PlayerData editMode(EditMode mode) {
		this.mode = mode;
		return this;
	}
	public static PlayerData create(final Consumer<PlayerData> builder) {
		PlayerData data = new PlayerData();
		builder.accept(data);
		return data;
	}
	public static PlayerData initial(UUID uuid) {
		PlayerData data = new PlayerData();
		data.uuid = uuid;
		return data;
	}

	public UUID getUuid() {
		return uuid;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public int getConversation() {
		return conversation;
	}
	public int getNPC() {
		return npc;
	}
	public int getSentence() {
		return sentence;
	}
	public Optional<Integer> findEdit() {
		return edit;
	}
	public EditMode getEditMode() {
		return mode;
	}
	
	public enum EditMode {
		JAPANESE,
		ENGLISH,
		SPEAKER,
		NONE,
		;
	}
}
