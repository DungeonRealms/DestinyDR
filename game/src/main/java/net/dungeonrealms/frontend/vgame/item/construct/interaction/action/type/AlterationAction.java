package net.dungeonrealms.frontend.vgame.item.construct.interaction.action.type;

import lombok.Getter;
import net.dungeonrealms.frontend.vgame.item.EnumGameItem;
import net.dungeonrealms.frontend.vgame.item.construct.ItemConstruction;
import net.dungeonrealms.frontend.vgame.item.construct.interaction.InteractionItem;
import net.dungeonrealms.frontend.vgame.item.construct.interaction.action.EnumInteractionAction;
import net.dungeonrealms.frontend.vgame.item.construct.interaction.action.IAction;
import net.dungeonrealms.frontend.vgame.item.gear.weapon.ItemWeapon;
import net.dungeonrealms.frontend.vgame.item.security.exception.CompoundException;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class AlterationAction implements IAction {

    @Getter
    private EnumInteractionAction action = EnumInteractionAction.ALTERATION;

    @Getter
    private InteractionItem interactionItem;

    @Getter
    private Player activityPlayer;

    @Getter
    private ItemStack targetItem;

    public AlterationAction(InteractionItem interactionItem, Player player, ItemStack targetItem) {
        this.interactionItem = interactionItem;
        this.activityPlayer = player;
        this.targetItem = targetItem;
    }

    @Override
    public void start() {
        net.minecraft.server.v1_9_R2.ItemStack minecraftItem = CraftItemStack.asNMSCopy(this.targetItem);
        if (minecraftItem.hasTag() || minecraftItem.getTag() != null) {
            if (minecraftItem.getTag().hasKey("gameItem")) {
                // Does it have an atomic id?
                if (minecraftItem.getTag().hasKey("atomicId")) {
                    // Is it a weapon?
                    if (minecraftItem.getTag().getString("itemType").equalsIgnoreCase(EnumGameItem.WEAPON.name())) {
                        try {
                            int chance = new Random().nextInt(100);
                            ItemWeapon itemWeapon = new ItemWeapon(CraftItemStack.asBukkitCopy(minecraftItem));
                            if (chance >= 40) {
                                // Finished
                                this.activityPlayer.getInventory().removeItem(this.targetItem);
                                this.activityPlayer.getInventory().addItem(itemWeapon.update());
                                this.activityPlayer.updateInventory();
                                this.activityPlayer.playSound(this.activityPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.4f);
                            } else {
                                this.activityPlayer.playSound(this.activityPlayer.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 0.4f);
                                if (itemWeapon.getAttributeList().size() > 0) {
                                    int clearChance = new Random().nextInt(100);
                                    if (clearChance >= 51) {
                                        // TODO Remove an attribute from the weapon
                                    } else {

                                    }
                                }
                            }
                        } catch (CompoundException e) {
                            e.printStackTrace();
                        }
                    }
                    // Is it an armor piece?
                    if (minecraftItem.getTag().getString("itemType").equalsIgnoreCase(EnumGameItem.ARMOR.name())) {

                    }
                }
            }
        } // Ignore compound exception
    }
}
