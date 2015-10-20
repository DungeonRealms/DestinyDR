package net.dungeonrealms.entities.types.monsters.boss;

/**
 * Created by Chase on Oct 19, 2015
 */
public enum EnumBoss {

	Mayel("Mayel The Cruel", 1, "How dare you challenge ME, the leader of the Cyrene Bandits! To me, my brethern, let us crush these incolents", "death"),
	Pyromancer("Mad Bandit Pyromancer", 1, "WAHAHAHA! EXPLOSIONS! BOOM, BOOM, BOOM! I'm gonna blow you all up!", "death"),
	Burick("Burick The Fanatic", 3, "Ahahaha! You dare try to kill ME?! I am Burick, disciple of Goragath! None of you will leave this place alive!", "death");

	public String name;
	public int tier;
	public String greeting;
	public String death;

	EnumBoss(String name, int tier, String greeting, String death) {
		this.name = name;
		this.tier = tier;
		this.greeting = greeting;
		this.death = death;
	}

}
