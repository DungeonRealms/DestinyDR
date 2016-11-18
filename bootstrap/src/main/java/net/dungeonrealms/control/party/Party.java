package net.dungeonrealms.control.party;

import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.player.DRPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Evoltr on 11/17/2016.
 */
public class Party {

    private String owner;

    private List<String> players;
    private List<String> invited;

    public Party(DRPlayer owner) {
        this.owner = owner.getUuid();

        // Create the players list.
        this.players = new ArrayList<>();
        this.invited = new ArrayList<>();

    }

    public DRPlayer getOwner() {
        return DRControl.getInstance().getPlayerManager().getPlayerByUUID(owner);
    }

    public List<DRPlayer> getPlayers() {
        List<DRPlayer> playerList = players.stream().map(uuid -> DRControl.getInstance().getPlayerManager().getPlayerByUUID(uuid)).collect(Collectors.toList());

        return playerList;
    }

    public boolean containsPlayer(DRPlayer player) {
        return players.contains(player.getUuid());
    }

}
