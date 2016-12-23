package net.dungeonrealms.updated.trade.handle;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.updated.Handler;
import net.dungeonrealms.updated.trade.ItemTrade;
import net.dungeonrealms.updated.trade.gui.TradeGUI;
import net.dungeonrealms.updated.trade.gui.action.ClickAction;
import net.dungeonrealms.updated.trade.gui.action.EnumClickAction;
import net.dungeonrealms.updated.trade.gui.action.EnumClicker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class TradeHandler implements Handler, Listener {

    // <Owner, TradeHolder>
    @Getter
    private HashMap<UUID, TradeGUI> currentTrades;

    @Override
    public void prepare() {
        DungeonRealms.getInstance().getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        this.currentTrades = Maps.newHashMap();
    }

    /**
     * Check all trades' status
     */
    private void checkTrades() {
        DungeonRealms.getInstance().getServer().getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), ()
                -> this.currentTrades.values().stream().filter(tradeGUI -> tradeGUI.getCorrespondingTrade().isParticipatorAccepted()
                && tradeGUI.getCorrespondingTrade().isOwnerAccepted()).forEach(tradeGUI -> tradeGUI.getCorrespondingTrade().finish()), 0L, 10);
    }

    /**
     * Stop all trades
     */
    public void timeOut() {
        for (TradeGUI tradeGUI : this.currentTrades.values()) {
            tradeGUI.getCorrespondingTrade().exit();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        // Owner of a trade leaves the server
        if (this.currentTrades.containsKey(event.getPlayer().getUniqueId())) {
            // Check
            ItemTrade itemTrade = this.currentTrades.get(event.getPlayer().getUniqueId()).getCorrespondingTrade();
            if (Bukkit.getPlayer(itemTrade.getParticipator()) != null && Bukkit.getPlayer(itemTrade.getParticipator()).isOnline()) {
                Player player = Bukkit.getPlayer(itemTrade.getParticipator());
                player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "TRADE CANCELLED");
            }
            // Stop the trade & give back items
            itemTrade.exit();
        }
        // Participator of a trade leaves the server
        this.currentTrades.values().stream().filter(tradeGUI -> tradeGUI.getCorrespondingTrade().getParticipator().equals(event.getPlayer().getUniqueId())).forEach(tradeGUI -> {
            ItemTrade itemTrade = tradeGUI.getCorrespondingTrade();
            if (Bukkit.getPlayer(itemTrade.getOwner()) != null && Bukkit.getPlayer(itemTrade.getOwner()).isOnline()) {
                Player player = Bukkit.getPlayer(itemTrade.getOwner());
                player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "TRADE CANCELLED");
            }
            // Stop the trade & give back items
            itemTrade.exit();
        });
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // Player clicks in a trade gui?
        if (event.getInventory().getName() != null && event.getInventory().getName().equals("Trading")) {
            Player player = (Player) event.getWhoClicked();
            // Check if the owner clicks in the GUI
            if (this.currentTrades.get(player.getUniqueId()) != null
                    && this.currentTrades.get(player.getUniqueId()).getCorrespondingTrade().getOwner().equals(player.getUniqueId())) {
                TradeGUI tradeGUI = this.currentTrades.get(player.getUniqueId());
                // If the player has an item on his cursor, he is adding an item to the trade
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    tradeGUI.handleClick(new ClickAction(EnumClicker.OWNER, EnumClickAction.ADD, event.getCursor(), event.getSlot()), event);
                } else {
                    // If not, he is removing an item from the trade
                    tradeGUI.handleClick(new ClickAction(EnumClicker.OWNER, EnumClickAction.REMOVE, null, event.getSlot()), event);
                }
            }
            // Check if the participator clicks in the GUI
            this.currentTrades.values().stream().filter(tradeGUI -> tradeGUI.getCorrespondingTrade().getParticipator().equals(player.getUniqueId())).forEach(tradeGUI -> {
                // If the player has an item on his cursor, he is adding an item to the trade
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    tradeGUI.handleClick(new ClickAction(EnumClicker.PARTICIPATOR, EnumClickAction.ADD, event.getCursor(), event.getSlot()), event);
                } else {
                    // If not, he is removing an item from the trade
                    tradeGUI.handleClick(new ClickAction(EnumClicker.PARTICIPATOR, EnumClickAction.REMOVE, null, event.getSlot()), event);
                }
            });
        }
    }

    @EventHandler
    public void onFinish(InventoryClickEvent event) {
        // Player clicks in a trade gui?
        if (event.getInventory().getName() != null && event.getInventory().getName().equals("Trading")) {
            Player player = (Player) event.getWhoClicked();
            // Check if the player clicked on an accept button
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                if (CraftItemStack.asNMSCopy(event.getCurrentItem()).hasTag()) {
                    if (CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("tradeActionButton")) {
                        // Check if the owner clicks in the GUI
                        if (this.currentTrades.get(player.getUniqueId()) != null
                                && this.currentTrades.get(player.getUniqueId()).getCorrespondingTrade().getOwner().equals(player.getUniqueId())) {
                            TradeGUI tradeGUI = this.currentTrades.get(player.getUniqueId());
                            // Check if the owner already accepted the trade
                            if (tradeGUI.getCorrespondingTrade().isOwnerAccepted()) {
                                // Set status back to false, the owner clicked the button again
                                tradeGUI.updateStatus(EnumClicker.OWNER, event.getInventory(), false);
                            } else {
                                // Owner clicks the button to accept the trade
                                tradeGUI.updateStatus(EnumClicker.OWNER, event.getInventory(), true);
                            }
                        }
                        // Check if the participator clicks in the GUI
                        this.currentTrades.values().stream().filter(tradeGUI -> tradeGUI.getCorrespondingTrade().getParticipator().equals(player.getUniqueId())).forEach(tradeGUI -> {
                            // Set status back to false, the participator clicked the button again
                            if (tradeGUI.getCorrespondingTrade().isParticipatorAccepted()) {
                                tradeGUI.updateStatus(EnumClicker.PARTICIPATOR, event.getInventory(), false);
                            } else {
                                // Participator clicks the button to accept the trade
                                tradeGUI.updateStatus(EnumClicker.PARTICIPATOR, event.getInventory(), true);
                            }
                        });
                    }
                }
            }
        }
    }
}
