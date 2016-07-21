package com.github.orgs.kotobaminers.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.github.orgs.kotobaminers.kotobatblt.PluginManager;
import com.github.orgs.kotobaminers.kotobatblt.PluginMessage.Message;
import com.github.orgs.kotobaminers.kotobatblt.Sentence;

public class DatabaseManager {
	
	private static Connection connection = null;
	private static Statement statement = null;
	private static final YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(PluginManager.getPlugin().getDataFolder().getAbsolutePath() + "/Config/config.yml")); 
	private static String table = "";
	private static String user = "";
	private static String pass = "";
	private static String sentence = "";
	
	private DatabaseManager() {
	}
	
	private static void loadConfig() {
		table = config.getString("TABLE");
		user = config.getString("USER");
		pass = config.getString("PASS");
		List<String> paths = new ArrayList<String>();
		paths.addAll(Arrays.asList(PluginManager.getPlugin().getDataFolder().getAbsolutePath().split("\\\\")));
		paths.add("Sentence");
		sentence = String.join("//", paths);
	}
	
	public static void test() {//TODO: DELETE AFTER TESTING
		loadConfig();
	}
	
	public synchronized static void openConnection() {
		try {
			loadConfig();
			connection = DriverManager.getConnection(table, user, pass);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized static void closeConnection() {
		try {
			if(connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static void importSentence(String name) {
		String path = sentence + "//" + name + ".csv";
		File file = new File(path);
		if(!file.exists()) {
			Bukkit.getLogger().info(Message.INVALID.getMessage(Arrays.asList(path)));
			return;
		}
		
		try {
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
			if(statement != null) {
				statement.close();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void updataPlayerData(UUID uuid, String name, int conversation) {
		int newLine = 0;
		
		PlayerData data = getPlayerData(uuid).orElse(PlayerData.create(null, null, 0, 0));
		int currentConv = data.getConversation();
		int currentLine = data.getLine();
		if (conversation == currentConv) {
			if(currentLine + 1 < findSentencesByConversation(conversation).orElse(new ArrayList<Sentence>()).size()) {
				newLine = currentLine + 1;
			}
		}
		
		String update = "INSERT INTO player (uuid, name, line, conversation) VALUES ('" + uuid.toString() + "', '" + name + "', 0, 0) ON DUPLICATE KEY UPDATE uuid = '" + uuid.toString() + "', line = '" + newLine + "', conversation = '" + conversation + "';";
		try {
			statement = connection.createStatement();
			statement.executeUpdate(update);
			if(statement != null) {
				statement.close();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void initializePlayerData(UUID uuid, String name) {
		String update = "INSERT INTO player (uuid, name, line, conversation) VALUES ('" + uuid.toString() + "', '" + name + "', 0, 0) ON DUPLICATE KEY UPDATE uuid = '" + uuid.toString() + "', name = '" + name + "', line = 0, conversation = 0;";
		try {
			statement = connection.createStatement();
			statement.executeUpdate(update);
			if(statement != null) {
				statement.close();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static Optional<Sentence> findSentence(Integer npc, UUID uuid) {
		String select = "SELECT * FROM sentence WHERE npc = " + npc.toString() + " LIMIT 1;";

		try {
			Optional<Sentence> optional = Optional.empty();
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(select);
			
			if(result.next()) {
				int conversation = result.getInt("conversation");
				String select2 = "SELECT * FROM sentence WHERE conversation = " + conversation + ";";
				ResultSet sentences = statement.executeQuery(select2);
				int line =  getPlayerData(uuid).orElse(PlayerData.create(null, null, 0, 0)).getLine();

				int count = 0;
				boolean found = false;
				if(sentences.next()) {
					sentences.previous();
					while(sentences.next()) {
						if(count == line) {
							found = true;
							break;
						}
						count++;
					}
					if(!found) {
						sentences.first();
					}
					optional = Optional.of(Sentence.create(
							sentences.getInt("conversation"),
							sentences.getInt("npc"),
							sentences.getString("task"),
							sentences.getString("kanji"),
							sentences.getString("kana"),
							"" /*result.getString("romaji")*/,
							sentences.getString("en")
						));
				}
				if(sentences != null) sentences.close();
			}
			
			if(result != null) result.close();
			if(statement != null) statement.close();
			return optional;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
	
	public static Optional<List<Sentence>> findSentencesByNPCId(int npc) {
		String select = "SELECT * FROM sentence WHERE npc = " + npc + " LIMIT 1;";
		List<Sentence> list = new ArrayList<>();

		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(select);
			
			if(result.next()) {
				int conversation = result.getInt("conversation");
				list = findSentencesByConversation(conversation).orElse(list);
			}
			if(result != null) result.close();
			if(statement != null) statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(0 < list.size()) {
			return Optional.ofNullable(list);
		} else {
			return Optional.empty();
		}
	}
	
	public static Optional<List<Sentence>> findSentencesByConversation(int conversation) {
		List<Sentence> list = new ArrayList<>();

		try {
			statement = connection.createStatement();
			String select = "SELECT * FROM sentence WHERE conversation = " + conversation + ";";
			ResultSet sentences = statement.executeQuery(select);
			
			if(sentences.next()) {
				sentences.previous();
				while(sentences.next()) {
					list.add(Sentence.create(
							sentences.getInt("conversation"),
							sentences.getInt("npc"),
							sentences.getString("task"),
							sentences.getString("kanji"),
							sentences.getString("kana"),
							"" /*result.getString("romaji")*/,
							sentences.getString("en")
							));
				}
			}
			if(sentences != null) sentences.close();
			if(statement != null) statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(0 < list.size()) {
			return Optional.ofNullable(list);
		} else {
			return Optional.empty();
		}
	}
	
	public static int getConversation(int npc) {
		return findSentencesByNPCId(npc).orElse(new ArrayList<Sentence>()).stream()
			.map(s -> s.getConversation())
			.findFirst()
			.orElse(0);
	}
	

	public static Optional<PlayerData> getPlayerData(UUID uuid) {
		String select = "SELECT * FROM player WHERE uuid = '" + uuid.toString() + "' LIMIT 1;";

		try {
			Optional<PlayerData> optional = Optional.empty();
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(select);
			if(!result.next()) {
				String name = Bukkit.getOfflinePlayer(uuid).getName();
				optional = Optional.of(PlayerData.create(uuid, name, 0, 0));
			} else {
				optional = Optional.of(PlayerData.create(uuid, result.getString("name"), result.getInt("line"), result.getInt("conversation")));
			}

			if(result != null) result.close();
			if(statement != null) statement.close();
			return optional;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
	
	public static void executeUpdate(String query) {
		try {
			statement = connection.createStatement();
			statement.executeUpdate(query);
			if(statement != null) {
				statement.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
