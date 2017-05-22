package net.dungeonrealms.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.dungeonrealms.network.discord.DiscordAPI;
import net.dungeonrealms.network.discord.DiscordChannel;
import net.dungeonrealms.network.packet.type.BasicMessagePacket;
import net.dungeonrealms.common.game.database.player.PlayerRank;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

/**
 * Handles all master server responses.
 * Since some of this code contains stuff like Discord tokens,
 * it's best not to keep all of it in a shared project like "network"
 * because anyone who gets any of the jars could then take the credentials.
 * 
 * Created March 11th, 2017.
 * @author Kneesnap
 */
public class MasterServerListener extends Listener {
	
	@Override
    public void received(Connection connection, Object object) {
		if(object instanceof BasicMessagePacket) {
			BasicMessagePacket packet = (BasicMessagePacket) object;
			
			byte[] data = packet.data;
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            try {
            	String task = in.readUTF();
            	
            	switch(task) {
            		case "StaffMessage":
            			DiscordChannel dc = DiscordChannel.getByChannel(PlayerRank.valueOf(in.readUTF()));
            			if (dc != null)
            				DiscordAPI.sendMessage(dc, in.readUTF());
            			break;
            		case "BanMessage":
            			DiscordAPI.sendMessage(DiscordChannel.STAFF_REPORTS, in.readUTF());
            			break;
            		case "buff":
            			String type = in.readUTF();
                        int duration = Integer.parseInt(in.readUTF()) / 60;
                        int bonusAmount = Integer.parseInt(in.readUTF());
                        String playerName = in.readUTF();
                        String friendlyName = type.equalsIgnoreCase("level") ? "XP" : type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
//                        DiscordAPI.sendMessage(DiscordChannel.DR_DISCUSSION, playerName + " has activated a " + bonusAmount + "% " + friendlyName + " Buff for " + duration + " minutes.");
                        break;
            	}
            } catch(Exception e) {
            	e.printStackTrace();
            }
		}
	}
}
