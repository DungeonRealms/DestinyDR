package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.game.miscellaneous.SkullCreator;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRZombie;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

/**
 * Created by Nick on 9/17/2015.
 */
public class EntityPirate extends DRZombie {

    public EntityPirate(World world, EnumMonster enumMons, int tier) {
        super(world, enumMons, tier, EnumEntityType.HOSTILE_MOB, true);
        this.setEquipment(4, CraftItemStack.asNMSCopy(SkullCreator.SkullTextures.PIRATE.getSkull()));
    }

    public EntityPirate(World world) {
        super(world);
    }

    @Override
    public void setStats() {

    }

    @Override
    protected void getRareDrop() {
    }

	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}
    
    @Override
    protected String z() {
        return "";
    }

    @Override
    protected String bo() {
        return "game.player.hurt";
    }

    @Override
    protected String bp() {
        return "";
    }
    
//    @Override
//	public void onMonsterDeath(){
//		getLoot();
//	}
}
