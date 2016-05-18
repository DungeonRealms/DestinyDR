package net.dungeonrealms.game.player.inventory;

import net.dungeonrealms.API;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.world.items.Item.ItemRarity;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Kieran on 10/26/2015.
 */
public class NPCMenus {

    public static void openMountPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, "Animal Vendor");

        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.SADDLE), ChatColor.GREEN + "Old Horse Mount", new String[]{
                ChatColor.RED + "Speed 120%",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "An old brown starter horse.",
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "5000g"}).setNBTString("mountType", "T1HORSE").setNBTInt("mountCost", 5000).build());
        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.DIAMOND_BARDING), ChatColor.AQUA + "Traveler's Horse Mount", new String[]{
                ChatColor.RED + "Speed 140%",
                ChatColor.RED + "Jump 110%",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "A well versed travelling companion.",
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "15000g"}).setNBTString("mountType", "DIAMONDHORSE").setNBTInt("mountCost", 15000).build());
        inv.setItem(2, new ItemBuilder().setItem(new ItemStack(Material.GOLD_BARDING), ChatColor.YELLOW + "Knight's Horse Mount", new String[]{
                ChatColor.RED + "Speed 160%",
                ChatColor.RED + "Jump 110%",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "A mount fit for even the best of adventurers.",
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "35000g"}).setNBTString("mountType", "GOLDHORSE").setNBTInt("mountCost", 35000).build());
        inv.setItem(9, new ItemBuilder().setItem(new ItemStack(Material.LEASH), ChatColor.GREEN + "Storage Mule", new String[]{
                ChatColor.RED + "Storage Size: 9 Items",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "An old worn-out storage mule.",
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "15000g"}).setNBTString("mountType", "MULE").setNBTInt("mountCost", 15000).build());
        player.openInventory(inv);
    }

    public static void openProfessionPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Profession Vendor");
        ItemStack pickAxe = ItemManager.createPickaxe(1);
        ItemStack fishingRod = ItemManager.createFishingPole(1);
        ItemMeta meta = pickAxe.getItemMeta();
        List<String> lore = meta.getLore();
//        String[] array = (String[]) lore.toArray();
        lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + "100g");
        String[] arr = lore.toArray(new String[lore.size()]);
        inv.addItem(editItem(pickAxe, pickAxe.getItemMeta().getDisplayName(), arr));

        ItemMeta meta2 = fishingRod.getItemMeta();
        List<String> lore2 = meta2.getLore();
        lore2.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + "100g");
        String[] arr2 = lore2.toArray(new String[lore2.size()]);
        inv.addItem(editItem(fishingRod, fishingRod.getItemMeta().getDisplayName(), arr2));
        player.openInventory(inv);
    }

    public static void openWizardMenu(Player player) {
        GamePlayer gp = API.getGamePlayer(player);
        if (gp.getLevel() >= 10) {
            if (gp.getStats().resetAmounts > 0) {
                player.sendMessage(ChatColor.GREEN + "You have a free stat reset available! Type 'yes' or 'y' to use it");
                Chat.listenForMessage(player, chat -> {
                    if (chat.getMessage().equalsIgnoreCase("yes") || chat.getMessage().equalsIgnoreCase("y")) {
                        gp.getStats().freeResets-= 1;
                    }
                }, p -> p.sendMessage(ChatColor.RED + "Action cancelled."));
            } else {
                player.sendMessage(ChatColor.RED + "You have already used your free stat reset for your character.");
                player.sendMessage(ChatColor.YELLOW + "You may purchase more resets from the E-Cash vendor!.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You need to be level 10 to use your ONE reset.");
        }
    }

    public static void openECashPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "E-Cash Vendor");

        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.BLAZE_POWDER), ChatColor.RED + "Flame Trail", new String[]{
                ChatColor.AQUA + "649 E-Cash"}).setNBTString("playerTrailType", "FLAME").setNBTInt("ecashCost", 649).build());
        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.JUKEBOX), ChatColor.LIGHT_PURPLE + "Musical Trail", new String[]{
                ChatColor.AQUA + "649 E-Cash"}).setNBTString("playerTrailType", "NOTE").setNBTInt("ecashCost", 649).build());
        inv.setItem(2, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Cloud Trail", new String[]{
                ChatColor.AQUA + "649 E-Cash"}).setNBTString("playerTrailType", "CLOUD").setNBTInt("ecashCost", 649).build());
        inv.setItem(3, new ItemBuilder().setItem(new ItemStack(Material.POTION), ChatColor.DARK_PURPLE + "Cursed Trail", new String[]{
                ChatColor.AQUA + "649 E-Cash"}).setNBTString("playerTrailType", "WITCHMAGIC").setNBTInt("ecashCost", 649).build());
        inv.setItem(4, new ItemBuilder().setItem(Material.SKULL_ITEM, (short) 2, ChatColor.GREEN + "Zombie Horse", new String[]{
                ChatColor.RED + "Speed 160%",
                ChatColor.RED + "Jump 110%",
                ChatColor.AQUA + "749 E-Cash"}).setNBTString("mountType", "ZOMBIEHORSE").setNBTInt("ecashCost", 749).build());
        inv.setItem(5, new ItemBuilder().setItem(Material.SKULL_ITEM, (short) 0, ChatColor.GRAY + "Skeleton Horse", new String[]{
                ChatColor.RED + "Speed 160%",
                ChatColor.RED + "Jump 110%",
                ChatColor.AQUA + "749 E-Cash"}).setNBTString("mountType", "SKELETONHORSE").setNBTInt("ecashCost", 749).build());
        inv.setItem(6, new ItemBuilder().setItem(Material.MONSTER_EGG, (short) 101, ChatColor.YELLOW + "Rabbit Pet", new String[]{
                ChatColor.AQUA + "749 E-Cash"}).setNBTString("petType", "RABBIT").setNBTInt("ecashCost", 749).build());
        inv.setItem(7, new ItemBuilder().setItem(Material.MONSTER_EGG, (short) 98, ChatColor.YELLOW + "Ocelot Pet", new String[]{
                ChatColor.AQUA + "749 E-Cash"}).setNBTString("petType", "OCELOT").setNBTInt("ecashCost", 749).build());
        inv.setItem(8, new ItemBuilder().setItem(new ItemStack(Material.ENDER_CHEST), ChatColor.GREEN + "Storage Expansion", new String[]{
                ChatColor.RED + "Expand Your Bank!",
                ChatColor.AQUA + "999 E-Cash"}).setNBTString("storageExpansion", "xFiniTEAPro").setNBTInt("ecashCost", 999).build());
        inv.setItem(9, new ItemBuilder().setItem(new ItemStack(Material.ENCHANTED_BOOK), ChatColor.GREEN + "Retraining Book", new String[]{
                ChatColor.RED + "Refund ALL Stat Points!",
                ChatColor.AQUA + "399 E-Cash"}).setNBTString("retrainingBook", "_Atlassie").setNBTInt("ecashCost", 399).build());
        inv.setItem(18, new ItemBuilder().setItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Our Store", new String[]{
                ChatColor.AQUA + "Click here to visit our store!"}).setNBTString("donationStore", "ProxyIsAwesome").build());
        inv.setItem(26, new ItemBuilder().setItem(new ItemStack(Material.GOLDEN_APPLE), ChatColor.GREEN + "Current E-Cash", new String[]{
                ChatColor.AQUA + "Your E-Cash Balance is: " + ChatColor.YELLOW.toString() + ChatColor.BOLD + API.getGamePlayer(player).getEcashBalance()}).build());

        player.openInventory(inv);
    }

    public static void openHearthstoneRelocateMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Hearthstone Re-Location");

        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Cyrennica", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "1000g"}).setNBTString("hearthstoneLocation", "CYRENNICA").setNBTInt("gemCost", 1000).build());
        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Harrison Fields", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "1500g"}).setNBTString("hearthstoneLocation", "HARRISON_FIELD").setNBTInt("gemCost", 1500).build());
        inv.setItem(2, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Dark Oak Tavern", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "3500g"}).setNBTString("hearthstoneLocation", "DARK_OAK").setNBTInt("gemCost", 3500).build());
        inv.setItem(3, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Gloomy Hollows", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "3500g"}).setNBTString("hearthstoneLocation", "GLOOMY_HOLLOWS").setNBTInt("gemCost", 3500).build());
        inv.setItem(4, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Tripoli", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "7500g"}).setNBTString("hearthstoneLocation", "TRIPOLI").setNBTInt("gemCost", 7500).build());
        inv.setItem(5, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Trollsbane Tavern", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "7500g"}).setNBTString("hearthstoneLocation", "TROLLSBANE").setNBTInt("gemCost", 7500).build());
        inv.setItem(6, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Crestguard Keep", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "15000g"}).setNBTString("hearthstoneLocation", "CRESTGUARD").setNBTInt("gemCost", 15000).build());
        inv.setItem(7, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Deadpeaks Mountain", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "25000g"}).setNBTString("hearthstoneLocation", "DEADPEAKS").setNBTInt("gemCost", 25000).build());

        player.openInventory(inv);
    }

    public static void openDungeoneerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Dungeoneer");

       /*
        inv.setItem(0, new ItemBuilder().setItem(SandS.getInstance().getScroll(SandS.ScrollType.WHITE_SCROLL, 1)).addLore(ChatColor.AQUA + "1500 Portal Shards [T1]").setNBTInt("shardTier", 1)
                .setNBTInt("shardCost", 1500).build());
        inv.setItem(1, new ItemBuilder().setItem(SandS.getInstance().getScroll(SandS.ScrollType.WHITE_SCROLL, 2)).addLore(ChatColor.AQUA + "1500 Portal Shards [T2]").setNBTInt("shardTier", 2)
                .setNBTInt("shardCost", 1500).build());
        inv.setItem(2, new ItemBuilder().setItem(SandS.getInstance().getScroll(SandS.ScrollType.WHITE_SCROLL, 3)).addLore(ChatColor.AQUA + "1500 Portal Shards [T3]").setNBTInt("shardTier", 3)
                .setNBTInt("shardCost", 1500).build());
        inv.setItem(3, new ItemBuilder().setItem(SandS.getInstance().getScroll(SandS.ScrollType.WHITE_SCROLL, 4)).addLore(ChatColor.AQUA + "1500 Portal Shards [T4]").setNBTInt("shardTier", 4)
                .setNBTInt("shardCost", 1500).build());
        inv.setItem(4, new ItemBuilder().setItem(SandS.getInstance().getScroll(SandS.ScrollType.WHITE_SCROLL, 5)).addLore(ChatColor.AQUA + "1500 Portal Shards [T5]").setNBTInt("shardTier", 5)
                .setNBTInt("shardCost", 1500).build());
        */

        player.openInventory(inv);
    }

    public static void openMerchantMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Merchant");

        inv.setItem(4, new ItemStack(Material.THIN_GLASS));
        inv.setItem(13, new ItemStack(Material.THIN_GLASS));
        inv.setItem(22, new ItemStack(Material.THIN_GLASS));
        inv.setItem(0, new ItemBuilder().setItem(Material.INK_SACK, (short) 8, ChatColor.YELLOW + "Click to ACCEPT", new String[]{
                ""
        }).setNBTString("acceptButton", "whynot").build());
        player.playSound(player.getLocation(), Sound.WOOD_CLICK, 1.f, 1.f);

        player.openInventory(inv);
    }

    public static ItemStack editItem(ItemStack itemStack, String name, String[] lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }

    public static void openFoodVendorMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, "Food Vendor");
        ItemStack potato = ShopMechanics.addPrice(ItemManager.createHealingFood(1, ItemRarity.COMMON), 2);
        ItemStack loadedPotato = ShopMechanics.addPrice(ItemManager.createHealingFood(1, ItemRarity.RARE), 4);
        ItemStack apple = ShopMechanics.addPrice(ItemManager.createHealingFood(1, ItemRarity.UNIQUE), 8);

        ItemStack unCookedChicken = ShopMechanics.addPrice(ItemManager.createHealingFood(2, ItemRarity.COMMON), 10);
        ItemStack RoastedChicken = ShopMechanics.addPrice(ItemManager.createHealingFood(2, ItemRarity.RARE), 14);
        ItemStack pumpkinPie = ShopMechanics.addPrice(ItemManager.createHealingFood(2, ItemRarity.UNIQUE), 18);


        ItemStack saltedPork = ShopMechanics.addPrice(ItemManager.createHealingFood(3, ItemRarity.COMMON), 20);
        ItemStack seasonedPork = ShopMechanics.addPrice(ItemManager.createHealingFood(3, ItemRarity.RARE), 25);
        ItemStack mushroomSoup = ShopMechanics.addPrice(ItemManager.createHealingFood(3, ItemRarity.UNIQUE), 30);

        ItemStack frozenSteak = ShopMechanics.addPrice(ItemManager.createHealingFood(4, ItemRarity.COMMON), 35);
        ItemStack sizzlingSteak = ShopMechanics.addPrice(ItemManager.createHealingFood(4, ItemRarity.RARE), 45);
        ItemStack grilledRabbit = ShopMechanics.addPrice(ItemManager.createHealingFood(4, ItemRarity.UNIQUE), 55);

        ItemStack kingsApple = ShopMechanics.addPrice(ItemManager.createHealingFood(5, ItemRarity.COMMON), 95);
        ItemStack enchantedApple = ShopMechanics.addPrice(ItemManager.createHealingFood(5, ItemRarity.RARE), 100);
        ItemStack goldCarrot = ShopMechanics.addPrice(ItemManager.createHealingFood(5, ItemRarity.UNIQUE), 128);

        inv.setItem(0, potato);
        inv.setItem(1, loadedPotato);
        inv.setItem(2, apple);

        inv.setItem(3, unCookedChicken);
        inv.setItem(4, RoastedChicken);
        inv.setItem(5, pumpkinPie);

        inv.setItem(6, saltedPork);
        inv.setItem(7, seasonedPork);
        inv.setItem(8, mushroomSoup);

        inv.setItem(9, frozenSteak);
        inv.setItem(10, sizzlingSteak);
        inv.setItem(11, grilledRabbit);

        inv.setItem(12, kingsApple);
        inv.setItem(13, enchantedApple);
        inv.setItem(14, goldCarrot);
        player.openInventory(inv);
    }


}
