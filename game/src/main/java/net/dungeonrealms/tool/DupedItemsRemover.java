package net.dungeonrealms.tool;

import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.functional.ItemMoney;
import net.dungeonrealms.game.item.items.functional.ItemOrb;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Class written by Alan Lu on 8/2/2016
 * 
 * This class is not used. Maybe we can write a better system later.
 */

public class DupedItemsRemover implements GenericMechanic {
    private static Map<String, Set<String>> uids = new HashMap<>(20000);
    private static Map<ItemStack, Set<String>> dupedItems = new HashMap<>();
    private static int playerGems = 0;
    private static int playerOrbs = 0;
    private static Map<String, Integer> playersWithHighGems = new HashMap<>();
    private static Map<String, Integer> playersWithHighOrbs = new HashMap<>();

    public void startInitialization() {
        final int[] totalDupedItemsFound = {0};
        final int[] totalQueries = {0};
        final int[] totalQueriesWithDupes = {0};
        long currTotalTime = System.currentTimeMillis();
        
        String formattedTime = DurationFormatUtils.formatDurationWords(System.currentTimeMillis() -
                currTotalTime, true, true);
        System.out.println("Checked " + totalQueries[0] + " players in " + formattedTime + " and found " +
                totalDupedItemsFound[0] + " total duped items on " + totalQueriesWithDupes[0] + " different players.");
        System.out.println("Duped item summary: ");
        for (Map.Entry<ItemStack, Set<String>> entry : dupedItems.entrySet()) {
            String players = "";
            for (String player : entry.getValue()) {
                players = players + player + ", ";
            }
            players = players.substring(0, players.length() - 2);
            System.out.println(entry.getKey().getItemMeta().getDisplayName() + " found on player(s) " + players);
        }
        System.out.println("Players with more than 50k gems: ");
        for (Map.Entry<String, Integer> entry : playersWithHighGems.entrySet()) {
            System.out.println("Player: " + entry.getKey() + " Gems: " + entry.getValue());
        }
        System.out.println("Players with more than 32 orbs: ");
        for (Map.Entry<String, Integer> entry : playersWithHighOrbs.entrySet()) {
            System.out.println("Player: " + entry.getKey() + " Orbs: " + entry.getValue());
        }
    }

    private static int addGearUIDSAndCheckDupes(Inventory inv, String name) {
        int dupedItemsFound = 0;
        for (ItemStack i : inv.getContents()) {
            if (i == null || i.getType() == Material.AIR) continue;
            if (ItemOrb.isOrb(i))
                playerOrbs += i.getAmount();
            else if (ItemMoney.isMoney(i))
                playerGems += ((ItemMoney)PersistentItem.constructItem(i)).getGemValue();
            if (!ItemGear.isCustomTool(i)) continue;
            final String uniqueEpochIdentifier = AntiDuplication.getUniqueEpochIdentifier(i);
            if (uniqueEpochIdentifier == null) continue;

            if (uids.containsKey(uniqueEpochIdentifier)) {
//                inv.remove(i); // duped item
                Set<String> strings = uids.get(uniqueEpochIdentifier);
                System.out.println("Detected duped item and removed " + i.getItemMeta().getDisplayName() + " from their " + inv.getName());
                final String[] sb = {""};
                strings.forEach(player -> sb[0] = sb[0] + player + ", ");
                sb[0] = sb[0].substring(0, sb[0].length() - 2);
                System.out.println("Other players who have this item: " + sb[0]);
                strings.add(name);
                uids.put(uniqueEpochIdentifier, strings);
                dupedItemsFound++;
                dupedItems.put(i, strings);
            }
            else {
                uids.put(uniqueEpochIdentifier, new HashSet<>(Arrays.asList(name)));
            }
        }
        /*if (dupedItemsFound > 0) { uncomment to automatically remove found dupes
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, data, inv, true);
        }*/
        return dupedItemsFound;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.ARCHBISHOPS;
    }

    @Override
    public void stopInvocation() {

    }

    private static int addGearUIDSAndCheckDupes(ItemStack[] items, UUID uuid, String name) {
        int dupedItemsFound = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null || items[i].getType() == Material.AIR) continue;
            if (!ItemGear.isCustomTool(items[i])) continue;
            final String uniqueEpochIdentifier = AntiDuplication.getInstance().getUniqueEpochIdentifier(items[i]);
            if (uniqueEpochIdentifier == null) continue;

            if (uids.containsKey(uniqueEpochIdentifier)) {
                Set<String> strings = uids.get(uniqueEpochIdentifier);
                System.out.println("Detected duped item and removed " + items[i].getItemMeta().getDisplayName() + " from their armor.");
                final String[] sb = {""};
                strings.forEach(player -> sb[0] = sb[0] + player + ", ");
                sb[0] = sb[0].substring(0, sb[0].length() - 2);
                System.out.println("Other players who have this item: " + sb[0]);
                strings.add(name);
//                items[i] = null;
                uids.put(uniqueEpochIdentifier, strings);
                dupedItemsFound++;
                dupedItems.put(items[i], strings);
            }
            else {
                uids.put(uniqueEpochIdentifier, new HashSet<>(Arrays.asList(name)));
            }
        }
        /*if (dupedItemsFound > 0) { uncomment to automatically remove fou dupes
            ArrayList<String> armor = new ArrayList<>();
            for (ItemStack i : items) {
                if (i == null || i.getType() == Material.AIR) {
                    armor.add("");
                } else {
                    armor.add(ItemSerialization.itemStackToBase64(i));
                }
            }
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, armor, true);
        }*/
        return dupedItemsFound;
    }

}
