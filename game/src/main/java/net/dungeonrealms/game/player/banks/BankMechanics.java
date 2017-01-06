package net.dungeonrealms.game.player.banks;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Created by Chase on Sep 18, 2015
 */
public class BankMechanics implements GenericMechanic {

    public static ItemStack gem;
    public static ItemStack banknote;
    public static HashMap<UUID, Storage> storage = new HashMap<>();
    public static HashMap<String, ItemStack> shopPricing = new HashMap<>();

    private static BankMechanics instance = null;

    public static BankMechanics getInstance() {
        if (instance == null) {
            instance = new BankMechanics();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
    public void startInitialization() {
        loadCurrency();


        /**
         * Random Place for this to start.
         */

        Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
            for (GamePlayer gp : GameAPI.GAMEPLAYERS.values()) {
                if (gp == null || gp.getPlayer() == null)
                    continue;
                if (gp.getStats().freePoints > 0) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        final JSONMessage normal = new JSONMessage(ChatColor.GREEN + "*" + ChatColor.GRAY + "You have available " + ChatColor.GREEN + "stat points. " + ChatColor.GRAY +
                                "To allocate click ", ChatColor.WHITE);
                        normal.addRunCommand(ChatColor.GREEN.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE!", ChatColor.GREEN, "/stats");
                        normal.addText(ChatColor.GREEN + "*");
                        normal.sendToPlayer(gp.getPlayer());
                    });
                }
            }
        }, (20 * 60) * 10);
        //Free Points every 10 minutes.
    }

    @Override
    public void stopInvocation() {

    }

    public int getTotalGemsInInventory(Player p) {
        Inventory i = p.getInventory();


        int found = 0;

        HashMap<Integer, ? extends ItemStack> invItems = i.all(Material.EMERALD);
        for (Map.Entry<Integer, ? extends ItemStack> entry : invItems.entrySet()) {
            ItemStack item = entry.getValue();
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
            if (!nms.hasTag() || !nms.getTag().hasKey("type") || !nms.getTag().getString("type").equalsIgnoreCase("money"))
                continue;
            int stackAmount = item.getAmount();
            found += stackAmount;
        }

        HashMap<Integer, ? extends ItemStack> bank_notes = i.all(Material.PAPER);
        for (Map.Entry<Integer, ? extends ItemStack> entry : bank_notes.entrySet()) {
            ItemStack item = entry.getValue();
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
            if (!nms.hasTag() || !nms.getTag().hasKey("type") || !nms.getTag().getString("type").equalsIgnoreCase("money"))
                continue;
            int bank_note_val = getNoteValue(item);
            found += bank_note_val;
        }

        return found;
    }

    public boolean takeGemsFromInventory(int amount, Player p) {
        Inventory i = p.getInventory();
        int paid_off = 0;

        if (amount <= 0) {
            return true; // It's free.
        }
        HashMap<Integer, ? extends ItemStack> invItems = i.all(Material.EMERALD);
        for (Map.Entry<Integer, ? extends ItemStack> entry : invItems.entrySet()) {
            int index = entry.getKey();
            ItemStack item = entry.getValue();
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
            if (!nms.hasTag() || !nms.getTag().hasKey("type") || !nms.getTag().getString("type").equalsIgnoreCase("money"))
                continue;
            int stackAmount = item.getAmount();

            if ((paid_off + stackAmount) <= amount) {
                p.getInventory().setItem(index, new ItemStack(Material.AIR));
                paid_off += stackAmount;
            } else {
                int to_take = amount - paid_off;
                p.getInventory().setItem(index, createGems(stackAmount - to_take));
                paid_off += to_take;
            }
            if (paid_off >= amount) {
                GamePlayer gp = GameAPI.getGamePlayer(p);
                if (gp != null) {
                    gp.getPlayerStatistics().setGemsSpent(gp.getPlayerStatistics().getGemsSpent() + amount);
                }
                p.updateInventory();
                return true;
            }
        }

        HashMap<Integer, ? extends ItemStack> bank_notes = i.all(Material.PAPER);
        for (Map.Entry<Integer, ? extends ItemStack> entry : bank_notes.entrySet()) {
            ItemStack item = entry.getValue();
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
            if (!nms.hasTag() || !nms.getTag().hasKey("type") || !nms.getTag().getString("type").equalsIgnoreCase("money"))
                continue;
            int bank_note_val = getNoteValue(item);
            int index = entry.getKey();

            if ((paid_off + bank_note_val) <= amount) {
                p.getInventory().setItem(index, new ItemStack(Material.AIR));
                paid_off += bank_note_val;
            } else {
                int to_take = amount - paid_off;
                paid_off += to_take;
                updateMoney(p, index, (bank_note_val - to_take));
            }

            if (paid_off >= amount) {
                GamePlayer gp = GameAPI.getGamePlayer(p);
                if (gp != null) {
                    gp.getPlayerStatistics().setGemsSpent(gp.getPlayerStatistics().getGemsSpent() + amount);
                }
                p.updateInventory();
                return true;
            }

        }

        if (paid_off > 0) {
            if (paid_off < 64) {
                p.getInventory().addItem(createGems(paid_off));
            } else {
                p.getInventory().addItem(createBankNote(paid_off));
            }
        }
        return false;
    }


    public void updateMoney(Player p, int slot, int new_amount) {
        p.getInventory().setItem(slot, createBankNote(new_amount));
    }

    /**
     * @param amount
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createGems(int amount) {
        ItemStack stack = gem.clone();
        stack.setAmount(amount);
        return stack;
    }

    /**
     * Return new ItemSTack of gem pouch
     *
     * @param amount
     * @return ITemStack
     */
    public ItemStack createGemPouch(int type, int amount) {
        ItemStack stack = null;
        net.minecraft.server.v1_9_R2.ItemStack nms = null;
        switch (type) {
            case 0:
            case 1:
                stack = ItemManager.createItem(Material.INK_SACK, "Small Gem Pouch" + ChatColor.GREEN + " " + amount + "g", new String[]{ChatColor.GRAY + "A small linen pouch that holds 100g"});
                break;
            case 2:
                stack = ItemManager.createItem(Material.INK_SACK, "Medium Gem Pouch" + ChatColor.GREEN + " " + amount + "g", new String[]{ChatColor.GRAY + "A small linen pouch that holds 150g"});
                break;
            case 3:
                stack = ItemManager.createItem(Material.INK_SACK, "Large Gem Pouch" + ChatColor.GREEN + " " + amount + "g", new String[]{ChatColor.GRAY + "A small linen pouch that holds 200g"});
                break;
            case 4:
                stack = ItemManager.createItem(Material.INK_SACK, "Gigantic Gem Pouch" + ChatColor.GREEN + " " + amount + "g", new String[]{ChatColor.GRAY + "A small linen pouch that holds 300g"});
                break;
        }
        nms = CraftItemStack.asNMSCopy(stack);

        NBTTagCompound tag = nms.getTag();
        tag.setString("type", "money");
        tag.setInt("worth", amount);
        tag.setInt("tier", type);
//		nms.setTag(tag);
        return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }

    public int getPouchMax(int tier) {
        switch (tier) {
            case 0:
            case 1:
                return 100;
            case 2:
                return 150;
            case 3:
                return 200;
            case 4:
                return 300;
        }
        return 0;
    }


    /**
     * Pre loads an itemstack version of our currency
     *
     * @return
     */
    private void loadCurrency() {
        ItemStack item = new ItemStack(Material.EMERALD, 1);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        meta.setDisplayName(ChatColor.GREEN + "Gem");
        item.setItemMeta(meta);
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nms.getTag() == null ? new NBTTagCompound() : nms.getTag();
        tag.setString("type", "money");
        nms.setTag(tag);
        gem = CraftItemStack.asBukkitCopy(nms);

        ItemStack item2 = new ItemStack(Material.PAPER, 1);
        ItemMeta meta2 = item2.getItemMeta();
        List<String> lore2 = new ArrayList<>();
        lore2.add(ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "Value: " + ChatColor.WHITE.toString());
        meta2.setLore(lore2);
        meta2.setDisplayName(ChatColor.GREEN.toString() + "Gem Note");
        item2.setItemMeta(meta2);
        net.minecraft.server.v1_9_R2.ItemStack nms2 = CraftItemStack.asNMSCopy(item2);
        NBTTagCompound tag2 = nms2.getTag() == null ? new NBTTagCompound() : nms2.getTag();
        tag2.setString("type", "money");
        tag2.setInt("worth", 0);
        nms2.setTag(tag2);
        banknote = CraftItemStack.asBukkitCopy(nms2);
    }

    /**
     * Creates a new Bank Note for the set amount
     *
     * @param amount
     * @return
     */
    public static ItemStack createBankNote(int amount) {

        ItemStack stack = BankMechanics.banknote.clone();
        ItemMeta meta = stack.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Value: " + ChatColor.WHITE + amount + " Gems");
        lore.add(ChatColor.GRAY + "Exchange at any bank for GEM(s)");
        meta.setLore(lore);
        stack.setItemMeta(meta);
        net.minecraft.server.v1_9_R2.ItemStack nms1 = CraftItemStack.asNMSCopy(stack);
        nms1.getTag().setInt("worth", amount);
        return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms1));
    }

    /**
     * Return the value of itemstack note
     *
     * @param stack
     * @return integer
     */
    public int getNoteValue(ItemStack stack) {
        return CraftItemStack.asNMSCopy(stack).getTag().getInt("worth");
    }

    /**
     * Add gems to player database
     *
     * @param uuid
     * @param num
     */
    public void addGemsToPlayerBank(UUID uuid, int num) {
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$INC, EnumData.GEMS, num, true);
    }

    /**
     * Add gems to player database
     *
     * @param p
     * @param num
     */
    public void addGemsToPlayerInventory(Player p, int num) {
        ItemStack gems = gem.clone();
        gems.setAmount(num);
        p.getInventory().addItem(gems);
    }

    /**
     * @param uniqueId
     */
    public Storage getStorage(UUID uniqueId) {
        return storage.get(uniqueId);
    }

    /**
     * @param invLvl
     * @return integer
     */
    public static int getPrice(int invLvl) {
        //100, 250, 1000, 3000, 7000, 15000
        switch (invLvl) {
            case 1:
                return 50;
            case 2:
                return 125;
            case 3:
                return 500;
            case 4:
                return 1500;
            case 5:
                return 3500;
            case 6:
                return 7500;
            default:
                return 7500;
        }
    }

    /**
     * @param stack
     * @return
     */
    public boolean isBankNote(ItemStack stack) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        return stack.getType() == Material.PAPER && nms.getTag() != null && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money");
    }

    public void checkBankAchievements(UUID uuid, int bankGemAmount) {
        if (bankGemAmount >= 100) {
            Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.ACQUIRE_CURRENCY_I);
            if (bankGemAmount >= 1000) {
                Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.ACQUIRE_CURRENCY_II);
                if (bankGemAmount >= 5000) {
                    Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.ACQUIRE_CURRENCY_III);
                    if (bankGemAmount >= 10000) {
                        Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.ACQUIRE_CURRENCY_IV);
                        if (bankGemAmount >= 50000) {
                            Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.ACQUIRE_CURRENCY_V);
                            if (bankGemAmount >= 100000) {
                                Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.ACQUIRE_CURRENCY_VI);
                                if (bankGemAmount >= 500000) {
                                    Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.ACQUIRE_CURRENCY_VII);
                                    if (bankGemAmount >= 1000000) {
                                        Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.ACQUIRE_CURRENCY_VIII);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isGem(ItemStack cursor) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(cursor);
        return cursor.getType() == Material.EMERALD &&
                nms.getTag() != null && nms.getTag().hasKey("type") &&
                nms.getTag().getString("type").equalsIgnoreCase("money");
    }

    public boolean isGemPouch(ItemStack cursor) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(cursor);
        return cursor.getType() == Material.INK_SACK &&
                nms.getTag() != null && nms.getTag().hasKey("type") &&
                nms.getTag().getString("type").equalsIgnoreCase("money");

    }

    public int getPouchAmount(ItemStack currentItem) {
        return CraftItemStack.asNMSCopy(currentItem).getTag().getInt("worth");

    }

    public ItemStack makeNewPouch(ItemStack currentItem, int number) {
        int tier = CraftItemStack.asNMSCopy(currentItem).getTag().getInt("tier");
        return createGemPouch(tier, number);

    }

    public void upgradeStorage(UUID uniqueId) {
        storage.get(uniqueId).upgrade();
    }
}
