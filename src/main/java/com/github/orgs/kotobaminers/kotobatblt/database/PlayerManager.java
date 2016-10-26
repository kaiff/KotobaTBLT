package com.github.orgs.kotobaminers.kotobatblt.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.BooleanUtils;

import com.github.orgs.kotobaminers.kotobatblt.database.PlayerData.EditMode;
import com.github.orgs.kotobaminers.kotobatblt.database.PlayerData.PluginPermission;

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
				final int sentence = result.getInt("sentence");
				final int display = result.getInt("display");
				final int edit = result.getInt("edit");
				final EditMode mode = EditMode.valueOf(result.getString("editMode"));
				final PluginPermission permission = PluginPermission.valueOf(result.getString("permission"));
				final boolean english = result.getBoolean("english");
				final boolean kanji = result.getBoolean("kanji");
				data = PlayerData.create(d ->
				d.uuid(uuid)
				.npc(npc)
				.sentence(sentence)
				.display(display)
				.edit(edit)
				.editMode(mode)
				.permission(permission)
				.english(english)
				.kanji(kanji)
				);
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
			+ "(uuid, npc, sentence, display, edit, editMode, permission, english, kanji)"
			+ " VALUES"
				+ " ('" + data.getUuid().toString() + "', '"
				+ data.getNPC() + "', '"
				+ data.getSentence() + "', '"
				+ data.getDisplay() + "', '"
				+ data.findEdit().orElse(0) + "', '"
				+ data.getEditMode().name() + "', '"
				+ data.getPermission().name() + "', '"
				+ BooleanUtils.toInteger(data.getEnglish()) + "', '"
				+ BooleanUtils.toInteger(data.getKanji()) +
				"') "
			+ "ON DUPLICATE KEY UPDATE "
				+ "uuid = '" + data.getUuid().toString() + "', "
				+ "npc = '" + data.getNPC() + "', "
				+ "sentence = '" + data.getSentence() + "', "
				+ "display = '" + data.getDisplay() + "', "
				+ "edit = '" + data.findEdit().orElse(0) + "', "
				+ "editMode = '" + data.getEditMode().name() + "', "
				+ "permission = '" + data.getPermission().name() + "', "
				+ "english = '" + BooleanUtils.toInteger(data.getEnglish()) + "', "
				+ "kanji = '" + BooleanUtils.toInteger(data.getKanji()) + "';";
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

	public synchronized static Optional<PlayerData> updateDisplay(PlayerData data, int npc) {
		List<Sentence> sentences = SentenceManager.findSentencesByNPCId(npc)
			.orElse(new ArrayList<Sentence>()).stream()
			.collect(Collectors.toList());
		
		List<Integer> ids = sentences.stream()
			.map(s -> s.getId())
			.collect(Collectors.toList());
		if(ids.size() < 1) return Optional.empty();

		if(ids.contains(data.getDisplay())) {
			int index = ids.indexOf(data.getDisplay());
			if(index < ids.size() - 1) {
				data.display(ids.get(index + 1));
			} else {
				data.display(ids.get(0));
			}
			update(data);
		} else {
			data.display(ids.get(0));
			update(data);
		}
		return Optional.of(data);
		
//		return sentences
//			.map(s -> s.getConversation())
//			.findFirst()
//			.map(conversation -> {
//				if (conversation == data.getConversation()) {
//					if(data.getDisplay() + 1 < SentenceManager.findSentencesByConversation(conversation).orElse(new ArrayList<Sentence>()).size()) {
//						data.display(data.getDisplay() + 1);
//					} else {
//						data.display(0);
//					}
//				}
//				data.conversation(conversation);
//				update(data);
//				return data;
//			});
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
//	public synchronized static Optional<PlayerData> updateSentenceByHologram(PlayerData data, int display) {
//		return SentenceManager.findSentencesByNPCId(data.getNPC())
//			.filter(sentences -> display < sentences.size())
//			.map(sentences -> sentences.get(display).getId())
//			.map(id -> data.sentence(id))
//			.map(d -> {
//				update(d);
//				return d;
//			});
//	}
}
