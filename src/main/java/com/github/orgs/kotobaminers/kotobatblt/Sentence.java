package com.github.orgs.kotobaminers.kotobatblt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Sentence {

	private int conversation = 0;
	private int npc = 0;
	private String task = "";
	private Map<Expression, String> lines = new HashMap<>();
	
	public enum Expression {JAPANESE, KANA, ROMAJI, ENGLISH}
	
	private Sentence() {
	}

	public static Sentence create(Integer conversation, Integer npc, String task, String kanji, String kana, String romaji, String english) {
		Sentence sentence = new Sentence();
		sentence.conversation = conversation;
		sentence.npc = npc;
		sentence.task = task;
		sentence.lines.put(Expression.JAPANESE, kanji);
		sentence.lines.put(Expression.KANA, kana);
		sentence.lines.put(Expression.ROMAJI, romaji);
		sentence.lines.put(Expression.ENGLISH, english);
		return sentence;
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
	
	@Override
	public String toString() {
		return "Cnv: " + conversation + ", NPC: " + npc + ", Tsk: " + task + ", Knj: " + lines.get(Expression.JAPANESE) + ", Kn: " + lines.get(Expression.KANA) + ", Rmj: " + lines.get(Expression.ROMAJI) + ", En: " + lines.get(Expression.ENGLISH);
	}
	
}
