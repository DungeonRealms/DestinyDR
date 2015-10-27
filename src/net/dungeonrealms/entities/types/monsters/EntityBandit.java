package net.dungeonrealms.entities.types.monsters;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Sep 21, 2015
 */
public class EntityBandit extends MeleeEntityZombie{
    public EntityBandit(World world) {
        super(world);
    }

    /**
     * @param world
     * @param tier
     * @param entityType
     */
    public EntityBandit(World world, int tier, EnumEntityType entityType) {
        super(world, EnumMonster.Bandit, tier, entityType, true);
        checkSpecial();
    }

    /**
     *
     */
    private void checkSpecial() {
        if (Utils.randInt(1, 10) == 4) {
        	int number = Utils.randInt(1, 3);
        	if(number == 1){
            this.getBukkitEntity().setMetadata("special", new FixedMetadataValue(DungeonRealms.getInstance(), "poison"));
            this.setCustomName(ChatColor.DARK_GREEN.toString() + ChatColor.UNDERLINE.toString() + monsterType.getPrefix() + " Poisonous Bandit");
        	}else if(number == 2){
                this.getBukkitEntity().setMetadata("special", new FixedMetadataValue(DungeonRealms.getInstance(), "fire"));
                this.setCustomName(ChatColor.DARK_RED.toString() + ChatColor.UNDERLINE.toString() + monsterType.getPrefix() + " Firey Bandit");

        	}else if(number == 3){
                this.getBukkitEntity().setMetadata("special", new FixedMetadataValue(DungeonRealms.getInstance(), "ice"));
                this.setCustomName(ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + monsterType.getPrefix() + " Freezing Bandit");

        	}
        }
    }

    /**
     * @return
     */
    private static String getRandomHead() {
        String[] list = new String[]{"Spy", "Demoman"};
        return list[Utils.randInt(0, list.length - 1)];
    }

    @Override
    protected Item getLoot() {
    	if(this.random.nextInt(100) < 50){
        ItemStack item = BankMechanics.gem.clone();
        item.setAmount(this.random.nextInt(5));
        this.world.getWorld().dropItemNaturally(this.getBukkitEntity().getLocation(), item);
    	}
        return null;
    }
    
	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}

    @Override
    protected void getRareDrop() {

    }

    @Override
    public void setStats() {

    }

    @Override
    protected String bo() {
        return "game.player.hurt";
    }

}
