package net.dungeonrealms.game.world.item;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerToggles.Toggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.handler.EnergyHandler;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.*;
import net.dungeonrealms.game.item.items.core.setbonus.SetBonus;
import net.dungeonrealms.game.item.items.core.setbonus.SetBonuses;
import net.dungeonrealms.game.listener.combat.AttackResult;
import net.dungeonrealms.game.listener.combat.AttackResult.CombatEntity;
import net.dungeonrealms.game.listener.combat.DamageResultType;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.PowerMove;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.Item.ElementalAttribute;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import net.minecraft.server.v1_9_R2.EntityArrow;
import net.minecraft.server.v1_9_R2.MathHelper;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Kieran on 9/21/2015.
 */
public class DamageAPI {

    public static HashMap<Player, HashMap<ArmorStand, BukkitTask>> DAMAGE_HOLOGRAMS = new HashMap<>();

    public static void calculateWeaponDamage(AttackResult res, boolean removeDurability) {
        CombatEntity attacker = res.getAttacker();
        CombatEntity defender = res.getDefender();

        ItemStack item = attacker.getEntity().getEquipment().getItemInMainHand();
        if (!ItemWeapon.isWeapon(item))
            return;

        if (attacker.getAttributes() == null) {
            if (attacker.isPlayer()) {
                Bukkit.getLogger().info("Null attacker attributes for " + attacker.getPlayer().getName());
            }
            return;
        }

        ItemWeapon weapon = (ItemWeapon) PersistentItem.constructItem(item);
        int weaponTier = weapon.getTier().getId();
        //  BASE DAMAGE  //
        double damage = attacker.getAttributes().getAttribute(WeaponAttributeType.DAMAGE).getValueInRange();
        int critHit = 0;

        if (!res.hasProjectile()) {
            //  MELEE WEAPON  //
            if (attacker.isPlayer()) {

                //  DAMAGE WEAPON  //
                if (removeDurability) {
                    int durabilityLoss = 1;

                    if (!defender.isPlayer()) { //ONLY FOR MOBS!
                        //  EXTRA DAMAGE FOR TIER GAPS  //
                        int mobTier = EntityAPI.getTier(defender.getEntity());

                        int tierDif = weaponTier - mobTier;
                        if (tierDif > 1)
                            durabilityLoss = 2 * (tierDif - 1);
                    }

                    weapon.damageItem(attacker.getPlayer(), durabilityLoss);
                }

                PlayerWrapper.getWrapper(attacker.getPlayer()).updateWeapon();

                //  STAT BONUS  //
                ItemType type = weapon.getItemType();


                if (type == ItemType.AXE) {
                    critHit += 3;
                    int strValue = attacker.getAttributes().getAttribute(ArmorAttributeType.STRENGTH).getValue();
                    damage = damage * (1 + (strValue * 0.0001));
                } else if (type == ItemType.SWORD) {
                    int vitValue = attacker.getAttributes().getAttribute(ArmorAttributeType.VITALITY).getValue();
                    damage = damage * (1 + (vitValue * 0.0001));
                } else if (type == ItemType.POLEARM) {
                    int strValue = attacker.getAttributes().getAttribute(ArmorAttributeType.STRENGTH).getValue();
                    damage = damage * (1 + (strValue * 0.0002));
                }
            }
        } else if (attacker.isPlayer()) {
            ItemType type = weapon.getItemType();
            if (type == ItemType.STAFF) {
                int intValue = attacker.getAttributes().getAttribute(ArmorAttributeType.INTELLECT).getValue();
                damage = damage * (1 + (intValue * 0.0002));
            } else if (type == ItemType.BOW) {
                int dexValue = attacker.getAttributes().getAttribute(ArmorAttributeType.DEXTERITY).getValue();
                damage = damage * (1 + (dexValue * 0.0001));
            }
        }

        //  CRIT  //
        critHit += attacker.getAttributes().getAttribute(WeaponAttributeType.CRITICAL_HIT).getValue();
        boolean isHitCrit = false;


        //  VS MONSTERS AND PLAYERS  //
        WeaponAttributeType vsEntity = defender.isPlayer() ? WeaponAttributeType.VS_PLAYER : WeaponAttributeType.VS_MONSTERS;
        damage += ((double) attacker.getAttributes().getAttribute(vsEntity).getValue() / 100) * damage;

        //  EXECUTE ATTACK HOOK  //
        if (defender.isPlayer() && EnumEntityType.HOSTILE_MOB.isType(attacker.getEntity()) && EntityAPI.isMonster(attacker.getEntity()))
            EntityAPI.getMonster(attacker.getEntity()).onMonsterAttack(defender.getPlayer());

        //  DPS  //
        damage += damage * ((double) attacker.getAttributes().getAttribute(ArmorAttributeType.DAMAGE).getValueInRange() / 100);
        //  KNOCKBACK  //
        if (attacker.isPlayer() && getChance(attacker.getAttributes(), WeaponAttributeType.KNOCKBACK))
            knockbackEntity(attacker.getPlayer(), defender.getEntity(), 1.5);

        //  BLIND  //
        if (getChance(attacker.getAttributes(), WeaponAttributeType.BLIND))
            applyBlind(defender.getEntity(), weaponTier);

        //  SLOW  //
        if (getChance(attacker.getAttributes(), WeaponAttributeType.SLOW))
            applySlow(defender.getEntity());

        //  ELEMENTAL DAMAGE  //
        if (attacker.isPlayer()) {
            if (defender.isPlayer()) {
                defender.getEntity().setMetadata("lastPlayerToDamageExpire", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + 3000));
                defender.getEntity().setMetadata("lastPlayerToDamage", new FixedMetadataValue(DungeonRealms.getInstance(), attacker.getEntity().getName()));
            }

            for (ElementalAttribute ea : ElementalAttribute.values()) {
                if (attacker.getAttributes().hasAttribute(ea.getAttack())) {
                    applyDebuff(defender.getEntity(), ea, weaponTier);
                    //Ele Resist for Vit
                    double damageBoost = attacker.getAttributes().getAttribute(ea.getAttack()).getValue();

                    int attackerIntValue = attacker.getAttributes().getAttribute(ArmorAttributeType.INTELLECT).getValue();

                    damageBoost = damageBoost * (1 + (attackerIntValue * 0.0005));
                    int defenderVitValue = defender.getAttributes().getAttribute(ArmorAttributeType.VITALITY).getValue();
                    double damageBoostReduction = damageBoost * (defenderVitValue * 0.0004);
                    damageBoost -= damageBoostReduction;

                    damage += damageBoost;

//                    res.setElementalDamage(damageBoost);
                    //damage += attacker.getAttributes().getAttribute(ea.getAttack()).getValue();
                }
            }

        } else if (EntityAPI.isElemental(attacker.getEntity())) {
            applyDebuff(defender.getEntity(), EntityAPI.getElement(attacker.getEntity()), weaponTier);
        }

        //  CRIT CHANCE  //
        if (ThreadLocalRandom.current().nextInt(100) < critHit) {
            defender.getEntity().getWorld().spawnParticle(Particle.CRIT, defender.getEntity().getLocation(), 10,
                    ThreadLocalRandom.current().nextDouble(), ThreadLocalRandom.current().nextDouble(), ThreadLocalRandom.current().nextDouble(), 0.5F);
            isHitCrit = true;
        }

        //  LIFESTEAL  //

        if (attacker.isPlayer()) {
            double hpPercent = -1;
            int bonusAmount = SetBonus.hasSetBonus(attacker.getPlayer(), SetBonuses.BLOOD_BUTCHER) && (hpPercent = HealthHandler.getHPPercent(attacker.getPlayer())) <= .2 ? 20 : 0;
            if (attacker.getAttributes().hasAttribute(WeaponAttributeType.LIFE_STEAL) || bonusAmount > 0) {
                double lifeToHeal = (double) (attacker.getAttributes().getAttribute(WeaponAttributeType.LIFE_STEAL).getValue() + bonusAmount) / 100 * damage;
                HealthHandler.heal(attacker.getPlayer(), (int) lifeToHeal + 1, true);
            }
        }
        //  STRENGTH BUFF  //
        damage = applyIncreaseDamagePotion(attacker.getEntity(), damage);

        //  ADD DAMAGE BONUS  //
        if (attacker.getEntity().hasMetadata("damageBonus"))
            damage += (damage * (attacker.getEntity().getMetadata("damageBonus").get(0).asDouble() / 100.));

        //  ADD ELITE BONUS  //
        damage = addSpecialDamage(attacker.getEntity(), damage);

        //  LEVEL DAMAGE  //
        if (defender.isPlayer() && !attacker.isPlayer()) {
            // add 5% damage per level difference
            int attackerLevel = EntityAPI.getLevel(attacker.getEntity());
            int defenderLevel = PlayerWrapper.getWrapper(defender.getPlayer()).getLevel();
            if (attackerLevel > defenderLevel)
                damage = addLevelDamage(attackerLevel, defenderLevel, damage);
        }

        //  CRITICAL HIT  //
        if (isHitCrit) {
            if (attacker.isPlayer()) {
                attacker.getWrapper().sendDebug(ChatColor.YELLOW + "" + ChatColor.BOLD + "                        *CRIT*");
                defender.getEntity().getWorld().playSound(attacker.getEntity().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.5F, 0.5F);
            }
            double critIncrease = 0.0;
            if (attacker.isPlayer()) {
                int int_val = attacker.getWrapper().getAttributes().getAttribute(ArmorAttributeType.INTELLECT).getValue();
                critIncrease = int_val * 0.0003;
            }
            damage *= (2 + critIncrease);

        }
        //  DAMAGE CAP  //
        if (!attacker.isPlayer())
            damage = Math.min(damage, weaponTier * 600);

        //Armor Reduction.
        ItemType type = weapon.getItemType();
        boolean isBlocking = attacker.isPlayer() && attacker.getPlayer().isBlocking();
        Integer reductionPercent = null;
        if (type.equals(ItemType.AXE) || type.equals(ItemType.SWORD) || type.equals(ItemType.POLEARM)) {
            reductionPercent = attacker.getAttributes().getAttribute(ArmorAttributeType.MELEE_ABSORBTION).getValue();
        } else if (type.equals(ItemType.BOW)) {
            reductionPercent = attacker.getAttributes().getAttribute(ArmorAttributeType.RANGE_ABSORBTION).getValue();
        } else if (type.equals(ItemType.STAFF)) {
            reductionPercent = attacker.getAttributes().getAttribute(ArmorAttributeType.MAGE_ABSORBTION).getValue();
        }

        if (reductionPercent != null) {
            if (!isBlocking) reductionPercent /= 2;
            double damageReduction = reductionPercent / 100;
            damage = damage * (1 - damageReduction);
        }


        res.setDamage(damage);
        return;
    }


    private static boolean getChance(AttributeList al, AttributeType at) {
        return al.hasAttribute(at) && ThreadLocalRandom.current().nextInt(100) < al.getAttribute(at).getValue();
    }

    private static void applyDebuff(LivingEntity defender, ElementalAttribute ea, int tier) {
        if (ea == ElementalAttribute.PURE)
            return;

        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
            defender.getWorld().playSound(defender.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1F, 1F);

            if (ea == ElementalAttribute.FIRE) {
                ParticleAPI.spawnParticle(Particle.FLAME, defender.getLocation(), 10, 0.5F);
                ParticleAPI.spawnParticle(Particle.SPELL, defender.getLocation(), 10, 1F);

                final int[] FIRE_TICKS = new int[]{15, 25, 30, 35, 40};
                defender.setFireTicks(FIRE_TICKS[tier - 1]);
            } else {
                defender.getWorld().playSound(defender.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1F, 1F);
                defender.getWorld().playEffect(defender.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8228);

                final int[] POTION_TICKS = new int[]{30, 40, 50, 40, 50};
                defender.addPotionEffect(new PotionEffect(ea.getAttackPotion(), POTION_TICKS[tier - 1], tier / 4));
            }
        }, 1);
    }

    public static void applySlow(LivingEntity defender) {
        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
            int tickLength = EntityAPI.getTier(defender) >= 4 ? 100 : 40;
            defender.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, tickLength, 1));
        });
    }

    public static void applyBlind(LivingEntity defender, int weaponTier) {
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
            int tickDelay = Math.min((weaponTier + 2) * 10, 60);
            defender.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, tickDelay, 1));
        }, 1);
    }

    //This makes mobs stronger when they hit you IF they are higher than you. Not if you are higher than them...
    private static double addLevelDamage(int attackerLevel, int defenderLevel, double damage) {
        int difference = attackerLevel - defenderLevel;
        if (difference > 10)
            difference = 10;
        return damage * (1 + (difference * 0.09));
    }

    public static void handlePolearmAOE(EntityDamageByEntityEvent event, double damage, LivingEntity damager) {
        handlePolearmAOE(event, damage, damager, false);
    }

    public static void handlePolearmAOE(EntityDamageByEntityEvent event, double damage, LivingEntity damager, boolean forcePole) {
        ItemStack held = damager.getEquipment().getItemInMainHand();

        if (!ItemWeaponPolearm.isPolearm(held) && !forcePole)
            return;

        boolean damagerIsMob = !(damager instanceof Player);
        int hitCount = 0;

        List<Entity> ents = event.getEntity().getNearbyEntities(2.5, 3, 2.5);

        PlayerWrapper attacker = null;
        if (!damagerIsMob)
            attacker = PlayerWrapper.getPlayerWrapper((Player) damager);

        float energyCostPerSwing = forcePole ? 0 : EnergyHandler.getWeaponSwingEnergyCost(held);
        for (Entity entity : ents) {
            //  ARE WE AN ALLOWED ENTITY  //
            if (!(entity instanceof LivingEntity) || (damagerIsMob && !GameAPI.isPlayer(entity)) || entity.isInvulnerable())
                continue;
            //  NO DAMAGE IN SAFE ZONES  //
            if (GameAPI.isInSafeRegion(event.getEntity().getLocation()) || GameAPI.isInSafeRegion(damager.getLocation()))
                continue;
            //  DONT DAMAGE OURSELVES  //
            if (entity.equals(damager))
                continue;

            //He has pvp toggled off so dont do damage to players with polearm.
            if (attacker != null) {
                if (!attacker.getToggles().getState(Toggles.PVP)) {
                    if (entity instanceof Player) continue;
                }
            }

            float totalEnergyCost = (float) (energyCostPerSwing * .15);
            //Cant do anymore damage.
            if (!damagerIsMob && EnergyHandler.getPlayerCurrentEnergy((Player) damager) < totalEnergyCost) break;

            AttackResult res = new AttackResult(damager, (LivingEntity) entity);
            res.setDamage(damage);
            applyArmorReduction(res, true);

            if (!entity.equals(event.getEntity()) && !res.getDefender().isPlayer()) {
                //  DAMAGING HOSTILE MOB  //
                if (!EnumEntityType.HOSTILE_MOB.isType(entity))
                    continue;

                if (!damagerIsMob)
                    EnergyHandler.removeEnergyFromPlayerAndUpdate((Player) damager, totalEnergyCost);
                HealthHandler.damageMonster(res);
            } else if (res.getDefender().isPlayer()) {
                if (damagerIsMob || !GameAPI.isNonPvPRegion(entity.getLocation())) {
                    if (GameAPI._hiddenPlayers.contains((Player) entity))
                        continue;
                    if (!damagerIsMob && !DuelingMechanics.isDuelPartner(damager.getUniqueId(), entity.getUniqueId())) {
                        if (!attacker.getToggles().getState(Toggles.PVP)) {
                            attacker.sendDebug(ChatColor.YELLOW + "You have toggle PvP disabled. You currently cannot attack players.");
                            continue;
                        }
                        //  IGNORE PARTIES  //
                        if (Affair.areInSameParty((Player) damager, (Player) entity))
                            continue;

                        //  IGNORE GUILDS  //
                        if (GuildDatabase.getAPI().areInSameGuild((Player) damager, res.getDefender().getPlayer()))
                            continue;
                    }
                    if (!damagerIsMob)
                        EnergyHandler.removeEnergyFromPlayerAndUpdate((Player) damager, totalEnergyCost);
                    HealthHandler.damagePlayer(res);
                }
            }

            hitCount++;

//            if (hitCounter > 0) {
//                if (hitCounter > 2) hitCounter--;
//
//                float totalEnergyCost = (float) ((energyCostPerSwing * .25) * (hitCounter));
//                EnergyHandler.removeEnergyFromPlayerAndUpdate(damager.getUniqueId(), totalEnergyCost);
//            }
        }

//        if (hitCount > 0) {
//            if (hitCount > 2)
//                hitCount--;
//
//            if (!damagerIsMob)
//                EnergyHandler.removeEnergyFromPlayerAndUpdate(damager.getUniqueId(),
//                        (EnergyHandler.getSwingCost(held) * hitCount) / 4F);
//        }
    }

    /**
     * Adds extra damage for dungeon, elite, and boss mobs.
     *
     * @param attacker
     * @param damage
     * @return
     */
    public static double addSpecialDamage(LivingEntity attacker, double damage) {
        if (PowerMove.doingPowerMove(attacker.getUniqueId()))
            return damage;

        //  ELITE DMG BOOST  //
        if (EntityAPI.isElite(attacker)) {
            int tier = EntityAPI.getTier(attacker);
            return damage * (tier <= 2 ? 2.5f : (3 + (tier - 3) * 2));
        } else if (EntityAPI.isBoss(attacker)) {
            return damage * 3;
        }
        return damage;
    }

    public static double applyIncreaseDamagePotion(LivingEntity attacker, double damage) {
        if (attacker.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            int potionTier = 0;
            for (PotionEffect potionEffect : attacker.getActivePotionEffects()) {
                if (potionEffect.getType() == PotionEffectType.INCREASE_DAMAGE) {
                    potionTier = potionEffect.getAmplifier();
                    break;
                }
            }

            if (potionTier > 2)
                potionTier = 2;

            damage *= (1.1 + (0.2 * potionTier));
        }
        return damage;
    }

    public static void applyArmorReduction(AttackResult res, boolean takeDura) {

        CombatEntity attacker = res.getAttacker();
        CombatEntity defender = res.getDefender();
        double originalDamage = res.getDamage();
        double damage = res.getDamage();

        double totalArmor = 0;
        double totalArmorReduction = 0;

        //  DAMAGE ARMOR  //
        if (defender.isPlayer())
            if (takeDura)
                for (ItemStack armor : defender.getPlayer().getEquipment().getArmorContents()) {
                    if (armor == null || armor.getType() == Material.AIR || !ItemArmor.isArmor(armor)) continue;
                    new ItemArmor(armor).damageItem(defender.getPlayer(), 1);
                }
        if (defender.getAttributes() == null || attacker.getAttributes() == null) {
            res.setDamage(1);
            //How?
            return;
        }

        int accuracy = res.hasProjectile() ? 0 : attacker.getAttributes().getAttribute(WeaponAttributeType.ACCURACY).getValue();

        //  BLOCK AND DODGE  //
        Random rand = ThreadLocalRandom.current();

        int dodgeChance = defender.getAttributes().getAttribute(ArmorAttributeType.DODGE).getValue();
        int blockChance = defender.getAttributes().getAttribute(ArmorAttributeType.BLOCK).getValue();
        final int dodgeRoll = rand.nextInt(100);
        final int blockRoll = rand.nextInt(100);

        if (dodgeRoll < dodgeChance - accuracy) {
            if (dodgeRoll >= dodgeChance) {
                attacker.getWrapper().sendDebug(ChatColor.GREEN + "Your " + accuracy + "% accuracy has prevented " +
                        defender.getEntity().getCustomName() + ChatColor.GREEN + " from dodging.");
            }
            removeElementalEffects(defender.getEntity());
            ParticleAPI.spawnParticle(Particle.CLOUD, defender.getEntity().getLocation(), 10, .5F);
            res.setResult(DamageResultType.DODGE);
            return;
        } else if (blockRoll < blockChance - accuracy) {
            if (blockRoll >= blockChance) {
                attacker.getWrapper().sendDebug(ChatColor.GREEN + "Your " + accuracy + "% accuracy has prevented " +
                        defender.getEntity().getCustomName() + ChatColor.GREEN + " from blocking.");
            }
            removeElementalEffects(defender.getEntity());
            ParticleAPI.spawnParticle(Particle.REDSTONE, defender.getEntity().getLocation(), 10, .5F);
            res.setResult(DamageResultType.BLOCK);
            return;
        }

        //  REFLECT  //
        int reflectChance = defender.getAttributes().getAttribute(ArmorAttributeType.REFLECTION).getValue();
        if (rand.nextInt(100) < Math.min(75, reflectChance)) {
            res.setResult(DamageResultType.REFLECT);
            return;
        }

        //  BASE ARMOR  //
        totalArmor = Math.min(75, defender.getAttributes().getAttribute(ArmorAttributeType.ARMOR).getValueInRange());

        //  ARMOR PENETRATION  //
        ModifierRange range = attacker.getAttributes().getAttribute(WeaponAttributeType.ARMOR_PENETRATION);
        if (!res.hasProjectile() && range.getValue() > 0) {
            totalArmor -= range.getValue();
            totalArmor = Math.max(0, totalArmor);
        }

        //  THORNS  //
        ModifierRange mr = defender.getAttributes().getAttribute(ArmorAttributeType.THORNS);
        if (mr.getValue() != 0 && !res.hasProjectile()) { // Only applies to Melee
            //Just only deal damage, but dont cancel the current event.
            int damageFromThorns = Math.max(1, (int) Math.round(damage * (mr.getValue() / 100f)));
            //We need to swap this?
            ParticleAPI.spawnBlockParticles(attacker.getEntity().getLocation(), Material.LEAVES);
            AttackResult result = new AttackResult(defender.getEntity(), attacker.getEntity());
            result.setCause(EntityDamageEvent.DamageCause.THORNS);
            result.setDamage(damageFromThorns);
            HealthHandler.damageEntity(result);
        }

        //  ELEMENTAL DAMAGE  //
        double elementalDamage = 0;
        double elementalResistance = 0;

        if (attacker.isPlayer()) {
            for (ElementalAttribute ea : ElementalAttribute.values()) {
                //  ADD DAMAGE  //
                double eDamage = attacker.getAttributes().getAttribute(ea.getAttack()).getValue();
                if (res.hasProjectile()) {
                    if (res.getProjectile().hasMetadata(ea.getAttack().getNBTName()))
                        eDamage = res.getProjectile().getMetadata(ea.getAttack().getNBTName()).get(0).asInt();
                }
                //double damageBoost = attacker.getAttributes().getAttribute(ea.getAttack()).getValue();

                int attackerIntValue = attacker.getAttributes().getAttribute(ArmorAttributeType.INTELLECT).getValue();

                eDamage = eDamage * (1 + (attackerIntValue * 0.0005));
                int defenderVitValue = defender.getAttributes().getAttribute(ArmorAttributeType.VITALITY).getValue();
                double damageBoostReduction = eDamage * (defenderVitValue * 0.0004);
                eDamage -= damageBoostReduction;
                elementalDamage += eDamage;

                //  ADD RESISTANCE  //
                if (ea.getResist() != null) {
                    elementalResistance += Math.min(75, defender.getAttributes().getAttribute(ea.getResist()).getValue());

                }
            }
        } else if (EntityAPI.isElemental(attacker.getEntity())) {
            ElementalAttribute ea = EntityAPI.getElement(attacker.getEntity());

            if (ea == ElementalAttribute.PURE) {
                totalArmor = 0;
            } else {
                totalArmor *= 0.2;
                totalArmor += Math.min(75, defender.getAttributes().getAttribute(ea.getResist()).getValue());
            }
        }

        //  APPLY ELEMENTAL  //
        damage -= elementalDamage;
        damage *= (100 - Math.min(75, totalArmor)) / 100D;
        // elemental damage ignores 80% but add on resistance
        if (elementalDamage != 0) {
            damage += (0.8 * elementalDamage) * ((double) (100 - elementalResistance)) / 100d;
        }
        //  ARMOR BONUS  //
        if (defender.getEntity().hasMetadata("armorBonus"))
            totalArmor += (defender.getEntity().getMetadata("armorBonus").get(0).asFloat() / 100f) * totalArmor;

        totalArmorReduction = originalDamage - damage;

        //  POTION BUFF	  //
        if (defender.getEntity().hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            int potionTier = 1;
            for (PotionEffect pe : defender.getEntity().getActivePotionEffects()) {
                if (pe.getType() == PotionEffectType.DAMAGE_RESISTANCE) {
                    potionTier = pe.getAmplifier();
                    break;
                }
            }
            final double[] LEVEL_REDUCTION = new double[]{1, 1.05, 1.1, 1.2};
            if (potionTier < LEVEL_REDUCTION.length)
                totalArmorReduction *= LEVEL_REDUCTION[potionTier];
        }


        res.setDamage(Math.max(1, damage));
        res.setTotalArmor(totalArmor);
        res.setTotalArmorReduction(totalArmorReduction);
    }

    public static void fireStaffProjectile(Player player, ItemWeapon staff, boolean subtractDurability) {
        if (subtractDurability)
            staff.damageItem(player, 1);
        PlayerWrapper pw = PlayerWrapper.getWrapper(player);
        pw.calculateAllAttributes();
        EnergyHandler.removeEnergyFromPlayerAndUpdate(player, EnergyHandler.getWeaponSwingEnergyCost(staff.getItem()), !subtractDurability);
        fireStaffProjectile(player, pw.getAttributes(), null, staff);
    }

    public static Projectile fireStaffProjectile(LivingEntity attacker, @Nullable LivingEntity target, ItemWeaponStaff staff) {
        return fireStaffProjectile(attacker, staff.getAttributes(), target, staff);
    }

    public static Projectile fireStaffProjectile(LivingEntity attacker, ItemWeaponStaff staff) {
        return fireStaffProjectile(attacker, staff.getAttributes(), attacker instanceof Creature ? ((Creature) attacker).getTarget() : null, staff);
    }

    //TODO: Modularize.
    public static Projectile fireStaffProjectile(LivingEntity attacker, AttributeList attributes, @Nullable LivingEntity target, ItemWeapon staff) {
        double accuracy = !(attacker instanceof Player) ? 85 + Utils.randInt(staff.getTier().getTierId() * 3) : attributes.getAttribute(WeaponAttributeType.PRECISION).getValue();
        org.bukkit.util.Vector vector = null;

        if (target != null) {
            vector = target.getLocation().toVector().subtract(attacker.getLocation().toVector());
            if (vector.length() != 0) vector.normalize();
        }


        boolean kilitanStaff = "kilatan".equals(GameAPI.getCustomID(staff.getItem()));
        Projectile projectile = null;
        Item.ItemTier tier = staff.getTier();
        switch (tier) {
            case TIER_1:
                projectile = attacker.launchProjectile(Snowball.class);
                break;
            case TIER_2:
                projectile = EntityMechanics.spawnFireballProjectile(((CraftWorld) attacker.getWorld()).getHandle(), (CraftLivingEntity) attacker, vector, SmallFireball.class, accuracy);
                break;
            case TIER_3:
                projectile = attacker.launchProjectile(EnderPearl.class);
                break;
            case TIER_4:
                projectile = EntityMechanics.spawnFireballProjectile(((CraftWorld) attacker.getWorld()).getHandle(), (CraftLivingEntity) attacker, vector, WitherSkull.class, accuracy);
                break;
            case TIER_5:
                projectile = EntityMechanics.spawnFireballProjectile(((CraftWorld) attacker.getWorld()).getHandle(), (CraftLivingEntity) attacker, vector, kilitanStaff ? DragonFireball.class : LargeFireball.class, accuracy);
                break;
        }

        if (projectile instanceof Fireball) {
            ((Fireball) projectile).setYield(0);
            ((Fireball) projectile).setIsIncendiary(false);
        }

        if (vector != null) {
            //mob
            vector = vector.multiply(tier.getTierId() <= 3 ? 1.75 : tier == Item.ItemTier.TIER_5 ? 1.4 : 1);
        } else {
            //player shooting
            //1, 1.25, 1.25, 1.5, 3x velocities
            projectile.setVelocity(projectile.getVelocity().multiply(tier.getTierId() == 1 ? 1.25 : tier.getTierId() <= 3 ? 2 : tier == Item.ItemTier.TIER_4 ? 2.25 : tier == Item.ItemTier.TIER_5 ? 3 : 1));
        }

        if (projectile == null) return null;
        projectile.setBounce(false);
        projectile.setShooter(attacker);
        if (vector != null)
            EntityMechanics.setVelocity(projectile, vector);
        MetadataUtils.registerProjectileMetadata(attributes, staff.getTier().getId(), projectile);
        return projectile;
    }

    public static void fireBowProjectile(Player player, ItemWeaponBow bow, boolean takeDura) {
        if (takeDura)
            bow.damageItem(player, 1);
        PlayerWrapper.getWrapper(player).calculateAllAttributes();
        EnergyHandler.removeEnergyFromPlayerAndUpdate(player, EnergyHandler.getWeaponSwingEnergyCost(bow.getItem()), !takeDura);
        fireBowProjectile(player, bow);
    }

    public static void fireBowProjectile(LivingEntity ent, ItemWeaponBow bow) {

        Projectile projectile = null;

        for (AttributeType type : bow.getAttributes().keySet()) {
            ElementalAttribute ea = ElementalAttribute.getByAttribute(type);
            if (ea != null) {
                if (projectile == null) {
                    projectile = EntityMechanics.spawnFireballProjectile(((CraftWorld) ent.getWorld()).getHandle(), (CraftLivingEntity) ent, null, TippedArrow.class, 100D);
                    if (projectile != null)
                        ((TippedArrow) projectile).addCustomEffect(new PotionEffect(ea.getDefensePotion(), 0, 0), true);
                }
            }
        }

        if (projectile == null)
            projectile = EntityMechanics.spawnFireballProjectile(((CraftWorld) ent.getWorld()).getHandle(), (CraftLivingEntity) ent, null, Arrow.class, 100D);
//        projectile = ent.launchProjectile(Arrow.class);

        projectile.setBounce(false);
        projectile.setVelocity(projectile.getVelocity().multiply(1.15));
        projectile.setShooter(ent);
        ((CraftArrow) projectile).getHandle().fromPlayer = EntityArrow.PickupStatus.DISALLOWED;
        MetadataUtils.registerProjectileMetadata(bow.getAttributes(), bow.getTier().getId(), projectile);
    }

    public static void removeElementalEffects(LivingEntity ent) {
        for (ElementalAttribute ea : ElementalAttribute.values())
            if (ea.getAttackPotion() != null && ent.hasPotionEffect(ea.getAttackPotion()))
                ent.removePotionEffect(ea.getAttackPotion());

        //  FIRE  //
        if (ent.getFireTicks() > 0)
            ent.setFireTicks(0);
    }

    private static Map<UUID, HitTracker> hitTrackerMap = new HashMap<>();

    public static void knockbackPlayerPVP(Player attacker, Player damaged) {
        // Get velocity unit vector:
        org.bukkit.util.Vector unitVector = damaged.getLocation().toVector().subtract(attacker.getLocation().toVector());

        if (unitVector.length() > 0) unitVector.normalize();

//        Bukkit.getLogger().info("Damaged Velocity: " + damaged.getVelocity().toString());
//        Bukkit.getLogger().info("UnitVector Velocity: " + unitVector.toString());

        HitTracker tracker = hitTrackerMap.computeIfAbsent(attacker.getUniqueId(), t -> new HitTracker());

        int hitCounter = tracker.trackHit(damaged);
        unitVector.setY(damaged.getVelocity().getY() + (hitCounter <= 1 ? .25 : hitCounter == 2 ? .15 : 0.05));

//        Bukkit.getLogger().info("New Y: " + unitVector.getY() + " from " + hitCounter + " hits.");

        EntityMechanics.setVelocity(damaged, unitVector.multiply(hitCounter >= 2 ? .35 : .45));
    }

    public static void knockbackEntity(Entity p, Entity ent, double speed) {
        if (ent instanceof Horse || ent instanceof ArmorStand) {
            return;
        }
        // Get velocity unit vector:
        org.bukkit.util.Vector unitVector = ent.getLocation().toVector().subtract(p.getLocation().toVector());

        if (unitVector.length() == 0 && p instanceof Projectile) {
            //Recalc off direction?
            unitVector = p.getLocation().getDirection().normalize();
            Bukkit.getLogger().info("Recalcing vector due to projectile code.");
        }

        //Dont cause NaN.
        if (unitVector.length() != 0) unitVector.normalize();

        unitVector.setY(0.35);
        if (speed > 1) unitVector.setY(0.2);
        if (p.getVelocity().getY() > 0) unitVector.setY(0);
        // Set speed and push entity:

        EntityMechanics.setVelocity(ent, unitVector.multiply(speed));
    }

    public static void knockbackEntityVanilla(Entity entity, float strength, Location damagerLocation) {

        double d0 = damagerLocation.getX() - entity.getLocation().getX();
        double d1 = damagerLocation.getZ() - entity.getLocation().getZ();
        float f1 = MathHelper.sqrt(d0 * d0 + d1 * d1);
        org.bukkit.util.Vector vel = entity.getVelocity();
        vel.divide(new Vector(2, 1, 2));

        vel.subtract(new Vector(d0 / (double) f1 * (double) strength, 0, d1 / (double) f1 * (double) strength));
        if (entity.isOnGround()) {
            vel.divide(new Vector(1, 2, 1));
            vel.add(new Vector(0, strength, 0));
            if (vel.getY() > 0.4000000059604645D) {
                vel.setY(0.4000000059604645D);
            }
        }


        EntityMechanics.setVelocity(entity, vel);
    }


    @SuppressWarnings("deprecation")
    public static void newKnockbackEntity(Player p, Entity ent, double speed) {
        if (ent instanceof Horse) {
            return;
        }
        // Get velocity unit vector:
        org.bukkit.util.Vector unitVector = ent.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
        unitVector.setY(p.isOnGround() ? 0.35 : 0.2);
        if (speed > 1) unitVector.setY(0.2);
        if (p.getVelocity().getY() > 0) unitVector.setY(0);
        // Set speed and push entity:
        if (ent instanceof Player) {
            EntityMechanics.setVelocity((Player) ent, unitVector.multiply(speed));
            return;
        }
        ent.setVelocity(unitVector.multiply(speed));
    }

    public static boolean isStaffProjectile(Entity entity) {
        EntityType type = entity.getType();
        return type == EntityType.SNOWBALL || type == EntityType.SMALL_FIREBALL || type == EntityType.ENDER_PEARL ||
                type == EntityType.FIREBALL || type == EntityType.WITHER_SKULL || type == EntityType.DRAGON_FIREBALL;
    }

    public static boolean isBowProjectile(Entity entity) {
        EntityType type = entity.getType();
        return type == EntityType.ARROW || type == EntityType.TIPPED_ARROW;
    }

    /**
     * Sets a blanket damage bonus for a specified entity. When the entity
     * attacks, the damage bonus will be added on to the final calculated
     * damage.
     *
     * @param ent
     * @param bonusPercent - the bonus amount as a percentage. (e.g. 50.0 will
     *                     increase damage by 50%).
     */
    public static void setDamageBonus(Entity ent, float bonusPercent) {
        ent.setMetadata("damageBonus", new FixedMetadataValue(DungeonRealms.getInstance(), bonusPercent));
    }

    public static float getDamageBonus(Entity ent) {
        return ent.hasMetadata("damageBonus") ? ent.getMetadata("damageBonus").get(0).asFloat() : 0;
    }

    public static void removeDamageBonus(Entity ent) {
        if (ent.hasMetadata("damageBonus")) ent.removeMetadata("damageBonus", DungeonRealms.getInstance());
    }

    /**
     * Sets a blanket armor ignore bonus for a specified entity. When the entity
     * attacks, the armor reduction of the attacked entity will be reduced by this
     * bonus.
     *
     * @param ent
     * @param bonusPercent - the bonus amount as a percentage. (e.g. 50.0 will
     *                     reduce armor reduction by 50%).
     */
    public static void setArmorBonus(Entity ent, float bonusPercent) {
        ent.setMetadata("armorBonus", new FixedMetadataValue(DungeonRealms.getInstance(), bonusPercent));
    }

    public static float getArmorBonus(Entity ent) {
        return ent.hasMetadata("armorBonus") ? ent.getMetadata("armorBonus").get(0).asFloat() : 0;
    }

    public static void removeArmorBonus(Entity ent) {
        if (ent.hasMetadata("armorBonus")) ent.removeMetadata("armorBonus", DungeonRealms.getInstance());
    }

    public static void setInvulnerable(Entity ent) {
        Metadata.INVULNERABLE.set(ent, true);
    }

    public static boolean isInvulnerable(Entity ent) {
        return Metadata.INVULNERABLE.get(ent).asBoolean();
    }

    public static void removeInvulnerable(Entity ent) {
        Metadata.INVULNERABLE.remove(ent);
    }

    public static void createDamageHologram(Player createFor, Location createAround, double hp) {
        createDamageHologram(createFor, createAround, ChatColor.RED + "-" + (int) hp + " ❤");
    }

    /**
     * Create a hologram that floats up and deletes itself.
     */
    public static void createDamageHologram(Player createFor, Location createAround, String display) {
        if (createFor != null && !PlayerWrapper.getPlayerWrapper(createFor).getToggles().getState(Toggles.HOLOGRAMS))
            return;
        double xDif = (Utils.randInt(0, 20) - 10) / 10D;
        double yDif = Math.random();
        double zDif = (Utils.randInt(0, 20) - 10) / 10D;

        ArmorStand stand = (ArmorStand) createAround.getWorld().spawnEntity(createAround.add(xDif, yDif, zDif).subtract(0, 1, 0), EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setCollidable(false);
        stand.setCustomName(display);
        stand.setCustomNameVisible(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);

        stand.setMarker(true);
//        Hologram hologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), createAround.add(xDif, yDif, zDif));
//        hologram.appendTextLine(display);
//        hologram.getVisibilityManager().setVisibleByDefault(true);

        EntityArmorStand nmsStand = ((CraftArmorStand) stand).getHandle();
        nmsStand.noclip = true;
        nmsStand.setSmall(true);
        HashMap<ArmorStand, BukkitTask> holograms = DAMAGE_HOLOGRAMS.computeIfAbsent(createFor, k -> new HashMap<>());

        BukkitTask runnable = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 20 || !stand.isValid() || createFor != null && !createFor.isOnline()) {
                    cancel();
                    removeDamageHologram(createFor, stand);
                    return;
                }
                nmsStand.locY = nmsStand.locY + .1D;
                ticks++;
            }
        }.runTaskTimer(DungeonRealms.getInstance(), 0, 1);

        holograms.put(stand, runnable);
        if (holograms.keySet().size() > 4)
            removeDamageHologram(createFor, holograms.keySet().toArray(new ArmorStand[1])[0]);
    }

    private static void removeDamageHologram(Player player, ArmorStand armorStand) {
        if (armorStand.isDead())
            return;

        HashMap<ArmorStand, BukkitTask> map = DAMAGE_HOLOGRAMS.get(player);
        BukkitTask task = map.remove(armorStand);
        armorStand.remove();
        if (task != null)
            task.cancel();

        if (map.isEmpty())
            DAMAGE_HOLOGRAMS.remove(player);
    }
}