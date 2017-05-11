package net.dungeonrealms.game.world.entity.type.mounts;

import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.minecraft.server.v1_9_R2.EntityEnderDragon;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.entity.Player;

/**
 * Redone by Kneesnap on April 27th, 2017.
 */
public class EnderDragonMount extends EntityEnderDragon implements JumpingMount {

	private Player owner;
	
	public EnderDragonMount(World world) {
		this(world, null);
	}
	
    public EnderDragonMount(World world, Player player) {
        super(world);
        this.owner = player;
        EntityAPI.clearAI(this.goalSelector, this.targetSelector);
        this.goalSelector.a(3, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 20));
    }

    @Override
    public void g(float sideMotion, float forwardMotion) {
    	MountUtils.handleMountLogic(this, owner);
        super.g(sideMotion, forwardMotion);
    }

	@Override
	public void customJump() {
		this.motY = 0.5D;
	}
}
