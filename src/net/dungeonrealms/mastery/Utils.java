package net.dungeonrealms.mastery;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by Nick on 9/17/2015.
 */
public class Utils {

    public static Logger log = Logger.getLogger("DungeonRealms");

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
