package net.dungeonrealms.game.player.cosmetics.particles.impl;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticles;
import net.dungeonrealms.game.player.cosmetics.particles.TimedSpecialParticleEffect;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 7/6/2017.
 */
public class CrateOpeningEffect extends TimedSpecialParticleEffect {

    private double lastY = 0;
    private int stage = 0;
    private int lastDegree = 0;
    private ArmorStand chestStand;
    private MaterialData winMaterial;
    private List<Item> items = new ArrayList<>();
    private Runnable callback;
    private Player player;
    public CrateOpeningEffect(Location toPlay, Player lookat, MaterialData winMaterial, Runnable callback) {
        super(toPlay);
        this.winMaterial = winMaterial;
        this.callback = callback;
        chestStand = getLocation().getWorld().spawn(getLocation(), ArmorStand.class);
        chestStand.setArms(false);
        chestStand.setBasePlate(false);
        chestStand.setGravity(false);
        chestStand.setCollidable(false);
        chestStand.setSmall(true);
        chestStand.setHelmet(new ItemStack(Material.ENDER_CHEST));
        chestStand.setVisible(false);
        this.player = lookat;
    }

    @Override
    public long getTickRate() {
        return 1;
    }

    @Override
    public boolean tickWhileMoving() {
        return false;
    }

    @Override
    public SpecialParticles getParticleEnum() {
        return null;
    }

    @Override
    protected int getTicksToDie() {
        return 350;
    }

    @Override
    protected boolean isSpecialTracked() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if(!canTick() || isDead) return;
        if(stage == 0) makeCircle();
        else if(stage == 1) makeSpiral();
        if(player != null && player.isOnline())chestStand.teleport(lookAt(chestStand.getLocation(), player.getEyeLocation()));
        //location.getWorld().playEffect(location, Effect.FLAME, 1);
        //ParticleAPI.spawnParticle(Particle.FLAME, center,0.0,0.0,0.0, 1, 0F);
        //chestStand.teleport(center);

    }

    public void makeCircle() {
        Location center = getLocation().clone();
        center.add(0,1,0);
        double radians = Math.toRadians(lastDegree);
        double x = Math.cos(radians);
        double z = Math.sin(radians);
        center.add(x, 0, z);

        lastDegree += 5;
        if(lastDegree >= 360) {
            lastDegree = 0;
            stage++;
        }

        chestStand.teleport(center);
        ParticleAPI.spawnParticle(Particle.CRIT, chestStand.getEyeLocation().add(0,-0.4,0),0.0,0.0,0.0, 1, 0F);
    }

    public void makeSpiral() {
        Location center = getLocation().clone();
        center.add(0,1,0);
        double radians = Math.toRadians(lastDegree);
        double x = Math.cos(radians);
        double z = Math.sin(radians);
        double y = lastY;
        double toMultiply = (1.0 - (lastDegree / 360.0));
        x = x * toMultiply;
        z = z * toMultiply;
        center.add(x, y, z);

        lastY += 0.01;

        lastDegree += 10;
        if(lastDegree >= 360) {
         lastDegree = 0;
            stage++;
            showReward();
        }

        //ParticleAPI.spawnParticle(Particle.REDSTONE, center.clone().add(0,-0.5,0), 1,0.95,0f, 0, 1F);
        chestStand.teleport(center);
        ParticleAPI.spawnParticle(Particle.CRIT, chestStand.getEyeLocation().add(0,-0.4,0),0.0,0.0,0.0, 1, 0F);
    }

    public void showReward() {
        chestStand.setHelmet(new ItemStack(winMaterial.getItemType(), 1,winMaterial.getData()));
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), callback);
        ParticleAPI.spawnParticle(Particle.FLAME, chestStand.getEyeLocation().clone(),0.0,0.0,0.0, 10, 0.3F);
        ParticleAPI.spawnParticle(Particle.SMOKE_NORMAL, chestStand.getEyeLocation().clone(),0.0,0.0,0.0, 10, 0.3F);
        chestStand.getWorld().playSound(chestStand.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            for(int k = 0; k < 3; k++) {
                throwRewardStack();
            }
        });
    }

    private void throwRewardStack() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        Location loc = chestStand.getEyeLocation().clone();
        //loc.getWorld().playSound(loc, Sound.ENTITY_CHICKEN_EGG, .2F, 1.4F);
        Item gem = loc.getWorld().dropItem(loc,winMaterial.getData() == 0 ? new ItemStack(winMaterial.getItemType()) : new ItemStack(winMaterial.getItemType(), 1,winMaterial.getData()));
        MetadataUtils.Metadata.NO_PICKUP.set(gem, true);
        gem.setPickupDelay(Integer.MAX_VALUE);
        gem.setVelocity(new Vector(rand.nextDouble(-0.2,0.2),rand.nextDouble(0.1,0.4),rand.nextDouble(-0.2,0.2)));
        items.add(gem);
    }

    @Override
    protected void die() {
        super.die();
        chestStand.remove();
        for(Item item : items) {
            item.remove();
        }
    }

    public Location lookAt(Location loc, Location lookat) {
        //Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();

        // Values of change in distance (make it relative)
        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();

        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }

        // Get the distance from dx/dz
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

        // Set pitch
        loc.setPitch((float) -Math.atan(dy / dxz));

        // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
        loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

        return loc;
    }
}
