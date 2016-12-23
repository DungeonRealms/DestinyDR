package net.dungeonrealms.updated.trade.merchant;


import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class MerchantTrade {

    @Getter
    private UUID player;

    @Getter
    private List<ItemStack> playerItems;

    @Getter
    private List<ItemStack> resultItems;

    public MerchantTrade(UUID player) {
        this.player = player;
        this.playerItems = Lists.newArrayList();
        this.resultItems = Lists.newArrayList();
    }

    /**
     * Finish the merchant trade
     */
    public void finish() {
        for(ItemStack itemStack : this.playerItems) {
            Bukkit.getPlayer(this.player).getInventory().remove(itemStack);
        }
        for(ItemStack itemStack : this.resultItems) {
            Bukkit.getPlayer(this.player).getInventory().addItem(itemStack);
        }
        Bukkit.getPlayer(this.player).updateInventory();
        Bukkit.getPlayer(this.player).sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "TRADE ACCEPTED");
    }

    /**
     * Stop the trade
     */
    public void timeOut() {
        this.resultItems.clear();
        this.playerItems.clear();
        Bukkit.getPlayer(this.player).sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "TRADE CANCELLED");
    }

    /**
     * Exit the merchant trade
     */
    public void exit() {
        this.resultItems.clear();
        for(ItemStack itemStack : this.playerItems) {
            Bukkit.getPlayer(this.player).getInventory().addItem(itemStack);
        }
        Bukkit.getPlayer(this.player).updateInventory();
        Bukkit.getPlayer(this.player).updateInventory();
        Bukkit.getPlayer(this.player).sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "TRADE CANCELLED");
    }
}
