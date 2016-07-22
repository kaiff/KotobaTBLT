package com.github.orgs.kotobaminers.database;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.github.orgs.kotobaminers.database.PlayerData.EditKey;
import com.github.orgs.kotobaminers.database.Sentence.Expression;

public class PlayerData {
	private UUID uuid = null;
	private int npc = 0;
	private int conversation = 0;
	private int sentence = 0;
	private int line = 0;
	private EditKey edit = new EditKey();
	
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
	public PlayerData editKey(int id, Expression expression) {
		this.edit = new EditKey(id, expression);
		return this;
	}
	public PlayerData editKeyEmpty() {
		this.edit = new EditKey();
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
	public EditKey getEditKey() {
		return edit;
	}
	public void setEditKey(int id, Expression expression) {
		edit = new EditKey(id, expression);
	}
	
	@Override
	public String toString() {
		return "PlayerData: " + uuid+ ", " + line + ", " + conversation;
	}
	
	public class EditKey {
		Optional<Integer> id = Optional.empty();
		Expression expression = Expression.JAPANESE;
		
		public EditKey() {
		}
		public EditKey(Integer id, Expression expression) {
			this.id = Optional.ofNullable(id);
			this.expression = expression;
		}
		public Optional<Integer> getId() {
			return id;
		}
		public Expression getExpression() {
			return expression;
		}
	}
}
