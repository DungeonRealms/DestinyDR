package net.dungeonrealms.vgame.item.weapon.attribute;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.api.collection.AtomicCollection;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumWeaponAttibute
{
    /**
     * % List
     * <p>
     * CRIT
     * MON_DMG
     * PLAYER_DMG
     * LIFE_STEAL
     * ACCURACY
     */

    PURE_DAMAGE("PURE DMG"),

    CRIT("CRIT"),

    PENETATION("ARMOR PENETATION"),

    MON_DMG("MONSTER DMG"),

    PLAYER_DMG("PLAYER DMG"),

    LIFE_STEAL("LIFE STEAL"),

    ICE_DAMAGE("ICE DMG"),

    FIRE_DAMAGE("POISONG DMG"),

    POISON_DAMAGE("POISON DMG"),

    ACCURACY("ACCURACY"),

    STRENGTH("STRENGTH"),

    VITALITY("VIT");

    @Getter
    private String name;

    private static AtomicCollection<EnumWeaponAttibute> atomicCollection = new AtomicCollection<>();

    private static boolean loaded;

    EnumWeaponAttibute(String name)
    {
        this.name = ChatColor.RED + name;
    }

    public static List<EnumWeaponAttibute> random(int maxAttibutes)
    {
        AtomicReference<List<EnumWeaponAttibute>> attibuteList;
        if (loaded)
        {
            attibuteList = new AtomicReference<List<EnumWeaponAttibute>>(); // Create a new list per process
            attibuteList.set(Lists.newArrayList());
            if (attibuteList.get().isEmpty()) // Better be safe than sorry
            {
                IntStream.range(0, maxAttibutes).parallel().forEach(max -> {
                    attibuteList.get().add(atomicCollection.next());
                });
                return attibuteList.get();
            }
        } else
        {
            // Weight is not final
            for (EnumWeaponAttibute enumWeaponAttibute : EnumWeaponAttibute.values())
                atomicCollection.getMap().get().put(0.5, enumWeaponAttibute);

            // Always add an empty entry
            atomicCollection.getMap().get().put(0.8, null);
            loaded = true;
            return random(maxAttibutes); // Redo the process
        }
        return null;
    }
}
