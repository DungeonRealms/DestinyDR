package net.dungeonrealms.mastery;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by Nick on 9/17/2015.
 */
public class Utils {

    public static Logger log = Logger.getLogger("DungeonRealms");

    /**
     * Get a players head.
     *
     * @param player
     * @return
     * @since 1.0
     */
    public static ItemStack getPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(player.getName());
        head.setItemMeta(meta);
        return head;
    }

    public static int randInt(int min, int max) {

        Random rand = new Random();

        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public static int getRandomFromTier(int tier) {
        Random r = new Random();
        int Low = 1;
        int High = 10;
        int R;
        switch (tier) {
            case 1:
                Low = 1;
                High = 10;
                R = r.nextInt(High - Low) + Low;
                return R;
            case 2:
                Low = 10;
                High = 20;
                R = r.nextInt(High - Low) + Low;
                return R;
            case 3:
                Low = 20;
                High = 30;
                R = r.nextInt(High - Low) + Low;
                return R;
            case 4:
                Low = 30;
                High = 40;
                R = r.nextInt(High - Low) + Low;
                return R;
            case 5:
                Low = 40;
                High = 50;
                R = r.nextInt(High - Low) + Low;
                return R;
        }
        return 1;
    }

}
