package net.dungeonrealms.entities.types.monsters;

import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.Items;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Nick on 9/17/2015.
 */
public class EntityPirate extends MeleeEntityZombie{

    public EntityPirate(World world, EnumMonster enumMons, int tier) {
        super(world, enumMons, tier, EnumEntityType.HOSTILE_MOB, true);
        int level = Utils.getRandomFromTier(tier);
        MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, tier, level);
        EntityStats.setMonsterRandomStats(this, level, tier);
    }

    /**
     * @return
     */
    public static String getRandomHead() {
        String[] list = new String[]{"samsamsam1234"};
        return list[Utils.randInt(0, list.length - 1)];
    }

    public EntityPirate(World world) {
        super(world);
    }

    @Override
    public void setStats() {

    }

    @Override
    public Item getLoot() {
        ItemStack item = BankMechanics.gem.clone();
        item.setAmount(this.random.nextInt(5));
        this.world.getWorld().dropItemNaturally(this.getBukkitEntity().getLocation(), item);
        return null;
    }

    @Override
    protected void getRareDrop() {
        switch (this.random.nextInt(3)) {
            case 0:
                this.a(Items.GOLD_NUGGET, 1);
                break;
            case 1:
                this.a(Items.WOODEN_SWORD, 1);
                break;
            case 2:
                this.a(Items.BOAT, 1);
        }
    }

	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}
    
    @Override
    protected String z() {
        return "mob.zombie.say";
    }

    @Override
    protected String bo() {
        return "game.player.hurt";
    }

    @Override
    protected String bp() {
        return "mob.zombie.death";
    }
    
//    @Override
//	public void onMonsterDeath(){
//		getLoot();
//	}
}
