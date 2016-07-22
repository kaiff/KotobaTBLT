package com.github.orgs.kotobaminers.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Sentence {
	
	private static final String JAPANESE_INI = "Enter Japanese";
	private static final String ENGLISH_INI = "Enter English";

	private int id = 0;
	private int conversation = 0;
	private int npc = 0;
	private int order = 0;
	private String task = "";
	private boolean key = false;
	private Map<Expression, String> lines = new HashMap<>();
	
	public enum Expression {JAPANESE, ENGLISH}
	
	private Sentence() {
	}

	private static Sentence create(int id, int npc, int conversation, int order, String task, boolean key, String japanese, String english) {
		Sentence sentence = new Sentence();
		sentence.id = id;
		sentence.npc = npc;
		sentence.conversation = conversation;
		sentence.order = order;
		sentence.task = task;
		sentence.key = key;
		sentence.lines.put(Expression.JAPANESE, japanese);
		sentence.lines.put(Expression.ENGLISH, english);
		return sentence;
	}
	
	public static Sentence create(ResultSet result) throws SQLException {
		return Sentence.create(
			result.getInt("id"),
			result.getInt("npc"),
			result.getInt("conversation"),
			result.getInt("ordering"),
			result.getString("task"),
			result.getBoolean("keyBool"),
			result.getString("japanese"),
			result.getString("english"));
	}
	
	public static Sentence empty(int id, int conversation, int npc) {
		Sentence sentence = new Sentence();
		sentence.conversation = conversation;
		sentence.npc = npc;
		sentence.lines.put(Expression.JAPANESE, JAPANESE_INI);
		sentence.lines.put(Expression.ENGLISH, ENGLISH_INI);
		return sentence;
	}

	public int getId() {
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
	public void setOrder(int order) {
		this.order = order;
	}
	public String getTask() {
		return task;
	}
	public boolean getKey() {
		return key;
	}
	public Sentence edit(String sentence, Expression expression) {
		lines.put(expression, sentence);
		return this;
	}
	
	@Override
	public String toString() {
		return "Cnv: " + conversation + ", NPC: " + npc + ", Tsk: " + task + ", Jp: " + lines.get(Expression.JAPANESE) + ", En: " + lines.get(Expression.ENGLISH);
	}
}
