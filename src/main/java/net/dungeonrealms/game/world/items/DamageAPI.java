package net.dungeonrealms.game.world.items;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.EnergyHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.entities.types.monsters.boss.Boss;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Kieran on 9/21/2015.
 */
public class DamageAPI {

    public static List<Entity> polearmAOEProcessing = new ArrayList<>();

    /**
     * Calculates the weapon damage based on the nbt tag of an item, the attacker and receiver
     *
     * @param attacker
     * @param receiver
     * @param tag
     * @since 1.0
     */
    public static double calculateWeaponDamage(LivingEntity attacker, Entity receiver, NBTTagCompound tag) {
        if (!API.isWeapon(attacker.getEquipment().getItemInMainHand())) return 0;

        // get the attacker's attributes
        Map<String, Integer[]> attackerAttributes;
        if (API.isPlayer(attacker)) {
            RepairAPI.subtractCustomDurability((Player) attacker, attacker.getEquipment().getItemInMainHand(), 1);
            attackerAttributes = API.getGamePlayer((Player) attacker).getAttributes();
            // a player switches weapons, so we need to recalculate weapon attributes
            attackerAttributes.putAll(API.calculateWeaponAttributes(attacker.getEquipment().getItemInMainHand(), false));
        }
        else if (attacker instanceof DRMonster) {
            attackerAttributes = ((DRMonster) ((CraftLivingEntity) attacker).getHandle()).getAttributes();
        }
        else if (attacker instanceof Boss) {
            attackerAttributes = ((Boss) ((CraftLivingEntity) attacker).getHandle()).getAttributes();
        }
        else {
            return 0;
        }

        double damage = Utils.randInt(attackerAttributes.get("damage")[0], attackerAttributes.get("damage")[1]);
        boolean isHitCrit = false;

        if (API.isPlayer(receiver)) {
            damage += ((((double) attackerAttributes.get("vsPlayers")[1]) / 100) * damage);
            if (attacker.hasMetadata("type")) {
                if (attacker.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                    if (((CraftLivingEntity)attacker).getHandle() instanceof DRMonster) {
                        ((DRMonster) ((CraftLivingEntity)attacker).getHandle()).onMonsterAttack((Player) receiver);
                    }
                }
            }
        } else {
            if (receiver.hasMetadata("type")) {
                if (receiver.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                        damage += ((((double) attackerAttributes.get("vsPlayers")[1]) / 100) * damage);
                }
            }
        }

        damage += attackerAttributes.get("pureDamage")[1];

        Item.ItemType type = Item.ItemType.getTypeFromMaterial(attacker.getEquipment().getItemInMainHand().getType());
        switch (type) {
            case Item.ItemType.AXE:
            case Item.ItemType.POLEARM:
                if (attackerAttributes.get("strength")[1] != 0) {
                    damage += (damage / 100) * (attackerAttributes.get("strength")[1] * 0.023D);
                }
                break;
            case Item.ItemType.SWORD:
                if (attackerAttributes.get("vitality")[1] != 0) {
                    damage += (damage / 100) * (attackerAttributes.get("vitality")[1] * 0.023D);
                }
                break;
            default:
                break;
        }

        if (attackerAttributes.get("fireDamage")[1] != 0) {
            receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1F, 1F);
            receiver.getWorld().playEffect(receiver.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8195);
            damage += attackerAttributes.get("fireDamage")[1];
        }
        else if (attackerAttributes.get("iceDamage")[1] != 0) {
            receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1F, 1F);
            receiver.getWorld().playEffect(receiver.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8194);
            damage += attackerAttributes.get("iceDamage")[1];
        }
        else if (attackerAttributes.get("poisonDamage")[1] != 0) {
            receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1F, 1F);
            receiver.getWorld().playEffect(receiver.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8196);
            damage += attackerAttributes.get("poisonDamage")[1];
        }

        int critHit = attackerAttributes.get("criticalHit")[1];

        if (attacker.getEquipment().getItemInMainHand() != null) {
            if (new Attribute(attacker.getEquipment().getItemInMainHand()).getItemType() == Item.ItemType.AXE) {
                critHit += 3;
            }
        }
        if (attacker instanceof Player) {
            if (API.getGamePlayer((Player) attacker) != null) {
                critHit += ((API.getGamePlayer((Player) attacker).getStats()).getCritChance() * 100);
            }
        }
        if (tag.getInt("intellect") != 0) {
            critHit += (tag.getInt("intellect") * 0.025);
        }
        if (new Random().nextInt(99) < critHit) {
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.MAGIC_CRIT, receiver.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            isHitCrit = true;
        }

        if (tag.getDouble("lifesteal") != 0) {
            double lifeToHeal = ((tag.getDouble("lifesteal") / 100) * damage);
            if (attacker instanceof Player) {
                HealthHandler.getInstance().healPlayerByAmount((Player) attacker, (int) lifeToHeal + 1);
            } else if (attacker instanceof CraftLivingEntity) {
                if (attacker.hasMetadata("type")) {
                    HealthHandler.getInstance().healMonsterByAmount(attacker, (int) lifeToHeal + 1);
                }
            }
        }

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
        for (NBTTagCompound nmsTag : nmsTags) {
            if (nmsTag == null) {
                damage += 0;
            } else {
                if (nmsTag.getDouble("dpsMin") != 0) {
                    damage += (damage * (Utils.randInt((int) nmsTag.getDouble("dpsMin"), (int) nmsTag.getDouble("dpsMax")) / 100));
                }
            }
        }
        if (!(attacker instanceof Player)) {
            if (attacker.hasMetadata("attack")) {
                damage += (damage * (attacker.getMetadata("attack").get(0).asDouble() / 100));
            }
        } else {
            Player player = (Player) attacker;
            if (API.getGamePlayer(player) != null) {
                switch (new Attribute(((Player) attacker).getEquipment().getItemInMainHand()).getItemType()) {
                    case POLEARM:
                        damage += (damage * (API.getGamePlayer(player).getStats().getPolearmDMG()));
                        break;
                    case AXE:
                        damage += (damage * (API.getGamePlayer(player).getStats().getAxeDMG()));
                        break;
                    case SWORD:
                        damage += (damage * (API.getGamePlayer(player).getStats().getSwordDMG()));
                        break;
                    case BOW:
                        damage /= 2;
                        break;
                    case STAFF:
                        damage /= 2;
                        break;
                    default:
                        break;
                }
            }
        }
        if (isHitCrit) {
            if (attacker instanceof Player) {
                if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, attacker.getUniqueId()).toString())) {
                    attacker.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "                        *CRIT*");
                }
                ((Player) attacker).playSound(attacker.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.5F, 0.5F);
            }
            damage *= 2;
        }
        return Math.round(damage);
    }

    /**
     * Calculates the weapon damage based on the metadata of the projectile, the attacker and receiver
     *
     * @param attacker
     * @param receiver
     * @param projectile
     * @since 1.0
     */
    public static double calculateProjectileDamage(LivingEntity attacker, Entity receiver, Projectile projectile) {
        EntityEquipment entityEquipment = attacker.getEquipment();
        ItemStack[] attackerArmor = entityEquipment.getArmorContents();
        NBTTagCompound nmsTags[] = new NBTTagCompound[4];
        double damage = 0;
        if (attackerArmor[3] != null && attackerArmor[3].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(attackerArmor[3]).getTag() != null) {
                nmsTags[0] = CraftItemStack.asNMSCopy(attackerArmor[3]).getTag();
            }
        }
        if (attackerArmor[2] != null && attackerArmor[2].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(attackerArmor[2]).getTag() != null) {
                nmsTags[1] = CraftItemStack.asNMSCopy(attackerArmor[2]).getTag();
            }
        }
        if (attackerArmor[1] != null && attackerArmor[1].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(attackerArmor[1]).getTag() != null) {
                nmsTags[2] = CraftItemStack.asNMSCopy(attackerArmor[1]).getTag();
            }
        }
        if (attackerArmor[0] != null && attackerArmor[0].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(attackerArmor[0]).getTag() != null) {
                nmsTags[3] = CraftItemStack.asNMSCopy(attackerArmor[0]).getTag();
            }
        }
        damage = Utils.randInt((int) projectile.getMetadata("damageMin").get(0).asDouble(), (int) projectile.getMetadata("damageMax").get(0).asDouble());

        boolean isHitCrit = false;
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

        if (projectile.getMetadata("pureDamage").get(0).asInt() != 0) {
            damage += projectile.getMetadata("pureDamage").get(0).asInt();
        }

        if (projectile.getMetadata("armorPenetration").get(0).asInt() != 0) {
            damage += projectile.getMetadata("armorPenetration").get(0).asInt();
        }

        if (projectile.getMetadata("fireDamage").get(0).asInt() != 0) {
            receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1F, 1F);
            receiver.getWorld().playEffect(receiver.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8195);
            damage += projectile.getMetadata("fireDamage").get(0).asInt();
        }

        if (projectile.getMetadata("iceDamage").get(0).asInt() != 0) {
            receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1F, 1F);
            receiver.getWorld().playEffect(receiver.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8194);
            damage += projectile.getMetadata("iceDamage").get(0).asInt();
        }

        if (projectile.getMetadata("poisonDamage").get(0).asInt() != 0) {
            receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1F, 1F);
            receiver.getWorld().playEffect(receiver.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8196);
            damage += projectile.getMetadata("poisonDamage").get(0).asInt();
        }

        if (projectile.getMetadata("criticalHit").get(0).asInt() != 0) {
            if (new Random().nextInt(99) < projectile.getMetadata("criticalHit").get(0).asInt()) {
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
                HealthHandler.getInstance().healPlayerByAmount((Player) attacker, (int) lifeToHeal);
            } else if (attacker instanceof CraftLivingEntity) {
                if (attacker.hasMetadata("type")) {
                    HealthHandler.getInstance().healMonsterByAmount(attacker, (int) lifeToHeal);
                }
            }
        }

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
        for (NBTTagCompound nmsTag : nmsTags) {
            if (nmsTag == null) {
                damage += 0;
            } else {
                if (nmsTag.getDouble("damageMin") != 0) {
                    damage += (damage * (Utils.randInt((int) nmsTag.getDouble("damageMin"), (int) nmsTag.getDouble("damageMax")) / 100));
                }
            }
        }
        if (!(attacker instanceof Player)) {
            if (attacker.hasMetadata("attack")) {
                damage += (damage * (attacker.getMetadata("attack").get(0).asDouble() / 100));
            }
        } else {
            Player player = (Player) attacker;
            if (API.getGamePlayer(player) != null) {
                switch (projectile.getType()) {
                    case ARROW:
                        damage += (damage * (API.getGamePlayer(player).getStats().getBowDMG()));
                        break;
                    case SNOWBALL:
                        damage += (damage * (API.getGamePlayer(player).getStats().getStaffDMG()));
                        break;
                    default:
                        break;
                }
            }
        }
        if (isHitCrit) {
            if (attacker instanceof Player) {
                if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, attacker.getUniqueId()).toString())) {
                    attacker.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "                        *CRIT*");
                }
                ((Player) attacker).playSound(attacker.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.5F, 0.5F);
            }
            damage = damage * 2;
        }
        return Math.round(damage) + 1;
    }

    /**
     * Calculates the new damage based on the armor of the defender and the previous damage
     *
     * @param attacker
     * @param defender
     * @param defenderArmor
     * @since 1.0
     */
    public static double[] calculateArmorReduction(Entity attacker, Entity defender, ItemStack[] defenderArmor, double attackingDamage) {
        double damageAfterArmor = attackingDamage;
        int totalArmor = 0;
        boolean block = false, dodge = false;
        double totalArmorReduction;
        NBTTagCompound nmsTags[] = new NBTTagCompound[4];
        LivingEntity leDefender = (LivingEntity) defender;
        if (defenderArmor[3] != null && defenderArmor[3].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(defenderArmor[3]).getTag() != null) {
                nmsTags[0] = CraftItemStack.asNMSCopy(defenderArmor[3]).getTag();
                if (API.isPlayer(leDefender)) {
                    RepairAPI.subtractCustomDurability((Player) leDefender, defenderArmor[3], 1);
                }
            }
        }
        if (defenderArmor[2] != null && defenderArmor[2].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(defenderArmor[2]).getTag() != null) {
                nmsTags[1] = CraftItemStack.asNMSCopy(defenderArmor[2]).getTag();
                if (API.isPlayer(leDefender)) {
                    RepairAPI.subtractCustomDurability((Player) leDefender, defenderArmor[2], 1);
                }
            }
        }
        if (defenderArmor[1] != null && defenderArmor[1].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(defenderArmor[1]).getTag() != null) {
                nmsTags[2] = CraftItemStack.asNMSCopy(defenderArmor[1]).getTag();
                if (API.isPlayer(leDefender)) {
                    RepairAPI.subtractCustomDurability((Player) leDefender, defenderArmor[1], 1);
                }
            }
        }
        if (defenderArmor[0] != null && defenderArmor[0].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(defenderArmor[0]).getTag() != null) {
                nmsTags[3] = CraftItemStack.asNMSCopy(defenderArmor[0]).getTag();
                if (API.isPlayer(leDefender)) {
                    RepairAPI.subtractCustomDurability((Player) leDefender, defenderArmor[0], 1);
                }
            }
        }
        for (NBTTagCompound nmsTag : nmsTags) {
            if (nmsTag == null) {
                damageAfterArmor -= 0;
            } else {
                if (block || dodge) {
                    continue;
                }
                int blockChance = 0;
                if (nmsTag.getInt("block") != 0) {
                    nmsTag.getInt("block");
                }
                if (nmsTag.getInt("strength") != 0) {
                    blockChance += (nmsTag.getInt("strength") * 0.017);
                }
                if (defender instanceof Player) {
                    if (API.getGamePlayer((Player) defender) != null) {
                        blockChance += ((API.getGamePlayer((Player) defender).getStats()).getBlock() * 100);
                    }
                }
                if (attacker instanceof Player) {
                    net.minecraft.server.v1_9_R2.ItemStack nmsItem = (CraftItemStack.asNMSCopy(((Player) attacker).getEquipment().getItemInMainHand()));
                    if (nmsItem != null && nmsItem.getTag() != null) {
                        NBTTagCompound tag = nmsItem.getTag();
                        if (tag.getInt("accuracy") != 0) {
                            blockChance -= tag.getInt("accuracy");
                            if (blockChance < 0) {
                                blockChance = 0;
                            }
                        }
                    }
                }
                if (new Random().nextInt(99) < blockChance) {
                    if (leDefender.hasPotionEffect(PotionEffectType.SLOW)) {
                        leDefender.removePotionEffect(PotionEffectType.SLOW);
                    }
                    if (leDefender.hasPotionEffect(PotionEffectType.POISON)) {
                        leDefender.removePotionEffect(PotionEffectType.POISON);
                    }
                    if (leDefender.getFireTicks() > 0) {
                        leDefender.setFireTicks(0);
                    }
                    try {
                        ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.RED_DUST, defender.getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    damageAfterArmor = -2;
                    block = true;
                }
                int dodgeChance = 0;
                if (nmsTag.getInt("dodge") != 0) {
                    dodgeChance += nmsTag.getInt("dodge");
                }
                if (nmsTag.getInt("dexterity") != 0) {
                    dodgeChance += (nmsTag.getInt("dexterity") * 0.017);
                }
                if (defender instanceof Player) {
                    if (API.getGamePlayer((Player) defender) != null) {
                        dodgeChance += ((API.getGamePlayer((Player) defender).getStats()).getDodge() * 100);
                    }
                }
                if (attacker instanceof Player) {
                    if (((Player) attacker).getEquipment().getItemInMainHand() != null && ((Player) attacker).getEquipment().getItemInMainHand().getType() != Material.AIR) {
                        net.minecraft.server.v1_9_R2.ItemStack nmsItem = (CraftItemStack.asNMSCopy(((Player) attacker).getEquipment().getItemInMainHand()));
                        NBTTagCompound tag = nmsItem.getTag();
                        if (tag != null) {
                            if (tag.getInt("accuracy") != 0) {
                                dodgeChance -= tag.getInt("accuracy");
                                if (dodgeChance < 0) {
                                    dodgeChance = 0;
                                }
                            }
                        }
                    }
                }
                if (new Random().nextInt(99) < dodgeChance) {
                    if (leDefender.hasPotionEffect(PotionEffectType.SLOW)) {
                        leDefender.removePotionEffect(PotionEffectType.SLOW);
                    }
                    if (leDefender.hasPotionEffect(PotionEffectType.POISON)) {
                        leDefender.removePotionEffect(PotionEffectType.POISON);
                    }
                    if (leDefender.getFireTicks() > 0) {
                        leDefender.setFireTicks(0);
                    }
                    try {
                        ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CLOUD, defender.getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    damageAfterArmor = -1;
                    dodge = true;
                }
                if (nmsTag.getInt("strength") != 0) {
                    damageAfterArmor -= (attackingDamage / 100) * (nmsTag.getInt("strength") * 0.023D);
                }
                if (nmsTag.getInt("thorns") != 0) {
                    if (attacker instanceof Player) {
                        if (((Player) attacker).getGameMode() == GameMode.SURVIVAL) {
                            if (((Player) attacker).getEquipment().getItemInMainHand() != null && ((Player) attacker).getEquipment().getItemInMainHand().getType() != Material.AIR) {
                                net.minecraft.server.v1_9_R2.ItemStack nmsItem = (CraftItemStack.asNMSCopy(((Player) attacker).getEquipment().getItemInMainHand()));
                                NBTTagCompound tag = nmsItem.getTag();
                                if (tag != null) {
                                    int damageFromThorns = (int) (attackingDamage * (nmsTag.getInt("thorns") / 2));
                                    HealthHandler.getInstance().healPlayerByAmount((Player) attacker,
                                            -damageFromThorns);
                                }
                            }
                        }
                    }
                }
                if (nmsTag.getInt("fireResistance") != 0) {
                    if (leDefender.getFireTicks() > 0) {
                        try {
                            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPLASH, defender.getLocation(),
                                    new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        leDefender.setFireTicks(0);
                        damageAfterArmor -= nmsTag.getInt("fireResistance");
                    }
                }
                int armor = Utils.randInt(nmsTag.getInt("armorMin"), nmsTag.getInt("armorMax"));
                totalArmor += armor;
                damageAfterArmor -= (damageAfterArmor / 100) * armor;
            }
        }
        if (dodge) {
            totalArmorReduction = -1;
        } else if (block) {
            totalArmorReduction = -2;
        } else {
            totalArmorReduction = attackingDamage - damageAfterArmor;
            if (leDefender.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                int potionTier = 1;
                for (PotionEffect pe : leDefender.getActivePotionEffects()) {
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
        }
        return new double[]{Math.round(totalArmorReduction), totalArmor};
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
                break;
            case 3:
                projectile = player.launchProjectile(EnderPearl.class);
                projectile.setVelocity(projectile.getVelocity().multiply(1.75));
                break;
            case 4:
                projectile = player.launchProjectile(LargeFireball.class);
                projectile.setVelocity(projectile.getVelocity().multiply(2));
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
        MetadataUtils.registerProjectileMetadata(tag, projectile, weaponTier);
    }

    public static void fireBowProjectile(Player player, ItemStack itemStack, NBTTagCompound tag) {
        RepairAPI.subtractCustomDurability(player, itemStack, 1);
        int weaponTier = tag.getInt("itemTier");
        Projectile projectile = player.launchProjectile(Arrow.class);
        //TODO: Tipped arrows for Fire/Ice/Poison dmg.
        //Projectile projectile1 = player.launchProjectile(TippedArrow.class);
        //((TippedArrow) projectile).addCustomEffect(new PotionEffect(PotionEffectType.JUMP, 1, 1), true);
        projectile.setBounce(false);
        projectile.setVelocity(projectile.getVelocity().multiply(1.1));
        EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(itemStack));
        projectile.setShooter(player);
        EntityArrow eArrow = ((CraftArrow) projectile).getHandle();
        eArrow.fromPlayer = EntityArrow.PickupStatus.DISALLOWED;
        MetadataUtils.registerProjectileMetadata(tag, projectile, weaponTier);
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
                break;
            case 3:
                projectile = livingEntity.launchProjectile(EnderPearl.class);
                vector.multiply(1.75);
                break;
            case 4:
                projectile = livingEntity.launchProjectile(LargeFireball.class);
                vector.multiply(2);
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
        MetadataUtils.registerProjectileMetadata(tag, projectile, weaponTier);
    }

    public static void fireArrowFromMob(CraftLivingEntity livingEntity, NBTTagCompound tag, LivingEntity target) {
        if (!(target instanceof Player)) return;
        org.bukkit.util.Vector vector = target.getLocation().toVector().subtract(livingEntity.getLocation().toVector()).normalize();
        int weaponTier = tag.getInt("itemTier");
        Projectile projectile = livingEntity.launchProjectile(Arrow.class);
        projectile.setBounce(false);
        vector.multiply(1.25);
        projectile.setVelocity(vector);
        projectile.setShooter(livingEntity);
        EntityArrow eArrow = ((CraftArrow) projectile).getHandle();
        eArrow.fromPlayer = EntityArrow.PickupStatus.DISALLOWED;
        MetadataUtils.registerProjectileMetadata(tag, projectile, weaponTier);
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
    public static void setArmorIgnoreBonus(Entity ent, float bonusPercent) {
        ent.setMetadata("armorIgnoreBonus", new FixedMetadataValue(DungeonRealms.getInstance(), bonusPercent));
    }

    public static float getArmorIgnoreBonus(Entity ent) {
        return ent.hasMetadata("armorIgnoreBonus") ? ent.getMetadata("armorIgnoreBonus").get(0).asFloat() : 0;
    }

    public static void removeArmorIgnoreBonus(Entity ent) {
        if (ent.hasMetadata("armorIgnoreBonus")) ent.removeMetadata("armorIgnoreBonus", DungeonRealms.getInstance());
    }
}
