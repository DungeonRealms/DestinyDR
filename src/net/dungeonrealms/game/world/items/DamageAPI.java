package net.dungeonrealms.game.world.items;

import net.dungeonrealms.API;
import net.dungeonrealms.game.handlers.EnergyHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mechanics.SoundAPI;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.minecraft.server.v1_8_R3.EntityMonster;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftMonster;
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
        double damage;
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
        damage = Utils.randInt(tag.getInt("damageMin"), tag.getInt("damageMax"));
        boolean isHitCrit = false;
        if (API.isPlayer(receiver)) {
            if (tag.getDouble("vsPlayers") != 0) {
                damage += ((tag.getDouble("vsPlayers") / 100) * damage);
            }
            if(attacker.hasMetadata("type")) {
                if(attacker.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                    EntityMonster nms = ((CraftMonster)attacker).getHandle();
                    if(nms instanceof net.dungeonrealms.game.world.entities.types.monsters.Monster){
                        net.dungeonrealms.game.world.entities.types.monsters.Monster mons = (net.dungeonrealms.game.world.entities.types.monsters.Monster)nms;
                        mons.onMonsterAttack((Player) receiver);
                    }
                }
            }
        } else {
            if (receiver.hasMetadata("type")) {
                if (receiver.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                    if (tag.getDouble("vsMonsters") != 0) {
                        damage += ((tag.getDouble("vsMonsters") / 100) * damage);
                    }
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
            damage += (damage / 100) * (tag.getDouble("strength") * 0.023D);
        }

        if (tag.getDouble("vitality") != 0) {
            damage += (damage / 100) * (tag.getDouble("vitality") * 0.023D);
        }

        if (tag.getInt("fireDamage") != 0) {
            SoundAPI.getInstance().playSoundAtLocation("game.potion.smash", receiver.getLocation(), 8);
            receiver.getWorld().playEffect(receiver.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8195);
            damage += tag.getInt("fireDamage");
        }

        if (tag.getInt("iceDamage") != 0) {
            SoundAPI.getInstance().playSoundAtLocation("game.potion.smash", receiver.getLocation(), 8);
            receiver.getWorld().playEffect(receiver.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8194);
            damage += tag.getInt("iceDamage");
        }

        if (tag.getInt("poisonDamage") != 0) {
            SoundAPI.getInstance().playSoundAtLocation("game.potion.smash", receiver.getLocation(), 8);
            receiver.getWorld().playEffect(receiver.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8196);
            damage += tag.getInt("poisonDamage");
        }

        int critHit = 0;
        if (tag.getInt("criticalHit") != 0) {
            critHit += tag.getInt("criticalHit");
        }
        if (attacker.getEquipment().getItemInHand() != null) {
            if (new Attribute(attacker.getEquipment().getItemInHand()).getItemType() == Item.ItemType.AXE) {
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
                switch (new Attribute(((Player) attacker).getItemInHand()).getItemType()) {
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
                ((Player) attacker).playSound(attacker.getLocation(), Sound.WOOD_CLICK, 1.5F, 0.5F);
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
            SoundAPI.getInstance().playSoundAtLocation("game.potion.smash", receiver.getLocation(), 8);
            receiver.getWorld().playEffect(receiver.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8195);
            damage += projectile.getMetadata("fireDamage").get(0).asInt();
        }

        if (projectile.getMetadata("iceDamage").get(0).asInt() != 0) {
            SoundAPI.getInstance().playSoundAtLocation("game.potion.smash", receiver.getLocation(), 8);
            receiver.getWorld().playEffect(receiver.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8194);
            damage += projectile.getMetadata("iceDamage").get(0).asInt();
        }

        if (projectile.getMetadata("poisonDamage").get(0).asInt() != 0) {
            SoundAPI.getInstance().playSoundAtLocation("game.potion.smash", receiver.getLocation(), 8);
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
                ((Player) attacker).playSound(attacker.getLocation(), Sound.WOOD_CLICK, 1.5F, 0.5F);
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
                    net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(((Player) attacker).getItemInHand()));
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
                            if (((Player) attacker).getItemInHand() != null && ((Player) attacker).getItemInHand().getType() != Material.AIR) {
                                net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(((Player) attacker).getItemInHand()));
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
        EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(itemStack));
        projectile.setShooter(player);
        MetadataUtils.registerProjectileMetadata(tag, projectile, weaponTier);
    }

    public static void fireStaffProjectileMob(CraftLivingEntity livingEntity, NBTTagCompound tag, LivingEntity target) {
        if (!(target instanceof Player)) return;
        org.bukkit.util.Vector vector = target.getLocation().toVector().subtract(livingEntity.getLocation().toVector()).normalize();
        int weaponTier = tag.getInt("itemTier");
        Projectile projectile = livingEntity.launchProjectile(Snowball.class);
        switch (weaponTier) {
            case 1:
                vector.multiply(1.05);
                break;
            case 2:
                vector.multiply(1.25);
                break;
            case 3:
                vector.multiply(1.5);
                break;
            case 4:
                vector.multiply(2);
                break;
            case 5:
                vector.multiply(2.5);
                break;
        }
        projectile.setVelocity(vector);
        projectile.setShooter(livingEntity);
        MetadataUtils.registerProjectileMetadata(tag, projectile, weaponTier);
    }

    public static void fireArrowFromMob(CraftLivingEntity livingEntity, NBTTagCompound tag, LivingEntity target) {
        if (!(target instanceof Player)) return;
        org.bukkit.util.Vector vector = target.getLocation().toVector().subtract(livingEntity.getLocation().toVector()).normalize();
        int weaponTier = tag.getInt("itemTier");
        Projectile projectile = livingEntity.launchProjectile(Arrow.class);
        vector.multiply(1.5);
        projectile.setVelocity(vector);
        projectile.setShooter(livingEntity);
        MetadataUtils.registerProjectileMetadata(tag, projectile, weaponTier);
    }
}
