package net.dungeonrealms.vgame.goal.achievement;

import lombok.Getter;
import net.dungeonrealms.vgame.goal.achievement.explorer.ExplorerAchievement;

/**
 * Created by Giovanni on 8-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumAchievement
{
    EXPLORER_ANDALUCIA(0, new ExplorerAchievement("&a&lWELCOME TO ANDALUCIA",
            new String[]{"&7> Discover the province of Andalucia", "&7[Discovered the province of Andalucia]"}, 100, "cyrennica", "explorer.andalucia")),

    EXPLORER_CYRENE_PLAINS(1, new ExplorerAchievement("&a&lGREAT PLAINS OF CYRENE",
            new String[]{"&7> Discover the plains of Cyrene", "&7[Discovered the plains of Cyrene]"}, 500, "cyrene_plains", "explorer.cplains")),

    EXPLORER_HARRISONS(2, new ExplorerAchievement("&a&lHARRISONS FIELDS",
            new String[]{"&7> Discover Harrisons Fields", "&7[Discovered Harrisons Fields]"}, 500, "harrisons", "explorer.harrisons")),

    EXPLORER_DARKOAK(3, new ExplorerAchievement("&a&lDARKOAK",
            new String[]{"&7> Discover Darkoak", "&7[Discovered Darkoak]"}, 500, "darkOak", "explorer.darkOak")),
    EXPLORER_JAGGED(4, new ExplorerAchievement("&a&lJAGGED ROCKS",
            new String[]{"&7> Discover Jagged Rocks", "&7[Discovered Jagged Rocks]"}, 500, "jaggedRocks", "explorer.jaggedRocks")),

    EXPLORER_SKULLNECK(5, new ExplorerAchievement("&a&lSKULLNECK",
            new String[]{"&7> Discover Skullneck", "&7[Discovered Skullneck]"}, 1000, "skullneck", "explorer.skullneck")),

    EXPLORER_TROLLINGOR(6, new ExplorerAchievement("&a&lTROLLINGOR",
            new String[]{"&7> Discover Trollingor", "&7[Discovered Trollingor]"}, 2500, "trollingor", "explorer.trollingor")),

    EXPLORER_CRYSTALPEAK(7, new ExplorerAchievement("&a&lCRYSTALPEAK TOWER",
            new String[]{"&7> Discover the Crystalpeak Tower", "&7[Discovered the Crystalpeak Tower]"}, 5000, "crystalPeak", "explorer.crystalPeak")),

    EXPLORER_HELMCHEN(8, new ExplorerAchievement("&a&lHELMCHEN",
            new String[]{"&7> Discover Helmchen", "&7[Discovered Helmchen]"}, 1000, "helmchen", "explorer.helmchen")),

    EXPLORER_AL_SAHRA(9, new ExplorerAchievement("&a&lAL SAHRA",
            new String[]{"&7> Discover Al Sahra", "&7[Discovered Al Sahra]"}, 1000, "alSahra", "explorer.alSahra")),

    EXPLORER_TRIPOLI(10, new ExplorerAchievement("&a&lTRIPOLI",
            new String[]{"&7> Discover Tripoli", "&7[Discovered Tripoli]"}, 1000, "tripoli", "explorer.tripoli")),

    EXPLORER_DREADWOOD(11, new ExplorerAchievement("&a&lDREADWOOD",
            new String[]{"&7> Discover Dreadwood", "&7[Discovered Dreadwood]"}, 500, "dreadwood", "explorer.dreadwood")),

    EXPLORER_GLOOMY(12, new ExplorerAchievement("&a&lGLOOMY HALLOWS",
            new String[]{"&7> Discover Gloomy Hallows", "&7[Discovered Gloomy Hallows]"}, 1000, "gloomyHallows", "explorer.gloomyHallows")),

    EXPLORER_CRESTGAURD(13, new ExplorerAchievement("&a&lCREST GAURD",
            new String[]{"&7> Discover Crest Gaurd", "&7[Discovered Crest Gaurd]"}, 2500, "crestgaurd", "explorer.crestGaurd")),

    EXPLORER_FROZEN_NORTH(14, new ExplorerAchievement("&5&lTHE FROZEN NORTH",
            new String[]{"&7> Discover the Frozen North", "&7[Discovered the Frozen North]"}, 5000, "frozeNorth", "explorer.frozenNorth")),

    EXPLORER_AVALON(15, new ExplorerAchievement("&5&lLOST CITY OF AVALON",
            new String[]{"&7> Discover the Lost city of Avalon", "&7[Discovered the Lost city of Avalon]"}, 10000, "avalon", "explorer.avalon")),

    EXPLORER_CHIEFS(16, new ExplorerAchievement("&a&lCHIEF'S GLORY",
            new String[]{"&7> Discover Chief's Glory", "&7[Discovered Chief's Glory]"}, 7500, "chiefsGlory", "explorer.chiefsGlory")),

    EXPLORER_DEADPEAKS(17, new ExplorerAchievement("&a&lDEADPEAKS",
            new String[]{"&7> Discover Deadpeaks", "&7[Discovered Deadpeaks]"}, 1000, "deadpeaks", "explorer.deadpeaks")),

    EXPLORER_MURE(18, new ExplorerAchievement("&a&lMURE",
            new String[]{"&7> Discover Mule", "&7[Discovered Mure]"}, 1000, "mure", "explorer.mure")),

    EXPLORER_SEBRATA(19, new ExplorerAchievement("&a&lSEBRATA",
            new String[]{"&7> Discover Sebrata", "&7[Discovered Sebrata]"}, 1000, "sebrata", "explorer.sebrata")),

    EXPLORER_AWK(20, new ExplorerAchievement("&5&lGREAT GATE OF AWK",
            new String[]{"&7> Discover the Great Gate of Awk", "&7[Discovered the Great Gate of Awk]"}, 45000, "gateAwk", "explorer.gateAwk"));

    /**
     * COMBAT_MAYEL(21, "&a&lDEFEAT MAYEL THE CRUEL", new String[]{"&7> Defeat Mayel the Cruel", "&7[Defeated Mayel the Cruel]"}, 15000),
     * COMBAT_BURICK(22, "&a&lDEFEAT BURICK THE FANATIC", new String[]{"&7> Defeat Burick the Fanatic", "&7[Defeated Burick the Fanatic]"}, 50000),
     * COMBAT_ABYSS(23, "&a&lDEFEAT INFERNAL ABYSS", new String[]{"&7> Defeat the Infernal Abyss", "&7[Defeated the Infernal Abyss]"}, 75000),
     * COMBAT_MONSTER_I(24, "&a&lMONSTER HUNTER I", new String[]{"&7> Defeat 100 monsters", "&7[Defeated 100 monsters]"}, 250),
     * COMBAT_MONSTER_II(25, "&a&lMONSTER HUNTER II", new String[]{"&7> Defeat 300 monsters", "&7[Defeated 300 monsters]"}, 500),
     * COMBAT_MONSTER_III(26, "&a&lMONSTER HUNTER III", new String[]{"&7> Defeat 500 monsters", "&7[Defeated 500 monsters]"}, 1000),
     * COMBAT_MONSTER_IV(27, "&a&lMONSTER HUNTER IV", new String[]{"&7> Defeat 1000 monsters", "&7[Defeated 1000 monsters]"}, 3000),
     * COMBAT_MONSTER_V(28, "&a&lMONSTER HUNTER V", new String[]{"&7> Defeat 1500 monsters", "&7[Defeated 1500 monsters]"}, 6000),
     * COMBAT_GATEKEEPER(29, "&5&lDEFEAT GREAT GATEKEEPER", new String[]{"&7> Defeat the Great Gatekeeper of Awk", "&7[Defeated the Great Gatekeeper of Awk]"}, 100000),
     * COMBAT_MAN_I(30, "&a&lMAN HUNTER I", new String[]{"&7> Defeat 1 player", "&7[Defeated 1 player]"}, 250),
     * COMBAT_MAN_II(31, "&a&lMAN HUNTER II", new String[]{"&7> Defeat 3 player", "&7[Defeated 3 players]"}, 500),
     * COMBAT_MAN_III(32, "&a&lMAN HUNTER III", new String[]{"&7> Defeat 5 player", "&7[Defeated 5 players]"}, 1000),
     * COMBAT_MAN_IV(33, "&a&lMAN HUNTER IV", new String[]{"&7> Defeat 10 player", "&7[Defeated 10 players]"}, 3000),
     * COMBAT_MAN_V(34, "&a&lMAN HUNTER V", new String[]{"&7> Defeat 15 player", "&7[Defeated 15 players]"}, 6000);
     **/


    @Getter
    private int id;

    @Getter
    private Achievement achievement;

    EnumAchievement(int id, Achievement achievement)
    {
        this.id = id;
        this.achievement = achievement;
    }

    public static EnumAchievement getByID(int id)
    {
        for (EnumAchievement enumAchievement : values())
        {
            if (enumAchievement.getId() == id)
            {
                return enumAchievement;
            }
        }
        return null;
    }
}
