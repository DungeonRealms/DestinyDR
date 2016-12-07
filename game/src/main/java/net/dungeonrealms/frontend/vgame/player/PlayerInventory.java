package net.dungeonrealms.frontend.vgame.player;

import lombok.Getter;
import net.dungeonrealms.common.awt.base64.Base64Array;
import net.dungeonrealms.frontend.Game;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Giovanni on 7-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PlayerInventory {

    @Getter
    private UUID owner;

    @Getter
    private ItemStack[] contents;

    @Getter
    private ItemStack[] armorContents;

    @Getter
    private String base64Contents;

    @Getter
    private String base64ArmorContents;

    // New player
    public PlayerInventory(UUID owner) {
        this.owner = owner;
    }

    // Existent player
    public PlayerInventory(UUID owner, String defaultContents, String armorContents) {
        this.owner = owner;
        this.base64Contents = defaultContents;
        this.base64ArmorContents = armorContents;
        this.itemStack(); // Convert the inventory data
    }

    /**
     * Update the player's inventory - Called whenever the actual bukkit inventory changes
     */
    public void update() {
        GamePlayer gamePlayer = Game.getGame().getRegistryRegistry().getPlayerRegistry().getPlayer(this.owner);
        this.contents = gamePlayer.getPlayer().getInventory().getContents();
        this.armorContents = gamePlayer.getPlayer().getInventory().getArmorContents();
        this.base64(); // Convert the inventory data
    }

    /**
     * Convert the player's contents to a String
     */
    private void base64() {
        this.base64Contents = String.valueOf(new Base64Array(this.contents).stream().value());
        this.base64ArmorContents = String.valueOf(new Base64Array(this.armorContents).stream().value());
    }

    /**
     * Convert the player's contents as String to an ItemStack
     */
    private void itemStack() {
        this.contents = new Base64Array(this.base64Contents).itemStackArray();
        this.armorContents = new Base64Array(this.base64ArmorContents).itemStackArray();
    }
}
