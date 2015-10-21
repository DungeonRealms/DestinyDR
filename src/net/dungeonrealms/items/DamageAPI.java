package net.dungeonrealms.items;

import net.dungeonrealms.API;
import net.dungeonrealms.handlers.EnergyHandler;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
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
        EntityEquipment entityEquipment = attacker.getEquipment();
        if (API.isPlayer(attacker)) {
            RepairAPI.subtractCustomDurability((Player) attacker, attacker.getEquipment().getItemInHand(), 1);
        }
        ItemStack[] attackerArmor = entityEquipment.getArmorContents();
        NBTTagCompound nmsTags[] = new NBTTagCompound[4];
        double damage = 0;
        if (attackerArmor[3].getType() != null && attackerArmor[3].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(attackerArmor[3]).getTag() != null) {
                nmsTags[0] = CraftItemStack.asNMSCopy(attackerArmor[3]).getTag();
            }
        }
        if (attackerArmor[2].getType() != null && attackerArmor[2].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(attackerArmor[2]).getTag() != null) {
                nmsTags[1] = CraftItemStack.asNMSCopy(attackerArmor[2]).getTag();
            }
        }
        if (attackerArmor[1].getType() != null && attackerArmor[1].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(attackerArmor[1]).getTag() != null) {
                nmsTags[2] = CraftItemStack.asNMSCopy(attackerArmor[1]).getTag();
            }
        }
        if (attackerArmor[0] != null && attackerArmor[0].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(attackerArmor[0]).getTag() != null) {
                nmsTags[3] = CraftItemStack.asNMSCopy(attackerArmor[0]).getTag();
            }
        }
        int weaponTier = tag.getInt("itemTier");
        int damageRandomizer = ItemGenerator.getRandomDamageVariable(weaponTier);
        damage = (double) Utils.randInt((int) Math.round(tag.getDouble("damage") - (tag.getDouble("damage") / damageRandomizer)), (int) Math.round(tag.getDouble("damage") + (tag.getDouble("damage") / (damageRandomizer - 1))));
        boolean isHitCrit = false;
        if (API.isPlayer(receiver)) {
            if (tag.getDouble("vsPlayers") != 0) {
                damage += ((tag.getDouble("vsPlayers") / 100) * damage);
            }
        } else {
            if (receiver.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                if (tag.getDouble("vsMonsters") != 0) {
                    damage += ((tag.getDouble("vsMonsters") / 100) * damage);
                }
            }
        }

        if (tag.getInt("pureDamage") != 0) {
            damage += tag.getInt("pureDamage");
        }

        if (tag.getInt("armorPenetration") != 0) {
            damage += tag.getInt("armorPenetration");
        }

        if (tag.getInt("accuracy") != 0) {
            damage += tag.getInt("accuracy");
        }

        if (tag.getDouble("strength") != 0) {
            damage += (damage * (tag.getDouble("strength") * 0.023D) / 100D);
        }

        if (tag.getDouble("vitality") != 0) {
            damage += (damage * (tag.getDouble("vitality") * 0.023D) / 100D);
        }

        LivingEntity leReceiver = (LivingEntity) receiver;
        if (tag.getInt("fireDamage") != 0) {
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.FLAME, receiver.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            switch (weaponTier) {
                case 1:
                    leReceiver.setFireTicks(15);
                    break;
                case 2:
                    leReceiver.setFireTicks(25);
                    break;
                case 3:
                    leReceiver.setFireTicks(30);
                    break;
                case 4:
                    leReceiver.setFireTicks(35);
                    break;
                case 5:
                    leReceiver.setFireTicks(40);
                    break;
            }
            damage += tag.getInt("fireDamage");
        }

        if (tag.getInt("iceDamage") != 0) {
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SNOWBALL_POOF, receiver.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            switch (weaponTier) {
                case 1:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0));
                    break;
                case 2:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                    break;
                case 3:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
                    break;
                case 4:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                    break;
                case 5:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 1));
                    break;
            }
            damage += tag.getInt("iceDamage");
        }

        if (tag.getInt("poisonDamage") != 0) {
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.HAPPY_VILLAGER, receiver.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            switch (weaponTier) {
                case 1:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30, 0));
                    break;
                case 2:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                    break;
                case 3:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 0));
                    break;
                case 4:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                    break;
                case 5:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 1));
                    break;
            }
            damage += tag.getInt("poisonDamage");
        }

        if (tag.getInt("criticalHit") != 0) {
            if (new Random().nextInt(99) < tag.getInt("criticalHit")) {
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.MAGIC_CRIT, receiver.getLocation(),
                            new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                isHitCrit = true;
            }
        }

        if (tag.getDouble("lifesteal") != 0) {
            double lifeToHeal = ((tag.getDouble("lifesteal") / 100) * damage);
            if (API.isPlayer(attacker)) {
                HealthHandler.getInstance().healPlayerByAmount((Player) attacker, (int) lifeToHeal);
            } else if (attacker instanceof Monster) {
                HealthHandler.getInstance().healMonsterByAmount(attacker, (int) lifeToHeal);
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
                if (nmsTag.getDouble("damage") != 0) {
                    damage += (damage * (nmsTag.getDouble("damage") / 100));
                }
            }
        }
        if (isHitCrit) {
            damage = damage * 1.5;
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
        if (attackerArmor[3].getType() != null && attackerArmor[3].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(attackerArmor[3]).getTag() != null) {
                nmsTags[0] = CraftItemStack.asNMSCopy(attackerArmor[3]).getTag();
            }
        }
        if (attackerArmor[2].getType() != null && attackerArmor[2].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(attackerArmor[2]).getTag() != null) {
                nmsTags[1] = CraftItemStack.asNMSCopy(attackerArmor[2]).getTag();
            }
        }
        if (attackerArmor[1].getType() != null && attackerArmor[1].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(attackerArmor[1]).getTag() != null) {
                nmsTags[2] = CraftItemStack.asNMSCopy(attackerArmor[1]).getTag();
            }
        }
        if (attackerArmor[0] != null && attackerArmor[0].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(attackerArmor[0]).getTag() != null) {
                nmsTags[3] = CraftItemStack.asNMSCopy(attackerArmor[0]).getTag();
            }
        }
        for (NBTTagCompound nmsTag : nmsTags) {
            if (nmsTag == null) {
                damage += 0;
            } else {
                if (nmsTag.getDouble("damage") != 0) {
                    damage += nmsTag.getDouble("damage");
                }
            }
        }
        int damageRandomizer = ItemGenerator.getRandomDamageVariable(projectile.getMetadata("itemTier").get(0).asInt());
        damage = (double) Utils.randInt(((int) Math.round(projectile.getMetadata("damage").get(0).asDouble() - projectile.getMetadata("damage").get(0).asDouble() / damageRandomizer)),
                (int) Math.round(projectile.getMetadata("damage").get(0).asDouble() + projectile.getMetadata("damage").get(0).asDouble() / (damageRandomizer - 1)));
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

        if (projectile.getMetadata("accuracy").get(0).asInt() != 0) {
            damage += projectile.getMetadata("accuracy").get(0).asInt();
        }

        if (projectile.getMetadata("dexterity").get(0).asDouble() != 0) {
            damage += (damage * (projectile.getMetadata("dexterity").get(0).asDouble() * 0.023D) / 100D);
        }

        if (projectile.getMetadata("intellect").get(0).asDouble() != 0) {
            damage += (damage * (projectile.getMetadata("intellect").get(0).asDouble() * 0.02D) / 100D);
        }

        LivingEntity leReceiver = (LivingEntity) receiver;
        if (projectile.getMetadata("fireDamage").get(0).asInt() != 0) {
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.FLAME, receiver.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            switch (projectile.getMetadata("itemTier").get(0).asInt()) {
                case 1:
                    leReceiver.setFireTicks(15);
                    break;
                case 2:
                    leReceiver.setFireTicks(25);
                    break;
                case 3:
                    leReceiver.setFireTicks(30);
                    break;
                case 4:
                    leReceiver.setFireTicks(35);
                    break;
                case 5:
                    leReceiver.setFireTicks(40);
                    break;
            }
            damage += projectile.getMetadata("fireDamage").get(0).asInt();
        }

        if (projectile.getMetadata("iceDamage").get(0).asInt() != 0) {
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SNOWBALL_POOF, receiver.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            switch (projectile.getMetadata("itemTier").get(0).asInt()) {
                case 1:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0));
                    break;
                case 2:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                    break;
                case 3:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
                    break;
                case 4:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                    break;
                case 5:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 1));
                    break;
            }
            damage += projectile.getMetadata("iceDamage").get(0).asInt();
        }

        if (projectile.getMetadata("poisonDamage").get(0).asInt() != 0) {
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.HAPPY_VILLAGER, receiver.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            switch (projectile.getMetadata("itemTier").get(0).asInt()) {
                case 1:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30, 0));
                    break;
                case 2:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                    break;
                case 3:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 0));
                    break;
                case 4:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                    break;
                case 5:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 1));
                    break;
            }
            damage += projectile.getMetadata("poisonDamage").get(0).asInt();
        }

        if (projectile.getMetadata("criticalHit").get(0).asInt() != 0) {
            if (new Random().nextInt(99) < projectile.getMetadata("criticalHit").get(0).asInt()) {
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.MAGIC_CRIT, receiver.getLocation(),
                            new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                isHitCrit = true;
            }
        }

        if (projectile.getMetadata("lifesteal").get(0).asDouble() != 0) {
            double lifeToHeal = ((projectile.getMetadata("lifesteal").get(0).asDouble() / 100) * damage);
            if (API.isPlayer(attacker)) {
                HealthHandler.getInstance().healPlayerByAmount((Player) attacker, (int) lifeToHeal);
            } else if (attacker instanceof Monster) {
                HealthHandler.getInstance().healMonsterByAmount(attacker, (int) lifeToHeal);
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
        if (isHitCrit) {
            damage = damage * 1.5;
        }
        return Math.round(damage);
    }

    /**
     * Calculates the new damage based on the armor of the defender and the previous damage
     *
     * @param attacker
     * @param defender
     * @param defenderArmor
     * @since 1.0
     */
    public static double calculateArmorReduction(Entity attacker, Entity defender, ItemStack[] defenderArmor) {
        double damageToBlock[] = new double[4];
        double totalArmorReduction;
        NBTTagCompound nmsTags[] = new NBTTagCompound[4];
        LivingEntity leDefender = (LivingEntity) defender;
        if (defenderArmor[3].getType() != null && defenderArmor[3].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(defenderArmor[3]).getTag() != null) {
                nmsTags[0] = CraftItemStack.asNMSCopy(defenderArmor[3]).getTag();
                if (API.isPlayer(leDefender)) {
                    RepairAPI.subtractCustomDurability((Player) leDefender, defenderArmor[3], 1);
                }
            }
        }
        if (defenderArmor[2].getType() != null && defenderArmor[2].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(defenderArmor[2]).getTag() != null) {
                nmsTags[1] = CraftItemStack.asNMSCopy(defenderArmor[2]).getTag();
                if (API.isPlayer(leDefender)) {
                    RepairAPI.subtractCustomDurability((Player) leDefender, defenderArmor[2], 1);
                }
            }
        }
        if (defenderArmor[1].getType() != null && defenderArmor[1].getType() != Material.AIR) {
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
        for (int i = 0; i < nmsTags.length; i++) {
            if (nmsTags[i] == null) {
                damageToBlock[i] += 0;
            } else {
                if (attacker.getType() != EntityType.ARROW) {
                    if (nmsTags[i].getInt("block") != 0) {
                        int blockChance = nmsTags[0].getInt("block");
                        if (nmsTags[i].getInt("strength") != 0) {
                            blockChance += (blockChance * (nmsTags[i].getInt("strength") * 0.017));
                        }
                        if (API.isPlayer(attacker)) {
                            net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(((Player) attacker).getItemInHand()));
                            NBTTagCompound tag = nmsItem.getTag();
                            if (tag.getInt("accuracy") != 0) {
                                blockChance -= tag.getInt("accuracy");
                                if (blockChance < 0) {
                                    blockChance = 0;
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
                                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.RED_DUST, defender.getLocation(),
                                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            damageToBlock[i] = -2;
                        }
                    }
                }
                if (attacker.getType() != EntityType.ARROW) {
                    if (nmsTags[i].getInt("dodge") != 0) {
                        int dodgeChance = nmsTags[i].getInt("dodge");
                        if (nmsTags[i].getInt("dexterity") != 0) {
                            dodgeChance += (dodgeChance * (nmsTags[i].getInt("dexterity") * 0.017));
                        }
                        if (API.isPlayer(attacker)) {
                            if (((Player) attacker).getItemInHand() != null && ((Player) attacker).getItemInHand().getType() != Material.AIR) {
                                net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(((Player) attacker).getItemInHand()));
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
                                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CLOUD, defender.getLocation(),
                                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            damageToBlock[i] = -1;
                        }
                    }
                }
                if (nmsTags[i].getInt("strength") != 0) {
                    damageToBlock[i] += (nmsTags[i].getInt("strength") * 0.023D) / 100D;
                }
                if (nmsTags[i].getInt("thorns") != 0) {
                    int damageFromThorns = 0;
                    switch (nmsTags[i].getInt("armorTier")) {
                        case 1:
                            damageFromThorns += 2 + nmsTags[i].getInt("thorns");
                            break;
                        case 2:
                            damageFromThorns += 10 + nmsTags[i].getInt("thorns");
                            break;
                        case 3:
                            damageFromThorns += 25 + nmsTags[i].getInt("thorns");
                            break;
                        case 4:
                            damageFromThorns += 30 + nmsTags[i].getInt("thorns");
                            break;
                        case 5:
                            damageFromThorns += 60 + nmsTags[i].getInt("thorns");
                            break;
                    }
                    if (API.isPlayer(attacker)) {
                        //HealthHandler.getInstance().handlePlayerBeingDamaged((Player) attacker, defender, damageFromThorns);
                    }
                    if (attacker instanceof Monster) {
                       //HealthHandler.getInstance().handleMonsterBeingDamaged((LivingEntity) attacker, damageFromThorns);
                    }
                }
                if (nmsTags[i].getInt("fireResistance") != 0) {
                    if (leDefender.getFireTicks() > 0) {
                        try {
                            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPLASH, defender.getLocation(),
                                    new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        leDefender.setFireTicks(0);
                        damageToBlock[i] += nmsTags[i].getInt("fireResistance");
                    }
                }
                damageToBlock[i] += (damageToBlock[i] * (nmsTags[i].getInt("armor") / 100));
            }
        }
        if (damageToBlock[0] == -1 || damageToBlock[1] == -1 || damageToBlock[2] == -1 || damageToBlock[3] == -1) {
            totalArmorReduction = -1;
        } else if (damageToBlock[0] == -2 || damageToBlock[1] == -2 || damageToBlock[2] == -2 || damageToBlock[3] == -2) {
            totalArmorReduction = -2;
        } else {
            totalArmorReduction = damageToBlock[0] + damageToBlock[1] + damageToBlock[2] + damageToBlock[3];
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
        return Math.round(totalArmorReduction);
    }

    public static int calculatePlayerLuck(Player player) {
        int playerLuck[] = new int[4];
        int totalLuck;
        NBTTagCompound nmsTags[] = new NBTTagCompound[4];
        EntityEquipment playerEquipment = player.getEquipment();
        ItemStack[] playerArmor = playerEquipment.getArmorContents();
        if (playerArmor[3].getType() != null && playerArmor[3].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[3]).getTag() != null) {
                nmsTags[0] = CraftItemStack.asNMSCopy(playerArmor[3]).getTag();
            }
        }
        if (playerArmor[2].getType() != null && playerArmor[2].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[2]).getTag() != null) {
                nmsTags[1] = CraftItemStack.asNMSCopy(playerArmor[2]).getTag();
            }
        }
        if (playerArmor[1].getType() != null && playerArmor[1].getType() != Material.AIR) {
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
                playerLuck[i] += 0;
            } else {
                if (nmsTags[i].getInt("luck") != 0) {
                    playerLuck[i] = nmsTags[i].getInt("luck");
                }
            }
        }
        totalLuck = playerLuck[0] + playerLuck[1] + playerLuck[2] + playerLuck[3];

        return Math.round(totalLuck);
    }

    public static void fireStaffProjectile(Player player, ItemStack itemStack, NBTTagCompound tag) {
        RepairAPI.subtractCustomDurability(player, itemStack, 1);
        EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(itemStack));
        int weaponTier = tag.getInt("itemTier");
        Projectile projectile = player.launchProjectile(Snowball.class);
        switch (weaponTier) {
            case 1:
                projectile.setVelocity(projectile.getVelocity().multiply(1.05));
                break;
            case 2:
                projectile.setVelocity(projectile.getVelocity().multiply(1.25));
                break;
            case 3:
                projectile.setVelocity(projectile.getVelocity().multiply(1.50));
                break;
            case 4:
                projectile.setVelocity(projectile.getVelocity().multiply(2.0));
                break;
            case 5:
                projectile.setVelocity(projectile.getVelocity().multiply(2.5));
                break;
        }
        MetadataUtils.registerProjectileMetadata(tag, projectile, weaponTier);
    }
}
