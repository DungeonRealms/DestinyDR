package net.dungeonrealms.entities.types.monsters;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mastery.Utils;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

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
        super(world, "Bandit", getRandomHead(), tier, entityType, true);
        checkPoison();
    }

    /**
     *
     */
    private void checkPoison() {
        if (Utils.randInt(1, 6) == 4) {
            this.getBukkitEntity().setMetadata("special",
                    new FixedMetadataValue(DungeonRealms.getInstance(), "poison"));
            this.setCustomName(ChatColor.GREEN.toString() + ChatColor.UNDERLINE.toString() +  getPrefix() + "Poisonous Bandit");
        }
    }
    @Override
    public String getPrefix() {
        String[] adjectives = new String[]{"Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy",
                "Majestic", "Intrigued", "Dignified", "Couragous", "Timid", "Gloomy", "Noble", "Naive", "Black"};
        List<String> list = Arrays.asList(adjectives);
        Collections.shuffle(list);
        return list.get(0) + " ";
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

	@Override
	public String getSuffix() {
		// TODO Auto-generated method stub
		return null;
	}
}
