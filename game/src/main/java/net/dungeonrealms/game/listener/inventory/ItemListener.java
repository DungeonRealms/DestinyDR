package net.dungeonrealms.game.listener.inventory;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.miscellaneous.Cooldown;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.types.pets.EnumPets;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import net.dungeonrealms.game.world.entities.utils.PetUtils;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kieran on 9/18/2015.
 */
public class ItemListener implements Listener {
    /**
     * Used to handle dropping a soulbound, untradeable, or
     * permanently untradeable item.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        if (!GameAPI.isItemDroppable(item)) { //Realm Portal, Character Journal.
            event.setCancelled(true);
            event.getItemDrop().remove();
        } else if (!GameAPI.isItemTradeable(item)) {
            net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tag = nmsItem.getTag();
            assert tag != null;
            event.getItemDrop().remove();
            p.sendMessage(ChatColor.GRAY + "This item was " + ChatColor.ITALIC + "untradeable" + ChatColor.GRAY + ", " + "so it has " + ChatColor.UNDERLINE + "vanished.");
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.6F, 0.2F);
        }
    }

    /**
     * Handles player clicking with a teleportation item
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseTeleportItem(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player player = event.getPlayer();
        if (player.getEquipment().getItemInMainHand() == null || player.getEquipment().getItemInMainHand().getType() != Material.BOOK)
            return;

        if (GameAPI.getGamePlayer(player) != null && GameAPI.getGamePlayer(player).isJailed()) {
            player.sendMessage(ChatColor.RED + "You have been jailed.");
            return;
        }

        ItemStack itemStack = player.getEquipment().getItemInMainHand();
        if (!(CombatLog.isInCombat(event.getPlayer()))) {
            if (TeleportAPI.isPlayerCurrentlyTeleporting(player.getUniqueId())) {
                player.sendMessage("You cannot restart a teleport during a cast!");
                return;
            }
            if (TeleportAPI.isTeleportBook(itemStack)) {

                if (!player.getLocation().getWorld().equals(Bukkit.getWorlds().get(0))) {
                    player.sendMessage("You can only use teleport books in the main world.");
                    return;
                }

                net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
                if (TeleportAPI.canTeleportToLocation(player, nmsItem.getTag())) {


                    Teleportation.getInstance().teleportPlayer(player.getUniqueId(), Teleportation.EnumTeleportType.TELEPORT_BOOK, nmsItem.getTag());
                    if (player.getEquipment().getItemInMainHand().getAmount() == 1) {
                        player.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
                    } else {
                        player.getEquipment().getItemInMainHand().setAmount((player.getEquipment().getItemInMainHand().getAmount() - 1));
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You cannot teleport to Safe Zones while Chaotic!");
                }
            } else {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "This item cannot be used to Teleport!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You are in combat! " + "(" + ChatColor.UNDERLINE + CombatLog.COMBAT.get(player) + "s" + ChatColor.RED + ")");
        }
    }

    /**
     * Handles Right Click of Character Journal
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseMap(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player p = event.getPlayer();
        if (p.getEquipment().getItemInMainHand() == null || p.getEquipment().getItemInMainHand().getType() != Material.EMPTY_MAP)
            return;
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getEquipment().getItemInMainHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (tag.hasKey("type")) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerUsePortalRune(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
                || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) return;
        Player p = event.getPlayer();
        if (p.getEquipment().getItemInMainHand() == null || p.getEquipment().getItemInMainHand().getType() != Material.NETHER_STAR)
            return;
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getEquipment().getItemInMainHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (tag.hasKey("realmPortalRune") && !(tag.getString("realmPortalRune").equalsIgnoreCase("true"))) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {

            if (Cooldown.hasCooldown(event.getPlayer().getUniqueId())) return;
            Cooldown.addCooldown(event.getPlayer().getUniqueId(), 1000);

            if (p.isSneaking()) {
                if (!GameAPI.isInWorld(p, Realms.getInstance().getRealmWorld(p.getUniqueId()))) {
                    p.sendMessage(ChatColor.RED + "You must be inside your realm to modify its size.");
                    return;
                }

                int tier = Realms.getInstance().getRealmTier(p.getUniqueId());

                if (tier >= 7) {
                    p.sendMessage(ChatColor.RED + "You have upgraded your realm to it's final tier");
                    return;
                }

                p.sendMessage("");
                p.sendMessage(ChatColor.DARK_GRAY + "           *** " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Realm Upgrade Confirmation"
                        + ChatColor.DARK_GRAY + " ***");
                p.sendMessage(ChatColor.DARK_GRAY + "FROM Tier " + ChatColor.LIGHT_PURPLE + tier + ChatColor.DARK_GRAY + " TO " + ChatColor.LIGHT_PURPLE
                        + (tier + 1));
                p.sendMessage(ChatColor.DARK_GRAY + "Upgrade Cost: " + ChatColor.LIGHT_PURPLE + "" + Realms.getInstance().getRealmUpgradeCost(tier + 1) + " Gem(s) from your bank");
                p.sendMessage("");
                p.sendMessage(ChatColor.GRAY + "Enter '" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "CONFIRM" + ChatColor.GRAY + "' to confirm realm upgrade.");
                p.sendMessage("");
                p.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "WARNING:" + ChatColor.RED + " Realm upgrades are " + ChatColor.BOLD + ChatColor.RED + "NOT"
                        + ChatColor.RED + " reversible or refundable. Type 'cancel' to void this upgrade request.");
                p.sendMessage("");


                Chat.listenForMessage(p, confirmation -> {
                    Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
                        if (confirmation.getMessage().equalsIgnoreCase("cancel")) {
                            p.sendMessage(ChatColor.RED + "Realm upgrade cancel");
                            return;
                        }

                        if (confirmation.getMessage().equalsIgnoreCase("confirm")) {

                            int balance = (Integer) DatabaseAPI.getInstance().getData(EnumData.GEMS, p.getUniqueId());
                            if (balance < Realms.getInstance().getRealmUpgradeCost(tier + 1)) {
                                p.sendMessage(ChatColor.RED + "You do not have enough GEM(s) in your bank to purchase this upgrade. Upgrade cancelled.");
                                p.sendMessage(ChatColor.RED + "COST: " + Realms.getInstance().getRealmUpgradeCost(tier + 1) + " Gem(s)");
                                return;
                            }

                            DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.GEMS, balance, false, doAfter -> Realms.getInstance().upgradeRealm(p));
                        }
                    });
                }, null);
                return;
            }

            if (GameAPI.isInWorld(p, Realms.getInstance().getRealmWorld(p.getUniqueId()))) {
                Location newLocation = event.getClickedBlock().getLocation().clone().add(0, 2, 0);

                if (GameAPI.isMaterialNearby(newLocation.clone().getBlock(), 3, Material.LADDER) || GameAPI.isMaterialNearby(newLocation.clone().getBlock(), 5, Material.ENDER_CHEST)) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You cannot place a realm portal here!");
                    return;
                }

                Realms.getInstance().setRealmSpawn(event.getPlayer().getUniqueId(), newLocation);
                event.setCancelled(true);
                return;
            }

            if (GameAPI.getRegionName(p.getLocation()).equalsIgnoreCase("tutorial_island")) {
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED
                        + " open a portal to your realm until you have completed Tutorial Island.");
                return;
            }

            if (event.getClickedBlock() != null) {
                if (Realms.getInstance().canPlacePortal(p, event.getClickedBlock().getLocation()))
                    Realms.getInstance().loadRealm(p, () -> Realms.getInstance().openRealmPortal(p, event.getClickedBlock().getLocation()));
            }

            event.setCancelled(true);
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {

            if (event.getPlayer().isSneaking())
                return;

            if (!GameAPI.isInWorld(p, Realms.getInstance().getRealmWorld(p.getUniqueId()))) {
                event.getPlayer().sendMessage(ChatColor.RED + "You must be in your realm to open the realm material store.");
                event.setCancelled(true);
                return;
            }

            // OPENS STORE //
            Realms.getInstance().openRealmMaterialStore(p);
            event.setCancelled(true);
        }
    }


    /**
     * Handles Right Click of Character Journal
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseCharacterJournal(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player p = event.getPlayer();
        if (p.getEquipment().getItemInMainHand() == null || p.getEquipment().getItemInMainHand().getType() != Material.WRITTEN_BOOK)
            return;
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getEquipment().getItemInMainHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (!tag.hasKey("journal")) return;
        if (tag.hasKey("journal") && !(tag.getString("journal").equalsIgnoreCase("true"))) return;
        p.getInventory().setItem(p.getInventory().getHeldItemSlot(), ItemManager.createCharacterJournal(p));
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuildBannerEquip(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        Player p = event.getPlayer();
        if (p.getInventory().getItemInMainHand() == null || p.getInventory().getItemInMainHand().getType() != Material.BANNER)
            return;
        if (!p.getInventory().getItemInMainHand().hasItemMeta()) return;
        if (p.getInventory().getItemInMainHand().getItemMeta().getDisplayName() == null) return;
        if (!p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("Guild banner")) return;

        String guildName = p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().substring(2).replace("'s Guild banner", "").replaceAll("\\s", "").toLowerCase();

        final ItemStack banner = p.getInventory().getItemInMainHand();

        p.getInventory().setItemInMainHand(p.getInventory().getHelmet());
        p.getInventory().setHelmet(banner);

        GuildDatabaseAPI.get().doesGuildNameExist(guildName, exists -> {
            if (exists && GuildDatabaseAPI.get().getGuildOf(p.getUniqueId()).equals(guildName)) {
                Achievements.getInstance().giveAchievement(p.getUniqueId(), Achievements.EnumAchievements.GUILD_REPESENT);
                String motd = GuildDatabaseAPI.get().getMotdOf(guildName);

                if (!motd.isEmpty())
                    p.sendMessage(ChatColor.GRAY + "\"" + ChatColor.AQUA + motd + ChatColor.GRAY + "\"");
            }
        });

        event.setCancelled(true);
    }

    /**
     * Handles player right clicking a stat reset book
     *
     * @param event
     * @since 1.0
     */

    @EventHandler(priority = EventPriority.MONITOR)
    public void useEcashItem(PlayerInteractEvent event) {
        if (event.getItem() != null) {
            Player player = event.getPlayer();
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(event.getItem());
            if (event.getItem().getType() == Material.ENCHANTED_BOOK) {
                if (nms.hasTag() && nms.getTag().hasKey("retrainingBook")) {
                    event.getPlayer().sendMessage(ChatColor.GREEN + "Reset stat points? Type 'yes' or 'y' to confirm.");
                    Chat.listenForMessage(event.getPlayer(), chat -> {
                        if (chat.getMessage().equalsIgnoreCase("Yes") || chat.getMessage().equalsIgnoreCase("y")) {
                            if (event.getItem().getAmount() > 1) {
                                event.getItem().setAmount(event.getItem().getAmount() - 1);
                            } else {
                                event.getPlayer().getInventory().remove(event.getItem());
                            }
                            GameAPI.getGamePlayer(event.getPlayer()).getStats().unallocateAllPoints();
                            event.getPlayer().sendMessage(ChatColor.YELLOW + "All Stat Points have been unallocated!");
                        }
                    }, p -> p.sendMessage(ChatColor.RED + "Action cancelled."));
                }
            }
            if (event.getItem().getType() == Material.FIREWORK) {
                if (nms.hasTag() && nms.getTag().hasKey("globalMessenger")) {
                    event.getPlayer().sendMessage("");
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "Please enter the message you'd like to send to " + ChatColor.UNDERLINE + "all servers" + ChatColor.YELLOW
                            + " -- think before you speak!");
                    event.getPlayer().sendMessage(ChatColor.GRAY + "Type 'cancel' (no apostrophes) to cancel this and get your Global Messenger back.");
                    event.getPlayer().sendMessage("");
                    Chat.listenForMessage(event.getPlayer(), chat -> {
                        if (chat.getMessage().equalsIgnoreCase("cancel")) {
                            event.getPlayer().sendMessage(ChatColor.RED + "Global Messenger - " + ChatColor.BOLD + "CANCELLED");
                            return;
                        }

                        String msg = chat.getMessage();
                        if (msg.contains(".com") || msg.contains(".net") || msg.contains(".org") || msg.contains("http://") || msg.contains("www.")) {
                            if (!Rank.isDev(event.getPlayer())) {
                                event.getPlayer().sendMessage(ChatColor.RED + "No " + ChatColor.UNDERLINE + "URL's" + ChatColor.RED + " in your global messages please!");
                                return;
                            }
                        }

                        if (event.getItem().getAmount() > 1) {
                            event.getItem().setAmount(event.getItem().getAmount() - 1);
                        } else {
                            event.getPlayer().getInventory().remove(event.getItem());
                        }


                        final String fixedMessage = Chat.getInstance().checkForBannedWords(msg);

                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Alert");
                        out.writeUTF(" \n" + ChatColor.GOLD.toString() + ChatColor.BOLD + ">>" + ChatColor.GOLD + " (" + DungeonRealms.getInstance().shardid + ") " + GameChat.getPreMessage(event.getPlayer()) + ChatColor.GOLD + fixedMessage + "\n ");

                        event.getPlayer().sendPluginMessage(DungeonRealms.getInstance(), "BungeeCord", out.toByteArray());
                    }, p -> p.sendMessage(ChatColor.RED + "Action cancelled."));
                }
            } else if (event.getItem().getType() == Material.ENDER_CHEST) {
                if (nms.hasTag() && nms.getTag().hasKey("type")) {
                    if (nms.getTag().getString("type").equalsIgnoreCase("upgrade")) {
                        if (BankMechanics.storage.get(player.getUniqueId()).collection_bin != null) {
                            player.sendMessage(ChatColor.RED + "You have item(s) waiting in your collection bin.");
                            player.sendMessage(ChatColor.GRAY + "Access your bank chest to claim them.");
                            return;
                        }
                        int invlvl = (int) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_LEVEL, player.getUniqueId());
                        if (invlvl >= 6) {
                            player.sendMessage(ChatColor.RED + "Sorry you've reached the current maximum storage size!");
                            return;
                        }
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.INVENTORY_LEVEL, invlvl + 1, true);
                        BankMechanics.getInstance().getStorage(player.getUniqueId()).update();
                        if (event.getPlayer().getEquipment().getItemInMainHand().getAmount() == 1) {
                            event.getPlayer().getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
                        } else {
                            ItemStack item = event.getPlayer().getEquipment().getItemInMainHand();
                            item.setAmount(item.getAmount() - 1);
                            event.getPlayer().getEquipment().setItemInMainHand(item);
                        }
                        event.getPlayer().sendMessage(ChatColor.YELLOW + "Your banks storage has been increased by 9 slots.");
                    }
                }
            } else if (event.getItem().getType() == Material.DIAMOND) {
                if (nms.hasTag() && nms.getTag().hasKey("buff") && nms.getTag().getString("buff").equals("loot")) {
                    event.setCancelled(true);
                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setUseItemInHand(Event.Result.DENY);
                    player.sendMessage("");
                    Utils.sendCenteredMessage(player, ChatColor.DARK_GRAY + "***" + ChatColor.GREEN.toString() +
                            ChatColor.BOLD + "GLOBAL LOOT BUFF CONFIRMATION" + ChatColor.DARK_GRAY + "***");
                    player.sendMessage(ChatColor.GOLD
                            + "Are you sure you want to use this item? It will apply a 20% buff to all character level XP gains across all servers for 30 minutes. This cannot be undone once it has begun.");
                    if (DonationEffects.getInstance().getActiveLootBuff() != null)
                        player.sendMessage(ChatColor.RED + "NOTICE: There is an ongoing loot buff, so your loot buff " +
                                "will be queued to be activated immediately after this one if you confirm. Cancel if " +
                                "you do not wish to queue yours.");
                    player.sendMessage("");
                    player.sendMessage(ChatColor.GRAY + "Type '" + ChatColor.GREEN + "Y" + ChatColor.GRAY + "' to confirm, or any other message to cancel.");
                    Chat.listenForMessage(player, e -> {
                        if (e.getMessage().equalsIgnoreCase("y")) {
                            event.getPlayer().getInventory().remove(event.getItem());
                            GameAPI.sendNetworkMessage("lootBuff", "1800", "20", GameChat.getFormattedName(player), DungeonRealms.getInstance().bungeeName);
                        } else {
                            player.sendMessage(ChatColor.RED + "Global Loot Buff - Cancelled");
                        }
                    }, p -> p.sendMessage(ChatColor.RED + "Global Loot Buff - CANCELLED"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerDrinkPotionMainHand(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack potion;
        net.minecraft.server.v1_9_R2.ItemStack nmsItem;
        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            //Drinking from Offhand.
            potion = player.getInventory().getItemInOffHand();
            nmsItem = CraftItemStack.asNMSCopy(potion);
            if (nmsItem == null || nmsItem.getTag() == null) return;
            if (!nmsItem.getTag().hasKey("type")) return;
            if (nmsItem.getTag().getString("type").equalsIgnoreCase("healthPotion")) {
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);
                if (HealthHandler.getInstance().getPlayerHPLive(player) < HealthHandler.getInstance().getPlayerMaxHPLive(player)) {
                    player.getInventory().setItemInOffHand(null);
                    player.updateInventory();
                    player.getInventory().setItemInOffHand(findPlayerNextPotion(player));
                    player.updateInventory();
                    HealthHandler.getInstance().healPlayerByAmount(event.getPlayer(), nmsItem.getTag().getInt("healAmount"));
                } else {
                    player.sendMessage(ChatColor.RED + "You are already at full HP!");
                }
            }
        } else if (player.getInventory().getItemInOffHand() == null || player.getInventory().getItemInOffHand().getType() == Material.AIR) {
            //Drinking from Mainhand.
            potion = player.getInventory().getItemInMainHand();
            nmsItem = CraftItemStack.asNMSCopy(potion);
            if (nmsItem == null || nmsItem.getTag() == null) return;
            if (!nmsItem.getTag().hasKey("type")) return;
            if (nmsItem.getTag().getString("type").equalsIgnoreCase("healthPotion")) {
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);
                if (HealthHandler.getInstance().getPlayerHPLive(player) < HealthHandler.getInstance().getPlayerMaxHPLive(player)) {
                    player.getInventory().setItemInMainHand(null);
                    player.updateInventory();
                    player.getInventory().setItemInMainHand(findPlayerNextPotion(player));
                    player.updateInventory();
                    HealthHandler.getInstance().healPlayerByAmount(event.getPlayer(), nmsItem.getTag().getInt("healAmount"));
                } else {
                    player.sendMessage(ChatColor.RED + "You are already at full HP!");
                }
            }
        } else {
            //Have a potion in both hands...
            ItemStack itemUsed = event.getItem();
            if (event.getItem() == null || event.getItem().getType() == Material.AIR) return;
            potion = player.getInventory().getItemInMainHand();
            nmsItem = CraftItemStack.asNMSCopy(potion);
            ItemStack potionOffhand = player.getInventory().getItemInOffHand();
            net.minecraft.server.v1_9_R2.ItemStack nmsOffhand = CraftItemStack.asNMSCopy(potionOffhand);
            if (AntiCheat.getInstance().getUniqueEpochIdentifier(itemUsed) == null) return;
            if (AntiCheat.getInstance().getUniqueEpochIdentifier(potion) == null && AntiCheat.getInstance().getUniqueEpochIdentifier(potionOffhand) == null)
                return;
            if (AntiCheat.getInstance().getUniqueEpochIdentifier(itemUsed).equalsIgnoreCase(AntiCheat.getInstance().getUniqueEpochIdentifier(potion))) {
                //Drinking their Mainhand potion
                if (nmsItem == null || nmsItem.getTag() == null) return;
                if (!nmsItem.getTag().hasKey("type")) return;
                if (nmsItem.getTag().getString("type").equalsIgnoreCase("healthPotion")) {
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setUseInteractedBlock(Event.Result.DENY);
                    if (HealthHandler.getInstance().getPlayerHPLive(player) < HealthHandler.getInstance().getPlayerMaxHPLive(player)) {
                        player.getInventory().setItemInMainHand(null);
                        player.updateInventory();
                        player.getInventory().setItemInMainHand(findPlayerNextPotion(player));
                        player.updateInventory();
                        HealthHandler.getInstance().healPlayerByAmount(event.getPlayer(), nmsItem.getTag().getInt("healAmount"));
                    } else {
                        player.sendMessage(ChatColor.RED + "You are already at full HP!");
                    }
                }
            } else {
                //Drinking their Offhand potion
                if (nmsOffhand == null || nmsOffhand.getTag() == null) return;
                if (!nmsOffhand.getTag().hasKey("type")) return;
                if (nmsOffhand.getTag().getString("type").equalsIgnoreCase("healthPotion")) {
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setUseInteractedBlock(Event.Result.DENY);
                    if (HealthHandler.getInstance().getPlayerHPLive(player) < HealthHandler.getInstance().getPlayerMaxHPLive(player)) {
                        player.getInventory().setItemInOffHand(null);
                        player.updateInventory();
                        player.getInventory().setItemInOffHand(findPlayerNextPotion(player));
                        player.updateInventory();
                        HealthHandler.getInstance().healPlayerByAmount(event.getPlayer(), nmsOffhand.getTag().getInt("healAmount"));
                    } else {
                        player.sendMessage(ChatColor.RED + "You are already at full HP!");
                    }
                }
            }
        }
    }

    public ItemStack findPlayerNextPotion(Player player) {
        ItemStack nextPot = null;
        net.minecraft.server.v1_9_R2.ItemStack nmsPot;
        int slotCount = -1;
        for (ItemStack stack : player.getInventory().getContents()) {
            slotCount++;
            if (stack == null || stack.getType() == Material.AIR) {
                continue;
            }
            if (stack.getType() != Material.POTION) {
                continue;
            }
            nmsPot = CraftItemStack.asNMSCopy(stack);
            if (nmsPot.hasTag() && nmsPot.getTag() != null && nmsPot.getTag().hasKey("type")) {
                if (nmsPot.getTag().getString("type").equalsIgnoreCase("healthPotion")) {
                    nextPot = stack;
                    player.getInventory().setItem(slotCount, new ItemStack(Material.AIR));
                    break;
                }
            }
        }
        return nextPot;
    }

    private boolean performPreFoodChecks(Player player) {
        if (CombatLog.isInCombat(player)) {
            player.sendMessage(ChatColor.RED + "You cannot eat this while in combat!");
            player.updateInventory();
            return false;
        }
        if (player.hasMetadata("FoodRegen")) {
            player.sendMessage(ChatColor.RED + "You cannot eat this while you have another food bonus active!");
            player.updateInventory();
            return false;
        }
        if (player.isSprinting()) {
            player.sendMessage(ChatColor.RED + "You cannot eat this while sprinting!");
            player.updateInventory();
            return false;
        }
        return true;
    }

    private void healPlayerTask(Player player, int amount) {
        player.setMetadata("FoodRegen", new FixedMetadataValue(DungeonRealms.getInstance(), true));
        int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            if (!player.isSprinting() && HealthHandler.getInstance().getPlayerHPLive(player) < HealthHandler.getInstance().getPlayerMaxHPLive(player) && !CombatLog.isInCombat(player)) {
                HealthHandler.getInstance().healPlayerByAmount(player, amount);
            } else {
                if (player.hasMetadata("FoodRegen")) {
                    player.removeMetadata("FoodRegen", DungeonRealms.getInstance());
                    player.sendMessage(ChatColor.RED + "Healing Cancelled!");
                }
            }
        }, 0L, 20L);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            Bukkit.getScheduler().cancelTask(taskID);
            if (player.hasMetadata("FoodRegen")) {
                player.removeMetadata("FoodRegen", DungeonRealms.getInstance());
            }
        }, 310L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack foodItem;
        net.minecraft.server.v1_9_R2.ItemStack nmsItem;
        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            //Eating from Offhand.
            foodItem = player.getInventory().getItemInOffHand();
            nmsItem = CraftItemStack.asNMSCopy(foodItem);
            if (nmsItem == null || nmsItem.getTag() == null) return;
            if (!nmsItem.getTag().hasKey("type")) return;
            if (nmsItem.getTag().getString("type").equalsIgnoreCase("healingFood")) {
                performPreFoodChecks(player);
                event.setCancelled(true);
                if (foodItem.getAmount() == 1) {
                    player.getInventory().setItemInOffHand(null);
                } else {
                    foodItem.setAmount(foodItem.getAmount() - 1);
                    player.getInventory().setItemInOffHand(foodItem);
                }
                player.updateInventory();
                player.setFoodLevel(player.getFoodLevel() + 6);
                if (HealthHandler.getInstance().getPlayerHPLive(player) < HealthHandler.getInstance().getPlayerMaxHPLive(player)) {
                    player.sendMessage(ChatColor.GREEN + "Healing " + ChatColor.BOLD + nmsItem.getTag().getInt("healAmount") + ChatColor.GREEN + "HP/s for 15 Seconds!");
                    healPlayerTask(player, nmsItem.getTag().getInt("healAmount"));
                } else {
                    player.sendMessage(ChatColor.YELLOW + "You are already at full HP, however, your hunger has been satisfied.");
                }
            }
        } else if (player.getInventory().getItemInOffHand() == null || player.getInventory().getItemInOffHand().getType() == Material.AIR) {
            //Eating from Mainhand.
            foodItem = player.getInventory().getItemInMainHand();
            nmsItem = CraftItemStack.asNMSCopy(foodItem);
            if (nmsItem == null || nmsItem.getTag() == null) return;
            if (!nmsItem.getTag().hasKey("type")) return;
            if (nmsItem.getTag().getString("type").equalsIgnoreCase("healingFood")) {
                performPreFoodChecks(player);
                event.setCancelled(true);
                if (foodItem.getAmount() == 1) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    foodItem.setAmount(foodItem.getAmount() - 1);
                    player.getInventory().setItemInMainHand(foodItem);
                }
                player.updateInventory();
                player.setFoodLevel(player.getFoodLevel() + 6);
                if (HealthHandler.getInstance().getPlayerHPLive(player) < HealthHandler.getInstance().getPlayerMaxHPLive(player)) {
                    player.sendMessage(ChatColor.GREEN + "Healing " + ChatColor.BOLD + nmsItem.getTag().getInt("healAmount") + ChatColor.GREEN + "HP/s for 15 Seconds!");
                    healPlayerTask(player, nmsItem.getTag().getInt("healAmount"));
                } else {
                    player.sendMessage(ChatColor.YELLOW + "You are already at full HP, however, your hunger has been satisfied.");
                }
            }
        } else {
            //Have food in both hands...
            if (event.getItem() == null || event.getItem().getType() == Material.AIR) return;
            foodItem = player.getInventory().getItemInMainHand();
            nmsItem = CraftItemStack.asNMSCopy(foodItem);
            if (nmsItem == null || nmsItem.getTag() == null) return;
            if (!nmsItem.getTag().hasKey("type")) return;
            if (nmsItem.getTag().getString("type").equalsIgnoreCase("healingFood")) {
                performPreFoodChecks(player);
                event.setCancelled(true);
                if (foodItem.getAmount() == 1) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    foodItem.setAmount(foodItem.getAmount() - 1);
                    player.getInventory().setItemInMainHand(foodItem);
                }
                player.updateInventory();
                player.setFoodLevel(player.getFoodLevel() + 6);
                if (HealthHandler.getInstance().getPlayerHPLive(player) < HealthHandler.getInstance().getPlayerMaxHPLive(player)) {
                    player.sendMessage(ChatColor.GREEN + "Healing " + ChatColor.BOLD + nmsItem.getTag().getInt("healAmount") + ChatColor.GREEN + "HP/s for 15 Seconds!");
                    healPlayerTask(player, nmsItem.getTag().getInt("healAmount"));
                } else {
                    player.sendMessage(ChatColor.YELLOW + "You are already at full HP, however, your hunger has been satisfied.");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPotionSplash(PotionSplashEvent event) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = (CraftItemStack.asNMSCopy(event.getPotion().getItem()));
        if (nmsItem != null && nmsItem.getTag() != null) {
            if (nmsItem.getTag().hasKey("type") && nmsItem.getTag().getString("type").equalsIgnoreCase("splashHealthPotion")) {
                event.setCancelled(true);
                for (LivingEntity entity : event.getAffectedEntities()) {
                    if (!GameAPI.isPlayer(entity)) {
                        continue;
                    }
                    HealthHandler.getInstance().healPlayerByAmount((Player) entity, nmsItem.getTag().getInt("healAmount"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void petRename(PlayerInteractEntityEvent event) {
        if (!event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        Player player = event.getPlayer();
        if (event.getRightClicked() instanceof Player) return;
        if (player.getEquipment().getItemInMainHand() == null || player.getEquipment().getItemInMainHand().getType() == Material.AIR)
            return;
        if (player.getEquipment().getItemInMainHand().getType() != Material.NAME_TAG) return;
        event.setCancelled(true);
        player.updateInventory();
        if (!EntityAPI.hasPetOut(player.getUniqueId())) return;
        if (EntityAPI.getPlayerPet(player.getUniqueId()).equals(((CraftEntity) event.getRightClicked()).getHandle())) {
            player.sendMessage(ChatColor.GRAY + "Enter a name for your pet, or type " + ChatColor.RED + ChatColor.UNDERLINE + "cancel" + ChatColor.GRAY + " to end the process.");
            player.closeInventory();
            Chat.listenForMessage(player, newPetName -> {
                if (newPetName.getMessage().equalsIgnoreCase("cancel") || newPetName.getMessage().equalsIgnoreCase("exit")) {
                    player.sendMessage(ChatColor.GRAY + "Pet naming " + ChatColor.RED + ChatColor.UNDERLINE + "CANCELLED.");
                    return;
                }
                Entity pet = EntityAPI.getPlayerPet(player.getUniqueId());
                if (pet == null) {
                    return;
                }

                String inputName = newPetName.getMessage();

                // Name must be below 20 characters
                if (inputName.length() > 20) {
                    player.sendMessage(ChatColor.RED + "Your pet name exceeds the maximum length of 20 characters.");
                    player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "You were " + (inputName.length() - 20) + " characters over the limit.");
                    return;
                }

                if (inputName.contains("@")) {
                    inputName = inputName.replaceAll("@", "_");
                }

                String checkedPetName = Chat.getInstance().checkForBannedWords(inputName);

                String activePet = (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_PET, player.getUniqueId());
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.PETS, activePet, true);
                if (activePet.contains("@")) {
                    activePet = activePet.split("@")[0];
                }
                String newPet = activePet + "@" + checkedPetName;
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.PETS, activePet, true);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.PETS, newPet, true);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_PET, newPet, true);
                ChatColor prefix = ChatColor.WHITE;
                if (Rank.isSubscriber(player)) {
                    String rank = Rank.getInstance().getRank(player.getUniqueId());
                    if (rank.equalsIgnoreCase("sub")) {
                        prefix = ChatColor.GREEN;
                    } else if (rank.equalsIgnoreCase("sub+")) {
                        prefix = ChatColor.GOLD;
                    } else if (rank.equalsIgnoreCase("sub++")) {
                        prefix = ChatColor.DARK_AQUA;
                    }
                }
                if (Rank.isDev(player)) {
                    prefix = ChatColor.AQUA;
                }
                pet.setCustomName(prefix + checkedPetName);
                player.sendMessage(ChatColor.GRAY + "Your pet's name has been changed to " + ChatColor.GREEN + ChatColor.UNDERLINE + checkedPetName + ChatColor.GRAY + ".");
            }, null);
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void playerEatFish(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final Player pl = e.getPlayer();

        ItemStack fish;

        if (pl.getInventory().getItemInMainHand() == null || pl.getInventory().getItemInMainHand().getType() == Material.AIR) {
            //Eating from Offhand.
            fish = pl.getInventory().getItemInOffHand();
            if (Fishing.getInstance().isCustomRawFish(fish)) {
                pl.sendMessage(ChatColor.RED + "You must cook this fish before you can eat it!");
                return;
            }
            if (Fishing.isCustomFish(fish)) {
                e.setUseInteractedBlock(Event.Result.DENY);
                e.setCancelled(true);
                if (fish.getAmount() == 1) {
                    pl.getInventory().setItemInOffHand(null);
                } else {
                    fish.setAmount(fish.getAmount() - 1);
                    pl.getInventory().setItemInOffHand(fish);
                }
                pl.updateInventory();
            } else {
                return;
            }
        } else if (pl.getInventory().getItemInOffHand() == null || pl.getInventory().getItemInOffHand().getType() == Material.AIR) {
            //Eating from Mainhand.
            fish = pl.getInventory().getItemInMainHand();
            if (Fishing.getInstance().isCustomRawFish(fish)) {
                pl.sendMessage(ChatColor.RED + "You must cook this fish before you can eat it!");
                return;
            }
            if (Fishing.isCustomFish(fish)) {
                e.setUseInteractedBlock(Event.Result.DENY);
                e.setCancelled(true);
                if (fish.getAmount() == 1) {
                    pl.getInventory().setItemInMainHand(null);
                } else {
                    fish.setAmount(fish.getAmount() - 1);
                    pl.getInventory().setItemInMainHand(fish);
                }
                pl.updateInventory();
            } else {
                return;
            }
        } else {
            //Have food in both hands...
            if (e.getItem() == null || e.getItem().getType() == Material.AIR) return;
            fish = pl.getInventory().getItemInMainHand();
            if (Fishing.getInstance().isCustomRawFish(fish)) {
                pl.sendMessage(ChatColor.RED + "You must cook this fish before you can eat it!");
                return;
            }
            if (Fishing.isCustomFish(fish)) {
                e.setUseInteractedBlock(Event.Result.DENY);
                e.setCancelled(true);
                if (fish.getAmount() == 1) {
                    pl.getInventory().setItemInMainHand(null);
                } else {
                    fish.setAmount(fish.getAmount() - 1);
                    pl.getInventory().setItemInMainHand(fish);
                }
                pl.updateInventory();
            } else {
                return;
            }
        }

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_BURP, 1F, 1F);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_BURP, 1F, 1.5F), 4L);
        List<String> lore = fish.getItemMeta().getLore();
        int food_to_restore = 0;


        for (String s : lore) {
            if (s.contains("% HUNGER")) {
                double percent = Integer.parseInt(s.substring(s.indexOf("-") + 1, s.indexOf("%")));
                int local_amount = (int) ((percent / 100.0D) * 20D);
                food_to_restore += local_amount;
            }
        }

        int cur_food = pl.getFoodLevel();
        if (cur_food + food_to_restore >= 20) {
            pl.setFoodLevel(20);
            pl.setSaturation(20);
        } else {
            pl.setFoodLevel(cur_food + food_to_restore);
            pl.setSaturation(pl.getSaturation() + food_to_restore);
        }

        for (String s : lore) {
            s = ChatColor.stripColor(s);
            if (s.contains("% HP (instant)")) {
                double percent_to_heal = Double.parseDouble(s.substring(s.indexOf("+") + 1, s.indexOf("%"))) / 100;
                double max_hp = HealthHandler.getInstance().getPlayerMaxHPLive(pl);
                int amount_to_heal = (int) Math.round((percent_to_heal * max_hp));
                double current_hp = HealthHandler.getInstance().getPlayerHPLive(pl);
                if (current_hp + 1 > max_hp) {
                    continue;
                }
                if ((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, pl.getUniqueId())) {
                    pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + amount_to_heal + ChatColor.BOLD + " HP"
                            + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " ["
                            + ((int) current_hp + amount_to_heal) + "/" + (int) max_hp + "HP]");
                }

                if ((current_hp + amount_to_heal) >= max_hp) {
                    pl.setHealth(20);
                    HealthHandler.getInstance().setPlayerHPLive(pl, (int) max_hp);
                } else if (pl.getHealth() <= 19 && ((current_hp + amount_to_heal) < max_hp)) {
                    HealthHandler.getInstance().setPlayerHPLive(pl, HealthHandler.getInstance().getPlayerHPLive(pl) + amount_to_heal);
                    double health_percent = (HealthHandler.getInstance().getPlayerHPLive(pl) + amount_to_heal) / max_hp;
                    double new_health_display = health_percent * 20;
                    if (new_health_display > 19) {
                        if (health_percent >= 1) {
                            new_health_display = 20;
                        } else if (health_percent < 1) {
                            new_health_display = 19;
                        }
                    }
                    if (new_health_display < 1) {
                        new_health_display = 1;
                    }
                    pl.setHealth((int) new_health_display);

                }
            } else if (s.startsWith("REGEN")) {
                double percent_to_regen = Double.parseDouble(s.substring(s.indexOf(" ") + 1, s.indexOf("%"))) / 100.0D;
                int regen_interval = Integer.parseInt(s.substring(s.lastIndexOf(" ") + 1, s.lastIndexOf("s")));
                double max_hp = HealthHandler.getInstance().getPlayerMaxHPLive(pl);

                final int amount_to_regen_per_interval = (int) (max_hp * percent_to_regen) / regen_interval;
                pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "      " + ChatColor.GREEN + amount_to_regen_per_interval + ChatColor.BOLD
                        + " HP/s" + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + regen_interval + "s]");
                GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.HEALTH_REGEN, (float) percent_to_regen);

                pl.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (int) (regen_interval + (regen_interval * 0.25)), 0));
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    pl.removePotionEffect(PotionEffectType.REGENERATION);
                    GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.HEALTH_REGEN, (float) -percent_to_regen);
                    pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "   " + amount_to_regen_per_interval + " HP/s " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, regen_interval * 20L);
            } else if (s.startsWith("SPEED")) {
                String tier_symbol = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                int effect_tier = 0;
                if (tier_symbol.equalsIgnoreCase("II")) {
                    effect_tier = 1;
                }
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, effect_time * 20, effect_tier));
            } else if (s.startsWith("NIGHTVISION")) {
                String tier_symbol = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                int effect_tier = 0;
                if (tier_symbol.equalsIgnoreCase("II")) {
                    effect_tier = 1;
                }
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                pl.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, effect_time * 20, effect_tier));
            } else if (s.contains("ENERGY REGEN")) {
                final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.ENERGY_REGEN, bonus_percent);
                pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "      " + ChatColor.GREEN + bonus_percent + ChatColor.BOLD + " Energy/s"
                        + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.ENERGY_REGEN, -bonus_percent);
                    pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + " +" + bonus_percent + "% Energy " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, effect_time * 20L);
            } else if (s.contains("% DMG")) {
                final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.WeaponAttributeType.DAMAGE, bonus_percent);
                pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + bonus_percent + ChatColor.BOLD + "% DMG"
                        + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.WeaponAttributeType.DAMAGE, -bonus_percent);
                    pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% DMG " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, effect_time * 20L);
            } else if (s.contains("% ARMOR")) {
                final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.ARMOR, bonus_percent);
                pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + bonus_percent + ChatColor.BOLD + "% ARMOR"
                        + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.ARMOR, -bonus_percent);
                    pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% ARMOR " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, effect_time * 20L);
            } else if (s.contains("% BLOCK")) {
                final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.BLOCK, bonus_percent);
                pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + bonus_percent + ChatColor.BOLD + "% BLOCK"
                        + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.BLOCK, -bonus_percent);
                    pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% BLOCK " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, effect_time * 20L);
            } else if (s.contains("% LIFESTEAL")) {
                final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.WeaponAttributeType.LIFE_STEAL, bonus_percent);
                pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + bonus_percent + ChatColor.BOLD + "% LIFESTEAL"
                        + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.WeaponAttributeType.LIFE_STEAL, -bonus_percent);
                    pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% LIFESTEAL " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, effect_time * 20L);
            } else if (s.contains("% CRIT")) {
                final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.WeaponAttributeType.CRITICAL_HIT, bonus_percent);
                pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + bonus_percent + ChatColor.BOLD + "% CRIT"
                        + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    GameAPI.getGamePlayer(pl).changeAttributeValPercentage(Item.WeaponAttributeType.CRITICAL_HIT, -bonus_percent);
                    pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% CRIT " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, effect_time * 20L);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseSpecialItem(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR)) return;
        Player player = event.getPlayer();
        if (player.getEquipment().getItemInMainHand() == null || player.getEquipment().getItemInMainHand().getType() == Material.AIR) {
            return;
        }
        if (player.getEquipment().getItemInMainHand().getType() == Material.SADDLE || player.getEquipment().getItemInMainHand().getType() == Material.EYE_OF_ENDER || player.getEquipment().getItemInMainHand().getType() == Material.NAME_TAG || player.getEquipment().getItemInMainHand().getType() == Material.LEASH) {
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(player.getEquipment().getItemInMainHand());
            NBTTagCompound tag = nmsStack.getTag();
            if (tag == null) return;
            if (!(tag.getString("type").equalsIgnoreCase("important"))) return;
            switch (tag.getString("usage")) {
                case "mount":
                case "mule":
                    if (EntityAPI.hasMountOut(player.getUniqueId())) {
                        Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                        if (entity.isAlive()) {
                            entity.getBukkitEntity().remove();
                        }
                        if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                            DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                        }
                        player.sendMessage(ChatColor.GREEN + "Your mount has been dismissed.");
                        EntityAPI.removePlayerMountList(player.getUniqueId());
                        return;
                    }
                    if (CombatLog.isInCombat(player)) {
                        player.sendMessage(ChatColor.RED + "You cannot summon a mount while in combat!");
                        return;
                    }
                    if (player.getEyeLocation().getBlock().getType() != Material.AIR) {
                        player.sendMessage(ChatColor.RED + "You cannot summon a mount here!");
                        return;
                    }
                    String mountType = tag.getString("usage").equals("mule") ? "MULE" : (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_MOUNT, player.getUniqueId());
                    if (mountType == null || mountType.equals("")) {
                        player.sendMessage(ChatColor.RED + "You don't have an active mount, please enter the mounts section in your profile to set one.");
                        player.closeInventory();
                        return;
                    }
                    if (tag.getString("usage").equals("mule")) {
                        List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, player.getUniqueId());
                        if (!playerMounts.contains("MULE")) {
                            player.sendMessage(ChatColor.RED + "Purchase a storage mule from the Animal Tamer.");
                            return;
                        }
                    }
                    player.sendMessage(ChatColor.GREEN + "Your mount is being summoned into this world!");
                    final int[] count = {0};
                    Location startingLocation = player.getLocation();
                    final boolean[] cancelled = {false};
                    int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                        if (!EntityAPI.hasMountOut(player.getUniqueId())) {
                            if (player.getLocation().distanceSquared(startingLocation) <= 4) {
                                if (!CombatLog.isInCombat(player)) {
                                    if (!cancelled[0]) {
                                        if (count[0] < 3) {
                                            count[0]++;
                                            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPELL, player.getLocation(), 1F, 0F, 1F, .1F, 40);
                                        } else {
                                            MountUtils.spawnMount(player.getUniqueId(), mountType, (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_MOUNT_SKIN, player.getUniqueId()));
                                        }
                                    }
                                } else {
                                    if (!cancelled[0]) {
                                        cancelled[0] = true;
                                        count[0] = 0;
                                        player.sendMessage(ChatColor.RED + "Combat has cancelled your mount summoning!");
                                    }
                                }
                            } else {
                                if (!cancelled[0]) {
                                    cancelled[0] = true;
                                    count[0] = 0;
                                    player.sendMessage(ChatColor.RED + "Movement has cancelled your mount summoning!");
                                }
                            }
                        }
                    }, 0L, 20L);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Bukkit.getScheduler().cancelTask(taskID), 65L);
                    break;
                case "pet":
                    if (EntityAPI.hasPetOut(player.getUniqueId())) {
                        Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                        if (entity.isAlive()) {
                            entity.getBukkitEntity().remove();
                        }
                        if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                            DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                        }
                        player.sendMessage(ChatColor.GREEN + "Your pet has been dismissed.");
                        EntityAPI.removePlayerPetList(player.getUniqueId());
                        return;
                    }
                    String petType = (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_PET, player.getUniqueId());
                    if (petType == null || petType.equals("")) {
                        player.sendMessage(ChatColor.RED + "You don't have an active pet, please enter the pets section in your profile to set one.");
                        player.closeInventory();
                        return;
                    }
                    String petName;
                    if (petType.contains("@")) {
                        petName = petType.split("@")[1];
                        petType = petType.split("@")[0];
                    } else {
                        petName = EnumPets.getByName(petType).getDisplayName();
                    }
                    PetUtils.spawnPet(player.getUniqueId(), petType, petName);
                    player.sendMessage(ChatColor.GREEN + "Your pet has been summoned.");
                    break;
                case "trail":
                    if (DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(player)) {
                        DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.remove(player);
                        player.sendMessage(ChatColor.GREEN + "Your have disabled your trail.");
                        return;
                    }
                    String trailType = (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_TRAIL, player.getUniqueId());
                    if (trailType == null || trailType.equals("")) {
                        player.sendMessage(ChatColor.RED + "You don't have an active trail, please enter the trails section in your profile to set one.");
                        player.closeInventory();
                        return;
                    }
                    DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.put(player, ParticleAPI.ParticleEffect.getByName(trailType));
                    player.sendMessage(ChatColor.GREEN + "Your active trail has been activated.");
                    break;
            }
        }
    }
}
