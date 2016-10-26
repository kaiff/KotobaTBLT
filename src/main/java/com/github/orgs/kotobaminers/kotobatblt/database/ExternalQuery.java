package com.github.orgs.kotobaminers.kotobatblt.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.orgs.kotobaminers.kotobatblt.kotobatblt.PluginManager;


public class ExternalQuery {
	public static File directory = new File(PluginManager.getPlugin().getDataFolder() + "\\Database\\Query");
	public static String loadQuery(String name) {
		String query = "";
		Path path = Paths.get(directory + "\\" + name + ".txt");
		try (Stream<String> stream = Files.lines(path)) { //Close Automatically
			query = String.join("", stream.collect(Collectors.toList()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return query;
	}
}
