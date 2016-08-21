package net.dungeonrealms.tool;

import com.mongodb.Block;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseInstance;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.entity.type.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Class written by Alan Lu on 8/2/2016
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
        DatabaseInstance.getInstance().startInitialization(true);

        DatabaseInstance.playerData.find().forEach(new Block<Document>() {
            @Override
            public void apply(Document doc) {
                totalQueries[0]++;
                int dupedItemsFound = 0;
                long currTime = System.currentTimeMillis();
                final Document infoDoc = doc.get("info", Document.class);
                if (infoDoc == null) return;

                final String rank = doc.get("rank", Document.class).get("rank", String.class);
                if (rank.equalsIgnoreCase("GM") || rank.equalsIgnoreCase("DEV") || rank.equalsIgnoreCase("HEADGM"))
                    return;

                String name = infoDoc.get("username", String.class);
                UUID uuid = UUID.fromString(infoDoc.get("uuid", String.class));

                if (name != null) System.out.println("Checking player " + name);

                playerGems = infoDoc.get("gems", Integer.class);

                Document invDoc = doc.get("inventory", Document.class);

                // PLAYER INVENTORY
                String playerInv = invDoc.get("player", String.class);
                if (playerInv != null && playerInv.length() > 0 && !playerInv.equalsIgnoreCase("null")) {
                    dupedItemsFound += addGearUIDSAndCheckDupes(ItemSerialization.fromString(playerInv, 36), EnumData
                            .INVENTORY, uuid, name);
                }
                // ARMOR
                List<String> playerArmor = (ArrayList<String>) invDoc.get("armor");
                ItemStack[] armorAndOffHand = new ItemStack[5];
                for (int i = 0; i < playerArmor.size(); i++) {
                    final String armor = playerArmor.get(i);
                    if (armor.equals("null") || armor.equals("")) {
                        armorAndOffHand[i] = new ItemStack(Material.AIR);
                    } else {
                        armorAndOffHand[i] = ItemSerialization.itemStackFromBase64(armor);
                    }
                }
                dupedItemsFound += addGearUIDSAndCheckDupes(armorAndOffHand, uuid, name);

                // STORAGE
                String storage = invDoc.get("storage", String.class);
                if (storage != null && storage.length() > 0 && !storage.equalsIgnoreCase("null")) {
                    dupedItemsFound += addGearUIDSAndCheckDupes(ItemSerialization.fromString(storage), EnumData
                            .INVENTORY_STORAGE, uuid, name);
                }

                // MULE
                int muleLevel = infoDoc.get("muleLevel", Integer.class);
                String invString = invDoc.get("mule", String.class);
                if (muleLevel > 3) {
                    muleLevel = 3;
                }
                MuleTier tier = MuleTier.getByTier(muleLevel);
                Inventory muleInv = null;
                if (tier != null && !invString.equalsIgnoreCase("") && !invString.equalsIgnoreCase("empty") && invString.length() > 4) {
                    muleInv = ItemSerialization.fromString(invString, tier.getSize());
                }
                if (!invString.equalsIgnoreCase("") && !invString.equalsIgnoreCase("empty") && invString.length() > 4 && muleInv != null)
                    dupedItemsFound += addGearUIDSAndCheckDupes(muleInv, EnumData.INVENTORY_MULE, uuid, name);

                // COLLECTION BIN
                String bin = invDoc.get("collection_bin", String.class);
                if (bin != null && bin.length() > 0 && !bin.equalsIgnoreCase("null")) {
                    dupedItemsFound += addGearUIDSAndCheckDupes(ItemSerialization.fromString(bin), EnumData
                            .INVENTORY_COLLECTION_BIN, uuid, name);
                }

                System.out.println("Single player took " + String.valueOf(System.currentTimeMillis() - currTime) +
                        "ms");

                if (dupedItemsFound > 0) {
                    System.out.println(dupedItemsFound + " duped items found and removed for player " + name);
                    totalQueriesWithDupes[0]++;
                }

                totalDupedItemsFound[0] += dupedItemsFound;
                if (playerGems > 50000) playersWithHighGems.put(name, playerGems);
                if (playerOrbs > 32) playersWithHighOrbs.put(name, playerOrbs);
                playerGems = 0;
                playerOrbs = 0;
            }
        });
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

    private static int addGearUIDSAndCheckDupes(Inventory inv, EnumData data, UUID uuid, String name) {
        int dupedItemsFound = 0;
        for (ItemStack i : inv.getContents()) {
            if (i == null || i.getType() == Material.AIR) continue;
            if (GameAPI.isOrb(i))
                playerOrbs += i.getAmount();
            else if (BankMechanics.getInstance().isBankNote(i))
                playerGems += BankMechanics.getInstance().getNoteValue(i) * i.getAmount();
            if (!RepairAPI.isItemArmorOrWeapon(i)) continue;
            final String uniqueEpochIdentifier = AntiDuplication.getInstance().getUniqueEpochIdentifier(i);
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
            if (!RepairAPI.isItemArmorOrWeapon(items[i])) continue;
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
