package com.github.orgs.kotobaminers.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.github.orgs.kotobaminers.database.Sentence.Expression;

public class PlayerData {
	private UUID uuid = null;
	private int npc = 0;
	private int sentence = 0;
	private int display = 0;
	private Optional<Integer> edit = Optional.empty();
	private EditMode mode = EditMode.NONE;
	private PluginPermission permission = PluginPermission.PLAYER;
	private boolean english = true;
	private boolean kanji = false;
	
	private PlayerData() {
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
	
	public PlayerData uuid(UUID uuid) {
		this.uuid = uuid;
		return this;
	}
	public PlayerData npc(int npc) {
		this.npc = npc;
		return this;
	}
	public PlayerData sentence(int sentence) {
		this.sentence = sentence;
		return this;
	}
	public PlayerData display(int display) {
		this.display = display;
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
	public PlayerData permission(PluginPermission permission) {
		this.permission = permission;
		return this;
	}
	public PlayerData english(boolean english) {
		this.english = english;
		return this;
	}
	public PlayerData kanji(boolean kanji) {
		this.kanji = kanji;
		return this;
	}
	
	public UUID getUuid() {
		return uuid;
	}
	public int getDisplay() {
		return display;
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
	public PluginPermission getPermission() {
		return permission;
	}
	public List<Expression> getExpressions() {
		List<Expression> expressions = new ArrayList<>();
		if(english) {
			expressions.add(Expression.ENGLISH);
		}
		if(kanji) {
			expressions.add(Expression.KANJI);
		}
		if(expressions.size() < 1) {
			expressions = Arrays.asList(Expression.ENGLISH);
		}
		return expressions;
	}
	public boolean getEnglish() {
		return english;
	}
	public boolean getKanji() {
		return kanji;
	}
	
	public enum PluginPermission{
		DEVELOPER,
		OP,
		EXAMINEE,
		PLAYER,;
		
		public static boolean hasPermission(PluginPermission permission, UUID uuid) {
			PluginPermission playerPermission = PlayerManager.getOrDefault(uuid).getPermission();
			switch (playerPermission) {
			case DEVELOPER:
				return true;
			case OP:
				switch(permission) {
				case OP:
				case EXAMINEE:
				case PLAYER:
					return true;
				default:
				}
				return false;
			case EXAMINEE:
				switch(permission) {
				case EXAMINEE:
				case PLAYER:
					return true;
				default:
				}
				return false;
			case PLAYER:
				switch(permission) {
				case PLAYER:
					return true;
				default:
				}
				return false;
			default:
			}
			return false;
		}
	}
	
	public enum EditMode {
		KANJI,
		ENGLISH,
		SPEAKER,
		SKIN,
		NONE,
		;
	}
}
