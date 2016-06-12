package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRZombie;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;

/**
 * Created by Chase on Oct 2, 2015
 */
public class BasicMeleeMonster extends DRZombie {

    /**
     * @param world
     * @param mobName
     * @param mobHead
     * @param tier
     */
	public EnumMonster monsterType;
	
    public BasicMeleeMonster(World world, EnumMonster type, int tier) {
        super(world, type, tier, EnumEntityType.HOSTILE_MOB, true);
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        switch (type) {
            case Troll:
            case Troll1:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.TROLL.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.TROLL.getSkull());
                break;
            case Goblin:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.GOBLIN.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.GOBLIN.getSkull());
                break;
            case Naga:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.NAGA.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.NAGA.getSkull());
                break;
            case Lizardman:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.LIZARD.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.LIZARD.getSkull());
                break;
            case Zombie:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.ZOMBIE.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.ZOMBIE.getSkull());
                break;
            case Monk:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.MONK.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.MONK.getSkull());
                break;
            case Tripoli:
            case Tripoli1:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.TRIPOLI_SOLDIER.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.TRIPOLI_SOLDIER.getSkull());
                break;
            case Undead:
                if (random.nextBoolean()) {
                    this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.ZOMBIE.getSkull()));
                    livingEntity.getEquipment().setHelmet(SkullTextures.ZOMBIE.getSkull());
                } else {
                    this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.SKELETON.getSkull()));
                    livingEntity.getEquipment().setHelmet(SkullTextures.SKELETON.getSkull());
                }
                break;
            default:
                break;
        }
    }

    public BasicMeleeMonster(World world) {
        super(world);
    }

	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}

    @Override
    protected void setStats() {

    }

}
