package net.dungeonrealms.game.world.entity.type.monster.type;

import net.dungeonrealms.game.mechanic.DungeonManager.DungeonType;

/**
 * Created by Chase on Oct 19, 2015
 */
public enum EnumDungeonBoss {

	Mayel("mayel", "Mayel The Cruel", "",
			"How dare you challenge ME, the leader of the Cyrene Bandits! To me, my brethren, let us crush these incolents",
			"No... how could it be?", true, DungeonType.BANDIT_TROVE),
			
	Pyromancer("pyromancer", "Mad Bandit Pyromancer", "",
			"WAHAHAHA! EXPLOSIONS! BOOM, BOOM, BOOM! I'm gonna blow you all up!",
			"Talk about going out with a...blast.", false, DungeonType.BANDIT_TROVE),
			
	Burick("burick", "Burick The Fanatic", "corrupt unholy priest",
			"Ahahaha! You dare try to kill ME?! I am Burick, disciple of Goragath! None of you will leave this place alive!",
			"I will have my revenge!", true, DungeonType.VARENGLADE),
			
	InfernalAbyss("infernalabyss", "The Infernal Abyss", "evil fire demon known as",
			"I have nothing to say to you foolish mortals, except for this: Burn.",
			"You...have... defeated me...ARGHHHH!!!!!", true, DungeonType.THE_INFERNAL_ABYSS),
	
	InfernalGhast("infernalghast", "Infernal Ghast", "",
			"",
			"", false, DungeonType.THE_INFERNAL_ABYSS),
	
	LordsGuard("lordsguard", "The Infernal Lords Guard", "",
			"I shall protect you my lord",
			"I'm... Sorry...", false, DungeonType.THE_INFERNAL_ABYSS);
	
	/*Aceron("aceron", "Aceron The Wicked", "", "", "", true),
	
	Albranir("albranir", "Albranir", "",
			"A cold wind blankets the land as the evil presence of Albranir emerges",
			"", true);*/

	private String name;
	private String greeting;
	private String death;
	private String nameid;
	private boolean isFinalBoss;
	private String prefix;
	private DungeonType type;
	
	EnumDungeonBoss(String nameID, String name, String prefix, String greeting, String death, boolean finalBoss, DungeonType type) {
		this.nameid = nameID;
		this.name = name;
		this.greeting = greeting;
		this.death = death;
		this.isFinalBoss = finalBoss;
		this.prefix = prefix;
		this.type = type;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getGreeting(){
		return this.greeting;
	}
	
	public String getDeathMessage(){
		return this.death;
	}
	
	public DungeonType getDungeonType(){
		return this.type;
	}
	
	public String getNameID(){
		return this.nameid;
	}
	
	public EnumDungeonBoss getBoss(String name){
		for(EnumDungeonBoss boss : values())
			if(boss.nameid.equalsIgnoreCase(name))
				return boss;
		return null;
	}

	/**
	 * @param bossName
	 * @return
	 */
	public static EnumDungeonBoss getByID(String bossName) {
		for(EnumDungeonBoss boss : values())
			if(boss.nameid.equalsIgnoreCase(bossName))
				return boss;
		return null;
	}
	
	
	public boolean isFinalBoss(){
		return this.isFinalBoss;
	}
	
	public String getPrefix(){
		return this.prefix;
	}
}
