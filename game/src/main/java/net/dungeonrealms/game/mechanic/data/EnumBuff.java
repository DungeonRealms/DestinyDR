package net.dungeonrealms.game.mechanic.data;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Material;

@AllArgsConstructor @Getter
public enum EnumBuff {
	
	@SerializedName("Profession")
	PROFESSION(Material.GOLDEN_CARROT, "Profession", "Increases all experience gained from professions for everyone", "character experience gained"),

	@SerializedName("Loot")
	LOOT(Material.DIAMOND, "Loot", "Increases all loot drop chances for everyone", "loot drop chances"),
	
	@SerializedName("Level")
	LEVEL(Material.EXP_BOTTLE, "Level EXP", "Increases all experience gained from mobs for everyone", "profession experience gained");
	
	private Material icon;
	@Getter
	private String name;
	private String description;
	private String miniDescription;
	
	public String getItemName() {
		return ChatColor.GOLD + getFriendlyName();
	}

	public String getColumnName(){
		return name().toLowerCase();
	}
	public String getFriendlyName() {
		return "Global " + getName() + " Buff";
	}

	public String getFormattedTime(int duration){
		return DurationFormatUtils.formatDurationWords(duration * 1000, true, true);
	}
}
