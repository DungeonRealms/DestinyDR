package net.dungeonrealms.game.player.trade;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.world.item.CC;
import net.minecraft.server.v1_9_R2.GameProfileSerializer;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Chase on Nov 16, 2015
 */

/**
 * Roll Dueling Created by SecondAmendment on 12/14/2017
 */
public class Trade {

    public Player p1;
    public Player p2;
    public boolean p1Ready;
    public boolean p2Ready;
    private boolean rollDuelSlot = true;
    public boolean p1RollDuel = false;
    public boolean p2RollDuel = false;
    public boolean duelInProgress = false;
    public ItemStack rollHead = ItemManager.createItem(Material.SKULL_ITEM, CC.WhiteB + "Roll Duel", (byte) SkullType.PLAYER.ordinal());
    public ItemStack p1Skull;
    public ItemStack p2Skull;
    public ArrayList<ItemStack> p1Bet = new ArrayList<>();
    public ArrayList<ItemStack> p2Bet = new ArrayList<>();
    private int p1result;
    private int p2result;

    public Player winner;
    //private String skullTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTFlMTJhZDk3NTlhM2QzNjhlNWQ5Njk2ZWQxMjRmNzMzNDA2YzRmNzE2MmJhYzRmYTM4YTk4MjE4YjdkN2M2In19fQ==";
    public Inventory inv;

    private List<ItemStack> p1Items;

    public Trade(Player p1, Player p2) {
        this.p1 = p1;
        this.p2 = p2;
        p1Skull = ItemManager.createItem(Material.SKULL_ITEM, CC.GreenB + p1.getName(), (byte) SkullType.PLAYER.ordinal());
        p2Skull = ItemManager.createItem(Material.SKULL_ITEM, CC.GreenB + p2.getName(), (byte) SkullType.PLAYER.ordinal());
        SkullMeta p1SkullMeta = (SkullMeta) p1Skull.getItemMeta();
        p1SkullMeta.setOwner(p1.getName());
        p1Skull.setItemMeta(p1SkullMeta);
        SkullMeta p2SkullMeta = (SkullMeta) p2Skull.getItemMeta();
        p2SkullMeta.setOwner(p2.getName());
        p2Skull.setItemMeta(p2SkullMeta);
        p1.sendMessage(ChatColor.YELLOW + "Trading with " + ChatColor.BOLD + p2.getName() + "...");
        p2.sendMessage(ChatColor.YELLOW + "Trading with " + ChatColor.BOLD + p1.getName() + "...");
        openInventory();
    }

    public void remove() {
        TradeManager.trades.remove(this);
    }

    /**
     * Opens Trade Window
     */
    private void openInventory() {
//        inv = Bukkit.createInventory(null, 36, generateTitle(p1.getName(), p2.getName()));
        inv = Bukkit.createInventory(null, 36, "Trade Window");
        if (!p1.isOnline() || p1 == null || !p2.isOnline() || p2 == null) {
            TradeManager.trades.remove(this);
            return;
        }
        Bukkit.getPlayer(p1.getUniqueId()).closeInventory();
        Bukkit.getPlayer(p2.getUniqueId()).closeInventory();
        p1.setCanPickupItems(false);
        p2.setCanPickupItems(false);
        ItemStack item = ItemManager.createItem(Material.INK_SACK, ChatColor.YELLOW.toString() + "READY UP");
        item.setDurability(DyeColor.GRAY.getDyeData());
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("status", "notready");
        nms.setTag(nbt);
        nms.c(ChatColor.YELLOW + "READY UP");
        inv.setItem(0, CraftItemStack.asBukkitCopy(nms));
        inv.setItem(8, CraftItemStack.asBukkitCopy(nms));
        setDividerColor(DyeColor.WHITE);
        if(rollDuelSlot)setRollDuelSlots();
        p1.openInventory(inv);
        p2.openInventory(inv);
    }

    public void setDividerColor(DyeColor dye) {
        ItemStack separator = ItemManager.createItem(Material.STAINED_GLASS_PANE, " ");
        separator.setDurability(dye.getData());
        if(!rollDuelSlot)inv.setItem(4, separator);
        inv.setItem(13, separator);
        inv.setItem(22, separator);
        inv.setItem(31, separator);
    }

    public void setRollDuelSlots(){
        ItemStack p1head = ItemManager.createItem(Material.SKULL_ITEM, CC.WhiteB + "DUEL: " + CC.DarkRedB  + "NO", (byte) SkullType.PLAYER.ordinal());
        ItemStack p2head = ItemManager.createItem(Material.SKULL_ITEM, CC.WhiteB + "DUEL: " + CC.DarkRedB  + "NO", (byte) SkullType.PLAYER.ordinal());

        net.minecraft.server.v1_9_R2.ItemStack nms1 = CraftItemStack.asNMSCopy(p1head);
        NBTTagCompound nbt1 = new NBTTagCompound();
        nbt1.setBoolean("rollduel", false);
        nms1.setTag(nbt1);
        nms1.c(CC.WhiteB + "DUEL: " + CC.DarkRedB  + "NO");

        net.minecraft.server.v1_9_R2.ItemStack nms2 = CraftItemStack.asNMSCopy(p2head);
        NBTTagCompound nbt2 = new NBTTagCompound();
        nbt2.setBoolean("rollduel", false);
        nms2.setTag(nbt2);
        nms2.c(CC.WhiteB + "DUEL: " + CC.DarkRedB  + "NO");

        p1head = CraftItemStack.asBukkitCopy(nms1);
        p2head = CraftItemStack.asBukkitCopy(nms2);

        SkullMeta diceMeta = (SkullMeta) rollHead.getItemMeta();
        SkullMeta p1headMeta = (SkullMeta) p1head.getItemMeta();
        SkullMeta p2headMeta = (SkullMeta) p2head.getItemMeta();
        //For setting custom textures (We need a signed Dice texture, but for now this guy's skin is registered on minecraft-heads.com so it wont change)

        //GameProfile gameProfile = new GameProfile(UUID.fromString("a3bfbea1-6a4b-4d71-be1e-4b6cb2a981f2"), "Dice");
        //byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", skullTexture).getBytes());
        //gameProfile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        //NBTTagCompound skinNBT = new NBTTagCompound();
        // GameProfileSerializer.serialize(skinNBT, gameProfile);

        diceMeta.setOwner("ElliotIsShort");
        p1headMeta.setOwner(p1.getName());
        p2headMeta.setOwner(p2.getName());
        rollHead.setItemMeta(diceMeta);
        p1head.setItemMeta(p1headMeta);
        p2head.setItemMeta(p2headMeta);
        inv.setItem(3, p1head);
        inv.setItem(4, rollHead);
        inv.setItem(5, p2head);
        p1.updateInventory();
        p2.updateInventory();
    }


    public static String generateTitle(String lPName, String rPName) {
        String title = lPName;

        int spacesLeft = 32 - (title.length() + rPName.length());

        for (int i = 0; i < spacesLeft; i++) {
            title += " ";
        }
        title += rPName;
        return title;
    }


    // 0, 8 Confirm
    // 4, 13, 22, 27, 31 separator

    /**
     * Checks if specified slot is owned the the player on the left side.
     * <p>
     * LEFT ITEMS 1, 2, 3 9, 10, 11, 12, 18, 19, 20, 21
     *
     * @param slot
     * @return boolean
     * @since 1.0
     */
    public boolean isLeftSlot(int slot) {
        int[] left = new int[]{0, 1, 2, 3, 9, 10, 11, 12, 13, 18, 19, 20, 21, 30, 27, 28, 29};
        for (int aLeft : left)
            if (aLeft == slot)
                return true;
        return false;
    }

    /**
     * // RIGHT ITEMS 23, 24, 25, 26, 5, 6, 7, 14, 15, 16, 17
     *
     * @param slot
     * @return
     */
    public boolean isRightSlot(int slot) {
        int[] right = new int[]{23, 24, 25, 26, 5, 6, 7, 8, 14, 15, 16, 17, 32, 33, 34, 35};
        for (int aRight : right)
            if (aRight == slot)
                return true;
        return false;
    }

    /**
     * Handles if one player closes the trade inv before both players are ready.
     */
    public void handleClose() {
        for (int i = 1; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null)
                continue;
            if (item.getType() == Material.AIR || item.getType() == Material.STAINED_GLASS_PANE || item.getType() == Material.SKULL_ITEM)
                continue;
            if (i == 8)
                continue;
            if (isLeftSlot(i)) {
                GameAPI.giveOrDropItem(p1, item);
            } else if (isRightSlot(i)) {
                GameAPI.giveOrDropItem(p2, item);
            }
        }

        if (p1.getItemOnCursor() != null) {
            ItemStack item = p1.getItemOnCursor().clone();
            p1.setItemOnCursor(null);
            GameAPI.giveOrDropItem(p1, item);

        }
        if (p2.getInventory() != null) {
            ItemStack item = p2.getItemOnCursor().clone();
            p2.setItemOnCursor(null);
            GameAPI.giveOrDropItem(p2, item);
//            p2.getInventory().addItem(item);

        }
        p1.setCanPickupItems(true);
        p2.setCanPickupItems(true);
        remove();
        p1.closeInventory();
        p2.closeInventory();
        p1.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Trade cancelled.");
        p2.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Trade cancelled.");
    }

    /**
     * @param uniqueId
     * @return
     */
    public boolean isLeftPlayer(UUID uniqueId) {
        return uniqueId.toString().equalsIgnoreCase(p1.getUniqueId().toString());
    }

    /**
     * Checks if both players are readied up and then doTrade
     */
    public void checkReady() {
        if(!(p1RollDuel && p2RollDuel) && !duelInProgress) {
            if (p1Ready && p2Ready) {
                for (int i = 1; i < inv.getSize(); i++) {
                    ItemStack item = inv.getItem(i);
                    if (item == null)
                        continue;
                    if (item.getType() == Material.AIR || item.getType() == Material.STAINED_GLASS_PANE || item.getType() == Material.SKULL_ITEM)
                        continue;
                    if (i == 8)
                        continue;

                    if (isLeftSlot(i)) {
                        if (p2.getInventory().firstEmpty() == -1) {
                            //CANT TRADE
                            p1.sendMessage(ChatColor.RED + p2.getName() + " does not have enough inventory space to accept the trade.");
                            p2.sendMessage(ChatColor.RED + p2.getName() + " does not have enough inventory space to accept the trade.");
                            changeReady();
                            return;
                        }
                    } else if (isRightSlot(i)) {
                        if (p1.getInventory().firstEmpty() == -1) {
                            p1.sendMessage(ChatColor.RED + p1.getName() + " does not have enough inventory space to accept the trade.");
                            p2.sendMessage(ChatColor.RED + p1.getName() + " does not have enough inventory space to accept the trade.");
                            changeReady();
                            return;
                        }
                    }
                }

                p1.closeInventory();
                p2.closeInventory();
                doTrade();
            }
        }
        else{
            changeReady();
            changeRollDuel();
            p1.sendMessage(CC.RedB + "You cannot trade and Roll Duel at the same time!");
            p2.sendMessage(CC.RedB + "You cannot trade and Roll Duel at the same time!");
        }
    }

    public void checkRollDuel(){
        if(!(p1Ready && p2Ready) && !duelInProgress) {
            if (p1RollDuel && p2RollDuel) {
                for (int i = 1; i < inv.getSize(); i++) {
                    ItemStack item = inv.getItem(i);
                    if (item == null)
                        continue;
                    if (item.getType() == Material.AIR || item.getType() == Material.STAINED_GLASS_PANE || item.getType() == Material.SKULL_ITEM)
                        continue;
                    if (i == 8)
                        continue;

                    if (isLeftSlot(i)) {
                        if (p2.getInventory().firstEmpty() == -1) {
                            //CANT DUEL
                            p1.sendMessage(ChatColor.RED + p2.getName() + " does not have enough inventory space to accept the Roll Duel.");
                            p2.sendMessage(ChatColor.RED + p2.getName() + " does not have enough inventory space to accept the Roll Duel.");
                            changeRollDuel();
                            return;
                        }
                    } else if (isRightSlot(i)) {
                        if (p1.getInventory().firstEmpty() == -1) {
                            p1.sendMessage(ChatColor.RED + p1.getName() + " does not have enough inventory space to accept the Roll Duel.");
                            p2.sendMessage(ChatColor.RED + p1.getName() + " does not have enough inventory space to accept the Roll Duel.");
                            changeRollDuel();
                            return;
                        }
                    }
                }
                doRollDuel();
            }
        }
        else{
            changeReady();
            changeRollDuel();
            p1.sendMessage(CC.RedB + "You cannot trade and Roll Duel at the same time!");
            p2.sendMessage(CC.RedB + "You cannot trade and Roll Duel at the same time!");
        }
    }

    /**
     * Finalize trade
     */
    private void doTrade() {
        for (int i = 1; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null)
                continue;
            if (item.getType() == Material.AIR || item.getType() == Material.STAINED_GLASS_PANE || item.getType() == Material.SKULL_ITEM)
                continue;
            if (i == 8)
                continue;
            if (isLeftSlot(i)) {
                p2.getInventory().addItem(item);
            } else if (isRightSlot(i)) {
                p1.getInventory().addItem(item);
            }
        }
        p1.setCanPickupItems(true);
        p2.setCanPickupItems(true);
        p1.sendMessage(ChatColor.GREEN + "Trade successful.");
        p2.sendMessage(ChatColor.GREEN + "Trade successful.");
        remove();
    }

    private void doRollDuel(){
        duelInProgress = true;
        for (int i = 1; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null)
                continue;
            if (item.getType() == Material.AIR || item.getType() == Material.STAINED_GLASS_PANE || item.getType() == Material.SKULL_ITEM)
                continue;
            if (i == 8)
                continue;
            if (isLeftSlot(i)) {
                p1Bet.add(item);
            } else if (isRightSlot(i)) {
                p2Bet.add(item);
            }
        }
        roll();
        playRollDuelAnimation();
    }

    public void giveWinnings(){
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), new Runnable() {
            public void run() {
                ArrayList<ItemStack> winnings = new ArrayList<>(p1Bet);
                winnings.addAll(p2Bet);
                inv.clear();
                p1.closeInventory();
                p2.closeInventory();

                for(ItemStack i : winnings){
                    winner.getInventory().addItem(i);
                }
                GameAPI.getNearbyPlayers(p1.getLocation(), 20).forEach(player1 -> player1.sendMessage(p1.getName() + ChatColor.GRAY + " has rolled a " + ChatColor.UNDERLINE + ChatColor.BOLD + p1result + ChatColor.GRAY + " out of " + ChatColor.UNDERLINE + ChatColor.BOLD + 100 + ChatColor.GRAY + "."));
                GameAPI.getNearbyPlayers(p2.getLocation(), 20).forEach(player2 -> player2.sendMessage(p2.getName() + ChatColor.GRAY + " has rolled a " + ChatColor.UNDERLINE + ChatColor.BOLD + p2result + ChatColor.GRAY + " out of " + ChatColor.UNDERLINE + ChatColor.BOLD + 100 + ChatColor.GRAY + "."));
                p1.sendMessage(ChatColor.GREEN + winner.getName() + " won the duel.");
                p2.sendMessage(ChatColor.GREEN + winner.getName() + " won the duel.");
                p1.setCanPickupItems(true);
                p2.setCanPickupItems(true);
                remove();
            }
        }, 20L);
    }

    public void changeReady() {
//        ItemStack item = ItemManager.createItem(Material.INK_SACK, ChatColor.YELLOW.toString() + "READY UP");
//        item.setDurability(DyeColor.GRAY.getWoolData());
//        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
//        NBTTagCompound nbt = new NBTTagCompound();
//        nbt.setString("status", "notready");
//        nms.setTag(nbt);
//        nms.c(ChatColor.YELLOW + "READY UP");

        ItemStack item =
                new NBTWrapper(ItemManager.createItem(Material.INK_SACK, ChatColor.YELLOW.toString() + "READY UP", DyeColor.GRAY.getDyeData(), ChatColor.GRAY + "Click to accept trade"))
                        .setString("status", "notready").build();

        inv.setItem(0, item);
        inv.setItem(8, item);
        p1Ready = false;
        p2Ready = false;
        playSound(Sound.BLOCK_ANVIL_FALL, 1.8F);
    }

    public void changeRollDuel() {
        ItemStack p1head = ItemManager.createItem(Material.SKULL_ITEM, CC.WhiteB + "DUEL: " + CC.DarkRedB  + "NO", (byte) SkullType.PLAYER.ordinal());
        ItemStack p2head = ItemManager.createItem(Material.SKULL_ITEM, CC.WhiteB + "DUEL: " + CC.DarkRedB  + "NO", (byte) SkullType.PLAYER.ordinal());

        net.minecraft.server.v1_9_R2.ItemStack nms1 = CraftItemStack.asNMSCopy(p1head);
        NBTTagCompound nbt1 = new NBTTagCompound();
        nbt1.setBoolean("rollduel", false);
        nms1.setTag(nbt1);
        nms1.c(CC.WhiteB + "DUEL: " + CC.DarkRedB  + "NO");

        net.minecraft.server.v1_9_R2.ItemStack nms2 = CraftItemStack.asNMSCopy(p2head);
        NBTTagCompound nbt2 = new NBTTagCompound();
        nbt2.setBoolean("rollduel", false);
        nms2.setTag(nbt2);
        nms2.c(CC.WhiteB + "DUEL: " + CC.DarkRedB  + "NO");

        p1head = CraftItemStack.asBukkitCopy(nms1);
        p2head = CraftItemStack.asBukkitCopy(nms2);

        SkullMeta p1headMeta = (SkullMeta) p1head.getItemMeta();
        SkullMeta p2headMeta = (SkullMeta) p2head.getItemMeta();

        p1headMeta.setOwner(p1.getName());
        p2headMeta.setOwner(p2.getName());

        p1head.setItemMeta(p1headMeta);
        p2head.setItemMeta(p2headMeta);
        inv.setItem(3, p1head);
        inv.setItem(5, p2head);

        p1RollDuel = false;
        p2RollDuel = false;
        playSound(Sound.BLOCK_ANVIL_FALL, 1.8F);
    }

    public void playSound(Sound sound, float speed) {
        p1.playSound(p1.getLocation(), sound, .3F, speed);
        p2.playSound(p2.getLocation(), sound, .3F, speed);
    }

    /**
     * @param uniqueId
     */
    public void updateReady(UUID uniqueId) {
        if (uniqueId.toString().equalsIgnoreCase(p1.getUniqueId().toString())) {
            p1Ready = !p1Ready;
            if (p1Ready && !duelInProgress) {
                p1.sendMessage(ChatColor.YELLOW + "Trade accepted, waiting for " + ChatColor.BOLD + p2.getName() + "...");
                p2.sendMessage(ChatColor.GREEN + p1.getName() + " has accepted the trade.");
                p2.sendMessage(ChatColor.GRAY + "Click the gray button (dye) to accept.");
            } else {
                if(!duelInProgress) {
                    p1.sendMessage(ChatColor.RED + "Trade is pending your accept..");
                    p2.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + p1.getName() + ChatColor.RED + " has unaccepted the trade");
                }
            }
        } else {
            p2Ready = !p2Ready;
            if (p2Ready && !duelInProgress) {
                p2.sendMessage(ChatColor.YELLOW + "Trade accepted, waiting for " + ChatColor.BOLD + p1.getName() + "...");
                p1.sendMessage(ChatColor.GREEN + p2.getName() + " has accepted the trade.");
                p1.sendMessage(ChatColor.GRAY + "Click the gray button (dye) to accept.");
            } else {
                if(!duelInProgress) {
                    p2.sendMessage(ChatColor.RED + "Trade is pending your accept..");
                    p1.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + p1.getName() + ChatColor.RED + " has unaccepted the trade");
                }
            }
        }
    }

    public void updateRollDuel(UUID uniqueId){
        if (uniqueId.toString().equalsIgnoreCase(p1.getUniqueId().toString())) {
            p1RollDuel = !p1RollDuel;
            if(p1RollDuel  && !duelInProgress) {
                p1.sendMessage(CC.Blue + "Roll Duel Accepted, waiting for " + CC.Bold + p2.getName() + CC.Blue + " to accept the duel.");
                p2.sendMessage(CC.Green + p1.getName() + " has challenged you to a Roll Duel.");
                p2.sendMessage(CC.Gray + "Click on your Roll Duel head to accept.");
            }
            else{
                if(!duelInProgress) {
                    p1.sendMessage(CC.Red + "Roll Duel is pending your approval...");
                    p2.sendMessage(CC.RedB + p1.getName() + CC.Red + " has unaccepted the Roll Duel.");
                }
            }
        }
        else{
            p2RollDuel = !p2RollDuel;
            if(p2RollDuel  && !duelInProgress) {
                p2.sendMessage(CC.Blue + "Roll Duel Accepted, waiting for " + CC.Bold + p1.getName() + CC.Blue + " to accept the duel.");
                p1.sendMessage(CC.Green + p2.getName() + " has challenged you to a Roll Duel.");
                p1.sendMessage(CC.Gray + "Click on your Roll Duel head to accept.");
            }
            else{
                if(!duelInProgress) {
                    p2.sendMessage(CC.Red + "Roll Duel is pending your approval...");
                    p1.sendMessage(CC.RedB + p2.getName() + CC.Red + " has unaccepted the Roll Duel.");
                }
            }
        }
        playSound(Sound.BLOCK_NOTE_PLING, 1.8F);
    }

    public Player getOppositePlayer(Player player) {
        if (p1.equals(player)) return p2;
        return p1;
    }

    public void roll(){
        ThreadLocalRandom random = ThreadLocalRandom.current();
        p1result = random.nextInt(100) + 1;
        p2result = random.nextInt(100) + 1;
        if(p1result != p2result) {
            winner = (p1result > p2result) ? p1 : p2;
        }
        else{
            roll();
        }
    }

    public void playRollDuelAnimation(){
        new BukkitRunnable() {
            long currentTime = System.currentTimeMillis();
            long futureTime = currentTime;
            long delay = 10;
            long increment = 10;

            @Override
            public void run() {
                if(currentTime >= futureTime){
                    if(((SkullMeta)(inv.getItem(4).getItemMeta())).getOwner().equalsIgnoreCase(p2.getName())){
                        inv.setItem(4, p1Skull);
                        playSound(Sound.BLOCK_NOTE_PLING, 1);
                        p1.updateInventory();
                        p2.updateInventory();
                    }
                    else{
                        inv.setItem(4, p2Skull);
                        playSound(Sound.BLOCK_NOTE_PLING, 1);
                        p1.updateInventory();
                        p2.updateInventory();
                    }
                    futureTime = System.currentTimeMillis() + delay;
                    delay += increment;
                    increment += 5;
                }
                currentTime = System.currentTimeMillis();
                if(delay >= 1000) {
                    cancel();
                    inv.setItem(4, (winner == p1) ? p1Skull : p2Skull);
                    playSound(Sound.ENTITY_PLAYER_LEVELUP, 1);
                    p1.updateInventory();
                    p2.updateInventory();
                    giveWinnings();
                }
            }
        }.runTaskTimer(DungeonRealms.getInstance(), 0, 1);
    }
}
