package net.dungeonrealms.game.player.altars;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Created by Rar349 on 8/3/2017.
 */
public enum Altars {

    DARK_OAK("Dark Oak Altar", "world", new Location(null,-211,67,1018),new Location(null,-211,66,1012), new Location(null,-207,66,1014),new Location(null,-205,66,1018),new Location(null,-207,66,1022), new Location(null,-211,66,1024),new Location(null,-215,66,1022), new Location(null,-217,66,1018), new Location(null,-215,66,1014));

    @Getter
    String name;
    @Getter
    String worldName;
    Location[] nodes;
    Altars(String name, String worldName,Location... nodes) {
        this.name = name;
        this.worldName = worldName;
        this.nodes = nodes;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public Location getNode(Location location) {
        for(Location loc : nodes) {
            loc = loc.clone();
            loc.setWorld(getWorld());
            if(loc.equals(location)) return loc;
        }

        return null;
    }

    public Location getNode(Block block) {
        return getNode(block.getLocation());
    }


    public Location getCenterLocation() {
        return getNode(0);
    }

    public Location getNode(int nodeIndex) {
        Location toReturn = nodes[nodeIndex].clone();
        toReturn.setWorld(getWorld());
        return toReturn;
    }

    public int getNodeIndex(Location loc) {
        for(int k = 0; k < nodes.length; k++) {
            Location toCompare = getNode(k);
            if(loc.equals(toCompare)) return k;
        }
        return -1;
    }

    public int getNodeSize() {
        return nodes.length;
    }
}
