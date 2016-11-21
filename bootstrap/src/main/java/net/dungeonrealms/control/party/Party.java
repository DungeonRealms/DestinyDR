package net.dungeonrealms.control.party;

import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.player.DRPlayer;
import net.dungeonrealms.control.player.rank.Rank;
import net.dungeonrealms.control.server.types.GameServer;

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

    public void setOwner(DRPlayer player) {
        if (player.getRank() == Rank.DEFAULT) {
            broadcast(player.getRank().getColor() + player.getName() + " &eis now the party leader.", true);
        } else {
            broadcast(player.getRank().getColor() + "[" + player.getRank().getName() + "] " + player.getName() + " &eis now the party leader.", true);
        }
        this.owner = player.getUuid();
    }

    public List<DRPlayer> getPlayers() {
        List<DRPlayer> playerList = players.stream().map(uuid -> DRControl.getInstance().getPlayerManager().getPlayerByUUID(uuid)).collect(Collectors.toList());

        return playerList;
    }

    public void addPlayer(DRPlayer player) {
        if (isInvited(player)) {
            invited.remove(player.getUuid());
        }

        players.add(player.getUuid());
    }

    public void removePlayer(DRPlayer player) {
        players.remove(player.getUuid());

        //Assign a new party leader.
        if (isOwner(player)) {

            if (getPlayers().size() >= 1) {
                setOwner(getPlayers().get(0));
            } else {
                DRControl.getInstance().getPartyManager().removeParty(this);
            }

        }
    }

    public boolean containsPlayer(DRPlayer player) {
        return players.contains(player.getUuid());
    }

    public boolean isOwner(DRPlayer player) {
        return getOwner().equals(player);
    }

    public boolean isInvited(DRPlayer player) {
        return invited.contains(player.getUuid());
    }

    public boolean isFull() { return players.size() >= 5; }

    public void invitePlayer(DRPlayer player) {
        invited.add(player.getUuid());
    }

    public void warp(GameServer server) {
        for (DRPlayer player : getPlayers()) {
            player.connect(server, false);
        }
    }

    public void broadcast(String msg, boolean prefix) {
        for (DRPlayer player : getPlayers()) {
            player.sendMessage(msg, prefix);
        }
    }

}
