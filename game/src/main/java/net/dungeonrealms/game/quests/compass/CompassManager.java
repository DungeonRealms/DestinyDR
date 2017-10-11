package net.dungeonrealms.game.quests.compass;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.game.util.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rar349 on 8/8/2017.
 */
public class CompassManager {

    public static int MAX_CHARS = 36;
    private static long COOLDOWN = 200;

    private static HashMap<Player, CompassManager> managers = new HashMap<>();

    @Getter
    private Player player;
    private List<CompassGoal> goals = new ArrayList<>();
    @Setter
    private long lastUpdate;
    //private List<CompassNode> nodes = calculateCompassBaseString();
    public CompassManager(Player player) {
        this.player = player;
    }



    public List<CompassNode> calculateCompassBaseString() {
        List<CompassNode> toReturn = new ArrayList<>();
        for(CompassDirection direction : CompassDirection.values()) {
            toReturn.add(new CompassNode(String.valueOf(direction.getSymbol())));
            for(int distance = 1; distance <= 14; distance++) {
                toReturn.add(new CompassNode(calculateFillerCharacter(distance)));
            }
        }

        return toReturn;
    }

    private boolean isOnCoolDown() {
        return System.currentTimeMillis() - lastUpdate <= COOLDOWN;
    }

    public boolean shouldShowBar() {
        return !goals.isEmpty() && !isOnCoolDown();
    }

    public String getCompassString(Player player) {
        //StringBuilder base = new StringBuilder(calculateCompassString());
        List<CompassNode> nodes = calculateCompassBaseString();
        for(CompassGoal goal : goals) {
            if(goal.getToDirect().getWorld() != player.getLocation().getWorld()) continue;
            double yaw = getYaw(player, goal.getToDirect()) + 90;

            yaw += 360.0;
            if (yaw > 360.0) {
                yaw -= 360.0;
            }

            double percent = (yaw / 360D);

            int index = (int)((nodes.size()) * percent);

            if(index < 0) index = 0;

            CompassNode node = nodes.get(index);

            node.setColor(goal.getColor());
        }


        return generateString(nodes, player);
    }

    public double getYaw(Player player, Location point) {
        Location playerLoc = player.getLocation();

        double xDiff = point.getX() - playerLoc.getX();
        double yDiff = point.getY() - playerLoc.getY();
        double zDiff = point.getZ() - playerLoc.getZ();

        double DistanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double DistanceY = Math.sqrt(DistanceXZ * DistanceXZ + yDiff * yDiff);
        double newYaw = Math.acos(xDiff / DistanceXZ) * 180 / Math.PI;
        double newPitch = Math.acos(yDiff / DistanceY) * 180 / Math.PI - 90;
        if (zDiff < 0.0)
            newYaw = newYaw + Math.abs(180 - newYaw) * 2;
        newYaw = (newYaw - 90);

        return newYaw;
    }

    public String generateString(List<CompassNode> nodes, Player player) {
        double yaw = player.getLocation().getYaw();

        yaw += 360.0;
        if (yaw > 360.0) {
            yaw -= 360.0;
        }

        double percent = (yaw / 360);

        int index = (int)((nodes.size()) * percent);

        if(index < 0) index = 0;
        StringBuilder toReturn = new StringBuilder("");
        for(int k = index; k < (index + MAX_CHARS > nodes.size() ? nodes.size() : index + MAX_CHARS); k++) {
            CompassNode node = nodes.get(k);
            toReturn.append(node.toString());
        }

        if(index + MAX_CHARS > nodes.size()) {
            int indexFromBase = (index + MAX_CHARS) - nodes.size();
            for(int k = 0; k < indexFromBase; k++) {
                CompassNode node = nodes.get(k);
                toReturn.append(node.toString());
            }
        }

        return toReturn.toString();
    }

    public void removeGoal(CompassGoal goal) {
        goals.remove(goal);
    }

    public void registerGoal(CompassGoal goal) {
        this.goals.add(goal);
    }

    /*
    * Used to get the filler character based on the distance from the last CompassDirection
    */
    private String calculateFillerCharacter(int distance) {
        if(distance == 1 || distance == 14) return "\u2588";
        if(distance == 2 || distance == 13) return "\u2593";
        if(distance == 3 || distance == 12) return "\u2592";
        if(distance == 4 || distance == 11) return "\u2592";
        return "\u2591";
    }

    public static CompassManager getManager(Player player) {
        return managers.get(player);
    }

    public static void registerManager(CompassManager manager) {
        managers.put(manager.getPlayer(), manager);
    }

    public static void unregisterManager(Player player) {
        managers.remove(player);
    }



    @Getter
    private enum CompassDirection {
        EAST(0, 'E'),
        SOUTH(1, 'S'),
        WEST(2, 'W'),
        NORTH(3, 'N');

        private int index;
        private char symbol;

        CompassDirection(int index, char symbol) {
            this.index = index;
            this.symbol = symbol;
        }
    }

}
