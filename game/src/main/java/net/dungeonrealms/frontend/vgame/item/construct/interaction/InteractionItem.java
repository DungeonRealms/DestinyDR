package net.dungeonrealms.frontend.vgame.item.construct.interaction;

import lombok.Getter;
import net.dungeonrealms.frontend.vgame.item.EnumGameItem;
import net.dungeonrealms.frontend.vgame.item.construct.interaction.action.EnumInteractionAction;
import net.dungeonrealms.frontend.vgame.item.construct.interaction.action.IAction;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public abstract class InteractionItem {

    /**
     * NBT Format default
     * gameItem true
     * itemType
     * interactionAction
     * atomicId
     *
     * NBT Format teleport book
     * inherit +
     * teleportLocation
     */

    @Getter
    private EnumGameItem gameItem;

    @Getter
    private ItemStack itemStack;

    @Getter
    private EnumInteractionAction interactionAction;

    public InteractionItem(EnumGameItem gameItem, EnumInteractionAction action) {
        this.gameItem = gameItem;
        this.interactionAction = action;
    }

    public InteractionItem(ItemStack itemStack) {
        NBTTagCompound tagCompound = CraftItemStack.asNMSCopy(itemStack).getTag();
        if (tagCompound != null && !tagCompound.isEmpty()) {
            if (tagCompound.hasKey("interactionItem")) { // Is it an interaction item?
                this.itemStack = itemStack;
                this.gameItem = EnumGameItem.valueOf(tagCompound.getString("gameItem"));
                if (tagCompound.hasKey("interactionAction")) { // Check which type of interaction this item has
                    this.interactionAction = EnumInteractionAction.valueOf(tagCompound.getString("interactionAction"));
                }
            }
        }
    }

    public void performAction(IAction action) {
        action.start();
    }

    public abstract void onUse();
}
