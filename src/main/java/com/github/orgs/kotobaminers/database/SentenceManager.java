package com.github.orgs.kotobaminers.database;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.BooleanUtils;
import org.bukkit.Bukkit;

import com.github.orgs.kotobaminers.database.Sentence.Expression;
import com.github.orgs.kotobaminers.userinterface.PluginMessage.Message;

public class SentenceManager extends DatabaseManager {
	public synchronized static void importSentence(String name) {
		String path = sentence + "//" + name + ".csv";
		File file = new File(path);
		if(!file.exists()) {
			Bukkit.getLogger().info(Message.INVALID.getMessage(Arrays.asList(path)));
			return;
		}
		
		try {
			openConnection();
			String create = "CREATE TABLE IF NOT EXISTS SENTENCE "
							+ "(id INTEGER NOT NULL, "
							+ "npc INTEGER, "
							+ "conversation INTEGER, "
							+ "task VARCHAR(100), "
							+ "keyBool tinyint(1) NOT NULL DEFAULT '0', "
							+ "kanji VARCHAR(100), "
							+ "kana VARCHAR(100), "
							+ "en VARCHAR(100), "
							+ "PRIMARY KEY (id));";
			statement = connection.createStatement();
			statement.executeUpdate(create);
			
			String importCsv = "LOAD DATA LOCAL INFILE \"" + sentence + "//" + name + ".csv \" REPLACE INTO TABLE SENTENCE FIELDS TERMINATED BY ',';";
			statement.executeUpdate(importCsv);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			closeConnection();
		}
	}
	
	public synchronized static Optional<List<Sentence>> findSentencesByConversation(int conversation) {
		List<Sentence> list = new ArrayList<>();
		ResultSet sentences = null;

		try {
			openConnection();
			statement = connection.createStatement();
			String select = "SELECT * FROM sentence WHERE conversation = " + conversation + " ORDER BY ordering ASC;";
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

		String select = "SELECT * FROM sentence WHERE npc = " + npc + " LIMIT 1;";
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
	
	public synchronized static Optional<Sentence> find(int id) {
		Optional<Sentence> sentence = Optional.empty();
		String select = "SELECT * FROM sentence WHERE id = '" + id + "' LIMIT 1;";
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

	public synchronized static void update(Sentence sentence) {
		String update = "INSERT INTO sentence "
			+ "(id, npc, conversation, ordering, task, keyBool, japanese, english) "
			+ "VALUES "
				+ "('" + sentence.getId() + "', '"
				+ sentence.getNPC() + "', '"
				+ sentence.getConversation() + "', '"
				+ sentence.getOrder() + "', '"
				+ sentence.getTask() + "', '"
				+ BooleanUtils.toInteger(sentence.getKey()) + "', '"
				+ sentence.getLines(Arrays.asList(Expression.JAPANESE)).get(0) + "', '"
				+ sentence.getLines(Arrays.asList(Expression.ENGLISH)).get(0) + "') "
			+ "ON DUPLICATE KEY UPDATE "
			+ "id = '" + sentence.getId() + "', "
			+ "npc = '" + sentence.getNPC() + "', "
			+ "conversation = '" + sentence.getConversation() + "', "
			+ "ordering = '" + sentence.getOrder() + "', "
			+ "task = '" + sentence.getTask() + "', "
			+ "keyBool = '" + BooleanUtils.toInteger(sentence.getKey()) + "', "
			+ "japanese = '" + sentence.getLines(Arrays.asList(Expression.JAPANESE)).get(0) + "', "
			+ "english = '" + sentence.getLines(Arrays.asList(Expression.ENGLISH)).get(0) + "';";
		
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
	
	
	public synchronized static void updateSentenceOrder(List<Sentence> sentences) {
		for(int i = 0; i < sentences.size(); i++) {
			sentences.get(i).setOrder(i);
		}
	}
	
	public synchronized static void insertEmptySentence(int position) {
		
	}
}
