package net.dungeonrealms.game.listeners;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.handlers.TutorialIslandHandler;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.miscellaneous.Cooldown;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
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
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        if (!API.isItemDroppable(item)) { //Realm Portal, Character Journal.
            event.setCancelled(true);
            event.getItemDrop().remove();
        } else if (!API.isItemTradeable(item)) {
            net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tag = nmsItem.getTag();
            assert tag != null;
            // send the untradeable message if not profile or hearthstone since they will be dropped
            // every time the inventory is closed
            if (!item.getItemMeta().getDisplayName().contains("Character Profile") && item.getItemMeta().getDisplayName().contains("Realm Portal Rune")
                    && !item.getItemMeta().getDisplayName().contains("Hearthstone")) {
                p.sendMessage(ChatColor.GRAY + "This item was " + ChatColor.ITALIC + "untradeable" + ChatColor.GRAY + ", " +
                        "so it has " + ChatColor.UNDERLINE + "vanished.");
                p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.6F, 0.2F);
            }
            event.getItemDrop().remove();
//            event.setCancelled(true);
        } else if (API.isItemSoulbound(item)) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.RED + "Are you sure you want to " + ChatColor.UNDERLINE + "destroy" + ChatColor
                    .RED + " this soulbound item? Type " + ChatColor.GREEN + ChatColor.BOLD + "Y" + ChatColor.RED + "" +
                    " or " + ChatColor.DARK_RED + ChatColor.BOLD + "N");
            Chat.listenForMessage(p, chat -> {
                if (chat.getMessage().equalsIgnoreCase("y")) {
                    p.sendMessage(ChatColor.RED + "Item " + item.getItemMeta().getDisplayName() + ChatColor.RED + " has been " + ChatColor.UNDERLINE + "destroyed.");
                    p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.6F, 0.2F);
                    p.getInventory().remove(item);
                }
            }, player -> player.sendMessage(ChatColor.RED + "Item destroying " + ChatColor.UNDERLINE + "cancelled."));
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
        ItemStack itemStack = player.getEquipment().getItemInMainHand();
        if (!(CombatLog.isInCombat(event.getPlayer()))) {
            if (TeleportAPI.isPlayerCurrentlyTeleporting(player.getUniqueId())) {
                player.sendMessage("You cannot restart a teleport during a cast!");
                return;
            }
            if (TeleportAPI.isTeleportBook(itemStack)) {
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
            player.sendMessage(
                    ChatColor.GREEN.toString() + ChatColor.BOLD + "TELEPORT " + ChatColor.RED + "You are in combat! " + ChatColor.RED.toString() + "(" + ChatColor.UNDERLINE + CombatLog.COMBAT.get(player.getUniqueId()) + "s" + ChatColor.RED + ")");
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


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUsePortalRune(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player p = event.getPlayer();
        if (p.getEquipment().getItemInMainHand() == null || p.getEquipment().getItemInMainHand().getType() != Material.NETHER_STAR)
            return;
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getEquipment().getItemInMainHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (tag.hasKey("realmPortalRune") && !(tag.getString("realmPortalRune").equalsIgnoreCase("true"))) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if (Cooldown.hasCooldown(event.getPlayer().getUniqueId())) return;
            Cooldown.addCooldown(event.getPlayer().getUniqueId(), 1000);


            if (Realms.getInstance().isRealmLoaded(event.getPlayer().getUniqueId()) && Realms.getInstance().getRealmWorld(p.getUniqueId()).equals(p.getLocation().getWorld())) {
                Location newLocation = event.getClickedBlock().getLocation().clone().add(0, 2, 0);


                if (TutorialIslandHandler.getInstance().onTutorialIsland(event.getPlayer().getUniqueId())) {
                    p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED
                            + " open a portal to your realm until you have completed Tutorial Island.");
                    return;
                }

                if (API.isMaterialNearby(newLocation.clone().getBlock(), 3, Material.LADDER) || API.isMaterialNearby(newLocation.clone().getBlock(), 5, Material.ENDER_CHEST)) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You cannot place a realm portal here!");
                    return;
                }

                Realms.getInstance().setRealmSpawn(event.getPlayer().getUniqueId(), newLocation);
                return;
            }

            if (!Realms.getInstance().isRealmCached(event.getPlayer().getUniqueId())) {
                Realms.getInstance().loadRealm(p, event.getClickedBlock().getLocation());
                return;
            }

            Realms.getInstance().openRealmPortal(p, event.getClickedBlock().getLocation());
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
        if (tag.hasKey("journal") && !(tag.getString("journal").equalsIgnoreCase("true"))) return;
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
            if (event.getItem().getType() == Material.ENCHANTED_BOOK) {
                net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(event.getItem());
                if (nms.hasTag() && nms.getTag().hasKey("retrainingBook")) {
                    event.getPlayer().sendMessage(ChatColor.GREEN + "Reset stat points? Type 'yes' or 'y' to confirm.");
                    Chat.listenForMessage(event.getPlayer(), chat -> {
                        if (chat.getMessage().equalsIgnoreCase("Yes") || chat.getMessage().equalsIgnoreCase("y")) {
                            if (event.getItem().getAmount() > 1) {
                                event.getItem().setAmount(event.getItem().getAmount() - 1);
                            } else {
                                event.getPlayer().getInventory().remove(event.getItem());
                            }
                            API.getGamePlayer(event.getPlayer()).getStats().unallocateAllPoints();
                            event.getPlayer().sendMessage(ChatColor.YELLOW + "All Stat Points have been unallocated!");
                        }
                    }, p -> p.sendMessage(ChatColor.RED + "Action cancelled."));
                }
            } else if (event.getItem().getType() == Material.ENDER_CHEST) {
                net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(event.getItem());
                if (nms.hasTag() && nms.getTag().hasKey("type")) {
                    if (nms.getTag().getString("type").equalsIgnoreCase("upgrade")) {
                        Player player = event.getPlayer();
                        int invlvl = (int) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_LEVEL, player.getUniqueId());
                        if (invlvl == 6) {
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
                        HealthHandler.getInstance().healPlayerByAmount(event.getPlayer(), nmsOffhand.getTag().getInt("healAmount"));
                    } else {
                        player.sendMessage(ChatColor.RED + "You are already at full HP!");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = (CraftItemStack.asNMSCopy(event.getItem()));
        if (nmsItem == null || nmsItem.getTag() == null) return;
        if (!nmsItem.getTag().hasKey("type")) return;
        if (nmsItem.getTag().getString("type").equalsIgnoreCase("healingFood")) {
            if (event.getPlayer().getFoodLevel() >= 20) {
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            if (CombatLog.isInCombat(event.getPlayer())) {
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot eat this while in combat!");
                event.getPlayer().updateInventory();
                return;
            }
            if (event.getPlayer().hasMetadata("FoodRegen")) {
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot eat this while you have another food bonus active!");
                event.getPlayer().updateInventory();
                return;
            }
            if (event.getPlayer().isSprinting()) {
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot eat this while sprinting!");
                event.getPlayer().updateInventory();
                return;
            }
            ItemStack foodItem = event.getItem();
            if (foodItem.getAmount() > 1) {
                foodItem.setAmount(foodItem.getAmount() - 1);
                event.getPlayer().getInventory().remove(foodItem);
                event.getPlayer().getInventory().addItem(foodItem);
                event.getPlayer().updateInventory();
            } else {
                event.getPlayer().getInventory().remove(foodItem);
            }
            event.getPlayer().updateInventory();
            event.getPlayer().setFoodLevel(event.getPlayer().getFoodLevel() + 6);
            event.getPlayer().sendMessage(ChatColor.GREEN + "Healing " + ChatColor.BOLD + nmsItem.getTag().getInt("healAmount") + ChatColor.GREEN + "HP/s for 15 Seconds!");
            event.getPlayer().setMetadata("FoodRegen", new FixedMetadataValue(DungeonRealms.getInstance(), "True"));
            int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                if (!event.getPlayer().isSprinting() && HealthHandler.getInstance().getPlayerHPLive(event.getPlayer()) < HealthHandler.getInstance().getPlayerMaxHPLive(event.getPlayer()) && !CombatLog.isInCombat(event.getPlayer())) {
                    HealthHandler.getInstance().healPlayerByAmount(event.getPlayer(), nmsItem.getTag().getInt("healAmount"));
                } else {
                    if (event.getPlayer().hasMetadata("FoodRegen")) {
                        event.getPlayer().removeMetadata("FoodRegen", DungeonRealms.getInstance());
                        event.getPlayer().sendMessage(ChatColor.RED + "Healing Cancelled!");
                    }
                }
            }, 0L, 20L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                Bukkit.getScheduler().cancelTask(taskID);
                if (event.getPlayer().hasMetadata("FoodRegen")) {
                    event.getPlayer().removeMetadata("FoodRegen", DungeonRealms.getInstance());
                }
            }, 300L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPotionSplash(PotionSplashEvent event) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = (CraftItemStack.asNMSCopy(event.getPotion().getItem()));
        if (nmsItem != null && nmsItem.getTag() != null) {
            if (nmsItem.getTag().hasKey("type") && nmsItem.getTag().getString("type").equalsIgnoreCase("splashHealthPotion")) {
                event.setCancelled(true);
                for (LivingEntity entity : event.getAffectedEntities()) {
                    if (!API.isPlayer(entity)) {
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
                String inputName = newPetName.getMessage();

                // Name must be below 14 characters
                if (inputName.length() > 14) {
                    player.sendMessage(ChatColor.RED + "Your pet name exceeds the maximum length of 12 characters.");
                    player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "You were " + (inputName.length() - 14) + " characters over the limit.");
                    return;
                }

                if (inputName.contains("@")) {
                    inputName = inputName.replaceAll("@", "_");
                }

                String checkedPetName = Chat.getInstance().checkForBannedWords(inputName);

                String activePet = (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_PET, player.getUniqueId());
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.PETS, activePet, false);
                if (activePet.contains("@")) {
                    activePet = activePet.split("@")[0];
                }
                String newPet = activePet + "@" + checkedPetName;
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.PETS, activePet, false);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.PETS, newPet, false);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_PET, newPet, true);
                Entity pet = EntityAPI.getPlayerPet(player.getUniqueId());
                pet.setCustomName(checkedPetName);
                player.sendMessage(ChatColor.GRAY + "Pet name changed to " + ChatColor.GREEN + ChatColor.UNDERLINE + checkedPetName);
            }, null);
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR)
            return;

        final Player pl = e.getPlayer();
        if (e.hasItem()) {
            final ItemStack is = e.getItem();
            if (Fishing.getInstance().isCustomRawFish(is)) {
                pl.sendMessage(ChatColor.RED + "You must cook this fish before you can eat it!");
                return;
            }


            if (Fishing.isCustomFish(is)) {
                e.setUseInteractedBlock(Event.Result.DENY);

                pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_BURP, 1F, 1F);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    // TODO: This could be abused with a macro.
                    if (Fishing.isCustomFish(pl.getItemInHand())) {
                        ItemStack fish = pl.getItemInHand();
                        if (fish.getAmount() > 1) {
                            // Subtract just 1.
                            fish.setAmount(fish.getAmount() - 1);
                            pl.setItemInHand(fish);
                        } else if (fish.getAmount() <= 1) {
                            pl.setItemInHand(new ItemStack(Material.AIR));
                        }

                        pl.updateInventory();
                    }
                }, 1L);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_BURP, 1F, 1.5F);
                }, 4L);
                // Ok, so now we need to see what we need to do for the player when they eat this. Let's handle the food aspect first.
                List<String> lore = is.getItemMeta().getLore();
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

                // Ok, food handled, now let's handle any misc. buffs that the fish might have.
                for (String s : lore) {
                    s = ChatColor.stripColor(s);
                    if (s.contains("% HP (instant)")) {
                        // Instant heal.
                        double percent_to_heal = Double.parseDouble(s.substring(s.indexOf("+") + 1, s.indexOf("%"))) / 100;
                        double max_hp = HealthHandler.getInstance().getPlayerMaxHPLive(pl);
                        int amount_to_heal = (int) Math.round((percent_to_heal * max_hp));
                        double current_hp = HealthHandler.getInstance().getPlayerHPLive(pl);
                        if (current_hp + 1 > max_hp) {
                            continue;
                        } // They have max HP.
                        // amount_to_heal += getHealthRegenAmount(p);

                        if ((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, pl.getUniqueId())) {
                            pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + (int) amount_to_heal + ChatColor.BOLD + " HP"
                                    + ChatColor.GREEN + " FROM " + is.getItemMeta().getDisplayName() + ChatColor.GRAY + " ["
                                    + ((int) current_hp + (int) amount_to_heal) + "/" + (int) max_hp + "HP]");
                        }

                        if ((current_hp + amount_to_heal) >= max_hp) {
                            pl.setHealth(20);
                            HealthHandler.getInstance().setPlayerHPLive(pl, (int) max_hp);
                            continue; // Full HP.
                        } else if (pl.getHealth() <= 19 && ((current_hp + amount_to_heal) < max_hp)) {
                            HealthHandler.getInstance().setPlayerHPLive(pl, (int) HealthHandler.getInstance().getPlayerHPLive(pl) + amount_to_heal);
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
                    }

                    if (s.startsWith("REGEN")) {
                        // Regen % of HP over X seconds.
                        double percent_to_regen = Double.parseDouble(s.substring(s.indexOf(" ") + 1, s.indexOf("%"))) / 100.0D;
                        int regen_interval = Integer.parseInt(s.substring(s.lastIndexOf(" ") + 1, s.lastIndexOf("s")));
                        double max_hp = HealthHandler.getInstance().getPlayerMaxHPLive(pl);

                        final int amount_to_regen_per_interval = (int) (max_hp * percent_to_regen) / regen_interval;
                        pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "      " + ChatColor.GREEN + amount_to_regen_per_interval + ChatColor.BOLD
                                + " HP/s" + ChatColor.GREEN + " FROM " + is.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + regen_interval + "s]");

//                        fish_health_regen.put(pl.getName(), amount_to_regen_per_interval);
                        API.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.HEALTH_REGEN, (float) percent_to_regen);

                        pl.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (int) (regen_interval + (regen_interval * 0.25)), 0));
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), new Runnable() {
                            public void run() {
                                pl.removePotionEffect(PotionEffectType.REGENERATION);
//                                fish_health_regen.remove(pl.getName());
                                API.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.HEALTH_REGEN, (float) -percent_to_regen);

                                pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "   " + amount_to_regen_per_interval + " HP/s " + ChatColor.RED + "FROM "
                                        + is.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                            }
                        }, regen_interval * 20L);
                    }

                    if (s.startsWith("SPEED")) {
                        // Speed effect.
                        String tier_symbol = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                        int effect_tier = 0;
                        if (tier_symbol.equalsIgnoreCase("II")) {
                            effect_tier = 1;
                        }

                        int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                        pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, effect_time * 20, effect_tier));
                    }

                    if (s.startsWith("NIGHTVISION")) {
                        // Night vision effect
                        String tier_symbol = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                        int effect_tier = 0;
                        if (tier_symbol.equalsIgnoreCase("II")) {
                            effect_tier = 1;
                        }

                        int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                        pl.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, effect_time * 20, effect_tier));
                    }

                    if (s.contains("ENERGY REGEN")) {
                        // Increased energy regen.
                        final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                        int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                        API.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.ENERGY_REGEN, bonus_percent);

//                        fish_energy_regen.put(pl.getName(), bonus_percent);
//                        RealmMechanics.playPotionEffect(pl, (LivingEntity) pl, 0xa47c48, (effect_time * 20));
                        pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "      " + ChatColor.GREEN + (int) bonus_percent + ChatColor.BOLD + " Energy/s"
                                + ChatColor.GREEN + " FROM " + is.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
//                                fish_energy_regen.remove(pl.getName());
                            API.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.ENERGY_REGEN, -bonus_percent);

                            pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + " +" + bonus_percent + "% Energy " + ChatColor.RED + "FROM "
                                    + is.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                        }, effect_time * 20L);
                    }

                    if (s.contains("% DMG")) {
                        final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                        int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));

                        API.getGamePlayer(pl).changeAttributeValPercentage(Item.WeaponAttributeType.DAMAGE, bonus_percent);

//                        fish_bonus_dmg.put(pl.getName(), bonus_percent);
//                        RealmMechanics.playPotionEffect(pl, (LivingEntity) pl, 0xdd0000, (effect_time * 20));
                        pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + (int) bonus_percent + ChatColor.BOLD + "% DMG"
                                + ChatColor.GREEN + " FROM " + is.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            API.getGamePlayer(pl).changeAttributeValPercentage(Item.WeaponAttributeType.DAMAGE, -bonus_percent);
//                                fish_bonus_dmg.remove(pl.getName());
                            pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% DMG " + ChatColor.RED + "FROM "
                                    + is.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                        }, effect_time * 20L);
                    }

                    if (s.contains("% ARMOR")) {
                        final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                        int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                        API.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.ARMOR, bonus_percent);

//                        fish_bonus_armor.put(pl.getName(), bonus_percent);
//                        RealmMechanics.playPotionEffect(pl, (LivingEntity) pl, 0xacacac, (effect_time * 20));
                        pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + (int) bonus_percent + ChatColor.BOLD + "% ARMOR"
                                + ChatColor.GREEN + " FROM " + is.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), new Runnable() {
                            public void run() {
                                API.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.ARMOR, -bonus_percent);
//                                fish_bonus_armor.remove(pl.getName());
                                pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% ARMOR " + ChatColor.RED + "FROM "
                                        + is.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                            }
                        }, effect_time * 20L);
                    }

                    if (s.contains("% BLOCK")) {
                        final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                        int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                        API.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.BLOCK, bonus_percent);


//                        fish_bonus_block.put(pl.getName(), bonus_percent);
//                        RealmMechanics.playPotionEffect(pl, (LivingEntity) pl, 0x014421, (effect_time * 20));
                        pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + (int) bonus_percent + ChatColor.BOLD + "% BLOCK"
                                + ChatColor.GREEN + " FROM " + is.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), new Runnable() {
                            public void run() {
                                API.getGamePlayer(pl).changeAttributeValPercentage(Item.ArmorAttributeType.BLOCK, -bonus_percent);

//                                fish_bonus_block.remove(pl.getName());
                                pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% BLOCK " + ChatColor.RED + "FROM "
                                        + is.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                            }
                        }, effect_time * 20L);
                    }

                    if (s.contains("% LIFESTEAL")) {
                        final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                        int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                        API.getGamePlayer(pl).changeAttributeValPercentage(Item.WeaponAttributeType.LIFE_STEAL, bonus_percent);

//                        fish_bonus_lifesteal.put(pl.getName(), bonus_percent);
//                        RealmMechanics.playPotionEffect(pl, (LivingEntity) pl, 0x4d2177, (effect_time * 20));
                        pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + (int) bonus_percent + ChatColor.BOLD + "% LIFESTEAL"
                                + ChatColor.GREEN + " FROM " + is.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), new Runnable() {
                            public void run() {
                                API.getGamePlayer(pl).changeAttributeValPercentage(Item.WeaponAttributeType.LIFE_STEAL, -bonus_percent);

//                                fish_bonus_lifesteal.remove(pl.getName());
                                pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% LIFESTEAL " + ChatColor.RED + "FROM "
                                        + is.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                            }
                        }, effect_time * 20L);
                    }

                    if (s.contains("% CRIT")) {
                        final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                        int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                        API.getGamePlayer(pl).changeAttributeValPercentage(Item.WeaponAttributeType.CRITICAL_HIT, bonus_percent);

//                        fish_bonus_critical_hit.put(pl.getName(), bonus_percent);
//                        RealmMechanics.playPotionEffect(pl, (LivingEntity) pl, 0xe52d00, (effect_time * 20));
                        pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + (int) bonus_percent + ChatColor.BOLD + "% CRIT"
                                + ChatColor.GREEN + " FROM " + is.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), new Runnable() {
                            public void run() {
                                API.getGamePlayer(pl).changeAttributeValPercentage(Item.WeaponAttributeType.CRITICAL_HIT, -bonus_percent);
//                                fish_bonus_critical_hit.remove(pl.getName());
                                pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% CRIT " + ChatColor.RED + "FROM "
                                        + is.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                            }
                        }, effect_time * 20L);
                    }
                }

            }
        }
    }

    @EventHandler
    public void playerEatFishItem(PlayerInteractEvent event) {

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
                        player.sendMessage(ChatColor.AQUA + "Mount dismissed.");
                        EntityAPI.removePlayerMountList(player.getUniqueId());
                        return;
                    }
                    if (CombatLog.isInCombat(player)) {
                        player.sendMessage(ChatColor.RED + "You cannot summon a mount while in Combat!");
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
                    player.sendMessage(ChatColor.GREEN + "Your Mount is being summoned into this world!");
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
                        player.sendMessage(ChatColor.AQUA + "Pet dismissed.");
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
                    player.sendMessage(ChatColor.GREEN + "Pet summoned.");
                    break;
                case "trail":
                    if (DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(player)) {
                        DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.remove(player);
                        player.sendMessage(ChatColor.AQUA + "You have disabled your trail.");
                        return;
                    }
                    String trailType = (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_TRAIL, player.getUniqueId());
                    if (trailType == null || trailType.equals("")) {
                        player.sendMessage(ChatColor.RED + "You don't have an active trail, please enter the trails section in your profile to set one.");
                        player.closeInventory();
                        return;
                    }
                    DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.put(player, ParticleAPI.ParticleEffect.getByName(trailType));
                    player.sendMessage(ChatColor.GREEN + "Enabling trail.");
                    break;
            }
        }
    }
}
