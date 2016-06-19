package net.dungeonrealms.game.handlers;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.AchievementManager;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.world.items.Item.ItemRarity;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Kieran on 30-Nov-15.
 */
public class TutorialIslandHandler implements GenericMechanic, Listener {

    private static TutorialIslandHandler instance = null;

    public static TutorialIslandHandler getInstance() {
        if (instance == null) {
            instance = new TutorialIslandHandler();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::hideVanishedPlayers, 100L, 1L);
    }

    @Override
    public void stopInvocation() {
    }

    public boolean onTutorialIsland(UUID uuid) {
        return AchievementManager.REGION_TRACKER.get(uuid).equalsIgnoreCase("tutorial_island");
    }

    private void hideVanishedPlayers() {
        API._hiddenPlayers.stream().filter(player -> player != null).forEach(player -> {
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                // GMs can see hidden players whereas non-GMs cannot.
                if (player1.getUniqueId().toString().equals(player.getUniqueId().toString()) || Rank.isGM(player1)) {
                    player1.showPlayer(player);
                } else {
                    player1.hidePlayer(player);
                }
            }
        });
    }
}
