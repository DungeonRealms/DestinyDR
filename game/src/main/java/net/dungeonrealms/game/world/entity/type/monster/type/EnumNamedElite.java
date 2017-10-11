package net.dungeonrealms.game.world.entity.type.monster.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster.CustomEntityType;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Created by Kieran Quigley (Proxying) on 09-Jun-16.
 */
@AllArgsConstructor
public enum EnumNamedElite {

    // TIER 1 //
    MITSUKI("Mitsuki The Dominator", "world",1, EnumMonster.Bandit, CustomEntityType.MELEE_ZOMBIE, -953, 72, 744, 300, 1),
    OLD_WHEAT_FARMER("Old Wheat Farmer", "world",1, EnumMonster.Bandit1, CustomEntityType.MELEE_SKELETON, -914, 59, 940, 400, 1),
    OLD_CARROT_FARMER("Old Carrot Farmer", "world",1, EnumMonster.Bandit1, CustomEntityType.MELEE_SKELETON, -918, 59, 937, 400, 1),

    // TIER 2 //
    COPJAK("Cop'jak", "world",2 ,EnumMonster.Troll1, CustomEntityType.MELEE_ZOMBIE, -82, 99, 1270, 1500, 2),
    LORD_TAYLOR("Lord Taylor", "world",2, EnumMonster.Zombie, CustomEntityType.MELEE_WITHER, -1034, 118, 1262, 400, 2),
    QUEEN_OF_THE_NEST("Queen of the Nest", "world",2, EnumMonster.Spider1, CustomEntityType.LARGE_SPIDER, 289, 38, 823, 500, 1),

    // TIER 3 //
    IMPA("Impa The Impaler", "world",3, EnumMonster.Imp, CustomEntityType.MELEE_WITHER, -144, 144, -3533, 1800, 1),
    GREED_KING("The King of Greed", "world",3, EnumMonster.Goblin, CustomEntityType.MELEE_ZOMBIE, 650, 120, 836, 2400, 3),
    ZION("Skeleton King Zion", "world",3, EnumMonster.Skeleton, CustomEntityType.MELEE_SKELETON, 841, 61, 1311, 2000, 2),
    PYRO_BANDIT("Pyro Bandit", "world",3, EnumMonster.Bandit, CustomEntityType.MELEE_WITHER, -331, 67, -407, 1900, 1), //650 - 810 Chest
    REDEYE_THE_CRUEL("Redeye The Cruel", "world",3, EnumMonster.Bandit, CustomEntityType.MELEE_WITHER, 188, 61, -594, 2100, 1), //790-920 HP Chest

    // TIER 4 //
    BLAYSHAN("Blayshan The Naga", "world",4, EnumMonster.Naga, CustomEntityType.MELEE_ZOMBIE, -278, 85, -202, 2500, 3),
    DURANOR("Duranor the Cruel", "world",4, EnumMonster.Imp, CustomEntityType.MELEE_SKELETON, -124, 162, -3167, 1300, 1),
    //    MOUNTAIN_KING("Mountain King", 4, EnumMonster.Skeleton2, CustomEntityType.BOW_SKELETON, -1649, 149, 1388, 1300, 1),
    MOTHER_OF_DOOM("Mother of Doom", "world",4, EnumMonster.Spider2, CustomEntityType.LARGE_SPIDER, -191, 144, -3621, 1200, 2),
    BLOOD_BUTCHER("Blood Butcher", "world",4, EnumMonster.Skeleton1, CustomEntityType.MELEE_WITHER, 230, 34, -3542, 2600, 1), //Better then blayshan pretty much

    // TIER 5 //
    KILATAN("Daemon Lord Kilatan", "world",5, EnumMonster.Imp, CustomEntityType.STAFF_SKELETON, -411, 33, -3487, 1800, 1),
    LIBRARIAN("The Librarian", "world",5, EnumMonster.Skeleton, CustomEntityType.BOW_SKELETON, -1590, 63, 1218, 2500, 1);

    @Getter
    private String displayName;
    @Getter
    @Setter
    private String world;
    @Getter
    private int tier;
    @Getter
    private EnumMonster monster;
    @Getter
    private CustomEntityType entity;

    private int x;
    private int y;
    private int z;

    @Getter
    private int respawnDelay;
    @Getter
    private int spread;

    /**
     * Return the location to spawn this elite at in the main world.
     */
    public Location getLocation() {
        return getLocation(GameAPI.getMainWorld());
    }

    /**
     * Generates a random level for this elite.
     */
    public int randomLevel() {
        return Utils.getRandomFromTier(getTier(), "low");
    }

    /**
     * Return the location to spawn this elite at.
     */
    public Location getLocation(World w) {
        return new Location(w, x, y, z);
    }

    public static EnumNamedElite getFromName(String name) {
        for (EnumNamedElite elite : values())
            if (elite.name().equalsIgnoreCase(name))
                return elite;
        return null;
    }
}
