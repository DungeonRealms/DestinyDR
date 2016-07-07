package net.dungeonrealms.game.network.packets;

import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.List;

public class PartyPacket implements Serializable {
     /**
	 * Party Packet for Cross Shard Party Instances
	 */
	private static final long serialVersionUID = 5868912266136712803L;
		private String serverfrom;
		private String serverto;
		private Player owner;
        private List<Player> members;

        public PartyPacket(String fromserver, String toserver, Player owner, List<Player> members) {
            this.serverfrom = fromserver;
            this.serverto = toserver;
        	this.owner = owner;
            this.members = members;
        }

        public String getFrom()
        {
        	return serverfrom;
        }
        public String getTo()
        {
        	return serverto;
        }
        public Player getOwner() {
            return owner;
        }

        public List<Player> getMembers() {
            return members;
        }
}
