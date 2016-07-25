package net.dungeonrealms.game.listener.world;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.type.EnumData;
import net.dungeonrealms.common.game.database.type.EnumOperators;
import net.dungeonrealms.common.game.updater.UpdateEvent;
import net.dungeonrealms.common.game.updater.UpdateType;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.miscellaneous.Cooldown;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.realms.instance.obj.RealmProperty;
import net.dungeonrealms.game.world.realms.instance.obj.RealmStatus;
import net.dungeonrealms.game.world.realms.instance.obj.RealmToken;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/21/2016
 */
public class RealmListener implements Listener {

    private Realms REALMS = Realms.getInstance();

    @EventHandler
    public void onWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (EntityAPI.hasPetOut(player.getUniqueId())) {
            net.minecraft.server.v1_9_R2.Entity pet = Entities.PLAYER_PETS.get(player.getUniqueId());
            pet.dead = true;
            EntityAPI.removePlayerPetList(player.getUniqueId());
        }

        if (EntityAPI.hasMountOut(player.getUniqueId())) {
            net.minecraft.server.v1_9_R2.Entity mount = Entities.PLAYER_MOUNTS.get(player.getUniqueId());
            mount.dead = true;
            EntityAPI.removePlayerMountList(player.getUniqueId());
        }

        World to = player.getWorld();

        if (REALMS.getRealm(to) != null) {
            RealmToken realm = REALMS.getRealm(to);

            if (!player.getUniqueId().equals(realm.getOwner()))
                player.sendMessage(ChatColor.LIGHT_PURPLE + "You have entered " + ChatColor.BOLD + realm.getName() + "'s" + ChatColor.LIGHT_PURPLE + " realm.");
            else
                player.sendMessage(ChatColor.LIGHT_PURPLE + "You have returned to " + ChatColor.BOLD + "YOUR" + ChatColor.LIGHT_PURPLE + " realm.");

            if (((realm.getBuilders().contains(player.getUniqueId()) || realm.getOwner().equals(player.getUniqueId())) && realm.getPropertyBoolean("flight"))) {
                player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "FLYING ENABLED");
                player.setAllowFlight(true);
            }

            if (!REALMS.getRealmTitle(realm.getOwner()).equals(""))
                player.sendMessage(ChatColor.GRAY + REALMS.getRealmTitle(realm.getOwner()));

            player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "INVINCIBILITY (15s)");
            player.sendMessage(ChatColor.GRAY + "You will " + ChatColor.UNDERLINE + "NOT" + ChatColor.GRAY.toString()
                    + " be flagged as 'combat logged' while invincible.");

            if (GameAPI.getGamePlayer(player) != null)
                GameAPI.getGamePlayer(player).setInvulnerable(true);

            Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
                player.setFireTicks(0);
                player.setFallDistance(0.0F);
                if (GameAPI.getGamePlayer(player) != null)
                    GameAPI.getGamePlayer(player).setInvulnerable(false);
            }, 15 * 20L);

        } else if (REALMS.getRealm(event.getFrom()) != null) {
            RealmToken realm = REALMS.getRealm(event.getFrom());

            if (!player.getGameMode().equals(GameMode.CREATIVE))
                player.setAllowFlight(false);

            if (player.getUniqueId().equals(realm.getOwner()))
                player.sendMessage(ChatColor.LIGHT_PURPLE + "You have left " + ChatColor.BOLD + "YOUR" + ChatColor.LIGHT_PURPLE + " realm.");
            else
                player.sendMessage(ChatColor.LIGHT_PURPLE + "You have left " + ChatColor.BOLD + realm.getName() + "'s" + ChatColor.LIGHT_PURPLE
                        + " realm.");
        }
    }


    @EventHandler
    public void onBlock(BlockPhysicsEvent event) {
        if (event.getBlock().getWorld().equals(Bukkit.getWorlds().get(0))) return;

        RealmToken realm = REALMS.getRealm(event.getBlock().getLocation().getWorld());

        if (realm != null && !realm.isSettingSpawn() && event.getBlock().getType().equals(Material.PORTAL))
            event.setCancelled(true);
    }

    @EventHandler
    public void RealmBlockProcessor(UpdateEvent e) {
        if (!e.getType().equals(UpdateType.SLOW)) return;

        for (Map.Entry<UUID, List<Location>> entry : REALMS.getProcessingBlocks().entrySet()) {
            String w_name = entry.getKey().toString();
            try {
                World w = Bukkit.getWorld(w_name);
                int limy = (128 - REALMS.getRealmDimensions(REALMS.getRealmTier(entry.getKey())));
                CopyOnWriteArrayList<Location> loc_list = new CopyOnWriteArrayList<>(entry.getValue());
                RealmToken realm = REALMS.getRealm(entry.getKey());
                int x = 0;

                for (Location loc : loc_list) {
                    if (x >= Realms.BLOCK_PROCESSOR_BUFFER_SIZE) {
                        break;
                    }
                    if (loc.getBlock().getY() > 127) {
                        if (loc.getBlock().getType() == Material.AIR) {
                            loc.getBlock().setType(Material.GRASS);
                        }
                    } else if (loc.getBlock().getY() <= limy + 1) {
                        if (loc.getBlock().getType() == Material.AIR) {
                            loc.getBlock().setType(Material.BEDROCK);
                        }

                    } else {
                        if (loc.getBlock().getType() == Material.AIR) {
                            loc.getBlock().setType(Material.DIRT);
                        }
                    }

                    loc_list.remove(loc);
                    x++;
                }

                if (loc_list.isEmpty()) {
                    Player p = Bukkit.getPlayer(entry.getKey());

                    if (p != null) {
                        p.sendMessage("");
                        Utils.sendCenteredMessage(p, ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "REALM UPGRADE COMPLETE.");
                        p.sendMessage("");

                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                        realm.setStatus(RealmStatus.CLOSED);

                    } else REALMS.removeRealm(entry.getKey(), true);

                    DatabaseAPI.getInstance().update(entry.getKey(), EnumOperators.$SET, EnumData.REALM_UPGRADE, false, true, doAfter -> {
                        GameAPI.updatePlayerData(entry.getKey());
                    });

                    REALMS.getProcessingBlocks().remove(entry.getKey());
                    realm.setUpgradeProgress(0);
                } else {
                    double total_area = REALMS.getRealmDimensions(REALMS.getRealmTier(UUID.fromString(w.getName())) + 1);
                    total_area = Math.pow(total_area, 3);
                    double complete_area = total_area - loc_list.size();

                    double percent = ((complete_area / total_area) * 100.0D);
                    realm.setUpgradeProgress(percent);
                    REALMS.getProcessingBlocks().put(entry.getKey(), loc_list);
                }

            } catch (NullPointerException ignored) {
                REALMS.getProcessingBlocks().remove(entry.getKey());
            }
        }
    }

    @EventHandler
    public void updateRealmEvent(UpdateEvent e) {
        if (!e.getType().equals(UpdateType.SEC)) return;

        for (RealmToken realm : REALMS.getCachedRealms().values()) {
            if (!realm.getPropertyBoolean("peaceful") && realm.getPropertyBoolean("flight")) {
                RealmProperty<Boolean> property = (RealmProperty<Boolean>) realm.getProperty("flight");
                property.setExpiry(System.currentTimeMillis() - 1000L);
            }
            if (realm.getWorld() == null) {
                continue;
            }

            for (RealmProperty<?> p : realm.getRealmProperties().values()) {
                if (!(p.hasExpired() && p.isAcknowledgeExpiration())) continue;

                switch (p.getName()) {
                    case "peaceful":
                        Player owner = Bukkit.getPlayer(realm.getOwner());
                        World world = REALMS.getRealmWorld(realm.getOwner());

                        // SET WORLD GUARD FLAG
                        REALMS.setRealmRegion(world, true);

                        if (owner != null)
                            owner.sendMessage(ChatColor.RED + "Your realm is now once again a " + ChatColor.BOLD + "CHAOTIC" + ChatColor.RED + " zone.");

                        if (realm.getPropertyBoolean("flight")) {

                            if (owner != null)
                                owner.sendMessage(ChatColor.GRAY + "Due to this, your " + ChatColor.UNDERLINE + "Orb of Flight" + ChatColor.GRAY
                                        + " will also expired.");

                            RealmProperty<Boolean> property = (RealmProperty<Boolean>) realm.getProperty("flight");
                            property.setExpiry(System.currentTimeMillis() - 1000L);
                        }

                        break;
                    case "flight":
                        if (Bukkit.getPlayer(realm.getOwner()) != null)
                            Bukkit.getPlayer(realm.getOwner()).sendMessage(ChatColor.RED + "Your " + ChatColor.UNDERLINE + "Orb of Flight" + ChatColor.RED + " effect has expired.");

                        for (Player pl : realm.getWorld().getPlayers()) pl.setAllowFlight(false);
                        break;
                }

                REALMS.updateRealmHologram(realm.getOwner());
                p.setAcknowledgeExpiration(false);
            }

            if (realm.getPortalLocation() == null || realm.getStatus() != RealmStatus.OPENED) continue;

            Location loc = realm.getPortalLocation().clone().add(0, 1, 0);

            if (Rank.isDev(Bukkit.getPlayer(realm.getOwner())) && !DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(Bukkit.getPlayer(realm.getOwner())))
                createDoubleHelix(loc);
            else if (DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(Bukkit.getPlayer(realm.getOwner())))
                DonationEffects.getInstance().spawnPlayerParticleEffects(loc);

            //loc.subtract(.5D, 2D, .5D);
            if (realm.getPropertyBoolean("peaceful"))
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.HAPPY_VILLAGER, loc.clone().add(0.5, 1.5, 0.5), 0, 0, 0, 0F, 20);

            //loc.subtract(.5D, 1.5D, .5D);
            if (realm.getPropertyBoolean("flight"))
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CLOUD, loc.clone().add(0.5, 1.5, 0.5), 0, 0, 0, 0F, 20);
        }
    }

    @EventHandler
    public void onIllegalItemDispense(ProjectileLaunchEvent event) {
        if (event.getEntityType() == EntityType.DROPPED_ITEM) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent event) {
        RealmToken realm = REALMS.getRealm(event.getSource().getLocation().getWorld());

        if (realm == null) return;
        ItemStack i = event.getItem();
        if (i == null) return;
        if (i.getType() == Material.AIR) return;
        if (GameAPI.isArmor(i) || GameAPI.isWeapon(i) || BankMechanics.getInstance().isBankNote(i) ||
                BankMechanics.getInstance().isGem(i) || ItemManager.isEnchantScroll(i) || ItemManager
                .isProtectScroll(i) || GameAPI.isOrb(i) || RepairAPI.isItemArmorScrap(i)) {
            i.setType(Material.AIR);
            event.setCancelled(true);
        }
    }

    private void createDoubleHelix(Location loc) {
        double radius;

        for (double y = 0; y < 2; y += 0.007) {
            radius = y / 3;
            double x = radius * Math.cos(3 * y);
            double z = radius * Math.sin(3 * y);

            double y2 = 3 - y;

            final Location loc2 = new Location(loc.getWorld(), loc.getX() + x + 0.5, loc.getY() + y2, loc.getZ() + z + 0.5);

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(),
                    () -> ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.RED_DUST, loc2, 0, 0, 0, 0, 1), (long) ((y + 1) * 20));
        }

        for (double y = 0; y < 2; y += 0.007) {
            radius = y / 3;
            double x = -(radius * Math.cos(3 * y));
            double z = -(radius * Math.sin(3 * y));

            double y2 = 5 - y;

            final Location loc2 = new Location(loc.getWorld(), loc.getX() + x + 0.5, loc.getY() + y2, loc.getZ() + z + 0.5);

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(),
                    () -> ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.RED_DUST, loc2, 0, 0, 0, 0, 1), (long) ((y + 1) * 20));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerUseOrb(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player p = event.getPlayer();
        if (p.getEquipment().getItemInMainHand() == null)
            return;
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getEquipment().getItemInMainHand());
        if (nmsStack == null) return;
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (!tag.hasKey("orb")) return;
        if (!tag.getString("orb").equalsIgnoreCase("flight") && !tag.getString("orb").equalsIgnoreCase("peace")) return;

        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);

        if (Cooldown.hasCooldown(event.getPlayer().getUniqueId())) return;
        Cooldown.addCooldown(event.getPlayer().getUniqueId(), 1000);

        if (!p.getWorld().getName().equalsIgnoreCase(p.getUniqueId().toString())) {
            // Trying to use in a realm that isn't theirs.
            if (tag.getString("orb").equalsIgnoreCase("flight"))
                p.sendMessage(ChatColor.RED + "You may only use an " + ChatColor.UNDERLINE + "Orb of Flight" + ChatColor.RED + " in your OWN realm.");
            else if (tag.getString("orb").equalsIgnoreCase("peace"))
                p.sendMessage(ChatColor.RED + "You may only use an " + ChatColor.UNDERLINE + "Orb of Peace" + ChatColor.RED + " in your OWN realm.");

            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }
        RealmToken realm = REALMS.getRealm(p.getUniqueId());


        if (tag.getString("orb").equalsIgnoreCase("flight")) {

            if (!realm.getPropertyBoolean("peaceful")) {
                p.sendMessage(ChatColor.RED + "You can only use an " + ChatColor.UNDERLINE + "Orb of Flight" + ChatColor.RED
                        + " in a realm with an active ORB OF PEACE effect.");
                return;
            }

            p.sendMessage("");
            p.sendMessage(ChatColor.AQUA + "Your realm will now be a " + ChatColor.BOLD + "FLY ENABLED ZONE" + ChatColor.AQUA
                    + " for 30 minute(s), or until logout.");
            p.sendMessage(ChatColor.GRAY + "Only YOU and anyone you add to your build list will be able to fly in your realm.");
            p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "FLYING ENABLED");

            for (Player pl : realm.getWorld().getPlayers()) {
                if (pl == null || (!realm.getBuilders().contains(pl.getUniqueId()) && !realm.getOwner().equals(pl.getUniqueId())))
                    continue;
                if (!realm.getOwner().equals(pl.getUniqueId()))
                    pl.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "FLYING ENABLED");
                pl.setAllowFlight(true);
            }

            RealmProperty<Boolean> property = (RealmProperty<Boolean>) realm.getProperty("flight");
            property.setExpiry(System.currentTimeMillis() + 1800000L);

            property.setValue(true);
            property.setAcknowledgeExpiration(true);
        } else if (tag.getString("orb").equalsIgnoreCase("peace")) {
            GamePlayer gp = GameAPI.getGamePlayer(p);

            if (gp.getPlayerAlignment() == KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " use an orb of peace while chaotic.");
                return;
            }

            p.sendMessage("");
            p.sendMessage(ChatColor.GREEN + "Your realm will now be a " + ChatColor.BOLD + "SAFE ZONE" + ChatColor.GREEN + " for 1 hour(s), or until logout.");
            p.sendMessage(ChatColor.GRAY + "All damage in your realm will be disabled for this time period.");
            p.getWorld().playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 10);

            RealmProperty<Boolean> property = (RealmProperty<Boolean>) realm.getProperty("peaceful");
            property.setExpiry(System.currentTimeMillis() + 3600000L);

            property.setValue(true);
            property.setAcknowledgeExpiration(true);

            // SET WORLD GUARD FLAG
            REALMS.setRealmRegion(REALMS.getRealmWorld(p.getUniqueId()), false);
            REALMS.updateRealmHologram(p.getUniqueId());
        }

        int amount = p.getInventory().getItemInMainHand().getAmount();
        ItemStack in_hand = p.getInventory().getItemInMainHand();

        if (amount <= 1) {
            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else if (amount > 1) {
            amount--;
            in_hand.setAmount(amount);
            p.getInventory().setItemInMainHand(in_hand);
        }
        p.updateInventory();
    }


    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;

        if (event.getTo().getY() <= 0) {
            RealmToken realm = REALMS.getRealm(event.getPlayer().getLocation().getWorld());

            if (realm == null) return;

            event.getPlayer().teleport(realm.getPortalLocation().clone().add(0, 1, 0));
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        RealmToken realm = REALMS.getRealm(event.getPlayer().getLocation().getWorld());

        Player p = event.getPlayer();

        if (realm == null) return;

        if (event.hasBlock())
            if (event.getClickedBlock().getType().equals(Material.ENCHANTMENT_TABLE)) {
                event.setCancelled(true);
                return;
            }

        if (event.hasItem())
            if (event.getItem().getType().equals(Material.ITEM_FRAME)) {
                event.setCancelled(true);
                return;
            }

        if (!realm.getOwner().equals(p.getUniqueId()) && !realm.getBuilders().contains(p.getUniqueId()) && !Rank.isGM(p)) {
            p.sendMessage(ChatColor.RED + "You aren't authorized to build in " + Bukkit.getPlayer(realm.getOwner()).getName() + "'s realm.");
            p.sendMessage(ChatColor.GRAY + Bukkit.getPlayer(realm.getOwner()).getName() + " will have to " + ChatColor.UNDERLINE + "Sneak Left Click" + ChatColor.GRAY +
                    " you with their Realm Portal Rune to add you to their builder list.");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPortalDestory(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (!event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;

        RealmToken realm = REALMS.getRealm(event.getClickedBlock().getLocation());
        if (realm == null) return;

        if (event.getClickedBlock().getType().equals(Material.PORTAL) && realm.getOwner().equals(event.getPlayer().getUniqueId()) ||
                Rank.isGM(event.getPlayer())) {
            REALMS.closeRealmPortal(realm.getOwner(), true, "");
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        if (p.getWorld().equals(Bukkit.getWorlds().get(0))) return;

        RealmToken realm = REALMS.getRealm(p.getLocation().getWorld());

        if (realm == null) return;

        if (p.getGameMode() == GameMode.CREATIVE) return;
        String world = e.getBlock().getWorld().getName();
        if (e.getBlock().getType() == Material.PORTAL) {
            e.setCancelled(true);
            return;
        }

        int realm_tier = REALMS.getRealmTier(realm.getOwner());
        int max_size = REALMS.getRealmDimensions(realm_tier) + 16;

        int max_y = 128;
        Block b = e.getBlock();
        if (!(Rank.isGM(p)))
            if (Math.round(b.getX() - 0.5) > max_size || Math.round(b.getX() - 0.5) < 16 || Math.round(b.getZ() - 0.5) > max_size
                    || Math.round(b.getZ() - 0.5) < 16 || (b.getY() > (max_y + (max_size) + 1)) || (b.getY() < (max_y - (max_size) - 1))) {
                e.setCancelled(true);
                p.updateInventory();
                return;
            }

        if (!Rank.isGM(p) && (e.getBlock().getType() == Material.TRAPPED_CHEST || e.getBlock().getType() == Material.GOLD_BLOCK)) {
            if (e.getBlock().getType() == Material.TRAPPED_CHEST) {
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " place this "
                        + e.getBlock().getType().name().toUpperCase() + " as it is an illegal item.");
            }
            p.updateInventory();
            e.setCancelled(true);
            return;
        }


        if (!realm.getOwner().equals(p.getUniqueId()) && !realm.getBuilders().contains(p.getUniqueId()) && !Rank.isGM(p)) {
            p.sendMessage(ChatColor.RED + "You aren't authorized to build in " + Bukkit.getPlayer(realm.getOwner()).getName() + "'s realm.");
            p.sendMessage(ChatColor.GRAY + Bukkit.getPlayer(realm.getOwner()).getName() + " will have to " + ChatColor.UNDERLINE + "Sneak Left Click" + ChatColor.GRAY +
                    " you with their Realm Portal Rune to add you to their builder list.");
            e.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAddBuilder(EntityDamageByEntityEvent event) {
        if (!GameAPI.isPlayer(event.getDamager()) || !GameAPI.isPlayer(event.getEntity())) return;

        Player p = (Player) event.getDamager();
        Player target = (Player) event.getEntity();

        if (p.getEquipment().getItemInMainHand() == null || p.getEquipment().getItemInMainHand().getType() != Material.NETHER_STAR)
            return;


        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getEquipment().getItemInMainHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (tag.hasKey("realmPortalRune") && !(tag.getString("realmPortalRune").equalsIgnoreCase("true"))) return;

        event.setCancelled(true);
        event.setDamage(0);

        if (!p.isSneaking()) return;

        if (!REALMS.isRealmCached(p.getUniqueId())) {
            p.sendMessage(ChatColor.GREEN + "You must open your realm portal to add builders.");
            return;
        }

        RealmToken realm = REALMS.getRealm(p.getUniqueId());

        if (!(FriendHandler.getInstance().areFriends(p, target.getUniqueId()))) {
            p.sendMessage(ChatColor.RED + "Cannot add a non-buddy to realm build list.");
            p.sendMessage(ChatColor.GRAY + "Goto your friends list in the character profile to add '" + ChatColor.BOLD + target.getName() + ChatColor.GRAY
                    + "' as friend.");
            return;
        }

        if (!realm.getBuilders().contains(target.getUniqueId())) {
            p.sendMessage(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "ADDED " + ChatColor.RESET + ChatColor.GREEN + "" + ChatColor.BOLD + target.getName()
                    + ChatColor.GREEN + " to your realm builder list.");
            p.sendMessage(ChatColor.GRAY + target.getName()
                    + " can now place/destroy blocks in your realm until you logout of your current game session.");
            target.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "ADDED" + ChatColor.GREEN + " to " + p.getName() + "'s build list.");
            target.sendMessage(ChatColor.GRAY + "You can now place/destroy blocks in their realm until the end of their game session.");

            realm.getBuilders().add(target.getUniqueId());

            if (target.getWorld().getName().equals(p.getUniqueId().toString()) && realm.getPropertyBoolean("flight")) {
                target.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "FLYING ENABLED");
                target.setAllowFlight(true);
            }

        } else {
            p.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "REMOVED " + ChatColor.RESET + ChatColor.RED + "" + ChatColor.BOLD + target.getName()
                    + ChatColor.RED + " from your realm builder list.");
            p.sendMessage(ChatColor.GRAY + target.getName() + " can no longer place/destroy blocks in your realm.");
            target.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "REMOVED " + ChatColor.RED + "from " + p.getName() + "'s builder list.");
            target.sendMessage(ChatColor.GRAY + "You can no longer place/destroy blocks/fly in their realm.");

            realm.getBuilders().remove(target.getUniqueId());

            if (target.getWorld().getName().equals(p.getUniqueId().toString()) && realm.getPropertyBoolean("flight"))
                target.setAllowFlight(false);

        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCreativeBlockBreak(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!(e.hasBlock()) || e.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (p.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (p.getWorld().equals(Bukkit.getWorlds().get(0))) return;


        RealmToken realm = REALMS.getRealm(p.getWorld());

        if (realm == null) return;


        if (!realm.getOwner().equals(p.getUniqueId()) && !realm.getBuilders().contains(p.getUniqueId()) && !Rank.isGM(p)) {
            p.sendMessage(ChatColor.RED + "You aren't authorized to build in " + Bukkit.getPlayer(realm.getOwner()).getName() + "'s realm.");
            p.sendMessage(ChatColor.GRAY + Bukkit.getPlayer(realm.getOwner()).getName() + " will have to " + ChatColor.UNDERLINE + "Sneak Left Click" + ChatColor.GRAY +
                    " you with their Realm Portal Rune to add you to their builder list.");
            e.setCancelled(true);
            return;
        }

        Block b = e.getClickedBlock();
        e.setCancelled(true);

        Material m = b.getType();
        if (m == Material.AIR || m == Material.PORTAL) {
            return;
        }
        ItemStack loot = (new ItemStack(b.getType(), 1, b.getData()));

        if (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER || b.getType() == Material.LAVA
                || b.getType() == Material.STATIONARY_LAVA) {
            return;
        }

        if (b.getType() == Material.CHEST) {
            Chest c = (Chest) b.getState();
            Inventory c_inv = c.getInventory();
            deleteIllegalItemsInInventory(c_inv, p);
            int in_chest = getUsedSlots(c_inv);
            int available_on_player = getAvailableSlots(p.getInventory());
            if (in_chest > available_on_player) {
                p.sendMessage(ChatColor.RED + "You do not have enough room in your inventory for all the items in this chest.");
                p.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "REQ: " + in_chest + " slots");
                return;
            }
            for (ItemStack is : c_inv.getContents()) {
                if (is != null && is.getType() != Material.AIR) {
                    p.getInventory().setItem(p.getInventory().firstEmpty(), is);
                }
            }

            c_inv.clear();
        }

        if (b.getState() instanceof InventoryHolder) {
            deleteIllegalItemsInInventory(((InventoryHolder) b.getState()).getInventory(), p);
        }

        if (b.getType() == Material.ITEM_FRAME) {
            b.setType(Material.AIR);
            loot.setTypeId(0);
        }

        if (b.getType() == Material.SKULL) {
            loot.setType(Material.SKULL_ITEM);
        }
        if (b.getType() == Material.DOUBLE_PLANT) {
            if (b.getData() == (byte) 0) {
                loot.setType(Material.DOUBLE_PLANT);
            } else {
                e.setCancelled(true);
                e.setUseInteractedBlock(Event.Result.DENY);
                b.setType(Material.DOUBLE_PLANT);
                loot.setType(Material.AIR);
            }
        }
        if (b.getType() == Material.REDSTONE_TORCH_ON) {
            loot.setType(Material.REDSTONE_TORCH_OFF);
        }

        if (b.getTypeId() == 64 || b.getTypeId() == 71) {
            // Door break, ensure they don't get the stupid bottom part.
            Location l = b.getLocation();
            int block_id = b.getTypeId();
            if (l.add(0, 1, 0).getBlock().getTypeId() == block_id) {
                // Door.
                if (block_id == 64) {
                    loot.setTypeId(324);
                }
                if (block_id == 71) {
                    loot.setTypeId(330);
                }
            }

            l.subtract(0, 1, 0);
            if (l.subtract(0, 1, 0).getBlock().getTypeId() == block_id) {
                if (block_id == 64) {
                    loot.setTypeId(324);
                }
                if (block_id == 71) {
                    loot.setTypeId(330);
                }
                l.getBlock().setType(Material.AIR);
            }
        }

        if (b.getType() == Material.REDSTONE_WIRE) {
            loot = new ItemStack(Material.REDSTONE, 1);
            // loot.setDurability((short)0);
        }

        if (b.getType() == Material.PISTON_BASE) {
            loot.setTypeId(33);
        }

        if (b.getType() == Material.PISTON_MOVING_PIECE || b.getType() == Material.PISTON_EXTENSION) {
            loot.setTypeId(0);
        }

        if (b.getType() == Material.CHEST) {
            loot = ItemManager.createItem(Material.CHEST, ChatColor.GREEN + "Realm Chest", new String[]{ChatColor.GRAY + "This chest can only be placed in realms."});
        }

        if (b.getType() == Material.PISTON_STICKY_BASE) {
            loot.setTypeId(29);
        }

        if (b.getType() == Material.WALL_SIGN) {
            loot.setType(Material.SIGN);
        }

        if (b.getType() == Material.SIGN_POST) {
            loot.setType(Material.SIGN);
        }

        if (b.getType() == Material.BED) {
            loot.setType(Material.AIR);
        }

        if (b.getType() == Material.BED_BLOCK) {
            loot.setType(Material.AIR);
        }

        if (b.getType() == Material.REDSTONE_TORCH_OFF) {
            loot.setType(Material.REDSTONE_TORCH_ON);
        }

        if (b.getTypeId() == 93 || b.getTypeId() == 94) {
            loot.setTypeId(356);
        }

        b.getLocation().getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getTypeId());
        b.setType(Material.AIR);

        int amount = loot.getAmount();
        int max_stack = loot.getMaxStackSize();
        Inventory i = p.getInventory();
        int slot = -1;

        HashMap<Integer, ? extends ItemStack> invItems = i.all(loot.getType());
        for (Map.Entry<Integer, ? extends ItemStack> entry : invItems.entrySet()) {
            ItemStack item = entry.getValue();
            int stackAmount = item.getAmount();
            if (item.getDurability() != loot.getDurability()) {
                continue;
            }
            if ((stackAmount + amount) <= max_stack) {
                slot = entry.getKey();
                item.setAmount(item.getAmount() + amount);
                b.setType(Material.AIR);
                p.getInventory().setItem(slot, item);
                p.updateInventory();
                break; // Set stack more, no need to add a new item to it.
            }
        }

        if (slot == -1) {
            // We never found a stack to add it to.
            if (p.getInventory().firstEmpty() == -1) {
                // No space!
                p.sendMessage(ChatColor.RED + "No inventory space.");
                e.setCancelled(true);
                b.setType(m); // Revert the block.
                return;
            }
            // There's room!
            p.getInventory().setItem(p.getInventory().firstEmpty(), loot);
        }

        p.updateInventory();
    }


    public int getAvailableSlots(Inventory i) {
        int count = 0;
        for (ItemStack is : i.getStorageContents()) {
            if (is == null || is.getType() == Material.AIR) {
                count++;
            }
        }
        return count;
    }

    public int getUsedSlots(Inventory i) {
        int count = 0;
        for (ItemStack is : i.getContents()) {
            if (is != null && is.getType() != Material.AIR) {
                count++;
            }
        }
        return count;
    }

    @EventHandler
    public void onRealmChestClose(InventoryCloseEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0)) || event.getPlayer().getWorld().getName().contains("DUNGEON")) return;
        if (!event.getInventory().getName().contains("container.chest") && !event.getInventory().getName().contains
                ("Realm Chest") && !event.getInventory().getName().equalsIgnoreCase("container.chestDouble")
                && !(event.getInventory().getName().equalsIgnoreCase("container.minecart"))
                && !(event.getInventory().getName().equalsIgnoreCase("container.dispenser")) && !(event.getInventory
                ().getName().equalsIgnoreCase("container.hopper"))
                && !(event.getInventory().getName().equalsIgnoreCase("container.dropper"))) {
            return;
        }
        deleteIllegalItemsInInventory(event.getInventory(), (Player)event.getPlayer());
    }

    @EventHandler
    public void onRealmContainerOpen(InventoryOpenEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0)) || event.getPlayer().getWorld().getName().contains("DUNGEON")) return;
        if (!event.getInventory().getName().contains("container.chest") && !event.getInventory().getName().contains
                ("Realm Chest") && !event.getInventory().getName().equalsIgnoreCase("container.chestDouble")
                && !(event.getInventory().getName().equalsIgnoreCase("container.minecart"))
                && !(event.getInventory().getName().equalsIgnoreCase("container.dispenser")) && !(event.getInventory
                ().getName().equalsIgnoreCase("container.hopper"))
                && !(event.getInventory().getName().equalsIgnoreCase("container.dropper"))) {
            return;
        }
        deleteIllegalItemsInInventory(event.getInventory(), (Player)event.getPlayer());
    }

    private static int deleteIllegalItemsInInventory(Inventory inv, Player p) {
        int deletedItems = 0;
        for (ItemStack i : inv.getContents()) {
            if (i == null) continue;
            if (i.getType() == Material.AIR) continue;
            if (GameAPI.isArmor(i) || GameAPI.isWeapon(i) || BankMechanics.getInstance().isBankNote(i) ||
                    BankMechanics.getInstance().isGem(i) || ItemManager.isEnchantScroll(i) || ItemManager
                    .isProtectScroll(i) || GameAPI.isOrb(i) || RepairAPI.isItemArmorScrap(i)) {
                inv.remove(i);
                deletedItems++;
            }
        }
        if (deletedItems > 0 && p != null) {
            p.sendMessage(ChatColor.RED + "Removed " + ChatColor.BOLD + deletedItems + " illegal item" +
                    (deletedItems > 1 ? "s" : "") + ChatColor.RED + " from your realm container.");
        }
        return deletedItems;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();

        if (!event.getInventory().getName().equalsIgnoreCase("container.chest") && !event.getInventory().getName().contains("Realm Chest") && !event.getInventory().getName().equalsIgnoreCase("container.chestDouble")
                && !(event.getInventory().getName().equalsIgnoreCase("container.minecart"))
                && !(event.getInventory().getName().equalsIgnoreCase("container.dispenser")) && !(event.getInventory().getName().equalsIgnoreCase("container.hopper"))
                && !(event.getInventory().getName().equalsIgnoreCase("container.dropper"))) {
            return;
        }

        if (p.getWorld().equals(Bukkit.getWorlds().get(0)) || p.isOp()) return;

        if (!event.getInventory().getName().equalsIgnoreCase("container.hopper") && event.getAction() == InventoryAction.PICKUP_ALL) {
            // Trying to grab all items from a chest in a realm.
            String realm_name = p.getWorld().getName();
            if (!(realm_name.equalsIgnoreCase(p.getUniqueId().toString()) || (REALMS.getRealm(p.getLocation()).getBuilders().contains(p.getUniqueId())))) {
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
                return;
            }
        }

        if (p.isOp()) {
            return;
        }

        int slot_num = event.getRawSlot();
        if (slot_num < event.getInventory().getSize()) {
            // An item inside the chest is being clicked.
            if (event.isShiftClick() || (event.getCursor() == null && event.getCurrentItem() != null)) {
                return; // Don't care if they're moving stuff OUT of inventory.
            }

            if (event.getCursor() != null) {
                // They're placing an item into the chest.
                ItemStack cursor = event.getCursor();
                if (Item.ItemType.isArmor(cursor) || Item.ItemType.isWeapon(cursor) || cursor.getType() == Material
                        .EMERALD
                        || cursor.getType() == Material.PAPER || BankMechanics.getInstance().isGemPouch(cursor) ||
                        BankMechanics.getInstance().isGem(cursor) || BankMechanics.getInstance().isBankNote(cursor)
                        || ItemManager.isEnchantScroll(cursor) || ItemManager.isProtectScroll(cursor) || GameAPI.isOrb(cursor) || RepairAPI.isItemArmorScrap(cursor)) {
                    event.setCancelled(true);
                    event.setCursor(cursor);
                    p.updateInventory();
                    p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " deposit weapons, armor, or gems in realm blocks.");
                    p.sendMessage(ChatColor.GRAY + "Deposit those items in your BANK CHEST.");
                }
            }
        } else if (slot_num >= event.getInventory().getSize()) {
            // Clicking in own inventory.
            ItemStack item = null;
            if (event.isShiftClick()) {
                item = event.getCurrentItem();
            }
            else if (event.getAction() == InventoryAction.HOTBAR_SWAP){
                item = event.getInventory().getItem(event.getHotbarButton());
            }
            else {
                return;
            }
            if (Item.ItemType.isArmor(item) || Item.ItemType.isWeapon(item) || item.getType() == Material
                    .EMERALD
                    || item.getType() == Material.PAPER || BankMechanics.getInstance().isGemPouch(item) ||
                    BankMechanics.getInstance().isGem(item) || BankMechanics.getInstance().isBankNote(item)
                    || ItemManager.isEnchantScroll(item) || ItemManager.isProtectScroll(item) || GameAPI.isOrb(item) || RepairAPI.isItemArmorScrap(item)) {
                event.setCancelled(true);
                event.setCurrentItem(item);
                p.updateInventory();
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " deposit weapons, armor, or gems in realm blocks.");
                p.sendMessage(ChatColor.GRAY + "Deposit those items in your BANK CHEST.");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEnterPortal(PlayerPortalEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) {
            if (EntityAPI.hasPetOut(event.getPlayer().getUniqueId())) {
                Entity pet = Entities.PLAYER_PETS.get(event.getPlayer().getUniqueId());
                pet.dead = true;
                EntityAPI.removePlayerPetList(event.getPlayer().getUniqueId());
            }
            if (EntityAPI.hasMountOut(event.getPlayer().getUniqueId())) {
                Entity mount = Entities.PLAYER_MOUNTS.get(event.getPlayer().getUniqueId());
                mount.dead = true;
                EntityAPI.removePlayerMountList(event.getPlayer().getUniqueId());
            }

            if (!CombatLog.isInCombat(event.getPlayer())) {
                RealmToken realm = REALMS.getRealm(event.getFrom());

                if (realm == null) return;

                if (!REALMS.isRealmLoaded(realm.getOwner()))
                    return;

                if (realm.getStatus() != RealmStatus.OPENED) return;

                // SAVES THEIR LOCATION
                DatabaseAPI.getInstance().update(event.getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_LOCATION, GameAPI.locationToString(event.getFrom()), true);
                event.setTo(REALMS.getRealmWorld(realm.getOwner()).getSpawnLocation().clone().add(0, 2, 0));

            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot enter a realm while in combat!");
            }
        } else if (REALMS.getRealm(event.getPlayer().getLocation().getWorld()) != null) {
            if (EntityAPI.hasPetOut(event.getPlayer().getUniqueId())) {
                Entity pet = Entities.PLAYER_PETS.get(event.getPlayer().getUniqueId());
                pet.dead = true;
                EntityAPI.removePlayerPetList(event.getPlayer().getUniqueId());
            }
            if (EntityAPI.hasMountOut(event.getPlayer().getUniqueId())) {
                Entity mount = Entities.PLAYER_MOUNTS.get(event.getPlayer().getUniqueId());
                mount.dead = true;
                EntityAPI.removePlayerMountList(event.getPlayer().getUniqueId());
            }

            RealmToken realm = REALMS.getRealm(event.getPlayer().getLocation().getWorld());
            event.setTo(realm.getPortalLocation().clone().add(0, 1, 0));
        }
    }
}
