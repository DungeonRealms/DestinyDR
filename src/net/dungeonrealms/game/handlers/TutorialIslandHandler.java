package net.dungeonrealms.game.handlers;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.mongo.achievements.AchievementManager;
import net.dungeonrealms.game.world.items.Item.ItemRarity;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;

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
                if (player1.getUniqueId().toString().equals(player.getUniqueId().toString())) {
                    continue;
                }
                player1.hidePlayer(player);
            }
        });
    }

    public void giveStarterKit(Player player) {
        player.getInventory().addItem(new ItemBuilder().setItem(new ItemGenerator().setType(ItemType.AXE).setTier(ItemTier.TIER_1).setRarity(ItemRarity.COMMON).generateItem().getItem())
                .setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(new ItemGenerator().setType(ItemType.HELMET).setTier(ItemTier.TIER_1).setRarity(ItemRarity.COMMON).generateItem().getItem())
                .setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(new ItemGenerator().setType(ItemType.CHESTPLATE).setTier(ItemTier.TIER_1).setRarity(ItemRarity.COMMON).generateItem().getItem())
                .setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(new ItemGenerator().setType(ItemType.LEGGINGS).setTier(ItemTier.TIER_1).setRarity(ItemRarity.COMMON).generateItem().getItem())
                .setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(new ItemGenerator().setType(ItemType.BOOTS).setTier(ItemTier.TIER_1).setRarity(ItemRarity.COMMON).generateItem().getItem())
                .setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(new ItemStack(Material.BREAD, 5)).setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false)).setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false)).setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false)).setNBTString("subtype", "starter").build());
    }
}
