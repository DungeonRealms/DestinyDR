package net.dungeonrealms.game.world.entity.type.monster.boss;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumDungeonBoss;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Created by Chase on Oct 18, 2015
 */

public interface DungeonBoss extends Boss {

    EnumDungeonBoss getEnumBoss();

    default void say(Entity ent, String msg) {
        for (Player p : GameAPI.getNearbyPlayers(ent.getLocation(), 50)) {
            p.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD + getEnumBoss().name() + ChatColor.WHITE + "] "
                    + ChatColor.GREEN + msg);
        }
    }

    default void say(Entity ent, Location location, String msg) {
        for (Player p : GameAPI.getNearbyPlayers(location, 50)) {
            p.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD + getEnumBoss().name() + ChatColor.WHITE + "] "
                    + ChatColor.GREEN + msg);
        }
    }

    public boolean enabled = false;
    boolean debug = false;

    default void dropMount(Entity entity, DungeonManager.DungeonType dungeonType) {
        if (!enabled) return;
        EnumMounts mountDrop = null;
        Random random = ThreadLocalRandom.current();
        if (getEnumBoss() == EnumDungeonBoss.Mayel) {
            //Drop WOLF Pet, 1%

            if (random.nextInt(100) == 0 || debug)
                mountDrop = EnumMounts.WOLF;

        } else if (getEnumBoss() == EnumDungeonBoss.Burick) {
            //.5%
            if (random.nextInt(1000) < 50 || debug)
                mountDrop = EnumMounts.SLIME;
        } else if (getEnumBoss() == EnumDungeonBoss.InfernalAbyss) {

            //.4% seems fair.
            if (random.nextInt(1000) < 40 || debug)
                mountDrop = EnumMounts.SPIDER;
        }

        if (mountDrop != null) {
            ItemStack mountItem = mountDrop.getMountData().createMountItem(mountDrop);
            if (mountItem != null) {
                //GIVE IT TO SOMEONE!!!

                List<Player> partyMembers = entity.getWorld().getPlayers().stream().filter((pl) -> pl.getGameMode() == GameMode.SURVIVAL).collect(Collectors.toList());

                if (partyMembers != null && !partyMembers.isEmpty()) {
                    Collections.shuffle(partyMembers);

                    Player winner = null;
                    for (Player win : partyMembers) {
                        //Its shuffled so first player can just be the winner if they have space.
                        //kys.
                        if (win.getInventory().firstEmpty() == -1) {
                            Bukkit.getLogger().info("Not giving mount to " + win.getName() + " due to full inventory.");
                            continue;
                        }

                        winner = win;
                        break;
                    }

                    if (winner != null) {
                        winner.playSound(winner.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
                        winner.getInventory().addItem(mountItem);

                        winner.getWorld().playSound(winner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, .3F);

                        ParticleAPI.sendParticleToEntityLocation(ParticleAPI.ParticleEffect.HAPPY_VILLAGER, winner, 1, 1, 1, 0.03F, 50);

                        Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + "** " + winner.getName() + ChatColor.GOLD + " has received a " +
                                mountItem.getItemMeta().getDisplayName() + ChatColor.GOLD + " from the " + dungeonType.getDungeonName() +
                                ChatColor.GOLD + " Dungeon as a rare drop! " + ChatColor.GOLD + ChatColor.BOLD + "**");
                        TitleAPI.sendActionBar(winner, ChatColor.GREEN.toString() + ChatColor.BOLD + "You have received a " +
                                mountItem.getItemMeta().getDisplayName() + ChatColor.GREEN + "!", 20 * 5);

                    }
                }
            }
        }

    }
}
