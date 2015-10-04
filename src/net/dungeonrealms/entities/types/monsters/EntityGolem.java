package net.dungeonrealms.entities.types.monsters;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Chase on Sep 28, 2015
 */
public class EntityGolem extends MeleeEntityZombie {

    public EntityGolem(World world, int tier, EnumEntityType entityType) {
        super(world, "Golem", null, tier, entityType, true);
        this.setEquipment(0, CraftItemStack.asNMSCopy(new ItemGenerator().next()));
        int level = Utils.getRandomFromTier(tier);
        MetadataUtils.registerEntityMetadata(this, entityType, tier, level);
        EntityStats.setMonsterStats(this, level, tier);
    }

    /**
     * @param world
     */
    public EntityGolem(World world) {
        super(world);
    }

    @Override
    protected Item getLoot() {
        ItemStack item = BankMechanics.gem.clone();
        item.setAmount(this.random.nextInt(5));
        this.world.getWorld().dropItemNaturally(this.getBukkitEntity().getLocation(), item);
        return null;
    }

    @Override
    protected void getRareDrop() {

    }

    @Override
    protected void setStats() {

    }

    @Override
    public String getPrefix() {
        String[] adjectives = new String[]{"Enchanted", "Ironclad", "Enchanted Ironclad", "Ice"};
        List<String> list = Arrays.asList(adjectives);
        Collections.shuffle(list);
        return list.get(0);
    }

	@Override
	public String getSuffix() {
		// TODO Auto-generated method stub
		return null;
	}

}
