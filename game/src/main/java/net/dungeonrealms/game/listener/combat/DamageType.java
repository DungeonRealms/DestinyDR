package net.dungeonrealms.game.listener.combat;

import lombok.Getter;

import org.bukkit.ChatColor;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public enum DamageType {
	
	FALL(ChatColor.GRAY, "FALL", false),
	THORNS(ChatColor.GREEN, "THORNS"),
	CUSTOM(ChatColor.GOLD, "REFLECT"),
	SUFFOCATION(ChatColor.BLACK, "SUFFOCATION"),
	DROWNING(ChatColor.DARK_BLUE, "DROWNING", false, 0.04),
	FIRE_TICK(ChatColor.RED, "ON FIRE", 0.01),
	FIRE(ChatColor.DARK_RED, "IN FIRE", 0.03),
	LAVA(ChatColor.RED, "LAVA", 0.03),
	WITHER(ChatColor.DARK_RED, "WITHER", false, 50),
	POISON(ChatColor.DARK_GREEN, "POISON", 0.01),
	CONTACT(ChatColor.GREEN, "CACTUS", false, 0.03),
	FAILSAFE(ChatColor.GRAY, "CUSTOM");
	
	private final ChatColor color;
	private final String display;
	@Getter private final double optional;
	private boolean affectsMobs;
	
	DamageType(ChatColor color, String s) {
		this(color, s, true);
	}
	
	DamageType(ChatColor color, String s, double num) {
		this(color, s, true, num);
	}
	
	DamageType(ChatColor color, String s, boolean mob) {
		this(color, s, mob, 0);
	}
	
	DamageType(ChatColor color, String s, boolean aMob, double optionalNum) {
		this.color = color;
		this.display = s;
		this.optional = optionalNum;
		this.affectsMobs = aMob;
	}
	
	public String getDisplay() {
		return this.color + "" + ChatColor.BOLD + "(" + this.display + ")";
	}
	
	public boolean doesAffectMobs() {
		return this.affectsMobs;
	}
	
	public static DamageType getByReason(DamageCause reason) {
		for (DamageType type : values())
			if (type.name().equals(reason.name()))
				return type;
		return DamageType.FAILSAFE;
	}
}
