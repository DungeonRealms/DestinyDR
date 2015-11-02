package net.dungeonrealms.entities.types.monsters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Chase on Oct 7, 2015
 */
public enum EnumMonster {

    Troll("troll", "Troll", "Steve", new String[]{"Strong", "Smelly"}, new String[]{"Warrior", "Rebel"}),
    Goblin("goblin", "Goblin", "Shrek", new String[]{"Short", "Ugly", "Smelly"}, new String[]{""}),
    Bandit("bandit", "Bandit", "Steve", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Majestic", "Intrigued", "Dignified", "Courageous", "Timid", "Gloomy", "Noble", "Naive"}, new String[]{""}),
    Pirate("pirate", "Pirate", "samsamsam1234", new String[]{""}, new String[]{""}),
    RangedPirate("rangedpirate", "Ranged Pirate", "samsamsam1234", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Majestic", "Intrigued", "Dignified", "Courageous", "Timid", "Gloomy", "Noble", "Naive"}, new String[]{""}),
    MayelPirate("mayelpirate", "Pirate", "samsamsam1234", new String[]{"Mayel"}, new String[]{""}),
    Naga("naga","Naga", "Das_Doktor", new String[]{"Weak"}, new String[]{"Shaman", "Mage"}),
    Tripoli("tripoli","Tripoli", "Xmattpt", new String[]{" "}, new String[]{"Soldier", "Commander"}),
    Golem("golem","Golem", "Steve", new String[]{"Enchanted", "Ironclad", "Enchanted Ironclad", "Ice"}, new String[]{" "}),
    Spider("spider", "Spider", "Steve", new String[]{"Scary", "Spooky", "Hairy"}, new String[]{""}),
    FireImp("imp", "Fire Imp", "Devil", new String[]{""}, new String[]{""}),
    Blaze("blaze", "Blaze", "Blaze", new String[]{""}, new String[]{""}),
    Mage("mage", "Mage", "Mage", new String[]{""}, new String[]{""}),
    Skeleton("skeleton1", "Skeleton", "Steve", new String[]{"Scary", "Spooky", "Spooky Scary"}, new String[]{"Archer", "Ranger"}),
    MagmaCube("magmacube", "Magma Cube", "Steve", new String[]{"Weak"}, new String[]{""}),
    Wither("skeleton2", "Chaos Skeleton", "Wither", new String[]{""}, new String[]{""}),
    Daemon("daemon", "Daemon", "Steve", new String[]{""}, new String[]{""}),
	SpawnOfInferno("spawnofinferno", "Spawn of Inferno", "Steve", new String[]{""},new String[]{""} ),
	GreaterAbyssalDemon("demon", "Greater Abyssal Demon", "Steve", new String[]{""}, new String[] {""});

	public String idName;
    public String name;
    public String mobHead;
    public String[] prefix;
    public String[] suffix;

    EnumMonster(String idname, String name, String mobHead, String[] prefix, String[] suffix) {
    	this.idName = idname;
        this.name = name;
        this.mobHead = mobHead;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * Gets the Prefix
     *
     * @return the prefix
     * @since 1.0
     */
    public String getPrefix() {
        List<String> list = Arrays.asList(prefix);
        Collections.shuffle(list);
        return list.get(0);
    }

    /**
     * Gets the suffix
     *
     * @return the suffix
     * @since 1.0
     */
    public String getSuffix() {
        List<String> list = Arrays.asList(suffix);
        Collections.shuffle(list);
        return list.get(0);
    }

	/**
	 * @param mob
	 * @return
	 */
	public static EnumMonster getMonsterByString(String mob) {
		for(EnumMonster mons : values()){
			if(mob.equalsIgnoreCase(mons.idName))
				return mons;
		}
		return null;
	}

}
