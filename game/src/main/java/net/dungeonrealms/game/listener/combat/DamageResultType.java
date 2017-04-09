package net.dungeonrealms.game.listener.combat;

import lombok.Getter;

import org.bukkit.Sound;

@Getter
public enum DamageResultType {
	
	NORMAL(),
	BLOCK(Sound.ENTITY_ZOMBIE_INFECT, 1.5F, 2.0F),
	DODGE(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2F, 1F),
	REFLECT(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2F, 1F);
	
	private final Sound sound;
	private final float volume;
	private final float pitch;
	
	DamageResultType() {
		//Random sound, this won't play.
		this(Sound.AMBIENT_CAVE, 0, 0);
	}
	
	DamageResultType(Sound sound, float volume, float pitch) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}
	
	public String getPastTenseName() {
		return name() + (name().endsWith("E") ? "" : "E") + "D";
	}
}
