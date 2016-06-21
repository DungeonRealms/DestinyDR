package net.dungeonrealms.game.world.entities.utils;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entities.types.mounts.EnumMounts;
import net.dungeonrealms.game.world.entities.types.mounts.Horse;
import net.dungeonrealms.game.world.entities.types.mounts.mule.MuleTier;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Kieran on 9/18/2015.
 */
public class MountUtils {

    public static ConcurrentHashMap<UUID, Inventory> inventories = new ConcurrentHashMap<>();

    public static boolean hasMountPrerequisites(EnumMounts mountType, List<String> playerMounts) {
        switch (mountType) {
            case TIER1_HORSE:
                return true;
            case TIER2_HORSE:
                return !playerMounts.isEmpty() && playerMounts.contains(EnumMounts.TIER1_HORSE.getRawName());
            case TIER3_HORSE:
                return !playerMounts.isEmpty() && playerMounts.contains(EnumMounts.TIER2_HORSE.getRawName());
            case TIER4_HORSE:
                return !playerMounts.isEmpty() && playerMounts.contains(EnumMounts.TIER3_HORSE.getRawName());
            default:
                return true;
        }
    }

    public static boolean hasRequiredLevel(EnumMounts mountType, UUID player) {
        int level = (int) DatabaseAPI.getInstance().getData(EnumData.LEVEL, player);
        switch (mountType) {
            case TIER1_HORSE:
                return true;
            case TIER2_HORSE:
                return level >= 20;
            case TIER3_HORSE:
                return level >= 30;
            case TIER4_HORSE:
                return level >= 40;
            default:
                return true;
        }
    }

    public static void spawnMount(UUID uuid, String mountType, String mountSkin) {
        Player player = Bukkit.getPlayer(uuid);
        World world = ((CraftWorld) player.getWorld()).getHandle();
        if (!API.isStringMount(mountType)) {
            player.sendMessage("Uh oh... Something went wrong with your mount! Please inform a staff member! [MountType]");
            return;
        }
        int horseType = 0;
        if (API.isStringMountSkin(mountSkin)) {
            switch (EnumMountSkins.getByName(mountSkin.toUpperCase())) {
                case SKELETON_HORSE:
                    horseType = 4;
                    break;
                case ZOMBIE_HORSE:
                    horseType = 3;
                    break;
                default:
                    break;
            }
        }
        org.bukkit.entity.Horse.Color color = Rank.isDev(player) ? org.bukkit.entity.Horse.Color.WHITE : org.bukkit.entity.Horse.Color.BROWN;
        EnumMounts enumMounts = EnumMounts.getByName(mountType.toUpperCase());
        switch (enumMounts) {
            case TIER1_HORSE: {
                Horse mountHorse = new Horse(world, horseType, 0.20D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                mountHorse.getBukkitEntity().setPassenger(player);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse.getBukkitEntity();
                horse.setColor(color);
                HorseInventory horseInventory = horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                horseInventory.setArmor(new ItemStack(Material.IRON_BARDING));
                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_AMBIENT, 1F, 1F);
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                break;
            }
            case TIER2_HORSE: {
                Horse mountHorse = new Horse(world, horseType, 0.25D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                mountHorse.getBukkitEntity().setPassenger(player);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse.getBukkitEntity();
                horse.setColor(color);
                HorseInventory horseInventory = horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                horseInventory.setArmor(new ItemStack(Material.IRON_BARDING));
                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_AMBIENT, 1F, 1F);
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                break;
            }
            case TIER3_HORSE: {
                Horse mountHorse = new Horse(world, horseType, 0.34D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                mountHorse.getBukkitEntity().setPassenger(player);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse.getBukkitEntity();
                horse.setColor(color);
                HorseInventory horseInventory = horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                horseInventory.setArmor(new ItemStack(Material.DIAMOND_BARDING));
                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_AMBIENT, 1F, 1F);
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                break;
            }
            case TIER4_HORSE: {
                Horse mountHorse = new Horse(world, horseType, 0.4D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                mountHorse.getBukkitEntity().setPassenger(player);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse.getBukkitEntity();
                horse.setColor(color);
                HorseInventory horseInventory = horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                horseInventory.setArmor(new ItemStack(Material.GOLD_BARDING));
                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_AMBIENT, 1F, 1F);
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                break;
            }
            case MULE: {
                org.bukkit.entity.Horse h = (org.bukkit.entity.Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
                h.setAdult();
                h.setAgeLock(true);
                h.setVariant(org.bukkit.entity.Horse.Variant.MULE);
                h.setCarryingChest(true);
                h.setTamed(true);
                h.setLeashHolder(player);
                h.setOwner(player);
                h.setColor(org.bukkit.entity.Horse.Color.BROWN);
                MetadataUtils.registerEntityMetadata(((CraftEntity) h).getHandle(), EnumEntityType.MOUNT, 0, 0);
//                h.setCustomName(player.getName() + "'s Storage Mule");
                h.setCustomNameVisible(true);
                h.setMetadata("mule", new FixedMetadataValue(DungeonRealms.getInstance(), "true"));
                String invString = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_MULE, uuid);
                int muleLevel = (int) DatabaseAPI.getInstance().getData(EnumData.MULELEVEL, player.getUniqueId());
                if (muleLevel > 3) {
                    muleLevel = 3;
                }
                MuleTier tier = MuleTier.getByTier(muleLevel);
                if (tier == null) {
                    return;
                }
                h.setCustomName(tier.getColor().toString() + player.getName() + "'s " + tier.getName());
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_DONKEY_AMBIENT, 1F, 1F);
                EntityAPI.addPlayerMountList(player.getUniqueId(), ((CraftEntity) h).getHandle());
                if (!inventories.containsKey(player.getUniqueId())) {
                    Inventory inv = Bukkit.createInventory(player, tier.getSize(), "Mule Storage");
                    if (!invString.equalsIgnoreCase("") && !invString.equalsIgnoreCase("empty") && invString.length() > 4) {
                        //Make sure the inventory is as big as we need
                        inv = ItemSerialization.fromString(invString, tier.getSize());
                    }
                    inventories.put(uuid, inv);
                }
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
