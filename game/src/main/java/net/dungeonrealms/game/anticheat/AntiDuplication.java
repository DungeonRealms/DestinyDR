package net.dungeonrealms.game.anticheat;

import com.google.common.collect.HashMultimap;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.util.AsyncUtils;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.functional.ItemScrap;
import net.dungeonrealms.game.item.items.functional.ItemTeleportBook;
import net.dungeonrealms.game.item.items.functional.PotionItem;
import net.dungeonrealms.game.mastery.NBTItem;
import net.dungeonrealms.game.mastery.Utils;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Nick on 10/1/2015.
 */

public class AntiDuplication implements GenericMechanic, Listener {

	@Getter private static AntiDuplication instance = new AntiDuplication();
    public static Set<UUID> EXCLUSIONS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final static long CHECK_TICK_FREQUENCY = 10L;

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }


    @Override
    public void startInitialization() {
    	Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(),
                () -> Bukkit.getOnlinePlayers().stream().forEach(p -> checkForSuspiciousDupedItems(p, new HashSet<>(Collections.singletonList(p.getInventory())))), 0, CHECK_TICK_FREQUENCY);
    }

    @Override
    public void stopInvocation() {

    }

    public void handleLogin(Player p) {
        Inventory muleInv = MountUtils.getInventory(p);
        Storage storage = BankMechanics.getStorage(p.getUniqueId());
        AsyncUtils.pool.submit(() -> checkForSuspiciousDupedItems(p, new HashSet<>(Arrays.asList(p.getInventory(), storage.inv, storage.collection_bin, muleInv))));
    }


    /**
     * Checks and removes duplicated items
     * when detected.
     *
     * @author APOLLOSOFTWARE
     */
    private static void checkForDuplications(Player p, HashMultimap<Inventory, Tuple<ItemStack, String>> map) {
        Set<String> duplicates = Utils.findDuplicates(map.values().stream().map(Tuple::b).collect(Collectors.toList()));
        Map<String, Integer> itemDesc = new HashMap<>();
        if (!duplicates.isEmpty()) { // caught red handed
            for (Map.Entry<Inventory, Tuple<ItemStack, String>> e : map.entries()) {
                String uniqueEpochIdentifier = e.getValue().b();
                if (duplicates.contains(uniqueEpochIdentifier)) {
                    String name = "";
                    ItemStack item = e.getValue().a();
                    ItemMeta meta = item.getItemMeta();
                    
                    if (meta.hasDisplayName())
                    	name += meta.getDisplayName();
                    else {
                        Material material = e.getValue().a().getType();
                        name += material.toString().replace("_", " ");
                    }
                    
                    if (itemDesc.containsKey(name))
                    	itemDesc.put(name, itemDesc.get(name) + 1);
                    else
                    	itemDesc.put(name, 1);
                    
                    // GIVE THEM AN ORIGINAL //
                    if (ItemGear.isCustomTool(e.getValue().a())) {
                        remove(e.getKey(), e.getValue().b());
                        // THIS WILL REMOVED THE DUPLICATE ITEMS //
                        if (traceCount(e.getKey(), e.getValue().b()) == 0)
                            e.getKey().addItem(e.getValue().a());
                    } else if (traceCount(e.getKey(), e.getValue().b()) == 0) {
                        e.getValue().a().setAmount(1);
                        e.getKey().addItem(e.getValue().a());
                    } else {
                        itemDesc.put(name, itemDesc.get(name) + (e.getValue().a().getAmount() - 2));
                        remove(e.getKey(), e.getValue().b());
                    }
                }
            }
            StringBuilder builder = new StringBuilder();
            int i = 0;
            for (Map.Entry<String, Integer> e : itemDesc.entrySet()) {
                int amount = e.getValue() - 1;
                String name = e.getKey();

                builder.append(i > 0 ? ", " : "").append(amount).append(" count(s) of ").append(ChatColor.AQUA).append(name).append(ChatColor.WHITE);
                i++;
            }
            p.sendMessage(ChatColor.GOLD + "Found a dupe? Don't " + ChatColor.RED + "abuse" + ChatColor.GOLD + " it! Report it and you may be eligible for " + ChatColor.YELLOW + ChatColor.BOLD + "SUB++" + ChatColor.GOLD + "!");
            GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED.toString() + "[ANTI CHEAT] " +
                    ChatColor.WHITE + "Player " + p.getName() + " has attempted to duplicate items. Removed: " + builder.toString() + " on shard {SERVER}.");
            
            DebugUtil.debugReport(p);
        }
    }

    /**
     * Checks for suspiciously duped items
     *
     * @param player      Player target
     * @param inventories Inventories to check
     * @author APOLLOSOFTWARE
     * @author EtherealTemplar
     *
     * Oh nice job lads, yes amazing, this is the most retarded thing I've ever seen. congrats.
     */
    public static void checkForSuspiciousDupedItems(Player player, final Set<Inventory> inventories) {
        if (Rank.isTrialGM(player)) return;
        if (EXCLUSIONS.contains(player.getUniqueId())) return;

        HashMultimap<Inventory, Tuple<ItemStack, String>> gearUids = HashMultimap.create();

        for (Inventory inv : inventories) {
            if (inv == null) continue;

            for (ItemStack i : inv.getContents()) {
                if (i == null || CraftItemStack.asNMSCopy(i) == null) continue;

                if (i.getAmount() <= 0) continue;
                if (ItemScrap.isScrap(i) || PotionItem.isPotion(i) || ItemTeleportBook.isTeleportBook(i)) continue;

                String uniqueEpochIdentifier = getUniqueEpochIdentifier(i);
                if (uniqueEpochIdentifier != null) for (int ii = 0; ii < i.getAmount(); ii++)
                    gearUids.put(inv, new Tuple<>(i, uniqueEpochIdentifier));
            }
        }

        checkForDuplications(player, gearUids);
    }

    private static void remove(Inventory inventory, String uniqueEpochIdentifier) {
        for (ItemStack i : inventory) {
            if (i == null || CraftItemStack.asNMSCopy(i) == null) continue;
            if (i.getAmount() <= 0) continue;
            if (isRegistered(i))
                if (getUniqueEpochIdentifier(i).equals(uniqueEpochIdentifier))
                    inventory.remove(i);
        }
    }


    private static int traceCount(Inventory inventory, String uniqueEpochIdentifier) {
        int amount = 0;
        for (ItemStack i : inventory) {
            if (i == null || CraftItemStack.asNMSCopy(i) == null) continue;
            if (i.getAmount() <= 0) continue;
            if (isRegistered(i))
                if (getUniqueEpochIdentifier(i).equals(uniqueEpochIdentifier))
                    amount += i.getAmount();
        }
        return amount;
    }

    /**
     * Returns the actual Epoch Unix String Identifier
     *
     * @param item
     * @return
     * @since 1.0
     */
    public static String getUniqueEpochIdentifier(ItemStack item) {
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
        tag.set("u", new NBTTagString(createEpoch(item)));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public ItemStack applyNewUID(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("u", createEpoch(item));
        return nbtItem.getItem();
    }

    public static String createEpoch(ItemStack item) {
    	return System.currentTimeMillis() + item.getType().toString() + item.getType().getMaxStackSize() + item.getType().getMaxDurability() + item.getDurability() + new Random().nextInt(99999) + "R";
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent evt){
    	if(evt.getPlayer() instanceof Player)
    		checkForSuspiciousDupedItems((Player)evt.getPlayer(), new HashSet<>(Arrays.asList(evt.getPlayer().getInventory(), evt.getInventory())) );
    }

}
