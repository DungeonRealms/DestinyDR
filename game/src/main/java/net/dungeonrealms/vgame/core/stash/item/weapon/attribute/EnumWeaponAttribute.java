package net.dungeonrealms.vgame.core.stash.item.weapon.attribute;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.api.collection.AtomicCollection;
import net.dungeonrealms.vgame.core.stash.item.EnumItemTier;
import net.dungeonrealms.vgame.core.stash.item.meta.AttributeMeta;
import org.bukkit.ChatColor;

import java.util.Collection;
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

    EMPTY(0, "", null),

    PURE_DAMAGE(1, "PURE DMG",
            new AttributeMeta(EnumItemTier.ONE, 1, 5),
            new AttributeMeta(EnumItemTier.TWO, 1, 8),
            new AttributeMeta(EnumItemTier.THREE, 1, 15),
            new AttributeMeta(EnumItemTier.FOUR, 1, 25),
            new AttributeMeta(EnumItemTier.FIVE, 1, 45)),

    CRITICAL_HIT(2, "CRIT. CHANCE",
            new AttributeMeta(EnumItemTier.ONE, 1, 2, true),
            new AttributeMeta(EnumItemTier.TWO, 1, 4, true),
            new AttributeMeta(EnumItemTier.THREE, 1, 5, true),
            new AttributeMeta(EnumItemTier.FOUR, 1, 7, true),
            new AttributeMeta(EnumItemTier.FIVE, 1, 10, true)),


    PENETRATION(3, "ARMOR PENETRATION",
            new AttributeMeta(EnumItemTier.ONE, 1, 1, true),
            new AttributeMeta(EnumItemTier.TWO, 1, 3, true),
            new AttributeMeta(EnumItemTier.THREE, 1, 5, true),
            new AttributeMeta(EnumItemTier.FOUR, 1, 8, true),
            new AttributeMeta(EnumItemTier.FIVE, 1, 10, true)),

    MON_DMG(4, "vs. MONSTERS",
            new AttributeMeta(EnumItemTier.ONE, 1, 10),
            new AttributeMeta(EnumItemTier.TWO, 1, 12),
            new AttributeMeta(EnumItemTier.THREE, 1, 15),
            new AttributeMeta(EnumItemTier.FOUR, 1, 20),
            new AttributeMeta(EnumItemTier.FIVE, 1, 27)),

    PLAYER_DMG(5, "vs. PLAYERS",
            new AttributeMeta(EnumItemTier.ONE, 1, 10),
            new AttributeMeta(EnumItemTier.TWO, 1, 12),
            new AttributeMeta(EnumItemTier.THREE, 1, 15),
            new AttributeMeta(EnumItemTier.FOUR, 1, 20),
            new AttributeMeta(EnumItemTier.FIVE, 1, 27)),

    LIFE_STEAL(6, "LIFE STEAL",
            new AttributeMeta(EnumItemTier.ONE, 1, 30, true),
            new AttributeMeta(EnumItemTier.TWO, 1, 15, true),
            new AttributeMeta(EnumItemTier.THREE, 1, 12, true),
            new AttributeMeta(EnumItemTier.FOUR, 1, 7, true),
            new AttributeMeta(EnumItemTier.FIVE, 1, 8, true)),

    ICE_DAMAGE(7, "ICE DMG",
            new AttributeMeta(EnumItemTier.ONE, 1, 4),
            new AttributeMeta(EnumItemTier.TWO, 1, 9),
            new AttributeMeta(EnumItemTier.THREE, 1, 15),
            new AttributeMeta(EnumItemTier.FOUR, 1, 25),
            new AttributeMeta(EnumItemTier.FIVE, 1, 55)),

    FIRE_DAMAGE(8, "FIRE DMG",
            new AttributeMeta(EnumItemTier.ONE, 1, 4),
            new AttributeMeta(EnumItemTier.TWO, 1, 9),
            new AttributeMeta(EnumItemTier.THREE, 1, 15),
            new AttributeMeta(EnumItemTier.FOUR, 1, 25),
            new AttributeMeta(EnumItemTier.FIVE, 1, 55)),

    POISON_DAMAGE(9, "POISON DMG",
            new AttributeMeta(EnumItemTier.ONE, 1, 4),
            new AttributeMeta(EnumItemTier.TWO, 1, 9),
            new AttributeMeta(EnumItemTier.THREE, 1, 14),
            new AttributeMeta(EnumItemTier.FOUR, 1, 26),
            new AttributeMeta(EnumItemTier.FIVE, 1, 55)),

    ACCURACY(10, "ACCURACY",
            new AttributeMeta(EnumItemTier.ONE, 1, 10),
            new AttributeMeta(EnumItemTier.TWO, 1, 12),
            new AttributeMeta(EnumItemTier.THREE, 1, 24),
            new AttributeMeta(EnumItemTier.FOUR, 1, 28),
            new AttributeMeta(EnumItemTier.FIVE, 1, 35));

    @Getter
    private int id;

    @Getter
    private String name;

    @Getter
    private AttributeMeta[] attributeMetas;

    private static AtomicCollection<EnumWeaponAttribute> atomicCollection = new AtomicCollection<>();

    private static boolean loaded;

    EnumWeaponAttribute(int id, String name, AttributeMeta... metas)
    {
        this.id = id;
        this.name = ChatColor.RED + name;
        this.attributeMetas = metas;
    }

    public static List<EnumWeaponAttribute> random(int maxAttributes)
    {
        //test
        AtomicReference<List<EnumWeaponAttribute>> attributeList;
        if (loaded)
        {
            attributeList = new AtomicReference<List<EnumWeaponAttribute>>(); // Create a new list per process
            attributeList.set(Lists.newArrayList());
            if (attributeList.get().isEmpty()) // Better be safe than sorry
            {
                IntStream.range(1, maxAttributes).parallel().forEach(max ->
                {
                    attributeList.get().add(atomicCollection.next());
                });
                return attributeList.get();
            }
        } else
        {
            EnumWeaponAttribute[] simpleAtomics = {PURE_DAMAGE, CRITICAL_HIT, MON_DMG, ICE_DAMAGE, POISON_DAMAGE, ACCURACY};
            for (EnumWeaponAttribute enumWeaponAttribute : simpleAtomics)
            {
                atomicCollection.add(0.1, enumWeaponAttribute);
            }
            // Heavy atomics
            atomicCollection.add(0.8, EMPTY);
            atomicCollection.add(0.09, PLAYER_DMG);
            atomicCollection.add(0.08, PENETRATION);
            atomicCollection.add(0.09, FIRE_DAMAGE);
            loaded = true;
            return random(maxAttributes); // Redo the process
        }
        return null;
    }

    public static boolean containsSpecificOnly(EnumWeaponAttribute par1, Collection<EnumWeaponAttribute> collection)
    {
        if (collection.contains(par1))
        {
            List<EnumWeaponAttribute> filteredList = Lists.newArrayList();
            filteredList.addAll(collection);
            // Filter the attributeList, so it only contains the attributes without the specified one
            filteredList.remove(par1);
            for (EnumWeaponAttribute attribute : filteredList)
            {
                if (collection.contains(attribute)) // Does the collection contain any attributes rather than the specified one?
                    return false;
                else return true;
            }
        } else
        {
            return false;
        }
        return false;
    }
}
