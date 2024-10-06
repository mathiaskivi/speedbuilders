package ee.mathiaskivi.speedbuilders.utility;

import org.bukkit.ChatColor;

import java.util.HashMap;

public class Translations {
	public static HashMap<String, String> messages = new HashMap<>();

	public static String translate(String message) {
		return ChatColor.stripColor(messages.get(message));
	}
}
