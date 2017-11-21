package net.dungeonrealms.game.world.teleportation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.WorldType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

@AllArgsConstructor
public enum TeleportLocation {
	STARTER("Tutorial", null, -1, 817.5, 47, -101.818, -179.7F, 6.2F, false, null),
	EVENT_AREA("Event Area", null, -1, -378, 85, 341, 0F, 0F, false, null),
	CYRENNICA("Cyrennica", WorldRegion.CYRENNICA, 1000, -378, 85, 357),
	HARRISON_FIELD("Harrison Field", WorldRegion.HARRISON, 1500, -594, 59, 687, 92.0F, 1F),
	DARK_OAK("Dark Oak Tavern", WorldRegion.DARK_OAK, 3500, 280, 59, 1132, 2.0F, 1F),
	GLOOMY_HOLLOWS("Gloomy Hollows", WorldRegion.GLOOMY, 3500, -590, 44, 0, 144F, 1F),
	TROLLSBANE("Trollsbane Tavern", WorldRegion.TROLLSBANE, 7500, 962, 95, 1069, -153.0F, 1F),
	TRIPOLI("Tripoli", WorldRegion.TRIPOLI, 7500, -1320, 91, 370, 153F, 1F),
	CRESTWATCH("Crestwatch", WorldRegion.CRESTWATCH, 15000, -522, 57, -433, -179.9F, 1F),
	CRESTGUARD("Crestguard Keep", WorldRegion.CRESTGUARD, 15000, -1428, 116, -489, 95F, 1F),
	AVALON("The Lost City of Avalon", WorldRegion.AVALON, 25000, -217, 153, -3488, -90F, 1F),
	DEADPEAKS("Deadpeaks Mountain Camp", WorldRegion.DEADPEAKS, 35000, -1173, 106, 1030, -88.0F, 1F, true, WorldType.ANDALUCIA),
	SETTLERS("Settler\'s Interlude", null, 10000, -655, 77, 1890),
	ANDLEHEIM("Andleheim Pier", null, 10000, 789, 31, 1642),

	NELIA("Nelia", null, 5000, -219, 71, 529, 0F, 0F, WorldType.ELORA),
	NOVIS("Novis", null, 5000, 175, 70, 182, 0F, 0F, WorldType.ELORA),
	NETYLI("Netyli", null, 5000, -661, 78, 709, 0F, 0F, WorldType.ELORA);

	@Getter private String displayName;
	private WorldRegion region;
	@Getter private int price;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;
	@Getter private boolean chaotic;
	@Getter private WorldType world;
	
	TeleportLocation(String displayName, WorldRegion region, int price, double x, double y, double z) {
		this(displayName, region, price, x, y, z, 0, 0);
	}
	
	TeleportLocation(String displayName, WorldRegion region, int price, double x, double y, double z, float yaw, float pitch) {
		this(displayName, region, price, x, y, z, yaw, pitch, WorldType.ANDALUCIA);
	}
	
	TeleportLocation(String displayName, WorldRegion region, int price, double x, double y, double z, float yaw, float pitch, WorldType type){
		this(displayName, region, price, x, y, z, yaw, pitch, false, type);
	}
	
	public boolean canSetHearthstone(Player player){
		return (this.region != null && this.region.getAchievement() != null) && Achievements.hasAchievement(player, this.region.getAchievement());
	}
	
	public Location getLocation(){
		return new Location((getWorld() != null ? getWorld() : WorldType.ANDALUCIA).getWorld(), this.x, this.y, this.z, this.yaw, this.pitch);
	}
	
	public boolean canTeleportTo(Player player){
		PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
		if (wrapper == null)
            return false;
		
        return wrapper.getAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC;
	}

	public boolean isBook() {
		return getWorld() != null;
	}

	public static TeleportLocation getRandomBookTP() {
		return getRandomBookTP((Utils.randChance(2) ? WorldType.ANDALUCIA : WorldType.ELORA).getWorld());
	}

	public static TeleportLocation getRandomBookTP(World world) {
		List<TeleportLocation> teleportable = new ArrayList<TeleportLocation>();
    	for(TeleportLocation tl : TeleportLocation.values())
    		if(tl.isBook() && (world == null || WorldType.getWorld(world) == tl.getWorld()))
    			teleportable.add(tl);
    	return teleportable.get(Utils.randInt(0, teleportable.size() - 1));
	}

	public static TeleportLocation getByName(String name){
		return Arrays.stream(TeleportLocation.values()).filter(loc -> loc.name().equals(name)).findFirst().orElse(TeleportLocation.NETYLI);
	}
}
