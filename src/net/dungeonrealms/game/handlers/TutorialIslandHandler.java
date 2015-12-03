package net.dungeonrealms.game.handlers;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.mongo.achievements.AchievementManager;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.ItemGenerator;
import net.dungeonrealms.game.world.items.armor.Armor;
import net.dungeonrealms.game.world.items.armor.ArmorGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

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
                if (player1.getUniqueId().toString().equals(player.getUniqueId().toString())) {
                    continue;
                }
                player1.hidePlayer(player);
            }
        });
    }

    public void giveStarterKit(Player player) {
        player.getInventory().addItem(new ItemBuilder().setItem(new ItemGenerator().getDefinedStack(Item.ItemType.AXE, Item.ItemTier.TIER_1, Item.ItemModifier.UNCOMMON))
                .setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.HELMET, Armor.ArmorTier.TIER_1, Armor.ArmorModifier.COMMON))
                .setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.CHESTPLATE, Armor.ArmorTier.TIER_1, Armor.ArmorModifier.COMMON))
                .setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.LEGGINGS, Armor.ArmorTier.TIER_1, Armor.ArmorModifier.COMMON))
                .setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.BOOTS, Armor.ArmorTier.TIER_1, Armor.ArmorModifier.COMMON))
                .setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(new ItemStack(Material.BREAD, 5)).setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false)).setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false)).setNBTString("subtype", "starter").build());
        player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false)).setNBTString("subtype", "starter").build());
    }
}
