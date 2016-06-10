package net.dungeonrealms.game.mastery;

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

        return rand.nextInt((max - min) + 1) + min;
    }

    public static int getRandomFromTier(int tier, String lvlRange) {
        Random r = new Random();
        int Low = 1;
        int High = 10;
        int R;
        //TODO: Remove the +2 from every level when we implement high/low properly
        switch (tier) {
            case 1:
                Low = 1;
                if(lvlRange.equalsIgnoreCase("high"))
                	Low = 5;
                High = 10;
                if(lvlRange.equalsIgnoreCase("low"))
                	High = 5;
                R = r.nextInt(High - Low) + Low + 2;
                return R;
            case 2:
                Low = 10;
                if(lvlRange.equalsIgnoreCase("high"))
                	Low = 15;
                High = 20;
                if(lvlRange.equalsIgnoreCase("low"))
                	High = 15;
                R = r.nextInt(High - Low) + Low + 2;
                return R;
            case 3:
                Low = 20;
                if(lvlRange.equalsIgnoreCase("high"))
                	Low = 25;
                High = 30;
                if(lvlRange.equalsIgnoreCase("low"))
                	High = 25;
                R = r.nextInt(High - Low) + Low + 2;
                return R;
            case 4:
                Low = 30;
                if(lvlRange.equalsIgnoreCase("high"))
                	Low = 35;
                High = 40;
                if(lvlRange.equalsIgnoreCase("low"))
                	High = 35;
                R = r.nextInt(High - Low) + Low + 2;
                return R;
            case 5:
                Low = 40;
                if(lvlRange.equalsIgnoreCase("high"))
                	Low = 45;
                High = 50;
                if(lvlRange.equalsIgnoreCase("low"))
                	High = 45;
                R = r.nextInt(High - Low) + Low + 2;
                return R;
        }
        return 1;
    }

    public static String ucfirst(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

}
