package net.dungeonrealms.game.world.entity.type.mounts;

import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.entity.Player;

public class WolfMount extends EntityWolf implements JumpingMount {

	private Player player;
	
	public WolfMount(World world) {
		this(world, null);
	}
	
    public WolfMount(World world, Player owner) {
        super(world);

        this.player = owner;
        EntityAPI.clearAI(this.goalSelector, this.targetSelector);
        this.goalSelector.a(0, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 20F));
    }

    @Override
    public String getName() {
        return "Wolf";
    }

    //Ambient sound.
    @Override
    protected SoundEffect G() {
        return SoundEffects.gN;
    }

    @Override
    public void n() {
        for (int i = 0; i < 2; ++i) 
            this.world.addParticle(EnumParticle.PORTAL, this.locX + (this.random.nextDouble() - 0.5D) * (double) this.width, this.locY + this.random.nextDouble() * (double) this.length - 0.25D, this.locZ + (this.random.nextDouble() - 0.5D) * (double) this.width, (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D, new int[0]);
        super.n();
    }
    
    @Override
    public void g(float sideMotion, float forwardMotion) {
        float[] motions = MountUtils.handleMountLogic(this, this.player);
        if(motions == null)return;
        super.g(motions[0], motions[1]);
    }

	@Override
	public void customJump() {
		this.motY = 0.5D;
	}
}
