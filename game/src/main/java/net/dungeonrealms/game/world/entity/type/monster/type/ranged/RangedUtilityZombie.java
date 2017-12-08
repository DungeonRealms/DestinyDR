package net.dungeonrealms.game.world.entity.type.monster.type.ranged;

import net.dungeonrealms.game.item.items.core.ItemWeaponMarksmanBow;
import net.dungeonrealms.game.world.entity.type.monster.base.DRZombie;
import net.dungeonrealms.game.world.entity.util.pathfinders.PathFinderShootBow;
import net.dungeonrealms.game.world.entity.util.pathfinders.PathFinderShootMarkBow;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.IRangedEntity;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class RangedUtilityZombie extends DRZombie implements IRangedEntity {

    public RangedUtilityZombie(World world) {
        super(world);
    }

    @Override
    protected void r() {
        super.r();
        this.goalSelector.a(1, new PathFinderShootMarkBow(this, 1.0D, 20, 15.0F));
    }

    @Override
    public ItemStack getWeapon() {
        return makeItem(new ItemWeaponMarksmanBow());
    }

    @Override
    public void a(EntityLiving entityLiving, float v) {
        DamageAPI.fireMarksmanBowProjectile((LivingEntity) getBukkitEntity(), new ItemWeaponMarksmanBow(getHeld()));
    }
}
