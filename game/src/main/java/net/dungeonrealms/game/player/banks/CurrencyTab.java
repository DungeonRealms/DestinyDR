package net.dungeonrealms.game.player.banks;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.miscellaneous.ScrapTier;
import net.minecraft.server.v1_9_R2.ChatMessage;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.PacketPlayOutOpenWindow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;


public class CurrencyTab {

    @Getter
    public static boolean enabled = true;

    @Getter
    private Map<ScrapTier, Integer> scrapStorage = new LinkedHashMap<>();

    private UUID owner;


    public boolean hasAccess = false;

    public CurrencyTab(UUID owner) {
        this.owner = owner;
    }

    public CurrencyTab deserializeCurrencyTab(String data) {
        if (data == null || !data.contains(":")) return null;
        String[] args = data.split(":");

        try {
            for (int i = 0; i < ScrapTier.values().length; i++) {
                ScrapTier tier = ScrapTier.values()[i];
                scrapStorage.put(tier, Integer.parseInt(args[i]));
            }

            hasAccess = true;
        } catch (Exception e) {
            Bukkit.getLogger().info("Unable to parse currency tab for " + owner.toString());
            e.printStackTrace();
        }
        return this;
    }

//    public void loadCurrencyTab(Consumer<CurrencyTab> doAfter) {
//        if (scrapStorage.isEmpty()) {
//            Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
//
//                Boolean access = (Boolean) DatabaseAPI.getInstance().getData(EnumData.CURRENCY_TAB_ACCESS, owner);
//
//                if (access == null || !access) {
//                    hasAccess = false;
//                } else {
//                    hasAccess = true;
//                    for (ScrapTier tier : ScrapTier.values()) {
//                        Integer found = (Integer) DatabaseAPI.getInstance().getData(tier.getDbData(), owner);
//                        scrapStorage.put(tier, found == null ? 0 : found);
//                    }
//                }
//
//                if (doAfter != null)
//                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> doAfter.accept(this));
//            });
//        } else if (doAfter != null) {
//            doAfter.accept(this);
//        }
//    }


    public String getSerializedScrapTab() {
        if(!this.hasAccess)return null;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < ScrapTier.values().length; i++) {
            ScrapTier tier = ScrapTier.values()[i];
            Integer count = scrapStorage.get(tier);
            builder.append(count == null ? 0 : count).append(":");
        }
        return builder.toString();
    }

    public Inventory createCurrencyInventory() {
        Inventory inventory = Bukkit.createInventory(null, 9 * 3, getInventoryName());

        updateInventory(inventory);

        return inventory;
    }

    public void withdrawScrap(ScrapTier tier, int amount) {
        int current = getScrapCount(tier);

        int newAmount = current - amount;
        if (newAmount < 0) newAmount = 0;
        if (newAmount > 250) newAmount = 250;

        this.scrapStorage.put(tier, newAmount);

        //Save async?
//        DatabaseAPI.getInstance().update(owner, EnumOperators.$SET, tier.getDbData(), newAmount, true);
    }

    public void depositScrap(ScrapTier tier, int amount) {
        int current = getScrapCount(tier);

        int newAmount = current + amount;
        if (newAmount < 0) newAmount = 0;
        if (newAmount > 250) newAmount = 250;

        this.scrapStorage.put(tier, newAmount);

        //Save async?
//        DatabaseAPI.getInstance().update(owner, EnumOperators.$SET, tier.getDbData(), newAmount, true);
    }

    public String getInventoryName() {
        int MAX = 500;
        int totalUsed = getTotalScrapStored();
        return "Scrap Tab (" + totalUsed + " / " + MAX + ")";
    }

    public void updateInventory(Inventory inventory) {
        int slot = 9;
        for (ScrapTier tier : ScrapTier.values()) {

            ItemStack scrapPiece = ItemManager.createArmorScrap(tier.getTier());

            int scrapCount = getScrapCount(tier);
            int scrap = Math.max(1, scrapCount);
            if (scrap > 64) scrap = 1;
            scrapPiece.setAmount(scrap);
            ItemBuilder builder = new ItemBuilder().setItem(scrapPiece).setName(tier.getName() + " Scrap")
                    .addLore("",
                            tier.getChatColor().toString() + ChatColor.BOLD + "Scrap Stored",
                            tier.getChatColor().toString() + ChatColor.BOLD + scrapCount + " / 250",
                            "",
                            ChatColor.GRAY + "Left-Click to withdraw " + tier.getChatColor().toString() + (scrapCount > 64 ? 64 : scrapCount) + "x " + ChatColor.GRAY + "Scrap",
                            ChatColor.GRAY + "Right-Click to enter amount to withdraw.");

            NBTWrapper wrapper = new NBTWrapper(builder.build());
            wrapper.setInt("scrapTier", tier.getTier());
            inventory.setItem(slot, wrapper.build());
            slot += 2;
        }

        ItemStack pane = new ItemBuilder().setItem(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GRAY.getWoolData())).setName(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (item != null && item.getType() != Material.AIR) continue;

            inventory.setItem(i, pane);
        }
    }

    public void updateWindowTitle(final Player player) {
        String title = getInventoryName();
        int size = 9 * 3;
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        final PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(entityPlayer.activeContainer.windowId, "minecraft:chest", (IChatBaseComponent) new ChatMessage(title, new Object[0]), size);
        entityPlayer.playerConnection.sendPacket(packet);
        entityPlayer.updateInventory(entityPlayer.activeContainer);
    }

    public int getTotalScrapStored() {
        int amount = 0;
        for (Integer am : this.scrapStorage.values()) {
            amount += am;
        }
        return amount;
    }

    public int getScrapCount(ScrapTier tier) {
        Integer found = this.scrapStorage.get(tier);
        if (found == null) return 0;
        return found;
    }
}
