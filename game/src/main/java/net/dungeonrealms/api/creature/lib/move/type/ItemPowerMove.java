package net.dungeonrealms.api.creature.lib.move.type;

import lombok.Getter;
import net.dungeonrealms.api.creature.lib.move.EnumPowerMove;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Giovanni on 1-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public abstract class ItemPowerMove extends CreaturePowerMove {

    @Getter
    private ItemStack[] itemStacks;

    @Getter
    private int chance;

    public ItemPowerMove(EnumPowerMove powerMove, ItemStack[] itemStacks) {
        super(powerMove);
        this.itemStacks = itemStacks;

        if (powerMove != EnumPowerMove.EMPTY) {
            for (ItemStack itemStack : itemStacks) {
                net.minecraft.server.v1_9_R2.ItemStack itemStack1 = CraftItemStack.asNMSCopy(itemStack);
                if (itemStack1.hasTag() || itemStack1.getTag() != null) {
                    if (itemStack1.getTag().hasKey(powerMove.getIdentifier())) {
                        this.chance += itemStack1.getTag().getInt(powerMove.getIdentifier());
                    }
                }
            } // Uhh?
        }
    }
}
