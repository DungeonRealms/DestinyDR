package net.dungeonrealms.updated.trade;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.game.mechanic.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ItemTrade {

    @Getter
    private UUID owner;

    @Getter
    private UUID participator;

    @Getter
    private List<ItemStack> ownerItems;

    @Getter
    private List<ItemStack> participatorItems;

    @Getter
    @Setter
    private boolean participatorAccepted = false;

    @Getter
    @Setter
    private boolean ownerAccepted = false;

    public ItemTrade(UUID owner, UUID participator) {
        this.owner = owner;
        this.participator = participator;
        this.ownerItems = Lists.newArrayList();
        this.participatorItems = Lists.newArrayList();
    }

    /**
     * Finish the trade
     */
    public void finish() {
        Player owner = Bukkit.getPlayer(this.owner);
        Player participator = Bukkit.getPlayer(this.participator);

        // Check if they have available slots
        if (!ItemManager.inventoryFull(owner) && !ItemManager.inventoryFull(participator)) {
            // Give the items
            for (ItemStack itemStack : this.participatorItems) {
                owner.getInventory().addItem(itemStack);
                owner.updateInventory();
            }
            for (ItemStack itemStack : this.ownerItems) {
                participator.getInventory().addItem(itemStack);
                owner.updateInventory();
            }
        }
        owner.closeInventory();
        participator.closeInventory();

        owner.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "TRADE ACCEPTED");
        participator.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "TRADE ACCEPTED");
    }

    /**
     * Stop the trade & give the items back to their corresponding owner
     */
    public void exit() {
        Player owner = Bukkit.getPlayer(this.owner);
        Player participator = Bukkit.getPlayer(this.participator);
        for (ItemStack itemStack : this.ownerItems) {
            owner.getInventory().addItem(itemStack);
        }
        for (ItemStack itemStack : this.participatorItems) {
            participator.getInventory().addItem(itemStack);
        }
        owner.closeInventory();
        participator.closeInventory();
    }
}
