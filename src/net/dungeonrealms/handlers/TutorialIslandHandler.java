package net.dungeonrealms.handlers;

import net.dungeonrealms.items.Item;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.Armor;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.miscellaneous.ItemBuilder;
import net.dungeonrealms.mongo.achievements.AchievementManager;
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
    }

    @Override
    public void stopInvocation() {
    }

    public boolean onTutorialIsland(UUID uuid) {
        return AchievementManager.REGION_TRACKER.get(uuid).equalsIgnoreCase("tutorial_island");
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
        player.getInventory().addItem(new ItemBuilder().setItem(new ItemStack(Material.BREAD, 10)).setNBTString("subtype", "starter").build());
    }
}
