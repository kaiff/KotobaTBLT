package com.github.orgs.kotobaminers.kotobatblt.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.BooleanUtils;

import com.github.orgs.kotobaminers.kotobatblt.database.Sentence.Expression;
import com.github.orgs.kotobaminers.kotobatblt.userinterface.PluginGUI;
import com.github.orgs.kotobaminers.kotobatblt.utility.Utility;

public class SentenceManager extends DatabaseManager {
	public synchronized static void update(Sentence sentence) {
		String update = "";
		if(sentence.getId() == null) {
			update = "INSERT INTO " + sentenceTable + " "
				+ "(npc, conversation, ordering, task, keyBool, japanese, english, owner) "
				+ "VALUES "
					+ "('" + sentence.getNPC() + "', '"
					+ sentence.getConversation() + "', '"
					+ sentence.getOrder() + "', '"
					+ sentence.getTask() + "', '"
					+ BooleanUtils.toInteger(sentence.getKey()) + "', '"
					+ sentence.getLines(Arrays.asList(Expression.KANJI)).get(0).replace("'", "''") + "', '"
					+ sentence.getLines(Arrays.asList(Expression.ENGLISH)).get(0).replace("'", "''") + "', '"
					+ sentence.getOwner().map(UUID::toString).orElse("") + "') ";
		} else {
			update = "INSERT INTO " + sentenceTable + " "
				+ "(id, npc, conversation, ordering, task, keyBool, japanese, english, owner) "
				+ "VALUES "
					+ "('" + sentence.getId() + "', '"
					+ sentence.getNPC() + "', '"
					+ sentence.getConversation() + "', '"
					+ sentence.getOrder() + "', '"
					+ sentence.getTask() + "', '"
					+ BooleanUtils.toInteger(sentence.getKey()) + "', '"
					+ sentence.getLines(Arrays.asList(Expression.KANJI)).get(0).replace("'", "''") + "', '"
					+ sentence.getLines(Arrays.asList(Expression.ENGLISH)).get(0).replace("'", "''") + "', '"
					+ sentence.getOwner().map(UUID::toString).orElse("") + "') "
				+ "ON DUPLICATE KEY UPDATE "
					+ "id = '" + sentence.getId() + "', "
					+ "npc = '" + sentence.getNPC() + "', "
					+ "conversation = '" + sentence.getConversation() + "', "
					+ "ordering = '" + sentence.getOrder() + "', "
					+ "task = '" + sentence.getTask() + "', "
					+ "keyBool = '" + BooleanUtils.toInteger(sentence.getKey()) + "', "
					+ "japanese = '" + sentence.getLines(Arrays.asList(Expression.KANJI)).get(0).replace("'", "''") + "', "
					+ "english = '" + sentence.getLines(Arrays.asList(Expression.ENGLISH)).get(0).replace("'", "''") + "', "
					+ "owner = '" + sentence.getOwner().map(UUID::toString).orElse("") + "';";
		}
		try {
			openConnection();
			statement = connection.createStatement();
			statement.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(statement != null) statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			closeConnection();
		}
	}

	public synchronized static Optional<Sentence> find(int id) {
		Optional<Sentence> sentence = Optional.empty();
		String select = "SELECT * FROM " + sentenceTable + " WHERE id = '" + id + "' LIMIT 1;";
		ResultSet result = null;
		
		try {
			openConnection();
			statement = connection.createStatement();
			result = statement.executeQuery(select);
			if(result.next()) {
				sentence = Optional.ofNullable(Sentence.create(result));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(result != null) result.close();
				if(statement != null) statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			closeConnection();
		}
		return sentence;
	}

	private synchronized static Optional<List<Sentence>> findSentencesByTask(String task) {
		List<Sentence> list = new ArrayList<>();
		ResultSet sentences = null;

		try {
			openConnection();
			statement = connection.createStatement();
			String select = "SELECT * FROM " + sentenceTable + " WHERE task = '" + task + "';";
			sentences = statement.executeQuery(select);
			
			if(sentences.next()) {
				sentences.previous();
				while(sentences.next()) {
					list.add(Sentence.create(sentences));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(sentences != null) sentences.close();
				if(statement != null) statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			closeConnection();
		}
		if(0 < list.size()) {
			return Optional.ofNullable(list);
		} else {
			return Optional.empty();
		}
	}

	public synchronized static Optional<List<Sentence>> findSentencesByConversation(int conversation) {
		List<Sentence> list = new ArrayList<>();
		ResultSet sentences = null;

		try {
			openConnection();
			statement = connection.createStatement();
			String select = "SELECT * FROM " + sentenceTable + " WHERE conversation = " + conversation + " ORDER BY ordering ASC;";
			sentences = statement.executeQuery(select);
			
			if(sentences.next()) {
				sentences.previous();
				while(sentences.next()) {
					list.add(Sentence.create(sentences));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(sentences != null) sentences.close();
				if(statement != null) statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			closeConnection();
		}
		if(0 < list.size()) {
			return Optional.ofNullable(list);
		} else {
			return Optional.empty();
		}
	}

	public synchronized static Optional<List<Sentence>> findSentencesByNPCId(int npc) {
		statement = null;
		ResultSet result = null;

		String select = "SELECT * FROM "  + sentenceTable + " WHERE npc = " + npc + " LIMIT 1;";
		List<Sentence> list = new ArrayList<>();
		
		try {
			openConnection();
			statement = connection.createStatement();
			result = statement.executeQuery(select);
			
			if(result.next()) {
				int conversation = result.getInt("conversation");
				list = findSentencesByConversation(conversation).orElse(list);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(result != null) result.close();
				if(statement != null) statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			closeConnection();
		}
		if(0 < list.size()) {
			return Optional.ofNullable(list);
		} else {
			return Optional.empty();
		}
	}
	
	private synchronized static List<Sentence> initializeSentenceOrder(List<Sentence> sentences) {
		for(int i = 0; i < sentences.size(); i++) {
			if(PluginGUI.MAX_WIDTH <= i) break;
			sentences.get(i).order(i);
		}
		return sentences;
	}
	
	public synchronized static void insertEmptySentence(Sentence sentence, int position) {
		findSentencesByConversation(sentence.getConversation())
			.filter(sentences -> sentence.getOrder() + position <= sentences.size() && sentences.size() < PluginGUI.MAX_WIDTH)
			.map(sentences -> {
				Sentence empty = Sentence.empty(sentence.getConversation(), sentence.getNPC()).task(sentence.getTask()).owner(sentence.getOwner());
				sentences.add(sentence.getOrder() + position, empty);
				return initializeSentenceOrder(sentences);
			})
			.ifPresent(sentences ->
				sentences.forEach(s -> update(s)));
	}

	public synchronized static void delete(Sentence sentence) {
		String update = "DELETE FROM " + sentenceTable + " WHERE id = '" + sentence.getId() + "';";
		try {
			openConnection();
			statement = connection.createStatement();
			statement.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(statement != null) statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			closeConnection();
		}
		findSentencesByConversation(sentence.getConversation())
			.ifPresent(sentences -> initializeSentenceOrder(sentences).forEach(SentenceManager::update));
	}
	
	public synchronized static Set<String> getAllTask() {
		Set<String> tasks = new HashSet<String>();
		String queryTask = "SELECT task FROM " + sentenceTable + ";";
		ResultSet resultTask = null;
		try {
			openConnection();
			statement = connection.createStatement();
			resultTask = statement.executeQuery(queryTask);
			while(resultTask.next()) {
				tasks.add(resultTask.getString("task").toUpperCase());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(statement != null) statement.close();
				if(resultTask != null) resultTask.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			closeConnection();
		}
		return tasks;
	}

	private synchronized static Set<Integer> getAllNPCId() {
		String queryNPC = "SELECT npc FROM " + sentenceTable + ";";
		Set<Integer> npcs = new HashSet<>();
		ResultSet resultNPC = null;
		try {
			openConnection();
			statement = connection.createStatement();
			resultNPC = statement.executeQuery(queryNPC);
			while(resultNPC.next()) {
				npcs.add(resultNPC.getInt("npc"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(statement != null) statement.close();
				if(resultNPC != null) resultNPC.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			closeConnection();
		}
		return npcs;
	}

	public synchronized static Set<Integer> getAllConversations() {
		Set<Integer> conversations = new HashSet<Integer>();
		String query = "SELECT conversation FROM " + sentenceTable + ";";
		ResultSet result = null;
		try {
			openConnection();
			statement = connection.createStatement();
			result = statement.executeQuery(query);
			while(result.next()) {
				conversations.add(result.getInt("conversation"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(statement != null) statement.close();
				if(result != null) result.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			closeConnection();
		}
		return conversations;
	}

	public synchronized static boolean tryCreateTask(String task, int npc) {
		Set<String> tasks = getAllTask().stream().map(String::toUpperCase).collect(Collectors.toSet());
		Set<Integer> npcs = getAllNPCId();
		
		if(!tasks.contains(task.toUpperCase()) && !npcs.contains(npc) && Utility.findNPC(npc).isPresent()) {
			return getAllConversations().stream().max(Comparator.naturalOrder())
				.map(conversation -> {
					update(Sentence.empty(conversation + 1, npc).task(task));
					return true;
				})
				.orElse(false);
		} else {
			return false;
		}
	}
	
	public synchronized static boolean tryCreateConversation(String task, int npc) {
		Set<String> tasks = getAllTask().stream().map(String::toUpperCase).collect(Collectors.toSet());
		Set<Integer> npcs = getAllNPCId();
		
		if(tasks.contains(task.toUpperCase()) && !npcs.contains(npc) && Utility.findNPC(npc).isPresent()) {
			return getAllConversations().stream().max(Comparator.naturalOrder())
				.map(conversation -> {
					update(Sentence.empty(conversation + 1, npc).task(task));
					return true;
				}).orElse(false);
		}
		return false;
	}

	public synchronized static boolean tryChangeSpeaker(Sentence sentence, int npc) {
		Set<Integer> npcs = getAllNPCId();
		Optional<List<Sentence>> sentences = findSentencesByConversation(sentence.getConversation());
		Set<Integer> thisNPCs = sentences
			.orElse(Arrays.asList()).stream()
			.map(s -> s.getNPC())
			.collect(Collectors.toSet());
		if((!npcs.contains(npc) && Utility.findNPC(npc).isPresent()) || thisNPCs.contains(npc)) {
			update(sentence.npc(npc));
			return true;
		} else {
			return false;
		}
	}

//	public synchronized static void importSentence(String name) {
//		String path = sentenceDir + "//" + name + ".csv";
//		File file = new File(path);
//		if(!file.exists()) {
//			Bukkit.getLogger().info(Message.INVALID.getMessageWithPrefix(Arrays.asList(path)));
//			return;
//		}
//		
//		try {
//			openConnection();
//			String importCsv = "LOAD DATA LOCAL INFILE \"" + sentenceDir + "//" + name + ".csv \" REPLACE INTO TABLE " + sentenceTable + " FIELDS TERMINATED BY ',';";
//			statement = connection.createStatement();
//			statement.executeUpdate(importCsv);
//	
//		} catch (SQLException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if(statement != null) statement.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//			closeConnection();
//		}
//	}
}
