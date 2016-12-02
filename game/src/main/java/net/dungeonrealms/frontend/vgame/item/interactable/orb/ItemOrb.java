package net.dungeonrealms.frontend.vgame.item.interactable.orb;

import lombok.Getter;
import net.dungeonrealms.frontend.vgame.item.EnumGameItem;
import net.dungeonrealms.frontend.vgame.item.construct.Item;
import net.dungeonrealms.frontend.vgame.item.construct.interaction.InteractionItem;
import net.dungeonrealms.frontend.vgame.item.construct.interaction.action.EnumInteractionAction;
import net.dungeonrealms.frontend.vgame.item.security.UAI;
import net.dungeonrealms.frontend.vgame.item.security.exception.CompoundException;
import net.dungeonrealms.frontend.vgame.world.location.EnumLocation;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Random;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ItemOrb extends InteractionItem implements Item {

    @Getter
    private UAI atomicId; // Uses UAI

    @Getter
    private ItemStack itemStack;

    public ItemOrb() {
        super(EnumGameItem.ORB, EnumInteractionAction.ALTERATION);

        this.atomicId = new UAI();

        this.itemStack = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Orb of Alteration");
        itemMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Randomizes bonus stats of selected equipment"));
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setBoolean("interactionItem", true);
        tagCompound.setString("gameItem", this.getGameItem().name());
        tagCompound.setString("interactionAction", this.getInteractionAction().name());
        this.itemStack.setItemMeta(itemMeta);
        CraftItemStack.asNMSCopy(this.itemStack).setTag(tagCompound);

        try {
            this.atomicId.attachTo(CraftItemStack.asNMSCopy(this.itemStack));
        } catch (CompoundException e) {
            // Ignore
        }
    }

    @Override
    public void onUse() {
        // Empty
    }
}
