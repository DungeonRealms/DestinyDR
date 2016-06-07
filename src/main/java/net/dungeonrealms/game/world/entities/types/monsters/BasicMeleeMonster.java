package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRZombie;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

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
        switch (type) {
            case Troll:
            case Troll1:
                this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.TROLL.getSkull()));
                break;
            case Goblin:
                this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.GOBLIN.getSkull()));
                break;
            case Naga:
                this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.NAGA.getSkull()));
                break;
            case Lizardman:
                this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.LIZARD.getSkull()));
                break;
            case Zombie:
                this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.ZOMBIE.getSkull()));
                break;
            case Monk:
                this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.MONK.getSkull()));
                break;
            case Tripoli:
            case Tripoli1:
                this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.TRIPOLI_SOLDIER.getSkull()));
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
    protected void getRareDrop() {

    }

    @Override
    protected void setStats() {

    }

}
