package net.dungeonrealms.game.world.entity.type.monster.type;

import lombok.Getter;
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

    InfernalEndermen("infernalendermen", "Endermen", "", new String[]{""}, new String[]{""}, Collections.singletonList("fire"), 20),
    Troll("troll", "Troll", "Steve", new String[]{"Strong", "Smelly"}, new String[]{"Warrior", "Rebel"}, Collections.singletonList("poison"), 20),
    Troll1("troll1", "Troll", "Steve", new String[]{"Strong", "Smelly"}, new String[]{"Warrior", "Rebel"}, Collections.singletonList("poison"), 20),
    Goblin("goblin", "Goblin", "Shrek", new String[]{"Short", "Ugly", "Smelly"}, new String[]{""}, Collections.singletonList("fire"), 20),
    Bandit("bandit", "Bandit", "", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"}, new String[]{""}, Collections.singletonList("poison"), 15),
    Bandit1("bandit1", "Bandit", "", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"}, new String[]{""}, Collections.singletonList("poison"), 15),
    Pirate("pirate", "Pirate", "samsamsam1234", new String[]{""}, new String[]{""}),
    RangedPirate("rangedpirate", "Ranged Pirate", "samsamsam1234", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"}, new String[]{""}),
    MayelPirate("mayelpirate", "Pirate", "samsamsam1234", new String[]{"Mayel"}, new String[]{""}),
    Naga("naga","Naga", "Das_Doktor", new String[]{"Weak"}, new String[]{"Shaman", "Mage"}, Collections.singletonList("ice"), 25),
    Tripoli("tripoli","Tripoli", "Xmattpt", new String[]{" "}, new String[]{"Soldier", "Commander"}, Collections.singletonList("fire"), 3),
    Tripoli1("tripoli1","Tripoli", "Xmattpt", new String[]{" "}, new String[]{"Soldier", "Commander"}, Collections.singletonList("fire"), 3),
    Golem("golem","Golem", "Steve", new String[]{"Enchanted", "Ironclad", "Enchanted"}, new String[]{""}, Collections.singletonList("ice"), 30),
    Spider1("spider1", "Spider", "Steve", new String[]{"Scary", "Spooky", "Hairy", "Giant"}, new String[]{""}, Collections.singletonList("ice"), 10),
    Spider2("spider2", "Spider", "Steve", new String[]{"Scary", "Spooky", "Hairy", "Giant"}, new String[]{""}, Collections.singletonList("ice"), 10),
    FireImp("imp", "Fire Imp", "Devil", new String[]{""}, new String[]{""}, Collections.singletonList("fire"), 15),
    Blaze("blaze", "Blaze", "Blaze", new String[]{""}, new String[]{""}, Collections.singletonList("fire"), 15),
    Mage("mage", "Mage", "Mage", new String[]{""}, new String[]{""}),
    LordsGuard("lordsguard", "Lords Guard", "Wither", new String[]{"Infernal"}, new String[]{""}, Collections.singletonList("fire"), 80),

    StaffZombie("staffzombie", "Zombie", "Steve", new String[]{"Deadly", "Piercing"}, new String[]{"Ranger"}, Collections.singletonList("pure"), 5),
    Skeleton("skeleton", "Skeleton", "Steve", new String[]{"Infernal", "Demonic", "Wicked", "Fiendish", "Spooky"}, new String[]{""}, Collections.singletonList("pure"), 5),
    Skeleton1("skeleton1", "Skeleton", "Steve", new String[]{"Infernal", "Demonic", "Wicked", "Fiendish", "Spooky"}, new String[]{""}, Collections.singletonList("pure"), 5),
    Skeleton2("skeleton2", "Chaos Skeleton", "",  new String[]{""}, new String[]{""}, Collections.singletonList("pure"), 5),
    Zombie("zombie", "Greater Zombie", "Steve", new String[]{""}, new String[] {""}, Collections.singletonList("fire"), 10),

    MagmaCube("magmacube", "Magma Cube", "Steve", new String[]{"Weak"}, new String[]{""}, Collections.singletonList("fire"), 40),
    Wither("skeleton2", "Chaos Skeleton", "Wither", new String[]{""}, new String[]{""}, Collections.singletonList("pure"), 5),
    Daemon("daemon", "Daemon", "", new String[]{""}, new String[]{""}, Collections.singletonList("pure"), 10),
    Silverfish("silverfish", "Silverfish", "Steve", new String[]{""}, new String[]{""}, Collections.singletonList("ice"), 15),
	SpawnOfInferno("spawnofinferno", "Spawn of Inferno", "Steve", new String[]{""},new String[]{""}, Collections.singletonList("fire"), 10),
	GreaterAbyssalDemon("demon", "Greater Abyssal Demon", "Steve", new String[]{""}, new String[] {""}),
	Monk("monk", "Crimson Crusader", "Yhmen", new String[] {""}, new String[] {""}, Collections.singletonList("poison"), 15),
	Lizardman("lizardman", "Lizardman", "Steve", new String[] {"Giant", "Tough"}, new String[] {""}, Collections.singletonList("fire"), 10),

    Wolf("wolf", "Fierce Wolf", "", new String[]{""}, new String[]{""}, Collections.singletonList("ice"), 10),
    Undead("undead", "Undead", "", new String[]{""}, new String[]{""}),
    FrozenSkeleton("frozenskeleton", "Mountain Walker", "", new String[]{""}, new String[]{""}, Collections.singletonList("ice"), 15),
    Witch("witch", "Old Hag", "", new String[]{""}, new String[]{""}, Collections.singletonList("poison"), 100), // witches do poison damage 100% of the time
    Daemon2("daemon2", "Daemon", "", new String[]{""}, new String[]{""}, Collections.singletonList("pure"), 10),
    Acolyte("acolyte", "Acolyte", "", new String[]{""}, new String[]{""}, Collections.singletonList("fire"), 20),
    Enderman("enderman", "Apparition", "", new String[]{""}, new String[]{""}, Collections.singletonList("pure"), 15),
    Pig("pig", "Pig", "", new String[]{""}, new String[]{""}),
    Bat("bat", "Bat", "", new String[]{""}, new String[]{""}),
    Cow("cow", "Cow", "", new String[]{""}, new String[]{""}),
    Ocelot("ocelot", "Ocelot", "", new String[]{""}, new String[]{""});

    @Getter
	public String idName;
    public String name;
    public String mobHead;
    public String[] prefix;
    public String[] suffix;
    public List<String> possibleElementalTypes;
    public int elementalChance;

    EnumMonster(String idname, String name, String mobHead, String[] prefix, String[] suffix) {
    	this.idName = idname;
        this.name = name;
        this.mobHead = mobHead;
        this.prefix = prefix;
        this.suffix = suffix;
        this.possibleElementalTypes = Collections.emptyList();
        this.elementalChance = 0;
    }

    EnumMonster(String idname, String name, String mobHead, String[] prefix, String[] suffix, List<String> elementalTypes, int elementalChance) {
        this.idName = idname;
        this.name = name;
        this.mobHead = mobHead;
        this.prefix = prefix;
        this.suffix = suffix;
        this.possibleElementalTypes = elementalTypes;
        this.elementalChance = elementalChance;
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

    public String getRandomElement() {
        return possibleElementalTypes.get(new Random().nextInt(possibleElementalTypes.size()));
    }

    public ItemStack getSkullItem(EnumMonster monster) {
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
            case StaffZombie:
                return SkullTextures.ZOMBIE.getSkull();
            case Acolyte:
                return SkullTextures.MONK.getSkull();
            default:
                return SkullTextures.PUG.getSkull();
        }
    }

}
