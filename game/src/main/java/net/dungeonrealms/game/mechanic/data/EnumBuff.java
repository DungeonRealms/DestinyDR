package net.dungeonrealms.game.mechanic.data;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

@AllArgsConstructor @Getter
public enum EnumBuff {
	
	@SerializedName("Profession")
	PROFESSION(Material.GOLDEN_CARROT, "activeProfessionBuff", "queuedProfessionBuffs", "Profession", "Increases all experience gained from professions for everyone", "character experience gained"),

	@SerializedName("Loot")
	LOOT(Material.DIAMOND, "activeLootBuff", "queuedLootBuffs", "Loot", "Increases all loot drop chances for everyone", "loot drop chances"),
	
	@SerializedName("Level")
	LEVEL(Material.EXP_BOTTLE, "activeLevelBuff", "queuedLevelBuffs", "Level EXP", "Increases all experience gained from mobs for everyone", "experience gained from professions");
	
	private Material icon;
	@Getter
	private String activeColumn, queuedColumn, name;
	private String description;
	private String miniDescription;
	
	public String getItemName() {
		return ChatColor.GOLD + getFriendlyName();
	}
	
	public String getFriendlyName() {
		return "Global " + getName() + " Buff";
	}
}
