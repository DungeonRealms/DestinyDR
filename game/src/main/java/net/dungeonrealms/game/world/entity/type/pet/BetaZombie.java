package net.dungeonrealms.game.world.entity.type.pet;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class BetaZombie extends ZombiePet implements Ownable {

	@Setter private Player owner;
    @Getter @Setter private long lastBrainEat;
    @Setter private int lastTick;

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
    public void n() {

        super.n();
        
        if(isPassenger() && getVehicle().isAlive()) // If we're chomping someone, mirror their head direction.
            this.setYawPitch(getVehicle().yaw, 90);
        
        if (lastTick++ % 10 != 0)
        	return;

        if (lastTick % 4 == 0) { // If we're too far away or in a different world, teleport.
            if (!owner.getWorld().equals(this.getBukkitEntity().getWorld()) || getBukkitEntity().getLocation().distanceSquared(owner.getLocation()) > 15) {
                if (this.isPassenger())
                    this.getBukkitEntity().eject();
                this.getBukkitEntity().teleport(owner.getLocation());
            }
        }
        
        if (isPassenger()) { // If we're chomping someone,
            long time = System.currentTimeMillis() - this.getLastBrainEat();
            if (time >= 750) {
                getBukkitEntity().getWorld().playSound(this.getBukkitEntity().getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1.2F);
                if (time >= 1500) {
                    this.getBukkitEntity().getWorld().playEffect(this.getBukkitEntity().getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK.getId());
                    setLastBrainEat(System.currentTimeMillis());
                }
            }
            return;
        }
        
        // Try to chomp nearby players.

        for(org.bukkit.entity.Entity ent : getBukkitEntity().getNearbyEntities(5, 5, 5)) {
            if(!(ent instanceof Player)) continue;
            Player player = (Player) ent;
            if(player.equals(owner)) continue;
            if(ThreadLocalRandom.current().nextInt(50) == 5) {
                player.setPassenger(getBukkitEntity());
                return;
            }
        }
    }

}
