package net.dungeonrealms.game.mastery;

import net.dungeonrealms.common.Constants;
import net.minecraft.server.v1_9_R2.Item;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.BlockIterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Nick on 9/17/2015.
 */
public class Utils {

    public static Logger log = Constants.log;

    
    public static void printTrace() {
        StackTraceElement trace = new Exception().getStackTrace()[2];

        Constants.log.info("[Database] Class: " + trace.getClassName());
        Constants.log.info("[Database] Method: " + trace.getMethodName());
        Constants.log.info("[Database] Line: " + trace.getLineNumber());
    }
    
    public static Location getLocation(String loc) {
        if (loc == null || !loc.contains(",")) return null;
        String[] args = loc.split(",");

        Location retr = new Location(Bukkit.getWorld(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]));

        if (args.length >= 6) {
            retr.setYaw(Float.parseFloat(args[4]));
            retr.setPitch(Float.parseFloat(args[5]));
        }
        return retr;
    }

    private static DecimalFormat format = new DecimalFormat("#.##");

    public static String getStringFromLocation(Location location, boolean round) {
        StringBuilder retr = new StringBuilder();
        retr.append(location.getWorld().getName()).append(",").append(round ? format.format(location.getX()) : location.getX()).append(",")
                .append(round ? format.format(location.getY()) : location.getY()).append(",").append(round ? format.format(location.getZ()) : location.getZ());

        if (location.getYaw() != 0 || location.getPitch() != 0) {
            retr.append(",").append(location.getYaw()).append(",").append(location.getPitch());
        }
        return retr.toString();
    }

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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static String format(int number) {
        return NumberFormat.getNumberInstance(Locale.US).format(number);
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

    /**
     * Given a name in the format lowercaseprefix# (e.g. us0), converts to proper format (e.g. US-0)
     *
     * @param name
     * @return
     */
    public static String getFormattedShardName(String name) {
        return name.split("(?=[0-9])", 2)[0].toUpperCase() + "-" + name.split("(?=[0-9])", 2)[1];
    }

    public static void setMaxStackSize(Item item, int i) {
        try {

            Field field = Item.class.getDeclaredField("maxStackSize");
            field.setAccessible(true);
            field.setInt(item, i);

        } catch (Exception ignored) {
        }
    }

    public static <T> Set<T> findDuplicates(Collection<T> list) {
        Set<T> duplicates = new HashSet<T>();
        Set<T> uniques = new HashSet<T>();

        duplicates.addAll(list.stream().filter(t -> !uniques.add(t)).collect(Collectors.toList()));
        return duplicates;
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
                High = 100;
                if (lvlRange.equalsIgnoreCase("low"))
                    High = 95;
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
            if (c == '&' || c == '\u00A7') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
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

    public static String capitalize(String s) {
    	return ucfirst(s);
    }
    
    public static String ucfirst(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase();
    }

    public static LivingEntity getTarget(LivingEntity entity, double range) {
        List<Entity> nearbyE = entity.getNearbyEntities(range,
                range, range);
        ArrayList<LivingEntity> livingE = nearbyE.stream().filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e).collect(Collectors.toCollection(ArrayList::new));

        LivingEntity target = null;
        BlockIterator bItr = new BlockIterator(entity, (int) range);
        Block block;
        Location loc;
        int bx, by, bz;
        double ex, ey, ez;
        // loop through player's line of sight
        while (bItr.hasNext()) {
            block = bItr.next();
            bx = block.getX();
            by = block.getY();
            bz = block.getZ();
            // check for entities near this block in the line of sight
            for (LivingEntity e : livingE) {
                loc = e.getLocation();
                ex = loc.getX();
                ey = loc.getY();
                ez = loc.getZ();
                if ((bx - .75 <= ex && ex <= bx + 1.75) && (bz - .75 <= ez && ez <= bz + 1.75) && (by - 1 <= ey && ey <= by + 2.5)) {
                    // entity is close enough, set target and stop
                    target = e;
                    break;
                }
            }
        }
        return target;
    }

    public static String getPid() throws IOException, InterruptedException {
        Vector<String> commands = new Vector<>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("echo $PPID");
        ProcessBuilder pb = new ProcessBuilder(commands);

        Process pr = pb.start();
        pr.waitFor();
        if (pr.exitValue() == 0) {
            BufferedReader outReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            return outReader.readLine().trim();
        } else {
            System.out.println("Error while getting PID");
            return "";
        }
    }

    /**
     * Convert seconds into a human readable format.
     *
     * @param seconds
     * @return seconds.
     */
    public static String formatTimeAgo(int seconds) {
        if (seconds == 0) return "0 seconds"; // @note: 0 seconds is a special case.

        String date = "";

        String[] unitNames = {"week", "day", "hour", "minute", "second"};
        int[] unitValues = {604800, 86400, 3600, 60, 1};

        // Loop through all of the units.
        for (int i = 0; i < unitNames.length; i++) {
            int quot = seconds / unitValues[i];
            if (quot > 0) {
                date += quot + " " + unitNames[i] + (Math.abs(quot) > 1 ? "s" : "") + ", ";
                seconds -= (quot * unitValues[i]);
            }
        }

        // Return the date, substring -2 to remove the trailing ", ".
        return date.substring(0, date.length() - 2);
    }

    /**
     * Convert milliseconds into a human readable format.
     *
     * @param milliseconds
     * @return String
     */
    public static String formatTimeAgo(Long milliseconds) {
        return formatTimeAgo((int) (milliseconds / 1000));
    }

    public static String getDate(Long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC-0"));
        return dateFormat.format(new Date(milliseconds));
    }
    
    /**
     * Sanitizes user input so it can be used as a file path.
     * Works by changing all non alphanumeric characters to underscore.
     * Multiple characters in a row will be treated as a single underscore.
     */
    public static String sanitizeFileName(String fileName) {
    	return fileName.replaceAll("[^a-zA-Z0-9\\._]+", "_");
    }

}
