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
public enum EnumWeaponAttribute
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

    CRIT("CRITICAL HIT"),

    PENETRATION("ARMOR PENETRATION"),

    MON_DMG("MONSTER DMG"),

    PLAYER_DMG("PLAYER DMG"),

    LIFE_STEAL("LIFE STEAL"),

    ICE_DAMAGE("ICE DMG"),

    FIRE_DAMAGE("POISON DMG"),

    POISON_DAMAGE("POISON DMG"),

    ACCURACY("ACCURACY"),

    STRENGTH("STRENGTH"),

    VITALITY("VIT");

    @Getter
    private String name;

    private static AtomicCollection<EnumWeaponAttribute> atomicCollection = new AtomicCollection<>();

    private static boolean loaded;

    EnumWeaponAttribute(String name)
    {
        this.name = ChatColor.RED + name;
    }

    public static List<EnumWeaponAttribute> random(int maxAttributes)
    {
        AtomicReference<List<EnumWeaponAttribute>> attributeList;
        if (loaded)
        {
            attributeList = new AtomicReference<List<EnumWeaponAttribute>>(); // Create a new list per process
            attributeList.set(Lists.newArrayList());
            if (attributeList.get().isEmpty()) // Better be safe than sorry
            {
                IntStream.range(0, maxAttributes).parallel().forEach(max -> {
                    attributeList.get().add(atomicCollection.next());
                });
                return attributeList.get();
            }
        } else
        {
            // Weight is not final
            for (EnumWeaponAttribute enumWeaponAttibute : EnumWeaponAttribute.values())
                atomicCollection.getMap().get().put(0.5, enumWeaponAttibute);

            // Always add an empty entry
            atomicCollection.getMap().get().put(0.8, null);
            loaded = true;
            return random(maxAttributes); // Redo the process
        }
        return null;
    }
}
