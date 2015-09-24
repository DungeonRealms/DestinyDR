package net.dungeonrealms.items;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * Created by Kieran on 9/21/2015.
 */
public class DamageAPI {

    /**
     * Calculates the weapon damage based on the nbt tag of an item, the attacker and receiver
     * @param attacker
     * @param receiver
     * @param tag
     * @since 1.0
     */
    public static double calculateWeaponDamage(LivingEntity attacker, Entity receiver, NBTTagCompound tag) {
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
                if (nmsTag.getInt("damage") != 0) {
                    damage += nmsTag.getInt("damage");
                    Bukkit.broadcastMessage("DAMAGE INCREASED DUE TO ARMOR");
                }
            }
        }
        ItemStack ourItem = entityEquipment.getItemInHand();
        int weaponTier = new Attribute(ourItem).getItemTier().getId();
        damage += tag.getDouble("damage");
        boolean isHitCrit = false;
        if (receiver instanceof Player) {
            if (tag.getInt("vsPlayers") != 0) {
                damage += tag.getInt("vsPlayers");
            }
        } else {
            if (receiver.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                if (tag.getInt("vsMonsters") != 0) {
                    damage += tag.getInt("vsMonsters");
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

        if (tag.getInt("strength") != 0) {
            damage += (damage * (tag.getInt("strength") * 0.023D) / 100D);
        }

        if (tag.getInt("vitality") != 0) {
            damage += (damage * (tag.getInt("vitality") * 0.023D) / 100D);
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
                case 0:
                    leReceiver.setFireTicks(15);
                    break;
                case 1:
                    leReceiver.setFireTicks(25);
                    break;
                case 2:
                    leReceiver.setFireTicks(30);
                    break;
                case 3:
                    leReceiver.setFireTicks(35);
                    break;
                case 4:
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
                case 0:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0));
                    break;
                case 1:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                    break;
                case 2:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
                    break;
                case 3:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                    break;
                case 4:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 1));
                    break;
            }
            damage += tag.getInt("iceDamage");
        }

        if (tag.getInt("poisonDamage") != 0) {
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.TOWN_AURA, receiver.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            switch (weaponTier) {
                case 0:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30, 0));
                    break;
                case 1:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                    break;
                case 2:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 0));
                    break;
                case 3:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                    break;
                case 4:
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

        if (tag.getInt("lifesteal") != 0) {
            //TODO: LIFESTEAL WHEN WE HAVE OUR CUSTOM HP SHIT DONE
        }

        if (tag.getInt("blind") != 0) {
            boolean canTargetBeBlinded = false;
            if (new Random().nextInt(99) < tag.getInt("blind")) {
                if (!leReceiver.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                    canTargetBeBlinded = true;
                } else if (leReceiver.hasMetadata("blind")) {
                    long last_blind = leReceiver.getMetadata("blind").get(0).asLong();
                    if ((System.currentTimeMillis() - last_blind) <= (10 * 1000)) {
                        // Less than 10 seconds, do nothing.
                        canTargetBeBlinded = false;
                    }
                }

                if (canTargetBeBlinded) {
                    try {
                        ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SMALL_SMOKE, receiver.getLocation(),
                                new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    switch (weaponTier) {
                        case 0:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 1));
                            break;
                        case 1:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
                            break;
                        case 2:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 1));
                            break;
                        case 3:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                            break;
                        case 4:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                            break;
                    }
                    leReceiver.setMetadata("blind", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
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
        if (isHitCrit) {
            damage = damage * 1.5;
        }
        return damage;
    }

    /**
     * Calculates the weapon damage based on the metadata of the projectile, the attacker and receiver
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
                if (nmsTag.getInt("damage") != 0) {
                    damage += nmsTag.getInt("damage");
                    Bukkit.broadcastMessage("DAMAGE INCREASED DUE TO ARMOR");
                }
            }
        }
        damage += projectile.getMetadata("damage").get(0).asDouble();
        boolean isHitCrit = false;
        if (receiver instanceof Player) {
            if (projectile.getMetadata("vsPlayers").get(0).asInt() != 0) {
                damage += projectile.getMetadata("vsPlayers").get(0).asInt();
            }
        } else {
            if (receiver.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                if (projectile.getMetadata("vsMonsters").get(0).asInt() != 0) {
                    damage += projectile.getMetadata("vsMonsters").get(0).asInt();
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

        if (projectile.getMetadata("dexterity").get(0).asInt() != 0) {
            damage += (damage * (projectile.getMetadata("dexterity").get(0).asInt() * 0.023D) / 100D);
        }

        if (projectile.getMetadata("intellect").get(0).asInt() != 0) {
            damage += (damage * (projectile.getMetadata("intellect").get(0).asInt() * 0.02D) / 100D);
        }

        LivingEntity leReceiver = (LivingEntity) receiver;
        if (projectile.getMetadata("fireDamage").get(0).asInt() != 0) {
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.FLAME, receiver.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            switch (projectile.getMetadata("tier").get(0).asInt()) {
                case 0:
                    leReceiver.setFireTicks(15);
                    break;
                case 1:
                    leReceiver.setFireTicks(25);
                    break;
                case 2:
                    leReceiver.setFireTicks(30);
                    break;
                case 3:
                    leReceiver.setFireTicks(35);
                    break;
                case 4:
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
            switch (projectile.getMetadata("tier").get(0).asInt()) {
                case 0:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0));
                    break;
                case 1:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                    break;
                case 2:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
                    break;
                case 3:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                    break;
                case 4:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 1));
                    break;
            }
            damage +=  projectile.getMetadata("iceDamage").get(0).asInt();
        }

        if (projectile.getMetadata("poisonDamage").get(0).asInt() != 0) {
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.TOWN_AURA, receiver.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            switch (projectile.getMetadata("tier").get(0).asInt()) {
                case 0:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30, 0));
                    break;
                case 1:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                    break;
                case 2:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 0));
                    break;
                case 3:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                    break;
                case 4:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 1));
                    break;
            }
            damage +=  projectile.getMetadata("poisonDamage").get(0).asInt();
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

        if (projectile.getMetadata("lifesteal").get(0).asInt() != 0) {
            //TODO: LIFESTEAL WHEN WE HAVE OUR CUSTOM HP SHIT DONE
        }

        if (projectile.getMetadata("blind").get(0).asInt() != 0) {
            boolean canTargetBeBlinded = false;
            if (new Random().nextInt(99) < projectile.getMetadata("blind").get(0).asInt()) {
                if (!leReceiver.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                    canTargetBeBlinded = true;
                } else if (leReceiver.hasMetadata("blind")) {
                    long last_blind = leReceiver.getMetadata("blind").get(0).asLong();
                    if ((System.currentTimeMillis() - last_blind) <= (10 * 1000)) {
                        // Less than 10 seconds, do nothing.
                        canTargetBeBlinded = false;
                    }
                }

                if (canTargetBeBlinded) {
                    try {
                        ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SMALL_SMOKE, receiver.getLocation(),
                                new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    switch (projectile.getMetadata("tier").get(0).asInt()) {
                        case 0:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 1));
                            break;
                        case 1:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
                            break;
                        case 2:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 1));
                            break;
                        case 3:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                            break;
                        case 4:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                            break;
                    }
                    leReceiver.setMetadata("blind", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
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
        if (isHitCrit) {
            damage = damage * 1.5;
        }
        Bukkit.broadcastMessage("Final Attack damage: " + damage);
        return damage;
    }

    /**
     * Calculates the new damage based on the armor of the defender and the previous damage
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
            }
        }
        if (defenderArmor[2].getType() != null && defenderArmor[2].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(defenderArmor[2]).getTag() != null) {
                nmsTags[1] = CraftItemStack.asNMSCopy(defenderArmor[2]).getTag();
            }
        }
        if (defenderArmor[1].getType() != null && defenderArmor[1].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(defenderArmor[1]).getTag() != null) {
                nmsTags[2] = CraftItemStack.asNMSCopy(defenderArmor[1]).getTag();
            }
        }
        if (defenderArmor[0] != null && defenderArmor[0].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(defenderArmor[0]).getTag() != null) {
                nmsTags[3] = CraftItemStack.asNMSCopy(defenderArmor[0]).getTag();
            }
        }
        for (int i = 0; i < nmsTags.length; i++) {
            if (nmsTags[i] == null) {
                damageToBlock[i] += 0;
            } else {
                damageToBlock[i]= nmsTags[i].getInt("armor");
                if (nmsTags[i].getInt("block") != 0) {
                    int blockChance = nmsTags[0].getInt("block");
                    if (nmsTags[i].getInt("strength") != 0) {
                        blockChance += (blockChance * (nmsTags[i].getInt("strength") * 0.017));
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
                if (nmsTags[i].getInt("dodge") != 0) {
                    int dodgeChance = nmsTags[i].getInt("dodge");
                    if (nmsTags[i].getInt("dexterity") != 0) {
                        dodgeChance += (dodgeChance * (nmsTags[i].getInt("dexterity") * 0.017));
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
                if (nmsTags[i].getInt("strength") != 0) {
                    damageToBlock[i] += (nmsTags[i].getInt("strength") * 0.023D) / 100D;
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
            }
        }
        if (damageToBlock[0] == -1 || damageToBlock[1] == -1 || damageToBlock[2] == -1 || damageToBlock[3] == -1) {
            totalArmorReduction = -1;
        } else if (damageToBlock[0] == -2 || damageToBlock[1] == -2 || damageToBlock[2] == -2 || damageToBlock[3] == -2) {
            totalArmorReduction = -2;
        } else {
            totalArmorReduction = damageToBlock[0] + damageToBlock[1] + damageToBlock[2] + damageToBlock[3];
        }
        return totalArmorReduction;
    }
}
