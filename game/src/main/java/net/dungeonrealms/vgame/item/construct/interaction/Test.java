package net.dungeonrealms.vgame.item.construct.interaction;

import net.dungeonrealms.vgame.item.construct.interaction.InteractionItem;
import net.dungeonrealms.vgame.item.construct.interaction.action.type.AlterationAction;
import net.dungeonrealms.vgame.item.construct.interaction.action.type.TeleportationAction;
import net.dungeonrealms.vgame.item.interactable.book.ItemTeleportBook;
import net.dungeonrealms.vgame.item.interactable.orb.ItemOrb;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Test {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            Player player = event.getPlayer();
            if (player.getItemInHand() != null && player.getItemInHand().hasItemMeta()) {
                if (player.getItemInHand().getType() != null && player.getItemInHand().getItemMeta().hasDisplayName()) {
                    if (player.getItemInHand().getType() == Material.FIREBALL) {
                        if (CraftItemStack.asNMSCopy(player.getItemInHand()).getTag() != null) {
                            NBTTagCompound nbtTagCompound = CraftItemStack.asNMSCopy(player.getItemInHand()).getTag();
                            if (!nbtTagCompound.isEmpty()) {
                                if (nbtTagCompound.hasKey("interactionItem")) {
                                    InteractionItem interactionItem = new InteractionItem(player.getItemInHand()) {
                                        @Override
                                        public void onUse() {
                                            switch (this.getInteractionAction()) {
                                                case TELEPORTATION:
                                                    this.performAction(new TeleportationAction(this, event.getPlayer()));
                                                    break;
                                                case ALTERATION:
                                                    this.performAction(new AlterationAction(this, event.getPlayer(), null));
                                                    break;
                                            }
                                        }
                                    };
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        event.getPlayer().getInventory().addItem(new ItemTeleportBook().getItemStack());
        event.getPlayer().getInventory().addItem(new ItemOrb().getItemStack());
    }
}
