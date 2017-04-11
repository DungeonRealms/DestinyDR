package net.dungeonrealms.game.world.entity.type.pet;

import lombok.Getter;

/**
 * Created by Kieran on 10/15/2015.
 */
public enum EnumPets {
    //Snowman - Christmas
    //Killer Rabbit - Easter
    //Green Baby Sheep - St.Patrick's
    //Primed Creeper - 4th of July
    //Cave Spider - Halloween
    //Blue Baby Sheep - St.Andrew's
    //Magma Cube - Guy Fawkes/Bonfire Night
    //Pink Baby Sheep - Cancer Awareness (Maybe different for Breast/Lung etc based on the ribbons)
    //Pig (In Love) - Valentines Day
    //Adult Chicken - Thanksgiving
    WOLF(0, "WOLF", 95, "Wolf"),
    ENDERMITE(1, "ENDERMITE", 67, "Endermite"),
    SILVERFISH(2, "SILVERFISH", 60, "Silverfish", true),
    CAVE_SPIDER(3, "CAVESPIDER", 59, "Cave Spider"),
    BABY_ZOMBIE(4, "BABYZOMBIE", 54, "Baby Zombie"),
    BABY_PIGZOMBIE(5, "BABYPIGZOMBIE", 57, "Baby Pig Zombie"),
    SNOWMAN(6, "SNOWMAN", 56, "Snowman", true),
    OCELOT(7, "OCELOT", 98, "Ocelot"),
    RABBIT(8, "RABBIT", 101, "Rabbit"),
    CHICKEN(9, "CHICKEN", 93, "Chicken"),
    BAT(10, "BAT", 65, "Bat"),
    SLIME(11, "SLIME", 55, "Slime"),
    MAGMA_CUBE(12, "MAGMA_CUBE", 62, "Magma Cube"),
    CREEPER_OF_INDEPENDENCE(13, "CREEPER_INDEPENDENCE", 50, "Independence Creeper", true),
    BABY_HORSE(14, "BABY_HORSE", 64, "Baby Horse", true);

    @Getter private int id;
    @Getter private String name;
    @Getter private int eggShortData;
    @Getter private String displayName;
    @Getter private boolean special;
    
    EnumPets(int id, String name, int eggData, String display) {
    	this(id, name, eggData, display, false);
    }

    EnumPets(int id, String name, int eggShortData, String displayName, boolean event) {
        this.id = id;
        this.name = name;
        this.eggShortData = eggShortData;
        this.displayName = displayName;
        this.special = event;
    }

    public static EnumPets getById(int id) {
        for (EnumPets ep : values())
            if (ep.getId() == id)
                return ep;
        return null;
    }

    public static EnumPets getByName(String rawName) {
        for (EnumPets ep : values())
            if (ep.name.equalsIgnoreCase(rawName))
                return ep;
        return null;
    }
}
