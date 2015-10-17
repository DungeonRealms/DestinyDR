package net.dungeonrealms.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Chase on Oct 7, 2015
 */
public enum EnumMonster {

    Troll("Troll", "Steve", new String[]{"Strong", "Smelly"}, new String[]{"Warrior", "Rebel"}),
    Goblin("Goblin", "Shrek", new String[]{"Short", "Ugly", "Smelly"}, new String[]{""}),
    Bandit("Bandit", "Steve", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Majestic", "Intrigued", "Dignified", "Couragous", "Timid", "Gloomy", "Noble", "Naive", "Black"}, new String[]{""}),
    Pirate("Pirate", "samsamsam1234", new String[]{""}, new String[]{""}),
    RangedPirate("Ranged Pirate", "samsamsam1234", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Majestic", "Intrigued", "Dignified", "Couragous", "Timid", "Gloomy", "Noble", "Naive", "Black"}, new String[]{""}),
    Naga("Naga", "Das_Doktor", new String[]{"Weak"}, new String[]{"Shaman", "Mage"}),
    Tripoli("Tripoli", "Xmattpt", new String[]{" "}, new String[]{"Soldier", "Commander"}),
    Golem("Golem", "Steve", new String[]{"Enchanted", "Ironclad", "Enchanted Ironclad", "Ice"}, new String[]{" "}),
    Spider("Spider", "Steve", new String[]{"Scary", "Spookey", "Hairy"}, new String[]{""}),
    FireImp("Fire Imp", "Devil", new String[]{""}, new String[]{""}),
    Blaze("Blaze", "Blaze", new String[]{""}, new String[]{""}),
    Mage("Mage", "Mage", new String[]{""}, new String[]{""}),
    Skeleton("Skeleton", "Steve", new String[]{"Scary", "Spookey", "Spookey Scary"}, new String[]{"Archer", "Ranger"});
	
    public String name;
    public String mobHead;
    public String[] prefix;
    public String[] suffix;

    EnumMonster(String name, String mobHead, String[] prefix, String[] suffix) {
        this.name = name;
        this.mobHead = mobHead;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * Gets the Prefix
     *
     * @return
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
     * @return
     * @since 1.0
     */
    public String getSuffix() {
        List<String> list = Arrays.asList(suffix);
        Collections.shuffle(list);
        return list.get(0);
    }

}
