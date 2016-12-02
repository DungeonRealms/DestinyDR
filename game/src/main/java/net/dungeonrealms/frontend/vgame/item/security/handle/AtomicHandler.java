package net.dungeonrealms.frontend.vgame.item.security.handle;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.awt.frame.handler.Handler;
import net.dungeonrealms.frontend.vgame.item.security.NUAIHolder;
import net.dungeonrealms.frontend.vgame.item.security.result.EnumCheckResult;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Giovanni on 17-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class AtomicHandler implements Handler {

    @Getter
    private boolean prepared;

    @Getter
    private UUID uniqueId;

    @Override
    public void prepare() {
        // Check if a player has duped an item
        DungeonRealms.getInstance().getServer().getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () ->
        {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (ItemStack itemStack : player.getInventory().getContents()) {
                    if (itemStack != null) {
                        for (ItemStack itemStack1 : player.getInventory().getContents()) {
                            if (itemStack1 != null) {
                                // Does the player have 2 of the exact same items?
                                if (itemStack.equals(itemStack1)) continue; // Suspicious
                                this.checkAtomics(itemStack, itemStack1);
                            }
                        }
                    }
                }
            }
        }, 0L, 5);

        // Second handle
        DungeonRealms.getInstance().getServer().getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () ->
        {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (ItemStack itemStack : player.getInventory().getContents()) {
                    if (itemStack != null) {
                        if (CraftItemStack.asNMSCopy(itemStack).getTag() != null && (CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("duplicated"))) {
                            player.getInventory().removeItem(itemStack);
                            player.updateInventory();
                        }
                    }
                }
            }
        }, 0L, 20 * 5);

        this.uniqueId = UUID.randomUUID();
        this.prepared = true;
    }

    @Override
    public void disable() {
        this.uniqueId = null;
        this.prepared = false;
    }

    protected EnumCheckResult checkAtomics(ItemStack itemStack, ItemStack itemStack1) {
        try {
            if ((CraftItemStack.asNMSCopy(itemStack).getTag() != null || CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("atomic"))) {
                NBTTagCompound tagCompound = CraftItemStack.asNMSCopy(itemStack).getTag();
                if ((tagCompound != null || tagCompound.hasKey("atomic"))) {
                    NBTTagCompound tagCompound1 = CraftItemStack.asNMSCopy(itemStack1).getTag();
                    if (tagCompound.getString("atomic").equals(tagCompound1.getString("atomic"))) {
                        tagCompound1.set("duplicated", new NBTTagString("you_are_a_twat")); // Second handle
                        NUAIHolder.getHolder().getAtomicList().get().add(itemStack1);
                        this.removeCompound(itemStack1);
                        return EnumCheckResult.TRUE;
                    } else {
                        return EnumCheckResult.FALSE; // Wait how?
                    }
                }
            }
        } catch (Exception e) {
            // Well, this shouldn't happen
        }
        return EnumCheckResult.TRUE;
    }

    private void removeCompound(ItemStack itemStack) {
        Bukkit.getOnlinePlayers().stream().filter(player ->
        {
            // Well damn, how did you get that, mate?
            return player.getInventory().contains(itemStack);
        }).forEach(player ->
        {
            for (ItemStack itemStack1 : player.getInventory().getContents()) // Time 2 scan
            {
                if (itemStack1 == itemStack) // Is it an instance of a duped item?
                {
                    player.getInventory().removeItem(itemStack1);
                    player.updateInventory();
                    if (NUAIHolder.getHolder().getAtomicList().get().contains(itemStack1)) {
                        NUAIHolder.getHolder().getAtomicList().get().remove(itemStack1);
                    }
                }
            }
        });
    }
}
