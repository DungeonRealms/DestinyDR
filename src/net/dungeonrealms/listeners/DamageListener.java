package net.dungeonrealms.listeners;

import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.duel.DuelMechanics;
import net.dungeonrealms.energy.EnergyHandler;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.health.HealthHandler;
import net.dungeonrealms.items.Attribute;
import net.dungeonrealms.items.DamageAPI;
import net.dungeonrealms.items.Item;
import net.dungeonrealms.karma.KarmaHandler;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.dungeonrealms.mechanics.PlayerManager;
import net.dungeonrealms.spawning.MobSpawner;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.dungeonrealms.teleportation.Teleportation;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Nick on 9/17/2015.
 */
public class DamageListener implements Listener {

    /**
     * This event listens for EnderCrystal explosions.
     * Which are buffs.. with the correct nbt at least.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBuffExplode(EntityExplodeEvent event) {
        event.blockList().clear();
        if (!(event.getEntity().hasMetadata("type"))) return;
        if (event.getEntity().getMetadata("type").get(0).asString().equalsIgnoreCase("buff")) {
            event.setCancelled(true);
            int radius = event.getEntity().getMetadata("radius").get(0).asInt();
            int duration = event.getEntity().getMetadata("duration").get(0).asInt();
            PotionEffectType effectType = PotionEffectType.getByName(event.getEntity().getMetadata("effectType").get(0).asString());
            for (Entity e : event.getEntity().getNearbyEntities(radius, radius, radius)) {
                if (!(e instanceof Player)) continue;
                ((Player) e).addPotionEffect(new PotionEffect(effectType, duration, 2));
                e.sendMessage(new String[]{
                        "",
                        ChatColor.BLUE + "[BUFF] " + ChatColor.YELLOW + "You have received the " + ChatColor.UNDERLINE + effectType.getName() + ChatColor.YELLOW + " buff!",
                        ""
                });
            }
        }
    }

    /**
     * Cancel World Guard PVP flag if players are dueling.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void disallowPVPEvent(DisallowedPVPEvent event) {
        Player p1 = event.getAttacker();
        Player p2 = event.getDefender();
        if (DuelMechanics.isDueling(p1.getUniqueId()) && DuelMechanics.isDueling(p2.getUniqueId())) {
            if (DuelMechanics.isDuelPartner(p1.getUniqueId(), p2.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void playerBreakArmorStand(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (((Player) event.getDamager()).getGameMode() != GameMode.CREATIVE) return;
        //Armor Stand Spawner check.
        if (event.getEntity().getType() == EntityType.ARMOR_STAND) {
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
        if ((!(event.getDamager() instanceof Player)) && ((event.getDamager().getType() != EntityType.ARROW) && (event.getDamager().getType() != EntityType.WITHER_SKULL)))
            return;
        if (!(event.getEntity().hasMetadata("type"))) return;
        //Make sure the player is HOLDING something!
        double finalDamage = 0;
        if (event.getDamager() instanceof Player) {
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
                attacker.playSound(attacker.getLocation(), Sound.WOLF_PANT, 12F, 1.5F);
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, event.getEntity().getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 40);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return;
            }
            if (CombatLog.isInCombat(attacker.getUniqueId())) {
                CombatLog.updateCombat(attacker.getUniqueId());
            } else {
                CombatLog.addToCombat(attacker.getUniqueId());
            }
            EnergyHandler.removeEnergyFromPlayerAndUpdate(attacker.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(attacker.getItemInHand()));
            attacker.getItemInHand().setDurability(((short) -1));
            finalDamage = DamageAPI.calculateWeaponDamage(attacker, event.getEntity(), tag);
        } else if (event.getDamager().getType() == EntityType.ARROW) {
            Arrow attackingArrow = (Arrow) event.getDamager();
            if (!(attackingArrow.getShooter() instanceof Player)) return;
            if (attackingArrow.getShooter() != null && attackingArrow.getShooter() instanceof Player) {
                finalDamage = DamageAPI.calculateProjectileDamage((Player) attackingArrow.getShooter(), event.getEntity(), attackingArrow);
                if (CombatLog.isInCombat(((Player) attackingArrow.getShooter()).getUniqueId())) {
                    CombatLog.updateCombat(((Player) attackingArrow.getShooter()).getUniqueId());
                } else {
                    CombatLog.addToCombat(((Player) attackingArrow.getShooter()).getUniqueId());
                }
            }
        } else if (event.getDamager().getType() == EntityType.WITHER_SKULL) {
            WitherSkull staffProjectile = (WitherSkull) event.getDamager();
            if (!(staffProjectile.getShooter() instanceof Player)) return;
            if (staffProjectile.getShooter() != null && staffProjectile.getShooter() instanceof Player) {
                finalDamage = DamageAPI.calculateProjectileDamage((Player) staffProjectile.getShooter(), event.getEntity(), staffProjectile);
                if (CombatLog.isInCombat(((Player) staffProjectile.getShooter()).getUniqueId())) {
                    CombatLog.updateCombat(((Player) staffProjectile.getShooter()).getUniqueId());
                } else {
                    CombatLog.addToCombat(((Player) staffProjectile.getShooter()).getUniqueId());
                }
            }
        }
        event.setDamage(finalDamage);
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
        if ((!(event.getDamager() instanceof Monster)) && ((event.getDamager().getType() != EntityType.ARROW) && (event.getDamager().getType() != EntityType.WITHER_SKULL)))
            return;
        if (!(event.getEntity() instanceof Player)) return;
        double finalDamage = 0;
        if (event.getDamager() instanceof Monster) {
            Monster attacker = (Monster) event.getDamager();
            EntityEquipment attackerEquipment = attacker.getEquipment();
            if (attackerEquipment.getItemInHand() == null) return;
            attackerEquipment.getItemInHand().setDurability(((short) -1));
            //Check if the item has NBT, all our custom weapons will have NBT.
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(attackerEquipment.getItemInHand()));
            if (nmsItem == null || nmsItem.getTag() == null) {
                Bukkit.broadcastMessage("MOB " + event.getDamager() + " does not have one of our custom weapons. CHASE!!!!!!");
                return;
            }
            //Get the NBT of the item the mob is holding.
            NBTTagCompound tag = nmsItem.getTag();
            //Check if it's a {WEAPON} the mob is hitting with. Once of our custom ones!
            if (!tag.getString("type").equalsIgnoreCase("weapon")) return;
            finalDamage = DamageAPI.calculateWeaponDamage(attacker, event.getEntity(), tag);
        } else if (event.getDamager().getType() == EntityType.ARROW) {
            Arrow attackingArrow = (Arrow) event.getDamager();
            if (!(attackingArrow.getShooter() instanceof Monster)) return;
            finalDamage = DamageAPI.calculateProjectileDamage((LivingEntity) attackingArrow.getShooter(), event.getEntity(), attackingArrow);
        } else if (event.getDamager().getType() == EntityType.WITHER_SKULL) {
            WitherSkull staffProjectile = (WitherSkull) event.getDamager();
            if (!(staffProjectile.getShooter() instanceof Monster)) return;
            finalDamage = DamageAPI.calculateProjectileDamage((LivingEntity) staffProjectile.getShooter(), event.getEntity(), staffProjectile);
        }
        if (CombatLog.isInCombat(event.getEntity().getUniqueId())) {
            CombatLog.updateCombat(event.getEntity().getUniqueId());
        } else {
            CombatLog.addToCombat(event.getEntity().getUniqueId());
        }
        event.setDamage(finalDamage);
    }


    /**
     * Reduces damage after it is set previously based on the defenders armor
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onArmorReduceBaseDamage(EntityDamageByEntityEvent event) {
        if ((!(event.getDamager() instanceof LivingEntity)) && ((event.getDamager().getType() != EntityType.ARROW) && (event.getDamager().getType() != EntityType.WITHER_SKULL)))
            return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        double armourReducedDamage = 0;
        LivingEntity defender = (LivingEntity) event.getEntity();
        EntityEquipment defenderEquipment = defender.getEquipment();
        if (defenderEquipment.getArmorContents() == null) return;
        ItemStack[] defenderArmor = defenderEquipment.getArmorContents();
        defenderArmor[0].setDurability((short) -1);
        defenderArmor[1].setDurability((short) -1);
        defenderArmor[2].setDurability((short) -1);
        defenderArmor[3].setDurability((short) -1);
        if (event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            armourReducedDamage = DamageAPI.calculateArmorReduction(attacker, defender, defenderArmor);
            if (attacker.getEquipment().getItemInHand() != null && attacker.getEquipment().getItemInHand().getType() != Material.AIR) {
                net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(attacker.getEquipment().getItemInHand()));
                if (nmsItem != null && nmsItem.getTag() != null) {
                    if (new Attribute(attacker.getEquipment().getItemInHand()).getItemType() == Item.ItemType.POLE_ARM && !(DamageAPI.polearmAOEProcessing.contains(attacker))) {
                        DamageAPI.polearmAOEProcessing.add(attacker);
                        for (Entity entityNear : event.getEntity().getNearbyEntities(2.5, 3, 2.5)) {
                            if (entityNear instanceof LivingEntity && entityNear != event.getEntity() && entityNear != event.getDamager()) {
                                if (event.getDamager().hasMetadata("type")) {
                                    if (!(entityNear instanceof Player)) {
                                        break;
                                    } else {
                                        ((LivingEntity) entityNear).damage((event.getDamage()), attacker);
                                        Vector unitVector = entityNear.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
                                        entityNear.setVelocity(unitVector.multiply(0.15D));
                                    }
                                } else {
                                    ((LivingEntity) entityNear).damage((event.getDamage()), attacker);
                                    Vector unitVector = entityNear.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
                                    entityNear.setVelocity(unitVector.multiply(0.15D));
                                }
                            }
                        }
                        DamageAPI.polearmAOEProcessing.remove(attacker);
                    }
                }
            }
        } else if (event.getDamager().getType() == EntityType.ARROW) {
            Arrow attackingArrow = (Arrow) event.getDamager();
            if (!(attackingArrow.getShooter() instanceof LivingEntity)) return;
            if (attackingArrow.getShooter() instanceof Monster) {
                armourReducedDamage = DamageAPI.calculateArmorReduction(attackingArrow, defender, defenderArmor);
            }
        } else if (event.getDamager().getType() == EntityType.WITHER_SKULL) {
            WitherSkull staffProjectile = (WitherSkull) event.getDamager();
            if (!(staffProjectile.getShooter() instanceof LivingEntity)) return;
            if (staffProjectile.getShooter() instanceof Monster) {
                armourReducedDamage = DamageAPI.calculateArmorReduction(staffProjectile, defender, defenderArmor);
            }
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
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CLOUD, defender.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 20);
            } catch (Exception ex) {
                ex.printStackTrace();
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
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.RED_DUST, defender.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 20);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            if (defender instanceof Player) {
                if (((Player) defender).isBlocking() && ((Player) defender).getItemInHand() != null && ((Player) defender).getItemInHand().getType() != Material.AIR) {
                    if (new Random().nextInt(100) <= 80) {
                        double blockDamage = event.getDamage() / 2;
                        HealthHandler.handlePlayerBeingDamaged((Player) event.getEntity(), (blockDamage - armourReducedDamage));
                        event.setDamage(0);
                        return;
                    }
                } else {
                    HealthHandler.handlePlayerBeingDamaged((Player) event.getEntity(), (event.getDamage() - armourReducedDamage));
                    event.setDamage(0);
                    return;
                }
            }
            event.setDamage(event.getDamage() - armourReducedDamage);
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
        if (!(event.getEntity().getShooter() instanceof Player) && ((event.getEntityType() != EntityType.ARROW) && (event.getEntityType() != EntityType.WITHER_SKULL)))
            return;
        LivingEntity shooter = (LivingEntity) event.getEntity().getShooter();
        EntityEquipment entityEquipment = shooter.getEquipment();
        if (entityEquipment.getItemInHand() == null) return;
        entityEquipment.getItemInHand().setDurability((short) -1);
        //Check if the item has NBT, all our custom weapons will have NBT.
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(entityEquipment.getItemInHand()));
        if (nmsItem == null || nmsItem.getTag() == null) return;
        //Get the NBT of the item the player is holding.
        if (!(shooter instanceof Player)) return;
        int weaponTier = new Attribute(entityEquipment.getItemInHand()).getItemTier().getTierId();
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
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            player.getItemInHand().setDurability((short) -1);
        }
        String metaValue = event.getEntity().getMetadata("type").get(0).asString().toLowerCase();
        switch (metaValue) {
            case "pet":
                event.setCancelled(true);
                break;
            case "mount":
                event.setCancelled(true);
                break;
            default:
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
                    event.getEntity().setFireTicks(0);
                    break;
                case "mount":
                    event.setCancelled(true);
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
                || event.getCause() == DamageCause.FALL || event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.FIRE
                || event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.BLOCK_EXPLOSION) {
            event.setCancelled(true);
            event.setDamage(0);
            event.getEntity().setFireTicks(0);
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
        Player player = event.getEntity();
        ItemStack itemToSave = null;
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
        if (player.getInventory().getItem(0) != null && player.getInventory().getItem(0).getType() != Material.AIR) {
            if (KarmaHandler.getPlayerRawAlignment(player).equalsIgnoreCase(KarmaHandler.EnumPlayerAlignments.LAWFUL.name()) ||
                    KarmaHandler.getPlayerRawAlignment(player).equalsIgnoreCase(KarmaHandler.EnumPlayerAlignments.NEUTRAL.name())) {
                itemToSave = player.getInventory().getItem(0);
            }
        }
        event.setDroppedExp(0);
        for (ItemStack itemStack : event.getDrops()) {
            if (itemStack != null) {
                    if (itemStack.equals(itemToSave)) {
                        break;
                    }
                net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(itemStack);
                if (nms.hasTag()) {
                    if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("important")) {
                        break;
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                    }
                }
            }
        }
        event.getDrops().clear();
        player.setHealth(3);
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 10));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 10));
        player.teleport(Teleportation.Cyrennica);
        player.setFireTicks(0);
        player.setMaximumNoDamageTicks(50);
        player.setNoDamageTicks(50);
        player.setFallDistance(0);
        final ItemStack finalItemToSave = itemToSave;
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            PlayerManager.checkInventory(player.getUniqueId());
            if (finalItemToSave != null) {
                player.getInventory().addItem(finalItemToSave);
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
        DamageAPI.fireStaffProjectile(event.getPlayer(), event.getPlayer().getItemInHand(), nmsItem.getTag());
        EnergyHandler.removeEnergyFromPlayerAndUpdate(event.getPlayer().getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(event.getPlayer().getItemInHand()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onStaffProjectileExplode(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof WitherSkull)) {
            return;
        }
        event.setCancelled(false);
        event.setRadius(0);
    }
}
