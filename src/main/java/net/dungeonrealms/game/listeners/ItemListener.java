package net.dungeonrealms.game.listeners;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
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
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.*;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

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
            return;
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
                        player.setItemInHand(new ItemStack(Material.AIR));
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


    /**
     * Handles Right Click of Realm portal rune
     */
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

            if (!Realms.getInstance().isRealmCached(event.getPlayer().getUniqueId())) {
                Realms.getInstance().loadRealm(p);
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
        ItemStack stack = ItemManager.createCharacterJournal(p);

        p.getInventory().setItem(7, stack);
        p.updateInventory();
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuildBannerEquip(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        Player p = event.getPlayer();
        if (p.getItemInHand() == null || p.getItemInHand().getType() != Material.BANNER) return;
        if (!p.getItemInHand().hasItemMeta()) return;
        if (p.getItemInHand().getItemMeta().getDisplayName() == null) return;
        if (!p.getItemInHand().getItemMeta().getDisplayName().contains("Guild banner")) return;

        String guildName = p.getItemInHand().getItemMeta().getDisplayName().substring(2).replace("'s Guild banner", "").replaceAll("\\s", "").toLowerCase();

        final ItemStack banner = p.getItemInHand();

        p.setItemInHand(p.getInventory().getHelmet());
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
                        Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () ->
                                BankMechanics.getInstance().getStorage(player.getUniqueId()).update(), 20);
                        if (event.getPlayer().getEquipment().getItemInMainHand().getAmount() == 1) {
                            event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
                        } else {
                            ItemStack item = event.getPlayer().getEquipment().getItemInMainHand();
                            item.setAmount(item.getAmount() - 1);
                            event.getPlayer().setItemInHand(item);
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
        } else if (event.getItem().getType() == Material.COOKED_FISH && nmsItem.getTag().getString("type").equalsIgnoreCase("fishBuff") && nmsItem.getTag().hasKey("buff")) {
            if (Fishing.fishBuffs.containsKey(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(ChatColor.RED + "You have an active fish buff already!");
                return;
            }
            Fishing.fishBuffs.put(event.getPlayer().getUniqueId(), nmsItem.getTag().getString("buff"));
            event.getPlayer().sendMessage("    " + ChatColor.BOLD.toString() + ChatColor.YELLOW + nmsItem.getTag().getString("buff") + " Active for 10 seconds!");
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Fishing.fishBuffs.remove(event.getPlayer().getUniqueId()));
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
