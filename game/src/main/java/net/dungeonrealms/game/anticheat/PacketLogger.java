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
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.game.mastery.Utils;

import org.apache.commons.net.ftp.FTP;
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
public class PacketLogger implements Listener {
	
	public static PacketLogger INSTANCE;
	
	private HashMap<Player, PacketLog> loggedPlayers = new HashMap<Player, PacketLog>();
	
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
	    			
	    			loggedPacket += PrettyPrinter.printObject(packet.getHandle());
	    			loggedPlayers.get(player).getWriter().write(loggedPacket + "\n");
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
		if(!loggedPlayers.containsKey(player)){
			loggedPlayers.put(player, new PacketLog(new File("./packetlog/" + player.getName() + new Date().getTime() + ".log")));
		}
	}
	    
	public void stopLogging(Player player){
		if(loggedPlayers.containsKey(player)){
			try {
				PacketLog pl = loggedPlayers.get(player);
				pl.getWriter().close();
				loggedPlayers.remove(player);
				Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> uploadPacketLog(pl.getFile()));
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
	
	private void uploadPacketLog(File file) {
        InputStream inputStream = null;
        try {
            FTPClient ftpClient = new FTPClient();

            ftpClient.connect(Constants.FTP_HOST_NAME);
            ftpClient.login(Constants.FTP_USER_NAME, Constants.FTP_PASSWORD);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            String REMOTE_FILE = "/packetlogs/" + file.getName();

            inputStream = new FileInputStream(file);

            Utils.log.info("[REALM] [ASYNC] Started uploading PacketLog");
            ftpClient.storeFile(REMOTE_FILE, inputStream);
            Utils.log.info("[REALM] [ASYNC] Successfully uploaded PacketLog");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (inputStream != null)
                inputStream.close();
        } catch (Exception e) {

        }
    }
	
	public void uploadAll(){
		for(File f : new File("./packetlog/").listFiles())
			Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> uploadPacketLog(f));
	}
	
	private class PacketLog {
		
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
	}
}
