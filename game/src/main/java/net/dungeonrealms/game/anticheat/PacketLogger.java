package net.dungeonrealms.game.anticheat;

import static com.comphenix.protocol.PacketType.Play.Client.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import net.dungeonrealms.DungeonRealms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.avaje.ebeaninternal.server.cluster.Packet;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.PrettyPrinter;
import com.google.gson.Gson;

/**
 * A Packet logger so we can log how exploits work or find ways to detect certain cheats.
 * 
 * Created February 5th, 2017.
 * @author Kneesnap
 */
public class PacketLogger implements Listener {
	
	public static PacketLogger INSTANCE;
	
	private HashMap<Player, BufferedWriter> loggedPlayers = new HashMap<Player, BufferedWriter>();
	
	private PacketListener listener;
	private PacketType[] ALL_INCOMING_PACKETS = new PacketType[] {ABILITIES, ARM_ANIMATION, BLOCK_DIG, BLOCK_PLACE, BOAT_MOVE, CHAT, CLIENT_COMMAND, CLOSE_WINDOW, CUSTOM_PAYLOAD, ENCHANT_ITEM, ENTITY_ACTION, FLYING, HELD_ITEM_SLOT, KEEP_ALIVE, POSITION, LOOK, POSITION_LOOK, RESOURCE_PACK_STATUS, SET_CREATIVE_SLOT, SETTINGS, SPECTATE, STEER_VEHICLE, TAB_COMPLETE, TELEPORT_ACCEPT, TRANSACTION, UPDATE_SIGN, USE_ENTITY, USE_ITEM, VEHICLE_MOVE, WINDOW_CLICK};
	
	public PacketLogger(){
		INSTANCE = this;
		File logDir = new File("./packetlog/");
	    if(!logDir.exists())
	    	logDir.mkdir();
	    listener = new PacketAdapter(DungeonRealms.getInstance(), ALL_INCOMING_PACKETS) {
	    	@Override
	    	public void onPacketReceiving(PacketEvent event) {
	    		Player player = event.getPlayer();
	    		PacketContainer packet = event.getPacket();
	    		if(!loggedPlayers.containsKey(player))
	    			return;
	    		try{
	    			String loggedPacket = packet.getType().name() + ") ";
	    			//CustomPayload packet can cause a StackOverflow Error
	    			/*if(event.getPacketType() == CUSTOM_PAYLOAD){
	    				loggedPacket += "Channel = " + packet.getStrings().getValues().get(0);
	    			}else{
	    				loggedPacket += new Gson().toJson(packet.getHandle());
	    			}*/
	    			loggedPacket += PrettyPrinter.printObject(packet.getHandle());
	    			loggedPlayers.get(player).write(loggedPacket + "\n");
	    		}catch(Exception e){
	    			e.printStackTrace();
	    		}
	    	}
	    };
	    ProtocolLibrary.getProtocolManager().addPacketListener(listener);
	}

	public void onDisable() {
		ProtocolLibrary.getProtocolManager().removePacketListener(listener);
		HandlerList.unregisterAll(this);
		this.loggedPlayers.keySet().forEach(player -> stopLogging(player));
	}
	    
	public void startLogging(Player player){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter("./packetlog/" + player.getName() + new Date().getTime() + ".log"));
			loggedPlayers.put(player, bw);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	    
	public void stopLogging(Player player){
		if(loggedPlayers.containsKey(player)){
			try {
				BufferedWriter bw = loggedPlayers.get(player);
				bw.close();
				loggedPlayers.remove(player);
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
	}
	
	public boolean isLogging(Player player){
		return loggedPlayers.containsKey(player);
	}
	    
	/**
	 * Log a player for X seconds
	 * 
	 * @param Player
	 * @param Seconds
	 */
	public void logPlayerTime(Player player, int seconds){
		startLogging(player);
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> stopLogging(player), seconds * 20);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent evt){
		stopLogging(evt.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerKickEvent evt){
		stopLogging(evt.getPlayer());
	}
}
