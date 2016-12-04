package net.dungeonrealms.frontend.vgame.item.construct.interaction.action.type;

import lombok.Getter;
import net.dungeonrealms.frontend.Game;
import net.dungeonrealms.frontend.vgame.item.construct.interaction.InteractionItem;
import net.dungeonrealms.frontend.vgame.item.construct.interaction.action.EnumInteractionAction;
import net.dungeonrealms.frontend.vgame.item.construct.interaction.action.IAction;
import net.dungeonrealms.frontend.vgame.player.GamePlayer;
import net.dungeonrealms.frontend.vgame.world.location.EnumLocation;
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
        ItemStack itemStack = this.interactionItem.getItemStack();
        GamePlayer gamePlayer = Game.getGame().getRegistryRegistry().getPlayerRegistry().getPlayer(this.activityPlayer.getUniqueId());

        gamePlayer.setTeleporting(true);

        this.activityPlayer.sendMessage(ChatColor.WHITE.toString() + ChatColor.BOLD + "CASTING" + " - " + ChatColor.RESET + "Teleport: " + ChatColor.AQUA
                + EnumLocation.valueOf(CraftItemStack.asNMSCopy(itemStack).getTag().getString("teleportLocation")).getName());
        this.activityPlayer.getInventory().removeItem(itemStack);

        final int taskTime[] = {6};
        int task = Game.getGame().getServer().getScheduler().scheduleAsyncRepeatingTask(Game.getGame(), () -> {
            // Are they in a teleportation? Prevent combat issues
            if (gamePlayer.isTeleporting()) {
                // Is the time between 1 & 6, tell them
                if (taskTime[0] > 0) {
                    this.activityPlayer.sendMessage(ChatColor.WHITE.toString() + ChatColor.BOLD + "CASTING" + ChatColor.RESET + " ... " + taskTime[0] + "s");
                    taskTime[0]--;
                }

                // Is the time 0? Teleport him
                if (taskTime[0] <= 0) {
                    this.activityPlayer.teleport(EnumLocation.valueOf(CraftItemStack.asNMSCopy(itemStack).getTag().getString("teleportLocation")).getLocation());
                }
            }
        }, 0L, 20L);

        Game.getGame().getServer().getScheduler().scheduleAsyncDelayedTask(Game.getGame(), () -> {
            Game.getGame().getServer().getScheduler().cancelTask(task);
            taskTime[0] = 6;
        }, taskTime[0] * 20);
    }
}
