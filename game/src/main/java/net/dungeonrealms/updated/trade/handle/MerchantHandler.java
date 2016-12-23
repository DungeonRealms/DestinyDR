package net.dungeonrealms.updated.trade.handle;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.updated.Handler;
import net.dungeonrealms.updated.trade.gui.action.ClickAction;
import net.dungeonrealms.updated.trade.gui.action.EnumClickAction;
import net.dungeonrealms.updated.trade.gui.action.EnumClicker;
import net.dungeonrealms.updated.trade.merchant.MerchantGUI;
import net.dungeonrealms.updated.trade.merchant.MerchantTrade;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class MerchantHandler implements Handler, Listener {

    @Getter
    private HashMap<UUID, MerchantGUI> currentTrades;

    @Getter
    private HashMap<UUID, List<ItemStack>> securedItems;

    @Override
    public void prepare() {
        DungeonRealms.getInstance().getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        this.currentTrades = Maps.newHashMap();
        this.securedItems = Maps.newHashMap();
    }

    /**
     * Stop all trades
     */
    public void timeOut() {
        for (MerchantGUI tradeGUI : this.currentTrades.values()) {
            tradeGUI.getCorrespondingTrade().exit();
        }
    }

    @EventHandler
    public void onItemAdd(InventoryClickEvent event) {
        // Player clicks in a trade gui?
        if (event.getInventory().getName() != null && event.getInventory().getName().equals("Merchant")) {
            Player player = (Player) event.getWhoClicked();
            // Check if the player clicks in the GUI
            if (this.currentTrades.get(player.getUniqueId()) != null) {
                MerchantGUI merchantGUI = this.currentTrades.get(player.getUniqueId());
                // If the player has an item on his cursor, he is adding an item to the trade
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    merchantGUI.handleClick(new ClickAction(EnumClicker.OWNER, EnumClickAction.ADD, event.getCursor(), event.getSlot()), event);
                } else {
                    // If not, he is removing an item from the trade
                    merchantGUI.handleClick(new ClickAction(EnumClicker.OWNER, EnumClickAction.REMOVE, null, event.getSlot()), event);
                }
            }
        }
    }

    @EventHandler
    public void onButtonClick(InventoryClickEvent event) {
        // Player clicks in a trade gui?
        if (event.getInventory().getName() != null && event.getInventory().getName().equals("Merchant")) {
            Player player = (Player) event.getWhoClicked();
            // Check if the player clicked on an accept button
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                if (CraftItemStack.asNMSCopy(event.getCurrentItem()).hasTag()) {
                    if (CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("tradeActionButton")) {
                        // Check if the owner clicks in the GUI
                        if (this.currentTrades.get(player.getUniqueId()) != null) {
                            MerchantGUI merchantGUI = this.currentTrades.get(player.getUniqueId());
                            // Player accepted the offer
                            merchantGUI.handleButtonClick();
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTrade(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getName() != null && event.getRightClicked().getName().equals("Merchant")) {
            MerchantTrade merchantTrade = new MerchantTrade(event.getPlayer().getUniqueId());
            MerchantGUI merchantGUI = new MerchantGUI(merchantTrade);
            this.currentTrades.put(event.getPlayer().getUniqueId(), merchantGUI);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (this.currentTrades.containsKey(event.getEntity().getUniqueId())) {
            // Secure items
            this.securedItems.put(event.getEntity().getUniqueId(), this.currentTrades.get(event.getEntity().getUniqueId()).getCorrespondingTrade().getPlayerItems());
            this.currentTrades.get(event.getEntity().getUniqueId()).getCorrespondingTrade().timeOut();
            this.currentTrades.remove(event.getEntity().getUniqueId());
        }
    }

    @EventHandler
    public void onStop(InventoryCloseEvent event) {
        if (event.getInventory().getName().equals("Merchant")) {
            Player player = (Player) event.getPlayer();
            if (this.currentTrades.containsKey(player.getUniqueId())) {
                this.currentTrades.get(player.getUniqueId()).getCorrespondingTrade().exit();
                this.currentTrades.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (this.currentTrades.containsKey(event.getPlayer().getUniqueId())) {
            this.currentTrades.get(event.getPlayer().getUniqueId()).getCorrespondingTrade().exit();
            this.currentTrades.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (this.securedItems.containsKey(event.getPlayer().getUniqueId())) {
            // Prevent item loss
            for (ItemStack itemStack : this.securedItems.get(event.getPlayer().getUniqueId())) {
                event.getPlayer().getInventory().addItem(itemStack);
                event.getPlayer().updateInventory();
            }
        }
    }
}
