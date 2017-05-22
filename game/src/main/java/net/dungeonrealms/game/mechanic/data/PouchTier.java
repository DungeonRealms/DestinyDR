package net.dungeonrealms.game.mechanic.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@Getter
@AllArgsConstructor
public enum PouchTier {
	TIER_1("Small", 100, "A small linen pouch that holds " + ChatColor.BOLD + "100g"),
	TIER_2("Medium", 150,  "A medium wool sack that holds " + ChatColor.BOLD + "150g"),
	TIER_3("Large", 200, "A large leather satchel that holds " + ChatColor.BOLD + "200g"),
	TIER_4("Gigantic", 300,  "A giant container that holds " + ChatColor.BOLD + "300g"),
	TIER_5("Colossal", 400,  "A giant container that holds " + ChatColor.BOLD + "400g"); //Unused. Kept for consistency.
	
	private final String name;
	private final int size;
	private String description;

	public int getId() {
		return ordinal() + 1;
	}

	public static PouchTier getById(int tier) {
		return values()[tier - 1];
	}
}
