package net.dungeonrealms.game.mechanic.dungeons;

import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster.CustomEntityType;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum BossType {
	Mayel("Mayel The Cruel", CustomEntityType.MAYEL, "cunning bandit lord",
			"How dare you challenge ME, the leader of the Cyrene Bandits! To me, my brethren, let us crush these incolents",
			"No... how could it be?", true, DungeonType.BANDIT_TROVE,
    		529, 55, -313,
    		Sound.AMBIENT_CAVE, 1F, 1F),
			
	Pyromancer("Mad Bandit Pyromancer", CustomEntityType.MELEE_SKELETON,
			"WAHAHAHA! EXPLOSIONS! BOOM, BOOM, BOOM! I'm gonna blow you all up!",
			"Talk about going out with a...blast.", DungeonType.BANDIT_TROVE),
			
	Burick("Burick The Fanatic", CustomEntityType.BURICK, "corrupt unholy priest",
			"Ahahaha! You dare try to kill ME?! I am Burick, disciple of Goragath! None of you will leave this place alive!",
			"I will have my revenge!", true, DungeonType.VARENGLADE,
    		-364, 60, -1,
    		Sound.ENTITY_ENDERDRAGON_HURT, 4F, 0.5F),
			
	InfernalAbyss("The Infernal Abyss", CustomEntityType.INFERNAL, "evil fire demon known as",
			"I have nothing to say to you foolish mortals, except for this: Burn.",
			"You...have... defeated me...ARGHHHH!!!!!", true, DungeonType.THE_INFERNAL_ABYSS,
    		-54, 158, 646,
    		Sound.ENTITY_LIGHTNING_THUNDER, 1F, 1F),
	
	InfernalGhast("Infernal Ghast", CustomEntityType.INFERNAL_GHAST, "", "Aaaannh", "Hooouuaaaaaagh!!!!",
			false, DungeonType.THE_INFERNAL_ABYSS, 0, 7, 0, Sound.ENTITY_GHAST_AMBIENT, 2F, 1F),
	
	LordsGuard("Infernal Lords Guard", CustomEntityType.INFERNAL_GUARD,
			"I shall protect you my lord",
			"My lord... I have failed you...", DungeonType.THE_INFERNAL_ABYSS);
	
	/*Aceron("aceron", "Aceron The Wicked", "", "", "", true),
	
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
	
	BossType(String name, CustomEntityType monster, DungeonType type) {
		this(name, monster, "", "", type);
	}
	
	BossType(String name, CustomEntityType monster, String greeting, String death, DungeonType type) {
		this(name, monster, "", greeting, death, false, type, 0, 0, 0, null, 1F, 1F);
	}
	
	public boolean isSpecialLocation() {
		return Math.abs(x) < 10 && y < 10 && Math.abs(z) < 10;
	}

	public Location getLocation(World world) {
		if (isSpecialLocation()) {// It's in relation to the boss.
			Dungeon d = DungeonManager.getDungeon(world);
			return d.getBoss().getBukkit().getLocation().add(x, y, z);
		}
		return new Location(world, x, y, z);
	}
}
