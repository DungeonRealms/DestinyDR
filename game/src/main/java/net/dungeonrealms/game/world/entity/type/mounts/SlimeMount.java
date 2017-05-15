package net.dungeonrealms.game.world.entity.type.mounts;

import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SlimeMount extends EntitySlime implements JumpingMount {

	private Player player;
	private boolean jumped;
	private int mountJumpTicks = 0;
	
	public SlimeMount(World world) {
		this(world, null);
	}
	
    public SlimeMount(World world, Player player) {
        super(world);

        this.player = player;
        EntityAPI.clearAI(this.goalSelector, this.targetSelector);
        this.goalSelector.a(0, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 20F));
    }

    @Override
    public void g(float sideMotion, float forwardMotion) {
	    if(!isVehicle()){
	        die();
	        return;
        }
    	this.jumped = false;
    	
    	float[] motion = MountUtils.handleMountLogic(this, this.player);
    	if (motion == null)
    		return;
    	
        if (!jumped && mountJumpTicks >= 24 && this.onGround) {
            this.motY = .6D;
            mountJumpTicks = 0;
            if (random.nextBoolean())
                this.world.getWorld().playSound(getBukkitEntity().getLocation(), Sound.BLOCK_SLIME_PLACE, 0.1F, 1);
        } else {
            mountJumpTicks++;
        }
        
        super.g(motion[0], motion[1]);
    }

	@Override
	public void customJump() {
		this.motY = 0.5D;
		this.jumped = true;
	}
}
