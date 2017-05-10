package net.dungeonrealms.game.anticheat;

import static com.comphenix.protocol.PacketType.Play.Client.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;

import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.PrettyPrinter;

/**
 * A Packet logger so we can log how exploits work or find ways to detect certain cheats.
 * 
 * Created February 5th, 2017.
 * @author Kneesnap
 */
public class PacketLogger implements Listener, GenericMechanic {
	
	private static HashMap<Player, PacketLog> loggedPlayers = new HashMap<Player, PacketLog>();
	private static PacketListener listener;
	private static PacketType[] ALL_LOGGED_PACKETS = new PacketType[] {ABILITIES, ARM_ANIMATION, BLOCK_DIG, BLOCK_PLACE, BOAT_MOVE, PacketType.Play.Client.CHAT, CLIENT_COMMAND, CLOSE_WINDOW, CUSTOM_PAYLOAD, ENCHANT_ITEM, ENTITY_ACTION, FLYING, PacketType.Play.Client.HELD_ITEM_SLOT, KEEP_ALIVE, POSITION, LOOK, POSITION_LOOK, RESOURCE_PACK_STATUS, SET_CREATIVE_SLOT, SETTINGS, SPECTATE, STEER_VEHICLE, TAB_COMPLETE, TELEPORT_ACCEPT, TRANSACTION, UPDATE_SIGN, USE_ENTITY, USE_ITEM, VEHICLE_MOVE, WINDOW_CLICK, /* SERVER  OPEN_WINDOW, SET_SLOT, CLOSE_WINDOW, PacketType.Play.Server.HELD_ITEM_SLOT, PacketType.Play.Server.CHAT, KICK_DISCONNECT, WORLD_EVENT, WINDOW_DATA, WINDOW_ITEMS, TRANSACTION */};
	
	public void startInitialization() {
		File logDir = new File("./packetlog/");
	    if(!logDir.exists())
	    	logDir.mkdir();
	    
	    listener = new PacketAdapter(DungeonRealms.getInstance(), ALL_LOGGED_PACKETS) {
	    	@Override
	    	public void onPacketReceiving(PacketEvent event) {
	    		Player player = event.getPlayer();
	    		PacketContainer packet = event.getPacket();
	    		if(loggedPlayers.containsKey(player))
	    			loggedPlayers.get(player).write("C -> S", packet);
	    	}
	    	
	    	/* Something internal in ProtocolLib breaks when this is enabled, and it kicks anyone if they open their inventory in gm0
	    	@Override
	    	public void onPacketSending(PacketEvent event) {
	    		Player player = event.getPlayer();
	    		PacketContainer packet = event.getPacket();
	    		if(loggedPlayers.containsKey(player))
	    			loggedPlayers.get(player).write("S -> C", packet);
	    	}*/
	    };
	    
	    ProtocolLibrary.getProtocolManager().addPacketListener(listener);
	    Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
	}

	public void stopInvocation() {
		ProtocolLibrary.getProtocolManager().removePacketListener(listener);
		HandlerList.unregisterAll(this);
		loggedPlayers.keySet().forEach(player -> stopLogging(player, "Server Shutdown"));
	}
	    
	public static void startLogging(Player player) {
		if(!loggedPlayers.containsKey(player))
			loggedPlayers.put(player, new PacketLog(new File("./packetlog/" + player.getName() + new Date().getTime() + ".log")));
	}
	    
	public static void stopLogging(Player player, String reason){
		if(!Bukkit.isPrimaryThread()){
			Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> stopLogging(player, reason));
			return;
		}
		if(loggedPlayers.containsKey(player)){
			try {
				PacketLog pl = loggedPlayers.get(player);
				pl.getWriter().write("Capture Complete: " + reason);
				pl.getWriter().close();
				loggedPlayers.remove(player);
				Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> uploadPacketLog(pl.getFile()));
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
	}
	
	public static boolean isLogging(Player player){
		return loggedPlayers.containsKey(player);
	}
	    
	/**
	 * Log a player for X seconds
	 * 
	 * @param Player
	 * @param Seconds
	 */
	public static void logPlayerTime(Player player, int seconds){
		startLogging(player);
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> stopLogging(player, "Finished Logging"), seconds * 20);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent evt){
		stopLogging(evt.getPlayer(), "Logout");
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent evt){
		stopLogging(evt.getPlayer(), "Kicked (" + evt.getReason() + ")");
	}
	
	private static void uploadPacketLog(File file) {
		if(DungeonRealms.isMaster() || DungeonRealms.isEvent())
			return;
		FTPClient ftpClient = DungeonRealms.getInstance().getFTPClient();
        InputStream inputStream = null;
        try {
            String REMOTE_FILE = "/packetlogs/" + file.getName();

            inputStream = new FileInputStream(file);

            Utils.log.info("[REALM] [ASYNC] Started uploading PacketLog");
            ftpClient.storeFile(REMOTE_FILE, inputStream);
            Utils.log.info("[REALM] [ASYNC] Successfully uploaded PacketLog");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	try{
        		if (inputStream != null)
                	inputStream.close();
        		if (ftpClient.isConnected()) {
                	ftpClient.logout();
                	ftpClient.disconnect();
        		}
        	}catch(Exception e){
        		
        	}
        }
    }
	
	public static void uploadAll(){
		for(File f : new File("./packetlog/").listFiles())
			Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> uploadPacketLog(f));
	}
	
	private static class PacketLog {
		
		private File file;
		private BufferedWriter bw;
		
		public PacketLog(File file){
			this.file = file;
			try{
				this.bw = new BufferedWriter(new FileWriter(file));
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public File getFile(){
			return this.file;
		}
		
		public BufferedWriter getWriter(){
			return this.bw;
		}
		
		public void write(String prefix, PacketContainer packet){
			try{
    			String loggedPacket = "[" + prefix + "] " + packet.getType().name() + ") ";
    			loggedPacket += PrettyPrinter.printObject(packet.getHandle());
    			this.getWriter().write(loggedPacket + "\n");
    		}catch(Exception e){
    			e.printStackTrace();
    		}
		}
	}

	@Override
	public EnumPriority startPriority() {
		return EnumPriority.BISHOPS;
	}
}
