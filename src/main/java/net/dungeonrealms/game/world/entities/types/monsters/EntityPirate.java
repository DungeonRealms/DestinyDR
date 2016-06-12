package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRZombie;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;

/**
 * Created by Nick on 9/17/2015.
 */
public class EntityPirate extends DRZombie {

    public EntityPirate(World world, EnumMonster enumMons, int tier) {
        super(world, enumMons, tier, EnumEntityType.HOSTILE_MOB, true);
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.PIRATE.getSkull()));
        livingEntity.getEquipment().setHelmet(SkullTextures.PIRATE.getSkull());
    }

    public EntityPirate(World world) {
        super(world);
    }

    @Override
    public void setStats() {

    }

	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}
}
