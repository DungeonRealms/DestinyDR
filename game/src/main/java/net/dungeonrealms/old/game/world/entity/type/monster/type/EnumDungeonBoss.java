package net.dungeonrealms.old.game.world.entity.type.monster.type;

/**
 * Created by Chase on Oct 19, 2015
 */
public enum EnumDungeonBoss {

    Mayel("mayel", "Mayel The Cruel", 1, "How dare you challenge ME, the leader of the Cyrene Bandits! To me, my brethren, let us crush these incolents", "No... how could it be?"),
    Pyromancer("pyromancer", "Mad Bandit Pyromancer", 1, "WAHAHAHA! EXPLOSIONS! BOOM, BOOM, BOOM! I'm gonna blow you all up!", "Talk about going out with a...blast."),
    Burick("burick", "Burick The Fanatic", 3, "Ahahaha! You dare try to kill ME?! I am Burick, disciple of Goragath! None of you will leave this place alive!", "I will have my revenge!"),
    InfernalAbyss("infernalabyss", "Infernal Abyss", 4, "I have nothing to say to you foolish mortals, except for this: Burn.", "You have defeated me. ARGHGHHG!"),
    InfernalGhast("infernalghast", "Infernal Abyss", 4, "I have nothing to say to you foolish mortals, except for this: Burn.", "You have defeated me. ARGHGHHG!"),
    LordsGuard("lordsguard", "The Infernal Lords Guard", 4, "I shall protect you my lord", "I'm... Sorry..."),
    Aceron("aceron", "Aceron The Wicked", 5, "", ""),
    Albranir("albranir", "Albranir", 4, "A cold wind blankets the land as the evil presence of Albranir emerges", "");

    public String name;
    public int tier;
    public String greeting;
    public String death;
    public String nameid;

    EnumDungeonBoss(String nameID, String name, int tier, String greeting, String death) {
        this.nameid = nameID;
        this.name = name;
        this.tier = tier;
        this.greeting = greeting;
        this.death = death;
    }

    public EnumDungeonBoss getBoss(String name) {
        for (EnumDungeonBoss boss : values()) {
            if (boss.nameid.equalsIgnoreCase(name))
                return boss;
        }
        return null;
    }

    /**
     * @param bossName
     * @return
     */
    public static EnumDungeonBoss getByID(String bossName) {
        for (EnumDungeonBoss boss : values())
            if (boss.nameid.equalsIgnoreCase(bossName))
                return boss;
        return null;
    }

}
