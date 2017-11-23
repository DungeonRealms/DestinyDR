package net.dungeonrealms.game.world.entity.type.mounts;

import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.entity.Player;

/**
 * Redone by Kneesnap on April 27th, 2017.
 */
public class EnderDragonMount extends EntityBlaze implements JumpingMount {

	private Player owner;
	private int mountJumpTicks = 0;
	private double height = 0;
	private boolean jumped;
	
	public EnderDragonMount(World world) {
		this(world, null);
	}
	
    public EnderDragonMount(World world, Player player) {
        super(world);
        this.owner = player;
        EntityAPI.clearAI(this.goalSelector, this.targetSelector);
        this.goalSelector.a(3, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 20));
    }

	//Ambient sound.
	@Override
	protected SoundEffect G() {
		return SoundEffects.bK;
	}

    @Override
    public void g(float sideMotion, float forwardMotion) {
		this.jumped = false;
		height = owner.getLocation().getY();

    	float[] motion = MountUtils.handleDragonMountLogic(this, owner);

		if (motion[0] > 0 && !this.onGround) {
			if (mountJumpTicks >= 1) {
				this.motY = -1.0D;
				mountJumpTicks = 0;
			} else {
				mountJumpTicks++;
			}
		}

    	if (motion != null) {
			if (height <= 180 && motion[0] < 0) {
				if (mountJumpTicks >= 1) {
					this.motY = 0.5D;
					mountJumpTicks = 0;
				} else {
					mountJumpTicks++;
				}
			}
			super.g(motion[0], motion[1]);
		}
	}

	@Override
	public void customJump() {
		this.motY = 0.5D;
		this.jumped = true;
	}
}
