package net.dungeonrealms.game.listener.inventory;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.common.game.util.Cooldown;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveOpenJournal;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.glow.GlowAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by Kieran on 9/18/2015.
 */
public class ItemListener implements Listener {


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        //Party handler here.
        Optional<Party> party = Affair.getInstance().getParty(event.getPlayer());
        if (party != null && party.isPresent()) {
            Party part = party.get();
            Affair.getInstance().handlePartyPickup(event, part);
        }
    }

    /**
     * Makes Uncommon+ Items glow
     */
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        this.applyRarityGlow(event.getEntity());
    }

    private void applyRarityGlow(Item entity) {
        ItemStack item = entity.getItemStack();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
            return;
        List<String> lore = item.getItemMeta().getLore();
        for (int i = 1; i < ItemRarity.values().length; i++) {
            ItemRarity rarity = ItemRarity.getById(i);
            for (String s : lore) {
                if (s.contains(rarity.getName())) {
                	Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
                		//Filter out players who have toggle glow off.
                		List<Player> sendTo = GameAPI.getNearbyPlayers(entity.getLocation(), 100, true).stream().filter(p -> {
                			return (boolean)DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOW, p.getUniqueId());
                		}).collect(Collectors.toList());
                		//Set the item as glowing.
                		GlowAPI.setGlowing(entity, GlowAPI.Color.valueOf(rarity.getColor().name()), sendTo);
                	});
                	return;
                }
            }
        }
    }

    /**
     * Used to handle dropping a soulbound, untradeable, or
     * permanently untradeable item.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        Player p = event.getPlayer();
        GamePlayer gp = GameAPI.getGamePlayer(p);
        if(gp != null && !gp.isAbleToDrop())
        	return;
        ItemStack item = event.getItemDrop().getItemStack();

        if (GameAPI.isItemSoulbound(item)) {
            //event.setCancelled(true);
            event.getItemDrop().remove();
            p.sendMessage(ChatColor.RED + "Are you sure you want to " + ChatColor.UNDERLINE + "destroy" + ChatColor.RED + " this Soulbound item? ");
            p.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + ChatColor.BOLD + "Y" + ChatColor.GRAY + " or " + ChatColor.DARK_RED + ChatColor.BOLD + "N" + ChatColor.GRAY + " to confirm.");
            p.playSound(p.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1, 1.2F);
            //p.getInventory().remove(item);
            if(p.getItemOnCursor().equals(item))
            	p.setItemOnCursor(null);
            Chat.listenForMessage(p, (message) -> {
                if (message.getMessage().equalsIgnoreCase("yes") || message.getMessage().equalsIgnoreCase("y")) {
                    p.sendMessage(ChatColor.RED + "Item " + (item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() + " " : "") + ChatColor.RED + "has been " + ChatColor.UNDERLINE + "destroyed.");
                } else {
                    if (p.getInventory().firstEmpty() == -1) {
                        p.sendMessage(ChatColor.RED + "Your inventory was " + ChatColor.UNDERLINE + "FULL" + ChatColor.RED + " so your Soulbound item has been destroyed.");
                    } else {
                        p.sendMessage(ChatColor.RED + "Soulbound item destruction " + ChatColor.UNDERLINE + "CANCELLED");
                        p.getInventory().addItem(item.clone());
                    }
                }
            }, (player) -> {
                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(ChatColor.RED + "Your inventory was " + ChatColor.UNDERLINE + "FULL" + ChatColor.RED + " so your Soulbound item has been destroyed.");
                } else {
                    player.sendMessage(ChatColor.RED + "Soulbound item destruction " + ChatColor.UNDERLINE + "CANCELLED");
                    p.getInventory().addItem(item.clone());
                }
            });

            return;
        }
        
        if(GameAPI.isItemPermanentlyUntradeable(item)) {
        	event.setCancelled(true);
        	event.getItemDrop().remove();
        	event.getPlayer().sendMessage(ChatColor.GRAY + "This item is " + ChatColor.UNDERLINE + "not" + ChatColor.GRAY + " droppable.");
        	return;
        }

        if (!GameAPI.isItemDroppable(item)) { 
        	net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        	NBTTagCompound tag = nmsItem.getTag();
        	assert tag != null;
        	event.getItemDrop().remove();
        	p.sendMessage(ChatColor.GRAY + "This item was " + ChatColor.ITALIC + "untradeable" + ChatColor.GRAY + ", " + "so it has " + ChatColor.UNDERLINE + "vanished.");
        	p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.6F, 0.2F);
        }
        PlayerManager.checkInventory(event.getPlayer().getUniqueId());
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
                TeleportLocation teleportTo = TeleportLocation.getTeleportLocation(nmsItem.getTag());
                
                if(teleportTo == null)
                	return;
                
                if(!teleportTo.canBeABook()) {
                	player.sendMessage(ChatColor.RED + "This teleport book is invalid, it has vanished into the wind.");
                	player.getInventory().setItemInMainHand(null);
                	player.updateInventory();
                	GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED + "[ALERT] " + ChatColor.WHITE + "Removed " + itemStack.getAmount() + "x " + teleportTo.getDisplayName() + " teleport books from " + player.getName() + ".");
                	return;
                }
                
                if (teleportTo.canTeleportTo(player)) {
                    Teleportation.getInstance().teleportPlayer(player.getUniqueId(), Teleportation.EnumTeleportType.TELEPORT_BOOK, teleportTo);
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

                if (DungeonRealms.getInstance().getRebootTime() - System.currentTimeMillis() < 5 * 60 * 1000) {
                    p.sendMessage(ChatColor.RED + "This shard is rebooting in less than 5 minutes, so you cannot upgrade this realm on this shard.");
                    return;
                }

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
                            int cost = Realms.getInstance().getRealmUpgradeCost(tier + 1);
                            if (balance < Realms.getInstance().getRealmUpgradeCost(tier + 1)) {
                                p.sendMessage(ChatColor.RED + "You do not have enough GEM(s) in your bank to purchase this upgrade. Upgrade cancelled.");
                                p.sendMessage(ChatColor.RED + "COST: " + Realms.getInstance().getRealmUpgradeCost(tier + 1) + " Gem(s)");
                                return;
                            }

                            DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.GEMS, balance - cost, false, doAfter -> Realms.getInstance().upgradeRealm(p));
                        }
                    });
                }, null);
                return;
            }

            if (GameAPI.isInWorld(p, Realms.getInstance().getRealmWorld(p.getUniqueId()))) {
                if (event.getClickedBlock() != null && event.getClickedBlock().getLocation() != null) {
                    Location newLocation = event.getClickedBlock().getLocation().clone().add(0, 2, 0);

                    if (GameAPI.isMaterialNearby(newLocation.clone().getBlock(), 3, Material.LADDER)
                    		|| GameAPI.isMaterialNearby(newLocation.clone().getBlock(), 5, Material.ENDER_CHEST)
                    		|| newLocation.getBlock().getType() != Material.AIR
                    		|| newLocation.clone().subtract(0, 1, 0).getBlock().getType() != Material.AIR) {
                        event.getPlayer().sendMessage(ChatColor.RED + "You cannot place a realm portal here!");
                        return;
                    }

                    Realms.getInstance().setRealmSpawn(event.getPlayer().getUniqueId(), newLocation);
                    event.setCancelled(true);
                    return;
                } // Player is clicking air
            }


            if (event.getClickedBlock() != null) {
                if (Realms.getInstance().canPlacePortal(p, event.getClickedBlock().getLocation()))
                    Realms.getInstance().loadRealm(p, () -> Realms.getInstance().openRealmPortal(p, event.getClickedBlock().getLocation()));
            }

            event.setCancelled(true);
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {

        	if(event.hasBlock() && event.getClickedBlock().getType() == Material.PORTAL)
        		return;
        	
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
        Quests.getInstance().triggerObjective(p, ObjectiveOpenJournal.class);
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
                if (event.getItem().getAmount() > 1) {
                    player.sendMessage(ChatColor.RED + "Please only use one item at a time.");
                    return;
                }
                if (nms.hasTag() && nms.getTag().hasKey("retrainingBook")) {
                    event.getPlayer().sendMessage(ChatColor.GREEN + "Reset stat points? Type 'yes' or 'y' to confirm.");
                    ItemStack resetBook = event.getItem().clone();
                    event.getPlayer().getInventory().remove(resetBook);
                    Chat.listenForMessage(event.getPlayer(), chat -> {
                        if (chat.getMessage().equalsIgnoreCase("Yes") || chat.getMessage().equalsIgnoreCase("y")) {
                            GameAPI.getGamePlayer(event.getPlayer()).getStats().unallocateAllPoints();
                            event.getPlayer().sendMessage(ChatColor.YELLOW + "All Stat Points have been unallocated!");
                        } else {
                            event.getPlayer().getInventory().addItem(resetBook);
                            event.getPlayer().sendMessage(ChatColor.RED + "Cancelled");
                        }
                    }, p -> {
                        event.getPlayer().getInventory().addItem(resetBook);
                        p.sendMessage(ChatColor.RED + "Action cancelled.");
                    });
                }
            }
            if (event.getItem().getType() == Material.FIREWORK) {
                if (nms.hasTag() && nms.getTag().hasKey("globalMessenger")) {
                    if (event.getItem().getAmount() > 1) {
                        player.sendMessage(ChatColor.RED + "Please only use one item at a time.");
                        return;
                    }

                    if (PunishAPI.isMuted(event.getPlayer().getUniqueId())) {
                        event.getPlayer().sendMessage(PunishAPI.getMutedMessage(event.getPlayer().getUniqueId()));
                        return;
                    }

                    event.getPlayer().sendMessage("");
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "Please enter the message you'd like to send to " + ChatColor.UNDERLINE + "all servers" + ChatColor.YELLOW
                            + " -- think before you speak!");
                    event.getPlayer().sendMessage(ChatColor.GRAY + "Type 'cancel' (no apostrophes) to cancel this and get your Global Messenger back.");
                    event.getPlayer().sendMessage("");
                    ItemStack messengerItem = event.getItem().clone();
                    event.getPlayer().getInventory().remove(messengerItem);
                    Chat.listenForMessage(event.getPlayer(), chat -> {
                        if (chat.getMessage().equalsIgnoreCase("cancel")) {
                            event.getPlayer().getInventory().addItem(messengerItem);
                            event.getPlayer().sendMessage(ChatColor.RED + "Global Messenger - " + ChatColor.BOLD + "CANCELLED");
                            return;
                        }

                        String msg = chat.getMessage();
                        if (msg.contains(".com") || msg.contains(".net") || msg.contains(".org") || msg.contains("http://") || msg.contains("www.")) {
                            if (!Rank.isDev(event.getPlayer())) {
                                event.getPlayer().getInventory().addItem(messengerItem);
                                event.getPlayer().sendMessage(ChatColor.RED + "No " + ChatColor.UNDERLINE + "URL's" + ChatColor.RED + " in your global messages please!");
                                return;
                            }
                        }

                        final String fixedMessage = Chat.getInstance().checkForBannedWords(msg);

                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Alert");
                        out.writeUTF(" \n" + ChatColor.GOLD.toString() + ChatColor.BOLD + ">>" + ChatColor.GOLD + " (" + DungeonRealms.getInstance().shardid + ") " + GameChat.getPreMessage(event.getPlayer()) + ChatColor.GOLD + fixedMessage + "\n ");

                        event.getPlayer().sendPluginMessage(DungeonRealms.getInstance(), "BungeeCord", out.toByteArray());
                    }, p -> {
                        event.getPlayer().getInventory().addItem(messengerItem);
                        p.sendMessage(ChatColor.RED + "Action cancelled.");
                    });
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
            } else if (nms.hasTag() && nms.getTag().hasKey("buff")) {

                if (event.getItem().getAmount() > 1) {
                    player.sendMessage(ChatColor.RED + "Please only use one item at a time.");
                    return;
                }

                String itemName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
                int duration = nms.getTag().getInt("duration");
                int bonusAmount = nms.getTag().getInt("bonusAmount");
                String formattedTime = DurationFormatUtils.formatDurationWords(duration * 1000, true, true);
                final String buffType = nms.getTag().getString("buff");

                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                player.updateInventory();

                player.sendMessage("");
                Utils.sendCenteredMessage(player, ChatColor.DARK_GRAY + "***" + ChatColor.GREEN.toString() +
                        ChatColor.BOLD + itemName.toUpperCase() + " CONFIRMATION" + ChatColor.DARK_GRAY + "***");
                player.sendMessage(ChatColor.GOLD
                        + "Are you sure you want to use this item? It will apply a " + bonusAmount + "% buff to all " + nms.getTag().get("description") + " across all servers for " + formattedTime + ". This cannot be undone once it has begun.");

                if (buffType.equals("loot")) {
                    if (DonationEffects.getInstance().getActiveLootBuff() != null)
                        player.sendMessage(ChatColor.RED + "NOTICE: There is an ongoing " + buffType + " buff, so your buff " +
                                "will be activated afterwards. Cancel if you do not wish to queue yours.");
                } else if (buffType.equals("profession")) {
                    if (DonationEffects.getInstance().getActiveProfessionBuff() != null)
                        player.sendMessage(ChatColor.RED + "NOTICE: There is an ongoing " + buffType + " buff, so your buff " +
                                "will be activated afterwards. Cancel if you do not wish to queue yours.");
                } else if (buffType.equals("level")) {
                    if (DonationEffects.getInstance().getActiveLevelBuff() != null)
                        player.sendMessage(ChatColor.RED + "NOTICE: There is an ongoing " + buffType + " buff, so your buff " +
                                "will be activated afterwards. Cancel if you do not wish to queue yours.");
                }

                player.sendMessage(ChatColor.GRAY + "Type '" + ChatColor.GREEN + "Y" + ChatColor.GRAY + "' to confirm, or any other message to cancel.");
                player.sendMessage("");
                ItemStack buffItem = event.getItem().clone();
                event.getPlayer().getInventory().remove(event.getItem());
                Chat.listenForMessage(player, e -> {
                    if (e.getMessage().equalsIgnoreCase("y")) {
                        GameAPI.sendNetworkMessage("buff", buffType, String.valueOf(nms.getTag().getInt("duration"))
                                , String.valueOf(nms.getTag().getInt("bonusAmount")), GameChat.getFormattedName
                                        (player), DungeonRealms.getInstance().bungeeName);
                    } else {
                        event.getPlayer().getInventory().addItem(buffItem);
                        player.sendMessage(ChatColor.RED + itemName + " - CANCELLED");
                    }
                }, p -> {
                    event.getPlayer().getInventory().addItem(buffItem);
                    p.sendMessage(ChatColor.RED + itemName + " - CANCELLED");
                });
            }
        }
    }

    @EventHandler
    public void onPlayerUseMountItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType() != Material.AIR) {
            NBTWrapper wrapper = new NBTWrapper(item);
            if (wrapper.hasTag("mount")) {
                String mount = wrapper.getString("mount");

                EnumMounts eMount = EnumMounts.getByName(mount);
                if (eMount == null) return;

                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                    Player player = event.getPlayer();
                    if (EntityAPI.hasMountOut(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "You already have a mount currently spawned.");
                        player.sendMessage(ChatColor.GRAY + "Use '/mount' to remove your spawned mount.");
                        return;
                    }

                    if (event.getPlayer().hasMetadata("summoningMount")) {
                        player.sendMessage(ChatColor.RED + "You are already summoning a mount!");
                        return;
                    }

                    Location startingLocation = player.getLocation();

                    AtomicInteger counter = new AtomicInteger(5);
                    player.sendMessage(ChatColor.WHITE.toString() + ChatColor.BOLD + "SUMMONING" + ChatColor.WHITE + " ... " + counter.get() + ChatColor.BOLD + "s");

                    player.setMetadata("summoningMount", new FixedMetadataValue(DungeonRealms.getInstance(), ""));
                    new BukkitRunnable() {
                        public void run() {
                            if (!player.isOnline()) {
                                player.removeMetadata("summoningMount", DungeonRealms.getInstance());
                                cancel();
                                return;
                            }

                            if (player.getLocation().distance(startingLocation) <= 4) {
                                if (!EntityAPI.hasMountOut(player.getUniqueId())) {
                                    if (!CombatLog.isInCombat(player) && !CombatLog.inPVP(player)) {
                                        if (counter.decrementAndGet() > 0) {
                                            player.sendMessage(ChatColor.WHITE.toString() + ChatColor.BOLD + "SUMMONING" + ChatColor.WHITE + " ... " + counter.get() + ChatColor.BOLD + "s");
                                            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPELL, player.getLocation().add(0, 0.15, 0),
                                                    ThreadLocalRandom.current().nextFloat(), ThreadLocalRandom.current().nextFloat(), ThreadLocalRandom.current().nextFloat(), 0.5F, 80);
//                                            ParticleAPI.ParticleEffect.sendToLocation(ParticleAPI.ParticleEffect.SPELL, pl.getLocation().add(0, 0.15, 0), new Random().nextFloat(),
//                                                    new Random().nextFloat(), new Random().nextFloat(), 0.5F, 80);
                                        } else {
                                            MountUtils.spawnMount(player.getUniqueId(), eMount.getRawName(), null);
                                            cancel();
                                            player.removeMetadata("summoningMount", DungeonRealms.getInstance());
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Combat has cancelled your mount summoning!");
                                        player.removeMetadata("summoningMount", DungeonRealms.getInstance());
                                        cancel();
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "Mount already detected out.");
                                    player.removeMetadata("summoningMount", DungeonRealms.getInstance());
                                    cancel();
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Movement has cancelled your mount summoning!");
                                player.removeMetadata("summoningMount", DungeonRealms.getInstance());
                                cancel();
                            }
                        }
                    }.runTaskTimer(DungeonRealms.getInstance(), 20, 20);
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
            if (AntiDuplication.getInstance().getUniqueEpochIdentifier(itemUsed) == null) return;
            if (AntiDuplication.getInstance().getUniqueEpochIdentifier(potion) == null && AntiDuplication.getInstance().getUniqueEpochIdentifier(potionOffhand) == null)
                return;
            if (AntiDuplication.getInstance().getUniqueEpochIdentifier(itemUsed).equalsIgnoreCase(AntiDuplication.getInstance().getUniqueEpochIdentifier(potion))) {
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


        int secondsToHeal = 15;
        new BukkitRunnable() {
            int time = 0;

            public void run() {

                if (time >= secondsToHeal) {
                    cancel();
                    return;
                }

                time++;
                if (!player.isSprinting() && HealthHandler.getInstance().getPlayerHPLive(player) < HealthHandler.getInstance().getPlayerMaxHPLive(player) && !CombatLog.isInCombat(player)) {
                    HealthHandler.getInstance().healPlayerByAmount(player, amount);
                } else {
                    if (player.hasMetadata("FoodRegen")) {
                        player.removeMetadata("FoodRegen", DungeonRealms.getInstance());
                        player.sendMessage(ChatColor.RED + "Healing Cancelled!");
                        cancel();
                    }
                }
            }
        }.runTaskTimer(DungeonRealms.getInstance(), 0, 20L);
//
//        int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
//            if (!player.isSprinting() && HealthHandler.getInstance().getPlayerHPLive(player) < HealthHandler.getInstance().getPlayerMaxHPLive(player) && !CombatLog.isInCombat(player)) {
//                HealthHandler.getInstance().healPlayerByAmount(player, amount);
//            } else {
//                if (player.hasMetadata("FoodRegen")) {
//                    player.removeMetadata("FoodRegen", DungeonRealms.getInstance());
//                    player.sendMessage(ChatColor.RED + "Healing Cancelled!");
//                }
//            }
//        }, 0L, 20L);
//        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
//            Bukkit.getScheduler().cancelTask(taskID);
//            if (player.hasMetadata("FoodRegen")) {
//                player.removeMetadata("FoodRegen", DungeonRealms.getInstance());
//            }
//        }, 310L);
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
                    if (rank.equalsIgnoreCase("sub") || rank.equalsIgnoreCase("hiddenmod")) {
                        prefix = ChatColor.GREEN;
                    } else if (rank.equalsIgnoreCase("sub+")) {
                        prefix = ChatColor.GOLD;
                    } else if (rank.equalsIgnoreCase("sub++")) {
                        prefix = ChatColor.YELLOW;
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


    @EventHandler(priority = EventPriority.LOWEST)
    public void playerEatFish(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getClickedBlock().getType() != null &&
                (e.getClickedBlock().getType().equals(Material.FURNACE) || e.getClickedBlock().getType().equals(Material.BURNING_FURNACE))) {
            return;
        }

        //Prevent double firing
        if (!e.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }

        final Player p = e.getPlayer();

        ItemStack fish;
        boolean eaten = false;

        if (p.getInventory().getItemInMainHand() != null && p.getInventory().getItemInMainHand().getType() != Material.AIR) {
            fish = p.getInventory().getItemInMainHand();

            if (Fishing.isCustomRawFish(fish)) {
                p.sendMessage(ChatColor.RED + "You must cook this fish before you can eat it!");

            } else if (Fishing.isCustomFish(fish)) {
                eaten = true;

                e.setUseInteractedBlock(Event.Result.DENY);
                e.setCancelled(true);

                Fishing.restoreFood(p, fish);
                Fishing.applyFishBuffs(p, fish);

                if (fish.getAmount() == 1) {
                    p.getInventory().setItemInMainHand(null);
                } else {
                    fish.setAmount(fish.getAmount() - 1);
//                    p.getInventory().setItemInOffHand(fish);
                }


            }


        }

        if (p.getInventory().getItemInOffHand() != null && p.getInventory().getItemInOffHand().getType() != Material.AIR) {
            fish = p.getInventory().getItemInOffHand();

            if (Fishing.isCustomRawFish(fish)) {
                p.sendMessage(ChatColor.RED + "You must cook this fish before you can eat it!");

            } else if (Fishing.isCustomFish(fish)) {
                eaten = true;

                e.setUseInteractedBlock(Event.Result.DENY);
                e.setCancelled(true);

                Fishing.restoreFood(p, fish);
                Fishing.applyFishBuffs(p, fish);

                if (fish.getAmount() == 1) {
                    p.getInventory().setItemInOffHand(null);
                } else {
                    fish.setAmount(fish.getAmount() - 1);
//                    p.getInventory().setItemInOffHand(fish);
                }


            }


        }

        if (eaten) {
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1F, 1F);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1F, 1.5F), 4L);
            p.updateInventory();
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
