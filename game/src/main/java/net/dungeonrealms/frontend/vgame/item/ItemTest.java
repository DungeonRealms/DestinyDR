package net.dungeonrealms.frontend.vgame.item;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.handler.Handler;
import net.dungeonrealms.frontend.vgame.item.construct.ItemConstruction;
import net.dungeonrealms.frontend.vgame.item.construct.gear.EnumGearType;
import net.dungeonrealms.frontend.vgame.item.construct.generic.EnumItemRarity;
import net.dungeonrealms.frontend.vgame.item.construct.generic.EnumItemTier;
import net.dungeonrealms.frontend.vgame.item.gear.weapon.ItemWeapon;
import net.dungeonrealms.frontend.vgame.item.security.exception.CompoundException;
import net.minecraft.server.v1_9_R2.ItemStack;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

/**
 * Created by Giovanni on 4-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ItemTest implements Handler.ListeningHandler {

    /**
     * Item Testing
     * <p>
     * Ignore
     */

    @Getter
    private boolean prepared;

    @Getter
    private UUID uniqueId;

    @Override
    public void prepare() {
        this.uniqueId = UUID.randomUUID();
        this.prepared = true;
    }

    @Override
    public void disable() {
        // Do nothing
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory().addItem(new ItemWeapon(new ItemConstruction(EnumGameItem.WEAPON, EnumItemRarity.random(), true, true)).getItemStack());
        event.getPlayer().getInventory().addItem(new ItemWeapon(new ItemConstruction(EnumGameItem.WEAPON, EnumItemTier.random(), true, true)).getItemStack());
        event.getPlayer().getInventory().addItem(new ItemWeapon(new ItemConstruction(EnumGameItem.WEAPON, "Test Object", true, true)).getItemStack());
        event.getPlayer().getInventory().addItem(new ItemWeapon(new ItemConstruction(EnumGearType.POLE_ARM, true, true)).getItemStack());
        event.getPlayer().getInventory().addItem(new ItemWeapon(new ItemConstruction(EnumGameItem.WEAPON, EnumItemRarity.random(), EnumItemTier.random(), true, true)).getItemStack());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(event.getItem());
        if (itemStack.getTag().getString("itemType").equalsIgnoreCase(EnumGameItem.WEAPON.name())) {
            try {
                ItemWeapon itemWeapon = new ItemWeapon(CraftItemStack.asBukkitCopy(itemStack));
            } catch (CompoundException e) {
                e.printStackTrace();
            }
        }
    }
}
