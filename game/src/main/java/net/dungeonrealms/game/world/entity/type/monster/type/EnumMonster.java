package net.dungeonrealms.game.world.entity.type.monster.type;

import lombok.Getter;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.item.Item.ElementalAttribute;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Chase on Oct 7, 2015
 */
public enum EnumMonster {

    InfernalEndermen("infernalendermen", "Endermen", "", new String[]{""}, new String[]{""}, 20, ElementalAttribute.FIRE),
    Troll("troll", "Troll", "Steve", new String[]{"Strong", "Smelly"}, new String[]{"Warrior", "Rebel"}, 20, ElementalAttribute.POISON),
    Troll1("troll1", "Troll", "Steve", new String[]{"Strong", "Smelly"}, new String[]{"Warrior", "Rebel"}, 20, ElementalAttribute.POISON),
    Goblin("goblin", "Goblin", "Shrek", new String[]{"Short", "Ugly", "Smelly"}, new String[]{""}, 20, ElementalAttribute.FIRE),
    Bandit("bandit", "Bandit", "", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"}, new String[]{""}, 15, ElementalAttribute.POISON),
    Bandit1("bandit1", "Bandit", "", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"}, new String[]{""}, 15, ElementalAttribute.POISON),
    PassiveBandit("passivebandit", "Bandit", "", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"}, new String[]{""}, 15, ElementalAttribute.POISON),
    Pirate("pirate", "Pirate", "samsamsam1234", new String[]{""}, new String[]{""}),
    RangedPirate("rangedpirate", "Ranged Pirate", "samsamsam1234", new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"}, new String[]{""}),
    MayelPirate("mayelpirate", "Pirate", "samsamsam1234", new String[]{"Mayel"}, new String[]{""}),
    Naga("naga","Naga", "Das_Doktor", new String[]{"Weak"}, new String[]{"Shaman", "Mage"}, 25, ElementalAttribute.ICE),
    Tripoli("tripoli","Tripoli", "Xmattpt", new String[]{" "}, new String[]{"Soldier", "Commander"}, 3, ElementalAttribute.FIRE),
    Tripoli1("tripoli1","Tripoli", "Xmattpt", new String[]{" "}, new String[]{"Soldier", "Commander"}, 3, ElementalAttribute.FIRE),
    Golem("golem","Golem", "Steve", new String[]{"Enchanted", "Ironclad", "Enchanted"}, new String[]{""}, 30, ElementalAttribute.ICE),
    Spider1("spider1", "Spider", "Steve", new String[]{"Scary", "Spooky", "Hairy", "Giant"}, new String[]{""}, 10, ElementalAttribute.ICE),
    Spider2("spider2", "Spider", "Steve", new String[]{"Scary", "Spooky", "Hairy", "Giant"}, new String[]{""}, 10, ElementalAttribute.ICE),
    FireImp("imp", "Fire Imp", "Devil", new String[]{""}, new String[]{""}, 15, ElementalAttribute.FIRE),
    Blaze("blaze", "Blaze", "Blaze", new String[]{""}, new String[]{""}, 15, ElementalAttribute.FIRE),
    Mage("mage", "Mage", "Mage", new String[]{""}, new String[]{""}),
    LordsGuard("lordsguard", "Lords Guard", "Wither", new String[]{"Infernal"}, new String[]{""}, 80, ElementalAttribute.FIRE),

    StaffZombie("staffzombie", "Zombie", "Steve", new String[]{"Deadly", "Piercing"}, new String[]{"Ranger"}, 5, ElementalAttribute.PURE),
    Skeleton("skeleton", "Skeleton", "Steve", new String[]{"Infernal", "Demonic", "Wicked", "Fiendish", "Spooky"}, new String[]{""}, 5, ElementalAttribute.PURE),
    PassiveSkeleton1("passiveskeleton1", "Skeleton", "Steve", new String[]{"Infernal", "Demonic", "Wicked", "Fiendish", "Spooky"}, new String[]{""}, 5, ElementalAttribute.PURE),
    Skeleton1("skeleton1", "Skeleton", "Steve", new String[]{"Infernal", "Demonic", "Wicked", "Fiendish", "Spooky"}, new String[]{""}, 5, ElementalAttribute.PURE),
    Skeleton2("skeleton2", "Chaos Skeleton", "",  new String[]{""}, new String[]{""}, 5, ElementalAttribute.PURE),
    Zombie("zombie", "Greater Zombie", "Steve", new String[]{""}, new String[] {""}, 10, ElementalAttribute.FIRE),

    MagmaCube("magmacube", "Magma Cube", "Steve", new String[]{"Weak"}, new String[]{""}, 40, ElementalAttribute.FIRE),
    Wither("skeleton2", "Chaos Skeleton", "Wither", new String[]{""}, new String[]{""}, 5, ElementalAttribute.PURE),
    Daemon("daemon", "Daemon", "", new String[]{""}, new String[]{""}, 10, ElementalAttribute.PURE),
    Silverfish("silverfish", "Silverfish", "Steve", new String[]{""}, new String[]{""}, 15, ElementalAttribute.ICE),
	SpawnOfInferno("spawnofinferno", "Spawn of Inferno", "Steve", new String[]{""},new String[]{""}, 10, ElementalAttribute.FIRE),
	GreaterAbyssalDemon("demon", "Greater Abyssal Demon", "Steve", new String[]{""}, new String[] {""}),
	Monk("monk", "Crimson Crusader", "Yhmen", new String[] {""}, new String[] {""}, 15, ElementalAttribute.POISON),
	Lizardman("lizardman", "Lizardman", "Steve", new String[] {"Giant", "Tough"}, new String[] {""}, 10, ElementalAttribute.FIRE),

    Wolf("wolf", "Fierce Wolf", "", new String[]{""}, new String[]{""}, 10, ElementalAttribute.ICE),
    Undead("undead", "Undead", "", new String[]{""}, new String[]{""}),
    FrozenSkeleton("frozenskeleton", "Mountain Walker", "", new String[]{""}, new String[]{""}, 15, ElementalAttribute.ICE),
    Witch("witch", "Old Hag", "", new String[]{""}, new String[]{""}, 100, ElementalAttribute.POISON), // witches do poison damage 100% of the time
    Daemon2("daemon2", "Daemon", "", new String[]{""}, new String[]{""}, 10, ElementalAttribute.PURE),
    Acolyte("acolyte", "Acolyte", "", new String[]{""}, new String[]{""}, 20, ElementalAttribute.FIRE),
    Enderman("enderman", "Apparition", "", new String[]{""}, new String[]{""}, 15, ElementalAttribute.PURE),
    PassiveChicken("passivechicken", "Quillen", "", new String[]{"Fierce"}, new String[]{""}),
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
    public List<ElementalAttribute> possibleElementalTypes;
    public int elementalChance;

    EnumMonster(String idname, String name, String mobHead, String[] prefix, String[] suffix) {
    	this.idName = idname;
        this.name = name;
        this.mobHead = mobHead;
        this.prefix = prefix;
        this.suffix = suffix;
        this.possibleElementalTypes = new ArrayList<>();
        this.elementalChance = 0;
    }

    EnumMonster(String idname, String name, String mobHead, String[] prefix, String[] suffix, int elementalChance, ElementalAttribute... elements) {
        this.idName = idname;
        this.name = name;
        this.mobHead = mobHead;
        this.prefix = prefix;
        this.suffix = suffix;
        this.possibleElementalTypes = Arrays.asList(elements);
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

    public ElementalAttribute getRandomElement() {
        return possibleElementalTypes.get(new Random().nextInt(possibleElementalTypes.size()));
    }

    public static ItemStack getSkullItem(EnumMonster monster) {
        switch (monster) {
            case Pirate:
            case MayelPirate:
                return SkullTextures.PIRATE.getSkull();
            case Bandit:
            case Bandit1:
            case PassiveBandit:
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
            case PassiveSkeleton1:
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

	public ItemStack getSkullItem() {
		return getSkullItem(this);
	}

}
