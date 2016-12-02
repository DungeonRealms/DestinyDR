package net.dungeonrealms.vgame.item.interactable.book;

import lombok.Getter;
import net.dungeonrealms.vgame.item.EnumGameItem;
import net.dungeonrealms.vgame.item.construct.Item;
import net.dungeonrealms.vgame.item.construct.interaction.InteractionItem;
import net.dungeonrealms.vgame.item.construct.interaction.action.EnumInteractionAction;
import net.dungeonrealms.vgame.item.security.UAI;
import net.dungeonrealms.vgame.world.location.EnumLocation;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ItemTeleportBook extends InteractionItem implements Item {

    @Getter
    private UAI atomicId; // Does not use UAI

    @Getter
    private ItemStack itemStack;

    public ItemTeleportBook() {
        super(EnumGameItem.TELEPORTATION_BOOK, EnumInteractionAction.TELEPORTATION);

        EnumLocation location = EnumLocation.getById(new Random().nextInt(EnumLocation.values().length));

        this.itemStack = new ItemStack(Material.BOOK);
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.BOLD.toString() + ChatColor.WHITE + "Teleportation: " + ChatColor.WHITE + location.getName());
        itemMeta.setLore(Arrays.asList(ChatColor.GRAY + "Teleports to " + location.getName(), ChatColor.GRAY + "Right-Click to teleport"));
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setBoolean("interactionItem", true);
        tagCompound.setString("gameItem", this.getGameItem().name());
        tagCompound.setString("interactionAction", this.getInteractionAction().name());
        tagCompound.setString("teleportLocation", location.name());
        this.itemStack.setItemMeta(itemMeta);
        CraftItemStack.asNMSCopy(this.itemStack).setTag(tagCompound);
    }

    @Override
    public void onUse() {
        // Empty
    }
}
