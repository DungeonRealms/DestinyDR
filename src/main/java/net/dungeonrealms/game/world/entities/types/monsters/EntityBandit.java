package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRZombie;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Chase on Sep 21, 2015
 */
public class EntityBandit extends DRZombie {
    public EntityBandit(World world) {
        super(world);
    }

    /**
     * @param world
     * @param tier
     * @param entityType
     */
    public EntityBandit(World world, int tier, EnumEntityType entityType, EnumMonster monster) {
        super(world, EnumMonster.Bandit, tier, entityType, true);
        if (monster == EnumMonster.Bandit) {
            this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.BANDIT.getSkull()));
        } else {
            this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.BANDIT_2.getSkull()));
        }
        checkSpecial();
    }

    /**
     *
     */
    private void checkSpecial() {
        if (Utils.randInt(1, 10) == 4) {
            int number = Utils.randInt(1, 3);
            if (number == 1) {
                this.getBukkitEntity().setMetadata("special", new FixedMetadataValue(DungeonRealms.getInstance(), "poison"));
                this.setCustomName(ChatColor.DARK_GREEN.toString() + ChatColor.UNDERLINE.toString() + monsterType.getPrefix() + " Poisonous Bandit");
            } else if (number == 2) {
                this.getBukkitEntity().setMetadata("special", new FixedMetadataValue(DungeonRealms.getInstance(), "fire"));
                this.setCustomName(ChatColor.DARK_RED.toString() + ChatColor.UNDERLINE.toString() + monsterType.getPrefix() + " Firey Bandit");

            } else if (number == 3) {
                this.getBukkitEntity().setMetadata("special", new FixedMetadataValue(DungeonRealms.getInstance(), "ice"));
                this.setCustomName(ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + monsterType.getPrefix() + " Freezing Bandit");

            }
        }
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
