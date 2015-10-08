package net.dungeonrealms.entities.types.monsters;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.enums.EnumMonster;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Chase on Sep 21, 2015
 */
public class EntityBandit extends MeleeEntityZombie {
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
        checkPoison();
    }

    /**
     *
     */
    private void checkPoison() {
        if (Utils.randInt(1, 6) == 4) {
            this.getBukkitEntity().setMetadata("special",
                    new FixedMetadataValue(DungeonRealms.getInstance(), "poison"));
            this.setCustomName(ChatColor.GREEN.toString() + ChatColor.UNDERLINE.toString() + monsterType.getPrefix() + "Poisonous Bandit");
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
        ItemStack item = BankMechanics.gem.clone();
        item.setAmount(this.random.nextInt(5));
        this.world.getWorld().dropItemNaturally(this.getBukkitEntity().getLocation(), item);
        return null;
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
