package net.dungeonrealms.game.mastery;

import org.bukkit.ChatColor;
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

    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String timeString(int totalMinutes) {
        String timeStr = "";

        int totalMins = totalMinutes;
        int totalHours = totalMins / 60;
        int totalDays = totalHours / 24;
        int remainingMins = totalMins % 60;
        int remainingHours = totalHours % 24;
        if (totalDays > 0) {
            timeStr = timeStr + Integer.toString(totalDays) + " day";
            if (totalDays > 1) {
                timeStr = timeStr + "s";
            }
        }
        if (totalHours > 0) {
            int hours = totalHours;
            if (totalDays > 0) {
                hours = remainingHours;
                if (remainingHours > 0) {
                    if (remainingMins > 0) {
                        timeStr = timeStr + ", ";
                    } else {
                        timeStr = timeStr + " and ";
                    }
                    timeStr = timeStr + Integer.toString(hours) + " hour";
                    if (hours > 1) {
                        timeStr = timeStr + "s";
                    }
                }
            } else {
                timeStr = timeStr + Integer.toString(hours) + " hour";
                if (hours > 1) {
                    timeStr = timeStr + "s";
                }
            }
        }
        if (totalMins > 0) {
            if (totalDays > 0) {
                if (remainingMins > 0) {
                    if (remainingHours > 0) {
                        timeStr = timeStr + ", and ";
                    } else {
                        timeStr = timeStr + " and ";
                    }
                }
            } else if ((totalHours > 0) &&
                    (remainingMins > 0)) {
                timeStr = timeStr + " and ";
            }
            int mins = totalMins;
            if ((totalDays > 0) || (totalHours > 0)) {
                mins = remainingMins;
            }
            if (mins > 0) {
                timeStr = timeStr + Integer.toString(mins) + " minute";
                if (mins > 1) {
                    timeStr = timeStr + "s";
                }
            }
        }
        if (totalMins < 1) {
            timeStr = "less than a minute";
        }

        return Character.toUpperCase(timeStr.charAt(0)) + timeStr.substring(1).toLowerCase();
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
                if (lvlRange.equalsIgnoreCase("high"))
                    Low = 5;
                High = 10;
                if (lvlRange.equalsIgnoreCase("low"))
                    High = 5;
                R = r.nextInt(High - Low) + Low + 2;
                return R;
            case 2:
                Low = 10;
                if (lvlRange.equalsIgnoreCase("high"))
                    Low = 15;
                High = 20;
                if (lvlRange.equalsIgnoreCase("low"))
                    High = 15;
                R = r.nextInt(High - Low) + Low + 2;
                return R;
            case 3:
                Low = 20;
                if (lvlRange.equalsIgnoreCase("high"))
                    Low = 25;
                High = 30;
                if (lvlRange.equalsIgnoreCase("low"))
                    High = 25;
                R = r.nextInt(High - Low) + Low + 2;
                return R;
            case 4:
                Low = 30;
                if (lvlRange.equalsIgnoreCase("high"))
                    Low = 35;
                High = 40;
                if (lvlRange.equalsIgnoreCase("low"))
                    High = 35;
                R = r.nextInt(High - Low) + Low + 2;
                return R;
            case 5:
                Low = 40;
                if (lvlRange.equalsIgnoreCase("high"))
                    Low = 45;
                High = 50;
                if (lvlRange.equalsIgnoreCase("low"))
                    High = 45;
                R = r.nextInt(High - Low) + Low + 2;
                return R;
        }
        return 1;
    }

    private final static int CENTER_PX = 154;

    public static void sendCenteredMessage(Player player, String message) {
        if (message == null || message.equals("")) player.sendMessage("");

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '&') {
                previousCode = true;
                continue;
            } else if (previousCode == true) {
                previousCode = false;
                if (c == 'l' || c == 'L') {
                    isBold = true;
                    continue;
                } else isBold = false;
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', sb.toString() + message));
    }

    public static String ucfirst(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase();
    }

}
