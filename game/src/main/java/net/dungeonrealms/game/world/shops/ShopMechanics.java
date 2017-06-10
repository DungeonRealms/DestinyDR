package net.dungeonrealms.game.world.shops;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.ItemGemPouch;
import net.dungeonrealms.game.item.items.functional.ItemMoney;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveCreateShop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Chase on Nov 17, 2015
 */
public class ShopMechanics implements GenericMechanic, Listener {


    //Stores all the recently sold items on this.
    @Getter
    public static LinkedList<SoldShopItem> recentlySoldItems = new LinkedList<>();

    public static ConcurrentHashMap<String, Shop> ALLSHOPS = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().toLowerCase().startsWith("/")) {
            ALLSHOPS.values().stream().filter(shop -> shop.uniqueViewers.contains(event.getPlayer().getName())).forEach(shop -> {
                event.setCancelled(true);
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getName().contains("@")) {
            Player player = (Player) event.getWhoClicked();
            if (Chat.listened(player)) {
                event.setCancelled(true);
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "You can't interact with inventories whilst being interactive with chat");
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        // Don't check for open inventory on this one
        ALLSHOPS.values().stream().filter(shop -> shop.uniqueViewers.contains(event.getPlayer().getName())).forEach(shop -> {
            event.setCancelled(true);
        });
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        ALLSHOPS.values().stream().filter(shop -> shop.uniqueViewers.contains(event.getPlayer().getName())).forEach(shop -> {
            event.setCancelled(true);
        });
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        ALLSHOPS.values().stream().filter(shop -> shop.uniqueViewers.contains(event.getPlayer().getName())).forEach(shop -> {
            event.setCancelled(true);
        });
    }

    public static Shop getShop(Block block) {
        for (Shop shop : ALLSHOPS.values()) {
            if (shop.block1.getX() == block.getX() && shop.block1.getY() == block.getY()
                    && shop.block1.getZ() == block.getZ()
                    || shop.block2.getX() == block.getX() && shop.block2.getY() == block.getY()
                    && shop.block2.getZ() == block.getZ()) {
                return shop;
            }
        }
        return null;
    }

    public static PreparedStatement deleteAllShops(boolean shutDown) throws SQLException {
        PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("");
        for (Shop shop : ALLSHOPS.values())
            shop.deleteShop(shutDown, statement);
        ALLSHOPS.clear();
        return statement;
    }

    public static void deleteShop(Player player, boolean shutdown) {
        deleteShop(player.getName(), shutdown);
    }

    public static void deleteShop(String name, boolean shutdown) {
        if (ALLSHOPS.containsKey(name))
            getShop(name).deleteShop(shutdown, null);
    }

    public static boolean isItemSellable(ItemStack i) {
        return ItemManager.isItemTradeable(i) && (!ItemMoney.isMoney(i) || ItemGemPouch.isPouch(i)) ;
    }

    public static void setupShop(Block block, UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        player.sendMessage(ChatColor.YELLOW + "Please enter a " + ChatColor.BOLD + "SHOP NAME." + ChatColor.YELLOW + " [max. 12 characters]");
        player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1F, 0.8F);
        Chat.listenForMessage(player, event -> {
            String shopName = event.getMessage();
            if (shopName.length() > 12) {
                player.sendMessage(ChatColor.RED + "Shop name '" + ChatColor.BOLD + shopName + ChatColor.RED + "' exceeds the MAX character limit of 12.");
                return;
            } else if (shopName.length() <= 2) {
                player.sendMessage(ChatColor.RED.toString() + "Shop name must be at least 3 characters.");
                return;
            }
            if (shopName.contains("@")) {
                player.sendMessage(ChatColor.RED + "Invalid character '@' in name.");
                return;
            }
            if (player.getWorld() != block.getWorld()) {
                player.sendMessage(ChatColor.RED + "You cannot place a shop in this world.");
                return;
            }

            boolean foundNearbyBlocks = GameAPI.isAnyMaterialNearby(block, 3, Lists.newArrayList(Material.CHEST, Material.ENDER_CHEST, Material.PORTAL, Material.END_GATEWAY, Material.ENDER_PORTAL_FRAME));

            if (foundNearbyBlocks) {
                player.sendMessage(ChatColor.RED + "You cannot place a shop here.");
                return;
            }

            Block b = block.getWorld().getBlockAt(block.getLocation().add(0, 1, 0));
            Block block2 = block.getWorld().getBlockAt(block.getLocation().add(1, 1, 0));
            if (b.getType() == Material.AIR && block2.getType() == Material.AIR) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                    if (wrapper == null) return;
                    block2.setType(Material.CHEST);
                    b.setType(Material.CHEST);

                    if (Chat.containsIllegal(shopName)) {
                        player.sendMessage(ChatColor.RED + "Shop name contains illegal characters.");
                        return;
                    }

                    Shop shop = new Shop(uniqueId, b.getLocation(), wrapper.getCharacterID(), Chat.getInstance().checkForBannedWords(shopName));
                    wrapper.setShopOpened(true);
                    SQLDatabaseAPI.getInstance().addQuery(QueryType.SET_HASSHOP, 1, wrapper.getCharacterID());

                    ALLSHOPS.put(player.getName(), shop);
                    player.sendMessage(ChatColor.YELLOW + "Shop name assigned.");
                    player.sendMessage("");
                    player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "YOU'VE CREATED A SHOP!");
                    player.sendMessage(ChatColor.YELLOW + "To stock your shop, simply drag items into your shop's inventory.");
                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.SHOP_CREATOR);

                    //  LOAD ITEMS FROM COLLECTION BIN  //
                    Storage storage = BankMechanics.getStorage(player);
                    if (storage.collection_bin != null) {
                        for (ItemStack i : storage.collection_bin.getContents()) {
                            if (i != null && i.getType() != Material.AIR) {
                                VanillaItem vi = new VanillaItem(i);
                                if (vi.getPrice() <= 0)
                                    continue;
                                vi.setShowPrice(true);
                                shop.inventory.addItem(vi.generateItem());
                            }
                        }
                        player.sendMessage(ChatColor.GREEN + "The items from your collection bin have been loaded into your shop.");
                        storage.clearCollectionBin();
                    }

                    Quests.getInstance().triggerObjective(player, ObjectiveCreateShop.class);

                }, 1L);
            } else {
                player.sendMessage(ChatColor.RED + "You can't place a shop there!");
            }
        }, p -> p.sendMessage(ChatColor.RED + "Action cancelled."));
        player.closeInventory();

    }

    /**
     * @param ownerName
     * @return
     */
    public static Shop getShop(String ownerName) {
        return ALLSHOPS.get(ownerName);
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.NO_STARTUP;
    }

    @Override
    public void startInitialization() {
        DungeonRealms.getInstance().getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    @Override
    public void stopInvocation() {

    }
}
