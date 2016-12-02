package net.dungeonrealms.vgame.item.construct.interaction.action.type;

import lombok.Getter;
import net.dungeonrealms.vgame.old.Game;
import net.dungeonrealms.vgame.item.construct.interaction.InteractionItem;
import net.dungeonrealms.vgame.item.construct.interaction.action.EnumInteractionAction;
import net.dungeonrealms.vgame.item.construct.interaction.action.IAction;
import net.dungeonrealms.vgame.player.GamePlayer;
import net.dungeonrealms.vgame.world.location.EnumLocation;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class TeleportationAction implements IAction {

    @Getter
    private EnumInteractionAction action = EnumInteractionAction.TELEPORTATION;

    @Getter
    private InteractionItem interactionItem;

    @Getter
    private Player activityPlayer;

    public TeleportationAction(InteractionItem interactionItem, Player player) {
        this.interactionItem = interactionItem;
        this.activityPlayer = player;
    }

    @Override
    public void start() {
        if (!Game.getGame().getRegistryHandler().getPlayerRegistry().getMap().get(this.activityPlayer.getUniqueId()).isTeleporting()) {
            ItemStack itemStack = this.interactionItem.getItemStack();
            GamePlayer gamePlayer = Game.getGame().getRegistryHandler().getPlayerRegistry().getMap().get(this.activityPlayer.getUniqueId());

            activityPlayer.sendMessage(ChatColor.WHITE.toString() + ChatColor.BOLD + "TELEPORTING" + " - " + ChatColor.AQUA
                    + EnumLocation.valueOf(CraftItemStack.asNMSCopy(itemStack).getTag().getString("teleportLocation")).getName());

            final int taskTime[] = {6};
            int task = Game.getGame().getServer().getScheduler().scheduleAsyncRepeatingTask(Game.getGame(), () -> {
                // Are they in a teleportation? Because they might combat log..
                if (gamePlayer.isTeleporting()) {
                    // Is the time between 1 & 6, tell them
                    if (taskTime[0] > 0) {
                        activityPlayer.sendMessage(ChatColor.WHITE.toString() + ChatColor.BOLD + "TELEPORTING " + ChatColor.RESET + "... " + taskTime + "s");
                        taskTime[0]--;
                    }

                    // Is the time 0? Teleport him
                    if (taskTime[0] <= 0) {
                        activityPlayer.teleport(EnumLocation.valueOf(CraftItemStack.asNMSCopy(itemStack).getTag().getString("teleportLocation")).getLocation());
                    }
                }
            }, 0L, 100);

            Game.getGame().getServer().getScheduler().scheduleAsyncDelayedTask(Game.getGame(), () -> {
                Game.getGame().getServer().getScheduler().cancelTask(task);
                taskTime[0] = 6;
            }, (taskTime[0] * 20)  + 10);
        }
    }
}
