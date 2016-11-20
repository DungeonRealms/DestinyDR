package net.dungeonrealms.common.frontend.hologram;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Evoltr on 11/20/2016.
 */
public class HologramManager {

    private static HologramManager instance;

    private HashMap<Player, List<Hologram>> playerHolograms = new HashMap<>();

    public static HologramManager getInstance() {
        return instance;
    }

    public void addPlayerToHologram(Player player, Hologram hologram) {
        if (!playerHolograms.containsKey(player)) {
            playerHolograms.put(player, new ArrayList<>());
        }
        List<Hologram> holograms = playerHolograms.get(player);
        holograms.add(hologram);
        playerHolograms.put(player, holograms);
    }

    public void handlePlayerQuit(Player player) {
        if (playerHolograms.containsKey(player)) {
            for (Hologram hologram : playerHolograms.get(player)) {
                hologram.getLoadedPlayers().remove(player);
            }
            playerHolograms.remove(player);
        }
    }

    public void removePlayerHologram(Player player, Hologram hologram) {
        if (!playerHolograms.containsKey(player)) return;
        List<Hologram> holograms = playerHolograms.get(player);
        holograms.remove(hologram);
        playerHolograms.put(player, holograms);
    }

    public void updatePlayerView(Player player) {
        if (!playerHolograms.containsKey(player)) return;
        for (Hologram hologram : playerHolograms.get(player)) {
            hologram.checkSending(player);
        }
    }

    public HashMap<Player, List<Hologram>> getPlayerHolograms() {
        return playerHolograms;
    }
}
