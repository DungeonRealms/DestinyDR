package net.dungeonrealms.game.mechanic.dungeons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster.CustomEntityType;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum BossType {
	
	Pyromancer("Mad Bandit Pyromancer", CustomEntityType.BANDIT_PYRO,
			"WAHAHAHA! EXPLOSIONS! BOOM, BOOM, BOOM! I'm gonna blow you all up!",
			"Talk about going out with a...blast.", DungeonType.BANDIT_TROVE,
			643, 55, -454),
	
	Mayel("Mayel The Cruel", CustomEntityType.MAYEL, "cunning bandit lord",
			"How dare you challenge ME, the leader of the Cyrene Bandits! To me, my brethren, let us crush these incolents",
			"No... how could it be?", true, DungeonType.BANDIT_TROVE,
    		529, 55, -313,
    		Sound.AMBIENT_CAVE, 1F, 1F),
    		
    BurickPriest("The Priest", CustomEntityType.VARENGLADE_PRIEST,
    		"Hah! You think you can stop the might Burick? Get through me first!", "Burick! Forgive me! For I am not worthy...",
    		DungeonType.VARENGLADE, 37, 56, -5),
			
	Burick("Burick The Fanatic", CustomEntityType.BURICK, "corrupt unholy priest",
			"Ahahaha! You dare try to kill ME?! I am Burick, disciple of Goragath! None of you will leave this place alive!",
			"I will have my revenge!", true, DungeonType.VARENGLADE,
    		-364, 60, -1,
    		Sound.ENTITY_ENDERDRAGON_HURT, 4F, 0.5F),
	
	InfernalGhast("Infernal Ghast", CustomEntityType.INFERNAL_GHAST, "Aaaannh", "Hooouuaaaaaagh!!!!",
			DungeonType.THE_INFERNAL_ABYSS, Sound.ENTITY_GHAST_AMBIENT, 2F, 1F),
	
	InfernalGuard("Infernal Lords Guard", CustomEntityType.INFERNAL_GUARD,
			"I shall protect you my lord",
			"My lord... I have failed you...", DungeonType.THE_INFERNAL_ABYSS),
	
	InfernalAbyss("The Infernal Abyss", CustomEntityType.INFERNAL, "evil fire demon known as",
			"I have nothing to say to you foolish mortals, except for this: Burn.",
			"You...have... defeated me...ARGHHHH!!!!!", true, DungeonType.THE_INFERNAL_ABYSS,
	   		-54, 158, 646,
    		Sound.ENTITY_LIGHTNING_THUNDER, 1F, 1F);
	
	
	
	/*Aceron("aceron", "Aceron The Wicked", "", "", "", true), //Dark depths of aceron.
	
	Albranir("albranir", "Albranir", "",
			"A cold wind blankets the land as the evil presence of Albranir emerges",
			"", true);*/

	private String name;
	private CustomEntityType monster;
	private String prefix;
	private String greeting;
	private String deathMessage;
	private boolean finalBoss;
	private DungeonType type;
	
	private int x;
	private int y;
	private int z;
	
	private Sound sound;
	private float volume;
	private float pitch;
	
	BossType(String name, CustomEntityType monster, String greeting, String death, DungeonType type) {
		this(name, monster, greeting, death, type, 0, 0, 0);
	}
	
	BossType(String name, CustomEntityType monster, String greeting, String death, DungeonType type, int x, int y, int z) {
		this(name, monster, "", greeting, death, false, type, x, y, z, null, 1F, 1F);
	}
	
	BossType(String name, CustomEntityType monster, String greeting, String death, DungeonType type, Sound s, float volume, float pitch) {
		this(name, monster, "", greeting, death, false, type, 0, 0, 0, s, volume, pitch);
	}
	
	/**
	 * Is this a special boss?
	 * This means it does not spawn automatically but is spawned in relation to a location
	 * in the code. Ie: Infernal's Ghast gets spawned a few blocks above infernal.
	 * @return
	 */
	public boolean isSpecial() {
		// This is pretty stupid do since we're using an enum, but it's getting pretty clogged up there, and there won't be too many of these:
		return Math.abs(x) + Math.abs(y) + Math.abs(z) == 0 || Arrays.asList(BurickPriest).contains(this);
	}
	
	/**
	 * Gets the location to spawn this boss in at.
	 */
	public Location getLocation(World w) {
		return new Location(w, x, y, z);
	}
	
	/**
	 * Gets all bosses for the specified dungeon.
	 */
	public static List<BossType> getFor(DungeonType type) {
		List<BossType> list = new ArrayList<>();
		for (BossType t : values())
			if (t.getType() == type)
				list.add(t);
		return list;
	}
}
