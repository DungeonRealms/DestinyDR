package net.dungeonrealms.game.world.realms.instance;

import lombok.Setter;
import net.dungeonrealms.API;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.menus.AbstractMenu;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.ui.GUIButtonClickEvent;
import net.dungeonrealms.game.ui.item.GUIButton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/26/2016
 */
class RealmMaterialFactory {

    // WHERE WE CACHE ALL THE STORES STATICALLY //
    private static List<RealmMaterialStore> REALM_MATERIAL_STORES = new CopyOnWriteArrayList<>();

    // THIS IS HOW MANY PAGES WE NEED FOR NOW //
    private static final int MAX_PAGES = 3;


    // THE INSTANCE //
    private static RealmMaterialFactory instance = null;

    protected static RealmMaterialFactory getInstance() {
        if (instance == null) instance = new RealmMaterialFactory();
        return instance;
    }

    private RealmMaterialFactory() {
        for (int i = 0; i < MAX_PAGES; i++)
            REALM_MATERIAL_STORES.add(new RealmMaterialStore("Realm Material Store (" + (i + 1) + "/" + MAX_PAGES + ")"));

        // ALL SHOP ITEMS // IKR? FOKING MESSY //
        List<RealmMaterialItem> items = new ArrayList<>();

        items.add(new RealmMaterialItem(new ItemStack(Material.DIRT, 1)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SAND, 3)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STONE, 3)));
        items.add(new RealmMaterialItem(new ItemStack(Material.LOG, 5, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.LOG, 6, (short) 1)));
        items.add(new RealmMaterialItem(new ItemStack(Material.LOG, 6, (short) 2)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SANDSTONE, 10)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SANDSTONE, 15, (short) 1)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SANDSTONE, 25, (short) 2)));
        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 3)));
        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 1)));
        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 2)));
        items.add(new RealmMaterialItem(new ItemStack(Material.GLASS, 5)));
        items.add(new RealmMaterialItem(new ItemStack(Material.GLOWSTONE, 10)));
        items.add(new RealmMaterialItem(new ItemStack(Material.REDSTONE, 10)));
        items.add(new RealmMaterialItem(new ItemStack(Material.COAL, 2)));
        items.add(new RealmMaterialItem(new ItemStack(Material.IRON_INGOT, 10)));
        items.add(new RealmMaterialItem(new ItemStack(Material.CLAY, 15)));

        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 3)));
        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 4)));
        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 5)));

        items.add(new RealmMaterialItem(new ItemStack(Material.WATER_BUCKET, 5)));
        items.add(new RealmMaterialItem(new ItemStack(Material.LAVA_BUCKET, 15)));

        items.add(new RealmMaterialItem(new ItemStack(Material.ICE, 3)));
        items.add(new RealmMaterialItem(new ItemStack(Material.OBSIDIAN, 55)));
        items.add(new RealmMaterialItem(new ItemStack(Material.LAPIS_BLOCK, 40)));
        //items.add( new RealmMaterialItem(new ItemStack(Material.QUARTZ, 50));
        items.add(new RealmMaterialItem(new ItemStack(Material.FLINT_AND_STEEL, 10)));

        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 6)));
        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 7)));
        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 8)));

        items.add(new RealmMaterialItem(new ItemStack(Material.MOSSY_COBBLESTONE, 10)));
        items.add(new RealmMaterialItem(new ItemStack(Material.COBBLESTONE, 3)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SMOOTH_BRICK, 8)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SMOOTH_BRICK, 7, (short) 1)));
        items.add(new RealmMaterialItem(new ItemStack(Material.LEAVES, 2)));

        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 9)));
        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 10)));
        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 11)));

        items.add(new RealmMaterialItem(new ItemStack(Material.WEB, 20)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SNOW_BLOCK, 10)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SMOOTH_BRICK, 12, (short) 2)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SMOOTH_BRICK, 12, (short) 3)));
        items.add(new RealmMaterialItem(new ItemStack(Material.NETHER_BRICK, 60)));
        items.add(new RealmMaterialItem(new ItemStack(Material.INK_SACK, 10, (short) 15)));

        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 12)));
        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 13)));
        items.add(new RealmMaterialItem(new ItemStack(Material.WOOL, 6, (short) 14)));
        //items.add( new RealmMaterialItem(new ItemStack(Material.PUMPKIN, 25));

        items.add(new RealmMaterialItem(new ItemStack(Material.GRASS, 2)));
        items.add(new RealmMaterialItem(new ItemStack(Material.MYCEL, 35)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SOUL_SAND, 55)));
        items.add(new RealmMaterialItem(new ItemStack(Material.NETHERRACK, 40)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SPONGE, 30)));

        items.add(new RealmMaterialItem(new ItemStack(Material.CACTUS, 30)));
        items.add(new RealmMaterialItem(new ItemStack(Material.CLAY_BRICK, 10)));
        items.add(new RealmMaterialItem(new ItemStack(Material.LEAVES, 3, (short) 1)));
        items.add(new RealmMaterialItem(new ItemStack(Material.LEAVES, 3, (short) 2)));
        items.add(new RealmMaterialItem(new ItemStack(Material.LEAVES, 3, (short) 3)));
        items.add(new RealmMaterialItem(new ItemStack(Material.LOG, 8, (short) 3)));
        items.add(new RealmMaterialItem(new ItemStack(Material.RED_ROSE, 5)));
        items.add(new RealmMaterialItem(new ItemStack(Material.YELLOW_FLOWER, 5)));
        items.add(new RealmMaterialItem(new ItemStack(Material.LONG_GRASS, 5)));

        items.add(new RealmMaterialItem(new ItemStack(Material.DISPENSER, 64, (short) 100)));
        items.add(new RealmMaterialItem(new ItemStack(Material.HOPPER, 64, (short) 150)));
        items.add(new RealmMaterialItem(new ItemStack(Material.DROPPER, 64, (short) 120)));
        items.add(new RealmMaterialItem(new ItemStack(Material.MINECART, 64)));
        items.add(new RealmMaterialItem(new ItemStack(Material.BOOKSHELF, 50, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.RAILS, 20)));
        items.add(new RealmMaterialItem(new ItemStack(Material.POWERED_RAIL, 20)));
        items.add(new RealmMaterialItem(new ItemStack(Material.ACTIVATOR_RAIL, 20)));
        items.add(new RealmMaterialItem(new ItemStack(111, 15)));

		/*
         * items.add( new RealmMaterialItem(new ItemStack(Material.JUKEBOX, 64, (short)1200)); items.add( new RealmMaterialItem(new ItemStack(Material.RECORD_3, 64, (short)500));
		 * items.add( new RealmMaterialItem(new ItemStack(Material.RECORD_4, 64, (short)500)); items.add( new RealmMaterialItem(new ItemStack(Material.RECORD_5, 64, (short)500));
		 */

        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 11)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 12)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 13)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 14)));

        // items.add( new RealmMaterialItem(new ItemStack(Material.PISTON_BASE, 64, (short)80));
        // items.add( new RealmMaterialItem(new ItemStack(Material.PISTON_STICKY_BASE, 64, (short)120));
        items.add(new RealmMaterialItem(new ItemStack(Material.SAPLING, 15, (short) 1)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SAPLING, 15, (short) 2)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SAPLING, 15, (short) 3)));

        items.add(new RealmMaterialItem(new ItemStack(Material.QUARTZ_BLOCK, 50, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.QUARTZ_BLOCK, 55, (short) 1)));
        items.add(new RealmMaterialItem(new ItemStack(Material.QUARTZ_BLOCK, 60, (short) 2)));

        items.add(new RealmMaterialItem(new ItemStack(397, 50, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(397, 50, (short) 1)));
        items.add(new RealmMaterialItem(new ItemStack(397, 50, (short) 2)));
        items.add(new RealmMaterialItem(new ItemStack(397, 50, (short) 4)));

        items.add(new RealmMaterialItem(new ItemStack(Material.HAY_BLOCK, 30, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.CARPET, 30, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.HARD_CLAY, 55, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.COAL_BLOCK, 45, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.DAYLIGHT_DETECTOR, 200, (short) 0)));
        // items.add( new RealmMaterialItem(new ItemStack(Material.FLOWER_POT_ITEM, 45, (short) 0));
        items.add(new RealmMaterialItem(new ItemStack(Material.REDSTONE_LAMP_OFF, 55, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.VINE, 50, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 0)));

        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 1)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 2)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 3)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 4)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 5)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 6)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 7)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 8)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 9)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 10)));
        items.add(new RealmMaterialItem(new ItemStack(Material.getMaterial(100), 15, (short) 14)));

        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 11)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 12)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 13)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 14)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_CLAY, 8, (short) 15)));

        items.add(new RealmMaterialItem(new ItemStack(Material.REDSTONE_ORE, 20, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.BEDROCK, 60, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.GRAVEL, 4, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.getMaterial(99), 15, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.getMaterial(99), 15, (short) 15)));
        items.add(new RealmMaterialItem(new ItemStack(Material.getMaterial(99), 15, (short) 14)));
        items.add(new RealmMaterialItem(new ItemStack(Material.getMaterial(162), 8, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.SAND, 4, (short) 1)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 1)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 2)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 3)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 4)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 5)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 6)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 7)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 8)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 9)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 10)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 11)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 12)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 13)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 14)));
        items.add(new RealmMaterialItem(new ItemStack(Material.STAINED_GLASS, 6, (short) 15)));
        items.add(new RealmMaterialItem(new ItemStack(Material.PACKED_ICE, 4, (short) 0)));
        items.add(new RealmMaterialItem(new ItemStack(Material.getMaterial(3), 8, (short) 2)));
        items.add(new RealmMaterialItem(new ItemStack(Material.PISTON_BASE, 64, (short) 80)));
        items.add(new RealmMaterialItem(new ItemStack(Material.PISTON_STICKY_BASE, 64, (short) 120)));


        // FILLS UP STORES //
        Iterator<RealmMaterialStore> iterator = REALM_MATERIAL_STORES.iterator();

        RealmMaterialStore cursor = iterator.next();

        for (RealmMaterialItem item : items) {
            if (cursor.getSize() == (cursor.getInventorySize() - 9)) {
                cursor.fillEmptySpaces(cursor.getSpaceFillerItem());
                cursor = iterator.next();
            }

            cursor.set(cursor.getSize() - 1, item);
        }
    }


    private static void calculatePrices(RealmMaterialItem item) {
        ItemStack is = item.getItemStack();

        double price_each = is.getAmount();
        double ecash_price;
        if (price_each == 64) {
            price_each = is.getDurability();
        }
        if (is.getType() == Material.HOPPER) {
            price_each = 1500;
        }
        if (is.getType() == Material.DROPPER) {
            price_each = 1500;
        }
        if (is.getType() == Material.DISPENSER) {
            price_each = 1500;
        }
        if (is.getType() == Material.JUKEBOX) {
            price_each = 1200;
        }
        if (is.getType() == Material.DAYLIGHT_DETECTOR) {
            price_each = 200;
        }

        is.setAmount(1);
        ecash_price = price_each / 20;
        price_each = price_each / 2;
        if (ecash_price > 1) {
            ecash_price = Math.round(ecash_price);
        }
        if (ecash_price < 1) {
            ecash_price = 1;
        }
        if (price_each < 1) {
            price_each = 1;
        }

        item.setPrice((int) price_each);
        item.setECashPrice(ecash_price);
    }


    private class RealmMaterialStore extends AbstractMenu {

        private int page;

        RealmMaterialStore(String name) {
            super(name, 63);
            this.page = getPage(name);

            if (page < MAX_PAGES) {
                GUIButton nextPageButton = new GUIButton(Material.ARROW) {
                    @Override
                    public void action(GUIButtonClickEvent event) {
                        Bukkit.getScheduler().runTask(plugin, () -> openMaterialStore(event.getWhoClicked(), page));
                    }
                };

                nextPageButton.setDisplayName(ChatColor.YELLOW + "Next Page");
                nextPageButton.setLore(Collections.singletonList(ChatColor.GRAY + "Page " + (page + 1) + "/" + MAX_PAGES));

                set(62, nextPageButton);

            }

            if (page > 1) {
                GUIButton previousPageButton = new GUIButton(Material.ARROW) {
                    @Override
                    public void action(GUIButtonClickEvent event) {
                        Bukkit.getScheduler().runTask(plugin, () -> openMaterialStore(event.getWhoClicked(), page - 2));
                    }
                };

                previousPageButton.setDisplayName(ChatColor.YELLOW + "Previous Page");
                previousPageButton.setLore(Collections.singletonList(ChatColor.GRAY + "Page " + (page - 1) + "/" + MAX_PAGES));

                set(54, previousPageButton);
            }
        }

        @Override
        public void open(Player player) {
            player.openInventory(inventory);
        }
    }

    private static int getPage(String title) {
        char page = title.charAt(title.length() - 4);
        return Integer.parseInt(String.valueOf(page));
    }

    public void openMaterialStore(Player player, int pageIndex) {
        player.closeInventory();
        REALM_MATERIAL_STORES.get(pageIndex).open(player);
    }

    private class RealmMaterialItem extends GUIButton {

        @Setter
        private double eCashPrice;

        @Setter
        private int price;

        RealmMaterialItem(ItemStack item) {
            super(item);
            calculatePrices(this);

            setLore(Arrays.asList(ChatColor.GREEN.toString() + "Price: " + ChatColor.WHITE.toString() + price + "g"
                    , ChatColor.WHITE.toString() + price + ChatColor.GREEN.toString() + " E-CASH",
                    ChatColor.ITALIC + "" + ChatColor.GRAY + "Left click to use gems, Right click to use E-CASH."));

        }


        @Override
        public void action(GUIButtonClickEvent event) throws Exception {
            InventoryClickEvent e = event.getClickEvent();
            Player player = event.getWhoClicked();

            if (e.isLeftClick())
                handleTransaction(player, getItemStack(), false, price);
            else handleTransaction(player, getItemStack(), true, eCashPrice);
        }
    }


    private static void handleTransaction(Player player, ItemStack item, boolean isEcash, double pricePerItem) {
        player.closeInventory();

        if (isEcash) {
            player.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "QUANTITY" + ChatColor.GREEN + " (1-64) you'd like to purchase.");
            player.sendMessage(ChatColor.GRAY + "This material costs " + ChatColor.GOLD + pricePerItem + "EC/each.");

            if (pricePerItem < 1) {
                double per_ecash = Math.round((1 / (pricePerItem)));
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This means you will get " + per_ecash
                        + "x blocks for each E-CASH you use. So a quantity of 1 = " + per_ecash + " blocks.");
            }

        } else {
            player.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "QUANTITY" + ChatColor.GREEN + " (1-64) you'd like to purchase.");
            player.sendMessage(ChatColor.GRAY + "This material costs " + ChatColor.GREEN + (int) pricePerItem + "G/each.");
        }

        Chat.listenForMessage(player, input -> {
            String message = input.getMessage();

            if (message.equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatColor.RED + "Realm Shop Purchase - " + ChatColor.BOLD + "CANCELLED.");
                player.updateInventory();
                return;
            }

            int amount_to_buy = 0;
            try {
                amount_to_buy = Integer.parseInt(message);
            } catch (NumberFormatException nfe) {
                player.sendMessage(ChatColor.RED + "Please enter a valid integer to complete a purchase");
                return;
            }

            if (amount_to_buy > 64) {
                player.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " buy MORE than " + ChatColor.BOLD + "64x" + ChatColor.RED
                        + " of a material per transaction.");
                return;
            }

            if (amount_to_buy <= 0) {
                player.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " buy LESS than " + ChatColor.BOLD + "1x" + ChatColor.RED
                        + " of a material per transaction.");
                return;
            }

            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(ChatColor.RED + "No space available in inventory. Clear some room.");
                return;
            }

            int total_price = (int) (amount_to_buy * pricePerItem);
            double needed_slots = amount_to_buy / 64;
            double available_slots = 0;

            for (ItemStack is : player.getInventory().getContents()) {
                if (is == null || is.getType() == Material.AIR) {
                    available_slots++;
                }
            }

            if (needed_slots > available_slots) {
                player.sendMessage(ChatColor.RED + "No space available in inventory. Clear some room.");
                player.sendMessage(ChatColor.GRAY + "You will need " + needed_slots + " slots.");
                return;
            }


            if (!isEcash && !BankMechanics.getInstance().takeGemsFromInventory(total_price, player)) {
                player.sendMessage(ChatColor.RED + "You do not have enough GEM(s) to complete this purchase.");
                player.sendMessage(ChatColor.GRAY + "" + amount_to_buy + " X " + pricePerItem + " gem(s)/ea = " + (pricePerItem * amount_to_buy) + " gem(s).");
                return;
            }

            GamePlayer gamePlayer = API.getGamePlayer(player);

            if (isEcash && gamePlayer.getEcashBalance() < pricePerItem) {
                player.sendMessage(ChatColor.RED + "You do not have enough E-CASH to complete this purchase.");
                player.sendMessage(ChatColor.GRAY + "" + amount_to_buy + " X " + pricePerItem + " EC/ea = " + (pricePerItem * amount_to_buy) + " EC.");
                player.sendMessage(ChatColor.GRAY + "Purchase more at store.dungeonrealms.net -- instant delivery!");
                return;

            }

            if (isEcash) {
                DonationEffects.getInstance().removeECashFromPlayer(player, total_price);
            }

            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "-" + ChatColor.RED + total_price + ChatColor.BOLD + (isEcash ? " E-CASH" : "G"));
            player.sendMessage(ChatColor.GREEN + "Transaction successful.");

            player.getInventory().setItem(player.getInventory().firstEmpty(), new ItemStack(item.getType(), amount_to_buy, item.getDurability()));
        }, null);
    }

}
