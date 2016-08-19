package net.dungeonrealms.game.anticheat;

import com.google.common.collect.HashMultimap;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.common.game.util.AsyncUtils;
import net.dungeonrealms.common.game.util.CooldownProvider;
import net.dungeonrealms.game.mastery.NBTItem;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Nick on 10/1/2015.
 */

public class AntiDuplication implements GenericMechanic {

    static AntiDuplication instance = null;

    public static Set<UUID> EXCLUSIONS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static CooldownProvider WARNING_SUPPRESSOR = new CooldownProvider();

    private final static long CHECK_TICK_FREQUENCY = 60L;

    public static AntiDuplication getInstance() {
        if (instance == null) {
            instance = new AntiDuplication();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }


    @Override
    public void startInitialization() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(),
                () -> Bukkit.getOnlinePlayers().stream().forEach(p -> checkForSuspiciousDupedItems(p, new HashSet<>(Collections.singletonList(p.getInventory())))), 0, CHECK_TICK_FREQUENCY);
    }

    @Override
    public void stopInvocation() {

    }

    public void handleLogin(Player p) {
        Inventory muleInv = MountUtils.inventories.get(p.getUniqueId());
        Storage storage = BankMechanics.getInstance().getStorage(p.getUniqueId());
        AsyncUtils.pool.submit(() -> checkForSuspiciousDupedItems(p, new HashSet<>(Arrays.asList(p.getInventory(), storage.inv, storage.collection_bin, muleInv))));
    }

    private static void checkForDuplications(Player p, HashMultimap<Inventory, Tuple<ItemStack, String>> map) {
        Set<String> duplicates = Utils.findDuplicates(map.values().stream().map(Tuple::b).collect(Collectors.toList()));
        if (!duplicates.isEmpty()) { // caught red handed

            for (Map.Entry<Inventory, Tuple<ItemStack, String>> e : map.entries()) {
                String uniqueEpochIdentifier = e.getValue().b();

                if (duplicates.contains(uniqueEpochIdentifier))
                    Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> e.getKey().remove(e.getValue().a()));
            }

            GameAPI.sendNetworkMessage("GMMessage", "");
            GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED.toString() + ChatColor.BOLD + "[DR ANTICHEAT] " + ChatColor.RED + ChatColor.UNDERLINE +
                    ChatColor.RED + "Player " + p.getName() + " has attempted to duplicate items. Amount: " + duplicates.size());
            GameAPI.sendNetworkMessage("GMMessage", "");

            //banAndBroadcast(p, duplicates.size());
        }
    }

    public static void checkForSuspiciousDupedItems(Player p, final Set<Inventory> INVENTORIES_TO_CHECK) {
        if (Rank.isGM(p)) return;
        if (EXCLUSIONS.contains(p.getUniqueId())) return;

        int orbCount = 0;
        int enchantCount = 0;
        int protectCount = 0;
        int gemCount = (int) DatabaseAPI.getInstance().getData(EnumData.GEMS, p.getUniqueId());

        HashMultimap<Inventory, Tuple<ItemStack, String>> gearUids = HashMultimap.create();

        for (Inventory inv : INVENTORIES_TO_CHECK) {
            if (inv == null) continue;

            for (ItemStack i : inv.getContents()) {
                if (CraftItemStack.asNMSCopy(i) == null) continue;

                if (i.getAmount() <= 0) continue;

                if (ItemManager.isScrap(i)) continue;

                String uniqueEpochIdentifier = AntiDuplication.getInstance().getUniqueEpochIdentifier(i);
                if (uniqueEpochIdentifier != null)
                    for (int ii = 0; ii < i.getAmount(); ii++)
                        gearUids.put(inv, new Tuple<>(i, uniqueEpochIdentifier));

                if (GameAPI.isOrb(i))
                    orbCount += i.getAmount();
                else if (ItemManager.isEnchantScroll(i))
                    enchantCount += i.getAmount();
                else if (ItemManager.isProtectScroll(i))
                    protectCount += i.getAmount();
                else if (BankMechanics.getInstance().isBankNote(i))
                    gemCount += (BankMechanics.getInstance().getNoteValue(i) * i.getAmount());
            }
        }

        checkForDuplications(p, gearUids);

        if (orbCount > 128 || enchantCount > 128 || protectCount > 128 || gemCount > 350000) {
            banAndBroadcast(p, orbCount, enchantCount, protectCount, gemCount);
        } else if (GameAPI.getGamePlayer(p).getLevel() < 20 && orbCount > 64 || enchantCount > 64 || protectCount > 64 || gemCount > 300000) { // IP BAN
            banAndBroadcast(p, orbCount, enchantCount, protectCount, gemCount);
        } else if (orbCount > 64 || enchantCount > 64 || protectCount > 64 || gemCount > 150000) { // WARN
            if (WARNING_SUPPRESSOR.isCooldown(p.getUniqueId())) return;

            WARNING_SUPPRESSOR.cache(p, 120000L);

            GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED + "WARNING: Player " + p.getName() + " has " + orbCount + " orbs, " +
                    enchantCount + " enchantment scrolls, " + protectCount + " protect scrolls, and " + gemCount + " " +
                    "gems. He is currently on shard " + DungeonRealms.getInstance().shardid);
        }
    }


    private static void banAndBroadcast(Player p, int i) {
        // @note: Please don't announce things to the public, everything should be a GM alert or silently logged.
        PunishAPI.ban(p.getUniqueId(), p.getName(), "DR ANTICHEAT", -1, "[DR ANTICHEAT] Automatic detection of duplicated items. Please appeal if you feel this ban was erroneous.", null);

        GameAPI.sendNetworkMessage("GMMessage", "");
        GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED.toString() + ChatColor.BOLD + "[DR ANTICHEAT] " + ChatColor.RED + ChatColor.UNDERLINE +
                "Banning" + ChatColor.RED + " player " + p.getName() + " for possession of DUPLICATED ITEMS. Amount: " + i);
        //todo: add system for broadcasting SHOW of duped items
        GameAPI.sendNetworkMessage("GMMessage", "");
    }

    private static void banAndBroadcast(Player p, int orbCount, int enchantCount, int protectCount, int gemCount) {
        PunishAPI.ban(p.getUniqueId(), p.getName(), "DR ANTICHEAT", -1, "[DR ANTICHEAT] Automatic detection of duplicated items. Please appeal if you feel this ban was erroneous.", null);

        GameAPI.sendNetworkMessage("GMMessage", "");
        GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED.toString() + ChatColor.BOLD + "[DR ANTICHEAT] " + ChatColor.RED + ChatColor.UNDERLINE +
                "Banned" + ChatColor.RED + " player " + p.getName() + " for possession of " + orbCount + " orbs, " + enchantCount +
                " enchantment scrolls, " + protectCount + " protect scrolls, and " + gemCount + " gems on shard " + ChatColor.UNDERLINE + DungeonRealms.getInstance().shardid);
        GameAPI.sendNetworkMessage("GMMessage", "");
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
    public static boolean isRegistered(ItemStack item) {
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


}
