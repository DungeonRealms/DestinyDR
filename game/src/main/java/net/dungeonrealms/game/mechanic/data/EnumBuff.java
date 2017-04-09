package net.dungeonrealms.game.mechanic.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;

@AllArgsConstructor
public enum EnumBuff {
	
	PROFESSION(Material.GOLDEN_CARROT, "Profession", "Increases all experience gained from professions for everyone", "character experience gained"),
	LOOT(Material.DIAMOND, "Loot", "Increases all loot drop chances for everyone", "loot drop chances"),
	LEVEL(Material.EXP_BOTTLE, "Level EXP", "Increases all experience gained from mobs for everyone", "experience gained from professions");
	
	@Getter private Material icon;
	@Getter private String name;
	@Getter private String description;
	@Getter private String miniDescription;
	
	
	public String getItemName() {
		return ChatColor.GOLD + getFriendlyName();
	}
	
	public String getFriendlyName() {
		return "Global " + getName() + " Buff";
	}
	
	public String getDatabaseTag() {
		return "buffs." + name().toLowerCase();
	}
}
