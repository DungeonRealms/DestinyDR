package net.dungeonrealms.game.world.entities.utils;

import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.EnderCrystal;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffectType;

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
        enderCrystal.setLocation(b.getX(), b.getY(), b.getZ(), 0, 0);
        world.addEntity(enderCrystal, CreatureSpawnEvent.SpawnReason.CUSTOM);
        enderCrystal.setLocation(b.getX(), b.getY(), b.getZ(), 0, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 1f, 63f);
        MetadataUtils.registerBuffMetadata(enderCrystal, getRandomPotionEffect(), 10, 600);
        return enderCrystal;
    }

    public static PotionEffectType getRandomPotionEffect() {
        switch (new Random().nextInt(8)) {
            case 0:
                return PotionEffectType.DAMAGE_RESISTANCE;
            case 1:
                return PotionEffectType.HEAL;
            case 2:
                return PotionEffectType.NIGHT_VISION;
            case 3:
                return PotionEffectType.HUNGER;
            case 4:
                return PotionEffectType.WATER_BREATHING;
            case 5:
                return PotionEffectType.JUMP;
            case 6:
                return PotionEffectType.SPEED;
            case 7:
                return PotionEffectType.INCREASE_DAMAGE;
            default:
                return PotionEffectType.SPEED;
        }
    }
}
