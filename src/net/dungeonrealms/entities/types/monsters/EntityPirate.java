package net.dungeonrealms.entities.types.monsters;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.Items;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Nick on 9/17/2015.
 */
public class EntityPirate extends MeleeEntityZombie {

    public EntityPirate(World world, EnumEntityType entityType, int tier) {
        super(world, "Pirate", getRandomHead(), tier, entityType, true);
        int level = Utils.getRandomFromTier(tier);
        MetadataUtils.registerEntityMetadata(this, entityType, tier, level);
        EntityStats.setMonsterStats(this, level, tier);
    }

    /**
     * @return
     */
    private static String getRandomHead() {
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
    protected Item getLoot() {
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

    @Override
    public String getPrefix() {
        String[] adjectives = new String[]{""};
        List<String> list = Arrays.asList(adjectives);
        Collections.shuffle(list);
        return list.get(0);
    }


    @Override
    public String getSuffix() {
        String[] adjectives = new String[]{""};
        List<String> list = Arrays.asList(adjectives);
        Collections.shuffle(list);
        return list.get(0);
    }
}
