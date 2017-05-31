package net.dungeonrealms.game.world.entity.type.pet;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Setter
public class BetaZombie extends ZombiePet implements Ownable {

	private Player owner;
    @Getter private long lastBrainEat;
    private int lastTick;
    private long lastRide = System.currentTimeMillis() - 5000;
    public BetaZombie(World world) {
        super(world);
    }

    @Override
    protected void r() {
    	// Override default AI.
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
    }

    @Override
    public void stopRiding() {
        lastRide = System.currentTimeMillis();
        super.stopRiding();
    }

    @Override
    public void n() {

        super.n();
        
        if(isPassenger() && getVehicle().isAlive()) // If we're chomping someone, mirror their head direction.
            this.setYawPitch(getVehicle().yaw, 90);
        
        if (lastTick++ % 10 != 0)
        	return;

        if (lastTick % 4 == 0) { // If we're too far away or in a different world, teleport.
            if (!owner.getWorld().equals(this.getBukkitEntity().getWorld()) || getBukkitEntity().getLocation().distanceSquared(owner.getLocation()) > 15) {
                if (this.isPassenger()) {
                    this.getBukkitEntity().eject();
                    this.lastRide = System.currentTimeMillis();
                }
                this.getBukkitEntity().teleport(owner.getLocation());
            }
        }
        
        if (isPassenger()) { // If we're chomping someone,
            long time = System.currentTimeMillis() - this.getLastBrainEat();
            if (time >= 750) {
                getBukkitEntity().getWorld().playSound(this.getBukkitEntity().getLocation(), Sound.ENTITY_GENERIC_EAT, .3F, 1.2F);
                if (time >= 1500) {
                    ParticleAPI.spawnBlockParticles(getBukkitEntity().getLocation(), Material.REDSTONE_BLOCK);
                    setLastBrainEat(System.currentTimeMillis());
                }
            }
            return;
        }
        
        // Try to chomp nearby players.

        for(org.bukkit.entity.Entity ent : getBukkitEntity().getNearbyEntities(7, 7, 7)) {
            if(!(ent instanceof Player)) continue;
            Player player = (Player) ent;
            if(player.equals(owner)) continue;
            //OR if its been 5 seocnds since he has dismounted.
            if(ThreadLocalRandom.current().nextInt(20) == 0 || System.currentTimeMillis() - lastRide > TimeUnit.SECONDS.toMillis(10)) {
                player.setPassenger(getBukkitEntity());
                Bukkit.getLogger().info("Setting passenger!");
                return;
            }
        }
    }

}
