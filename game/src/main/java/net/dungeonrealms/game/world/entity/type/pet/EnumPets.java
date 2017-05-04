package net.dungeonrealms.game.world.entity.type.pet;

import org.bukkit.entity.EntityType;

/**
 * Created by Kieran on 10/15/2015.
 */
public enum EnumPets {
    //Snowman - Christmas
    //Killer Rabbit - Easter
    //Green Baby BabySheep - St.Patrick's
    //Primed Creeper - 4th of July
    //Cave Spider - Halloween
    //Blue Baby BabySheep - St.Andrew's
    //Magma Cube - Guy Fawkes/Bonfire Night
    //Pink Baby BabySheep - Cancer Awareness (Maybe different for Breast/Lung etc based on the ribbons)
    //Pig (In Love) - Valentines Day
    //Adult Chicken - Thanksgiving
    WOLF(0, "WOLF", 95, "Wolf"),
    ENDERMITE(1, "ENDERMITE", 67, "Endermite"),
    SILVERFISH(2, "SILVERFISH", 60, "Silverfish"),
    CAVE_SPIDER(3, "CAVESPIDER", 59, "Cave Spider"),
    BABY_ZOMBIE(4, "BABYZOMBIE", 54, "Baby Zombie"),
    BABY_PIGZOMBIE(5, "BABYPIGZOMBIE", 57, "Baby Pig Zombie"),
    SNOWMAN(6, "SNOWMAN", 56, "Snowman"),
    OCELOT(7, "OCELOT", 98, "Ocelot"),
    RABBIT(8, "RABBIT", 101, "Rabbit"),
    CHICKEN(9, "CHICKEN", 93, "Chicken"),
    BAT(10, "BAT", 65, "Bat"),
    SLIME(11, "SLIME", 55, "Slime"),
    MAGMA_CUBE(12, "MAGMA_CUBE", 62, "Magma Cube"),
    CREEPER_OF_INDEPENDENCE(13, "CREEPER_INDEPENDENCE", 50, "Independence Creeper"),
    BABY_HORSE(14, "BABY_HORSE", 64, "Baby Horse"),
    BETA_ZOMBIE(15, "BETA_ZOMBIE", 54, "Beta Zombie",false),
    ENDERMAN(16, "ENDERMAN", 67, "Enderman",false,false),
    GUARDIAN(17, "GUARDIAN", 68, "Guardian",false),
    BABY_SHEEP(18, "BABY_SHEEP", EntityType.SHEEP.getTypeId(), "Baby Sheep",false),
    RAINBOW_SHEEP(18, "RAINBOW_SHEEP", EntityType.SHEEP.getTypeId(), "Rainbow Sheep",false);

    private int id;
    private String name;
    private int eggShortData;
    private String displayName;
    private boolean subGetsFree;
    //Maybe pets we have not released yet?
    private boolean showInGui;

    public int getId() {
        return id;
    }

    public String getRawName() {
        return name;
    }

    public int getEggShortData() {
        return eggShortData;
    }

    public String getDisplayName() {
        return displayName;
    }
    public boolean subGetsFree() {
        return subGetsFree;
    }
    public boolean showInGUI() {
        return showInGui;
    }

    EnumPets(int id, String name, int eggShortData, String displayName, boolean subGetsFree, boolean showInGui) {
        this.id = id;
        this.name = name;
        this.eggShortData = eggShortData;
        this.displayName = displayName;
        this.subGetsFree = subGetsFree;
        this.showInGui = showInGui;
    }

    EnumPets(int id, String name, int eggShortData, String displayName, boolean subGetsFree) {
        this(id,name,eggShortData,displayName,subGetsFree,true);
    }

    EnumPets(int id, String name, int eggShortData, String displayName) {
        this(id,name,eggShortData,displayName,true);
    }
    public static EnumPets getById(int id) {
        for (EnumPets ep : values()) {
            if (ep.getId() == id) {
                return ep;
            }
        }
        return null;
    }

    public static EnumPets getByName(String rawName) {
        if(rawName == null)return null;
        for (EnumPets ep : values()) {
            if (ep.name.equalsIgnoreCase(rawName)) {
                return ep;
            }
        }
        return null;
    }
}
