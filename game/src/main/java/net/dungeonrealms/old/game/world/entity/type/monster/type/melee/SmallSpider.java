package net.dungeonrealms.old.game.world.entity.type.monster.type.melee;

import net.dungeonrealms.old.game.miscellaneous.SkullTextures;
import net.dungeonrealms.old.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.old.game.world.entity.type.monster.base.DRCaveSpider;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class SmallSpider extends DRCaveSpider {
    public SmallSpider(World world) {
        super(world);
    }

    public SmallSpider(World world, int tier, EnumMonster monster) {
        super(world, monster, tier);
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        if (monster == EnumMonster.Bandit) {
            this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.BANDIT.getSkull()));
            livingEntity.getEquipment().setHelmet(SkullTextures.BANDIT.getSkull());
        } else {
            this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.BANDIT_2.getSkull()));
            livingEntity.getEquipment().setHelmet(SkullTextures.BANDIT_2.getSkull());
        }
    }

    @Override
    public EnumMonster getEnum() {
        return this.monsterType;
    }


    @Override
    public void setStats() {

    }
}
