package net.dungeonrealms.game.world.entities.utils;

import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.EnderCrystal;
import net.dungeonrealms.game.world.spawning.BuffManager;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Kieran on 9/18/2015.
 */
public class BuffUtils {

    /**
     * Adds the buff to a Endercrystal entity, takes a player UUID.
     *
     * @param uuid
     * @since 1.0
     */
    public static EnderCrystal spawnBuff(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        World world = ((CraftWorld) player.getWorld()).getHandle();
        EnderCrystal enderCrystal = new EnderCrystal(world, EnumEntityType.BUFF);
        Block b = Bukkit.getWorlds().get(0).getHighestBlockAt((int) player.getLocation().getX(), (int) player.getLocation().getZ());
        if (b.getLocation().distanceSquared(player.getLocation()) < 25) {
            enderCrystal.setLocation(b.getX(), b.getY(), b.getZ(), 0, 0);
            world.addEntity(enderCrystal, CreatureSpawnEvent.SpawnReason.CUSTOM);
            enderCrystal.setLocation(b.getX(), b.getY(), b.getZ(), 0, 0);
        } else {
            enderCrystal.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
            world.addEntity(enderCrystal, CreatureSpawnEvent.SpawnReason.CUSTOM);
            enderCrystal.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 1f, 63f);
        MetadataUtils.registerBuffMetadata(enderCrystal, getRandomPotionEffect(), 10, 600);
        return enderCrystal;
    }

    public static PotionEffectType getRandomPotionEffect() {
        switch (new Random().nextInt(9)) {
            case 0:
                return PotionEffectType.INCREASE_DAMAGE;
            case 1:
                return PotionEffectType.DAMAGE_RESISTANCE;
            case 2:
                return PotionEffectType.SPEED;
            case 3:
                return PotionEffectType.NIGHT_VISION;
            case 4:
                return PotionEffectType.INVISIBILITY;
            case 5:
                return PotionEffectType.JUMP;
            case 6:
                return PotionEffectType.FIRE_RESISTANCE;
            case 7:
                return PotionEffectType.WATER_BREATHING;
            case 8:
                return PotionEffectType.HEAL;
            default:
                return PotionEffectType.SPEED;
        }
    }

    public static void handleBuffEffects(org.bukkit.entity.Entity buff, List<Player> toBuff) {
        PotionEffectType effectTypeToGive = getRandomPotionEffect();

        buff.getWorld().playSound(buff.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 5F, 1.5F);
        Random random = new Random();
        BuffManager.getInstance().CURRENT_BUFFS.stream().filter(enderCrystal -> enderCrystal.getBukkitEntity().getLocation().equals(buff.getLocation())).forEach(BuffManager.getInstance().CURRENT_BUFFS::remove);

        int tier = random.nextInt(2);
        int ticksToLast = 1800;
        if (effectTypeToGive != PotionEffectType.HEAL) {
            for (Player player : toBuff) {
                player.addPotionEffect(new PotionEffect(effectTypeToGive, ticksToLast, tier));
                player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "           " + Utils.ucfirst(effectTypeToGive.getName()).replaceAll("_", " ") + " Buff [" + 90 + "s]");
            }
        } else if (effectTypeToGive == PotionEffectType.HEAL) {
            String correctName = "Instant Health";
            boolean fullHP = random.nextInt(100) < 20;
            if (fullHP) {
                correctName += " (100%)";
                for (Player player : toBuff) {
                    HealthHandler.getInstance().setPlayerHPLive(player, HealthHandler.getInstance().getPlayerMaxHPLive(player));
                }
            } else {
                correctName += " (50%)";
                for (Player player : toBuff) {
                    double maxHP = HealthHandler.getInstance().getPlayerMaxHPLive(player);
                    double halfMax = maxHP / 2.D;
                    double currentHP = HealthHandler.getInstance().getPlayerHPLive(player);

                    double healthPercent = (currentHP + halfMax) / maxHP;
                    double toDisplay = healthPercent * 20.D;
                    if (toDisplay >= 19.5D) {
                        if (healthPercent >= 1.D) {
                            toDisplay = 20.D;
                        } else {
                            toDisplay = 19.D;
                        }
                    }
                    if (toDisplay < 1D) {
                        toDisplay = 1D;
                    }

                    HealthHandler.getInstance().setPlayerHPLive(player, (int) (currentHP + halfMax));
                    player.setHealth((int) toDisplay);
                }
            }
            for (Player player : toBuff) {
                player.addPotionEffect(new PotionEffect(effectTypeToGive, ticksToLast, tier));
                player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "           " + correctName);
            }
        }
    }
}
