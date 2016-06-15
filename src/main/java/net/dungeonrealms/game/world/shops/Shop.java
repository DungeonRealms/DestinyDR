package net.dungeonrealms.game.world.shops;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Chase on Nov 17, 2015
 */
public class Shop {

    public UUID ownerUUID;
    public String ownerName;
    public Block block1;
    public Block block2;
    public Hologram hologram;
    public boolean isopen;
    public Inventory inventory;
    public Inventory collectionBin;
    public String shopName;
    public int viewCount;
    public List<String> uniqueViewers = new ArrayList<>();

    public Shop(UUID uuid, Location loc, String shopName) {
        this.ownerUUID = uuid;
        this.ownerName = getOwner().getName();
        this.block1 = loc.getWorld().getBlockAt(loc);
        this.block2 = loc.getWorld().getBlockAt(loc.add(1, 0, 0));
        this.shopName = shopName;
        hologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), loc.add(0, 1.5, .5));
        hologram.insertTextLine(0, ChatColor.RED + shopName);
        hologram.insertTextLine(1, "0 " + ChatColor.RED + "❤");
        hologram.getVisibilityManager().setVisibleByDefault(true);
        isopen = false;
        inventory = createNewInv(ownerUUID);
        viewCount = 0;
        this.uniqueViewers = new ArrayList<>();
    }

    private Inventory createNewInv(UUID uuid) {
        int invSize = getInvSize();
        Inventory inv = Bukkit.createInventory(null, invSize, shopName + " - @" + Bukkit.getPlayer(uuid).getName());
        ItemStack button = new ItemStack(Material.INK_SACK, 1, DyeColor.GRAY.getDyeData());
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN.toString() + "Click to OPEN Shop");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "This will open your shop to the public.");
        meta.setLore(lore);
        button.setItemMeta(meta);
        net.minecraft.server.v1_9_R2.ItemStack nmsButton = CraftItemStack.asNMSCopy(button);
        nmsButton.getTag().setString("status", "off");
        inv.setItem(invSize - 1, CraftItemStack.asBukkitCopy(nmsButton));
        return inv;
    }

    public int getInvSize() {
        int lvl = (int) DatabaseAPI.getInstance().getData(EnumData.SHOPLEVEL, ownerUUID);
        return 9 * lvl;
    }

    public Player getOwner() {
        return Bukkit.getPlayer(ownerUUID);
    }

    /**
     * Deletes block, and unregisters all things for shop.
     *
     * @since 1.0
     */
    public void deleteShop(boolean shutDown) {
        if (BankMechanics.shopPricing.containsKey(Bukkit.getPlayer(ownerUUID).getName())) {
            Bukkit.getPlayer(ownerUUID).getInventory().addItem(BankMechanics.shopPricing.get(Bukkit.getPlayer(ownerUUID).getName()));
            BankMechanics.shopPricing.remove(Bukkit.getPlayer(ownerUUID).getName());
        }
        DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.HASSHOP, false, false);
        hologram.delete();
        block1.setType(Material.AIR);
        block2.setType(Material.AIR);
        block1.getWorld().playSound(block1.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
        uniqueViewers.clear();
        viewCount = 0;
        saveCollectionBin();
        ShopMechanics.ALLSHOPS.remove(ownerName);
    }

    /**
     * save to collection
     */
    private void saveCollectionBin() {
        Inventory inv = Bukkit.createInventory(null, inventory.getSize(), "Collection Bin");
        int count = 0;
        for (ItemStack stack : inventory) {
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
            if (stack != null && stack.getType() != Material.AIR) {
                if (stack.getType() == Material.INK_SACK && nms.hasTag() && nms.getTag().hasKey("status"))
                    continue;
                ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.getLore();
                    if (lore != null)
                        for (int j = 0; j < lore.size(); j++) {
                            String currentStr = lore.get(j);
                            if (currentStr.contains("Price")) {
                                lore.remove(j);
                                break;
                            }
                        }
                    if (nms.getTag().hasKey("worth"))
                        nms.getTag().remove("worth");
                    meta.setLore(lore);
                    stack.setItemMeta(meta);
                }
                inv.addItem(stack);
                count++;
            }
        }
        if (count > 0) {
            if (Bukkit.getPlayer(ownerUUID) != null) {
                Bukkit.getPlayer(ownerUUID).sendMessage(ChatColor.GREEN + "Your shop was saved and can now be found in your Collection Bin.");
            }
            if (BankMechanics.getInstance().getStorage(ownerUUID) != null) {
                BankMechanics.getInstance().getStorage(ownerUUID).collection_bin = inv;
            }
            String invToString = ItemSerialization.toString(inv);
            DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, invToString, true);
        } else {
            DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, "", true);
        }
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
            viewers.stream().forEach(HumanEntity::closeInventory);
            viewers.clear();
            ItemStack button = new ItemStack(Material.INK_SACK, 1, DyeColor.GRAY.getDyeData());
            ItemMeta meta = button.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN.toString() + "Click to OPEN Shop");
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "This will open your shop to the public.");
            meta.setLore(lore);
            button.setItemMeta(meta);
            net.minecraft.server.v1_9_R2.ItemStack nmsButton = CraftItemStack.asNMSCopy(button);
            nmsButton.getTag().setString("status", "off");
            inventory.setItem(inventory.getSize() - 1, CraftItemStack.asBukkitCopy(nmsButton));
            hologram.clearLines();
            hologram.insertTextLine(0, ChatColor.RED + shopName);
            hologram.insertTextLine(1, String.valueOf(viewCount) + ChatColor.RED + " ❤");
        } else {
            ItemStack button = new ItemStack(Material.INK_SACK, 1, DyeColor.LIME.getDyeData());
            ItemMeta meta = button.getItemMeta();
            meta.setDisplayName(ChatColor.RED.toString() + "Click to CLOSE Shop");
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "This will allow you to edit your stock.");
            meta.setLore(lore);
            button.setItemMeta(meta);
            net.minecraft.server.v1_9_R2.ItemStack nmsButton = CraftItemStack.asNMSCopy(button);
            nmsButton.getTag().setString("status", "on");
            inventory.setItem(inventory.getSize() - 1, CraftItemStack.asBukkitCopy(nmsButton));
            hologram.clearLines();
            hologram.insertTextLine(0, ChatColor.GREEN + "[S] " + shopName);
            hologram.insertTextLine(1, String.valueOf(viewCount) + ChatColor.RED + " ❤");
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
        int new_tier = (int) DatabaseAPI.getInstance().getData(EnumData.SHOPLEVEL, ownerUUID) + 1;

        /*if (Rank.getInstance().getRank(p.getUniqueId()).getName().equalsIgnoreCase("DEFAULT")) {
            if (new_tier >= 4) {
                //Click to view shop!
                TextComponent bungeeMessage = new TextComponent(ChatColor.YELLOW.toString() + ChatColor.UNDERLINE + ChatColor.BOLD + "SHOP");
                bungeeMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://shop.dungeonrealms.net/category/566366"));
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
            if (chat.getMessage().equalsIgnoreCase("cancel")) {
                p.sendMessage(ChatColor.RED + "Shop upgrade cancelled.");
                return;
            }
            if (!chat.getMessage().equalsIgnoreCase("confirm")) {
                p.sendMessage(ChatColor.RED + "Shop upgrade cancelled.");
                return;
            }
            if (BankMechanics.getInstance().getTotalGemsInInventory(p) < cost) {
                p.sendMessage(ChatColor.RED + "You do not have enough gems to purchase this upgrade. Upgrade cancelled.");
                p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "COST: " + ChatColor.RED + cost + ChatColor.BOLD + "G");
                return;
            }
            BankMechanics.getInstance().takeGemsFromInventory(cost, p);
            upgradeShop(p, new_tier);
            p.sendMessage("");
            p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "*** SHOP UPGRADE TO LEVEL " + new_tier + " COMPLETE ***");
            p.sendMessage(ChatColor.GRAY + "You now have " + (new_tier * 9) + " shop slots available.");
            p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "-" + ChatColor.RED + cost + ChatColor.BOLD + "G");
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1.25F);
            Achievements.getInstance().giveAchievement(p.getUniqueId(), Achievements.EnumAchievements.SHOP_UPGRADE_1);
        }, player -> player.sendMessage(ChatColor.RED + "Action cancelled."));
    }

    /**
     * @param p
     * @param new_tier
     */
    private void upgradeShop(Player p, int new_tier) {
        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.SHOPLEVEL, new_tier, true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            ItemStack[] items = inventory.getContents();
            inventory = createNewInv(p.getUniqueId());
            for (ItemStack stack : items) {
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
        }, 20);
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


}
