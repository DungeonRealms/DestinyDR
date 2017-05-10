package net.dungeonrealms.game.world.entity.type.pet;

import lombok.Setter;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

/**
 * Created by Rar349 on 4/30/2017.
 */
public class EndermanPet extends EntityEnderman implements Ownable {

	@Setter private Player owner;
	
    public EndermanPet(World world) {
        super(world);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.45F);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        return false;
    }

    @Override
    public void setGoalTarget(@Nullable EntityLiving entityliving) {
    	
    }

    @Override
    protected boolean db() {
        return false;
    }

    @Override
    protected boolean a(Entity entity) {
        return false;
    }

    @Override
    public void setCarried(@Nullable IBlockData iblockdata) {
    	// Don't carry blocks.
    }

    public void teleport() { // Teleport to the player.
        double xCoord = getRandomDouble(owner.getLocation().getX() - 2, owner.getLocation().getX() + 2);
        double zCoord = getRandomDouble(owner.getLocation().getZ() - 2, owner.getLocation().getZ() + 2);

        Location currentLocation = new Location(this.getBukkitEntity().getWorld(),this.lastX, this.lastY, this.lastZ);
        Location newLocation = new Location(this.getBukkitEntity().getWorld(),xCoord, this.lastY, zCoord);
        if(currentLocation.distanceSquared(newLocation) >= 2)
        	teleport(xCoord, owner.getLocation().getY(), zCoord);
    }

    private boolean teleport(double d0, double d1, double d2) { // Overrides the NMS class so we don't play a sound when we teleport.
        boolean flag = this.k(d0, d1, d2);
        if(flag)
            this.world.a((EntityHuman)null, this.lastX, this.lastY, this.lastZ, SoundEffects.ba, this.bA(), 1.0F, 1.0F);

        return flag;
    }

    @Override
    protected void r() {
    	// Don't register default AI.
    }

    @Override // ???
    public void n() {
        super.n();
        if(ticksLived % 50 == 0)
        	teleport();
    }


    public double getRandomDouble(double min, double max) {
        double random = this.getRandom().nextDouble();
        double result = min + (random * (max - min));

        return result;
    }
}
