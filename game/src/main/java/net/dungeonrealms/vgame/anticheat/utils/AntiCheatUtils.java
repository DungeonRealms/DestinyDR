package net.dungeonrealms.vgame.anticheat.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Matthew E on 10/30/2016 at 12:01 PM.
 *
 * https://github.com/Vinc0682/NTAC/blob/master/src/net/newtownia/NTAC/Utils/MaterialUtils.java
 * https://github.com/Vinc0682/NTAC/blob/master/src/net/newtownia/NTAC/Utils/PlayerUtils.java
 */

public class AntiCheatUtils {

    private static AntiCheatUtils instance;

    private List<Material> unsolidMaterials = Arrays.asList(Material.AIR, Material.SIGN, Material.SIGN_POST,
            Material.TRIPWIRE, Material.TRIPWIRE_HOOK, Material.SUGAR_CANE_BLOCK, Material.LONG_GRASS, Material.FLOWER_POT,
            Material.YELLOW_FLOWER);
    private List<Material> stepableMaterials = Arrays.asList(Material.STEP, Material.ACACIA_STAIRS, Material.BIRCH_WOOD_STAIRS,
            Material.BIRCH_WOOD_STAIRS, Material.BRICK_STAIRS, Material.COBBLESTONE_STAIRS, Material.DARK_OAK_STAIRS,
            Material.JUNGLE_WOOD_STAIRS, Material.NETHER_BRICK_STAIRS, Material.PURPUR_STAIRS, Material.QUARTZ_STAIRS,
            Material.RED_SANDSTONE_STAIRS, Material.SANDSTONE_STAIRS, Material.SMOOTH_STAIRS, Material.SPRUCE_WOOD_STAIRS,
            Material.WOOD_STAIRS, Material.STONE_SLAB2, Material.PURPUR_SLAB);

    private final double GROUND_THRESHOLD = 0.001;

    public boolean isPlayerOnGround(Player p) {
        return isLocationOnGround(p.getLocation());
    }

    public boolean isLocationOnGround(Location loc) {
        List<Material> materials = getMaterialsAround(loc.clone().add(0, -GROUND_THRESHOLD, 0));
        for (Material m : materials)
            if (!isUnsolid(m) && m != Material.WATER && m != Material.STATIONARY_WATER &&
                    m != Material.LAVA && m != Material.STATIONARY_LAVA)
                return true;
        return false;
    }

    public Location getPlayerStandOnBlockLocationstair(Location locationUnderPlayer) {
        Location b11 = locationUnderPlayer.clone().add(0.3, 0, -0.3);
        if (b11.getBlock().getType().name().contains("STAIR")) {
            return b11;
        }
        Location b12 = locationUnderPlayer.clone().add(-0.3, 0, -0.3);
        if (b12.getBlock().getType().name().contains("STAIR")) {
            return b12;
        }
        Location b21 = locationUnderPlayer.clone().add(0.3, 0, 0.3);
        if (b21.getBlock().getType().name().contains("STAIR")) {
            return b21;
        }
        Location b22 = locationUnderPlayer.clone().add(-0.3, 0, +0.3);
        if (b22.getBlock().getType().name().contains("STAIR")) {
            return b22;
        }
        return locationUnderPlayer;
    }

    public boolean isOnStair(Player p) {
        Location loc = p.getLocation().subtract(0, GROUND_THRESHOLD, 0);
        if (getPlayerStandOnBlockLocationstair(loc).getBlock().getType().name().contains("STAIR")) {
            return true;
        }
        return false;
    }

    public boolean isInWeb(Location loc) {
        return loc.getBlock().getType() == Material.WEB ||
                loc.getBlock().getRelative(BlockFace.UP).getType() == Material.WEB;
    }

    public boolean isOnClimbable(Location loc) {
        return loc.getBlock().getType() == Material.LADDER || loc.getBlock().getType() == Material.VINE;
    }

    public boolean isUnderBlock(Player p) {
        Block blockAbove = p.getEyeLocation().getBlock().getRelative(BlockFace.UP);
        return blockAbove != null && !isUnsolid(blockAbove);
    }

    public boolean isOnIce(Player p, boolean strict) {
        if (isPlayerOnGround(p) || strict) {
            List<Material> materials = getMaterialsAround(p.getLocation().clone().add(0, -GROUND_THRESHOLD, 0));
            return materials.contains(Material.ICE) || materials.contains(Material.PACKED_ICE);
        } else {
            List<Material> m1 = getMaterialsAround(p.getLocation().clone().add(0, -1, 0));
            List<Material> m2 = getMaterialsAround(p.getLocation().clone().add(0, -2, 0));
            return m1.contains(Material.ICE) || m1.contains(Material.PACKED_ICE) ||
                    m2.contains(Material.ICE) || m2.contains(Material.PACKED_ICE);
        }
    }

    public boolean isOnSteps(Player p) {
        List<Material> materials = getMaterialsAround(p.getLocation().clone().add(0, -GROUND_THRESHOLD, 0));
        for (Material m : materials)
            if (isStepable(m))
                return true;
        return false;
    }

    public Location getPlayerStandOnBlockLocation(Location locationUnderPlayer, Material mat) {
        Location b11 = locationUnderPlayer.clone().add(0.3, 0, -0.3);
        if (b11.getBlock().getType() != mat) {
            return b11;
        }
        Location b12 = locationUnderPlayer.clone().add(-0.3, 0, -0.3);
        if (b12.getBlock().getType() != mat) {
            return b12;
        }
        Location b21 = locationUnderPlayer.clone().add(0.3, 0, 0.3);
        if (b21.getBlock().getType() != mat) {
            return b21;
        }
        Location b22 = locationUnderPlayer.clone().add(-0.3, 0, +0.3);
        if (b22.getBlock().getType() != mat) {
            return b22;
        }
        return locationUnderPlayer;
    }

    public boolean isInWater(Player p) {
        Location loc = p.getLocation().subtract(0, 0.2, 0);
        return getPlayerStandOnBlockLocation(loc, Material.STATIONARY_WATER).getBlock().getType() == Material.STATIONARY_WATER
                || getPlayerStandOnBlockLocation(loc, Material.WATER).getBlock().getType() == Material.WATER;
    }

    public boolean isInBlock(Player p, Material block) {
        Location loc = p.getLocation().add(0, 0, 0);
        return getPlayerStandOnBlockLocation(loc, block).getBlock().getType() == block;
    }

    public boolean isOnWater(Player p) {
        Location loc = p.getLocation().subtract(0, 1, 0);
        return getPlayerStandOnBlockLocation(loc, Material.STATIONARY_WATER).getBlock().getType() == Material.STATIONARY_WATER;
    }

    public boolean isOnBlock(Player p, Material mat) {
        Location loc = p.getLocation().subtract(0, 1, 0);
        return getPlayerStandOnBlockLocation(loc, mat).getBlock().getType() == mat;
    }

    public List<Material> getMaterialsAround(Location loc) {
        List<Material> result = new ArrayList<>();
        result.add(loc.getBlock().getType());
        result.add(loc.clone().add(0.3, 0, -0.3).getBlock().getType());
        result.add(loc.clone().add(-0.3, 0, -0.3).getBlock().getType());
        result.add(loc.clone().add(0.3, 0, 0.3).getBlock().getType());
        result.add(loc.clone().add(-0.3, 0, 0.3).getBlock().getType());
        return result;
    }

    public List<Material> getMaterialsBelowOld(Player p) {
        return getMaterialsBelowOld(p.getLocation());
    }

    public List<Material> getMaterialsBelowOld(Location loc) {
        Block blockDown = loc.getBlock().getRelative(BlockFace.DOWN);

        ArrayList<Material> materials = new ArrayList<>();
        materials.add(blockDown.getType());
        materials.add(blockDown.getRelative(BlockFace.NORTH).getType());
        materials.add(blockDown.getRelative(BlockFace.NORTH_EAST).getType());
        materials.add(blockDown.getRelative(BlockFace.EAST).getType());
        materials.add(blockDown.getRelative(BlockFace.SOUTH_EAST).getType());
        materials.add(blockDown.getRelative(BlockFace.SOUTH).getType());
        materials.add(blockDown.getRelative(BlockFace.SOUTH_WEST).getType());
        materials.add(blockDown.getRelative(BlockFace.WEST).getType());
        materials.add(blockDown.getRelative(BlockFace.NORTH_WEST).getType());

        return materials;
    }

    public boolean materialsBelowContains(Player p, Material m) {
        return getMaterialsBelowOld(p).contains(m);
    }

    public boolean isGlidingWithElytra(Player p) {
        ItemStack chestplate = p.getInventory().getChestplate();
        return p.isGliding() && chestplate != null && chestplate.getType() == Material.ELYTRA;
    }

    public boolean isOnEntity(Player p, EntityType type) {
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), 1, 1, 1))
            if (e.getType() == type && e.getLocation().getY() < p.getLocation().getY())
                return true;
        return false;
    }

    public PotionEffect getPotionEffect(Player p, PotionEffectType type) {
        PotionEffect effect = null;
        for (PotionEffect tmp : p.getActivePotionEffects()) {
            if (tmp.getType() == type) {
                effect = tmp;
                break;
            }
        }
        return effect;
    }

    public List<Material> getUnsolidMaterials() {
        return unsolidMaterials;
    }

    public boolean isUnsolid(Material m) {
        return getUnsolidMaterials().contains(m);
    }

    public boolean isUnsolid(Block b) {
        return isUnsolid(b.getType());
    }

    public List<Material> getStepableMaterials() {
        return stepableMaterials;
    }

    public boolean isStepable(Material m) {
        return getStepableMaterials().contains(m);
    }

    public boolean isStepable(Block b) {
        return isStepable(b.getType());
    }

    public static AntiCheatUtils getInstance() {
        if (instance == null) {
            instance = new AntiCheatUtils();
        }
        return instance;
    }
}
