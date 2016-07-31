package com.github.orgs.kotobaminers.userinterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

public class PluginMessage {
	
	private static final String prefix = "" + ChatColor.GOLD + ChatColor.BOLD + "[TBLT]" + ChatColor.RESET;
	private static final String TOO_MANY_ARGS = "Too Many Args: Message Error";
	
	public enum Message {
		NO_PERMISSION(Arrays.asList("No Permission: ")),
		INVALID(Arrays.asList("Invalid: ")),
		EDIT_SENTENCE(Arrays.asList("Edit the Sentence: ")),
		SUCCESS(Arrays.asList("Success: ")),
		NONE(Arrays.asList("")),
		;
		
		private List<String> base;
		private Message(List<String> base) {
			this.base = base;
		}
		
		public String getMessage(List<String> args) {
			if(args == null) {
				return String.join("", base);
			}
			if(base.size() < args.size()) {
				return prefix + " " + TOO_MANY_ARGS;
			} 
			List<String> parts = new ArrayList<>();
			for(int i = 0; i < args.size(); i++) {
				parts.add(base.get(i));
				parts.add(args.get(i));
			}
			for (int i = args.size(); i < base.size(); i++) {
				parts.add(base.get(i));
			}
			return String.join("", parts);
		}
		public String getMessageWithPrefix(List<String> args) {
			return prefix + " " + getMessage(args);
		}
	}
}
