package net.dungeonrealms.game.world.items;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.EnergyHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.minecraft.server.v1_9_R2.EntityArrow;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Created by Kieran on 9/21/2015.
 */
public class DamageAPI {

    public static Set<Entity> polearmAOEProcessing = new HashSet<>();

    /**
     * Calculates the weapon damage based on the nbt tag of an item, the attacker and receiver
     *
     * @param attacker
     * @param receiver
     * @since 1.0
     */
    public static double calculateWeaponDamage(LivingEntity attacker, LivingEntity receiver) {
        try {
            ItemStack weapon = attacker.getEquipment().getItemInMainHand();
            if (!API.isWeapon(weapon)) return 1; // air or something else not a weapon should do 1 dmg

            // get the attacker's attributes
            Map<String, Integer[]> attackerAttributes;
            if (API.isPlayer(attacker)) {
                if (receiver.hasMetadata("tier")) {
                    //Player attacking monster, check if its a lower tier.
                    int mobTier = receiver.getMetadata("tier").get(0).asInt();
                    int wepTier = RepairAPI.getArmorOrWeaponTier(weapon);
                    if (wepTier > mobTier) {
                        int tierDif = RepairAPI.getArmorOrWeaponTier(weapon) - receiver.getMetadata("tier").get(0).asInt();

                        if (tierDif == 2) {
                            RepairAPI.subtractCustomDurability((Player) attacker, weapon, 4);
                        } else if (tierDif == 3) {
                            RepairAPI.subtractCustomDurability((Player) attacker, weapon, 6);
                        } else if (tierDif == 4) {
                            RepairAPI.subtractCustomDurability((Player) attacker, weapon, 8);
                        } else {
                            RepairAPI.subtractCustomDurability((Player) attacker, weapon, 1);
                        }
                    } else {
                        RepairAPI.subtractCustomDurability((Player) attacker, weapon, 1);
                    }
                } else {
                    RepairAPI.subtractCustomDurability((Player) attacker, weapon, 1);
                }
                GamePlayer gp = API.getGamePlayer((Player) attacker);

                // a player switches weapons, so we need to recalculate weapon attributes
                if (!gp.getCurrentWeapon().equals(API.getItemUID(weapon))) {
                    API.calculateAllAttributes((Player) attacker);
                    gp.setCurrentWeapon(API.getItemUID(weapon));
                }

                attackerAttributes = gp.getAttributes();
            } else if (((CraftLivingEntity) attacker).getHandle() instanceof DRMonster) {
                attackerAttributes = ((DRMonster) ((CraftLivingEntity) attacker).getHandle()).getAttributes();
            } else {
                return 0;
            }

            // BASE DAMAGE
            double damage = Utils.randInt(attackerAttributes.get("damage")[0], attackerAttributes.get("damage")[1]);
            boolean isHitCrit = false;

            // VS MONSTERS AND PLAYERS
            if (API.isPlayer(receiver)) {
                damage += ((((double) attackerAttributes.get("vsPlayers")[1]) / 100.) * damage);
                if (attacker.hasMetadata("type")) {
                    if (attacker.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                        if (((CraftLivingEntity) attacker).getHandle() instanceof DRMonster) {
                            ((DRMonster) ((CraftLivingEntity) attacker).getHandle()).onMonsterAttack((Player) receiver);
                        }
                    }
                }
            } else {
                if (receiver.hasMetadata("type")) {
                    if (receiver.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                        damage += ((((double) attackerAttributes.get("vsMonsters")[1]) / 100.) * damage);
                    }
                }
            }

            // DPS AND PURE DAMAGE
            damage += damage * (((double) Utils.randInt(attackerAttributes.get("dps")[0], attackerAttributes.get("dps")[1])) / 100.);

            damage += attackerAttributes.get("pureDamage")[1];
            int critHit = attackerAttributes.get("criticalHit")[1];

            // STAT BONUSES
            switch (new Attribute(weapon).getItemType()) {
                case AXE:
                    critHit += 3;
                case POLEARM:
                    if (attackerAttributes.get("strength")[1] != 0) {
                        damage += (damage / 100.) * (attackerAttributes.get("strength")[1] * 0.023D);
                    }
                    break;
                case SWORD:
                    if (attackerAttributes.get("vitality")[1] != 0) {
                        damage += (damage / 100.) * (attackerAttributes.get("vitality")[1] * 0.023D);
                    }
                    break;
                default:
                    break;
            }

            // KNOCKBACK
            if (API.isPlayer(attacker) && attackerAttributes.get("knockback")[1] > 0) {
                if (new Random().nextInt(100) < attackerAttributes.get("knockback")[1]) {
                    knockbackEntity((Player) attacker, receiver, 1.5);
                }
            }

            int weaponTier = new Attribute(weapon).getItemTier().getTierId();

            // BLIND AND SLOW
            if (attackerAttributes.get("blind")[1] > 0) {
                if (new Random().nextInt(100) < attackerAttributes.get("blind")[1]) {
                    applyBlind(receiver, weaponTier);
                }
            }

            if (attackerAttributes.get("slow")[1] > 0) {
                if (new Random().nextInt(100) < attackerAttributes.get("slow")[1]) {
                    applySlow(receiver);
                }
            }

            // ELEMENTAL DAMAGE
            if (API.isPlayer(attacker)) {
                if (attackerAttributes.get("fireDamage")[1] != 0) {
                    applyFireDebuff(receiver, weaponTier);
                    damage += attackerAttributes.get("fireDamage")[1];
                } else if (attackerAttributes.get("iceDamage")[1] != 0) {
                    applyIceDebuff(receiver, weaponTier);
                    damage += attackerAttributes.get("iceDamage")[1];
                } else if (attackerAttributes.get("poisonDamage")[1] != 0) {
                    applyPoisonDebuff(receiver, weaponTier);

                    damage += attackerAttributes.get("poisonDamage")[1];
                }
            } else if (API.isMobElemental(attacker)) {
                if (API.getMobElement(attacker).equals("fire")) {
                    applyFireDebuff(receiver, weaponTier);
                } else if (API.getMobElement(attacker).equals("ice")) {
                    applyIceDebuff(receiver, weaponTier);
                } else if (API.getMobElement(attacker).equals("poison")) {
                    applyPoisonDebuff(receiver, weaponTier);
                }
            }

            // CRIT CHANCE
            if (new Random().nextInt(100) < critHit) {
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.MAGIC_CRIT, receiver.getLocation(),
                            new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                isHitCrit = true;
            }

            // LIFESTEAL
            if (attackerAttributes.get("lifesteal")[1] != 0) {
                double lifeToHeal = ((((float) attackerAttributes.get("lifesteal")[1]) / 100.) * damage);
                if (attacker instanceof Player) {
                    HealthHandler.getInstance().healPlayerByAmount((Player) attacker, (int) lifeToHeal + 1);
                } else if (attacker.hasMetadata("type")) {
                    HealthHandler.getInstance().healMonsterByAmount(attacker, (int) lifeToHeal + 1);
                }
                damage += lifeToHeal + 1;
            }

            // DAMAGE BUFFS
            damage = applyIncreaseDamagePotion(attacker, damage);

            if (!(attacker instanceof Player)) {
                if (attacker.hasMetadata("attack")) {
                    damage += (damage * (attacker.getMetadata("attack").get(0).asDouble() / 100.));
                }
            }

            if (attacker.hasMetadata("damageBonus")) {
                damage += (damage * (attacker.getMetadata("damageBonus").get(0).asDouble() / 100.));
            }

            // ELITE CALCULATION
            if (attacker.hasMetadata("elite")) {
                switch (attacker.getMetadata("tier").get(0).asInt()) {
                    case 1:
                        damage *= 2.5;
                        break;
                    case 2:
                        damage *= 2.5;
                        break;
                    case 3:
                        damage *= 3;
                        break;
                    case 4:
                        damage *= 5;
                        break;
                    case 5:
                        damage *= 7;
                        break;
                    default:
                        break;
                }
            }

            if (isHitCrit) {
                if (attacker instanceof Player) {
                    if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, attacker.getUniqueId
                            ()).toString())) {
                        attacker.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "                        *CRIT*");
                    }
                    receiver.getWorld().playSound(attacker.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.5F, 0.5F);
                }
                damage *= 2;
            }

            return Math.round(damage);
        }
        catch (Exception ex) { // SAFETY CHECK todo: debug everything causing exceptions
            // recalculate all attributes as a failsafe
            if (!API.isPlayer(attacker)) {
                Utils.log.warning("[DamageAPI] Mob caused exception in calculateWeaponDamage.");
                API.calculateAllAttributes(attacker, ((DRMonster) attacker).getAttributes());
                ex.printStackTrace();
                Utils.log.info("Attacker: " + attacker.getName());
                Utils.log.info("Defender: " + receiver.getName());
                Utils.log.info("Attacker attributes: ");
                ((DRMonster) attacker).getAttributes().toString();
                return calculateWeaponDamage(attacker, receiver);
            }
            API.calculateAllAttributes((Player) attacker);
            Utils.log.info("[DamageAPI] calculateWeaponDamage attribute error.");
            ex.printStackTrace();
            Utils.log.info("Attacker: " + attacker.getName());
            Utils.log.info("Defender: " + receiver.getName());
            Utils.log.info("Attacker attributes: ");
            API.getGamePlayer((Player) attacker).getAttributes().toString();
            return calculateWeaponDamage(attacker, receiver);
        }
    }

    public static void applyPoisonDebuff(LivingEntity receiver, int weaponTier) {
        receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1F, 1F);
        receiver.getWorld().playEffect(receiver.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 4);

        switch (weaponTier) {
            case 1:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30, 0));
                break;
            case 2:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                break;
            case 3:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 0));
                break;
            case 4:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                break;
            case 5:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 1));
                break;
        }
    }

    public static void applyFireDebuff(LivingEntity receiver, int weaponTier) {
        try {
            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.FLAME, receiver.getLocation(),
                    new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        switch (weaponTier) {
            case 1:
                receiver.setFireTicks(15);
                break;
            case 2:
                receiver.setFireTicks(25);
                break;
            case 3:
                receiver.setFireTicks(30);
                break;
            case 4:
                receiver.setFireTicks(35);
                break;
            case 5:
                receiver.setFireTicks(40);
                break;
        }
    }

    public static void applySlow(LivingEntity receiver) {
        if (receiver.hasMetadata("type")) {
            final int MOB_TIER = receiver.getMetadata("tier").get(0).asInt();
            if (MOB_TIER == 4 || MOB_TIER == 5)
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
            else
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
        }
        else {
            receiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
        }
    }

    public static void applyBlind(LivingEntity receiver, int weaponTier) {
        switch (weaponTier) {
            case 1:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 1));
                break;
            case 2:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
                break;
            case 3:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 1));
                break;
            case 4:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                break;
            case 5:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                break;
            default:
                break;
        }
    }

    /**
     * Calculates the weapon damage based on the metadata of the projectile, the attacker and receiver
     *
     * @param attacker
     * @param receiver
     * @param projectile
     * @since 1.0
     */
    public static double calculateProjectileDamage(LivingEntity attacker, LivingEntity receiver, Projectile projectile) {
        try {
            Map<String, Integer[]> attributes;
            // grab the attacker's armor attributes
            if (attacker instanceof Player && API.isPlayer(attacker)) {
                attributes = API.getGamePlayer((Player) attacker).getAttributes();
            } else if (((CraftLivingEntity) attacker).getHandle() instanceof DRMonster) {
                attributes = ((DRMonster) ((CraftLivingEntity) attacker).getHandle()).getAttributes();
            } else {
                return 0;
            }

            double damage = Utils.randInt(projectile.getMetadata("damageMin").get(0).asInt(), projectile.getMetadata
                    ("damageMax").get(0).asInt());
            boolean isHitCrit = false;

            // VS PLAYERS AND VS MONSTERS
            if (API.isPlayer(receiver)) {
                if (projectile.getMetadata("vsPlayers").get(0).asDouble() != 0) {
                    damage += ((projectile.getMetadata("vsPlayers").get(0).asDouble() / 100) * damage);
                }
            } else {
                if (receiver.hasMetadata("type") && receiver.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {

                    if (projectile.getMetadata("vsMonsters").get(0).asDouble() != 0) {
                        damage += ((projectile.getMetadata("vsMonsters").get(0).asDouble() / 100) * damage);
                    }
                }
            }

            // PURE DAMAGE
            if (projectile.getMetadata("pureDamage").get(0).asInt() != 0) {
                damage += projectile.getMetadata("pureDamage").get(0).asInt();
            }

            // KNOCKBACK
            if (API.isPlayer(attacker) && projectile.getMetadata("knockback").get(0).asInt() > 0) {
                if (new Random().nextInt(100) < projectile.getMetadata("knockback").get(0).asInt()) {
                    knockbackEntity((Player) attacker, receiver, 1.5);
                }
            }

            // we add 1 because it's tierID which is 0-4 instead of 1-5
            int weaponTier = projectile.getMetadata("itemTier").get(0).asInt() + 1;

            // DPS
            damage += Utils.randInt(attributes.get("dps")[0], attributes.get("dps")[1]);

            // BLIND AND SLOW
            if (projectile.getMetadata("blind").get(0).asInt() > 0) {
                if (new Random().nextInt(100) < projectile.getMetadata("blind").get(0).asInt()) {
                    applyBlind(receiver, weaponTier);
                }
            }

            if (projectile.getMetadata("slow").get(0).asInt() > 0) {
                if (new Random().nextInt(100) < projectile.getMetadata("slow").get(0).asInt()) {
                    applySlow(receiver);
                }
            }

            // ELEMENTAL DAMAGE
            if (API.isPlayer(attacker)) {
                if (projectile.getMetadata("fireDamage").get(0).asInt() != 0) {
                    applyFireDebuff(receiver, weaponTier);
                    damage += projectile.getMetadata("fireDamage").get(0).asInt();
                } else if (projectile.getMetadata("iceDamage").get(0).asInt() != 0) {
                    applyIceDebuff(receiver, weaponTier);
                    damage += projectile.getMetadata("iceDamage").get(0).asInt();
                } else if (projectile.getMetadata("poisonDamage").get(0).asInt() != 0) {
                    applyPoisonDebuff(receiver, weaponTier);
                    damage += projectile.getMetadata("poisonDamage").get(0).asInt();
                }
            } else if (API.isMobElemental(attacker)) {
                if (API.getMobElement(attacker).equals("fire")) {
                    applyFireDebuff(receiver, weaponTier);
                } else if (API.getMobElement(attacker).equals("ice")) {
                    applyIceDebuff(receiver, weaponTier);
                } else if (API.getMobElement(attacker).equals("poison")) {
                    applyPoisonDebuff(receiver, weaponTier);
                }
            }

            // STAT BONUS DAMAGE
            switch (projectile.getType()) {
                case ARROW:
                case TIPPED_ARROW:
                    damage += (damage / 100.) * attributes.get("dexterity")[1] * 0.015D;
                    break;
                case SNOWBALL:
                case SMALL_FIREBALL:
                case ENDER_PEARL:
                case FIREBALL:
                case WITHER_SKULL:
                    damage += (damage / 100.) * attributes.get("intellect")[1] * 0.02D;
                    break;
                default:
                    break;
            }

            if (projectile.getMetadata("criticalHit").get(0).asInt() != 0) {
                if (new Random().nextInt(100) < projectile.getMetadata("criticalHit").get(0).asInt()) {
                    try {
                        ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.MAGIC_CRIT, receiver.getLocation().add(0, 1, 0),
                                new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    isHitCrit = true;
                }
            }

            if (projectile.getMetadata("lifesteal").get(0).asDouble() != 0) {
                double lifeToHeal = ((projectile.getMetadata("lifesteal").get(0).asDouble() / 100) * damage);
                if (attacker instanceof Player) {
                    HealthHandler.getInstance().healPlayerByAmount((Player) attacker, (int) lifeToHeal + 1);
                } else if (attacker instanceof CraftLivingEntity) {
                    if (attacker.hasMetadata("type")) {
                        HealthHandler.getInstance().healMonsterByAmount(attacker, (int) lifeToHeal + 1);
                    }
                }
                damage += (int) lifeToHeal + 1;
            }

            damage = applyIncreaseDamagePotion(attacker, damage);

            if (!(attacker instanceof Player)) {
                if (attacker.hasMetadata("attack")) {
                    damage += (damage * (attacker.getMetadata("attack").get(0).asDouble() / 100));
                }
            }
            if (attacker.hasMetadata("damageBonus")) {
                damage += (damage * (attacker.getMetadata("damageBonus").get(0).asDouble() / 100.));

            }
            if (isHitCrit) {
                if (attacker instanceof Player) {
                    if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, attacker.getUniqueId
                            ()).toString())) {
                        attacker.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "                        *CRIT*");
                    }
                    receiver.getWorld().playSound(attacker.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.5F, 0.5F);
                }
                damage = damage * 2;
            }
            return Math.round(damage) + 1;
        }
        catch (Exception ex) { // SAFETY CHECK todo: debug everything causing exceptions
            // recalculate all attributes as a failsafe
            if (!API.isPlayer(attacker)) {
                Utils.log.warning("[DamageAPI] Mob caused exception in calculateProjectileDamage.");
                API.calculateAllAttributes(attacker, ((DRMonster) attacker).getAttributes());
                ex.printStackTrace();
                Utils.log.info("Attacker: " + attacker.getName());
                Utils.log.info("Defender: " + receiver.getName());
                Utils.log.info("Attacker attributes: ");
                ((DRMonster) attacker).getAttributes().toString();
                return calculateProjectileDamage(attacker, receiver, projectile);
            }
            API.calculateAllAttributes((Player) attacker);
            Utils.log.info("[DamageAPI] calculateProjectileDamage attribute error.");
            ex.printStackTrace();
            Utils.log.info("Attacker: " + attacker.getName());
            Utils.log.info("Defender: " + receiver.getName());
            Utils.log.info("Attacker attributes: ");
            API.getGamePlayer((Player) attacker).getAttributes().toString();
            return calculateProjectileDamage(attacker, receiver, projectile);
        }
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
            switch (potionTier) {
                case 0:
                    damage *= 1.1;
                    break;
                case 1:
                    damage *= 1.3;
                    break;
                case 2:
                    damage *= 1.5;
                    break;
            }
        }
        return damage;
    }

    public static void applyIceDebuff(LivingEntity receiver, int weaponTier) {
        receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1F, 1F);
        receiver.getWorld().playEffect(receiver.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8194);

        switch (weaponTier) {
            case 1:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0));
                break;
            case 2:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                break;
            case 3:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
                break;
            case 4:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                break;
            case 5:
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 1));
                break;
        }
    }

    /**
     * Calculates the new damage based on the armor of the defender and the previous damage
     *
     * @param attacker
     * @param defender
     * @param totalDamage
     * @param projectile must leave null if melee damage (no projectile)!
     * @since 1.0
     */
    public static double[] calculateArmorReduction(LivingEntity attacker, LivingEntity defender, double totalDamage, Projectile projectile) {
        try {
            double damageAfterArmor = totalDamage;
            int totalArmor = 0;
            double totalArmorReduction = 0;
            Map<String, Integer[]> defenderAttributes;
            Map<String, Integer[]> attackerAttributes;

            if (API.isPlayer(defender)) {
                Player p = (Player) defender;
                for (ItemStack armor : defender.getEquipment().getArmorContents()) {
                    if (!API.isArmor(armor)) continue;
                    RepairAPI.subtractCustomDurability(p, armor, 1);
                }
                defenderAttributes = API.getGamePlayer(p).getAttributes();
            } else if (((CraftLivingEntity) defender).getHandle() instanceof DRMonster) {
                defenderAttributes = ((DRMonster) ((CraftLivingEntity) defender).getHandle()).getAttributes();
            } else {
                return new double[]{totalArmorReduction, totalArmor};
            }

            if (API.isPlayer(attacker)) {
                if (projectile == null)
                    attackerAttributes = API.getGamePlayer((Player) attacker).getAttributes();
                else
                    attackerAttributes = new HashMap<>(API.getGamePlayer((Player) attacker).getAttributes());
            } else if (((CraftLivingEntity) attacker).getHandle() instanceof DRMonster) {
                attackerAttributes = ((DRMonster) ((CraftLivingEntity) attacker).getHandle()).getAttributes();
            } else {
                return new double[]{totalArmorReduction, totalArmor};
            }

            if (projectile != null) { // if projectile damage, we need to transfer metadata
                Map<String, Integer[]> projectileAttributes = new HashMap<>();
                for (Item.WeaponAttributeType type : Item.WeaponAttributeType.values()) {
                    String modifier = type.getNBTName();
                    if (type.isRange()) {
                        if (projectile.hasMetadata(modifier + "Min") && projectile.getMetadata(modifier + "Min").get(0)
                                .asInt() != 0) {
                            projectileAttributes.put(type.getName(), new Integer[]{projectile.getMetadata(modifier +
                                    "Min").get(0).asInt(), projectile.getMetadata(modifier + "Max").get(0).asInt()});
                        }
                    } else {
                        if (projectile.hasMetadata(modifier) && projectile.getMetadata(modifier).get(0).asInt() != 0) {
                            projectileAttributes.put(type.getName(), new Integer[]{0, projectile.getMetadata(modifier)
                                    .get(0).asInt()});
                        }
                    }
                }
                attackerAttributes.putAll(projectileAttributes);
            }

            // DODGE AND BLOCK
            int dodgeChance = defenderAttributes.get("dodge")[1];
            int blockChance = defenderAttributes.get("block")[1];
            final int DODGE_ROLL = new Random().nextInt(100);
            final int BLOCK_ROLL = new Random().nextInt(100);
            boolean toggleDebug = attacker instanceof Player ? (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, attacker.getUniqueId()) : false;

            if (DODGE_ROLL < dodgeChance - attackerAttributes.get("accuracy")[1]) {
                if (toggleDebug && DODGE_ROLL >= dodgeChance) {
                    attacker.sendMessage(ChatColor.GREEN + "Your " + API.getGamePlayer((Player) attacker)
                            .getRangedAttributeVal(Item.WeaponAttributeType.ACCURACY)[1] + "% accuracy has prevented " +
                            defender.getCustomName() + ChatColor.GREEN + " from dodging.");
                }
                removeElementalEffects(defender);
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CLOUD, defender.getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                totalArmorReduction = -1;
                return new double[]{Math.round(totalArmorReduction), totalArmor};
            } else if (BLOCK_ROLL < blockChance - attackerAttributes.get("accuracy")[1]) {
                if (toggleDebug && BLOCK_ROLL >= blockChance) {
                    attacker.sendMessage(ChatColor.GREEN + "Your " + API.getGamePlayer((Player) attacker)
                            .getRangedAttributeVal(Item.WeaponAttributeType.ACCURACY)[1] + "% accuracy has prevented " +
                            defender.getCustomName() + ChatColor.GREEN + " from blocking.");
                }
                removeElementalEffects(defender);
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.RED_DUST, defender.getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                totalArmorReduction = -2;
                return new double[]{Math.round(totalArmorReduction), totalArmor};
            }
            // REFLECT
//            int reflectChance = defenderAttributes.get("reflection")[1];
//            if (new Random().nextInt(100) < reflectChance) {
//                totalArmorReduction = -3;
//                return new double[]{Math.round(totalArmorReduction), totalArmor};
//            }
            // BASE ARMOR
            totalArmor = Utils.randInt(defenderAttributes.get("armor")[0], defenderAttributes.get("armor")[1]);

            // ARMOR PENETRATION
            if (attackerAttributes.get("armorPenetration")[1] != 0) {
                totalArmor -= attackerAttributes.get("armorPenetration")[1];
                if (totalArmor < 0) totalArmor = 0;
            }

            // THORNS
            if (defenderAttributes.get("thorns")[1] != 0 && projectile == null) { // thorns only applies for melee
                // combat now
                int damageFromThorns = (int) Math.round(totalDamage * (((float) defenderAttributes.get("thorns")[1]) / 100f));
                if (damageFromThorns <= 0) damageFromThorns = 1; // always at least one damage from thorns
                if (damageFromThorns > 0) {
                    attacker.getLocation().getWorld().playEffect(attacker.getLocation(), Effect.STEP_SOUND, 18);
                }
                if (attacker instanceof Player) {
                    if (((Player) attacker).getGameMode() == GameMode.SURVIVAL && !API.getGamePlayer((Player)
                            attacker).isInvulnerable()) {
                        HealthHandler.getInstance().handlePlayerBeingDamaged((Player) attacker, defender, damageFromThorns, 0, 0);
                    }
                } else {
                    HealthHandler.getInstance().handleMonsterBeingDamaged(attacker, defender, damageFromThorns);
                }
            }

            // ELEMENTAL DAMAGE
            int pureDamage = API.isPlayer(attacker) ? attackerAttributes.get("pureDamage")[1] : 0;
            int fireDamage = API.isPlayer(attacker) ? attackerAttributes.get("fireDamage")[1] : 0;
            int iceDamage = API.isPlayer(attacker) ? attackerAttributes.get("iceDamage")[1] : 0;
            int poisonDamage = API.isPlayer(attacker) ? attackerAttributes.get("poisonDamage")[1] : 0;
            int elementalDamage = 0;
            int armorFromResistance = 0;

            if (API.isPlayer(attacker)) {
                if (fireDamage != 0) {
                    float fireResistance = (float) defenderAttributes.get("fireResistance")[1];
                    elementalDamage = fireDamage;
                    if (fireResistance != 0) {
                        armorFromResistance += fireResistance;
                    }
                } else if (iceDamage != 0) {
                    float iceResistance = (float) defenderAttributes.get("iceResistance")[1];
                    elementalDamage = iceDamage;
                    if (iceResistance != 0) {
                        armorFromResistance += iceResistance;
                    }
                } else if (poisonDamage != 0) {
                    float poisonResistance = (float) defenderAttributes.get("poisonResistance")[1];
                    elementalDamage = poisonDamage;
                    if (poisonResistance != 0) {
                        armorFromResistance += poisonResistance;
                    }
                }
            } else if (API.isMobElemental(attacker)) {
                if (API.getMobElement(attacker).equals("fire")) {
                    totalArmor *= 0.2;
                    totalArmor += defenderAttributes.get("fireResistance")[1];
                } else if (API.getMobElement(attacker).equals("ice")) {
                    totalArmor *= 0.2;
                    totalArmor += defenderAttributes.get("iceResistance")[1];
                } else if (API.getMobElement(attacker).equals("poison")) {
                    totalArmor *= 0.2;
                    totalArmor += defenderAttributes.get("poisonResistance")[1];
                } else if (API.getMobElement(attacker).equals("pure")) {
                    totalArmor *= 0;
                }
            }

            damageAfterArmor = (damageAfterArmor - pureDamage - elementalDamage) * ((double) (100 - totalArmor)) / 100d;
            // pure damage ignores all armor
            damageAfterArmor += pureDamage;
            // elemental damage ignores 80% but add on resistance
            if (elementalDamage != 0) {
                damageAfterArmor += ((0.8) * elementalDamage) * ((double) (100 - armorFromResistance)) / 100d;
            }
            totalArmorReduction = totalDamage - damageAfterArmor;

            // POTION EFFECTS
            if (defender.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                int potionTier = 1;
                for (PotionEffect pe : defender.getActivePotionEffects()) {
                    if (pe.getType() == PotionEffectType.DAMAGE_RESISTANCE) {
                        potionTier = pe.getAmplifier();
                        break;
                    }
                }
                switch (potionTier) {
                    case 1:
                        totalArmorReduction *= 1.05;
                        break;
                    case 2:
                        totalArmorReduction *= 1.1;
                        break;
                    case 3:
                        totalArmorReduction *= 1.2;
                        break;
                }
            }

            // ARMOR BONUS
            if (defender.hasMetadata("armorBonus")) {
                totalArmor += (defender.getMetadata("armorBonus").get(0).asFloat() / 100f) * totalArmor;
                totalArmorReduction += (defender.getMetadata("armorBonus").get(0).asFloat() / 100f) * totalArmorReduction;
            }
            return new double[]{Math.round(totalArmorReduction), totalArmor};
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Utils.log.warning("Attacker: " + attacker.getName());
            Utils.log.warning("Defender: " + defender.getName());
            if (API.isPlayer(attacker)) {
                API.calculateAllAttributes((Player) attacker);
            }
            else {
                API.calculateAllAttributes(attacker, ((DRMonster) attacker).getAttributes());
            }
            if (API.isPlayer(defender)) {
                API.calculateAllAttributes((Player) defender);
            }
            else {
                API.calculateAllAttributes(attacker, ((DRMonster) attacker).getAttributes());
            }
            return calculateArmorReduction(attacker, defender, totalDamage, projectile);
        }
    }

    public static int calculatePlayerStat(Player player, Item.ArmorAttributeType type) {
        int statAmount[] = new int[4];
        int totalStat;
        NBTTagCompound nmsTags[] = new NBTTagCompound[4];
        EntityEquipment playerEquipment = player.getEquipment();
        ItemStack[] playerArmor = playerEquipment.getArmorContents();
        if (playerArmor[3] != null && playerArmor[3].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[3]).getTag() != null) {
                nmsTags[0] = CraftItemStack.asNMSCopy(playerArmor[3]).getTag();
            }
        }
        if (playerArmor[2] != null && playerArmor[2].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[2]).getTag() != null) {
                nmsTags[1] = CraftItemStack.asNMSCopy(playerArmor[2]).getTag();
            }
        }
        if (playerArmor[1] != null && playerArmor[1].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[1]).getTag() != null) {
                nmsTags[2] = CraftItemStack.asNMSCopy(playerArmor[1]).getTag();
            }
        }
        if (playerArmor[0] != null && playerArmor[0].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[0]).getTag() != null) {
                nmsTags[3] = CraftItemStack.asNMSCopy(playerArmor[0]).getTag();
            }
        }
        for (int i = 0; i < nmsTags.length; i++) {
            if (nmsTags[i] == null) {
                statAmount[i] += 0;
            } else {
                if (nmsTags[i].getInt(type.getNBTName()) != 0) {
                    statAmount[i] = nmsTags[i].getInt(type.getNBTName());
                }
            }
        }
        totalStat = statAmount[0] + statAmount[1] + statAmount[2] + statAmount[3];

        return Math.round(totalStat);
    }

    public static void fireStaffProjectile(Player player, ItemStack itemStack, NBTTagCompound tag) {
        RepairAPI.subtractCustomDurability(player, itemStack, 1);
        int weaponTier = tag.getInt("itemTier");
        Projectile projectile = null;
        switch (weaponTier) {
            case 1:
                projectile = player.launchProjectile(Snowball.class);
                projectile.setVelocity(projectile.getVelocity().multiply(1.15));
                break;
            case 2:
                projectile = player.launchProjectile(SmallFireball.class);
                projectile.setVelocity(projectile.getVelocity().multiply(1.5));
                ((SmallFireball) projectile).setYield(0);
                ((SmallFireball) projectile).setIsIncendiary(false);
                break;
            case 3:
                projectile = player.launchProjectile(EnderPearl.class);
                projectile.setVelocity(projectile.getVelocity().multiply(1.75));
                break;
            case 4:
                projectile = player.launchProjectile(LargeFireball.class);
                projectile.setVelocity(projectile.getVelocity().multiply(2));
                ((LargeFireball) projectile).setYield(0);
                ((LargeFireball) projectile).setIsIncendiary(false);
                break;
            case 5:
                projectile = player.launchProjectile(WitherSkull.class);
                projectile.setVelocity(projectile.getVelocity().multiply(2.5));
                break;
        }
        if (projectile == null) return;
        projectile.setBounce(false);
        EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(itemStack));
        projectile.setShooter(player);
        // a player switches weapons, so we need to recalculate weapon attributes
        GamePlayer gp = API.getGamePlayer(player);
        if (!gp.getCurrentWeapon().equals(API.getItemUID(itemStack))) {
            API.calculateAllAttributes(player);
            gp.setCurrentWeapon(API.getItemUID(itemStack));
        }
        MetadataUtils.registerProjectileMetadata(gp.getAttributes(), tag, projectile);
    }

    public static void fireBowProjectile(Player player, ItemStack itemStack, NBTTagCompound tag) {
        RepairAPI.subtractCustomDurability(player, itemStack, 1);
        GamePlayer gp = API.getGamePlayer(player);
        Projectile projectile;
        if (tag.hasKey("fireDamage")) {
            projectile = player.launchProjectile(TippedArrow.class);
            ((TippedArrow) projectile).addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1, 1), true);
        } else if (tag.hasKey("iceDamage")) {
            projectile = player.launchProjectile(TippedArrow.class);
            ((TippedArrow) projectile).addCustomEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 1, 1), true);
        } else if (tag.hasKey("poisonDamage")) {
            projectile = player.launchProjectile(TippedArrow.class);
            ((TippedArrow) projectile).addCustomEffect(new PotionEffect(PotionEffectType.JUMP, 1, 1), true);
        } else {
            projectile = player.launchProjectile(Arrow.class);
        }
        if (projectile == null) {
            return;
        }
        projectile.setBounce(false);
        projectile.setVelocity(projectile.getVelocity().multiply(1.1));
        EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(itemStack));
        projectile.setShooter(player);
        EntityArrow eArrow = ((CraftArrow) projectile).getHandle();
        eArrow.fromPlayer = EntityArrow.PickupStatus.DISALLOWED;
        // a player switches weapons, so we need to recalculate weapon attributes
        if (!gp.getCurrentWeapon().equals(API.getItemUID(itemStack))) {
            API.calculateAllAttributes(player);
            gp.setCurrentWeapon(API.getItemUID(itemStack));
        }
        MetadataUtils.registerProjectileMetadata(gp.getAttributes(), tag, projectile);
    }

    public static void fireStaffProjectileMob(CraftLivingEntity livingEntity, NBTTagCompound tag, LivingEntity target) {
        if (!(target instanceof Player)) return;
        org.bukkit.util.Vector vector = target.getLocation().toVector().subtract(livingEntity.getLocation().toVector()).normalize();
        int weaponTier = tag.getInt("itemTier");
        Projectile projectile = null;
        switch (weaponTier) {
            case 1:
                projectile = livingEntity.launchProjectile(Snowball.class);
                vector.multiply(1.15);
                break;
            case 2:
                projectile = livingEntity.launchProjectile(SmallFireball.class);
                vector.multiply(1.5);
                ((SmallFireball) projectile).setYield(0);
                ((SmallFireball) projectile).setIsIncendiary(false);
                break;
            case 3:
                projectile = livingEntity.launchProjectile(EnderPearl.class);
                vector.multiply(1.75);
                break;
            case 4:
                projectile = livingEntity.launchProjectile(LargeFireball.class);
                vector.multiply(2);
                ((LargeFireball) projectile).setYield(0);
                ((LargeFireball) projectile).setIsIncendiary(false);
                break;
            case 5:
                projectile = livingEntity.launchProjectile(WitherSkull.class);
                vector.multiply(2.5);
                break;
        }
        if (projectile == null) return;
        projectile.setBounce(false);
        projectile.setVelocity(vector);
        projectile.setShooter(livingEntity);
        MetadataUtils.registerProjectileMetadata(((DRMonster) livingEntity.getHandle()).getAttributes(), tag, projectile);
    }

    public static void fireArrowFromMob(CraftLivingEntity livingEntity, NBTTagCompound tag, LivingEntity target) {
        if (!(target instanceof Player)) return;
        org.bukkit.util.Vector vector = target.getLocation().toVector().subtract(livingEntity.getLocation().toVector()).normalize();
        Projectile projectile = livingEntity.launchProjectile(Arrow.class);
        projectile.setBounce(false);
        vector.multiply(1.25);
        projectile.setVelocity(vector);
        projectile.setShooter(livingEntity);
        EntityArrow eArrow = ((CraftArrow) projectile).getHandle();
        eArrow.fromPlayer = EntityArrow.PickupStatus.DISALLOWED;
        MetadataUtils.registerProjectileMetadata(((DRMonster) livingEntity.getHandle()).getAttributes(), tag, projectile);
    }

    public static void removeElementalEffects(LivingEntity ent) {
        if (ent.hasPotionEffect(PotionEffectType.SLOW)) {
            ent.removePotionEffect(PotionEffectType.SLOW);
        }
        if (ent.hasPotionEffect(PotionEffectType.POISON)) {
            ent.removePotionEffect(PotionEffectType.POISON);
        }
        if (ent.getFireTicks() > 0) {
            ent.setFireTicks(0);
        }
    }

    public static void knockbackEntity(Player p, Entity ent, double speed) {
        if (ent instanceof Horse) {
            return;
        }
        // Get velocity unit vector:
        org.bukkit.util.Vector unitVector = ent.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
        // Set speed and push entity:
        ent.setVelocity(unitVector.multiply(speed));
    }

    public static boolean isStaffProjectile(Entity entity) {
        EntityType type = entity.getType();
        return type == EntityType.SNOWBALL || type == EntityType.SMALL_FIREBALL || type == EntityType.ENDER_PEARL ||
                type == EntityType.FIREBALL || type == EntityType.WITHER_SKULL;
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
}
