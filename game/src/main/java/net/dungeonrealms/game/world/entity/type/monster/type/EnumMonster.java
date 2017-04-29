package net.dungeonrealms.game.world.entity.type.monster.type;

import lombok.Getter;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mastery.NMSUtils;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.entity.type.monster.base.*;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.*;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.InfernalGhast;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.InfernalLordsGuard;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.*;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.*;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff.*;
import net.dungeonrealms.game.world.item.Item.ElementalAttribute;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * EnumMonster - A registry of all monsters in the game.
 * 
 * Redone on April 17th, 2017.
 * @author Kneesnap
 */
@Getter
public enum EnumMonster {

    Troll(l(CustomEntityType.MELEE_ZOMBIE), "Troll", null, SkullTextures.TROLL, l("Strong", "Smelly"), l("Warrior", "Rebel"), 20, ElementalAttribute.POISON),
    Troll1(l(CustomEntityType.MELEE_ZOMBIE), "Troll", null, SkullTextures.TROLL, l("Strong", "Smelly"), l("Warrior", "Rebel"), 20, ElementalAttribute.POISON),
    Goblin(l(CustomEntityType.MELEE_ZOMBIE), "Goblin", ItemType.AXE, SkullTextures.GOBLIN, l("Short", "Ugly", "Smelly"), 20, ElementalAttribute.FIRE),
    
    // Bandits
    Bandit(l(CustomEntityType.MELEE_ZOMBIE, CustomEntityType.BOW_SKELETON, CustomEntityType.MELEE_SKELETON), "Bandit", ItemType.AXE, SkullTextures.BANDIT, l("Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"), 15, ElementalAttribute.POISON),
    Bandit1(l(CustomEntityType.MELEE_ZOMBIE, CustomEntityType.BOW_SKELETON, CustomEntityType.MELEE_SKELETON, CustomEntityType.MELEE_SKELETON), "Bandit", ItemType.AXE, SkullTextures.BANDIT, l("Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"), 15, ElementalAttribute.POISON),
    PassiveBandit(l(CustomEntityType.MELEE_ZOMBIE, CustomEntityType.BOW_SKELETON, CustomEntityType.MELEE_SKELETON), "Bandit", ItemType.AXE, SkullTextures.BANDIT, l("Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"), 15, ElementalAttribute.POISON),
    
    // Skeletons
    FrozenSkeleton(l(CustomEntityType.MELEE_SKELETON, CustomEntityType.BOW_SKELETON, CustomEntityType.BOW_SKELETON), "Mountain Walker", null, SkullTextures.FROZEN_SKELETON, 15, ElementalAttribute.ICE),
    Skeleton(l(CustomEntityType.MELEE_SKELETON, CustomEntityType.MELEE_SKELETON, CustomEntityType.BOW_SKELETON), "Skeleton", ItemType.BOW, SkullTextures.SKELETON, l("Infernal", "Demonic", "Wicked", "Fiendish", "Spooky"), 5, ElementalAttribute.PURE),
    PassiveSkeleton1(l(CustomEntityType.MELEE_SKELETON, CustomEntityType.MELEE_SKELETON, CustomEntityType.BOW_SKELETON), "Skeleton", ItemType.BOW, SkullTextures.SKELETON, l("Infernal", "Demonic", "Wicked", "Fiendish", "Spooky"), 5, ElementalAttribute.PURE),
    Skeleton1(l(CustomEntityType.MELEE_SKELETON, CustomEntityType.MELEE_SKELETON, CustomEntityType.BOW_SKELETON), "Skeleton", ItemType.BOW, SkullTextures.SKELETON, l("Infernal", "Demonic", "Wicked", "Fiendish", "Spooky"), 5, ElementalAttribute.PURE),
    Skeleton2(l(CustomEntityType.MELEE_SKELETON, CustomEntityType.BOW_SKELETON, CustomEntityType.BOW_SKELETON), "Chaos Skeleton", ItemType.BOW, SkullTextures.SKELETON, 5, ElementalAttribute.PURE),
    
    // Pirates
    Pirate(l(CustomEntityType.MELEE_SKELETON, CustomEntityType.BOW_SKELETON), "Pirate", null, SkullTextures.PIRATE),
    RangedPirate(l(CustomEntityType.BOW_SKELETON), "Ranged Pirate", null, SkullTextures.PIRATE, l("Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy", "Timid", "Gloomy", "Noble", "Naive"), l("")),
    
    Naga(l(CustomEntityType.BOW_SKELETON, CustomEntityType.STAFF_SKELETON, CustomEntityType.MELEE_SKELETON), "Naga", null, SkullTextures.NAGA, l("Weak"), l("Shaman", "Mage"), 25, ElementalAttribute.ICE),
    Tripoli(l(CustomEntityType.MELEE_ZOMBIE), "Tripoli", ItemType.AXE, SkullTextures.TRIPOLI_SOLDIER, l(""), l("Soldier", "Commander"), 3, ElementalAttribute.FIRE),
    Tripoli1(l(CustomEntityType.MELEE_ZOMBIE), "Tripoli", ItemType.AXE, SkullTextures.TRIPOLI_SOLDIER, l(""), l("Soldier", "Commander"), 3, ElementalAttribute.FIRE),
    Golem(l(CustomEntityType.MELEE_GOLEM), "Golem", ItemType.SWORD, l("Enchanted", "Ironclad", "Enchanted"), 30, ElementalAttribute.ICE),
    Spider1(l(CustomEntityType.LARGE_SPIDER), "Spider", ItemType.SWORD, l("Scary", "Spooky", "Hairy", "Giant"), 10, ElementalAttribute.ICE),
    Spider2(l(CustomEntityType.SMALL_SPIDER), "Spider", ItemType.SWORD, l("Scary", "Spooky", "Hairy", "Giant"), 10, ElementalAttribute.ICE),
    Imp(l(CustomEntityType.STAFF_SKELETON), "Fire Imp", ItemType.STAFF, SkullTextures.IMP, 15, ElementalAttribute.FIRE),
    Blaze(l(CustomEntityType.STAFF_BLAZE), "Blaze", ItemType.STAFF, 15, ElementalAttribute.FIRE),
    Mage(l(CustomEntityType.STAFF_SKELETON), "Mage", ItemType.STAFF, SkullTextures.MAGE),

    // Daemons
    Daemon(l(CustomEntityType.DR_PIGMAN), "Daemon", ItemType.POLEARM, SkullTextures.DEVIL, 10, ElementalAttribute.PURE),
    Daemon2(l(CustomEntityType.STAFF_SKELETON), "Daemon", ItemType.STAFF, SkullTextures.DEVIL, 10, ElementalAttribute.PURE),
    
    // Zombie
    StaffZombie(l(CustomEntityType.STAFF_ZOMBIE), "Zombie", ItemType.STAFF, SkullTextures.ZOMBIE, l("Deadly", "Piercing"), l("Ranger"), 5, ElementalAttribute.PURE),
    Zombie(l(CustomEntityType.MELEE_ZOMBIE), "Greater Zombie", ItemType.SWORD, SkullTextures.ZOMBIE, 10, ElementalAttribute.FIRE),

    MagmaCube(l(CustomEntityType.DR_MAGMA), "Magma Cube", null, 40, ElementalAttribute.FIRE),
    Silverfish(l(CustomEntityType.DR_SILVERFISH), "Silverfish", ItemType.SWORD, 15, ElementalAttribute.ICE),
	
	// Misc
	Monk(l(CustomEntityType.MELEE_ZOMBIE), "Crimson Crusader", ItemType.POLEARM, SkullTextures.MONK, 15, ElementalAttribute.POISON),
	Lizardman(l(CustomEntityType.MELEE_ZOMBIE), "Lizardman", ItemType.POLEARM, SkullTextures.LIZARD, new String[] {"Giant", "Tough"}, 10, ElementalAttribute.FIRE),

    Wolf(l(CustomEntityType.DR_WOLF), "Fierce Wolf", null, 10, ElementalAttribute.ICE),
    Undead(l(CustomEntityType.MELEE_ZOMBIE), "Undead", ItemType.SWORD, SkullTextures.UNDEAD),
    
    Witch(l(CustomEntityType.DR_WITCH), "Old Hag", null, 100, ElementalAttribute.POISON), // witches do poison damage 100% of the time
    Acolyte(l(CustomEntityType.BOW_SKELETON, CustomEntityType.STAFF_SKELETON, CustomEntityType.MELEE_SKELETON), "Acolyte", null, SkullTextures.MONK, 20, ElementalAttribute.FIRE),
    
    // Dungeon
    MayelPirate(l(CustomEntityType.STAFF_SKELETON, CustomEntityType.BOW_SKELETON, CustomEntityType.MELEE_SKELETON), "Pirate", null, SkullTextures.PIRATE, l("Mayel"), l("")),
    Enderman(l(CustomEntityType.MELEE_ENDERMAN), "Apparition", ItemType.SWORD, 15, ElementalAttribute.PURE),
    SpawnOfInferno(l(CustomEntityType.DR_MAGMA), "Spawn of Inferno", null, 10, ElementalAttribute.FIRE),
	Demon(l(CustomEntityType.DR_SILVERFISH), "Greater Abyssal Demon", ItemType.SWORD),
	InfernalEndermen(l(CustomEntityType.INFERNAL_ENDERMAN), "Endermen", null, 20, ElementalAttribute.FIRE),
    
    // Vanilla NonHostiles
	PassiveChicken(l(CustomEntityType.PASSIVE_CHICKEN), "Quillen", ItemType.SWORD),
    Pig(l(CustomEntityType.PIG), "Pig"),
    Bat(l(CustomEntityType.BAT), "Bat"),
    Cow(l(CustomEntityType.COW), "Cow"),
    Ocelot(l(CustomEntityType.OCELOT), "Ocelot");

    private CustomEntityType[] clazzes;
    private String name;
    private SkullTextures skull;
    private String[] prefix;
    private String[] suffix;
    private List<ElementalAttribute> possibleElementalTypes;
    private int elementalChance;
    private ItemType weaponType;
    private boolean friendly = false;
    
    EnumMonster(CustomEntityType[] c, String name) {
    	this(c, name, null);
    	this.friendly = true;
    }
    
    EnumMonster(CustomEntityType[] c, String name, ItemType w) {
    	this(c, name, w, SkullTextures.PUG);
    }
    
    EnumMonster(CustomEntityType[] c, String name, ItemType w, SkullTextures skull) {
    	this(c, name, w, skull, new String[0], new String[0]);
    }

    EnumMonster(CustomEntityType[] c, String name, ItemType w, SkullTextures skull, String[] prefix, String[] suffix) {
    	this(c, name, w, skull, prefix, suffix, 0);
    }
    
    EnumMonster(CustomEntityType[] c, String name, ItemType w, int eChance, ElementalAttribute... elements) {
    	this(c, name, w, SkullTextures.PUG, eChance, elements);
    }
    
    EnumMonster(CustomEntityType[] c, String name, ItemType w, SkullTextures skull, int eChance, ElementalAttribute... elements) {
    	this(c, name, w, skull, new String[0], eChance, elements);
    }
    
    EnumMonster(CustomEntityType[] c, String name, ItemType w, String[] prefixes, int eChance, ElementalAttribute... elements) {
    	this(c, name, w, SkullTextures.PUG, prefixes, new String[0], eChance, elements);
    }
    
    EnumMonster(CustomEntityType[] c, String name, ItemType w, SkullTextures skull, String[] prefixes, int eChance, ElementalAttribute... elements) {
    	this(c, name, w, skull, prefixes, new String[0], eChance, elements);
    }

    EnumMonster(CustomEntityType[] c, String name, ItemType weaponType, SkullTextures skull, String[] prefix, String[] suffix, int elementalChance, ElementalAttribute... elements) {
        this.clazzes = c;
        this.name = name;
        this.skull = skull;
        this.prefix = prefix;
        this.suffix = suffix;
        this.possibleElementalTypes = Arrays.asList(elements);
        this.elementalChance = elementalChance;
        this.weaponType = weaponType;
    }
    
    public String getIdName() {
    	return name().toLowerCase();
    }

    public String getPrefix() {
    	return prefix[new Random().nextInt(prefix.length)];
    }

    public String getSuffix() {
        return suffix[new Random().nextInt(suffix.length)];
    }

	public static EnumMonster getMonsterByString(String mob) {
		for (EnumMonster mons : values())
			if (mob.equalsIgnoreCase(mons.getIdName()))
                return mons;
		return null;
	}
	
	public static EnumMonster getByName(String name) {
		return getMonsterByString(name);
	}

    public ElementalAttribute getRandomElement() {
        return possibleElementalTypes.get(new Random().nextInt(possibleElementalTypes.size()));
    }

	public ItemStack getSkullItem() {
		return getSkull().getSkull();
	}
	/**
	 * Selects from the choice of random entity classes.
	 */
	public CustomEntityType getCustomEntity() {
		return clazzes[new Random().nextInt(clazzes.length)];
	}
	
	//This is here to be used as a shortcut. so we can do l(1, 2) instead of new int[] {1, 2};
 	@SafeVarargs
	private static <T> T[] l(T... args) {
		return args;
	}
 	
 	@Getter
 	public enum CustomEntityType {
 		
 		//  MELEE  //
 		MELEE_GOLEM(MeleeGolem.class, EntityGolem.class, 99),
 		MELEE_ZOMBIE(MeleeZombie.class, EntityZombie.class, 54),
 		LARGE_SPIDER(LargeSpider.class, EntitySpider.class, 52),
 		SMALL_SPIDER(SmallSpider.class, EntityCaveSpider.class, 59),
 		MELEE_WITHER(MeleeWitherSkeleton.class, EntitySkeleton.class, 51),
 		MELEE_SKELETON(MeleeSkeleton.class, EntitySkeleton.class, 51),
        MELEE_ENDERMAN(MeleeEnderman.class, EntityEnderman.class, 58),
        
        //  STAFF  //
        STAFF_BLAZE(BasicEntityBlaze.class, EntityBlaze.class, 61),
        STAFF_ZOMBIE(StaffZombie.class, EntityZombie.class, 54),
        STAFF_SKELETON(StaffSkeleton.class, EntitySkeleton.class, 51),
        
        //  BOW  //
        BOW_SKELETON(RangedSkeleton.class, EntitySkeleton.class, 51),
        BOW_ZOMBIE(RangedZombie.class, EntityZombie.class, 54),
        BOW_WITHER(RangedWitherSkeleton.class, EntitySkeleton.class, 51),
        
        //  PASSIVE  //
        PASSIVE_CHICKEN(PassiveDRChicken.class, 1),
        
        PIG(EntityPig.class),
        BAT(EntityBat.class),
        COW(EntityCow.class),
        OCELOT(EntityOcelot.class),
        
        //  BASE  //
        DR_SPIDER(DRSpider.class, 59),
        DR_WITHER(DRWitherSkeleton.class, 51),
        DR_BLAZE(DRBlaze.class, 61),
        DR_MAGMA(DRMagma.class, 62),
        DR_PIGMAN(DRPigman.class, 57),
        DR_SILVERFISH(DRSilverfish.class, 60),
        DR_WOLF(DRWolf.class, 95),
        DR_WITCH(DRWitch.class, 66),
        DR_ZOMBIE(DRZombie.class, 54),
        DR_GOLEM(DRGolem.class, 99),
        DR_ENDERMAN(DREnderman.class, 58),
        DR_GHAST(DRGhast.class, 56),
        
        //  DUNGEON  //
        INFERNAL_GHAST(InfernalGhast.class, EntityGhast.class, 56),
        INFERNAL_ENDERMAN(InfernalEndermen.class, EntityEnderman.class, 58),
        INFERNAL_GUARD(InfernalLordsGuard.class, EntitySkeleton.class, 51),
        
        
        //  BOSSES  //
        MAYEL(Mayel.class, EntitySkeleton.class, 51),
        BURICK(Burick.class, EntitySkeleton.class, 51),
        INFERNAL(InfernalAbyss.class, EntitySkeleton.class, 51);
 		
 		private Class<? extends EntityInsentient> clazz;
 		private Class<? extends EntityInsentient> superClass;
 		private int entityId;
 		
 		CustomEntityType(Class<? extends EntityInsentient> cls) {
 			this(cls, null, 0);
 		}
 		
 		@SuppressWarnings("unchecked")
		CustomEntityType(Class<? extends EntityInsentient> cls, int id) {
 			this(cls, (Class<? extends EntityInsentient>) cls.getSuperclass(), id);
 		}
 		
 		CustomEntityType(Class<? extends EntityInsentient> cls, Class<? extends EntityInsentient> cls2, int type) {
 			this.clazz = cls;
 			this.superClass = cls2;
 			this.entityId = type;
 		}
 		
 		/**
 		 * Registers this entity in NMS.
 		 */
		public void register() {
 			if (getSuperClass() != null)
 				NMSUtils.registerEntity(getClazz().getSimpleName(), getEntityId(), getSuperClass(), getClazz());
 		}
 	}

	public boolean isPassive() {
		return name().contains("Passive");
	}
}
