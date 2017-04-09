package net.dungeonrealms.game.world.entity.type.monster.boss.type;

import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.world.entity.type.monster.base.DRWitherSkeleton;
import net.dungeonrealms.game.world.entity.type.monster.boss.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumDungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Chase on Oct 20, 2015
 */
public class Aceron extends DRWitherSkeleton implements DungeonBoss {

    public Aceron(World world) {
        super(world, EnumMonster.Undead, 5);
    }

    @Override
    public EnumDungeonBoss getEnumBoss() {
        return null;
    }

	@Override
	public int getGemDrop() {
		return 0;
	}

	@Override
	public int getXPDrop() {
		return 0;
	}

	@Override
	public String[] getItems() {
		return null;
	}

	@Override
	public void addKillStat(GamePlayer gp) {
		
	}
}
