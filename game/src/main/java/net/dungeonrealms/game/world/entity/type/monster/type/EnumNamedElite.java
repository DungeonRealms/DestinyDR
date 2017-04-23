package net.dungeonrealms.game.world.entity.type.monster.type;

import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster.CustomEntityType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Kieran Quigley (Proxying) on 09-Jun-16.
 */
@AllArgsConstructor
public enum EnumNamedElite {

	// TIER 1 //
    MITSUKI("mitsuki", "mitsuki the dominator", 1, EnumMonster.Bandit, CustomEntityType.MELEE_ZOMBIE),
    
    // TIER 2 //
    COPJAK("cop'jak", "cop'jak", 2, EnumMonster.Troll1, CustomEntityType.MELEE_ZOMBIE),
    LORD_TAYLOR("lordtaylor", "lord taylor", 2, EnumMonster.Zombie, CustomEntityType.MELEE_WITHER),
    
    // TIER 3 //
    IMPATHEIMPALER("impa", "impa the impaler", 3, EnumMonster.Imp, CustomEntityType.MELEE_WITHER),
    GREEDKING("greedking", "the king of greed", 3, EnumMonster.Goblin, CustomEntityType.MELEE_ZOMBIE),
    ZION("zion", "skeleton king zion", 3, EnumMonster.Skeleton, CustomEntityType.MELEE_SKELETON),
    
    // TIER 4 //
    BLAYSHAN("blayshan", "blayshan the naga", 4, EnumMonster.Naga, CustomEntityType.MELEE_ZOMBIE),
    DURANOR("duranor", "duranor the cruel", 4, EnumMonster.Imp, CustomEntityType.MELEE_SKELETON),
    MOTHEROFDOOM("MotherofDoom", "mother of doom", 4, EnumMonster.Spider2, CustomEntityType.LARGE_SPIDER),
    
    // TIER 5 //
    KILATAN("kilatan", "daemon lord kilatan", 5, EnumMonster.Imp, CustomEntityType.STAFF_SKELETON);

    @Getter private String templateStarter;
    @Getter private String configName;
    @Getter private int tier;
    @Getter private EnumMonster monster;
    @Getter private CustomEntityType entity;

    public static EnumNamedElite getFromName(String name){
        for(EnumNamedElite elite : values())
            if(elite.getConfigName().equalsIgnoreCase(name))
            	return elite;
        return null;
    }
}
