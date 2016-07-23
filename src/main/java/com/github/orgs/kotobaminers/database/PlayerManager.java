package com.github.orgs.kotobaminers.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import com.github.orgs.kotobaminers.database.PlayerData.EditMode;

public class PlayerManager extends DatabaseManager {
	public synchronized static PlayerData getOrDefault(UUID uuid) {
		String select = "SELECT * FROM " + playerTable + " WHERE uuid = '" + uuid.toString() + "' LIMIT 1;";
		PlayerData data = PlayerData.initial(uuid);
		ResultSet result = null;
		
		try {
			openConnection();
			statement = connection.createStatement();
			result = statement.executeQuery(select);
			if(result.next()) {
				final int npc = result.getInt("npc");
				final int conversation = result.getInt("conversation");
				final int sentence = result.getInt("sentence");
				final int line = result.getInt("line");
				final int edit = result.getInt("edit");
				final EditMode mode = EditMode.valueOf(result.getString("editMode"));
				data = PlayerData.create(d ->
				d.uuid(uuid)
				.npc(npc)
				.conversation(conversation)
				.sentence(sentence)
				.line(line)
				.edit(edit)
				.editMode(mode));
			}
			update(data);
			
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
		return data;
	}

	public synchronized static void update(PlayerData data) {
		String update = "INSERT INTO " + playerTable + " "
			+ "(uuid, npc, conversation, sentence, line, edit, editMode)"
			+ " VALUES"
				+ " ('" + data.getUuid().toString() + "', '"
				+ data.getNPC() + "', '"
				+ data.getConversation() + "', '"
				+ data.getSentence() + "', '"
				+ data.getLine() + "', '"
				+ data.findEdit().orElse(0) + "', '"
				+ data.getEditMode().name() + "') "
			+ "ON DUPLICATE KEY UPDATE "
				+ "uuid = '" + data.getUuid().toString() + "', "
				+ "npc = '" + data.getNPC() + "', "
				+ "conversation = '" + data.getConversation() + "', "
				+ "sentence = '" + data.getSentence() + "', "
				+ "line = '" + data.getLine() + "', "
				+ "edit = '" + data.findEdit().orElse(0) + "', "
				+ "editMode = '" + data.getEditMode().name() + "';";
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

	public synchronized static Optional<PlayerData> updateHologram(PlayerData data, int npc) {
		return SentenceManager.findSentencesByNPCId(npc).orElse(new ArrayList<Sentence>()).stream()
			.map(s -> s.getConversation())
			.findFirst()
			.map(conversation -> {
				if (conversation == data.getConversation()) {
					if(data.getLine() + 1 < SentenceManager.findSentencesByConversation(conversation).orElse(new ArrayList<Sentence>()).size()) {
						data.line(data.getLine() + 1);
					} else {
						data.line(0);
					}
				}
				data.conversation(conversation);
				update(data);
				return data;
			});
	}
	
	public synchronized static Optional<PlayerData> updateSentenceByInventory(PlayerData data, int order) {
		return SentenceManager.findSentencesByNPCId(data.getNPC())
			.filter(sentences -> order < sentences.size())
			.map(sentences -> sentences.get(order).getId())
			.map(id -> data.sentence(id))
			.map(d -> {
				update(d);
				return d;
			});
	}
	public synchronized static Optional<PlayerData> updateSentenceByHologram(PlayerData data, int hologram) {
		return SentenceManager.findSentencesByNPCId(data.getNPC())
			.filter(sentences -> hologram < sentences.size())
			.map(sentences -> sentences.get(hologram).getId())
			.map(id -> data.sentence(id))
			.map(d -> {
				update(d);
				return d;
			});
	}
}
