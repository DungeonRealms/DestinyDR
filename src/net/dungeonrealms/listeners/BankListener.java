package net.dungeonrealms.listeners;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.banks.Storage;
import net.dungeonrealms.mastery.ItemSerialization;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Chase, by fixed by Proxying and under inspection of xFinityPro.
 */
public class BankListener implements Listener {
	
	public ArrayList<UUID> prompted = new ArrayList<>();
	
    /**
     * Bank Inventory. When a player moves items
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderChestRightClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType() == Material.ENDER_CHEST) {
                Storage storage = BankMechanics.getInstance().getStorage(e.getPlayer().getUniqueId());
            	if(storage.collection_bin != null){
            		if(!prompted.contains(e.getPlayer().getUniqueId())){
            			prompted.add(e.getPlayer().getUniqueId());
            			e.getPlayer().sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.YELLOW + "Collection Bin emptied once you open it.");
            			e.getPlayer().sendMessage(ChatColor.YELLOW + "Open your chest again once you're ready to empty your collection bin.");
            			e.setCancelled(true);
            			Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), ()->{prompted.remove(e.getPlayer().getUniqueId());}, 100);
            			return;
            		}
            		e.getPlayer().openInventory(storage.collection_bin);
            		DatabaseAPI.getInstance().update(e.getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, "", true);
            		storage.collection_bin = null;
            		e.setCancelled(true);
            		return;
            	}
            	
                Block b = e.getClickedBlock();
                ItemStack stack = new ItemStack(b.getType(), 1);
                NBTTagCompound nbt = CraftItemStack.asNMSCopy(stack).getTag();
                e.setCancelled(true);
                e.getPlayer().openInventory(getBank(e.getPlayer().getUniqueId()));
                e.getPlayer().playSound(e.getPlayer().getLocation(), "random.chestopen", 1, 1);
            }
        }
    }

    /**
     * Bank inventorys clicked.
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBankClicked(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getInventory().getTitle().equalsIgnoreCase("Bank Chest")) {
//        	e.setCancelled(true);
            if (e.getCursor() != null) {
                net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(e.getCursor());
//                e.setCancelled(true);
                if (e.getRawSlot() < 9) {
                    if (e.getRawSlot() == 8) {
                        e.setCancelled(true);
                        if (e.getCursor() != null) {
                            if (e.getClick() == ClickType.LEFT) {
                                AnvilGUIInterface gui = AnvilApi.createNewGUI(player, event -> {
                                    if (event.getSlot() == AnvilSlot.OUTPUT) {
                                        int number = 0;
                                        try {
                                            number = Integer.parseInt(event.getName());
                                        } catch (Exception exc) {
                                            event.setWillClose(true);
                                            event.setWillDestroy(true);
                                            Bukkit.getPlayer(event.getPlayerName()).sendMessage("Please enter a valid number");
                                            return;
                                        }
                                        event.setWillClose(true);
                                        event.setWillDestroy(true);
                                        int currentGems = getPlayerGems(player.getUniqueId());
                                        if (number < 0) {
                                            player.getPlayer().sendMessage("You can't ask for negative money!");
                                        } else if (number > currentGems) {
                                            player.getPlayer().sendMessage("You only have " + currentGems);
                                        } else {
                                            ItemStack stack = BankMechanics.gem.clone();
                                            if (hasSpaceInInventory(player.getUniqueId(), number)) {
                                                Player p = player.getPlayer();
                                                DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(),
                                                        EnumOperators.$INC, EnumData.GEMS, -number, true);
                                                while (number > 0) {
                                                    while (number > 64) {
                                                        ItemStack item = stack.clone();
                                                        item.setAmount(64);
                                                        p.getInventory().setItem(p.getInventory().firstEmpty(), item);
                                                        number -= 64;
                                                    }
                                                    ItemStack item = stack.clone();
                                                    item.setAmount(number);
                                                    p.getInventory().setItem(p.getInventory().firstEmpty(), item);
                                                    number = 0;
                                                }
                                                player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                                            }
                                        }

                                    }
                                });
                                ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
                                ItemMeta meta = stack.getItemMeta();
                                meta.setDisplayName("Withdraw?");
                                stack.setItemMeta(meta);
                                gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
                                player.closeInventory();
                                gui.open();
                            } else if (e.getClick() == ClickType.RIGHT) {
                                AnvilGUIInterface gui = AnvilApi.createNewGUI(player, event -> {
                                    if (event.getSlot() == AnvilSlot.OUTPUT) {
                                        int number = 0;
                                        try {
                                            number = Integer.parseInt(event.getName());
                                        } catch (Exception exc) {
                                            event.setWillClose(true);
                                            event.setWillDestroy(true);
                                            Bukkit.getPlayer(event.getPlayerName()).sendMessage("Please enter a valid number");
                                            return;
                                        }
                                        event.setWillClose(true);
                                        event.setWillDestroy(true);
                                        int currentGems = getPlayerGems(player.getUniqueId());
                                        if (number < 0) {
                                            player.getPlayer().sendMessage("You can't ask for negative money!");
                                        } else if (number > currentGems) {
                                            player.getPlayer().sendMessage("You only have " + currentGems);
                                        } else {
                                            Player p = player.getPlayer();
                                            p.getInventory().addItem(BankMechanics.createBankNote(number));
                                            DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(),
                                                    EnumOperators.$INC, EnumData.GEMS, -number, true);
                                            player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);

                                        }

                                    }
                                });
                                ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
                                ItemMeta meta = stack.getItemMeta();
                                meta.setDisplayName("Withdraw?");
                                stack.setItemMeta(meta);
                                gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
                                player.closeInventory();
                                gui.open();
                            }

                        }
                    } else if (e.getRawSlot() != 0) {
                        if (nms == null)
                            return;
                        e.setCancelled(true);
                        if (nms.hasTag() && e.getCursor().getType() == Material.EMERALD
                                || nms.hasTag() && e.getCursor().getType() == Material.PAPER || nms.hasTag() && e.getCursor().getType() == Material.INK_SACK) {
                            if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")) {
                                int size = 0;
                                if (e.isLeftClick()) {
                                	if(e.getCursor().getType() == Material.INK_SACK){
                                		int type = nms.getTag().getInt("tier");
                                		size = nms.getTag().getInt("worth");
                                    	e.setCursor(null);
                                    	e.setCurrentItem(null);
                                		e.getWhoClicked().getInventory().addItem(BankMechanics.getInstance().createGemPouch(type, 0));
                                	}else if (e.getCursor().getType() == Material.EMERALD){
                                    	size = e.getCursor().getAmount();
                                    	e.setCursor(null);
                                    	e.setCurrentItem(null);
                                    }else if (e.getCursor().getType() == Material.PAPER){
                                        size = e.getCursor().getAmount() * nms.getTag().getInt("worth");
                                    	e.setCursor(null);
                                    	e.setCurrentItem(null);
                                    }
                                } else if (e.isRightClick()) {
                                    
                                    if (e.getCursor().getType() == Material.EMERALD)
                                        size = 1;
                                    else
                                        size = nms.getTag().getInt("worth");
                                    
                                    if(e.getCursor().getAmount() > 1){
                                        e.getCursor().setAmount(e.getCursor().getAmount() - 1);
                                    }else{
                                    	e.setCursor(null);
                                    }
                                }
                                BankMechanics.getInstance().addGemsToPlayerBank(player.getUniqueId(), size);
                                ItemStack bankItem = new ItemStack(Material.EMERALD);
                                ItemMeta meta = bankItem.getItemMeta();
                                meta.setDisplayName(getPlayerGems(player.getUniqueId()) + size + ChatColor.BOLD.toString()
                                        + ChatColor.GREEN + " GEM(s)");
                                ArrayList<String> lore = new ArrayList<>();
                                lore.add(ChatColor.GREEN + "Left Click " + ChatColor.GRAY + "to withdraw " + ChatColor.GREEN.toString() + ChatColor.BOLD + "RAW GEMS");
                                lore.add(ChatColor.GREEN + "Right Click " + ChatColor.GRAY + "to create " + ChatColor.GREEN.toString() + ChatColor.BOLD + "A GEM NOTE");
                                meta.setLore(lore);
                                bankItem.setItemMeta(meta);
                                net.minecraft.server.v1_8_R3.ItemStack nmsBank = CraftItemStack.asNMSCopy(bankItem);
                                nmsBank.getTag().setString("type", "bank");
                                e.getInventory().setItem(8, CraftItemStack.asBukkitCopy(nmsBank));
                                // checkOtherBankSlots(e.getInventory(),
                                // player.getUniqueId());
                                player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                            }
                        }
                    } else {
                        e.setCancelled(true);
                        Storage storage = BankMechanics.getInstance().getStorage(player.getUniqueId());
                        if (e.isLeftClick()) {
                            // Open Storage
                            player.openInventory(storage.inv);
                        } else if (e.isRightClick()) {
                            Inventory inv = Bukkit.createInventory(null, 9, "Upgrade your bank storage?");
                        	int invLvl = (int) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_LEVEL, player.getUniqueId());
                			int num = BankMechanics.getPrice(invLvl);
                            ItemStack accept = new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData());
                            ItemMeta acceptMeta = accept.getItemMeta();
                            acceptMeta.setDisplayName(ChatColor.GREEN.toString() +"Accept");
                            acceptMeta.setLore(Arrays.asList(new String[] {ChatColor.GRAY + "Upgrade your bank storage for " + ChatColor.RED.toString() + num +" gems"}));
                            accept.setItemMeta(acceptMeta);
                            
                            
                            ItemStack deny = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());
                            ItemMeta denyMeta = deny.getItemMeta();
                            denyMeta.setDisplayName(ChatColor.RED.toString() +"Deny");
                            denyMeta.setLore(Arrays.asList(new String[] {ChatColor.GRAY + "Cancel bank upgrade"}));
                            deny.setItemMeta(denyMeta);
                            
                            inv.setItem(3, accept);
                            inv.setItem(5, deny);
                            player.openInventory(inv);

                            // Upgrade Storage
                        }
                    }
                } else {
                    if (e.isShiftClick()) {
                    	if(e.getCurrentItem().getType() != Material.EMERALD && e.getCurrentItem().getType() != Material.PAPER && e.getCurrentItem().getType() != Material.INK_SACK){ e.setCancelled(true); return;}
                    	
                        nms = CraftItemStack.asNMSCopy(e.getCurrentItem());
                        if(!nms.hasTag())
                        	return;
                        int size = 0;
                        if (e.getCurrentItem().getType() == Material.EMERALD){
                            size = e.getCurrentItem().getAmount();
                            e.setCurrentItem(null);
                        }else if (e.getCurrentItem().getType() == Material.PAPER) {
                            size = e.getCurrentItem().getAmount() * nms.getTag().getInt("worth");
                            e.setCurrentItem(null);
                        }else if(e.getCurrentItem().getType() == Material.INK_SACK){
                        	int tier = nms.getTag().getInt("tier");
                            size = nms.getTag().getInt("worth");
                        	e.setCurrentItem(BankMechanics.getInstance().createGemPouch(tier, 0));
                        }
                        if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")) {
                            e.setCancelled(true);
                            BankMechanics.getInstance().addGemsToPlayerBank(player.getUniqueId(), size);
                            ItemStack bankItem = new ItemStack(Material.EMERALD);
                            ItemMeta meta = bankItem.getItemMeta();
                            meta.setDisplayName(getPlayerGems(player.getUniqueId()) + size + ChatColor.BOLD.toString()
                                    + ChatColor.GREEN + " Gem(s)");
                            ArrayList<String> lore = new ArrayList<>();
                            lore.add(ChatColor.GREEN.toString() + "Left Click " + " to withdraw Raw Gems.");
                            lore.add(ChatColor.GREEN.toString() + "Right Click " + " to create a Bank Note.");
                            meta.setLore(lore);
                            bankItem.setItemMeta(meta);
                            net.minecraft.server.v1_8_R3.ItemStack nmsBank = CraftItemStack.asNMSCopy(bankItem);
                            nmsBank.getTag().setString("type", "bank");
                            e.getInventory().setItem(8, CraftItemStack.asBukkitCopy(nmsBank));
                            player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                        }
                    }
                }
            }
        } else if (e.getInventory().getTitle().equalsIgnoreCase("How Many?")) {
            e.setCancelled(true);
            if (e.getRawSlot() < 27) {
                ItemStack current = e.getCurrentItem();
                if (current != null) {
                    if (current.getType() == Material.STAINED_GLASS_PANE) {
                        int number = getAmmount(e.getRawSlot());
                        int currentWith = CraftItemStack.asNMSCopy(e.getInventory().getItem(4)).getTag().getInt("withdraw");
                        int finalNum = 0;
                        finalNum = currentWith + number;
                        if (finalNum < 0)
                            finalNum = 0;
                        ItemStack item = new ItemStack(Material.EMERALD, 1);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName("Withdraw " + finalNum + " Gems");
                        item.setItemMeta(meta);
                        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
                        nms.getTag().setInt("withdraw", finalNum);
                        e.getInventory().setItem(4, CraftItemStack.asBukkitCopy(nms));
                    } else if (current.getType() == Material.INK_SACK) {
                        int number = CraftItemStack.asNMSCopy(e.getInventory().getItem(4)).getTag().getInt("withdraw");
                        if (number == 0) {
                            return;
                        }
                        int currentGems = getPlayerGems(player.getUniqueId());
                        try {
                            if (number < 0) {
                                player.getPlayer().sendMessage("You can't ask for negative money!");
                            } else if (number > currentGems) {
                                player.getPlayer().sendMessage("You only have " + currentGems);
                            } else {
                                ItemStack stack = BankMechanics.gem.clone();
                                if (hasSpaceInInventory(player.getUniqueId(), number)) {
                                    Player p = player.getPlayer();
                                    DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(), EnumOperators.$INC,
                                            EnumData.GEMS, -number, true);
                                    while (number > 0) {
                                        while (number > 64) {
                                            ItemStack item = stack.clone();
                                            item.setAmount(64);
                                            p.getInventory().setItem(p.getInventory().firstEmpty(), item);
                                            number -= 64;
                                        }
                                        ItemStack item = stack.clone();
                                        item.setAmount(number);
                                        p.getInventory().setItem(p.getInventory().firstEmpty(), item);
                                        number = 0;
                                    }
                                    player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                                } else {
                                    player.getPlayer().sendMessage("You do not have space for all those gems");
                                }
                            }
                            player.closeInventory();
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }

                    }
                }
            }
        } else if (e.getInventory().getTitle().equalsIgnoreCase("How much?")) {
            e.setCancelled(true);
            if (e.getRawSlot() < 27) {
                ItemStack current = e.getCurrentItem();
                if (current != null) {
                    if (current.getType() == Material.STAINED_GLASS_PANE) {
                        int number = getAmmount(e.getRawSlot());
                        int currentWith = CraftItemStack.asNMSCopy(e.getInventory().getItem(4)).getTag().getInt("withdraw");
                        int finalNum = 0;
                        finalNum = currentWith + number;
                        if (finalNum < 0)
                            finalNum = 0;
                        ItemStack item = new ItemStack(Material.PAPER, 1);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName("Withdraw " + finalNum + " Gems");
                        item.setItemMeta(meta);
                        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
                        nms.getTag().setInt("withdraw", finalNum);
                        e.getInventory().setItem(4, CraftItemStack.asBukkitCopy(nms));
                    } else if (current.getType() == Material.INK_SACK) {
                        int number = CraftItemStack.asNMSCopy(e.getInventory().getItem(4)).getTag().getInt("withdraw");
                        if (number == 0) {
                            return;
                        }
                        int currentGems = getPlayerGems(player.getUniqueId());
                        try {
                            if (number < 0) {
                                player.getPlayer().sendMessage("You can't ask for negative money!");
                            } else if (number > currentGems) {
                                player.getPlayer().sendMessage("You only have " + currentGems);
                            } else {
                                ItemStack stack = BankMechanics.banknote.clone();
                                ItemMeta meta = stack.getItemMeta();
                                ArrayList<String> lore = new ArrayList<>();
                                lore.add(ChatColor.BOLD.toString() + "Value: " + ChatColor.WHITE.toString() + number);
                                meta.setLore(lore);
                                stack.setItemMeta(meta);
                                net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
                                nms.getTag().setInt("worth", number);
                                Player p = player.getPlayer();
                                p.getInventory().addItem(CraftItemStack.asBukkitCopy(nms));
                                DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(), EnumOperators.$INC,
                                        EnumData.GEMS, -number, true);
                                player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                            }
                            player.closeInventory();
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }

                    }
                }
            }
        }else if(e.getInventory().getTitle().contains("Upgrade your bank storage")){
        	e.setCancelled(true);
        	int invLvl = (int) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_LEVEL, player.getUniqueId());
			int num = BankMechanics.getPrice(invLvl);
			//TODO PRICE OF UPGRADE ^ 
        	int slot = e.getRawSlot();
        	if(slot ==3){
            	boolean tookGems = BankMechanics.getInstance().takeGemsFromInventory(num, player);
        		if(tookGems){
        			Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), ()->{
        			DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.INVENTORY_LEVEL, invLvl + 1,
    				        true);
        			player.sendMessage(ChatColor.GREEN.toString() + "Storage updated!");
        			player.closeInventory();
        			Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), ()->BankMechanics.getInstance().getStorage(player.getUniqueId()).update(), 20l);
        			});
        		}else{
        			player.closeInventory();
        			player.sendMessage(ChatColor.RED.toString() + "Not enough Gems in your inventory!");
        		}
        	}
        }
    }

    /**
     * Gets amount to add, or subtract for each slot clicked in How Many?
     * Inventory.
     *
     * @param slot
     * @since 1.0
     */
    private int getAmmount(int slot) {
        switch (slot) {
            case 0:
                return -1000;
            case 1:
                return -100;
            case 2:
                return -10;
            case 3:
                return -1;
            case 5:
                return 1;
            case 6:
                return 10;
            case 7:
                return 100;
            case 8:
                return 1000;
        }
        return 0;
    }

    /**
     * Checks if player has room in inventory for ammount of gems to withdraw.
     *
     * @param uuid
     * @param Gems_worth being added
     * @since 1.0
     */
    private boolean hasSpaceInInventory(UUID uuid, int Gems_worth) {
        if (Gems_worth > 64) {
            int space_needed = Math.round(Gems_worth / 64) + 1;
            int count = 0;
            ItemStack[] contents = Bukkit.getPlayer(uuid).getInventory().getContents();
            for (ItemStack content : contents) {
                if (content == null || content.getType() == Material.AIR) {
                    count++;
                }
            }
            int empty_slots = count;

            if (space_needed > empty_slots) {
                Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED
                        + "You do not have enough space in your inventory to withdraw " + Gems_worth + " GEM(s).");
                Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "REQ: " + space_needed + " slots");
                return false;
            } else
                return true;
        }
        return Bukkit.getPlayer(uuid).getInventory().firstEmpty() != -1;
    }

    /**
     * Gets an Inventory specific for player.
     *
     * @param uuid
     * @since 1.0
     */
    private Inventory getBank(UUID uuid) {
        Inventory inv = Bukkit.createInventory(null, 9, "Bank Chest");
        ItemStack bankItem = new ItemStack(Material.EMERALD);
        ItemStack storage = new ItemStack(Material.CHEST, 1);
        ItemMeta storagetMeta = storage.getItemMeta();
        storagetMeta.setDisplayName(ChatColor.RED.toString() + "Storage");
        ArrayList<String> storelore = new ArrayList<>();
        storelore.add(ChatColor.GREEN.toString() + "Left Click to open your storage.");
        storelore.add(ChatColor.GREEN.toString() + "Right Click to upgrade your storage!");
        storagetMeta.setLore(storelore);
        storage.setItemMeta(storagetMeta);
        net.minecraft.server.v1_8_R3.ItemStack storagenms = CraftItemStack.asNMSCopy(storage);
        storagenms.getTag().setString("type", "storage");
        inv.setItem(0, CraftItemStack.asBukkitCopy(storagenms));

        ItemMeta meta = bankItem.getItemMeta();
        meta.setDisplayName(getPlayerGems(uuid) + ChatColor.BOLD.toString() + ChatColor.GREEN + " Gem(s)");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN.toString() + "Left Click to withdraw Raw Gems.");
        lore.add(ChatColor.GREEN.toString() + "Right Click to create a Bank Note.");
        meta.setLore(lore);
        bankItem.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(bankItem);
        nms.getTag().setString("type", "bank");
        inv.setItem(8, CraftItemStack.asBukkitCopy(nms));
        return inv;
    }

    /**
     * Get Player Gems.
     *
     * @param uuid
     * @since 1.0
     */
    private int getPlayerGems(UUID uuid) {
        return (int) DatabaseAPI.getInstance().getData(EnumData.GEMS, uuid);
    }

}
