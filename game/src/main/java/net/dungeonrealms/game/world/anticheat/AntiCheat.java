package net.dungeonrealms.game.world.anticheat;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.mastery.AsyncUtils;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.NBTItem;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.punishment.PunishAPI;
import net.dungeonrealms.game.world.entities.types.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Nick on 10/1/2015.
 */
public class AntiCheat implements GenericMechanic {

    static AntiCheat instance = null;
    @Getter
    @Setter
    private Set<String> uids = new HashSet<>(2000);

    public static AntiCheat getInstance() {
        if (instance == null) {
            instance = new AntiCheat();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
//        Bukkit.getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () -> Bukkit.getOnlinePlayers().stream().forEach(this::checkPlayer), 0, 20);

    }

    @Override
    public void stopInvocation() {

    }

    public void handleLogin(Player p) {
        Inventory muleInv = MountUtils.inventories.get(p.getUniqueId());
        Storage storage = BankMechanics.getInstance().getStorage(p.getUniqueId());
        AsyncUtils.pool.submit(() -> {
            checkForSuspiciousDupedItems(p, new HashSet<>(Arrays.asList(p.getInventory(), storage.inv, storage.collection_bin, muleInv)));
        });
    }

    //TODO: Have a look at this
    public void checkForDupedItems(Player player) {
        if (Rank.isGM(player) || player.getGameMode() != GameMode.SURVIVAL) return;
        CopyOnWriteArrayList<ItemStack> registeredItems = new CopyOnWriteArrayList<>();
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null || is.getType() == Material.AIR) continue;
            if (!player.isOnline()) return;
            if (!isRegistered(is)) continue;
            registeredItems.add(is);
        }
        if (registeredItems.isEmpty()) return;
        String toCheck;
        int listIndex = -1;
        int cloneIndex = -1;
        for (ItemStack is : registeredItems) {
            listIndex++;
            toCheck = getUniqueEpochIdentifier(is);
            for (ItemStack itemStack : registeredItems) {
                cloneIndex++;
                if (cloneIndex == listIndex) {
                    continue;
                }
                if (toCheck.equals(getUniqueEpochIdentifier(itemStack))) {
                    player.getInventory().remove(is);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.getInventory().addItem(is), 1L);
                    System.out.println("Duplication : " + player.getName() + " removed item(s).");
                    registeredItems.remove(cloneIndex);
                }
            }
            cloneIndex = -1;
        }
    }

    public static void checkForDuplicatedEquipment(Player p, final Set<Inventory> INVENTORIES_TO_CHECK) {
        if (Rank.isGM(p)) return;

        Map<ItemStack, String> gearUids = new HashMap<>();

        for (Inventory inv : INVENTORIES_TO_CHECK) {
            if (inv == null) continue;

            for (ItemStack i : inv.getContents()) {
                if (CraftItemStack.asNMSCopy(i) == null) continue;
                if (RepairAPI.isItemArmorOrWeapon(i)) {
                    String uniqueEpochIdentifier = AntiCheat.getInstance().getUniqueEpochIdentifier(i);
                    if (uniqueEpochIdentifier != null)
                        gearUids.put(i, uniqueEpochIdentifier);
                }
            }
        }

        Set<String> duplicates = Utils.findDuplicates(gearUids.values());
        if (!duplicates.isEmpty()) { // caught red handed

            for (ItemStack i : gearUids.keySet()) {
                String uniqueEpochIdentifier = AntiCheat.getInstance().getUniqueEpochIdentifier(i);

                if (duplicates.contains(uniqueEpochIdentifier))
                    i.setType(Material.AIR);
            }

            banAndBroadcast(p, duplicates.size());
        }
    }

    public static void checkForSuspiciousDupedItems(Player p, final Set<Inventory> INVENTORIES_TO_CHECK) {
        if (Rank.isGM(p)) return;

        Map<String, ItemStack> gearUids = new HashMap<>();

        int orbCount = 0;
        int enchantCount = 0;
        int protectCount = 0;
        int gemCount = (int) DatabaseAPI.getInstance().getData(EnumData.GEMS, p.getUniqueId());
        for (Inventory inv : INVENTORIES_TO_CHECK) {
            if (inv == null) continue;

            for (ItemStack i : inv.getContents()) {

                if (CraftItemStack.asNMSCopy(i) == null) continue;

                if (RepairAPI.isItemArmorOrWeapon(i)) {
                    String uniqueEpochIdentifier = AntiCheat.getInstance().getUniqueEpochIdentifier(i);
                    if (uniqueEpochIdentifier != null)
                        gearUids.put(uniqueEpochIdentifier, i);

                    continue;
                }
                if (GameAPI.isOrb(i))
                    orbCount += i.getAmount();
                else if (BankMechanics.getInstance().isBankNote(i))
                    gemCount += BankMechanics.getInstance().getNoteValue(i);
                else if (ItemManager.isEnchantScroll(i))
                    enchantCount += i.getAmount();
                else if (ItemManager.isProtectScroll(i))
                    protectCount += i.getAmount();
            }
        }

        Set<String> duplicates = Utils.findDuplicates(gearUids.keySet());
        for (String s : duplicates) {
            ItemStack itemStack = gearUids.get(s);
            if (itemStack != null)
                itemStack.setType(Material.AIR);
        }

        if (orbCount > 128 || enchantCount > 128 || protectCount > 128 || gemCount > 350000) {
            banAndBroadcast(p, orbCount, enchantCount, protectCount, gemCount);
        } else if (GameAPI.getGamePlayer(p).getLevel() < 20 && orbCount > 64 || enchantCount > 64 || protectCount > 64 || gemCount > 300000) { // IP BAN
            banAndBroadcast(p, orbCount, enchantCount, protectCount, gemCount);
            /*GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED + "NOTICE: Banned player " + p.getName() + " for possession of " + orbCount + " orbs, " +
                    enchantCount + " enchantment scrolls, " + protectCount + " protect scrolls, and " + gemCount + " " +
                    "gems at level " + GameAPI.getGamePlayer(p).getLevel());*/
        } else if (orbCount > 16 || enchantCount > 16 || protectCount > 16 || gemCount > 50000) { // WARN
            GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED + "WARNING: Player " + p.getName() + " has " + orbCount + " orbs, " +
                    enchantCount + " enchantment scrolls, " + protectCount + " protect scrolls, and " + gemCount + " " +
                    "gems. He is currently on shard " + DungeonRealms.getInstance().shardid);
        }
    }

    private static void banAndBroadcast(Player p, int i) {
        PunishAPI.ban(p.getUniqueId(), p.getName(), -1, "[DR ANTICHEAT] Automatic detection of duplicated items. Please appeal if you feel this ban was erroneous.", null);
        GameAPI.sendNetworkMessage("Broadcast", "");
        GameAPI.sendNetworkMessage("Broadcast", ChatColor.RED.toString() + ChatColor.BOLD + "[DR ANTICHEAT] " + ChatColor.RED + ChatColor.UNDERLINE +
                "PERMANENTLY IP BANNED" + ChatColor.RED + " player " + p.getName() + " for possession of DUPLICATED EQUIPMENT. Amount: " + i);
        //todo: add system for broadcasting SHOW of duped items
        GameAPI.sendNetworkMessage("Broadcast", "");
        GameAPI.sendNetworkMessage("BroadcastSound", Sound.ENTITY_ENDERDRAGON_GROWL.toString());
    }

    private static void banAndBroadcast(Player p, int orbCount, int enchantCount, int protectCount, int gemCount) {
        PunishAPI.ban(p.getUniqueId(), p.getName(), -1, "[DR ANTICHEAT] Automatic detection of duplicated items. Please appeal if you feel this ban was erroneous.", null);
        GameAPI.sendNetworkMessage("Broadcast", "");
        GameAPI.sendNetworkMessage("Broadcast", ChatColor.RED.toString() + ChatColor.BOLD + "[DR ANTICHEAT] " + ChatColor.RED + ChatColor.UNDERLINE +
                "PERMANENTLY IP BANNED" + ChatColor.RED + " player " + p.getName() + " for possession of " + orbCount + " orbs, " + enchantCount +
                " enchantment scrolls, " + protectCount + " protect scrolls, and " + gemCount + " gems on shard " + ChatColor.UNDERLINE + DungeonRealms.getInstance().shardid);
        GameAPI.sendNetworkMessage("Broadcast", "");
        GameAPI.sendNetworkMessage("BroadcastSound", Sound.ENTITY_ENDERDRAGON_GROWL.toString());
    }

    /*
        public void checkForDupedItems(InventoryClickEvent event) {
        ItemStack checkItem = event.getCurrentItem();
        if (checkItem == null) return;
        if (!isRegistered(checkItem)) return;
        Player player = (Player) event.getWhoClicked();
        if (Rank.isGM(player) || player.getGameMode() != GameMode.SURVIVAL) return;
        final String checkAgainst = getUniqueEpochIdentifier(checkItem);
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null || is.getType() == Material.AIR || is.getType() == Material.SKULL_ITEM) continue;
            if (!isRegistered(is)) continue;
            if (checkAgainst.equals(getUniqueEpochIdentifier(is))) {
                player.getInventory().remove(is);
            }
        }
    }
     */

    /**
     * Will be placed inside an eventlistener to make sure
     * the player isn't duplicating.
     *
     * @param event
     * @return
     * @since 1.0
     */
    public boolean watchForDupes(InventoryClickEvent event) {
        ItemStack checkItem = event.getCurrentItem();
        if (checkItem == null) return false;
        if (!isRegistered(checkItem)) return false;
        String check = getUniqueEpochIdentifier(checkItem);
        for (ItemStack item : event.getInventory().getContents()) {
            if (item == null || item.getType() == null || item.getType().equals(Material.AIR)) continue;
            if (check.equals(getUniqueEpochIdentifier(item))) {
                event.getWhoClicked().getInventory().remove(checkItem);
                return true;
            }
        }
        checkPlayer(((Player) event.getWhoClicked()));
        return false;
    }

    public void checkPlayer(Player player) {

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().equals(Material.AIR)) {
                if (item.getAmount() > 1) {
                    if (isRegistered(item)) {
                        player.getInventory().remove(item);
                        Utils.log.warning("[ANTI-CHEAT] [DUPE] Player: " + player.getName());
                        //player.sendMessage(ChatColor.RED + "Duplication detected in your inventory! Action has been logged and most certainly prevented you from any future opportunities.");
                        //Bukkit.broadcastMessage(ChatColor.RED + "Detected Duplicated Items in: " + ChatColor.AQUA + player.getName() + "'s" + ChatColor.RED + " inventory. Duplicated Items Removed.");
                    }
                }
            }
        }


        if (player.getItemOnCursor() != null && !player.getItemOnCursor().getType().equals(Material.AIR)) {
            if (player.getItemOnCursor().getAmount() > 1) {
                if (isRegistered(player.getItemOnCursor())) {
                    player.setItemOnCursor(new ItemStack(Material.AIR));
                }
            }
        }
    }

    /**
     * Returns the actual Epoch Unix String Identifier
     *
     * @param item
     * @return
     * @since 1.0
     */
    public String getUniqueEpochIdentifier(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        if (nmsStack == null) return null;
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null || !tag.hasKey("u")) return null;
        return tag.getString("u");
    }

    /**
     * Check to see if item contains 'u' field.
     *
     * @param item
     * @return
     * @since 1.0
     */
    public boolean isRegistered(ItemStack item) {
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        return !(nmsStack == null || nmsStack.getTag() == null) && nmsStack.getTag().hasKey("u");
    }

    /**
     * Adds a (u) to the item. (u) -> UNIQUE IDENTIFIER
     *
     * @param item
     * @return
     * @since 1.0
     */
    public ItemStack applyAntiDupe(ItemStack item) {
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null || tag.hasKey("u")) return item;
        tag.set("u", new NBTTagString(System.currentTimeMillis() + item.getType().toString() + item.getType().getMaxStackSize() + item.getType().getMaxDurability() + item.getDurability() + new Random().nextInt(999) + "R"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public ItemStack applyNewUID(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("u", System.currentTimeMillis() + item.getType().toString() + item.getType().getMaxStackSize() + item.getType().getMaxDurability() + item.getDurability() + new Random().nextInt(999) + "R");
        return nbtItem.getItem();
    }

    /**
     * Checks if an item's (u) is equal to any other items' (u) in a player's bank, storage, and inventory.
     *
     * @param p
     * @param item
     * @return False if the item isn't registered or has no duplicate (u). True otherwise.
     * @since 2.0
     */
    public boolean checkIfUIDPresentPlayer(ItemStack item, Player p) {
        if (!isRegistered(item)) return false;

        boolean duplicateFound = false;
        String u = getUniqueEpochIdentifier(item);

        // INVENTORY CHECK
        Inventory inv = p.getInventory();
        for (ItemStack i : inv.getContents()) {
            if (!isRegistered(i) || i.getType() != item.getType()) continue;
            if (getUniqueEpochIdentifier(i).equals(u)) return true;
        }

        // BANK CHECK
//        Inventory bank = BankMechanics.getInstance().;

        // STORAGE CHECK
        Inventory storage = BankMechanics.getInstance().getStorage(p.getUniqueId()).inv;
        for (ItemStack i : storage.getContents()) {
            if (!isRegistered(i) || i.getType() != item.getType()) continue;
            if (getUniqueEpochIdentifier(i).equals(u)) return true;
        }

        // COLLECTION BIN
        Inventory collectionBin = BankMechanics.getInstance().getStorage(p.getUniqueId()).collection_bin;
        for (ItemStack i : collectionBin.getContents()) {
            if (!isRegistered(i) || i.getType() != item.getType()) continue;
            if (getUniqueEpochIdentifier(i).equals(u)) return true;
        }

        // todo: implement a realm check
        return duplicateFound;
    }

    public boolean checkIfDupedDatabase(ItemStack item) {
        return uids.contains(getUniqueEpochIdentifier(item));
    }

    public void mergeUniqueIdentifiers(ItemStack mergingItem, ItemStack itemToMerge) {
        NBTItem mergingItemNBT = new NBTItem(mergingItem);
        NBTItem itemToMergeNBT = new NBTItem(itemToMerge);
        uids.remove(itemToMergeNBT.getString("u"));
        itemToMergeNBT.setString("u", mergingItemNBT.getString("u"));
    }

    public void dupedItemFound(Player p, ItemStack i) {
        List<String> hoveredChat = new ArrayList<>();
        ItemMeta meta = i.getItemMeta();
        hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : i.getType().name()));
        if (meta.hasLore())
            hoveredChat.addAll(meta.getLore());
        final JSONMessage normal = new JSONMessage("", ChatColor.WHITE);
        normal.addText(ChatColor.RED + "[ANTICHEAT] [DUPE] Duped item ");
        normal.addHoverText(hoveredChat, ChatColor.WHITE.toString() + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
        normal.addText(ChatColor.RED + " detected and removed from player " + p.getName() + "! This action has been " +
                "logged.");
        Bukkit.getOnlinePlayers().forEach(player -> normal.sendToPlayer(player));
        Utils.log.info("[ANTI-CHEAT] [DUPE] Player: " + p.getName());
    }
}
