package net.dungeonrealms.game.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.database.PlayerToggles.Toggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.UpdateType;
import net.dungeonrealms.game.achievements.Achievements.AchievementCategory;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.ItemGemNote;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMount;
import net.dungeonrealms.game.item.items.functional.ecash.ItemPet;
import net.dungeonrealms.game.item.items.functional.ecash.ItemPetSelector;
import net.dungeonrealms.game.listener.NPCMenu;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.ParticleAPI.ParticleEffect;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.miscellaneous.TradeCalculator;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.player.inventory.SupportMenus;
import net.dungeonrealms.game.player.inventory.menus.AchievementMenu;
import net.dungeonrealms.game.player.inventory.menus.guis.AchievementGUI;
import net.dungeonrealms.game.player.inventory.menus.guis.MountSelectionGUI;
import net.dungeonrealms.game.player.menu.CraftingMenu;
import net.dungeonrealms.game.player.support.Support;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Nick on 10/2/2015.
 */
public class ClickHandler {

	@Getter
    private static ClickHandler instance = new ClickHandler();

    public void doClick(InventoryClickEvent event) {
        String name = event.getInventory().getName();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (slot == -999)
        	return; // Dropping item.

        switch (name) {
            case "Merchant":
                Inventory tradeWindow = event.getInventory();
                if (event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
                    event.setCancelled(true);
                    return;
                }
                if (!(event.isShiftClick()) || (event.isShiftClick() && slot < 27)) {
                    if (!(event.getSlotType() == InventoryType.SlotType.CONTAINER)) {
                        return;
                    }
                    if (event.getInventory().getType() == InventoryType.PLAYER) {
                        return;
                    }
                    if (slot > 26 || slot < 0) {
                        return;
                    }
                    if (!(slot == 0 || slot == 1 || slot == 2 || slot == 3 || slot == 9 || slot == 10 || slot == 11 || slot == 12 || slot == 18 || slot == 19
                            || slot == 20 || slot == 21) && !(slot > 27)) {
                        event.setCancelled(true);
                        tradeWindow.setItem(slot, tradeWindow.getItem(slot));
                        player.setItemOnCursor(event.getCursor());
                        player.updateInventory();
                    } else if (!(event.isShiftClick())) {
                        if ((event.getCursor() == null || event.getCursor().getType() == Material.AIR && event.getCurrentItem() != null && CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && (!CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("acceptButton")))) {
                            event.setCancelled(true);
                            ItemStack slotItem = tradeWindow.getItem(slot);
                            tradeWindow.setItem(slot, new ItemStack(Material.AIR));
                            event.setCursor(slotItem);
                            player.updateInventory();
                        } else if ((event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR && event.getCursor() != null)) {
                            if (CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && (CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("subType"))) {
                                event.setCancelled(true);
                                player.updateInventory();
                            }
                            event.setCancelled(true);
                            ItemStack currentItem = event.getCursor();
                            tradeWindow.setItem(slot, currentItem);
                            event.setCursor(new ItemStack(Material.AIR));
                            player.updateInventory();
                        } else if (event.getCurrentItem() != null && event.getCursor() != null && CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && (!CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("acceptButton"))) {
                            event.setCancelled(true);
                            ItemStack currentItem = event.getCursor();
                            ItemStack slotItem = event.getCurrentItem();
                            event.setCursor(slotItem);
                            event.setCurrentItem(currentItem);
                            player.updateInventory();
                        }
                    }
                }
                if (event.isShiftClick() && slot < 26) {
                    if (!(slot == 0 || slot == 1 || slot == 2 || slot == 3 || slot == 9 || slot == 10 || slot == 11 || slot == 12 || slot == 18
                            || slot == 19 || slot == 20 || slot == 21) && !(slot > 27)) {
                        event.setCancelled(true);
                        if (tradeWindow.getItem(slot) != null && tradeWindow.getItem(slot).getType() != Material.AIR) {
                            tradeWindow.setItem(slot, tradeWindow.getItem(slot));
                            player.updateInventory();
                        }
                    } else if (CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && !CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("acceptButton")) {
                        event.setCancelled(true);
                        ItemStack slotItem = event.getCurrentItem();
                        if (player.getInventory().firstEmpty() != -1) {
                            tradeWindow.setItem(slot, new ItemStack(Material.AIR));
                            player.getInventory().setItem(player.getInventory().firstEmpty(), slotItem);
                            player.updateInventory();
                        }
                    }
                }
                if (event.isShiftClick() && slot >= 27 && !(event.isCancelled()) && !(event.getCurrentItem().getType() == Material.BOOK)) {
                    event.setCancelled(true);
                    ItemStack slotItem = event.getCurrentItem();
                    int localSlot = event.getSlot();
                    int x = -1;
                    while (x < 26) {
                        x++;
                        if (!(x == 0 || x == 1 || x == 2 || x == 3 || x == 9 || x == 10 || x == 11 || x == 12 || x == 18 || x == 19 || x == 20 || x == 21)) {
                            continue;
                        }
                        ItemStack itemStack = tradeWindow.getItem(x);
                        if (!(itemStack == null)) {
                            continue;
                        }
                        tradeWindow.setItem(x, slotItem);
                        if (tradeWindow.getItem(x) != null) {
                            tradeWindow.getItem(x).setAmount(slotItem.getAmount());
                        }
                        player.getInventory().setItem(localSlot, new ItemStack(Material.AIR));
                        player.updateInventory();
                        break;
                    }
                }
                List<ItemStack> player_Offer = new ArrayList<>();
                int x = -1;
                while (x < 26) {
                    x++;
                    if (!(x == 0 || x == 1 || x == 2 || x == 3 || x == 9 || x == 10 || x == 11 || x == 12 || x == 18 || x == 19 || x == 20 || x == 21)) {
                        continue;
                    }
                    ItemStack itemStack = tradeWindow.getItem(x);
                    if (itemStack == null || itemStack.getType() == Material.AIR || CraftItemStack.asNMSCopy(itemStack) != null && CraftItemStack.asNMSCopy(itemStack).getTag() != null && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("acceptButton")) {
                        continue;
                    }
                    player_Offer.add(itemStack);
                }
                List<ItemStack> new_Offer = TradeCalculator.calculateMerchantOffer(player_Offer);
                x = -1;
                while (x < 26) {
                    x++;
                    if ((x == 0 || x == 1 || x == 2 || x == 3 || x == 4 || x == 9 || x == 10 || x == 11 || x == 12 || x == 13 || x == 22 || x == 18 || x == 19
                            || x == 20 || x == 21)) {
                        continue;
                    }
                    tradeWindow.setItem(x, new ItemStack(Material.AIR));
                }
                x = -1;
                while (x < 26) {
                    x++;
                    if (new_Offer.size() > 0) {
                        if ((x == 0 || x == 1 || x == 2 || x == 3 || x == 4 || x == 9 || x == 10 || x == 11 || x == 12 || x == 13 || x == 22 || x == 18 || x == 19
                                || x == 20 || x == 21)) {
                            continue;
                        }
                        int index = new_Offer.size() - 1;
                        ItemStack itemStack = new_Offer.get(index);
                        tradeWindow.setItem(x, itemStack);
                        new_Offer.remove(index);
                    }
                }
                player.updateInventory();
                if (CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("acceptButton")) {
                    event.setCancelled(true);
                    if (event.getCurrentItem().getDurability() == 8) {
                        int player_Inv_Available = 0;
                        int inv_Needed = 0;
                        event.setCurrentItem(new ItemBuilder().setItem(Material.INK_SACK, (short) 10, ChatColor.GREEN + "Trade accepted.", new String[]{
                                ""
                        }).setNBTString("acceptButton", "whynot").build());
                        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1F, 2.F);

                        for (ItemStack itemStack : player.getInventory().getStorageContents()) {
                            if (itemStack == null || itemStack.getType() == Material.AIR) {
                                player_Inv_Available++;
                            }
                        }
                        int slot_Variable = -1;
                        while (slot_Variable < 26) {
                            slot_Variable++;
                            if (!(slot_Variable == 5 || slot_Variable == 6 || slot_Variable == 7 || slot_Variable == 8 || slot_Variable == 14 || slot_Variable == 15
                                    || slot_Variable == 16 || slot_Variable == 17 || slot_Variable == 23 || slot_Variable == 24 || slot_Variable == 25 || slot_Variable == 26)) {
                                continue;
                            }
                            ItemStack itemStack = tradeWindow.getItem(slot_Variable);
                            if (itemStack == null || itemStack.getType() == Material.AIR || (CraftItemStack.asNMSCopy(itemStack) != null && CraftItemStack.asNMSCopy(itemStack).getTag() != null && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("acceptButton")) || itemStack.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            inv_Needed++;
                        }
                        if (player_Inv_Available < inv_Needed) {
                            player.sendMessage(ChatColor.RED + "Inventory is full.");
                            player.sendMessage(ChatColor.GRAY + "You require " + ChatColor.BOLD + (inv_Needed - player_Inv_Available) + ChatColor.GRAY + " more free slots to complete this trade!");
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                InventoryCloseEvent closeEvent = new InventoryCloseEvent(player.getOpenInventory());
                                Bukkit.getServer().getPluginManager().callEvent(closeEvent);
                            }, 2L);
                            return;
                        }
                        slot_Variable = -1;
                        while (slot_Variable < 26) {
                            slot_Variable++;
                            if (!(slot_Variable == 5 || slot_Variable == 6 || slot_Variable == 7 || slot_Variable == 8 || slot_Variable == 14 || slot_Variable == 15
                                    || slot_Variable == 16 || slot_Variable == 17 || slot_Variable == 23 || slot_Variable == 24 || slot_Variable == 25 || slot_Variable == 26)) {
                                continue;
                            }
                            ItemStack itemStack = tradeWindow.getItem(slot_Variable);
                            if (itemStack == null || itemStack.getType() == Material.AIR || (CraftItemStack.asNMSCopy(itemStack) != null && CraftItemStack.asNMSCopy(itemStack).getTag() != null && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("acceptButton")) || itemStack.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            if (itemStack.getType() == Material.EMERALD) {
                            	itemStack = new ItemGemNote(player.getName(), itemStack.getAmount()).generateItem();
                            }
                            player.getInventory().setItem(player.getInventory().firstEmpty(), itemStack);
                        }
                        player.sendMessage(ChatColor.GREEN + "Trade Accepted.");

                        for (ItemStack stack : tradeWindow.getContents()) {
                            if (stack != null && stack.getType() == Material.MAGMA_CREAM) {
                                break;
                            }
                        }

                        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1F, 1F);
                        tradeWindow.clear();

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            player.updateInventory();
                            player.closeInventory();
                        }, 2L);

                        return;
                    }
                    player.updateInventory();
                }
                break;
            // CUSTOMER SUPPORT @todo: Move to own class to clean up & take advantage of own methods for reusing vars.
            case "Support Tools":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    break;

                NBTTagCompound tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                String playerName = tag.getString("name");
                UUID uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) break;

                switch (slot) {
                    case 4:
                        break;
                    case 19:
                        if (!playerName.equalsIgnoreCase(player.getDisplayName())) {
                            SupportMenus.openRankMenu(player, playerName, uuid);
                        } else {
                            player.sendMessage(ChatColor.RED + "You " + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "CANNOT" + ChatColor.RED + " change the rank of your own account.");
                            player.closeInventory();
                        }
                        break;
                    case 22:
                        SupportMenus.openLevelMenu(player, playerName, uuid);
                        break;
                    case 25:
                        SupportMenus.openECashMenu(player, playerName, uuid);
                        break;
                    case 28:
                        SupportMenus.openBankMenu(player, playerName, uuid);
                        break;
                    case 31:
                        SupportMenus.openHearthstoneMenu(player, playerName, uuid);
                        break;
                    case 34:
                        SupportMenus.openCosmeticsMenu(player, playerName, uuid);
                        break;

                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        break;
                }
                break;
            case "Support Tools (Rank)":
                /*event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;
                // Only developers can apply certain ranks (GM / Builder / Support / YouTuber)
                if (!Rank.isDev(player) && (slot == 29 || slot == 30 || slot == 32 || slot == 33)) return;

                String newRank;
                boolean subscriptionPrompt = false;

                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 20:
                        newRank = "DEFAULT";
                        break;
                    case 21:
                        newRank = "SUB";
                        //subscriptionPrompt = true;
                        break;
                    case 22:
                        newRank = "SUB+";
                        //subscriptionPrompt = true;
                        break;
                    case 23:
                        newRank = "SUB++";
                        break;
                    case 24:
                        newRank = "PMOD";
                        break;
                    case 29:
                        newRank = "BUILDER";
                        break;
                    case 30:
                        newRank = "YOUTUBE";
                        break;
                    case 32:
                        newRank = "SUPPORT";
                        break;
                    case 33:
                        newRank = "GM";
                        break;
                    default:
                        return;
                }

                if (subscriptionPrompt) {
                    SupportMenus.openRankSubscriptionMenu(player, playerName, uuid, newRank);
                    return;
                }

                // Always update the database with the new rank.
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK, newRank, true, doAfter -> {
                    if (Bukkit.getPlayer(playerName) != null) {
                        Rank.getInstance().setRank(uuid, newRank);
                    } else {
                        GameAPI.updatePlayerData(uuid);
                    }
                });

                player.sendMessage(ChatColor.GREEN + "Successfully set the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + newRank + ChatColor.GREEN + ".");
                SupportMenus.openMainMenu(player, playerName);*/
                break;
            case "Support Tools (Subscription)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));
                String subscriptionRank;

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                String subscriptionType;
                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 21:
                        subscriptionRank = tag.getString("rank");
                        subscriptionType = "add";
                        break;
                    case 22:
                        subscriptionRank = tag.getString("rank");
                        subscriptionType = "set";
                        break;
                    case 23:
                        subscriptionRank = tag.getString("rank");
                        subscriptionType = "remove";
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        return;
                }

                if (subscriptionRank != null && subscriptionType != null) {
                    player.sendMessage(ChatColor.YELLOW + "Please enter the number of days you would to " + subscriptionType + ":");

                    final String customSubscriptionRank = subscriptionRank;
                    final String customSubscriptionType = subscriptionType;
                    Chat.listenForMessage(player, chat -> {
                        Player target = Bukkit.getPlayer(chat.getMessage());
                        if (target != null) {
                            FriendHandler.getInstance().sendRequest(player, wrapper.getAccountID(), target);
                            player.sendMessage(ChatColor.GREEN + "Friend request sent to " + ChatColor.BOLD + target.getName() + ChatColor.GREEN + ".");
                        } else {
                            player.sendMessage(ChatColor.RED + "Oops, I can't find that player!");
                        }
                    }, p -> p.sendMessage(ChatColor.RED + "Action cancelled."));
                }

                break;
            case "Support Tools (Level)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                String levelType = "add";
                String variableName = "experience";
                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 21:
                        levelType = "add";
                        variableName = "experience";
                        break;
                    case 22:
                        levelType = "set";
                        variableName = "experience";
                        break;
                    case 23:
                        levelType = "remove";
                        variableName = "experience";
                        break;
                    case 30:
                        levelType = "add";
                        variableName = "level";
                        break;
                    case 31:
                        levelType = "set";
                        variableName = "level";
                        break;
                    case 32:
                        levelType = "remove";
                        variableName = "level";
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        return;
                }

                player.sendMessage(ChatColor.YELLOW + "Please enter the amount you would to " + levelType + ":");
                final String customLevelType = levelType;
                final String finalVariableName = variableName;
                Chat.listenForMessage(player, customAmount -> {
                    if (!customAmount.getMessage().equalsIgnoreCase("cancel") && !customAmount.getMessage().equalsIgnoreCase("exit")) {
                        try {
                            if (finalVariableName.equals("level")) {
                                Support.modifyLevel(player, playerName, uuid, Integer.parseInt(customAmount.getMessage()), customLevelType);
                            } else {
                                Support.modifyExp(player, playerName, uuid, Integer.parseInt(customAmount.getMessage()), customLevelType);
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + customAmount.getMessage() + " is not a valid number.");
                        }
                    }
                });

                break;
            case "Support Tools (E-Cash)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                boolean customInput = false;
                String ecashType = "add";
                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 21:
                        customInput = true;
                        ecashType = "add";
                        break;
                    case 22:
                        customInput = true;
                        ecashType = "set";
                        break;
                    case 23:
                        customInput = true;
                        ecashType = "remove";
                        break;
                    case 29:
                        Support.modifyEcash(player, playerName, uuid, 500, "add");
                        break;
                    case 30:
                        Support.modifyEcash(player, playerName, uuid, 2500, "add");
                        break;
                    case 32:
                        Support.modifyEcash(player, playerName, uuid, 5000, "add");
                        break;
                    case 33:
                        Support.modifyEcash(player, playerName, uuid, 9999, "add");
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        return;
                }

                if (customInput) {
                    player.sendMessage(ChatColor.YELLOW + "Please enter the amount you would to " + ecashType + ":");
                    final String customEcashType = ecashType;
                    Chat.listenForMessage(player, customAmount -> {
                        if (!customAmount.getMessage().equalsIgnoreCase("cancel") && !customAmount.getMessage().equalsIgnoreCase("exit")) {
                            try {
                                Support.modifyEcash(player, playerName, uuid, Integer.parseInt(customAmount.getMessage()), customEcashType);
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + customAmount.getMessage() + " is not a valid number.");
                            }
                        }
                    });
                }
                break;
            case "Support Tools (Bank)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                String bankType = "add";
                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 21:
                        bankType = "add";
                        break;
                    case 22:
                        bankType = "set";
                        break;
                    case 23:
                        bankType = "remove";
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        return;
                }

                player.sendMessage(ChatColor.YELLOW + "Please enter the amount you would to " + bankType + ":");
                final String customBankType = bankType;
                Chat.listenForMessage(player, customAmount -> {
                    if (!customAmount.getMessage().equalsIgnoreCase("cancel") && !customAmount.getMessage().equalsIgnoreCase("exit")) {
                        try {
                            Support.modifyGems(player, playerName, uuid, Integer.parseInt(customAmount.getMessage()), customBankType);
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + customAmount.getMessage() + " is not a valid number.");
                        }
                    }
                });

                break;
            case "Support Tools (Hearthstone)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                
                if (slot >= 17) {
                	int index = slot - 17;
                	int cIndex = 0;
                	for (TeleportLocation tl : TeleportLocation.values()) {
                		if (!tl.canBeABook())
                			continue;
                		if (cIndex == index) {
                			PlayerWrapper.getPlayerWrapper(uuid, false, true, (otherWrapper) -> {
                                if (otherWrapper == null) {
                                    player.sendMessage(ChatColor.RED + "Something went wrong!!");
                                    return;
                                }

                                otherWrapper.setHearthstone(tl);
                                otherWrapper.saveData(true, null, (someWrapper) -> {
                                    player.sendMessage(ChatColor.GREEN + "Successfully set the hearthstone of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + tl.getDisplayName() + ChatColor.GREEN + ".");
                                    GameAPI.updatePlayerData(uuid, UpdateType.HEARTHSTONE);
                                    SupportMenus.openMainMenu(player, playerName);
                                });
                            });
                			return;
                		}
                		cIndex++;
                	}
                	
                	
                	
                } else if (slot == 4) {
                	SupportMenus.openMainMenu(player, playerName);
                } else {
                	player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                	return;
                }

                break;
            case "Support Tools (Cosmetics)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 19:
                        SupportMenus.openTrailsMenu(player, playerName, uuid);
                        break;
                    case 22:
                        SupportMenus.openPetsMenu(player, playerName, uuid);
                        break;
                    case 25:
                        SupportMenus.openMountsMenu(player, playerName, uuid);
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        return;
                }
                break;
            case "Game Master Toggles":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

                switch (slot) {
                    case 0: // Vanish
                        if (GameAPI._hiddenPlayers.contains(player)) {
                            GameAPI._hiddenPlayers.remove(player);
                            for (Player player1 : Bukkit.getOnlinePlayers()) {
                                if (player1.getUniqueId().toString().equals(player.getUniqueId().toString())) {
                                    continue;
                                }
                                player1.showPlayer(player);
                            }
                            player.removePotionEffect(PotionEffectType.INVISIBILITY);
                            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You are now visible.");
                            GameAPI.sendNetworkMessage("vanish", player.getUniqueId().toString(), "false");
                            player.setCustomNameVisible(true);
                            player.setGameMode(GameMode.CREATIVE);
                            PlayerWrapper.getWrapper(player).getToggles().toggle(Toggles.VANISH);
                        } else {
                            // Remove Trail
                            if (DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(player)) {
                                DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.remove(player);
                                player.sendMessage(ChatColor.GREEN + "Your have disabled your trail.");
                            }
                            
                            PetUtils.removePet(player);

                            // Make the user invisible / hidden.
                            GameAPI._hiddenPlayers.add(player);
                            player.setCustomNameVisible(false);
                            Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(player));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
                            player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "You are now hidden.");
                            GameAPI.sendNetworkMessage("vanish", player.getUniqueId().toString(), "true");
                            player.setGameMode(GameMode.SPECTATOR);
                            PlayerWrapper.getWrapper(player).getToggles().toggle(Toggles.VANISH);
                        }
                        break;

                    case 1: // Allow Fight
                        GamePlayer gp = GameAPI.getGamePlayer(player);
                        if (gp == null) break;
                        // invert invulnerable flag

                        boolean newVal = !gp.isInvulnerable();
                        gp.setInvulnerable(newVal);
                        //If we are invunerable = true, then targetable = false.
                        gp.setTargettable(!newVal);

                        if (!gp.isTargettable())
                            EntityAPI.untargetEntity(player, 20);

                        Utils.sendCenteredMessage(player, ChatColor.AQUA.toString() + ChatColor.BOLD + "GM " +
                                "INVINCIBILITY - " + (gp.isInvulnerable() ? ChatColor.GREEN.toString() + ChatColor
                                .BOLD + "ENABLED" : ChatColor.RED.toString() + ChatColor.BOLD + "DISABLED"));
                        break;

                    case 2: // Toggle Stream Mode
                        gp = GameAPI.getGamePlayer(player);
                        if (gp == null) break;

                        // Change the status.
                        gp.setStreamMode(!gp.isStreamMode());

                        // Update Permission
                        player.performCommand("ncp notify " + (gp.isStreamMode() ? "off" : "on"));

                        // Clear Immediate Chat
                        for (int i = 0; i < 50; i++) {
                            player.sendMessage("");
                        }

                        // Prompt user about the change.
                        if (gp.isStreamMode()) {
                            player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Stream Mode - ENABLED");
                        } else {
                            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Stream Mode - DISABLED");
                        }

                        break;

                    default:
                        break;
                }

                PlayerMenus.openGameMasterTogglesMenu(player);
                break;
            case "Head Game Master Toggles":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

                switch (slot) {
                    case 0: // Game Master Extended Permissions
                        DungeonRealms.getInstance().isGMExtendedPermissions = !DungeonRealms.getInstance().isGMExtendedPermissions;
                        if (DungeonRealms.getInstance().isGMExtendedPermissions) {
                            player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Game Master Extended Permissions - ENABLED");
                        } else {
                            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Game Master Extended Permissions - DISABLED");
                        }
                        break;
                    default:
                        break;
                }

                PlayerMenus.openHeadGameMasterTogglesMenu(player);
                break;
        }
    }
}