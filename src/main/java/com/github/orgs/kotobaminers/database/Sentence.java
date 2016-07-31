package com.github.orgs.kotobaminers.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.orgs.kotobaminers.database.PlayerData.EditMode;

public class Sentence {
	private static final String JAPANESE_INI = "Enter Japanese";
	private static final String ENGLISH_INI = "Enter English";
	private static final String TASK_INI = "DEFAULT";

	private Integer id = null;
	private int conversation = 0;
	private int npc = 0;
	private int order = 0;
	private String task = TASK_INI;
	private boolean key = false;
	private Map<Expression, String> lines = new HashMap<>();
	private Optional<UUID> owner = Optional.empty();
	
	public enum Expression {JAPANESE, ENGLISH}
	
	private Sentence() {
	}
	
	public static Sentence create(final Consumer<Sentence> builder) {
		Sentence sentence = new Sentence();
		builder.accept(sentence);
		return sentence;
	}
	public Sentence id(int id) {
		this.id = id;
		return this;
	}
	public Sentence npc(int npc) {
		this.npc = npc;
		return this;
	}
	public Sentence conversation(int conversation) {
		this.conversation = conversation;
		return this;
	}
	public Sentence order(int ordering) {
		this.order = ordering;
		return this;
	}
	public Sentence task(String task) {
		this.task = task;
		return this;
	}
	public Sentence key(boolean key) {
		this.key = key;
		return this;
	}
	public Sentence japanese(String japanese) {
		this.lines.put(Expression.JAPANESE, japanese);
		return this;
	}
	public Sentence english(String english) {
		this.lines.put(Expression.ENGLISH, english);
		return this;
	}
	public Sentence owner(Optional<UUID> owner) {
		this.owner = owner;
		return this;
	}
	
	public static Sentence create(ResultSet result) throws SQLException {
		final int id = result.getInt("id");
		final int npc = result.getInt("npc");
		final int conversation = result.getInt("conversation");
		final int order = result.getInt("ordering");
		final String task = result.getString("task");
		final boolean key = result.getBoolean("keyBool");
		final String japanese = result.getString("japanese");
		final String english = result.getString("english");
		final Optional<UUID> owner = Optional.ofNullable(result.getString("owner")).filter(o -> o.length() == 36).map(UUID::fromString);
		return Sentence.create(s ->
			s.id(id)
			.npc(npc)
			.conversation(conversation)
			.order(order)
			.task(task)
			.key(key)
			.japanese(japanese)
			.english(english)
			.owner(owner)
			);
	}
	
	/**
	 * id is AUTO_INCREMENT
	 * @param conversation
	 * @param npc
	 * @return
	 */
	public static Sentence empty(int conversation, int npc) {
		return Sentence.create(s ->
			s.conversation(conversation)
			.npc(npc)
			.japanese(JAPANESE_INI)
			.english(ENGLISH_INI)
		);
	}

	public Integer getId() {
		return id;
	}
	public int getNPC() {
		return npc;
	}
	public List<String> getLines(List<Expression> expressions) {
		return expressions.stream()
			.map(e -> lines.get(e))
			.collect(Collectors.toList());
	}
	public int getConversation() {
		return conversation;
	}
	public int getOrder() {
		return order;
	}
	public String getTask() {
		return task;
	}
	public boolean getKey() {
		return key;
	}
	public Optional<UUID> getOwner() {
		return owner;
	}
	public Sentence edit(String sentence, EditMode mode) {
		switch(mode)  {
		case ENGLISH:
			lines.put(Expression.ENGLISH, sentence);
			break;
		case JAPANESE:
			lines.put(Expression.JAPANESE, sentence);
			break;
		case NONE:
		case SPEAKER:
		default:
			break;
		}
		return this;
	}

	@Override
	public String toString() {
		return "Cnv: " + conversation + ", NPC: " + npc + ", Tsk: " + task + ", Jp: " + lines.get(Expression.JAPANESE) + ", En: " + lines.get(Expression.ENGLISH);
	}
}
