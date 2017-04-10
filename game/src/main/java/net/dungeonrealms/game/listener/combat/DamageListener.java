package net.dungeonrealms.game.listener.combat;

import com.google.common.collect.Lists;
import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.event.PlayerEnterRegionEvent;
import net.dungeonrealms.game.handler.EnergyHandler;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ItemWeaponRanged;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.listener.mechanic.RestrictionListener;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.GraveyardMechanic;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.miscellaneous.Graveyard;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.combat.CombatLogger;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.powermove.type.PowerStrike;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DREnderman;
import net.dungeonrealms.game.world.entity.type.monster.base.DRWitch;
import net.dungeonrealms.game.world.entity.type.monster.boss.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumDungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.util.BuffUtils;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.Item.ElementalAttribute;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.*;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

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
        if (event.getEntity().getWorld().getName().contains("DUNGEON")) return;
        if (!(event.getEntity().hasMetadata("type"))) return;
        event.blockList().clear();
        event.setYield(0.0F);
        event.setCancelled(true);
        if (EnumEntityType.BUFF.isType(event.getEntity())) {
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
        if (DuelingMechanics.isDuelPartner(p1.getUniqueId(), p2.getUniqueId())) {
            DuelOffer offer = DuelingMechanics.getOffer(p1.getUniqueId());
            if (offer != null && offer.canFight)
                event.setCancelled(true);
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
        
        event.setDamage(0);
        event.setCancelled(true);
    }


    /**
     * Checks for null gameplayer on damage by entity. Keep this priority lowest because onPlayerHitEntity and
     * onMonsterHitPlayer are priority low.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void entDamageNullCheck(EntityDamageByEntityEvent event) {
        if (GameAPI.isPlayer(event.getDamager()))
            if (GameAPI.getGamePlayer((Player) event.getDamager()) == null)
                event.setCancelled(true);
                
        if (GameAPI.isPlayer(event.getEntity()))
            if (GameAPI.getGamePlayer((Player) event.getEntity()) == null)
                event.setCancelled(true);
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
        if (!GameAPI.isPlayer(event.getEntity()))
        	return;
        
        if (event.getDamager() instanceof Projectile) {
            if (!(((Projectile) event.getDamager()).getShooter() instanceof LivingEntity))
            	return;
            if (((Projectile) event.getDamager()).getShooter() instanceof Player)
            	return;
        }

        event.setDamage(0);

        Player player = (Player) event.getEntity();
        LivingEntity leDamageSource = event.getDamager() instanceof LivingEntity ? (LivingEntity) event.getDamager()
                : (LivingEntity) ((Projectile) event.getDamager()).getShooter();
        
        if (!player.hasMetadata("loggingIn"))
        	return;
        
        AttackResult res = new AttackResult(leDamageSource, (LivingEntity)event.getEntity(),
        		DamageAPI.isBowProjectile(event.getDamager()) || DamageAPI.isStaffProjectile(event.getDamager()) ? (Projectile)event.getDamager() : null);
        
        DamageAPI.calculateWeaponDamage(res, false);
        CombatLog.updateCombat(player);
        
        if (PowerStrike.powerStrike.contains(leDamageSource.getUniqueId())) {
        	res.setDamage(res.getDamage() * 2);
        	PowerStrike.chargedMonsters.remove(leDamageSource.getUniqueId());
        	PowerStrike.powerStrike.remove(leDamageSource.getUniqueId());
        	player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 0.5F);
        	player.getWorld().playEffect(player.getLocation(), Effect.EXPLOSION, 3, 3);
        }
        
        DamageAPI.applyArmorReduction(res, true);
        
        res.applyDamage();
    }

    /**
     * Handling Duels. When a player punches another player.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void playerPunchPlayer(EntityDamageByEntityEvent event) {
        if (!GameAPI.isPlayer(event.getEntity()) || !GameAPI.isPlayer(event.getDamager()))
            return;
        Player p1 = (Player) event.getDamager();
        Player p2 = (Player) event.getEntity();

        event.setDamage(0);

        if (!GameAPI.isNonPvPRegion(p1.getLocation()) && !GameAPI.isNonPvPRegion(p2.getLocation())) return;

        if (!GameAPI.isMainWorld(p1.getWorld()))
        	return;

        if (event.isCancelled() && p1.isSneaking()) {
            if (!(p1.hasMetadata("duelCooldown") && p1.getMetadata("duelCooldown").get(0).asLong() > System.currentTimeMillis())) {
                //Check if anyone has a duel already.
                DuelOffer currentDuel = DuelingMechanics.getOffer(p1.getUniqueId());
                DuelOffer otherDuel = DuelingMechanics.getOffer(p2.getUniqueId());
                if (currentDuel == null && otherDuel == null) {
                    //Send invite?
                    DuelingMechanics.sendDuelRequest(p1, p2);
                    return;
                }
            }
        }

        if (DuelingMechanics.isDueling(p1.getUniqueId()) && !DuelingMechanics.isDuelPartner(p1.getUniqueId(), p2.getUniqueId())) {
            p1.sendMessage("That's not your dueling partner!");
            event.setDamage(0);
            event.setCancelled(true);
            return;
        }

        DuelOffer offer = DuelingMechanics.getOffer(p1.getUniqueId());
        if (offer != null && !offer.canFight) {
            event.setCancelled(true);
            event.setDamage(0);
        }
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDamageOnMount(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            if (event.getEntity().getVehicle() != null && event.getEntity().getVehicle().isValid()) {
                Entity vehicle = event.getEntity().getVehicle();

                if (vehicle.hasMetadata("mount")) {
                    EnumMounts mount = EnumMounts.getByName(vehicle.getMetadata("mount").get(0).asString());
                    if (mount == null || mount.getMountData() == null) return;

                    if (vehicle.hasMetadata("type") && vehicle.hasMetadata("owner") && EntityMechanics.PLAYER_MOUNTS.containsValue(vehicle)) {
                        //Will eject the mount and handle through
                        event.getEntity().sendMessage(ChatColor.RED + "Your mount has been dismissed due to taking damage.");
                        vehicle.eject();
                    }
                }
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
    public void mountDamageListener(EntityDamageByEntityEvent event) {
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
                if (event.getEntity() instanceof Horse) {
                    Horse horse = (Horse) event.getEntity();
                    if (!horse.getVariant().equals(Variant.MULE)) return;
                    if (horse.getOwner().getUniqueId().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
                        if (!GameAPI.isInSafeRegion(event.getEntity().getLocation())) {
                            // Not in a safezone
                            EntityAPI.removePlayerMountList(p.getUniqueId());
                            horse.remove();
                        } else {
                            if (event.getDamager() instanceof Player) {
                                event.getDamager().sendMessage(org.bukkit.ChatColor.RED + "You cannot damage a mount in a " + org.bukkit.ChatColor.UNDERLINE + "safezone");
                            }
                        }
                    }
                } else {
                    if (!GameAPI.isNonPvPRegion(event.getEntity().getLocation())) {
                        Entity passenger = event.getEntity().getPassenger();
                        if (passenger != null && passenger instanceof Player) {
                            passenger.sendMessage(ChatColor.RED + "You have been dismounted due to your mount taking damage.");
                        }
                        event.getEntity().eject();
                    } else {
                        event.setCancelled(true);
                        if (event.getDamager() instanceof Player)
                            event.getDamager().sendMessage(org.bukkit.ChatColor.RED + "You cannot damage a mount in a " + org.bukkit.ChatColor.UNDERLINE + "safezone");
                    }
                    break;
                }
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
                case "mount":
                case "spawner":
                	event.setCancelled(true);
                    event.setDamage(0);
                    event.getEntity().setFireTicks(0);
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
            //Running this one tick later avoids a screen lock. (Player cannot move and is frozen in place under the map)
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
                event.getEntity().teleport(TeleportLocation.CYRENNICA.getLocation());
                event.getEntity().setFallDistance(0);
            }, 1);
        }

        if (!(event.getEntity() instanceof Player) && event.getCause() == DamageCause.FALL) {
            event.setDamage(0);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handlePlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
        final Player p = event.getEntity();
        final Location deathLocation = p.getEyeLocation();
        p.setExp(0F);
        p.setLevel(0);

        if (Rank.isTrialGM(p) && !Rank.isDev(p))
            event.getDrops().clear();

        List<ItemStack> gearToSave = new ArrayList<>();
        KarmaHandler.EnumPlayerAlignments alignment = KarmaHandler.getInstance().getPlayerRawAlignment(p);

        if (alignment == null) return;
        
        //  KEEP PERMANENT UNTRADEABLE  //
        event.getDrops().stream().filter(ItemManager::isItemPermanentlyUntradeable).forEach(gearToSave::add);
        
        ItemStack dontSave = (alignment == KarmaHandler.EnumPlayerAlignments.NEUTRAL && new Random().nextInt(25) <= 25) ?
        						p.getEquipment().getArmorContents()[new Random().nextInt(
        						p.getEquipment().getArmorContents().length)] : null;
        boolean skipWeapon = new Random().nextInt(100) <= 50 && alignment == KarmaHandler.EnumPlayerAlignments.NEUTRAL;

        System.out.println(p.getName() + " DIED " + alignment.name());
        
        if (alignment != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
            int durabilityLoss = (int) (ItemGear.MAX_DURABILITY * 0.3); // 30%
            
            //  KEEP ARMOR  //
            for(ItemStack item : p.getEquipment().getArmorContents()) {
            	if (dontSave == null || !dontSave.equals(item)) {
            		ItemGear gear = (ItemGear)PersistentItem.constructItem(item);
            		gear.damageItem(p, durabilityLoss);
            		gearToSave.add(gear.generateItem());
            	}
            }
            
            p.getEquipment().setArmorContents(new ItemStack[4]);
            
            //  KEEP PROFESSION ITEMS AND MAIN WEAPON  //
            new ArrayList<>(event.getDrops()).forEach(is -> {
            	if(ProfessionItem.isProfessionItem(is) ||
            			(!skipWeapon && is.equals(p.getInventory().getItem(p.getInventory().getHeldItemSlot())))) {
            		ItemGear gear = (ItemGear)PersistentItem.constructItem(is);
            		gear.damageItem(p, durabilityLoss);
            		gearToSave.add(gear.generateItem());
            	}
            });
            
            //  KEEP SOULBOUND ITEMS  //
            event.getDrops().stream().filter(ItemManager::isItemSoulbound).forEach(gearToSave::add);

        } else {
            //  REMOVE SOULBOUND ITEMS  //
        	Lists.newArrayList(event.getDrops()).stream().filter(ItemManager::isItemSoulbound)
        			.forEach(event.getDrops()::remove);
        }

        Inventory mountInventory = MountUtils.inventories.get(p.getUniqueId());
        if (MountUtils.inventories.containsKey(p.getUniqueId()) && mountInventory != null) {
            for (ItemStack stack : mountInventory.getContents()) {
                mountInventory.remove(stack);
                event.getDrops().add(stack);
            }
            if (EntityAPI.hasMountOut(p.getUniqueId())) {
                net.minecraft.server.v1_9_R2.Entity entity = EntityAPI.getPlayerMount(p.getUniqueId());
                if (entity.getBukkitEntity() instanceof Horse) {
                    Horse horse = (Horse) entity.getBukkitEntity();
                    if (horse.getVariant() == Variant.MULE) {
                        horse.remove();
                        EntityAPI.removePlayerMountList(p.getUniqueId());
                    }
                }
            }
        }
        
        //  DONT DROP SAVED ITEMS  //
        gearToSave.forEach(event.getDrops()::remove);
        
        //  DONT DROP UNDROPPABLE ITEMS  //
        new ArrayList<>(event.getDrops()).stream().filter(i -> !ItemManager.isItemDroppable(i))
        		.forEach(event.getDrops()::remove);
        
        //  DROP ITEMS  //
        for (ItemStack stack : event.getDrops())
            if (stack != null && stack.getType() != Material.SKULL_ITEM)
            	event.getEntity().getWorld().dropItemNaturally(deathLocation, stack);

        Location respawnLocation;
        if (alignment == KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
            respawnLocation = KarmaHandler.CHAOTIC_RESPAWNS.get(new Random().nextInt(KarmaHandler.CHAOTIC_RESPAWNS.size()));
        } else if (DungeonRealms.getInstance().isEventShard) {
            respawnLocation = TeleportLocation.EVENT_AREA.getLocation();
        } else {
            Graveyard closest = GraveyardMechanic.get().getClosestGraveyard(p.getLocation());
            respawnLocation = closest != null ? closest.getLocation() : TeleportLocation.CYRENNICA.getLocation();

            if (closest != null) {
                if (!closest.getName().equalsIgnoreCase("cyrennica")) {
                    p.sendMessage("");
                    p.sendMessage(ChatColor.RED + ChatColor.ITALIC.toString() + "You regain consciousness in the graveyard of " + closest.getName() + "...");
                    p.sendMessage("");
                }
            }
        }
        
        for (PotionEffect potionEffect : p.getActivePotionEffects())
            p.removePotionEffect(potionEffect.getType());
        
        p.setCanPickupItems(false);
        p.setHealth(20);
        event.getDrops().clear();
        p.setGameMode(GameMode.SPECTATOR);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1));
        p.teleport(respawnLocation);
        p.setFireTicks(0);
        p.setFallDistance(0);
        EntityMechanics.setVelocity(p, p.getVelocity().zero());
        
        p.updateInventory();
        
        //This needs a slight delay otherwise it gets wiped. Don't delay it too much, or people who logout will get wiped.	
        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
        	PlayerManager.checkInventory(p);

            for (ItemStack stack : gearToSave)
                p.getInventory().addItem(stack);

            ItemManager.giveStarter(p);
        });
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            p.setCanPickupItems(true);
            p.setGameMode(GameMode.SURVIVAL);
            GamePlayer gamePlayer = GameAPI.getGamePlayer(p);
            if (gamePlayer != null)
                gamePlayer.getAttributes().clear();
        }, 20L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        PlayerManager.checkInventory(event.getPlayer());
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
        if (!ItemWeapon.isWeapon(leShooter.getEquipment().getItemInMainHand()) && entityType != EntityType.WITCH)
            return;
        
        DRMonster monster = (DRMonster)((CraftLivingEntity)leShooter).getHandle();
        MetadataUtils.registerProjectileMetadata(monster.getAttributes(), monster.getTier(), event.getEntity());
    }

    @EventHandler
    public void onWitchSplashPotion(PotionSplashEvent event) {
        if (!(event.getPotion().getShooter() instanceof LivingEntity)) return;
        LivingEntity leShooter = (LivingEntity) event.getPotion().getShooter();
        if (!(leShooter.getType() == EntityType.WITCH)) return;

        for (LivingEntity le : event.getAffectedEntities()) {
        	if (!GameAPI.isPlayer(le) || GameAPI.isInSafeRegion(le.getLocation()))
        		continue;
        	AttackResult res = new AttackResult(leShooter, le, event.getPotion());
        	DamageAPI.calculateWeaponDamage(res, false);
        	DamageAPI.applyArmorReduction(res, true);
            HealthHandler.damagePlayer(res);
        }
    }
    
    /**
     * Listens for ranged weapon usage.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerUseRangedWeapon(PlayerInteractEvent event) {
    	if (!event.hasItem())
            return;
        
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        ItemStack item = event.getPlayer().getEquipment().getItemInMainHand();
        if (!ItemWeaponRanged.isRangedWeapon(item))
        	return;
        
        ItemWeaponRanged weapon = (ItemWeaponRanged)PersistentItem.constructItem(item);
        
        Player player = event.getPlayer();
        
        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);
        
        //  DISABLE SHOOTING WHILE RIDING SOMETHING  //
        if (player.isInsideVehicle())
            return;
        
        //  CHECK DELAY  //
        String delayMeta = "last" + weapon.getItemType().getNBT() + "Shoot";
        if (player.hasMetadata(delayMeta) && System.currentTimeMillis() - player.getMetadata(delayMeta).get(0).asLong() < weapon.getShootDelay())
        	return;
        
        //  PREVENT SHOOTING IN SAFE ZONES  //
        boolean inDuel = false;
        if (GameAPI.isInSafeRegion(player.getLocation())) {
            inDuel = DuelingMechanics.isDueling(player.getUniqueId());
            if (!inDuel) {
                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1F, 1.25F);
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.MAGIC_CRIT, player.getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 20);
                player.setMetadata(delayMeta, new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                return;
            }
        }
        
        //  LEVEL RESTRICTIONS  //
        if (!RestrictionListener.canPlayerUseTier(player, weapon.getTier())) {
        	player.sendMessage(ChatColor.RED + "You must to be " + ChatColor.UNDERLINE + "at least" + ChatColor.RED + " level "
                    + weapon.getTier().getLevelRequirement() + " to use this weapon.");
            EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), 1F);
            return;
        }
        
        //  OUT OF ENERGY  //
        if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING) || EnergyHandler.getPlayerCurrentEnergy(player) <= 0) {
            event.getPlayer().playSound(player.getLocation(), Sound.ENTITY_WOLF_PANT, 12F, 1.5F);
            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, player.getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 40);
            return;
        }
        
        //  LAUNCH PROJECTILE  //
        player.setMetadata(delayMeta, new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        weapon.fireProjectile(player, !inDuel);
        player.playSound(player.getLocation(), weapon.getShootSound(), 1f, 1f);
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
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, armorContents, true);
            } else {
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, new ArrayList<String>(), true);
            }
            if (!combatLogger.getItemsToSave().isEmpty()) {
                Inventory inventory = Bukkit.createInventory(null, 27, "LoggerInventory");
                combatLogger.getItemsToSave().forEach(inventory::addItem);
                itemsToSave = ItemSerialization.toString(inventory);
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, itemsToSave, true);
            } else {
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, "", true);
            }
            combatLogger.handleNPCDeath();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onMiscDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player)
            return;

        if (event.getCause() == DamageCause.VOID || event.getCause() == DamageCause.SUFFOCATION) {

            if (event.getEntity().hasMetadata("boss")) {
                if (event.getEntity() instanceof CraftLivingEntity) {
                    DungeonBoss b = (DungeonBoss) ((CraftLivingEntity) event.getEntity()).getHandle();
                    if (b.getEnumBoss() == EnumDungeonBoss.InfernalGhast) {
                        //Dont suffocate the ghast, teleport him to the middle.
                        event.getEntity().teleport(new Location(event.getEntity().getWorld(), -53, 170, 660));
                        return;
                    }
                }
                return;
            }
            //Dont even despawn the boss.. or elites
            if (event.getEntity().hasMetadata("elite")) return;

            Bukkit.getLogger().info("Removing entity " + event.getEntity().getType() + " at " + event.getEntity().getLocation().toString() + " inside: " + event.getEntity().getLocation().getBlock().getType().name());
            event.getEntity().remove();
            return;
        } else if (event.getCause() == DamageCause.POISON) {
            //Endermen and witches immune to poison?
            if (((CraftEntity) event.getEntity()).getHandle() instanceof DRWitch || ((CraftEntity) event.getEntity()).getHandle() instanceof DREnderman) {
                event.setCancelled(true);
                event.setDamage(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityHurtByNonCombat(EntityDamageEvent event) {
    	//  ONLY APPLIES TO PLAYERS AND MOBS  //
        if (!(event.getEntity() instanceof Player) && !EnumEntityType.HOSTILE_MOB.isType(event.getEntity()))
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
        
        boolean isPlayer = GameAPI.isPlayer(event.getEntity());
        
        GamePlayer gp = isPlayer ? GameAPI.getGamePlayer((Player)event.getEntity()) : null;
        LivingEntity ent = (LivingEntity)event.getEntity();
        
        int maxHP = isPlayer ? gp.getMaxHP() : HealthHandler.getMonsterHP(ent);
        
        if (GameAPI.isInSafeRegion(event.getEntity().getLocation())) {
            event.setDamage(0);
            event.setCancelled(true);
            return;
        }
        
        DamageType type = DamageType.getByReason(event.getCause());
        AttackResult res = new AttackResult(null, (LivingEntity)event.getEntity());
        
        if (isPlayer || type.doesAffectMobs()) {
        	switch (event.getCause()) {
            	case FALL:
            	float blocks = ent.isInsideVehicle() ? event.getEntity().getVehicle().getFallDistance() : event.getEntity().getFallDistance();
            	
            	//OB Algoritm
            	if (blocks >= (ent.isInsideVehicle() ? 6 : 2))
            		dmg = maxHP * 0.02D * dmg;
            	Player p = (Player) event.getEntity();
            	
            	//  PREVENT DYING  //
            	if (dmg >= gp.getHP())
            		dmg = gp.getHP() - 1;
            	
            	if (blocks >= 49 && dmg <= gp.getHP())
            		Achievements.getInstance().giveAchievement(p.getUniqueId(), Achievements.EnumAchievements.LEAP_OF_FAITH);
            	break;
            	
            	case WITHER:
            		dmg = type.getOptional();
            		break;
                
            	case FIRE_TICK:
            	case LAVA:
            	case FIRE:
            		dmg = ent.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE) ? 0 : maxHP * type.getOptional();
                	break;
                
            	case DROWNING:
            	case POISON:
            	case CONTACT:
            		dmg = maxHP * type.getOptional();
                	break;
            	case SUFFOCATION:
            	case VOID: //Happens when exiting realms.
            	default:
            		return;
        	}
        } else {
        	dmg = 0;
        }
        
        if (isPlayer)
        	gp.updateWeapon();
        
        AttributeList attributes = res.getDefender().getAttributes();

        if (attributes != null) {
            //  APPLY ELEMENTAL RESISTANCE  //
            ElementalAttribute element = null;
            for(ElementalAttribute ea : ElementalAttribute.values())
            	for (DamageCause dc : ea.getDamageCauses())
            		if (dc == event.getCause())
            			element = ea;

            if (element != null) {
                double resistanceToApply = Math.min(attributes.getAttribute(element.getResist()).getValue(), 75);
                dmg *=  (100D - resistanceToApply) / 100D;
            }
        }
        
        res.setDamage(dmg);
        HealthHandler.damageEntity(res);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityCombust(EntityCombustEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
            event.setCancelled(true);

        //Wont auto teleport?
        if (event.getTo().getWorld() != event.getFrom().getWorld() || event.getTo().distance(event.getFrom()) > 100) {
        	
            List<Player> spectators = GameAPI.getNearbyPlayers(event.getPlayer().getLocation(), 1).stream().filter((pl) -> pl.getGameMode() == GameMode.SPECTATOR && Rank.isTrialGM(pl) && pl.getSpectatorTarget() != null && pl.getSpectatorTarget().equals(event.getPlayer())).collect(Collectors.toList());
            spectators.forEach((pl) -> {
                pl.setSpectatorTarget(null);
                pl.teleport(event.getTo());
            });
            
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                spectators.forEach((p) -> {
                    //Why so buggy..
                    p.teleport(event.getPlayer());
                    p.setSpectatorTarget(null);
                    p.sendMessage(ChatColor.RED + "Teleporting to " + event.getPlayer().getName());
                    p.setSpectatorTarget(event.getPlayer());
                });
            }, 20);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFireballHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof LargeFireball) {
            LivingEntity shooter = (LivingEntity) event.getEntity().getShooter();
            if (shooter instanceof Ghast) {
                for (Entity ent : event.getEntity().getNearbyEntities(4, 4, 4)) {
                    if (GameAPI.isPlayer(ent) && !GameAPI.isInSafeRegion(ent.getLocation())) {
                    	AttackResult res = new AttackResult(shooter, (LivingEntity)ent, event.getEntity());
                    	DamageAPI.calculateWeaponDamage(res, false);
                    	DamageAPI.applyArmorReduction(res, true);
                    	HealthHandler.damagePlayer(res);
                    }
                }
                
                //TODO: Move this code into the infernal listener (Once it exists), and make this less terrible.
                if (new Random().nextInt(10) == 0) {
                    // 10% chance of adds on explosion.
                    Location hit_loc = event.getEntity().getLocation();
                    World world = ((CraftWorld) event.getEntity().getWorld()).getHandle();
                    for (int i = 0; i <= 3; i++) {
                        net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, 2, EnumMonster.MagmaCube);
                        int level = Utils.getRandomFromTier(2, "low");
                        String newLevelName = org.bukkit.ChatColor.AQUA + "[Lvl. " + level + "] ";
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
                        String newLevelName = org.bukkit.ChatColor.AQUA + "[Lvl. " + level + "] ";
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