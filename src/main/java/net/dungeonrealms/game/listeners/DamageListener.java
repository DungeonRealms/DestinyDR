package net.dungeonrealms.game.listeners;

import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.events.PlayerEnterRegionEvent;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.handlers.EnergyHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.handlers.ProtectionHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mechanics.PlayerManager;
import net.dungeonrealms.game.miscellaneous.Cooldown;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.combat.CombatLogger;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.PowerMove;
import net.dungeonrealms.game.world.entities.powermoves.PowerStrike;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRWitch;
import net.dungeonrealms.game.world.entities.types.monsters.boss.Boss;
import net.dungeonrealms.game.world.entities.utils.BuffUtils;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import net.dungeonrealms.game.world.items.Attribute;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.dungeonrealms.game.world.party.Affair;
import net.dungeonrealms.game.world.spawning.BaseMobSpawner;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.*;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Nick on 9/17/2015.
 */
public class DamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onSufficate(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getCause() == DamageCause.SUFFOCATION) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * This event listens for EnderCrystal explosions.
     * Which are buffs.. with the correct nbt at least.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBuffExplode(EntityExplodeEvent event) {
        if (event.getEntity().getWorld().getName().contains("DUNGEON")) return;
        if (!(event.getEntity().hasMetadata("type"))) return;
        event.blockList().clear();
        event.setYield(0.1F);
        event.setCancelled(true);
        if (event.getEntity().getMetadata("type").get(0).asString().equalsIgnoreCase("buff")) {
            event.setCancelled(true);
            event.blockList().clear();
            List<Player> toBuff = new ArrayList<>();
            for (Entity entity : event.getEntity().getNearbyEntities(8D, 8D, 8D)) {
                if (!API.isPlayer(entity)) continue;
                toBuff.add((Player) entity);
            }
            BuffUtils.handleBuffEffects(event.getEntity(), toBuff);
        }
    }

    /**
     * Cancel World Guard PVP flag if players are dueling.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void disallowPVPEvent(DisallowedPVPEvent event) {
        Player p1 = event.getAttacker();
        Player p2 = event.getDefender();
        if (DuelingMechanics.isDueling(p1.getUniqueId()) && DuelingMechanics.isDuelPartner(p1.getUniqueId(), p2.getUniqueId())) {
            DuelOffer offer = DuelingMechanics.getOffer(p1.getUniqueId());
            if (offer.canFight) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void playerBreakArmorStand(EntityDamageByEntityEvent event) {
        if (!(API.isPlayer(event.getDamager()))) return;
        if (((Player) event.getDamager()).getGameMode() != GameMode.CREATIVE) return;
        //Armor Stand Spawner check.
        if (event.getEntity().getType() != EntityType.ARMOR_STAND) return;
        if (!event.getEntity().hasMetadata("type")) {
            event.setCancelled(false);
            event.getEntity().remove();
            return;
        }
        if (event.getEntity().getMetadata("type").get(0).asString().equalsIgnoreCase("spawner")) {
            Player attacker = (Player) event.getDamager();
            if (attacker.isOp() || attacker.getGameMode() == GameMode.CREATIVE) {
                ArrayList<BaseMobSpawner> list = SpawningMechanics.getALLSPAWNERS();
                for (BaseMobSpawner current : list) {
                    if (current.getLoc().getBlockX() == event.getEntity().getLocation().getBlockX() && current.getLoc().getBlockY() == event.getEntity().getLocation().getBlockY() &&
                            current.getLoc().getBlockZ() == event.getEntity().getLocation().getBlockZ()) {
                        current.remove();
                        current.kill();
                        break;
                    }
                }
            } else {

                event.setDamage(0);
                event.setCancelled(true);
            }
        } else {
            event.setDamage(0);
            event.setCancelled(true);
        }
    }

    /**
     * Listen for the players weapon hitting an entity
     * Used for calculating damage based on player weapon
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onPlayerHitEntity(EntityDamageByEntityEvent event) {
<<<<<<< Temporary merge branch 1
        if ((!(API.isPlayer(event.getDamager()))) && (!DamageAPI.isBowProjectile(event.getDamager()) && (!DamageAPI.isStaffProjectile(event.getDamager()))))
=======
        LivingEntity leDamageSource = event.getDamager() instanceof LivingEntity ? (LivingEntity) event.getDamager()
                : (LivingEntity) ((Projectile) event.getDamager()).getShooter();

        if ((!(API.isPlayer(leDamageSource))) && (!DamageAPI.isBowProjectile(leDamageSource) && (!DamageAPI.isStaffProjectile(leDamageSource))))
>>>>>>> Temporary merge branch 2
            return;
        if (!(event.getEntity() instanceof LivingEntity) && !(API.isPlayer(event.getEntity()))) return;
        if (Entities.PLAYER_PETS.containsValue(((CraftEntity) event.getEntity()).getHandle())) return;
        if (Entities.PLAYER_MOUNTS.containsValue(((CraftEntity) event.getEntity()).getHandle())) return;
        if (event.getEntity() instanceof LivingEntity) {
            if (!(event.getEntity() instanceof Player)) {
                if (!event.getEntity().hasMetadata("type")) return;
            }
        }


        if (API.isPlayer(leDamageSource) && event.getEntity().getLocation().distance(leDamageSource.getLocation()) >= 10D)
            ((Player) leDamageSource).playSound(leDamageSource.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

        if (API.isInSafeRegion(leDamageSource.getLocation()) || API.isInSafeRegion(event.getEntity().getLocation())) {
            event.setDamage(0);
            event.setCancelled(true);
            if (API.isPlayer(leDamageSource)) {
                ((Player) leDamageSource).updateInventory();
            }
            return;
        }

        if (API.isPlayer(event.getEntity()) && API.isPlayer(leDamageSource)) {
            if (!DuelingMechanics.isDueling(leDamageSource.getUniqueId())) {
                if (API.isInSafeRegion(event.getEntity().getLocation())) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    ((Player) leDamageSource).updateInventory();
                    return;
                } else if (ProtectionHandler.getInstance().hasNewbieProtection((Player) event.getEntity())) {
                    leDamageSource.sendMessage(ChatColor.RED + "The player you are attempting to attack has " +
                            "newbie " +
                            "protection! You cannot attack them.");
                    event.getEntity().sendMessage(ChatColor.GREEN + "Your " + ChatColor.UNDERLINE + "NEWBIE " +
                            "PROTECTION" + ChatColor.GREEN + " has prevented " + leDamageSource.getCustomName() +
                            ChatColor.GREEN + " from attacking you!");
                    event.getEntity();
                    event.setCancelled(true);
                    event.setDamage(0);
                    ((Player) leDamageSource).updateInventory();
                    return;
                }
            }
        }

        //Make sure the player is HOLDING something!
        double finalDamage = 0;
        if (API.isPlayer(leDamageSource)) {
            if (API.isNonPvPRegion(leDamageSource.getLocation()) || API.isNonPvPRegion(event.getEntity().getLocation())) {
                if (API.isPlayer(event.getEntity()) && API.isPlayer(leDamageSource)) {
                    if (DuelingMechanics.isDueling(event.getEntity().getUniqueId()) && DuelingMechanics.isDueling(leDamageSource.getUniqueId())) {
                        if (!DuelingMechanics.isDuelPartner(leDamageSource.getUniqueId(), event.getEntity().getUniqueId())) {
                            event.setCancelled(true);
                            event.setDamage(0);
                            return;
                        }
                    } else {
                        event.setCancelled(true);
                        event.setDamage(0);
                        return;
                    }
                }
            }

            if (API.isPlayer(event.getEntity())) {
                if (Affair.getInstance().areInSameParty((Player) leDamageSource, (Player) event.getEntity())) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }

                if (!GuildDatabaseAPI.get().isGuildNull(leDamageSource.getUniqueId()) && !GuildDatabaseAPI.get().isGuildNull(event.getEntity().getUniqueId()))
                    if (GuildDatabaseAPI.get().getGuildOf(leDamageSource.getUniqueId()).equals(GuildDatabaseAPI.get().getGuildOf(event.getEntity().getUniqueId()))) {
                        event.setCancelled(true);
                        event.setDamage(0);
                        return;
                    }
            }

            Player attacker = (Player) leDamageSource;

            if (attacker.hasPotionEffect(PotionEffectType.SLOW_DIGGING) || EnergyHandler.getPlayerCurrentEnergy(attacker) <= 0) {
                event.setCancelled(true);
                event.setDamage(0);
                attacker.playSound(attacker.getLocation(), Sound.ENTITY_WOLF_PANT, 12F, 1.5F);
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, event.getEntity().getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 40);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return;
            }


            if (event.getEntity() instanceof Player && leDamageSource instanceof Player) {
                if (Cooldown.hasCooldown(attacker.getUniqueId())) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
                Cooldown.addCooldown(attacker.getUniqueId(), 250L);
                event.getEntity().setVelocity(event.getEntity().getVelocity().divide(new Vector(2, 2.5, 2)));
            }

            if (CombatLog.isInCombat(attacker)) {
                CombatLog.updateCombat(attacker);
            } else {
                CombatLog.addToCombat(attacker);
            }
            EnergyHandler.removeEnergyFromPlayerAndUpdate(attacker.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(attacker.getEquipment().getItemInMainHand()));


            //Check if it's a {WEAPON} the player is hitting with. Once of our custom ones!
            if (!API.isWeapon(attacker.getEquipment().getItemInMainHand())) {
                event.setDamage(1);
                return;
            }

            ItemType weaponType = new Attribute(attacker.getInventory().getItemInMainHand()).getItemType();
            Item.ItemTier tier = new Attribute(attacker.getInventory().getItemInMainHand()).getItemTier();

            switch (weaponType) {
                case BOW:
                    switch (tier) {
                        case TIER_1:
                            DamageAPI.knockbackEntity(attacker, event.getEntity(), 1.2);
                            break;
                        case TIER_2:
                            DamageAPI.knockbackEntity(attacker, event.getEntity(), 1.5);
                            break;
                        case TIER_3:
                            DamageAPI.knockbackEntity(attacker, event.getEntity(), 1.8);
                            break;
                        case TIER_4:
                            DamageAPI.knockbackEntity(attacker, event.getEntity(), 2.0);
                            break;
                        case TIER_5:
                            DamageAPI.knockbackEntity(attacker, event.getEntity(), 2.2);
                            break;
                        default:
                            break;
                    }
                case STAFF:
                    event.setDamage(0);
                    event.setCancelled(true);
                    return;
                default:
                    break;
            }

            finalDamage = DamageAPI.calculateWeaponDamage(attacker, (LivingEntity) event.getEntity());

            if (API.isPlayer(leDamageSource) && API.isPlayer(event.getEntity())) {
                if (API.getGamePlayer((Player) event.getEntity()) != null && API.getGamePlayer((Player) leDamageSource) != null) {
                    if (API.getGamePlayer((Player) event.getEntity()).getPlayerAlignment() == KarmaHandler.EnumPlayerAlignments.LAWFUL) {
                        if (API.getGamePlayer((Player) leDamageSource).getPlayerAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, leDamageSource.getUniqueId()).toString())) {
                                if (finalDamage >= HealthHandler.getInstance().getPlayerHPLive((Player) event.getEntity())) {
                                    event.setCancelled(true);
                                    event.setDamage(0);
                                    leDamageSource.sendMessage(ChatColor.YELLOW + "Your Chaotic Prevention Toggle has activated preventing the death of " + event.getEntity().getName() + "!");
                                    event.getEntity().sendMessage(ChatColor.YELLOW + leDamageSource.getName() + " has their Chaotic Prevention Toggle ON, your life has been spared!");
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } else if (DamageAPI.isBowProjectile(leDamageSource)) { // bow
            Projectile attackingArrow = (Projectile) leDamageSource;
            if (!(attackingArrow.getShooter() instanceof Player)) return;
            finalDamage = DamageAPI.calculateProjectileDamage((Player) attackingArrow.getShooter(), (LivingEntity) event.getEntity(), attackingArrow);
            if (CombatLog.isInCombat(((Player) attackingArrow.getShooter()))) {
                CombatLog.updateCombat(((Player) attackingArrow.getShooter()));
            } else {
                CombatLog.addToCombat(((Player) attackingArrow.getShooter()));
            }
        } else if (DamageAPI.isStaffProjectile(leDamageSource)) { // staff
            Projectile staffProjectile = (Projectile) leDamageSource;
            if (!(staffProjectile.getShooter() instanceof Player)) return;
            finalDamage = DamageAPI.calculateProjectileDamage((Player) staffProjectile.getShooter(), (LivingEntity) event.getEntity(), staffProjectile);
            if (CombatLog.isInCombat(((Player) staffProjectile.getShooter()))) {
                CombatLog.updateCombat(((Player) staffProjectile.getShooter()));
            } else {
                CombatLog.addToCombat(((Player) staffProjectile.getShooter()));
            }
        }
        event.setDamage(finalDamage);


        LivingEntity entity = (LivingEntity) event.getEntity();
        if (!entity.hasMetadata("tier")) return;
        if (PowerMove.chargedMonsters.contains(entity.getUniqueId()) || PowerMove.chargingMonsters.contains(entity.getUniqueId()))
            return;

        int tier = entity.getMetadata("tier").get(0).asInt();
        Random rand = new Random();
        int powerChance = 0;
        if (entity.hasMetadata("elite")) {
            switch (tier) {
                case 1:
                    powerChance = 5;
                    break;
                case 2:
                    powerChance = 7;
                    break;
                case 3:
                    powerChance = 10;
                    break;
                case 4:
                    powerChance = 13;
                    break;
                case 5:
                    powerChance = 20;
                    break;

            }
            if (rand.nextInt(100) <= powerChance) {
                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1F, 4.0F);
                PowerMove.doPowerMove("whirlwind", entity, null);

            }
        } else if (event.getEntity().hasMetadata("boss")) {
            if (event.getEntity() instanceof CraftLivingEntity) {
                Boss b = (Boss) ((CraftLivingEntity) event.getEntity()).getHandle();
                b.onBossHit(event);
            }
        } else {
            switch (tier) {
                case 1:
                    powerChance = 5;
                    break;
                case 2:
                    powerChance = 7;
                    break;
                case 3:
                    powerChance = 10;
                    break;
                case 4:
                    powerChance = 13;
                    break;
                case 5:
                    powerChance = 20;
                    break;

            }

            if (rand.nextInt(100) <= powerChance) {
                PowerMove.doPowerMove("powerstrike", entity, null);
            }
        }
    }

    /**
     * Makes mobs untarget a player after they have entered a safezone.
     *
     * @param event
     */
    public void onPlayerEnterSafezone(PlayerEnterRegionEvent event) {
        if (API.isInSafeRegion(event.getPlayer().getLocation())) {
            for (Entity ent : event.getPlayer().getNearbyEntities(10, 10, 10)) {
                if (!(ent instanceof Creature)) continue;
                ((Creature) ent).setTarget(null);
            }
        }
    }

    /**
     * Listen for the monsters hitting a player
     * Used for calculating damage based on mob weapon
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onMonsterHitPlayer(EntityDamageByEntityEvent event) {
        if (API.isPlayer(event.getDamager()))
            return;
        if ((!(event.getDamager() instanceof LivingEntity)) && (!DamageAPI.isBowProjectile(event.getDamager()) && (!DamageAPI.isStaffProjectile(event.getDamager()))))
            return;
        if (!(API.isPlayer(event.getEntity()))) return;
        if (API.isInSafeRegion(event.getDamager().getLocation()) || API.isInSafeRegion(event.getEntity().getLocation())) {
            event.setCancelled(true);
            event.setDamage(0);
            return;
        }
        double finalDamage = 0;
        Player player = (Player) event.getEntity();
        if (event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            EntityEquipment attackerEquipment = attacker.getEquipment();
            if (attackerEquipment.getItemInMainHand() == null) return;
            attackerEquipment.getItemInMainHand().setDurability(((short) -1));
            //Check if it's a {WEAPON} the mob is hitting with. Once of our custom ones!
            if (!API.isWeapon(attackerEquipment.getItemInMainHand())) return;
            finalDamage = DamageAPI.calculateWeaponDamage(attacker, (LivingEntity) event.getEntity());
            if (CombatLog.isInCombat(player)) {
                CombatLog.updateCombat(player);
            } else {
                CombatLog.addToCombat(player);
            }
        } else if (DamageAPI.isBowProjectile(event.getDamager())) {
            Projectile attackingArrow = (Projectile) event.getDamager();
            if (!(attackingArrow.getShooter() instanceof CraftLivingEntity)) return;
            if (((CraftLivingEntity) attackingArrow.getShooter()).hasMetadata("type")) {
                if (!(attackingArrow.getShooter() instanceof Player) && !(event.getEntity() instanceof Player)) {
                    attackingArrow.remove();
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
                finalDamage = DamageAPI.calculateProjectileDamage((LivingEntity) attackingArrow.getShooter(), (LivingEntity) event.getEntity(), attackingArrow);
            }
            if (CombatLog.isInCombat(player)) {
                CombatLog.updateCombat(player);
            } else {
                CombatLog.addToCombat(player);
            }
        } else if (DamageAPI.isStaffProjectile(event.getDamager())) {
            Projectile staffProjectile = (Projectile) event.getDamager();
            if (!(staffProjectile.getShooter() instanceof CraftLivingEntity)) return;
            if (((CraftLivingEntity) staffProjectile.getShooter()).hasMetadata("type")) {
                if (!(staffProjectile.getShooter() instanceof Player) && !(event.getEntity() instanceof Player)) {
                    staffProjectile.remove();
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
                finalDamage = DamageAPI.calculateProjectileDamage((LivingEntity) staffProjectile.getShooter(), (LivingEntity) event.getEntity(), staffProjectile);
            }
            if (CombatLog.isInCombat(player)) {
                CombatLog.updateCombat(player);
            } else {
                CombatLog.addToCombat(player);
            }
        }


        Entity theEntity = null;
        if (event.getDamager() instanceof LivingEntity) {

            theEntity = event.getDamager();

        } else if (DamageAPI.isBowProjectile(event.getDamager())) {
            Projectile attackingArrow = (Projectile) event.getDamager();
            theEntity = (Entity) attackingArrow.getShooter();
        } else if (DamageAPI.isStaffProjectile(event.getDamager())) {
            Projectile attackingSkull = (Projectile) event.getDamager();
            theEntity = (Entity) attackingSkull.getShooter();
        }

        if (PowerStrike.powerStrike.contains(theEntity.getUniqueId())) {
            finalDamage *= 3;
            PowerStrike.chargedMonsters.remove(theEntity.getUniqueId());
            PowerStrike.powerStrike.remove(theEntity.getUniqueId());
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 0.5F);
            player.getWorld().playEffect(player.getLocation(), Effect.EXPLOSION, 3, 3);

        }
        event.setDamage(finalDamage);
    }

    /**
     * Handling Duels. When a player punches another player.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void playerPunchPlayer(EntityDamageByEntityEvent event) {
        if (!API.isPlayer(event.getEntity()) || !API.isPlayer(event.getDamager()))
            return;
        Player p1 = (Player) event.getDamager();
        Player p2 = (Player) event.getEntity();

        if (!API.isNonPvPRegion(p1.getLocation()) && !API.isNonPvPRegion(p2.getLocation())) return;
        if (!DuelingMechanics.isDueling(p2.getUniqueId())) return;
        if (!DuelingMechanics.isDuelPartner(p1.getUniqueId(), p2.getUniqueId())) {
            p1.sendMessage("That's not your dueling partner!");
            event.setDamage(0);
            event.setCancelled(true);
            return;
        }
        DuelOffer offer = DuelingMechanics.getOffer(p1.getUniqueId());
        if (!offer.canFight) {
            event.setCancelled(true);
            event.setDamage(0);
        }
    }

    /**
     * Reduces damage after it is set previously based on the defenders armor
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onArmorReduceBaseDamage(EntityDamageByEntityEvent event) {
        EntityType damagerType = event.getDamager().getType();
        if ((!(event.getDamager() instanceof LivingEntity)) && (!DamageAPI.isBowProjectile(event.getDamager()) && (!DamageAPI.isStaffProjectile(event.getDamager()))))
            return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (Entities.PLAYER_PETS.containsValue(((CraftEntity) event.getEntity()).getHandle())) return;
        if (Entities.PLAYER_MOUNTS.containsValue(((CraftEntity) event.getEntity()).getHandle())) return;
        if (API.isNonPvPRegion(event.getDamager().getLocation()) || API.isNonPvPRegion(event.getEntity().getLocation())) {
            if (API.isPlayer(event.getEntity()) && API.isPlayer(event.getDamager())) {
                if (DuelingMechanics.isDueling(event.getEntity().getUniqueId()) && DuelingMechanics.isDueling(event.getDamager().getUniqueId())) {
                    if (!DuelingMechanics.isDuelPartner(event.getDamager().getUniqueId(), event.getEntity().getUniqueId())) {
                        event.setCancelled(true);
                        event.setDamage(0);
                        return;
                    }
                } else {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
            }
        }
        if (API.isPlayer(event.getDamager()) && API.isPlayer(event.getEntity())) {
            if (!DuelingMechanics.isDuelPartner(event.getDamager().getUniqueId(), event.getEntity().getUniqueId())) {
                if (!Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_PVP, event.getDamager().getUniqueId()).toString())) {
                    if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, event.getDamager().getUniqueId()).toString())) {
                        event.getDamager().sendMessage(org.bukkit.ChatColor.YELLOW + "You have toggle PvP disabled. You currently cannot attack players.");
                    }
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
                if (Affair.getInstance().areInSameParty((Player) event.getDamager(), (Player) event.getEntity())) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
            }
        }
        double armourReducedDamage = 0, totalArmor = 0;
        LivingEntity defender = (LivingEntity) event.getEntity();
        EntityEquipment defenderEquipment = defender.getEquipment();
        LivingEntity attacker = null;
        if (defenderEquipment.getArmorContents() == null) return;
        if (event.getDamager() instanceof LivingEntity) {
            attacker = (LivingEntity) event.getDamager();
            double[] result = DamageAPI.calculateArmorReduction(attacker, defender, event.getDamage(), null);
            armourReducedDamage = result[0];
            totalArmor = result[1];
            ItemStack attackerWeapon = attacker.getEquipment().getItemInMainHand();
            if (API.isWeapon(attackerWeapon) && new Attribute(attackerWeapon).getItemType() == Item.ItemType.POLEARM && !(DamageAPI.polearmAOEProcessing.contains(attacker))) {
                DamageAPI.polearmAOEProcessing.add(attacker);
                boolean attackerIsMob = attacker.hasMetadata("type");
                for (Entity entity : event.getEntity().getNearbyEntities(2.5, 3, 2.5)) {
                    // mobs should only be able to damage players, not other mobs
                    if (attackerIsMob && (!(API.isPlayer(entity) || API.isInSafeRegion(entity.getLocation()))))
                        continue;
                    // let's not damage ourself
                    if (entity.equals(attacker)) continue;
                    if ((event.getDamage() - armourReducedDamage) <= 0) continue;
                    if (entity instanceof LivingEntity && entity != event.getEntity() && !(entity instanceof Player)) {
                        if (entity.hasMetadata("type") && entity.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                            entity.playEffect(EntityEffect.HURT);
                            HealthHandler.getInstance().handleMonsterBeingDamaged((LivingEntity) entity, attacker, (event.getDamage() - armourReducedDamage));
                        }
                    } else if (API.isPlayer(entity)) {
                        if (attackerIsMob) {
                            HealthHandler.getInstance().handlePlayerBeingDamaged((Player) entity, attacker, (event.getDamage() - armourReducedDamage), armourReducedDamage, totalArmor);
                        } else if (!API.isNonPvPRegion(entity.getLocation())) {
                            if (!DuelingMechanics.isDuelPartner(attacker.getUniqueId(), entity.getUniqueId())) {
                                if (!Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_PVP, attacker.getUniqueId()).toString())) {
                                    if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, event.getDamager().getUniqueId()).toString())) {
                                        attacker.sendMessage(org.bukkit.ChatColor.YELLOW + "You have toggle PvP disabled. You currently cannot attack players.");
                                    }
                                    continue;
                                }
                                if (Affair.getInstance().areInSameParty((Player) attacker, (Player) entity)) {
                                    continue;
                                }
                            }
                            HealthHandler.getInstance().handlePlayerBeingDamaged((Player) entity, attacker, (event.getDamage() - armourReducedDamage), armourReducedDamage, totalArmor);
                        }
                    }
                }
                DamageAPI.polearmAOEProcessing.remove(attacker);
            }
        } else if (DamageAPI.isBowProjectile(event.getDamager())) {
            Projectile attackingArrow = (Projectile) event.getDamager();
            if (!(attackingArrow.getShooter() instanceof LivingEntity)) return;
            attacker = (LivingEntity) attackingArrow.getShooter();
            if (defender instanceof Player) {
                if (API.isNonPvPRegion(defender.getLocation()) && attacker instanceof Player) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
            }
            if (attacker instanceof Player) {
                if (API.isNonPvPRegion(attacker.getLocation()) && defender instanceof Player) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
            }
            if (attacker instanceof Player && defender instanceof Player) {
                if (!DuelingMechanics.isDuelPartner(attacker.getUniqueId(), defender.getUniqueId())) {
                    if (!Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_PVP, attacker.getUniqueId()).toString())) {
                        if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, event.getDamager().getUniqueId()).toString())) {
                            attacker.sendMessage(org.bukkit.ChatColor.YELLOW + "You have toggle PvP disabled. You currently cannot attack players.");
                        }
                        event.setCancelled(true);
                        event.setDamage(0);
                        return;
                    }
                    if (Affair.getInstance().areInSameParty((Player) attacker, (Player) defender)) {
                        event.setCancelled(true);
                        event.setDamage(0);
                        return;
                    }
                }
            }
            if (!(attacker instanceof Player)) {
                if (!(defender instanceof Player)) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
            }
            double[] result = DamageAPI.calculateArmorReduction(attacker, defender, event.getDamage(), attackingArrow);
            armourReducedDamage = result[0];
            totalArmor = result[1];
        } else if (DamageAPI.isStaffProjectile(event.getDamager())) {
            Projectile staffProjectile = (Projectile) event.getDamager();
            if (!(staffProjectile.getShooter() instanceof LivingEntity)) return;
            attacker = (LivingEntity) staffProjectile.getShooter();
            if (defender instanceof Player) {
                if (API.isNonPvPRegion(defender.getLocation()) && attacker instanceof Player) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
            }
            if (attacker instanceof Player) {
                if (API.isNonPvPRegion(attacker.getLocation()) && defender instanceof Player) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
            }
            if (attacker instanceof Player && defender instanceof Player) {
                if (!DuelingMechanics.isDuelPartner(attacker.getUniqueId(), defender.getUniqueId())) {
                    if (!Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_PVP, attacker.getUniqueId()).toString())) {
                        if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, event.getDamager().getUniqueId()).toString())) {
                            attacker.sendMessage(org.bukkit.ChatColor.YELLOW + "You have toggle PvP disabled. You currently cannot attack players.");
                        }
                        event.setCancelled(true);
                        event.setDamage(0);
                        return;
                    }
                    if (Affair.getInstance().areInSameParty((Player) attacker, (Player) defender)) {
                        event.setCancelled(true);
                        event.setDamage(0);
                        return;
                    }
                }
            }
            if (!(attacker instanceof Player)) {
                if (!(defender instanceof Player)) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
            }
            // staff hit particle
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.MAGIC_CRIT, defender.getLocation(), new Random().nextFloat(), new Random().nextFloat(),
                        new Random().nextFloat(), 0.5F, 20);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            double[] result = DamageAPI.calculateArmorReduction(attacker, defender, event.getDamage(), staffProjectile);
            armourReducedDamage = result[0];
            totalArmor = result[1];
        }
        if (event.getDamage() - armourReducedDamage <= 0) {
            event.setCancelled(true);
            event.setDamage(0);
            return;
        }
        String defenderName;
        if (defender instanceof Player) {
            defenderName = defender.getName();
        } else if (defender.hasMetadata("customname")) {
            defenderName = defender.getMetadata("customname").get(0).asString().trim();
        } else {
            defenderName = "Enemy";
        }
        String attackerName;
        if (attacker instanceof Player) {
            attackerName = attacker.getName();
        } else if (attacker.hasMetadata("customname")) {
            attackerName = attacker.getMetadata("customname").get(0).asString().trim();
        } else {
            attackerName = "Enemy";
        }
        if (armourReducedDamage == -1) {
            if (attacker instanceof Player) {
                attacker.sendMessage(org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + "                   *OPPONENT DODGED* (" + defenderName + org.bukkit.ChatColor.RED + ")");
            }
            if (defender instanceof Player) {
                defender.sendMessage(org.bukkit.ChatColor.GREEN + "" + org.bukkit.ChatColor.BOLD + "                        *DODGE* (" + org.bukkit.ChatColor.RED + attackerName + org.bukkit.ChatColor.GREEN + ")");
            }
            //The defender dodged the attack
            defender.getWorld().playSound(defender.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.5F, 2.0F);
            event.setDamage(0);
        } else if (armourReducedDamage == -2) {
            if (attacker instanceof Player) {
                attacker.sendMessage(org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + "                   *OPPONENT BLOCKED* (" + defenderName + org.bukkit.ChatColor.RED + ")");
            }
            if (defender instanceof Player) {
                defender.sendMessage(org.bukkit.ChatColor.DARK_GREEN + "" + org.bukkit.ChatColor.BOLD + "                        *BLOCK* (" + org.bukkit.ChatColor.RED + attackerName + org.bukkit.ChatColor.DARK_GREEN + ")");
            }
            defender.getWorld().playSound(defender.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2F, 1.0F);
            event.setDamage(0);
        } else if (armourReducedDamage == -3) {
            //todo: debug reflect
//            if (API.isPlayer(attacker)) {
//                attacker.sendMessage(org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + "                   *OPPONENT REFLECT* ("
//                        + defenderName + org.bukkit.ChatColor.RED + ")");
//                HealthHandler.getInstance().handlePlayerBeingDamaged((Player) attacker, defender, event.getDamage(), 0, 0);
//            } else {
//                if (attacker instanceof Projectile) {
//                    ProjectileSource shooter = ((Projectile) attacker).getShooter();
//                    if (!(shooter instanceof LivingEntity)) return;
//                    LivingEntity leShooter = (LivingEntity) shooter;
//                    if (API.isPlayer(leShooter)) {
//                        HealthHandler.getInstance().handlePlayerBeingDamaged((Player)leShooter, defender, event.getDamage(), 0, 0);
//                    }
//                    else {
//                        HealthHandler.getInstance().handleMonsterBeingDamaged(leShooter, defender, event.getDamage());
//                    }
//                }
//                else {
//                    HealthHandler.getInstance().handleMonsterBeingDamaged(attacker, defender, event.getDamage());
//                }
//            }
//            if (API.isPlayer(defender)) {
//                defender.sendMessage(org.bukkit.ChatColor.GOLD + "" + org.bukkit.ChatColor.BOLD + "              " +
//                        "          *REFLECT* (" + attackerName + org.bukkit.ChatColor.GOLD + ")");
//            }
            defender.getWorld().playSound(defender.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
        } else {
            if (API.isPlayer(defender)) {
                if (((Player) defender).isBlocking() && ((Player) defender).getEquipment().getItemInMainHand() != null && ((Player) defender).getEquipment().getItemInMainHand().getType() != Material.AIR) {
                    if (new Random().nextInt(100) <= 80) {
                        double blockDamage = event.getDamage() / 2;
                        HealthHandler.getInstance().handlePlayerBeingDamaged((Player) event.getEntity(), event.getDamager(), (blockDamage - armourReducedDamage), armourReducedDamage, totalArmor);
                        event.setDamage(0);
                    } else {
                        HealthHandler.getInstance().handlePlayerBeingDamaged((Player) event.getEntity(), event.getDamager(), (event.getDamage() - armourReducedDamage), armourReducedDamage, totalArmor);
                        event.setDamage(0);
                    }
                } else {
                    HealthHandler.getInstance().handlePlayerBeingDamaged((Player) event.getEntity(), event.getDamager(), (event.getDamage() - armourReducedDamage), armourReducedDamage, totalArmor);
                    event.setDamage(0);
                }
            } else if (defender.hasMetadata("type") && defender.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                HealthHandler.getInstance().handleMonsterBeingDamaged((LivingEntity) event.getEntity(), attacker, (event.getDamage() - armourReducedDamage));
                event.setDamage(0);
            } else {
                event.setDamage(event.getDamage() - armourReducedDamage);
            }
        }
    }

    /**
     * Listen for Pets Damage.
     * <p>
     * E.g. I can't attack Xwaffle's Wolf it's a pet!
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void petDamageListener(EntityDamageByEntityEvent event) {
        if (!(event.getEntity().hasMetadata("type"))) return;
        if (event.getEntity() instanceof Player) return;
        String metaValue = event.getEntity().getMetadata("type").get(0).asString().toLowerCase();
        switch (metaValue) {
            case "pet":
                event.setCancelled(true);
                event.setDamage(0);
                break;
            case "mount":
                event.setCancelled(true);
                event.setDamage(0);
                Player p = (Player) event.getDamager();
                Horse horse = (Horse) event.getEntity();
                if (!horse.getVariant().equals(Variant.MULE)) return;
                if (horse.getOwner().getUniqueId().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
                    EntityAPI.removePlayerMountList(p.getUniqueId());
                    horse.remove();
                }
                break;
            default:
                break;
        }
    }


    /**
     * Listen for players being damaged by non Entities.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onEntityDamaged(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata("type")) {
            String metaValue = event.getEntity().getMetadata("type").get(0).asString().toLowerCase();
            switch (metaValue) {
                case "pet":
                    event.setCancelled(true);
                    event.setDamage(0);
                    event.getEntity().setFireTicks(0);
                    break;
                case "mount":
                    event.setCancelled(true);
                    event.setDamage(0);
                    event.getEntity().setFireTicks(0);
                    break;
                case "spawner":
                    event.setCancelled(true);
                    break;
                default:
                    break;
            }
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            event.setCancelled(true);
            event.setDamage(0);
            event.getEntity().setFireTicks(0);
        }
        if (event.getCause() == DamageCause.LAVA) {
            if (event.getEntity() instanceof Player) {
                event.setDamage(0);
                event.setCancelled(true);
                event.getEntity().setFireTicks(0);
            }
        }
        if (event.getCause() == DamageCause.FIRE) {
            event.setDamage(0);
            event.setCancelled(true);
        }
        if (event.getEntity() instanceof Player && event.getCause() == DamageCause.VOID) {
            event.setCancelled(true);
            event.getEntity().teleport(event.getEntity().getWorld().getSpawnLocation());
        }

        if (!(event.getEntity() instanceof Player) && event.getCause() == DamageCause.FALL) {
            event.setDamage(0);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handlePlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
        Player p = event.getEntity();
        final Location deathLocation = p.getEyeLocation();
        p.setExp(0F);
        p.setLevel(0);
        for (ItemStack itemStack : new ArrayList<>(event.getDrops())) {
            if (API.isItemSoulbound(itemStack) || !API.isItemDroppable(itemStack) || API.isItemUntradeable(itemStack)) {
                event.getDrops().remove(itemStack);
            }
        }
        List<ItemStack> gearToSave = new ArrayList<>();
        KarmaHandler.EnumPlayerAlignments alignment = KarmaHandler.EnumPlayerAlignments.getByName(KarmaHandler.getInstance().getPlayerRawAlignment(p));

        if (alignment == null) return;

        boolean neutral_boots = false, neutral_legs = false, neutral_chest = false, neutral_helmet = false, neutral_weapon = false;
        if (alignment == KarmaHandler.EnumPlayerAlignments.NEUTRAL) {
            // 50% of weapon dropping, 25% for every piece of equipped armor.
            if (new Random().nextInt(100) <= 50) {
                neutral_weapon = true;
            }

            if (new Random().nextInt(100) <= 25) {
                int index = new Random().nextInt(4);
                if (index == 0) {
                    neutral_boots = true;
                } else if (index == 1) {
                    neutral_legs = true;
                } else if (index == 2) {
                    neutral_chest = true;
                } else if (index == 3) {
                    neutral_helmet = true;
                }
            }
        }
        if (alignment != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
            double durability_to_take = (1500 * 0.30D); // 30%
            double w_durability_to_take = (1500 * 0.30D);

            if (!neutral_boots && p.getInventory().getBoots() != null && p.getInventory().getBoots().getType() != Material.AIR) {
                ItemStack boots = p.getInventory().getBoots();
                event.getDrops().remove(boots);
                p.getInventory().setBoots(new ItemStack(Material.AIR));
                if ((RepairAPI.getCustomDurability(boots) - durability_to_take) > 0.1D) {
                    RepairAPI.subtractCustomDurability(p, boots, durability_to_take);
                    gearToSave.add(boots);
                }
            }
            if (!neutral_legs && p.getInventory().getLeggings() != null && p.getInventory().getLeggings().getType() != Material.AIR) {
                ItemStack leggings = p.getInventory().getLeggings();
                event.getDrops().remove(leggings);
                p.getInventory().setLeggings(new ItemStack(Material.AIR));
                if ((RepairAPI.getCustomDurability(leggings) - durability_to_take) > 0.1D) {
                    RepairAPI.subtractCustomDurability(p, leggings, durability_to_take);
                    gearToSave.add(leggings);
                }
            }
            if (!neutral_chest && p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getType() != Material.AIR) {
                ItemStack chestplate = p.getInventory().getChestplate();
                event.getDrops().remove(chestplate);
                p.getInventory().setChestplate(new ItemStack(Material.AIR));
                if ((RepairAPI.getCustomDurability(chestplate) - durability_to_take) > 0.1D) {
                    RepairAPI.subtractCustomDurability(p, chestplate, durability_to_take);
                    gearToSave.add(chestplate);
                }
            }
            if (!neutral_helmet && p.getInventory().getHelmet() != null && p.getInventory().getHelmet().getType() != Material.AIR) {
                ItemStack helmet = p.getInventory().getHelmet();
                event.getDrops().remove(helmet);
                p.getInventory().setHelmet(new ItemStack(Material.AIR));
                if ((RepairAPI.getCustomDurability(helmet) - durability_to_take) > 0.1D) {
                    RepairAPI.subtractCustomDurability(p, helmet, durability_to_take);
                    gearToSave.add(helmet);
                }
            }

            List<ItemStack> drop_copy = new ArrayList<>(event.getDrops());

            for (ItemStack is : drop_copy) {
                if (Mining.isDRPickaxe(is) || Fishing.isDRFishingPole(is)) {
                    event.getDrops().remove(is);
                    if ((RepairAPI.getCustomDurability(is) - w_durability_to_take) > 0.1D) {
                        RepairAPI.subtractCustomDurability(p, is, w_durability_to_take);
                        gearToSave.add(is);
                    }
                }
            }

            ItemStack weapon_slot = p.getInventory().getItem(0);
            if (!neutral_weapon && API.isWeapon(weapon_slot)) {
                event.getDrops().remove(weapon_slot);
                if ((RepairAPI.getCustomDurability(weapon_slot) - w_durability_to_take) > 0.1D) {
                    RepairAPI.subtractCustomDurability(p, weapon_slot, w_durability_to_take);
                    gearToSave.add(weapon_slot);
                }
            }
        }

        if (MountUtils.inventories.containsKey(p.getUniqueId())) {
            boolean hasMuleOut = false;
            if (EntityAPI.hasMountOut(p.getUniqueId())) {
                net.minecraft.server.v1_9_R2.Entity entity = EntityAPI.getPlayerMount(p.getUniqueId());
                if (entity.getBukkitEntity() instanceof Horse) {
                    Horse horse = (Horse) entity.getBukkitEntity();
                    if (horse.getVariant() == Variant.MULE) {
                        hasMuleOut = true;
                    }
                }
            }

            if (hasMuleOut) {
                for (ItemStack stack : MountUtils.inventories.get(p.getUniqueId()).getContents()) {
                    event.getDrops().add(stack);
                }

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    if (Bukkit.getPlayer(p.getUniqueId()) != null) {
                        MountUtils.inventories.remove(p.getUniqueId());
                    }
                });
            }
        }

        for (ItemStack stack : event.getDrops()) {
            if (API.isItemPermanentlyUntradeable(stack)) {
                gearToSave.add(stack);
            }
        }

        for (ItemStack stack : gearToSave) {
            if (event.getDrops().contains(stack)) {
                event.getDrops().remove(stack);
            }
        }

        for (ItemStack stack : new ArrayList<>(event.getDrops())) {
            p.getWorld().dropItemNaturally(deathLocation, stack);
            event.getDrops().remove(stack);
        }
        event.getDrops().clear();

        Location respawnLocation = Teleportation.Cyrennica;
        if (alignment == KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
            respawnLocation = KarmaHandler.CHAOTIC_RESPAWNS.get(new Random().nextInt(KarmaHandler.CHAOTIC_RESPAWNS.size()));
        }
        for (PotionEffect potionEffect : p.getActivePotionEffects()) {
            p.removePotionEffect(potionEffect.getType());
        }
        p.setCanPickupItems(false);
        p.setHealth(20);
        p.setCanPickupItems(false);
        p.setGameMode(GameMode.SPECTATOR);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1));
        p.teleport(respawnLocation);
        p.setFireTicks(0);
        p.setFallDistance(0);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            p.setCanPickupItems(true);
            p.setGameMode(GameMode.SURVIVAL);
            GamePlayer gamePlayer = API.getGamePlayer(p);
            if (gamePlayer != null) {
                gamePlayer.getAttributeBonusesFromStats().entrySet().stream().forEach(entry -> entry.setValue(0f));
                gamePlayer.getAttributes().entrySet().stream().forEach(entry -> entry.setValue(new Integer[]{0, 0}));
            }

            PlayerManager.checkInventory(p.getUniqueId());

            for (ItemStack stack : gearToSave) {
                p.getInventory().addItem(stack);
            }

            ItemManager.giveStarter(p);
        }, 20L);
    }
<<<<<<< Temporary merge branch 1
    
=======

    /**
     * Listen for Players dying
     * NOT TO BE USED FOR NON-PLAYERS
     * Drops items, not their head/hearthstone
     * Saves their first slot
     * Respawns them at Cyrennica
     *
     * @param event
     * @since 1.0
     */
    /*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
        Player player = event.getEntity();
        Location deathLocation = event.getEntity().getLocation();
        ItemStack armorToSave[] = new ItemStack[5];
//        ArrayList<ItemStack> savedItems = new ArrayList<ItemStack>();
        Location respawnLocation = Teleportation.Cyrennica;
        boolean savedArmorContents = false;
        if (EntityAPI.hasPetOut(player.getUniqueId())) {
            net.minecraft.server.v1_9_R2.Entity pet = EntityAPI.getPlayerPet(player.getUniqueId());
            if (!pet.getBukkitEntity().isDead()) { //Safety check
                pet.getBukkitEntity().remove();
            }
            EntityAPI.removePlayerPetList(player.getUniqueId());
            player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "For its own safety, your pet has returned to its home.");
        }
        if (EntityAPI.hasMountOut(player.getUniqueId())) {
            net.minecraft.server.v1_9_R2.Entity mount = EntityAPI.getPlayerMount(player.getUniqueId());
            if (mount.isAlive()) {
                mount.getBukkitEntity().remove();
            }
            EntityAPI.getPlayerMount(player.getUniqueId());
            player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "For its own safety, your mount has returned to the stable.");
        }

//        for(ItemStack stack : event.getEntity().getInventory()){
//        	if(stack == null || stack.getType() == Material.AIR) continue;
//        	if(Mining.isDRPickaxe(stack) || Fishing.isDRFishingPole(stack)){
//        		if (RepairAPI.getCustomDurability(stack) - 400 > 0.1D) {
//                    RepairAPI.subtractCustomDurability(player, stack, 400);
//                    savedItems.add(stack);
//                }
//        	}
//        }


        if (KarmaHandler.getInstance().getPlayerRawAlignment(player).equalsIgnoreCase(KarmaHandler.EnumPlayerAlignments.LAWFUL.name())) {
            if (player.getInventory().getItem(0) != null && player.getInventory().getItem(0).getType() != Material.AIR) {
                armorToSave[4] = player.getInventory().getItem(0);
            }
            armorToSave[0] = player.getEquipment().getBoots();
            armorToSave[1] = player.getEquipment().getLeggings();
            armorToSave[2] = player.getEquipment().getChestplate();
            armorToSave[3] = player.getEquipment().getHelmet();

            for (ItemStack itemStack : armorToSave) {
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    if (!savedArmorContents) {
                        savedArmorContents = true;
                    }
                }
            }
        } else if (KarmaHandler.getInstance().getPlayerRawAlignment(player).equalsIgnoreCase(KarmaHandler.EnumPlayerAlignments.NEUTRAL.name())) {
            if (new Random().nextInt(99) <= 75) {
                if (player.getInventory().getItem(0) != null && player.getInventory().getItem(0).getType() != Material.AIR) {
                    armorToSave[4] = player.getInventory().getItem(0);
                }
            }
            if (new Random().nextInt(99) <= 75) {
                armorToSave[0] = player.getEquipment().getBoots();
            }
            if (new Random().nextInt(99) <= 75) {
                armorToSave[1] = player.getEquipment().getLeggings();
            }
            if (new Random().nextInt(99) <= 75) {
                armorToSave[2] = player.getEquipment().getChestplate();
            }
            if (new Random().nextInt(99) <= 75) {
                armorToSave[3] = player.getEquipment().getHelmet();
            }
            for (ItemStack itemStack : armorToSave) {
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    if (!savedArmorContents) {
                        savedArmorContents = true;
                    }
                }
            }
        } else if (KarmaHandler.getInstance().getPlayerRawAlignment(player).equalsIgnoreCase(KarmaHandler.EnumPlayerAlignments.CHAOTIC.name())) {
            respawnLocation = KarmaHandler.CHAOTIC_RESPAWNS.get(new Random().nextInt(KarmaHandler.CHAOTIC_RESPAWNS.size() - 1));
        }
        event.setDroppedExp(0);
        ArrayList<ItemStack> items = new ArrayList<>();
        if (!event.getDrops().isEmpty()) {
            for (ItemStack itemStack : event.getDrops()) {
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    if (itemStack.equals(armorToSave[0]) || itemStack.equals(armorToSave[1]) || itemStack.equals(armorToSave[2]) || itemStack.equals(armorToSave[3]) || itemStack.equals(armorToSave[4])) {
                        //event.getDrops().remove(itemStack);
                        continue;
                    }
                    net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(itemStack);
                    if (nms != null) {
                        if (nms.getTag() != null) {
                            if ((nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("important")) || nms.hasTag() && nms.getTag().hasKey("subtype")) {
                                //event.getDrops().remove(itemStack);
                                continue;
                            }
                        }
                    }
                    if (API.isItemUntradeable(itemStack) || API.isItemPermanentlyUntradeable(itemStack) || API.isItemSoulbound(itemStack))
                        continue;
//                    if (Mining.isDRPickaxe(itemStack) || Fishing.isDRFishingPole(itemStack)) {
//                        //event.getDrops().remove(itemStack);
//                        continue;
//                    }
                    items.add(itemStack);
                }
            }
        }
        event.getDrops().clear();
        for (ItemStack itemStack : items) {
            event.getEntity().getWorld().dropItemNaturally(deathLocation, itemStack);
        }
        items.clear();
        player.teleport(respawnLocation);
        player.setHealth(20);
        player.teleport(respawnLocation);
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 10));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 10));
        player.teleport(respawnLocation);
        player.setFireTicks(0);
        player.setMaximumNoDamageTicks(50);
        player.setNoDamageTicks(50);
        player.setFallDistance(0);
        final boolean finalSavedArmorContents = savedArmorContents;
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            HealthHandler.getInstance().setPlayerMaxHPLive(player, 50);
            HealthHandler.getInstance().setPlayerHPLive(player, 50);
            PlayerManager.checkInventory(player.getUniqueId());
            GamePlayer gp = API.getGamePlayer(player);
            if (gp != null) {
                gp.getAttributeBonusesFromStats().entrySet().stream().forEach(entry -> entry.setValue(0f));
                gp.getAttributes().entrySet().stream().forEach(entry -> entry.setValue(new Integer[]{0, 0}));
            }
            if (finalSavedArmorContents) {

//            	for(ItemStack itemStack : savedItems){
//            		 if (itemStack != null && itemStack.getType() != Material.AIR) {
//                         if (RepairAPI.getCustomDurability(itemStack) - 400 > 0.1D) {
//                             RepairAPI.subtractCustomDurability(player, itemStack, 400);
//                         }
//                         player.getInventory().addItem(itemStack);
//                     }	
//            	}

                for (ItemStack itemStack : armorToSave) {
                    if (itemStack != null && itemStack.getType() != Material.AIR) {
                        if (RepairAPI.getCustomDurability(itemStack) - 400 > 1D) {
                            RepairAPI.subtractCustomDurability(player, itemStack, 400);
                            player.getInventory().addItem(itemStack);
                        } else {
                        }
                    }
                }
            }
            ItemManager.giveStarter(player);
        }, 20L);
    }*/
>>>>>>> Temporary merge branch 2
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        PlayerManager.checkInventory(event.getPlayer().getUniqueId());
    }

    /**
     * Listen for blazes, ghasts, and witches firing their projectile so we
     * can correctly add the metadata to the projectile.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCustomProjectileEntityLaunchProjectile(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof LivingEntity)) return;
        EntityType entityType = ((LivingEntity) event.getEntity().getShooter()).getType();
        if (entityType != EntityType.BLAZE && entityType != EntityType.GHAST && entityType != EntityType.WITCH)
            return;

        LivingEntity leShooter = (LivingEntity) event.getEntity().getShooter();
        if (!(leShooter.hasMetadata("type"))) return;
        if (!(API.isWeapon(leShooter.getEquipment().getItemInMainHand())) && entityType != EntityType.WITCH) return;

        if (entityType != EntityType.WITCH)
            MetadataUtils.registerProjectileMetadata(((DRMonster) ((CraftLivingEntity) leShooter).getHandle()).getAttributes
                    (), CraftItemStack.asNMSCopy(leShooter.getEquipment().getItemInMainHand()).getTag(), event.getEntity());
        else {
            DRWitch witch = (DRWitch) ((CraftLivingEntity) leShooter).getHandle();
            MetadataUtils.registerProjectileMetadata(witch.getAttributes(), CraftItemStack.asNMSCopy(witch.getWeapon
                    ()).getTag(), event.getEntity());
        }
    }

    @EventHandler
    public void onWitchSplashPotion(PotionSplashEvent event) {
        if (!(event.getPotion().getShooter() instanceof LivingEntity)) return;
        LivingEntity leShooter = (LivingEntity) event.getPotion().getShooter();
        if (!(leShooter.getType() == EntityType.WITCH)) return;

        for (LivingEntity le : event.getAffectedEntities()) {
            if (API.isInSafeRegion(le.getLocation())) continue;
            if (!API.isPlayer(le)) continue;
            double damage = DamageAPI.calculateProjectileDamage(leShooter, le, event.getPotion());
            double[] armorResult = DamageAPI.calculateArmorReduction(leShooter, le, damage, event.getPotion());

            HealthHandler.getInstance().handlePlayerBeingDamaged((Player) le, leShooter, damage - armorResult[0], armorResult[0], armorResult[1]);
        }
    }

    /**
     * Listen for Players using their "staff" item
     * Checks to see if they can, and
     * then fires the staff projectile
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerUseStaff(PlayerInteractEvent event) {
        if (!event.hasItem()) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = (CraftItemStack.asNMSCopy(event.getPlayer().getEquipment().getItemInMainHand()));
        if (nmsItem == null || nmsItem.getTag() == null || !nmsItem.getTag().hasKey("itemType")) return;

        Item.ItemType itemType = new Attribute(event.getPlayer().getEquipment().getItemInMainHand()).getItemType();
        if (itemType != Item.ItemType.STAFF) {
            return;
        }
        Player player = event.getPlayer();
        if (player.isInsideVehicle()) {
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }
        if (player.hasMetadata("last_Staff_Use")) {
            event.setCancelled(true);
            if (System.currentTimeMillis() - player.getMetadata("last_Staff_Use").get(0).asLong() < 100) {
                event.setUseItemInHand(Event.Result.DENY);
                return;
            }
        }
        if (API.isInSafeRegion(player.getLocation())) {
            if (!DuelingMechanics.isDueling(player.getUniqueId())) {
                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1F, 1.25F);
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.MAGIC_CRIT, player.getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 20);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                //prevent spam of particles
                player.setMetadata("last_Staff_Use", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                return;
            }
        }
        if (!RestrictionListener.canPlayerUseTier(player, RepairAPI.getArmorOrWeaponTier(player.getEquipment().getItemInMainHand()))) {
            player.sendMessage(org.bukkit.ChatColor.RED + "You must to be " + org.bukkit.ChatColor.UNDERLINE + "at least" + org.bukkit.ChatColor.RED + " level "
                    + RestrictionListener.getLevelToUseTier(RepairAPI.getArmorOrWeaponTier(player.getEquipment().getItemInMainHand())) + " to use this weapon.");
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), 1F);
            return;
        }

        if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING) || EnergyHandler.getPlayerCurrentEnergy(player) <= 0) {
            event.setCancelled(true);
            event.getPlayer().playSound(player.getLocation(), Sound.ENTITY_WOLF_PANT, 12F, 1.5F);
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, player.getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 40);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }
        event.setCancelled(true);
        player.setMetadata("last_Staff_Use", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        DamageAPI.fireStaffProjectile(player, player.getEquipment().getItemInMainHand(), nmsItem.getTag());
        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_LAUNCH, 1f, 1f);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onStaffProjectileExplode(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof WitherSkull) && !(event.getEntity() instanceof Fireball) && !(event
                .getEntity() instanceof LargeFireball) && !(event.getEntity() instanceof SmallFireball)) {
            return;
        }
        event.setCancelled(false);
        event.setRadius(0);
        event.setFire(false);
    }

    /**
     * Fired when monster is killed. Checks if the monster is elite.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onEliteDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (!(event.getEntity() instanceof CraftLivingEntity)) return;
        event.getDrops().clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerDMGOnHorse(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getEntity().getVehicle() == null) return;
        if (event.getDamage() <= 0) return;
        if (event.isCancelled()) return;
        if (EntityAPI.hasMountOut(event.getEntity().getUniqueId())) {
            event.getEntity().getVehicle().setPassenger(null);
            event.getEntity().getVehicle().remove();
            EntityAPI.removePlayerMountList(event.getEntity().getUniqueId());
            event.getEntity().sendMessage(ChatColor.RED + "You have been dismounted as you have taken damage!");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleCombatLoggerNPCDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (!(event.getEntity() instanceof CraftLivingEntity)) return;
        if (!event.getEntity().hasMetadata("uuid")) return;
        UUID uuid = UUID.fromString(event.getEntity().getMetadata("uuid").get(0).asString());
        if (CombatLog.getInstance().getCOMBAT_LOGGERS().containsKey(uuid)) {
            CombatLogger combatLogger = CombatLog.getInstance().getCOMBAT_LOGGERS().get(uuid);
            final Location location = event.getEntity().getLocation();
            if (!combatLogger.getItemsToDrop().isEmpty()) {
                for (ItemStack itemStack : combatLogger.getItemsToDrop()) {
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        continue;
                    }
                    net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
                    if ((nmsStack.hasTag() && nmsStack.getTag().hasKey("type") && nmsStack.getTag().getString("type").equalsIgnoreCase("important")) || (nmsStack.hasTag() && nmsStack.getTag().hasKey("subtype"))) {
                        continue;
                    }
                    location.getWorld().dropItemNaturally(location, itemStack);
                }
            }
            if (!combatLogger.getArmorToDrop().isEmpty()) {
                for (ItemStack itemStack : combatLogger.getArmorToDrop()) {
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        continue;
                    }
                    net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
                    if ((nmsStack.hasTag() && nmsStack.getTag().hasKey("type") && nmsStack.getTag().getString("type").equalsIgnoreCase("important")) || (nmsStack.hasTag() && nmsStack.getTag().hasKey("subtype"))) {
                        continue;
                    }
                    location.getWorld().dropItemNaturally(location, itemStack);
                }
            }
            ArrayList<String> armorContents = new ArrayList<>();
            String itemsToSave;
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, "", false);
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, new ArrayList<String>(), false);
            if (!combatLogger.getArmorToSave().isEmpty()) {
                for (ItemStack itemStack : combatLogger.getArmorToSave()) {
                    if (itemStack.getType() == null || itemStack.getType() == Material.AIR || itemStack.getType() == Material.MELON) {
                        armorContents.add("null");
                    } else {
                        armorContents.add(ItemSerialization.itemStackToBase64(itemStack));
                    }
                }
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, armorContents, false);
            } else {
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, new ArrayList<String>(), false);
            }
            if (!combatLogger.getItemsToSave().isEmpty()) {
                Inventory inventory = Bukkit.createInventory(null, 27, "LoggerInventory");
                for (ItemStack stack : combatLogger.getItemsToSave()) {
                    inventory.addItem(stack);
                }
                itemsToSave = ItemSerialization.toString(inventory);
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, itemsToSave, false);
            } else {
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, "", false);
            }
            combatLogger.handleNPCDeath();

        /*final Zombie loggerNPC = CombatLog.LOGGER.get(uuid);
        final Location location = event.getEntity().getLocation();
        Inventory inv = CombatLog.LOGGER_INVENTORY.get(uuid);
        if (inv == null) {
        	return;
        }
        Location loc =  KarmaHandler.CHAOTIC_RESPAWNS.get(new Random().nextInt(KarmaHandler.CHAOTIC_RESPAWNS.size() - 1));
        for (ItemStack itemStack : inv.getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            if ((nmsStack.hasTag() && nmsStack.getTag().hasKey("type") && nmsStack.getTag().getString("type").equalsIgnoreCase("important")) || (nmsStack.hasTag() && nmsStack.getTag().hasKey("subtype"))) {
                continue;
            }
            location.getWorld().dropItemNaturally(location, itemStack);
        }
        for (ItemStack itemStack : loggerNPC.getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            if ((nmsStack.hasTag() && nmsStack.getTag().hasKey("type") && nmsStack.getTag().getString("type").equalsIgnoreCase("important")) || (nmsStack.hasTag() && nmsStack.getTag().hasKey("subtype"))) {
                continue;
            }
            location.getWorld().dropItemNaturally(location, itemStack);
        }
        CombatLog.checkCombatLog(uuid);
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, "", false);
  		DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, new ArrayList<String>(), false);
  		if (loc != null) {
  			String locString = loc.getBlockX() +"," + loc.getBlockY() + 5 + "," + loc.getBlockZ() + "," + "0,0";
  			DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.CURRENT_LOCATION, locString, true);
  		}
  		DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.LOGGERDIED, true, true);
        CombatLog.LOGGER_INVENTORY.remove(uuid);
        */
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityHurtByNonCombat(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) && !(event.getEntity().hasMetadata("type") && event.getEntity().getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")))
            return;
        if (event.getDamage() <= 0) return;
        if (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.PROJECTILE || event.getCause() == DamageCause.CUSTOM)
            return;

        double dmg = event.getDamage();
        event.setDamage(0);
        event.setCancelled(true);

        int maxHP = 0;
        if (API.isPlayer(event.getEntity())) {
            maxHP = API.getGamePlayer((Player) event.getEntity()).getPlayerMaxHP();
        } else {
            maxHP = HealthHandler.getInstance().getMonsterHPLive((LivingEntity) event.getEntity());
        }
        if (API.isInSafeRegion(event.getEntity().getLocation())) {
            event.setDamage(0);
            event.setCancelled(true);
            return;
        }
        if (event.getEntity().hasMetadata("last_environment_damage")) {
            if (System.currentTimeMillis() - event.getEntity().getMetadata("last_environment_damage").get(0).asLong() <= 800) {
                event.setCancelled(true);
                event.setDamage(0);
                return;
            }
        }
        event.getEntity().setMetadata("last_environment_damage", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));

        switch (event.getCause()) {
            case FALL:
                double blocks = dmg;
                if (blocks >= 2) {
                    dmg = maxHP * 0.02D * event.getDamage();
                }
                if (API.isPlayer(event.getEntity())) {
                    Player p = (Player) event.getEntity();
                    GamePlayer gp = API.getGamePlayer(p);
                    if (dmg >= gp.getPlayerCurrentHP()) {
                        dmg = gp.getPlayerCurrentHP() - 1;
                    }
                    if (blocks >= 49 && dmg <= gp.getPlayerCurrentHP()) {
                        Achievements.getInstance().giveAchievement(p.getUniqueId(), Achievements.EnumAchievements.LEAP_OF_FAITH);

                    }
                }
                break;
            case DROWNING:
                dmg = maxHP * 0.04;
                break;
            case FIRE_TICK:
                if (!(((LivingEntity) event.getEntity()).hasPotionEffect(PotionEffectType.FIRE_RESISTANCE))) {
                    dmg = maxHP * 0.01;
                } else {
                    dmg = 0;
                }
                break;
            case LAVA:
            case FIRE:
                if (!(((LivingEntity) event.getEntity()).hasPotionEffect(PotionEffectType.FIRE_RESISTANCE))) {
                    dmg = maxHP * 0.03;
                } else {
                    dmg = 0;
                }
                break;
            case POISON:
                dmg = maxHP * 0.01;
                break;
            case CONTACT:
                dmg = maxHP * 0.03;
                break;
            case SUFFOCATION:
                dmg = 0;
                break;
            case VOID:
                dmg = 0;
                break;
            default:
                dmg = 0;
                break;
        }
        if (dmg > 0) {
            if (event.getEntity() instanceof Player) {
                HealthHandler.getInstance().handlePlayerBeingDamaged((Player) event.getEntity(), null, dmg, 0, 0);
            } else if (event.getEntity().hasMetadata("type") && event.getEntity().getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                HealthHandler.getInstance().handleMonsterBeingDamaged((LivingEntity) event.getEntity(), null, dmg);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityCombust(EntityCombustEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerUseBow(PlayerInteractEvent event) {
        if (!event.hasItem()) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = (CraftItemStack.asNMSCopy(event.getPlayer().getEquipment().getItemInMainHand()));
        if (nmsItem == null || nmsItem.getTag() == null || !nmsItem.getTag().hasKey("itemType")) return;

        Player player = event.getPlayer();
        Item.ItemType itemType = new Attribute(player.getEquipment().getItemInMainHand()).getItemType();
        if (itemType != ItemType.BOW) {
            return;
        }
        if (player.isInsideVehicle()) {
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }
        ItemStack hand = player.getEquipment().getItemInMainHand();
        if (player.hasMetadata("last_Bow_Use")) {
            event.setCancelled(true);
            if (System.currentTimeMillis() - player.getMetadata("last_Bow_Use").get(0).asLong() < 650) {
                event.setUseItemInHand(Event.Result.DENY);
                return;
            }
        }

        if (API.isInSafeRegion(player.getLocation())) {
            if (!DuelingMechanics.isDueling(player.getUniqueId())) {
                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1.25F);
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, player.getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 20);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                //Prevent spam of particles.
                player.setMetadata("last_Bow_Use", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                return;
            }
        }

        if (!RestrictionListener.canPlayerUseTier(player, RepairAPI.getArmorOrWeaponTier(hand))) {
            player.sendMessage(org.bukkit.ChatColor.RED + "You must to be " + org.bukkit.ChatColor.UNDERLINE + "at least" + org.bukkit.ChatColor.RED + " level "
                    + RestrictionListener.getLevelToUseTier(RepairAPI.getArmorOrWeaponTier(hand)) + " to use this weapon.");
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), 1F);
            return;
        }

        if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING) || EnergyHandler.getPlayerCurrentEnergy(player) <= 0) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_WOLF_PANT, 12F, 1.5F);
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, player.getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 40);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }
        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);
        DataWatcher watcher = new DataWatcher(((CraftPlayer) player).getHandle());
        watcher.register(new DataWatcherObject<>(5, DataWatcherRegistry.a), (byte) 1);
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            //TODO: Not sure if we wanna send the packet.
            if (player != player1) {
                ((CraftPlayer) player1).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(((CraftPlayer) player).getHandle().getId(), watcher, true));
            }
        }
        player.setMetadata("last_Bow_Use", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        DamageAPI.fireBowProjectile(player, hand, nmsItem.getTag());
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFireballHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof LargeFireball) {
            LivingEntity shooter = (LivingEntity) event.getEntity().getShooter();
            if (shooter instanceof Ghast) {
                for (Entity ent : event.getEntity().getNearbyEntities(4, 4, 4)) {
                    if (ent instanceof Player) {
                        if (API.isInSafeRegion(ent.getLocation())) continue;
                        if (!API.isPlayer(ent)) continue;
                        double damage = DamageAPI.calculateProjectileDamage(shooter, (LivingEntity) ent, event.getEntity());
                        double[] armorResult = DamageAPI.calculateArmorReduction(shooter, (LivingEntity) ent, damage, event.getEntity());

                        HealthHandler.getInstance().handlePlayerBeingDamaged((Player) ent, shooter, damage - armorResult[0], armorResult[0], armorResult[1]);
                    }
                }

                if (new Random().nextInt(10) == 0) {
                    // 10% chance of adds on explosion.
                    Location hit_loc = event.getEntity().getLocation();
                    World world = ((CraftWorld) event.getEntity().getWorld()).getHandle();
                    for (int i = 0; i <= 3; i++) {
                        net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, 2, EnumMonster.MagmaCube);
                        int level = Utils.getRandomFromTier(2, "low");
                        String newLevelName = org.bukkit.ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
                        EntityStats.setMonsterRandomStats(entity, level, 2);
                        SpawningMechanics.rollElement(entity, EnumMonster.MagmaCube);
                        if (entity == null) {
                            return; //WTF?? UH OH BOYS WE GOT ISSUES
                        }
                        entity.setCustomName(newLevelName + API.getTierColor(2).toString() + "Lesser Spawn of Inferno");
                        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + API.getTierColor(2).toString() + "Lesser Spawn of Inferno"));
                        Location location = new Location(world.getWorld(), hit_loc.getX() + new Random().nextInt(3), hit_loc.getY(), hit_loc.getZ() + new Random().nextInt(3));
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    }
                }
            }
        } else if (event.getEntity() instanceof SmallFireball) {
            LivingEntity shooter = (LivingEntity) event.getEntity().getShooter();
            if (shooter instanceof Blaze) {
                if (new Random().nextInt(5) == 0) {
                    if (event.getEntity().hasMetadata("tier")) {
                        Location toSpawn = event.getEntity().getLocation();
                        int tier = event.getEntity().getMetadata("tier").get(0).asInt();
                        World world = ((CraftWorld) event.getEntity().getWorld()).getHandle();
                        net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, tier, EnumMonster.MagmaCube);
                        int level = Utils.getRandomFromTier(tier, "low");
                        String newLevelName = org.bukkit.ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
                        EntityStats.setMonsterRandomStats(entity, level, tier);
                        SpawningMechanics.rollElement(entity, EnumMonster.MagmaCube);
                        if (entity == null) {
                            return; //WTF?? UH OH BOYS WE GOT ISSUES
                        }
                        entity.setCustomName(newLevelName + API.getTierColor(tier).toString() + EnumMonster.MagmaCube.name);
                        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + API.getTierColor(tier).toString() + EnumMonster.MagmaCube.name));
                        Location location = new Location(world.getWorld(), toSpawn.getX(), toSpawn.getY(), toSpawn.getZ());
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    }
                }
            }
        }
    }
}