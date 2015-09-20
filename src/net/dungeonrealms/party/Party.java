package net.dungeonrealms.party;

import java.util.List;
import java.util.UUID;

/**
 * Created by Nick on 8/30/2015.
 */
public class Party {

    private UUID owner;
    private List<UUID> members;

    public Party(UUID owner, List<UUID> members) {
        this.owner = owner;
        this.members = members;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void addMember(UUID uuid) {
        if (!(this.members.contains(uuid))) {
            this.members.add(uuid);
        }
    }
}
