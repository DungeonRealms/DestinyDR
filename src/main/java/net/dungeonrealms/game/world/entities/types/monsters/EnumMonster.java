package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.game.miscellaneous.SkullTextures;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Chase on Oct 7, 2015
 */
public enum EnumMonster {

    Troll("troll", "Troll", "Steve", new String[]{"Strong", "Smelly"}, new String[]{"Warrior", "Rebel"}),
    Troll1("troll1", "Troll", "Steve", new String[]{"Strong", "Smelly"}, new String[]{"Warrior", "Rebel"}),
    Goblin("goblin", "Goblin", "Shrek", new String[]{"Short", "Ugly", "Smelly"}, new String[]{""}),
    Bandit("bandit", "Bandit", "", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"}, new String[]{""}),
    Bandit1("bandit1", "Bandit", "", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"}, new String[]{""}),
    Pirate("pirate", "Pirate", "samsamsam1234", new String[]{""}, new String[]{""}),
    RangedPirate("rangedpirate", "Ranged Pirate", "samsamsam1234", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"}, new String[]{""}),
    MayelPirate("mayelpirate", "Pirate", "samsamsam1234", new String[]{"Mayel"}, new String[]{""}),
    Naga("naga","Naga", "Das_Doktor", new String[]{"Weak"}, new String[]{"Shaman", "Mage"}),
    Tripoli("tripoli","Tripoli", "Xmattpt", new String[]{" "}, new String[]{"Soldier", "Commander"}),
    Tripoli1("tripoli1","Tripoli", "Xmattpt", new String[]{" "}, new String[]{"Soldier", "Commander"}),
    Golem("golem","Golem", "Steve", new String[]{"Enchanted", "Ironclad", "Enchanted Ironclad", "Ice"}, new String[]{" "}),
    Spider1("spider1", "Spider", "Steve", new String[]{"Scary", "Spooky", "Hairy", "Giant"}, new String[]{""}),
    Spider2("spider2", "Spider", "Steve", new String[]{"Scary", "Spooky", "Hairy", "Giant"}, new String[]{""}),
    FireImp("imp", "Fire Imp", "Devil", new String[]{""}, new String[]{""}),
    Blaze("blaze", "Blaze", "Blaze", new String[]{""}, new String[]{""}),
    Mage("mage", "Mage", "Mage", new String[]{""}, new String[]{""}),
    Skeleton("skeleton", "Skeleton", "Steve", new String[]{"Scary", "Spooky"}, new String[]{""}),
    Skeleton1("skeleton1", "Skeleton", "Steve", new String[]{"Scary", "Spooky"}, new String[]{""}),
    Skeleton2("skeleton2", "Chaos Skeleton", "",  new String[]{""}, new String[]{""}),
    MagmaCube("magmacube", "Magma Cube", "Steve", new String[]{"Weak"}, new String[]{""}),
    Wither("skeleton2", "Chaos Skeleton", "Wither", new String[]{""}, new String[]{""}),
    Daemon("daemon", "Daemon", "", new String[]{""}, new String[]{""}),
    Silverfish("silverfish", "Silverfish", "Steve", new String[]{""}, new String[]{""}),
	SpawnOfInferno("spawnofinferno", "Spawn of Inferno", "Steve", new String[]{""},new String[]{""} ),
	GreaterAbyssalDemon("demon", "Greater Abyssal Demon", "Steve", new String[]{""}, new String[] {""}),
	Monk("monk", "Crimson Crusader", "Yhmen", new String[] {""}, new String[] {""}),
	Lizardman("lizardman", "Lizardman", "Steve", new String[] {"Giant", "Tough"}, new String[] {""}),
	Zombie("zombie", "Greater Zombie", "Steve", new String[]{""}, new String[] {""}),
    Wolf("wolf", "Fierce Wolf", "", new String[]{""}, new String[]{""}),
    Undead("undead", "Undead", "", new String[]{""}, new String[]{""}),
    FrozenSkeleton("frozenskeleton", "Mountain Walker", "", new String[]{""}, new String[]{""}),
    Witch("witch", "Old Hag", "", new String[]{""}, new String[]{""}),
    Daemon2("daemon2", "Daemon", "", new String[]{""}, new String[]{""}),
    Acolyte("acolyte", "Acolyte", "", new String[]{""}, new String[]{""}),
    Pig("pig", "Pig", "", new String[]{""}, new String[]{""}),
    Bat("bat", "Bat", "", new String[]{""}, new String[]{""}),
    Cow("cow", "Cow", "", new String[]{""}, new String[]{""});

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
		for (EnumMonster mons : values()) {
			if (mob.equalsIgnoreCase(mons.idName)) {
                return mons;
            }
		}
		return null;
	}

    public ItemStack getSullItem(EnumMonster monster) {
        switch (monster) {
            case Pirate:
            case MayelPirate:
                return SkullTextures.PIRATE.getSkull();
            case Bandit:
            case Bandit1:
                if (new Random().nextBoolean()) {
                    return SkullTextures.BANDIT.getSkull();
                } else {
                    return SkullTextures.BANDIT_2.getSkull();
                }
            case Monk:
                return SkullTextures.MONK.getSkull();
            case FrozenSkeleton:
                return SkullTextures.FROZEN_SKELETON.getSkull();
            case Skeleton:
            case Skeleton1:
            case Skeleton2:
                return SkullTextures.SKELETON.getSkull();
            case FireImp:
                return SkullTextures.IMP.getSkull();
            case Goblin:
                return SkullTextures.GOBLIN.getSkull();
            case Troll:
            case Troll1:
                return SkullTextures.TROLL.getSkull();
            case Daemon:
            case Daemon2:
                return SkullTextures.DEVIL.getSkull();
            case Undead:
                if (new Random().nextBoolean()) {
                    return SkullTextures.ZOMBIE.getSkull();
                } else {
                    return SkullTextures.SKELETON.getSkull();
                }
            case Mage:
                return SkullTextures.MAGE.getSkull();
            case Tripoli:
            case Tripoli1:
                return SkullTextures.TRIPOLI_SOLDIER.getSkull();
            case Naga:
                return SkullTextures.NAGA.getSkull();
            case Lizardman:
                return SkullTextures.LIZARD.getSkull();
            case Zombie:
                return SkullTextures.ZOMBIE.getSkull();
            case Acolyte:
                return SkullTextures.MONK.getSkull();
            default:
                return SkullTextures.PUG.getSkull();
        }
    }

}
