package net.dungeonrealms.game.world.entity.type.mounts;

import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.entity.Player;

public class SpiderMount extends EntitySpider implements JumpingMount {
	
	private Player player;
	private int floatTicks = 0;
    private long floatCooldown = -1;
	
	public SpiderMount(World world) {
		this(world, null, null);
	}

    public SpiderMount(World world, Player owner, EnumMounts mount) {
        super(world);
        
        this.player = owner;
        EntityAPI.clearAI(this.goalSelector, this.targetSelector);
        this.goalSelector.a(0, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 20F));
    }

    //Disable wall climbing hopefully?
    @Override
    public boolean n_() {
        return false;
    }

    //Ambient sound.
    @Override
    protected SoundEffect G() {
        return SoundEffects.bK;
    }

    @Override
    public void n() {
        for (int i = 0; i < 2; ++i)
            this.world.addParticle(EnumParticle.PORTAL, this.locX + (this.random.nextDouble() - 0.5D) * (double) this.width, this.locY + this.random.nextDouble() * (double) this.length - 0.25D, this.locZ + (this.random.nextDouble() - 0.5D) * (double) this.width, (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D, new int[0]);
        super.n();
    }

    @Override
    public void g(float sideMotion, float forwardMotion) {
    	MountUtils.handleMountLogic(this, this.player);
    	
        if (this.floatCooldown != -1 && this.floatCooldown < System.currentTimeMillis()) {
        	floatCooldown = -1;
        	this.floatTicks = 0;
        }
    }

	@Override
	public void customJump() {
        if (!this.onGround || floatTicks < 18) { //Spider is hovering.
            floatTicks++;
            this.motY = .185D;
            this.floatCooldown = System.currentTimeMillis() + 2000;
            ParticleAPI.sendParticleToEntityLocation(ParticleAPI.ParticleEffect.SMOKE_NORMAL, getBukkitEntity(), 0.0F, 0.0F, 0.0F, 0.01F, 3);
        } else {
            this.motY = 0.5D;    // Default jump.
        }
	}
}
