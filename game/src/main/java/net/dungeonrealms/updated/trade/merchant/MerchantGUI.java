package net.dungeonrealms.updated.trade.merchant;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.dungeonrealms.game.miscellaneous.TradeCalculator;
import net.dungeonrealms.updated.trade.gui.action.ClickAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class MerchantGUI implements IMerchantGUI {

    @Getter
    private MerchantTrade correspondingTrade;

    @Getter
    private Map<Integer, ItemStack> itemsInScreen;

    private Inventory inventory;

    public MerchantGUI(MerchantTrade merchantTrade) {
        this.correspondingTrade = merchantTrade;
        this.itemsInScreen = Maps.newHashMap();
        this.inventory = Bukkit.createInventory(null, 36, "Merchant");

        int middleSlots[] = {4, 13, 22, 31};
        // Create barriers in middle
        ItemStack itemStack = new ItemStack(Material.IRON_FENCE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("");
        itemStack.setItemMeta(itemMeta);
        // Set items in inventory
        for (int i : middleSlots) {
            this.inventory.setItem(i, itemStack);
        }
        this.inventory.setItem(8, this.getButton(false));
    }

    @Override
    public void handleButtonClick() {
        this.correspondingTrade.finish();
    }

    @Override
    public void handleClick(ClickAction clickAction, InventoryClickEvent event) {
        // Clicked slot is 36, or under 36
        event.setCancelled(true);
        if (clickAction.getSlot() <= 36) {
            Player player = (Player) event.getWhoClicked();
            // Player is clicking a disallowed slot
            if (!this.disallowedSlots().contains(clickAction.getSlot())) {
                // Check if the clicker can actually click on it's side
                if (this.allowedSlots(clickAction.getClicker()).contains(clickAction.getSlot())) {
                    switch (clickAction.getClickAction()) {
                        case ADD:
                            if (clickAction.getItemStack() != null) {
                                // Is the slot already occupied?
                                if (!this.itemsInScreen.containsKey(clickAction.getSlot())) {
                                    this.itemsInScreen.put(clickAction.getSlot(), clickAction.getItemStack());
                                    // Register item
                                    switch (clickAction.getClicker()) {
                                        case PARTICIPATOR:
                                            // Remove & set item
                                            Bukkit.getPlayer(this.correspondingTrade.getPlayer()).getInventory().removeItem(clickAction.getItemStack());
                                            this.correspondingTrade.getPlayerItems().add(clickAction.getItemStack());
                                            this.inventory.setItem(clickAction.getSlot(), clickAction.getItemStack());
                                            // Calculate
                                            List<ItemStack> playerOffers = Lists.newArrayList();
                                            playerOffers.addAll(this.itemsInScreen.values());
                                            // Get the offers
                                            List<ItemStack> merchantOffer = TradeCalculator.calculateMerchantOffer(playerOffers);
                                            this.correspondingTrade.getResultItems().clear();
                                            this.correspondingTrade.getResultItems().addAll(merchantOffer);
                                            break;
                                    }
                                } else {
                                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                                }
                            }
                            break;
                        case REMOVE:
                            // Verify existence
                            if (this.itemsInScreen.containsKey(clickAction.getSlot()) && this.itemsInScreen.get(clickAction.getSlot()) != null) {
                                switch (clickAction.getClicker()) {
                                    case PARTICIPATOR:
                                        // Add back
                                        Bukkit.getPlayer(this.correspondingTrade.getPlayer()).getInventory().addItem(this.itemsInScreen.get(clickAction.getSlot()));
                                        // Remove after giving back
                                        this.itemsInScreen.remove(clickAction.getSlot());
                                        this.correspondingTrade.getPlayerItems().remove(clickAction.getItemStack());
                                        this.inventory.remove(clickAction.getSlot());
                                        // Calculate
                                        List<ItemStack> playerOffers = Lists.newArrayList();
                                        playerOffers.addAll(this.itemsInScreen.values());
                                        // Get the offers
                                        List<ItemStack> merchantOffer = TradeCalculator.calculateMerchantOffer(playerOffers);
                                        this.correspondingTrade.getResultItems().clear();
                                        this.correspondingTrade.getResultItems().addAll(merchantOffer);
                                        break;
                                }
                            }
                            break;
                    }
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                }
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            }
        }
    }
}
