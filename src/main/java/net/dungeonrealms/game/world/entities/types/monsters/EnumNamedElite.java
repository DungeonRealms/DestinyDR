package net.dungeonrealms.game.world.entities.types.monsters;

/**
 * Created by Kieran Quigley (Proxying) on 09-Jun-16.
 */
public enum EnumNamedElite {

    MITSUKI("mitsuki", "mitsuki the dominator"),
    KILATAN("kilatan", "daemon lord kilatan"),
    IMPATHEIMPALER("impa", "impa the impaler"),
    GREEDKING("greedking", "the king of greed"),
    COPJAK("cop'jak", "cop'jak"),
    BLAYSHAN("blayshan", "blayshan the naga"),
    ACERON("aceron", "???"), //TODO: No one in config with name Aceron. But custom loot file exists.
    ZION("zion", "skeleton king zion"),
    DURANOR("duranor", "duranor the cruel"),
    MOTHEROFDOOM("motherofdoom", "mother of doom"),
    NONE("none", "none");

    public String getTemplateStarter() {
        return templateStarter;
    }

    private String templateStarter;

    public String getConfigName() {
        return configName;
    }

    private String configName;

    EnumNamedElite(String templateStarter, String configName) {
        this.templateStarter = templateStarter;
        this.configName = configName;
    }
}
