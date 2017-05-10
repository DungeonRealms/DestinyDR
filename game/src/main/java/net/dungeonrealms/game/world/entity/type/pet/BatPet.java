package net.dungeonrealms.game.world.entity.type.pet;

import net.minecraft.server.v1_9_R2.EntityBat;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * BatPet - A bat pet.
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class BatPet extends EntityBat {

    private Player target;
    private int repetitions;
    
    public BatPet(World world, Player owner) {
        super(world);
        this.target = owner;
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.45F);
    }
    
    public BatPet(World world) {
    	this(world, null);
    }

    @Override
    public void M() { //AI.
        if (getBukkitEntity().getLocation().distanceSquared(target.getLocation()) <= 4)
            return;
        
        repetitions++;
        if (repetitions % 5 == 0) {
            if (repetitions == 20)
                repetitions = 0;
            return;
        }
        
        if (target == null || !target.isOnline()) {
            die();
            return;
        }
        
        setAsleep(false);

        double dX = target.getLocation().getX() - locX;
        double dY = target.getLocation().getY() + 1 - locY;
        double dZ = target.getLocation().getZ() - locZ;

        Vector posVec = new Vector(dX + 1, dY + 1, dZ + 1);
        posVec.normalize();

        motX = posVec.getX() * 0.3;
        motY = posVec.getY() * 0.3;
        motZ = posVec.getZ() * 0.3;

        Vector dirVec = new Vector(dX, dY, dZ);
        dirVec.multiply(1 / dirVec.length());

        yaw = target.getLocation().getYaw();
        pitch = target.getLocation().getPitch();
    }
}
