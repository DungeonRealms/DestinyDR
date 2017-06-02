package net.dungeonrealms.game.world.shops;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.chat.Chat;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Chase on Nov 17, 2015
 */
public class Shop {

    public UUID ownerUUID;
    @Getter
    public int characterID;
    public String ownerName;
    public Block block1;
    public Block block2;
    public Hologram hologram;
    public boolean isopen;
    public int shopLevel = 1;
    public Inventory inventory;
    public String shopName;
    @Getter
    @Setter
    private String description;
    public int viewCount;
    public List<String> uniqueViewers = new ArrayList<>();
    @Getter
    @Setter
    private LinkedList<String> purchaseHistory = new LinkedList<>();

    public static final String HEART = "❤";

    public Shop(UUID uuid, Location loc, int characterID, String shopName) {
        this.ownerUUID = uuid;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        this.shopLevel = wrapper.getShopLevel();
        this.ownerName = getOwner().getName();
        this.block1 = loc.getWorld().getBlockAt(loc);
        this.block2 = loc.getWorld().getBlockAt(loc.add(1, 0, 0));
        this.shopName = shopName;
        hologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), loc.add(0, 1.5, .5));
        hologram.insertTextLine(0, ChatColor.RED + shopName);
        hologram.insertTextLine(1, "0 " + ChatColor.RED + HEART);
        hologram.getVisibilityManager().setVisibleByDefault(true);
        isopen = false;
        inventory = createNewInv(ownerUUID);
        viewCount = 0;
        this.characterID = characterID;
        this.uniqueViewers = new ArrayList<>();
        this.purchaseHistory.addAll(wrapper.getPurchaseHistory());
        this.description = wrapper.getShopDescription();
    }

    private ItemStack getOpenShopButton() {
        return new NBTWrapper(ItemManager.createItem(Material.INK_SACK,
                ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Click to OPEN Shop", DyeColor.GRAY.getDyeData()," ",
                ChatColor.GRAY + "This will open your shop to the public.")).setString("status", "off").build();
    }

    private ItemStack getDeleteShopButton() {
        return new NBTWrapper(ItemManager.createItem(Material.BARRIER,
                ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Click to DELETE Shop"," ",
                ChatColor.GRAY + "This will safely delete your shop")).setString("statusClose", "disabledInventorySessionChecker")
                .build();
    }

    private ItemStack getDescriptionShopButton() {
        String description = this.description == null ? "No Description" : this.description;
        ItemStack stack = new NBTWrapper(ItemManager.createItem(Material.SKULL_ITEM,
                ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + ownerName + "'s Shop", (short) 3," ",
                ChatColor.GRAY + description))
                .build();
        ItemMeta meta = stack.getItemMeta();
        ((SkullMeta) meta).setOwner(ownerName);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack getPurchaseHistoryShopButton() {
        ItemStack stack = new NBTWrapper(ItemManager.createItem(Material.BOOK_AND_QUILL,
                ChatColor.GREEN + ChatColor.BOLD.toString() + "Shop Ledger"))
                .build();
        ItemMeta meta = stack.getItemMeta();

        List<String> lists = Lists.newArrayList("");
        if (purchaseHistory.size() <= 0) {
            lists.add(ChatColor.GRAY + "No items recently sold.");
        } else {
            lists.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Recently Sold");
            lists.addAll(purchaseHistory);
        }

        meta.setLore(lists);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack getFillerShopItem() {
        return new NBTWrapper(ItemManager.createItem(Material.STAINED_GLASS_PANE, (short) 15, " ")).build();
    }

    private Inventory createNewInv(UUID uuid) {
        int invSize = getInvSize();
        Inventory inv = Bukkit.createInventory(null, invSize, shopName + " - @" + Bukkit.getPlayer(uuid).getName());
        fillBottomRow(inv);
        return inv;
    }

    private void fillBottomRow(Inventory inv) {
        int invSize = getInvSize();
        inv.setItem(invSize - 1, getOpenShopButton());
        inv.setItem(invSize - 2, getDeleteShopButton());

        inv.setItem(invSize - 5, getDescriptionShopButton());
        inv.setItem(invSize - 9, getPurchaseHistoryShopButton());
        for (int slot = invSize - 1; slot >= invSize - 9; slot--) {
            ItemStack current = inv.getItem(slot);
            if (current != null && !current.getType().equals(Material.AIR)) continue;
            inv.setItem(slot, getFillerShopItem());
        }

    }

    public void addPurchaseHistory(String itemName, int gemPrice, int quantity) {
        purchaseHistory.add(ChatColor.GREEN.toString() + ChatColor.BOLD + " * " + ChatColor.GREEN + quantity + "x " + itemName + " " + ChatColor.GREEN + ChatColor.BOLD + gemPrice + "g");
        if (purchaseHistory.size() > 5) {
            purchaseHistory.remove(0);
        }
        inventory.setItem(inventory.getSize() - 9, getPurchaseHistoryShopButton());
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(ownerUUID);
        if (wrapper != null) {
            wrapper.getPurchaseHistory().clear();
            wrapper.getPurchaseHistory().addAll(purchaseHistory);
        }
    }

    public void setDescription(String newDescription) {
        if (newDescription.length() > 32) newDescription = newDescription.substring(0, 32);
        this.description = newDescription;
        inventory.setItem(getInvSize() - 5, getDescriptionShopButton());
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(ownerUUID);
        if (wrapper != null) {
            wrapper.setShopDescription(newDescription);
        }
    }

    public int getInvSize() {
        return (9 * this.shopLevel) + 9;
    }

    public Player getOwner() {
        return Bukkit.getPlayer(ownerUUID);
    }


    /**
     * Deletes block, and unregisters all things for shop.
     *
     * @since 1.0
     */
    @SneakyThrows
    public void deleteShop(boolean shutDown, PreparedStatement saveStatement) {
        // Remove the actual game gui
        ShopMechanics.ALLSHOPS.remove(ownerName);
        // Remove blocks
        hologram.delete();
        if (!shutDown) { //Prevents a crash in crashHandler() (Accessing Bukkit API Async)
            block1.setType(Material.AIR);
            block2.setType(Material.AIR);
            block1.getWorld().playSound(block1.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
        }

        Player owner = Bukkit.getPlayer(ownerUUID);

        viewCount = 0;
        //Rip concurrency.
        Lists.newArrayList(inventory.getViewers()).forEach(HumanEntity::closeInventory);
        uniqueViewers.clear();

        saveCollectionBin(shutDown, owner, saveStatement);

        if (shutDown) {
            if (saveStatement != null) {
                saveStatement.addBatch(QueryType.SET_HASSHOP.getQuery(0, this.characterID));
            }
//            DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.HASSHOP, false, true);
            DungeonRealms.getInstance().getLogger().info(ownerName + " shop deleted correctly.");
        } else {
            if (owner != null) {
                if (BankMechanics.shopPricing.containsKey(owner.getName())) {
                    owner.getInventory().addItem(BankMechanics.shopPricing.get(owner.getName()));
                    BankMechanics.shopPricing.remove(owner.getName());
                }
            }
            SQLDatabaseAPI.getInstance().addQuery(QueryType.SET_HASSHOP, 0, this.characterID);
//            DungeonRealms.getInstance().getServer().getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
//                DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.HASSHOP, false, true)
//            });
        }

        if (owner != null) {
            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(owner);
            if (wrapper != null) {
                //Shop closed.
                wrapper.setShopOpened(false);
            }
        }
    }

    /**
     * save to collection
     */
    @SneakyThrows
    private void saveCollectionBin(boolean shutDown, Player player, PreparedStatement state) {
        Inventory inv = Bukkit.createInventory(null, inventory.getSize(), "Collection Bin");
        int count = 0;
        for (int slot = 0; slot < inventory.getSize() - 9; slot++) {
            ItemStack stack = inventory.getItem(slot);
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
            if (stack != null && stack.getType() != Material.AIR) {
                if (stack.getType() == Material.INK_SACK && nms.hasTag() && nms.getTag().hasKey("status") || stack.getType() == Material.BARRIER && nms.hasTag() && nms.getTag().hasKey("statusClose"))
                    continue;
                VanillaItem vi = new VanillaItem(stack);
                vi.setShowPrice(false);
                inv.addItem(vi.generateItem());
                count++;
            }
        }
        if (count > 0) {
//            Player player = Bukkit.getPlayer(ownerUUID);
            if (player != null)
                player.sendMessage(ChatColor.GREEN + "Your shop was saved and can now be found in your Collection Bin.");

            Storage storage = BankMechanics.getStorage(ownerUUID);
            if (storage != null)
                storage.collection_bin = inv;

            String invToString = ItemSerialization.toString(inv);
            //Only save on shutdown / logout, otherwise they can take items out from the inventory, then /closeshop to load it back from Mongo.

            if (shutDown)
                state.addBatch(QueryType.UPDATE_COLLECTION_BIN.getQuery(invToString, this.characterID));
            else if (state == null) {
                //Just need to save collection bin?
                SQLDatabaseAPI.getInstance().executeUpdate(callback -> {
                    Bukkit.getLogger().info("Setting collection_storage to " + invToString + " for " + this.characterID);
                }, QueryType.UPDATE_COLLECTION_BIN.getQuery(invToString, this.characterID));
            }
//                DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, invToString, true);
        } else {
            SQLDatabaseAPI.getInstance().addQuery(QueryType.UPDATE_COLLECTION_BIN, "", this.characterID);
//            DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, "", true);
        }

        Storage storage = BankMechanics.getStorage(ownerUUID);
        if (storage != null)
            storage.collection_bin = inv;
    }

    /**
     * @return
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     *
     */
    public void updateStatus() {
        isopen = !isopen;
        hologram.clearLines();
        if (!isopen) {
            List<HumanEntity> viewers = new ArrayList<>();
            if (inventory.getViewers().size() > 0) {
                inventory.getViewers().forEach(viewers::add);
            }
            viewers.forEach(HumanEntity::closeInventory);
            viewers.clear();
            inventory.setItem(inventory.getSize() - 1, getOpenShopButton());
            hologram.clearLines();
            hologram.insertTextLine(0, ChatColor.RED + shopName);
            hologram.insertTextLine(1, String.valueOf(viewCount) + ChatColor.RED + " " + HEART);
        } else {
            inventory.setItem(inventory.getSize() - 1, new NBTWrapper(ItemManager.createItem(Material.INK_SACK,
                    ChatColor.RED.toString() + "Click to " + ChatColor.BOLD + "CLOSE" + ChatColor.RED + " Shop",
                    DyeColor.LIME.getDyeData(), ChatColor.GRAY + "This will allow you to edit your stock.")).setString("status", "on")
                    .build());
            hologram.clearLines();
            hologram.insertTextLine(0, ChatColor.GREEN + "[S] " + shopName);
            hologram.insertTextLine(1, String.valueOf(viewCount) + ChatColor.RED + " " + HEART);
        }
    }


    public boolean promptUpgrade = false;

    /**
     * ;^)
     */
    public void promptUpgrade() {
        Player p = getOwner();
        if (p == null)
            return;
        int new_tier = this.shopLevel + 1;
        /*if (Rank.getInstance().getRank(p.getUniqueId()).getName().equalsIgnoreCase("DEFAULT")) {
            if (new_tier >= 4) {
                //Click to view shop!
                TextComponent bungeeMessage = new TextComponent(ChatColor.YELLOW.toString() + ChatColor.UNDERLINE + ChatColor.BOLD + "SHOP");
                bungeeMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://www.dungeonrealms.net/store/category/566366"));
                bungeeMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to view shop!").create()));
                TextComponent test = new TextComponent(ChatColor.RED + "Purchase Subscriber @ ");
                test.addExtra(bungeeMessage);
                test.addExtra(ChatColor.RED + " to upgrade further.");
                p.spigot().sendMessage(test);
                return;
            }
        }*/

        if (new_tier >= 7) {
            p.sendMessage(ChatColor.RED + "Your shop is already at it's maximum size. (54 slots)");
            return;
        }
        int cost = getShopUpgradeCost(new_tier);
        p.sendMessage("");
        p.sendMessage(ChatColor.DARK_GRAY + "           *** " + ChatColor.GREEN + ChatColor.BOLD + "Shop Upgrade Confirmation" + ChatColor.DARK_GRAY + " ***");
        p.sendMessage(ChatColor.DARK_GRAY + "           CURRENT Slots: " + ChatColor.GREEN + (new_tier - 1) * 9 + ChatColor.DARK_GRAY + "          NEW Slots: " + ChatColor.GREEN + new_tier * 9);
        p.sendMessage(ChatColor.DARK_GRAY + "                  Upgrade Cost: " + ChatColor.GREEN + "" + cost + " Gem(s)");
        p.sendMessage("");
        p.sendMessage(ChatColor.GREEN + "Type 'confirm' to confirm your upgrade.");
        p.sendMessage("");
        p.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "WARNING:" + ChatColor.RED + " Player owned shop upgrades are " + ChatColor.BOLD + ChatColor.RED + "NOT" + ChatColor.RED + " reversible or refundable. Type 'cancel' to void this upgrade request.");
        p.sendMessage("");
        Chat.listenForMessage(p, chat -> {
            if (!chat.getMessage().equalsIgnoreCase("confirm")) {
                p.sendMessage(ChatColor.RED + "Shop upgrade cancelled.");
                return;
            }
            if (BankMechanics.getGemsInInventory(p) < cost) {
                p.sendMessage(ChatColor.RED + "You do not have enough gems to purchase this upgrade. Upgrade cancelled.");
                p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "COST: " + ChatColor.RED + cost + ChatColor.BOLD + "G");
                return;
            }
            BankMechanics.takeGemsFromInventory(p, cost);
            upgradeShop(p, new_tier);
            p.sendMessage("");
            p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "*** SHOP UPGRADE TO LEVEL " + new_tier + " COMPLETE ***");
            p.sendMessage(ChatColor.GRAY + "You now have " + (new_tier * 9) + " shop slots available.");
            p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "-" + ChatColor.RED + cost + ChatColor.BOLD + "G");
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1.25F);
            Achievements.getInstance().giveAchievement(p.getUniqueId(), Achievements.EnumAchievements.SHOP_UPGRADE_I);
        }, player -> player.sendMessage(ChatColor.RED + "Action cancelled."));
    }

    /**
     * @param p
     * @param new_tier
     */
    private void upgradeShop(Player p, int new_tier) {
        //DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.SHOPLEVEL, new_tier, true);
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(p);
        wrapper.setShopLevel(new_tier);
        this.shopLevel = new_tier;

        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
            ItemStack[] items = inventory.getContents();
            inventory = createNewInv(p.getUniqueId());
            for (int slot = 0; slot < items.length - 9; slot++) {
                ItemStack stack = items[slot];
                if (stack == null || stack.getType() == Material.AIR) {
                    continue;
                }
                net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
                if (nms.hasTag()) {
                    if (nms.getTag().hasKey("status")) {
                        continue;
                    }
                }
                inventory.addItem(stack);
            }
        });
    }

    /**
     * @param new_tier
     * @return
     */
    public int getShopUpgradeCost(int new_tier) {
        if (new_tier == 2) {
            return 200;
        }
        if (new_tier == 3) {
            return 450;
        }
        if (new_tier == 4) {
            return 800;
        }
        if (new_tier == 5) {
            return 1200;
        }
        if (new_tier == 6) {
            return 1500;
        }
        if (new_tier == 7) {
            return 2000;
        }
        return 0;
    }

    public boolean hasCustomName(ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            if (itemStack.getItemMeta().hasDisplayName()) {
                return true;
            }
        }
        return false;
    }


    public boolean hasRoom() {
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR)
                return true;
        }
        return false;
    }
}
