package net.dungeonrealms.control.party;

import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.player.DRPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evoltr on 11/17/2016.
 */
public class PartyManager {

    private DRControl control;

    private long partyUptime;

    private List<Party> parties = new ArrayList<>();

    public PartyManager(DRControl control) {
        this.control = control;
    }

    public List<Party> getParties() {
        return parties;
    }

    public Party getParty(DRPlayer player) {
        for (Party party : getParties()) {
            if (party.containsPlayer(player)) {
                return party;
            }
        }
        return null;
    }

    public long getPartyUptime() {
        return partyUptime;
    }

    public Party createParty(DRPlayer player) {
        Party party = new Party(player);

        parties.add(party);
        partyUptime = System.currentTimeMillis();
        return party;
    }

    public void removeParty(Party party) {
        parties.remove(party);
    }

}
