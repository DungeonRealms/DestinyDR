package net.dungeonrealms.game.player.inventory;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Kieran on 10/26/2015.
 */
public class NPCMenus {

    public static void openMountPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, "Animal Tamer");

        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.SADDLE), ChatColor.GREEN + EnumMounts.TIER1_HORSE.getDisplayName(), new String[]{
                ChatColor.RED + "Speed 120%",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "An old brown starter horse.",
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "3000g",
                ChatColor.GRAY + "Display Item"}).setNBTString("mountType", EnumMounts.TIER1_HORSE.getRawName()).setNBTInt("mountCost", 3000).build());
        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.IRON_BARDING), ChatColor.AQUA + EnumMounts.TIER2_HORSE.getDisplayName(), new String[]{
                ChatColor.RED + "Speed 140%",
                ChatColor.RED + "Jump 110%",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "A horse fit for a humble squire.",
                ChatColor.RED.toString() + ChatColor.BOLD + "REQ: " + ChatColor.RESET + ChatColor.GREEN + EnumMounts.TIER1_HORSE.getDisplayName(),
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "7000g",
                ChatColor.GRAY + "Display Item"}).setNBTString("mountType", EnumMounts.TIER2_HORSE.getRawName()).setNBTInt("mountCost", 7000).build());
        inv.setItem(2, new ItemBuilder().setItem(new ItemStack(Material.DIAMOND_BARDING), ChatColor.LIGHT_PURPLE + EnumMounts.TIER3_HORSE.getDisplayName(), new String[]{
                ChatColor.RED + "Speed 170%",
                ChatColor.RED + "Jump 110%",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "A well versed travelling companion.",
                ChatColor.RED.toString() + ChatColor.BOLD + "REQ: " + ChatColor.RESET + ChatColor.AQUA + EnumMounts.TIER2_HORSE.getDisplayName(),
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "15000g",
                ChatColor.GRAY + "Display Item"}).setNBTString("mountType", EnumMounts.TIER3_HORSE.getRawName()).setNBTInt("mountCost", 15000).build());
        inv.setItem(3, new ItemBuilder().setItem(new ItemStack(Material.GOLD_BARDING), ChatColor.YELLOW + EnumMounts.TIER4_HORSE.getDisplayName(), new String[]{
                ChatColor.RED + "Speed 200%",
                ChatColor.RED + "Jump 110%",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "A mount fit for even the best of adventurers.",
                ChatColor.RED.toString() + ChatColor.BOLD + "REQ: " + ChatColor.RESET + ChatColor.LIGHT_PURPLE + EnumMounts.TIER3_HORSE.getDisplayName(),
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "30000g",
                ChatColor.GRAY + "Display Item"}).setNBTString("mountType", EnumMounts.TIER4_HORSE.getRawName()).setNBTInt("mountCost", 30000).build());
        inv.setItem(9, new ItemBuilder().setItem(new ItemStack(Material.LEASH), ChatColor.GREEN + "Storage Mule", new String[]{
                ChatColor.RED + "Storage Size: 9 Items",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "An old worn-out storage mule.",
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "5000g",
                ChatColor.GRAY + "Display Item"}).setNBTString("mountType", EnumMounts.MULE.getRawName()).setNBTInt("mountCost", 5000).build());
        player.openInventory(inv);
    }

    public static void openProfessionPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Profession Vendor");
        ItemStack pickAxe = ItemManager.createPickaxe(1);
        ItemStack fishingRod = ItemManager.createFishingPole(1);
        ItemMeta meta = pickAxe.getItemMeta();
        List<String> lore = meta.getLore();
        lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + "100g");
        lore.add(ChatColor.GRAY + "Display Item");
        String[] arr = lore.toArray(new String[lore.size()]);
        inv.addItem(editItem(pickAxe, pickAxe.getItemMeta().getDisplayName(), arr));

        ItemMeta meta2 = fishingRod.getItemMeta();
        List<String> lore2 = meta2.getLore();
        lore2.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + "100g");
        lore2.add(ChatColor.GRAY + "Display Item");
        String[] arr2 = lore2.toArray(new String[lore2.size()]);
        inv.addItem(editItem(fishingRod, fishingRod.getItemMeta().getDisplayName(), arr2));
        player.openInventory(inv);
    }

    public static void openWizardMenu(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        int totalResets = wrapper.getPlayerStats().resetAmounts + 1; //Ours start at 0, old DR started at 1.

        int resetCost = (int) ((1000. * Math.pow(1.8, (totalResets + 1))) - ((1000. * Math.pow(1.8, (totalResets + 1))) % 1000));
        resetCost = (resetCost > 60000 ? 60000 : (int) ((1000. * Math.pow(1.8, (totalResets + 1))) - ((1000. * Math.pow(1.8, (totalResets + 1))) % 1000)));
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GRAY + "           *** " + ChatColor.GREEN + ChatColor.BOLD + "Stat Reset Confirmation" + ChatColor.DARK_GRAY + " ***");
        player.sendMessage(ChatColor.DARK_GRAY + "           TOTAL Points: " + ChatColor.GREEN + wrapper.getLevel() * PlayerStats.POINTS_PER_LEVEL + ChatColor.DARK_GRAY + "          SPENT Points: " + ChatColor.GREEN + (wrapper.getLevel() * PlayerStats.POINTS_PER_LEVEL - wrapper.getPlayerStats().freePoints));
        player.sendMessage(ChatColor.DARK_GRAY + "           Reset Cost: " + ChatColor.GREEN + "" + resetCost + " Gem(s)" + ChatColor.DARK_GRAY + "  TOTAL Resets: " + ChatColor.GREEN + totalResets);
        player.sendMessage(ChatColor.DARK_GRAY + "           E-Cash Cost: " + ChatColor.GREEN + "500 ECASH");
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "Enter the code '" + ChatColor.BOLD + "confirm" + ChatColor.GREEN + "' to confirm your gem purchase of a reset." + ChatColor.GREEN + " Or enter the code '" + ChatColor.BOLD + "ecash" + ChatColor.GREEN + "' to purchase using E-CASH.");
        player.sendMessage("");
        player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "WARNING:" + ChatColor.RED + " Stat resets are " + ChatColor.BOLD + ChatColor.RED + "NOT" + ChatColor.RED + " reversible or refundable. Each time you reset your stats the price will increase for the next reset. Type 'cancel' to void this request.");
        player.sendMessage("");

        int finalResetCost = resetCost;
        Chat.listenForMessage(player, event -> {
            if (event.getMessage().equalsIgnoreCase("confirm")) {
                if (BankMechanics.getInstance().takeGemsFromInventory(finalResetCost, player)) {
                    wrapper.getPlayerStats().addReset();
                    wrapper.getPlayerStats().unallocateAllPoints(wrapper.getLevel());
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "All Stat Points have been unallocated!");
                    event.getPlayer().sendMessage(ChatColor.RED.toString() + finalResetCost + "G taken from your account.");
                } else {
                    player.sendMessage(ChatColor.RED + "Stat Reset - Cancelled");
                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "COST: " + finalResetCost + "G " + ChatColor.RED + "insufficient funds.");
                }
            } else if (event.getMessage().equalsIgnoreCase("ecash")) {
                if (wrapper.getEcash() >= 500) {
                    DonationEffects.getInstance().removeECashFromPlayer(player, 500);
                    wrapper.getPlayerStats().addReset();
                    wrapper.getPlayerStats().unallocateAllPoints(wrapper.getLevel());
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "All Stat Points have been unallocated!");
                    event.getPlayer().sendMessage(ChatColor.RED.toString() + 500 + "E-CASH taken from your account.");
                } else {
                    player.sendMessage(ChatColor.RED + "Stat Reset - Cancelled");
                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "COST: 500 ECASH" + ChatColor.RED + " insufficient funds.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Stat Reset - Cancelled");
            }
        }, null);


    }

    public static void openECashPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, "E-Cash Vendor");

        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.MONSTER_EGG), ChatColor.GOLD + "Pets", new String[]{
                ChatColor.GRAY + "View the available E-Cash Pets.",
                ChatColor.GRAY + "Display Item"
        }).build());
        inv.setItem(3, new ItemBuilder().setItem(new ItemStack(Material.GLOWSTONE_DUST), ChatColor.GOLD + "Effects", new String[]{
                ChatColor.GRAY + "View the available E-Cash Effects.",
                ChatColor.GRAY + "Display Item"
        }).build());
        inv.setItem(5, new ItemBuilder().setItem(new ItemStack(Material.SKULL_ITEM), ChatColor.GOLD + "Skins", new String[]{
                ChatColor.GRAY + "View the available E-Cash Skins.",
                ChatColor.GRAY + "Display Item"
        }).build());
        inv.setItem(7, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK), ChatColor.GOLD + "Miscellaneous", new String[]{
                ChatColor.GRAY + "View the available E-Cash Miscellaneous Items.",
                ChatColor.GRAY + "Display Item"
        }).build());
        inv.setItem(9, new ItemBuilder().setItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Our Store", new String[]{
                ChatColor.AQUA + "Click here to visit our store!",
                ChatColor.GRAY + "Display Item"}).setNBTString("donationStore", "ProxyIsAwesome").build());
        inv.setItem(17, new ItemBuilder().setItem(new ItemStack(Material.GOLDEN_APPLE), ChatColor.GREEN + "Current E-Cash", new String[]{
                ChatColor.AQUA + "Your E-Cash Balance is: " + ChatColor.YELLOW.toString() + ChatColor.BOLD + GameAPI.getGamePlayer(player).getEcashBalance(),
                ChatColor.GRAY + "Display Item"}).build());

        player.openInventory(inv);
    }

    public static void openHearthstoneRelocateMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Hearthstone Re-Location");
        
        int slot = 0;
        for(TeleportLocation tl : TeleportLocation.values()){
        	if(tl.getPrice() > 0){
        		inv.setItem(slot, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + tl.getDisplayName(), new String[] {
        			ChatColor.GREEN + "Price: " + ChatColor.WHITE + tl.getPrice() + "g",
        			ChatColor.GRAY + "Display Item"}).setNBTString("hearthstoneLocation", tl.name()).setNBTInt("gemCost", tl.getPrice()).build());
        		slot++;
        	}
        }

        player.openInventory(inv);
    }

    public static void openDungeoneerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Dungeoneer");

        inv.setItem(0, new ItemBuilder().setItem(ItemManager.createMuleUpgrade(2)).addLore(ChatColor.WHITE + "5000" + ChatColor.AQUA + " Portal Key Shards")
                .addLore(ChatColor.GRAY + "Display Item")
                .setNBTInt("shardCost", 5000).setNBTInt("shardTier", 3).build());
        inv.setItem(1, new ItemBuilder().setItem(ItemManager.createMuleUpgrade(3)).addLore(ChatColor.WHITE + "8000" + ChatColor.LIGHT_PURPLE + " Portal Key Shards")
                .addLore(ChatColor.GRAY + "Display Item")
                .setNBTInt("shardCost", 8000).setNBTInt("shardTier", 4).build());
        inv.setItem(2, new ItemBuilder().setItem(ItemManager.createProtectScroll(1)).addLore(ChatColor.WHITE + "1500 Portal Shards")
                .addLore(ChatColor.GRAY + "Display Item").setNBTInt("shardTier", 1)
                .setNBTString("shardColor", ChatColor.WHITE.toString()).setNBTInt("shardCost", 1500).build());
        inv.setItem(3, new ItemBuilder().setItem(ItemManager.createProtectScroll(2)).addLore(ChatColor.WHITE + "1500 " + ChatColor.GREEN + "Portal Shards")
                .addLore(ChatColor.GRAY + "Display Item").setNBTInt("shardTier", 2)
                .setNBTString("shardColor", ChatColor.GREEN.toString()).setNBTInt("shardCost", 1500).build());
        inv.setItem(4, new ItemBuilder().setItem(ItemManager.createProtectScroll(3)).addLore(ChatColor.WHITE + "1500 " + ChatColor.AQUA + "Portal Shards")
                .addLore(ChatColor.GRAY + "Display Item").setNBTInt("shardTier", 3)
                .setNBTString("shardColor", ChatColor.AQUA.toString()).setNBTInt("shardCost", 1500).build());
        inv.setItem(5, new ItemBuilder().setItem(ItemManager.createProtectScroll(4)).addLore(ChatColor.WHITE + "1500 " + ChatColor.LIGHT_PURPLE + "Portal Shards")
                .addLore(ChatColor.GRAY + "Display Item").setNBTInt("shardTier", 4)
                .setNBTString("shardColor", ChatColor.LIGHT_PURPLE.toString()).setNBTInt("shardCost", 1500).build());
        inv.setItem(6, new ItemBuilder().setItem(ItemManager.createProtectScroll(5)).addLore(ChatColor.WHITE + "1500 " + ChatColor.YELLOW + "Portal Shards")
                .addLore(ChatColor.GRAY + "Display Item").setNBTInt("shardTier", 5)
                .setNBTString("shardColor", ChatColor.YELLOW.toString()).setNBTInt("shardCost", 1500).build());

        player.openInventory(inv);
    }

    public static void openMerchantMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Merchant");

        inv.setItem(4, addDisplayLore(new ItemStack(Material.THIN_GLASS)));
        inv.setItem(13, addDisplayLore(new ItemStack(Material.THIN_GLASS)));
        inv.setItem(22, addDisplayLore(new ItemStack(Material.THIN_GLASS)));
        inv.setItem(0, new ItemBuilder().setItem(Material.INK_SACK, (short) 8, ChatColor.YELLOW + "Click to ACCEPT", new String[]{
                "",
                ChatColor.GRAY + "Display Item"
        }).setNBTString("acceptButton", "whynot").build());
        player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.f, 1.f);

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

    private static ItemStack addDisplayLore(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        lore.add(ChatColor.GRAY + "Display Item");
        String[] arr = lore.toArray(new String[lore.size()]);
        stack = NPCMenus.editItem(stack, stack.getItemMeta().getDisplayName(), arr);
        return stack;
    }

    public static void openItemVendorMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, "Item Vendor");

        ItemStack orbOfPeace = ShopMechanics.addPrice(ItemManager.createOrbofPeace(false), 100);
        ItemStack orbOfFlight = ShopMechanics.addPrice(ItemManager.createOrbofFlight(false), 500);

        if (!GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) {
            String guildName = GuildDatabaseAPI.get().getGuildOf(player.getUniqueId());
            ItemStack item = GameAPI.makeItemUntradeable(ItemSerialization.itemStackFromBase64(GuildDatabaseAPI.get().getBannerOf(guildName)));
            ItemStack guildBanner = ShopMechanics.addPrice(item, 1000);

            inv.setItem(0, guildBanner);
            inv.setItem(1, orbOfFlight);
            inv.setItem(2, orbOfPeace);


        } else {

            inv.setItem(0, orbOfFlight);
            inv.setItem(1, orbOfPeace);

        }

        for (int i = 0; i < inv.getContents().length; i++) {
            ItemStack item = inv.getContents()[i];
            if (item == null || item.getType().equals(Material.AIR)) {

                ItemStack spaceFiller = new ItemStack(Material.THIN_GLASS);
                ItemMeta meta = spaceFiller.getItemMeta();
                meta.setDisplayName(" ");
                spaceFiller.setItemMeta(meta);

                inv.setItem(i, spaceFiller);
            }
        }

        player.openInventory(inv);
    }

    public static void openFoodVendorMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, "Food Vendor");
        ItemStack potato = ShopMechanics.addPrice(ItemManager.createHealingFood(1, Item.ItemRarity.COMMON), 2);
        ItemStack loadedPotato = ShopMechanics.addPrice(ItemManager.createHealingFood(1, Item.ItemRarity.RARE), 4);
        ItemStack apple = ShopMechanics.addPrice(ItemManager.createHealingFood(1, Item.ItemRarity.UNIQUE), 8);

        ItemStack unCookedChicken = ShopMechanics.addPrice(ItemManager.createHealingFood(2, Item.ItemRarity.COMMON), 10);
        ItemStack RoastedChicken = ShopMechanics.addPrice(ItemManager.createHealingFood(2, Item.ItemRarity.RARE), 14);
        ItemStack pumpkinPie = ShopMechanics.addPrice(ItemManager.createHealingFood(2, Item.ItemRarity.UNIQUE), 18);


        ItemStack saltedPork = ShopMechanics.addPrice(ItemManager.createHealingFood(3, Item.ItemRarity.COMMON), 20);
        ItemStack seasonedPork = ShopMechanics.addPrice(ItemManager.createHealingFood(3, Item.ItemRarity.RARE), 25);
        ItemStack mushroomSoup = ShopMechanics.addPrice(ItemManager.createHealingFood(3, Item.ItemRarity.UNIQUE), 30);

        ItemStack frozenSteak = ShopMechanics.addPrice(ItemManager.createHealingFood(4, Item.ItemRarity.COMMON), 35);
        ItemStack sizzlingSteak = ShopMechanics.addPrice(ItemManager.createHealingFood(4, Item.ItemRarity.RARE), 45);
        ItemStack grilledRabbit = ShopMechanics.addPrice(ItemManager.createHealingFood(4, Item.ItemRarity.UNIQUE), 55);

        ItemStack kingsApple = ShopMechanics.addPrice(ItemManager.createHealingFood(5, Item.ItemRarity.COMMON), 95);
        ItemStack enchantedApple = ShopMechanics.addPrice(ItemManager.createHealingFood(5, Item.ItemRarity.RARE), 100);
        ItemStack goldCarrot = ShopMechanics.addPrice(ItemManager.createHealingFood(5, Item.ItemRarity.UNIQUE), 128);

        inv.setItem(0, addDisplayLore(potato));
        inv.setItem(1, addDisplayLore(loadedPotato));
        inv.setItem(2, addDisplayLore(apple));

        inv.setItem(3, addDisplayLore(unCookedChicken));
        inv.setItem(4, addDisplayLore(RoastedChicken));
        inv.setItem(5, addDisplayLore(pumpkinPie));

        inv.setItem(6, addDisplayLore(saltedPork));
        inv.setItem(7, addDisplayLore(seasonedPork));
        inv.setItem(8, addDisplayLore(mushroomSoup));

        inv.setItem(9, addDisplayLore(frozenSteak));
        inv.setItem(10, addDisplayLore(sizzlingSteak));
        inv.setItem(11, addDisplayLore(grilledRabbit));

        inv.setItem(12, addDisplayLore(kingsApple));
        inv.setItem(13, addDisplayLore(enchantedApple));
        inv.setItem(14, addDisplayLore(goldCarrot));
        player.openInventory(inv);
    }


}
