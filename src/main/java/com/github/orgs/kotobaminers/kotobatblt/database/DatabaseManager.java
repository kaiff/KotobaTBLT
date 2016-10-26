package com.github.orgs.kotobaminers.kotobatblt.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.github.orgs.kotobaminers.kotobatblt.kotobatblt.PluginManager;

public abstract class DatabaseManager {
	static Connection connection = null;
	static Statement statement = null;
	private static final YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(PluginManager.getPlugin().getDataFolder().getAbsolutePath() + "/Config/config.yml")); 
	private static String database = "";
	private static String user = "";
	private static String pass = "";
	static String sentenceTable = "";;
	static String playerTable = "";;
	static String sentenceDir = "";
	
	DatabaseManager() {
	}
	
	public static void loadConfig() {
		database = config.getString("DATABASE");
		user = config.getString("USER");
		pass = config.getString("PASS");
		sentenceTable = config.getString("SENTENCE_TABLE");
		playerTable = config.getString("PLAYER_TABLE");
		List<String> paths = new ArrayList<String>();
		paths.addAll(Arrays.asList(PluginManager.getPlugin().getDataFolder().getAbsolutePath().split("\\\\")));
		paths.add("Sentence");
		sentenceDir = String.join("//", paths);
	}
	
	public synchronized static void openConnection() {
		try {
			connection = DriverManager.getConnection(database + "?useUnicode=true&characterEncoding=utf8", user, pass);
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
	
	public synchronized static void executeUpdate(String query) {
		try {
			openConnection();
			statement = connection.createStatement();
			statement.executeUpdate(query);
			if(statement != null) {
				statement.close();
			}
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

}
