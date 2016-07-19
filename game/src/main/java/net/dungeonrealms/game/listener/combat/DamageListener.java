package net.dungeonrealms.game.listener.combat;

import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import net.dungeonrealms.game.events.PlayerEnterRegionEvent;
import net.dungeonrealms.game.handlers.EnergyHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.listener.mechanic.RestrictionListener;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mechanics.PlayerManager;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.combat.CombatLogger;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.entities.powermoves.PowerStrike;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRWitch;
import net.dungeonrealms.game.world.entities.utils.BuffUtils;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import net.dungeonrealms.game.world.items.Attribute;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.dungeonrealms.game.world.spawning.BaseMobSpawner;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.*;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
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
        if (event.getEntity() instanceof LivingEntity) if (event.getCause() == DamageCause.SUFFOCATION)
            event.setCancelled(true);
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
        event.setYield(0.0F);
        event.setCancelled(true);
        if (event.getEntity().getMetadata("type").get(0).asString().equalsIgnoreCase("buff")) {
            event.setCancelled(true);
            event.blockList().clear();
            List<Player> toBuff = new ArrayList<>();
            for (Entity entity : event.getEntity().getNearbyEntities(8D, 8D, 8D)) {
                if (!GameAPI.isPlayer(entity)) continue;
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
        if (!(GameAPI.isPlayer(event.getDamager()))) return;
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
     * Checks for null gameplayer on damage by entity. Keep this priority lowest because onPlayerHitEntity and
     * onMonsterHitPlayer are priority low.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void entDamageNullCheck(EntityDamageByEntityEvent event) {
        if (GameAPI.isPlayer(event.getDamager())) {
            if (GameAPI.getGamePlayer((Player) event.getDamager()) == null) {
                event.setCancelled(true);
            }
        }
        if (GameAPI.isPlayer(event.getEntity())) {
            if (GameAPI.getGamePlayer((Player) event.getEntity()) == null) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Makes mobs untarget a player after they have entered a safezone.
     *
     * @param event
     */
    public void onPlayerEnterSafezone(PlayerEnterRegionEvent event) {
        if (GameAPI.isInSafeRegion(event.getPlayer().getLocation())) {
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
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMonsterHitPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) return;
        if ((!(event.getDamager() instanceof LivingEntity)) && (!DamageAPI.isBowProjectile(event.getDamager()) && (!DamageAPI.isStaffProjectile(event.getDamager()))))
            return;
        if (!(GameAPI.isPlayer(event.getEntity()))) return;
        if (!(event.getDamager() instanceof LivingEntity)) {
            if (!(((Projectile) event.getDamager()).getShooter() instanceof LivingEntity)) return;
            if (((Projectile) event.getDamager()).getShooter() instanceof Player) return;
        }

        event.setDamage(0);

        double finalDamage = 0;
        Player player = (Player) event.getEntity();
        LivingEntity leDamageSource = event.getDamager() instanceof LivingEntity ? (LivingEntity) event.getDamager()
                : (LivingEntity) ((Projectile) event.getDamager()).getShooter();
        if (event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            EntityEquipment attackerEquipment = attacker.getEquipment();
            if (attackerEquipment.getItemInMainHand() == null) return;
            attackerEquipment.getItemInMainHand().setDurability(((short) -1));
            //Check if it's a {WEAPON} the mob is hitting with. Once of our custom ones!
            if (!GameAPI.isWeapon(attackerEquipment.getItemInMainHand())) return;
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

        if (PowerStrike.powerStrike.contains(leDamageSource.getUniqueId())) {
            finalDamage *= 2;
            PowerStrike.chargedMonsters.remove(leDamageSource.getUniqueId());
            PowerStrike.powerStrike.remove(leDamageSource.getUniqueId());
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 0.5F);
            player.getWorld().playEffect(player.getLocation(), Effect.EXPLOSION, 3, 3);
        }

        double[] armorCalculation = DamageAPI.calculateArmorReduction(leDamageSource, player, finalDamage, null);
        finalDamage = finalDamage - armorCalculation[0];
        double armorReducedDamage = armorCalculation[0];
        String attackerName;
        if (leDamageSource.hasMetadata("customname")) {
            attackerName = leDamageSource.getMetadata("customname").get(0).asString().trim();
        } else {
            attackerName = "Enemy";
        }
        if (armorReducedDamage == -1) {
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "                        *DODGE* (" + ChatColor.RED + attackerName + ChatColor.GREEN + ")");
            //The defender dodged the attack
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.5F, 2.0F);
            finalDamage = 0;
        } else if (armorReducedDamage == -2) {
            player.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "                        *BLOCK* (" + ChatColor.RED + attackerName + ChatColor.DARK_GREEN + ")");
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2F, 1.0F);
            finalDamage = 0;
        } else if (armorReducedDamage == -3) {
            //Reflect when its fixed. @TODO
        } else {
            finalDamage = finalDamage - armorCalculation[0];
        }
        HealthHandler.getInstance().handlePlayerBeingDamaged(player, leDamageSource, finalDamage, armorCalculation[0], armorCalculation[1]);
    }

    /**
     * Handling Duels. When a player punches another player.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void playerPunchPlayer(EntityDamageByEntityEvent event) {
        if (!GameAPI.isPlayer(event.getEntity()) || !GameAPI.isPlayer(event.getDamager()))
            return;
        Player p1 = (Player) event.getDamager();
        Player p2 = (Player) event.getEntity();

        event.setDamage(0);

        if (!GameAPI.isNonPvPRegion(p1.getLocation()) && !GameAPI.isNonPvPRegion(p2.getLocation())) return;
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
        String metaValue = event.getEntity().getMetadata("type").get(0).asString().toLowerCase();
        switch (metaValue) {
            case "pet":
                event.setCancelled(true);
                event.setDamage(0);
                break;
            case "mount":
                event.setCancelled(true);
                event.setDamage(0);
                Player p = null;
                if (event.getDamager() instanceof Player) {
                    p = (Player) event.getDamager();
                } else if (event.getDamager() instanceof Projectile) {
                    if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                        p = (Player) ((Projectile) event.getDamager()).getShooter();
                    }
                }
                if (p == null) return;
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

        if (Rank.isGM(p))
            event.getDrops().clear();

        for (ItemStack itemStack : new ArrayList<>(event.getDrops())) {
            if (!GameAPI.isItemDroppable(itemStack) || GameAPI.isItemUntradeable(itemStack)) {
                event.getDrops().remove(itemStack);
            }
        }
        List<ItemStack> gearToSave = new ArrayList<>();
        KarmaHandler.EnumPlayerAlignments alignment = KarmaHandler.getInstance().getPlayerRawAlignment(p);

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
            if (!neutral_weapon && GameAPI.isWeapon(weapon_slot)) {
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
            if (GameAPI.isItemPermanentlyUntradeable(stack)) {
                gearToSave.add(stack);
            }
        }

        for (ItemStack stack : gearToSave) {
            if (event.getDrops().contains(stack)) {
                event.getDrops().remove(stack);
            }
        }

        List<ItemStack> toDrop = new ArrayList<>();
        for (ItemStack stack : event.getDrops()) {
            if (stack.getType() != Material.SKULL_ITEM) {
                toDrop.add(stack);
            }
        }
        event.getDrops().clear();

        for (ItemStack stack : toDrop) {
            event.getEntity().getWorld().dropItemNaturally(deathLocation, stack);
        }
        toDrop.clear();

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
        event.getDrops().clear();
        p.setGameMode(GameMode.SPECTATOR);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1));
        p.teleport(respawnLocation);
        p.setFireTicks(0);
        p.setFallDistance(0);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            event.getDrops().clear();
            p.setCanPickupItems(true);
            p.setGameMode(GameMode.SURVIVAL);
            GamePlayer gamePlayer = GameAPI.getGamePlayer(p);
            if (gamePlayer != null) {
                gamePlayer.getAttributeBonusesFromStats().entrySet().forEach(entry -> entry.setValue(0f));
                gamePlayer.getAttributes().entrySet().forEach(entry -> entry.setValue(new Integer[]{0, 0}));
            }

            PlayerManager.checkInventory(p.getUniqueId());

            for (ItemStack stack : gearToSave) {
                p.getInventory().addItem(stack);
            }

            ItemManager.giveStarter(p);
        }, 20L);
    }

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
        if (!(GameAPI.isWeapon(leShooter.getEquipment().getItemInMainHand())) && entityType != EntityType.WITCH) return;

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
            if (GameAPI.isInSafeRegion(le.getLocation())) continue;
            if (!GameAPI.isPlayer(le)) continue;
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
        if (GameAPI.isInSafeRegion(player.getLocation())) {
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
//        if (!RestrictionListener.canPlayerUseTier(player, RepairAPI.getArmorOrWeaponTier(player.getEquipment().getItemInMainHand()))) {
//            player.sendMessage(org.bukkit.ChatColor.RED + "You must to be " + org.bukkit.ChatColor.UNDERLINE + "at least" + org.bukkit.ChatColor.RED + " level "
//                    + RestrictionListener.getLevelToUseTier(RepairAPI.getArmorOrWeaponTier(player.getEquipment().getItemInMainHand())) + " to use this weapon.");
//            event.setCancelled(true);
//            event.setUseItemInHand(Event.Result.DENY);
//            EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), 1F);
//            return;
//        }

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
                    if ((nmsStack.hasTag() && nmsStack.getTag() != null && nmsStack.getTag().hasKey("type") && nmsStack.getTag().getString("type").equalsIgnoreCase("important")) || (nmsStack.hasTag() && nmsStack.getTag().hasKey("subtype"))) {
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
                    if ((nmsStack.hasTag() && nmsStack.getTag() != null && nmsStack.getTag().hasKey("type") && nmsStack.getTag().getString("type").equalsIgnoreCase("important")) || (nmsStack.hasTag() && nmsStack.getTag().hasKey("subtype"))) {
                        continue;
                    }
                    location.getWorld().dropItemNaturally(location, itemStack);
                }
            }
            ArrayList<String> armorContents = new ArrayList<>();
            String itemsToSave;
            if (!combatLogger.getArmorToSave().isEmpty()) {
                for (ItemStack itemStack : combatLogger.getArmorToSave()) {
                    if (itemStack.getType() == null || itemStack.getType() == Material.AIR || itemStack.getType() == Material.MELON) {
                        armorContents.add("null");
                    } else {
                        armorContents.add(ItemSerialization.itemStackToBase64(itemStack));
                    }
                }
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, armorContents, false, true);
            } else {
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, new ArrayList<String>(), false, true);
            }
            if (!combatLogger.getItemsToSave().isEmpty()) {
                Inventory inventory = Bukkit.createInventory(null, 27, "LoggerInventory");
                combatLogger.getItemsToSave().forEach(inventory::addItem);
                itemsToSave = ItemSerialization.toString(inventory);
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, itemsToSave, false, true);
            } else {
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, "", false, true);
            }
            combatLogger.handleNPCDeath();
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

        if (event.getEntity().hasMetadata("lastEnvironmentDamage") && (System.currentTimeMillis() - event.getEntity()
                .getMetadata("lastEnvironmentDamage").get(0).asLong()) < 800) {
            event.setCancelled(true);
            return;
        }
        event.getEntity().setMetadata("lastEnvironmentDamage", new FixedMetadataValue(DungeonRealms.getInstance(),
                System.currentTimeMillis()));

        int maxHP;
        if (GameAPI.isPlayer(event.getEntity())) {
            if (GameAPI.getGamePlayer((Player)event.getEntity()) == null) return;
            maxHP = GameAPI.getGamePlayer((Player) event.getEntity()).getPlayerMaxHP();
        } else {
            maxHP = HealthHandler.getInstance().getMonsterHPLive((LivingEntity) event.getEntity());
        }
        if (GameAPI.isInSafeRegion(event.getEntity().getLocation())) {
            event.setDamage(0);
            event.setCancelled(true);
            return;
        }


        switch (event.getCause()) {
            case FALL:
                float blocks = event.getEntity().getFallDistance();
                if (blocks >= 2) {
                    dmg = maxHP * 0.02D * event.getDamage();
                }
                if (GameAPI.isPlayer(event.getEntity())) {
                    Player p = (Player) event.getEntity();
                    GamePlayer gp = GameAPI.getGamePlayer(p);
                    if (dmg >= gp.getPlayerCurrentHP()) {
                        dmg = gp.getPlayerCurrentHP() - 1;
                    }
                    if (blocks >= 49 && dmg <= gp.getPlayerCurrentHP()) {
                        Achievements.getInstance().giveAchievement(p.getUniqueId(), Achievements.EnumAchievements.LEAP_OF_FAITH);

                    }
                }
                break;
            case DROWNING:
                if (GameAPI.isPlayer(event.getEntity())) {
                    dmg = maxHP * 0.04;
                } else
                    dmg = 0;
                break;
            case FIRE_TICK:
                if (!(((LivingEntity) event.getEntity()).hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)))
                    dmg = maxHP * 0.01;
                else dmg = 0;
                break;
            case LAVA:
            case FIRE:
                if (!(((LivingEntity) event.getEntity()).hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)))
                    dmg = maxHP * 0.03;
                else dmg = 0;
                break;
            case POISON:
                dmg = maxHP * 0.01;
                break;
            case CONTACT:
                dmg = maxHP * 0.03;
                break;
            case SUFFOCATION:
                return;
            case VOID: // should only be when exiting realm
                return;
            default:
                return;
        }
        if (dmg > 0) {
            if (event.getEntity() instanceof Player) {
                HealthHandler.getInstance().handlePlayerBeingDamaged((Player) event.getEntity(), null, dmg, 0, 0, event.getCause());
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
        if (itemType != Item.ItemType.BOW) {
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

        if (GameAPI.isInSafeRegion(player.getLocation())) {
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

//        if (!RestrictionListener.canPlayerUseTier(player, RepairAPI.getArmorOrWeaponTier(hand))) {
//            player.sendMessage(org.bukkit.ChatColor.RED + "You must to be " + org.bukkit.ChatColor.UNDERLINE + "at least" + org.bukkit.ChatColor.RED + " level "
//                    + RestrictionListener.getLevelToUseTier(RepairAPI.getArmorOrWeaponTier(hand)) + " to use this weapon.");
//            event.setCancelled(true);
//            event.setUseItemInHand(Event.Result.DENY);
//            EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), 1F);
//            return;
//        }

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
                        if (GameAPI.isInSafeRegion(ent.getLocation())) continue;
                        if (!GameAPI.isPlayer(ent)) continue;
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
                        entity.setCustomName(newLevelName + GameAPI.getTierColor(2).toString() + "Lesser Spawn of Inferno");
                        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(2).toString() + "Lesser Spawn of Inferno"));
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
                        entity.setCustomName(newLevelName + GameAPI.getTierColor(tier).toString() + EnumMonster.MagmaCube.name);
                        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(tier).toString() + EnumMonster.MagmaCube.name));
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