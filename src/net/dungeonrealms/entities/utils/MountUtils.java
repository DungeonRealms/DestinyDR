package net.dungeonrealms.entities.utils;

import net.dungeonrealms.API;
import net.dungeonrealms.donate.DonationEffects;
import net.dungeonrealms.entities.types.mounts.EnumMounts;
import net.dungeonrealms.entities.types.mounts.Horse;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Kieran on 9/18/2015.
 */
public class MountUtils {

    public static void spawnMount(UUID uuid, String mountType) {
        Player player = Bukkit.getPlayer(uuid);
        World world = ((CraftWorld) player.getWorld()).getHandle();
        EnumMounts enumMounts = EnumMounts.getByName(mountType.toUpperCase());
        if (!API.isStringMount(mountType)) {
            player.sendMessage("Uh oh... Something went wrong with your mount! Please inform a staff member! [PetType]");
            return;
        }
        switch (enumMounts) {
            case TIER1_HORSE: {
                Horse mountHorse = new Horse(world, 0, 0.20D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                mountHorse.getBukkitEntity().setPassenger(player);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse.getBukkitEntity();
                HorseInventory horseInventory = horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                horseInventory.setArmor(new ItemStack(Material.IRON_BARDING));
                player.playSound(player.getLocation(), Sound.HORSE_IDLE, 1F, 1F);
                player.sendMessage("Mount Spawned!");
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                break;
            }
            case GOLD_HORSE: {
                Horse mountHorse = new Horse(world, 0, 0.25D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                mountHorse.getBukkitEntity().setPassenger(player);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse.getBukkitEntity();
                HorseInventory horseInventory = horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                horseInventory.setArmor(new ItemStack(Material.GOLD_BARDING));
                player.playSound(player.getLocation(), Sound.HORSE_IDLE, 1F, 1F);
                player.sendMessage("Mount Spawned!");
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                break;
            }
            case DIAMOND_HORSE: {
                Horse mountHorse = new Horse(world, 0, 0.3D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                mountHorse.getBukkitEntity().setPassenger(player);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse.getBukkitEntity();
                HorseInventory horseInventory = horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                horseInventory.setArmor(new ItemStack(Material.DIAMOND_BARDING));
                player.playSound(player.getLocation(), Sound.HORSE_IDLE, 1F, 1F);
                player.sendMessage("Mount Spawned!");
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                break;
            }
            case SKELETON_HORSE: {
                Horse mountHorse = new Horse(world, 4, 0.3D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                mountHorse.getBukkitEntity().setPassenger(player);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse.getBukkitEntity();
                HorseInventory horseInventory = horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                player.playSound(player.getLocation(), Sound.HORSE_SKELETON_IDLE, 1F, 1F);
                player.sendMessage("Mount Spawned!");
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                DonationEffects.ENTITY_PARTICLE_EFFECTS.put(mountHorse, ParticleAPI.ParticleEffect.CLOUD);
                break;
            }
            case ZOMBIE_HORSE: {
                Horse mountHorse = new Horse(world, 3, 0.3D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                mountHorse.getBukkitEntity().setPassenger(player);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse.getBukkitEntity();
                HorseInventory horseInventory = horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                player.playSound(player.getLocation(), Sound.HORSE_ZOMBIE_IDLE, 1F, 1F);
                player.sendMessage("Mount Spawned!");
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                break;
            }
            /*case 7: {
                EnderDragon mountEnderDragon = new EnderDragon(world, player.getUniqueId(), EnumEntityType.MOUNT);
                mountEnderDragon.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountEnderDragon, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountEnderDragon.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                mountEnderDragon.getBukkitEntity().setPassenger(player);
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1F, 1F);
                player.sendMessage("Mount Spawned!");
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountEnderDragon);
                player.closeInventory();
                break;
            }*/
        }
    }
}
