package net.dungeonrealms.listeners;

import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.duel.DuelOffer;
import net.dungeonrealms.duel.DuelingMechanics;
import net.dungeonrealms.entities.Entities;
import net.dungeonrealms.entities.types.monsters.boss.Boss;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.handlers.EnergyHandler;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.handlers.KarmaHandler;
import net.dungeonrealms.items.Attribute;
import net.dungeonrealms.items.DamageAPI;
import net.dungeonrealms.items.Item;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.Armor;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.dungeonrealms.mechanics.PlayerManager;
import net.dungeonrealms.miscellaneous.ItemBuilder;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.spawning.BuffManager;
import net.dungeonrealms.spawning.MobSpawner;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.dungeonrealms.teleportation.Teleportation;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
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
        if (!(event.getEntity().hasMetadata("type"))) return;
        event.blockList().clear();
        event.setYield(0.1F);
        event.setCancelled(true);
        if (event.getEntity().getMetadata("type").get(0).asString().equalsIgnoreCase("buff")) {
            event.setCancelled(true);
            event.blockList().clear();
            int radius = event.getEntity().getMetadata("radius").get(0).asInt();
            int duration = event.getEntity().getMetadata("duration").get(0).asInt();
            PotionEffectType effectType = PotionEffectType.getByName(event.getEntity().getMetadata("effectType").get(0).asString());
            for (Entity e : event.getEntity().getNearbyEntities(radius, radius, radius)) {
                if (!(API.isPlayer(e))) continue;
                ((Player) e).addPotionEffect(new PotionEffect(effectType, duration, 2));
                if (effectType.equals(PotionEffectType.HEAL)) {
                    HealthHandler.getInstance().healPlayerByAmount((Player) e, HealthHandler.getInstance().getPlayerMaxHPLive((Player) e) / 2);
                    ((Player) e).removePotionEffect(PotionEffectType.HEAL);
                }
                e.sendMessage(new String[]{
                        "",
                        ChatColor.BLUE + "An Invocation of " + ChatColor.YELLOW.toString() + ChatColor.UNDERLINE + effectType.getName() + ChatColor.BLUE + " has begun!",
                        ""
                });
            }
            BuffManager.getInstance().CURRENT_BUFFS.stream().filter(enderCrystal -> enderCrystal.getBukkitEntity().getLocation().equals(event.getEntity().getLocation())).forEach(BuffManager.getInstance().CURRENT_BUFFS::remove);
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
                ArrayList<MobSpawner> list = SpawningMechanics.getSpawners();
                for (MobSpawner current : list) {
                    if (current.loc.getBlockX() == event.getEntity().getLocation().getBlockX() && current.loc.getBlockY() == event.getEntity().getLocation().getBlockY() &&
                            current.loc.getBlockZ() == event.getEntity().getLocation().getBlockZ()) {
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
        if ((!(API.isPlayer(event.getDamager()))) && ((event.getDamager().getType() != EntityType.ARROW) && (event.getDamager().getType() != EntityType.SNOWBALL)))
            return;
        if (!(event.getEntity() instanceof CraftLivingEntity) && !(API.isPlayer(event.getEntity()))) return;
        if (Entities.PLAYER_PETS.containsValue(((CraftEntity) event.getEntity()).getHandle())) return;
        if (Entities.PLAYER_MOUNTS.containsValue(((CraftEntity) event.getEntity()).getHandle())) return;
        if (event.getEntity() instanceof CraftLivingEntity) {
            if (!(event.getEntity() instanceof Player)) {
                if (!event.getEntity().hasMetadata("type")) return;
            }
        }
        //Make sure the player is HOLDING something!
        double finalDamage = 0;
        if (API.isPlayer(event.getDamager())) {
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
            Player attacker = (Player) event.getDamager();
            if (attacker.getItemInHand() == null) return;
            //Check if the item has NBT, all our custom weapons will have NBT.
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(attacker.getItemInHand()));
            if (nmsItem == null || nmsItem.getTag() == null) return;
            //Get the NBT of the item the player is holding.
            NBTTagCompound tag = nmsItem.getTag();
            //Check if it's a {WEAPON} the player is hitting with. Once of our custom ones!
            if (!tag.getString("type").equalsIgnoreCase("weapon")) return;
            if (attacker.hasPotionEffect(PotionEffectType.SLOW_DIGGING) || EnergyHandler.getPlayerCurrentEnergy(attacker.getUniqueId()) <= 0) {
                event.setCancelled(true);
                event.setDamage(0);
                attacker.playSound(attacker.getLocation(), Sound.WOLF_PANT, 12F, 1.5F);
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, event.getEntity().getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 40);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return;
            }
            if (CombatLog.isInCombat(attacker)) {
                CombatLog.updateCombat(attacker);
            } else {
                CombatLog.addToCombat(attacker);
            }
            EnergyHandler.removeEnergyFromPlayerAndUpdate(attacker.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(attacker.getItemInHand()));
            finalDamage = DamageAPI.calculateWeaponDamage(attacker, event.getEntity(), tag);

            if (API.isPlayer(event.getDamager()) && API.isPlayer(event.getEntity())) {
                if (API.getGamePlayer((Player) event.getEntity()) != null && API.getGamePlayer((Player) event.getDamager()) != null) {
                    if (API.getGamePlayer((Player) event.getEntity()).getPlayerAlignment() == KarmaHandler.EnumPlayerAlignments.LAWFUL) {
                        if (API.getGamePlayer((Player) event.getDamager()).getPlayerAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, event.getDamager().getUniqueId()).toString())) {
                                if (finalDamage >= HealthHandler.getInstance().getPlayerHPLive((Player) event.getEntity())) {
                                    event.setCancelled(true);
                                    event.setDamage(0);
                                    event.getDamager().sendMessage(ChatColor.YELLOW + "Your Chaotic Prevention Toggle has activated preventing the death of " + event.getEntity().getName() + "!");
                                    event.getEntity().sendMessage(ChatColor.YELLOW + event.getDamager().getName() + " has their Chaotic Prevention Toggle ON, your life has been spared!");
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } else if (event.getDamager().getType() == EntityType.ARROW) {
            Arrow attackingArrow = (Arrow) event.getDamager();
            if (!(attackingArrow.getShooter() instanceof Player)) return;
            finalDamage = DamageAPI.calculateProjectileDamage((Player) attackingArrow.getShooter(), event.getEntity(), attackingArrow);
            if (CombatLog.isInCombat(((Player) attackingArrow.getShooter()))) {
                CombatLog.updateCombat(((Player) attackingArrow.getShooter()));
            } else {
                CombatLog.addToCombat(((Player) attackingArrow.getShooter()));
            }
        } else if (event.getDamager().getType() == EntityType.SNOWBALL) {
            Snowball staffProjectile = (Snowball) event.getDamager();
            if (!(staffProjectile.getShooter() instanceof Player)) return;
            finalDamage = DamageAPI.calculateProjectileDamage((Player) staffProjectile.getShooter(), event.getEntity(), staffProjectile);
            if (CombatLog.isInCombat(((Player) staffProjectile.getShooter()))) {
                CombatLog.updateCombat(((Player) staffProjectile.getShooter()));
            } else {
                CombatLog.addToCombat(((Player) staffProjectile.getShooter()));
            }
        }
        event.setDamage(finalDamage);
        if (event.getEntity().hasMetadata("boss")) {
            if (event.getEntity() instanceof CraftLivingEntity) {
                Boss b = (Boss) ((CraftLivingEntity) event.getEntity()).getHandle();
                b.onBossHit(event);
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
    	if(API.isPlayer(event.getDamager()))return; // THIS METHOD WAS BREAKING EVERYTHING BECAUSE IT WASN'T MAKING SURE DAMAGER WAS A PLAYER FCKING NIGGERS I SPENT SO LONG ON THIS
        if ((!(event.getDamager() instanceof CraftLivingEntity)) && ((event.getDamager().getType() != EntityType.ARROW) && (event.getDamager().getType() != EntityType.SNOWBALL)))
            return;
        if (!(API.isPlayer(event.getEntity()))) return;
        if (API.isInSafeRegion(event.getDamager().getLocation()) || API.isInSafeRegion(event.getEntity().getLocation())) {
            event.setCancelled(true);
            event.setDamage(0);
            return;
        }
        double finalDamage = 0;
        Player player = (Player) event.getEntity();
        if (event.getDamager() instanceof CraftLivingEntity) {
            CraftLivingEntity attacker = (CraftLivingEntity) event.getDamager();
            EntityEquipment attackerEquipment = attacker.getEquipment();
            if (attackerEquipment.getItemInHand() == null) return;
            attackerEquipment.getItemInHand().setDurability(((short) -1));
            //Check if the item has NBT, all our custom weapons will have NBT.
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(attackerEquipment.getItemInHand()));
            if (nmsItem == null || nmsItem.getTag() == null) {
                return;
            }
            //Get the NBT of the item the mob is holding.
            NBTTagCompound tag = nmsItem.getTag();
            //Check if it's a {WEAPON} the mob is hitting with. Once of our custom ones!
            if (!tag.getString("type").equalsIgnoreCase("weapon")) return;
            finalDamage = DamageAPI.calculateWeaponDamage(attacker, event.getEntity(), tag);
            if (CombatLog.isInCombat(player)) {
                CombatLog.updateCombat(player);
            } else {
                CombatLog.addToCombat(player);
            }
        } else if (event.getDamager().getType() == EntityType.ARROW) {
            Arrow attackingArrow = (Arrow) event.getDamager();
            if (!(attackingArrow.getShooter() instanceof CraftLivingEntity)) return;
            if (((CraftLivingEntity) attackingArrow.getShooter()).hasMetadata("type")) {
                if (!(attackingArrow.getShooter() instanceof Player) && !(event.getEntity() instanceof Player)) {
                    attackingArrow.remove();
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
                finalDamage = DamageAPI.calculateProjectileDamage((LivingEntity) attackingArrow.getShooter(), event.getEntity(), attackingArrow);
            }
            if (CombatLog.isInCombat(player)) {
                CombatLog.updateCombat(player);
            } else {
                CombatLog.addToCombat(player);
            }
        } else if (event.getDamager().getType() == EntityType.SNOWBALL) {
            Snowball staffProjectile = (Snowball) event.getDamager();
            if (!(staffProjectile.getShooter() instanceof CraftLivingEntity)) return;
            if (((CraftLivingEntity) staffProjectile.getShooter()).hasMetadata("type")) {
                if (!(staffProjectile.getShooter() instanceof Player) && !(event.getEntity() instanceof Player)) {
                    staffProjectile.remove();
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
                finalDamage = DamageAPI.calculateProjectileDamage((LivingEntity) staffProjectile.getShooter(), event.getEntity(), staffProjectile);
            }
            if (CombatLog.isInCombat(player)) {
                CombatLog.updateCombat(player);
            } else {
                CombatLog.addToCombat(player);
            }
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
        if(!DuelingMechanics.isDueling(p2.getUniqueId())) return;
        if (!DuelingMechanics.isDuelPartner(p1.getUniqueId(), p2.getUniqueId())) {
        	p1.sendMessage("That's not you're dueling partner!");
        	event.setDamage(0);
        	event.setCancelled(true);
        	return;
        }
        DuelOffer offer = DuelingMechanics.getOffer(p1.getUniqueId());
         if(!offer.canFight){
        	event.setCancelled(true);
        	event.setDamage(0);
        	return;
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
        if ((!(event.getDamager() instanceof LivingEntity)) && ((event.getDamager().getType() != EntityType.ARROW) && (event.getDamager().getType() != EntityType.SNOWBALL) && (event.getDamager().getType() != EntityType.WITHER_SKULL)))
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
        double armourReducedDamage = 0;
        LivingEntity defender = (LivingEntity) event.getEntity();
        EntityEquipment defenderEquipment = defender.getEquipment();
        LivingEntity attacker = null;
        if (defenderEquipment.getArmorContents() == null) return;
        ItemStack[] defenderArmor = defenderEquipment.getArmorContents();
        if (event.getDamager() instanceof LivingEntity) {
            attacker = (LivingEntity) event.getDamager();
            armourReducedDamage = DamageAPI.calculateArmorReduction(attacker, defender, defenderArmor);
            if (attacker.getEquipment().getItemInHand() != null && attacker.getEquipment().getItemInHand().getType() != Material.AIR) {
                net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(attacker.getEquipment().getItemInHand()));
                if (nmsItem != null && nmsItem.getTag() != null) {
                    if (new Attribute(attacker.getEquipment().getItemInHand()).getItemType() == Item.ItemType.POLE_ARM && !(DamageAPI.polearmAOEProcessing.contains(attacker))) {
                        DamageAPI.polearmAOEProcessing.add(attacker);
                        for (Entity entity : event.getEntity().getNearbyEntities(2.5, 3, 2.5)) {
                            if (entity instanceof LivingEntity && entity != event.getEntity() && !(entity instanceof Player)) {
                                if ((event.getDamage() - armourReducedDamage) > 0) {
                                    HealthHandler.getInstance().handleMonsterBeingDamaged((LivingEntity) entity, attacker, (event.getDamage() - armourReducedDamage));
                                }
                            } else {
                                continue;
                            }
                        }
                        DamageAPI.polearmAOEProcessing.remove(attacker);
                    }
                }
            }
        } else if (event.getDamager().getType() == EntityType.ARROW) {
            Arrow attackingArrow = (Arrow) event.getDamager();
            if (!(attackingArrow.getShooter() instanceof LivingEntity)) return;
            attacker = (LivingEntity) attackingArrow.getShooter();
            if (defender instanceof Player) {
                if (API.isNonPvPRegion(defender.getLocation()) && attacker instanceof Player) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
            }
            if (!(attacker instanceof Player)) {
                if (!(defender instanceof Player)) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
            }
            armourReducedDamage = DamageAPI.calculateArmorReduction(attackingArrow, defender, defenderArmor);
        } else if (event.getDamager().getType() == EntityType.SNOWBALL) {
            Snowball staffProjectile = (Snowball) event.getDamager();
            if (!(staffProjectile.getShooter() instanceof LivingEntity)) return;
            attacker = (LivingEntity) staffProjectile.getShooter();
            if (defender instanceof Player) {
                if (API.isNonPvPRegion(defender.getLocation()) && attacker instanceof Player) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
            }
            if (!(attacker instanceof Player)) {
                if (!(defender instanceof Player)) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    return;
                }
            }
            armourReducedDamage = DamageAPI.calculateArmorReduction(staffProjectile, defender, defenderArmor);
        }
        if (armourReducedDamage == -1) {
            //The defender dodged the attack
            event.setDamage(0);
            LivingEntity leDefender = (LivingEntity) event.getEntity();
            if (leDefender.hasPotionEffect(PotionEffectType.SLOW)) {
                leDefender.removePotionEffect(PotionEffectType.SLOW);
            }
            if (leDefender.hasPotionEffect(PotionEffectType.POISON)) {
                leDefender.removePotionEffect(PotionEffectType.POISON);
            }
            return;
        }
        if (event.getDamage() - armourReducedDamage <= 0 || armourReducedDamage == -2) {
            event.setDamage(0);
            LivingEntity leDefender = (LivingEntity) event.getEntity();
            if (leDefender.hasPotionEffect(PotionEffectType.SLOW)) {
                leDefender.removePotionEffect(PotionEffectType.SLOW);
            }
            if (leDefender.hasPotionEffect(PotionEffectType.POISON)) {
                leDefender.removePotionEffect(PotionEffectType.POISON);
            }
        } else {
            if (API.isPlayer(defender)) {
                if (((Player) defender).isBlocking() && ((Player) defender).getItemInHand() != null && ((Player) defender).getItemInHand().getType() != Material.AIR) {
                    if (new Random().nextInt(100) <= 80) {
                        double blockDamage = event.getDamage() / 2;
                        HealthHandler.getInstance().handlePlayerBeingDamaged((Player) event.getEntity(), event.getDamager(), (blockDamage - armourReducedDamage));
                        event.setDamage(0);
                    }
                } else {
                    HealthHandler.getInstance().handlePlayerBeingDamaged((Player) event.getEntity(), event.getDamager(), (event.getDamage() - armourReducedDamage));
                    event.setDamage(0);
                }
            } else if (defender instanceof CraftLivingEntity) {
                if (defender.hasMetadata("type") && defender.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                    HealthHandler.getInstance().handleMonsterBeingDamaged((LivingEntity) event.getEntity(), attacker, (event.getDamage() - armourReducedDamage));
                    event.setDamage(0);
                }
            } else {
                event.setDamage(event.getDamage() - armourReducedDamage);
            }
        }
    }

    /**
     * Listen for Players [NOT DISPENSERS/MOBS] firing projectiles
     * Used to apply metadata from the nbt data of the bow in the entities hand
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onLivingEntityFireProjectile(ProjectileLaunchEvent event) {
        if ((!(event.getEntity().getShooter() instanceof Player)) && ((event.getEntityType() != EntityType.ARROW) && (event.getEntityType() != EntityType.SNOWBALL)))
            return;
        LivingEntity shooter = (LivingEntity) event.getEntity().getShooter();
        EntityEquipment entityEquipment = shooter.getEquipment();
        if (entityEquipment.getItemInHand() == null) return;
        //Check if the item has NBT, all our custom weapons will have NBT.
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(entityEquipment.getItemInHand()));
        if (nmsItem == null || nmsItem.getTag() == null) return;
        //Get the NBT of the item the player is holding.
        if (!(API.isPlayer(shooter))) return;
        if (API.isInSafeRegion(shooter.getLocation()) && event.getEntity().getType() != EntityType.SPLASH_POTION) {
            event.setCancelled(true);
            return;
        }
        int weaponTier = nmsItem.getTag().getInt("itemTier");
        Player player = (Player) shooter;
        player.updateInventory();
        if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING) || EnergyHandler.getPlayerCurrentEnergy(player.getUniqueId()) <= 0) {
            event.setCancelled(true);
            event.getEntity().remove();
            player.playSound(shooter.getLocation(), Sound.WOLF_PANT, 12F, 1.5F);
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, event.getEntity().getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 40);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }
        EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(player.getItemInHand()));
        MetadataUtils.registerProjectileMetadata(nmsItem.getTag(), event.getEntity(), weaponTier);
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
                break;
            case "mount":
                event.setCancelled(true);
            	Player p = (Player) event.getDamager();
        		Horse horse = (Horse) event.getEntity();
        		if(!horse.getVariant().equals(Variant.MULE)) return;
        		if(horse.getOwner().getUniqueId().toString().equalsIgnoreCase(p.getUniqueId().toString())){
                    EntityAPI.removePlayerMountList(p.getUniqueId());
        			horse.remove();
        		}
                break;
            default:
                break;
        }
    }


    /**
     * Listen for Entities being damaged by non Entities.
     * Mainly used for damage cancelling
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
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
        if (event.getCause() == DamageCause.CONTACT || event.getCause() == DamageCause.CONTACT || event.getCause() == DamageCause.DROWNING
                || event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FALL
                || event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.BLOCK_EXPLOSION || event.getCause() == DamageCause.FIRE_TICK) {
            event.setCancelled(true);
            event.setDamage(0);
            event.getEntity().setFireTicks(0);
        }
        if (event.getEntity() instanceof Player && event.getCause() == DamageCause.VOID) {
            event.setCancelled(true);
            event.getEntity().teleport(event.getEntity().getWorld().getSpawnLocation());
        }
    }

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
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
        Player player = event.getEntity();
        Location deathLocation = event.getEntity().getLocation();
        ItemStack armorToSave[] = new ItemStack[5];
//        ArrayList<ItemStack> savedItems = new ArrayList<ItemStack>();
        Location respawnLocation = Teleportation.Cyrennica;
        boolean savedArmorContents = false;
        if (EntityAPI.hasPetOut(player.getUniqueId())) {
            net.minecraft.server.v1_8_R3.Entity pet = EntityAPI.getPlayerPet(player.getUniqueId());
            if (!pet.getBukkitEntity().isDead()) { //Safety check
                pet.getBukkitEntity().remove();
            }
            EntityAPI.removePlayerPetList(player.getUniqueId());
            player.sendMessage("For it's own safety, your pet has returned to its home.");
        }
        if (EntityAPI.hasMountOut(player.getUniqueId())) {
            net.minecraft.server.v1_8_R3.Entity mount = EntityAPI.getPlayerMount(player.getUniqueId());
            if (mount.isAlive()) {
                mount.getBukkitEntity().remove();
            }
            EntityAPI.getPlayerMount(player.getUniqueId());
            player.sendMessage("For it's own safety, your mount has returned to the stable.");
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
            if (player.getInventory().getItem(0)!= null && player.getInventory().getItem(0).getType() != Material.AIR) {
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
            if (new Random().nextInt(99) <= 50) {
                if (player.getInventory().getItem(0)!= null && player.getInventory().getItem(0).getType() != Material.AIR) {
                    armorToSave[4] = player.getInventory().getItem(0);
                }
            }
            if (new Random().nextInt(99) <= 25) {
                armorToSave[0] = player.getEquipment().getBoots();
            }
            if (new Random().nextInt(99) <= 25) {
                armorToSave[1] = player.getEquipment().getLeggings();
            }
            if (new Random().nextInt(99) <= 25) {
                armorToSave[2] = player.getEquipment().getChestplate();
            }
            if (new Random().nextInt(99) <= 25) {
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
                    net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(itemStack);
                    if (nms != null) {
                        if (nms.getTag() != null) {
                            if ((nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("important")) || nms.hasTag() && nms.getTag().hasKey("subtype")) {
                                //event.getDrops().remove(itemStack);
                                continue;
                            }
                        }
                    }
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
            player.getInventory().addItem(new ItemBuilder().setItem(new ItemStack(Material.BREAD, 10)).setNBTString("subtype", "starter").build());
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
                        if (RepairAPI.getCustomDurability(itemStack) - 400 > 0.1D) {
                            RepairAPI.subtractCustomDurability(player, itemStack, 400);
                        }
                        player.getInventory().addItem(itemStack);
                    }
                }
            } else {
                player.getInventory().addItem(new ItemBuilder().setItem(new ItemGenerator().getDefinedStack(Item.ItemType.AXE, Item.ItemTier.TIER_1, Item.ItemModifier.UNCOMMON))
                        .setNBTString("subtype", "starter").build());
                player.getInventory().addItem(new ItemBuilder().setItem(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.HELMET, Armor.ArmorTier.TIER_1, Armor.ArmorModifier.COMMON))
                        .setNBTString("subtype", "starter").build());
                player.getInventory().addItem(new ItemBuilder().setItem(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.CHESTPLATE, Armor.ArmorTier.TIER_1, Armor.ArmorModifier.COMMON))
                        .setNBTString("subtype", "starter").build());
                player.getInventory().addItem(new ItemBuilder().setItem(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.LEGGINGS, Armor.ArmorTier.TIER_1, Armor.ArmorModifier.COMMON))
                        .setNBTString("subtype", "starter").build());
                player.getInventory().addItem(new ItemBuilder().setItem(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.BOOTS, Armor.ArmorTier.TIER_1, Armor.ArmorModifier.COMMON))
                        .setNBTString("subtype", "starter").build());
            }
        }, 20L);
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        PlayerManager.checkInventory(event.getPlayer().getUniqueId());
    }

    /**
     * Listen for Players using their "staff" item
     * Checks to see if they can, and
     * then fires the staff projectile
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerUseStaff(PlayerInteractEvent event) {
        if (!event.hasItem()) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(event.getPlayer().getItemInHand()));
        if (nmsItem == null || nmsItem.getTag() == null || !nmsItem.getTag().hasKey("itemType")) return;

        Item.ItemType itemType = new Attribute(event.getPlayer().getItemInHand()).getItemType();
        if (itemType != Item.ItemType.STAFF) {
            return;
        }
        if (event.getPlayer().isInsideVehicle()) {
            event.setCancelled(true);
            return;
        }
        if (API.isInSafeRegion(event.getPlayer().getLocation())) {
            event.setCancelled(true);
            return;
        }
        if (event.getPlayer().hasMetadata("last_Staff_Use")) {
            event.setCancelled(true);
            if (System.currentTimeMillis() - event.getPlayer().getMetadata("last_Staff_Use").get(0).asLong() < 400)
                return;
        }
        if (event.getPlayer().hasPotionEffect(PotionEffectType.SLOW_DIGGING) || EnergyHandler.getPlayerCurrentEnergy(event.getPlayer().getUniqueId()) <= 0) {
            event.setCancelled(true);
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.WOLF_PANT, 12F, 1.5F);
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, event.getPlayer().getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 40);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }
        event.setCancelled(true);
        event.getPlayer().setMetadata("last_Staff_Use", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        DamageAPI.fireStaffProjectile(event.getPlayer(), event.getPlayer().getItemInHand(), nmsItem.getTag());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onStaffProjectileExplode(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof WitherSkull)) {
            return;
        }
        event.setCancelled(false);
        event.setRadius(0);
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
        if(!event.getEntity().hasMetadata("uuid"))return;
        UUID uuid = UUID.fromString(event.getEntity().getMetadata("uuid").get(0).asString());
        Zombie z = CombatLog.LOGGER.get(uuid);
        z.setBaby(false);
        Inventory inv = CombatLog.LOGGER_INVENTORY.get(uuid);
        if(inv == null){
        	Utils.log.info("COMBAT LOGGER INV NULL");
        	return;
        }
        Location loc =  KarmaHandler.CHAOTIC_RESPAWNS.get(new Random().nextInt(KarmaHandler.CHAOTIC_RESPAWNS.size() - 1));
        for(int i = 0; i < inv.getContents().length; i++){
        	if(i == 0)
        		continue;	
        	ItemStack stack = inv.getItem(i);
        	net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        	if(stack == null || stack.getType() == Material.AIR || nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("important") || nms.hasTag() && nms.getTag().hasKey("subtype"))
        		continue;
        	event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), stack);
        }
        CombatLog.checkCombatLog(uuid);
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, "", true);
  		DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, new ArrayList<String>(), true);
  		if(loc != null){
  			String locString = loc.getBlockX() +"," + loc.getBlockY() + 5 + "," + loc.getBlockZ() + "," + "0,0";
  			DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.CURRENT_LOCATION, locString, true);
  		}
  		DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.LOGGERDIED, true, true);
        CombatLog.LOGGER_INVENTORY.remove(uuid);
    }
}