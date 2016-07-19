package com.github.orgs.kotobaminers.kotobatblt;

import java.util.UUID;

public class KotobaTBLTTest {
	private KotobaTBLTTest() {
	}
	
	public static void testAll() {
		System.out.println("test");
		testHologram();
	}
	
	private static void testHologram() {
	}
	
	private static void testSentence() {
		UUID uuid = UUID.fromString("de7bd32b-48a9-4aae-9afa-ef1de55f5bad");
	}
	
	private static void testPlayer() {
		UUID uuid = UUID.fromString("5797c479-ad5a-43b0-87ca-8852d65ac639");
//		DatabaseManager.updataPlayerData(uuid, "kai_f");
		DatabaseManager.getPlayerData(uuid).ifPresent(data -> System.out.println(data.toString()));
	}
}