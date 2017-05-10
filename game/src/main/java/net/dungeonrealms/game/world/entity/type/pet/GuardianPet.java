package net.dungeonrealms.game.world.entity.type.pet;

import net.dungeonrealms.common.game.util.PacketUtils;
import net.dungeonrealms.game.world.entity.type.CustomNavigationGuardian;
import net.minecraft.server.v1_9_R2.*;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;

/**
 * Created by Rar349 and iFamassxD on 4/30/2017.
 */
public class GuardianPet extends EntityGuardian implements Ownable {

    public GuardianPet(World world) {
        super(world);
        setElder(false);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.45F);
    }
    
    @Override
    public void setOwner(Player player) {
    	// Displays the guardian on your screen effect.
    	PacketUtils.sendPacket(player, new PacketPlayOutGameStateChange(10, 0.0F));
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        return false;
    }

    @Override
    public void setGoalTarget(@Nullable EntityLiving entityliving) {
    	// Don't set goals.
    }

    @Override
    protected void r() {
    	// Don't register default AI
    }

    @Override
    public boolean isInWater() {
        return true; //Prevents the NMS code from freaking out.
    }

    protected NavigationAbstract b(World var1) {
        return new CustomNavigationGuardian(this, var1);
    }

    @Override // Is this entity server-side and without an AI? This prevents using NMS guardian movement code.
    public boolean cp() {
        return true;
    }

    @Override // On Tick
    public void n() {
        if(this.onGround) {
            this.motY += 0.5D; //Bounce if on ground.
            this.onGround = false;
            this.impulse = true; // Unknown.
        }
        super.n();
    }
}
