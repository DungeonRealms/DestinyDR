package net.dungeonrealms.game.mastery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.Bogdacutu.VoidGenerator.VoidGeneratorGenerator;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * Created by Nick on 9/22/2015.
 */
public class RealmManager implements GenericMechanic {

    static RealmManager instance = null;

    public static RealmManager getInstance() {
        if (instance == null) {
            instance = new RealmManager();
        }
        return instance;
    }

    public CopyOnWriteArrayList<RealmObject> CURRENT_REALMS = new CopyOnWriteArrayList<>();

    public static String HOST = "167.114.65.102";
    public static String USER = "dr.53";
    public static String PASSWORD = "19584!cK";
    public static String ROOT_DIR = "/testing/";
    public static String zipPass = "JRmyzc-E+7Qw27qD##L84CJqdbqJE$MXpJmXKBQMT&%-=+=!vymg8JLj*VpZ-vmvhW7qmNTEbEaXU3GybSTMTfd^X5zMtNrLQE_na74*-UkBzz*GER3MarzT2s!6yzgk?Us2nhdke!Zxy%ygnT!PC6Ta$p58KX?8LVtrPUGJHguLU8E5h6=YCzXA%C?gjx6V3Z_jK3gwA3u9__@EsP#%mc=aCfJXebD5B!6rJRw$KHLZyzxBWfHsM-bLEKDEeUCA+zv%KVp8^%AR@vXAv*yTuzDU4^KM4f8@#pgbc#USNh+sYxYZ8tKbt_sJXE9AzCFWsbpy!jEH6$Fa*APRaX%GEDk=d!j^?mWASfGQWC6QdhwPf6$fc2DC6R#jnwMCFuR5*+4+PVj$N#Lx2L8rs7#Z5a&p$WtBc@E-y6wz5c$BPk!%!+9%YA73y8$m%WzEqVN-ms9V^Z*6d+eLFyg-%+gGdw%@ntM?cjCp2Tn6Ss%7tQCgJ*aM4&v7CPPYYU%ef6=3AtHqRsV5KD?^t&hc!Jz7#VzcpJ7dazV%ne9A5a3#k^dUQR_4Htq6NLC*DC?eCZB6Q2xh@+6C-$q4!vMR3daXeAn2LqEmh-92Yv!%p+RMqFsH6C#L%x*%K@QM8Nf77kS79sEL7_yDGMukrPRt*uDFLKtmbTTb*ZdHUcbCzxv*Tb3UMvQzUrPWbc_q&wE2vShWHhWFU9xekuyrQQEgEy@6*mXcmG#GZhXfWNzfDJqDCNr-Ck-VDX=b^frwRuFDp5xLjjnE2UZe36fvfhcmRJ?U5Mq&f=8&8Bw4-f&$fxyutC&SdA!dwcVjSg?MHFUmYxS*hBJRUS9te=4Kf!aHt5J-LH*scK-9JGfa8xx_tj2n#Cp64H2^tGgxcasmcsmswrp-#EB-GY7dGQKPuVSW*uFg*7ULg=ajDL*!3SDvwyaRqH+vkhVH!&dF5DY=wqSpp+SG*@4t_yZ^_AA53g*+WAmmYg88=ba4erjCcn@6!gBBsQ#fABWsJ?bTxnR$?&T*qRC@C2fqTX5tY5GFD-?LhxAGBTDfRg_j*bDJX?hL7gb!UkgA8dG_jkNThEP#*7gAkz!5hBD7q!MNQsxFNdMu+3KRHXY!UhV_GRpL24ap98*mWbDfVJ&jPdH^M&+BqcWsQw=%ERqaDFW+5weNUDb32LVKNwCkNRBaunBJgjSKmFaWWaT_cFebzA-eW?gCb!ynL!S==DUuffs@Mc9f$UswRcJ^%c+Wg?$D?LPPceq$kugQ6*5-xByGwE&P-SxyRz?KF@b$9Ehpfxcs&B8yKn^PN4-?nc3Q#vwm5+FrDw5q9%^_?Y&9xkt@utK+cBVyWjFwsGGvgq_pxa!N=tuvJBkQz7ARF@7Qx6A$x=_8L$Q^sppMSuKRS$4DK+@*d$GjX8azC+q4MWCeh@Ngh?9qcV!cgJMd4Pt5+Npgz%DMr&2yh4f7fdRkyJcEp$t7H@aTrkd^5ppw6puVCFD4jHm3rmeJM@quMeeSWs2RUg6LHcYCA_6QuPjq3m2xfD^nKSveRuV=g3wNU8G8aCf_kXYcp=Gy3@AFkARA!K_GZmQXJrMxNMJWvddPEq+P5vSgqQsQw5m4!#uv&paq6d?@g%vC&4n3Y3FH#3cB3Q^ThA!$tGn#M9fNtJ7LmfAJA_FLVz#6KTLbgAQ^VV*Hsx&-gg!rf-@umWSCynG+DyACDm?eJpaBaPSbzt%?=+yky=34tHauf3&PC@gdhjWDdAAyUKcpJ3$pwSJBX2?p3%kb7&Fscv#W5EZDWXwVG&^d^_RvP7Vj=g8YJAz3Qw9RWdS+4Hh%d@2m8UVfMaM9N^7r6xbVpg#Xg4hSx39p#CDuPV@zS3C5k$Tu=WHrEbfWr%HMLH2xre3%+p2!6JSP6FncTb4j5gP^uxsYf4F+24hb5^g37q+hRj@t-Se-bUjrbDB%==7_ZTgj+8WhnL6gDrRSe99N$Hk39QEF&fZZpP@BYf=RAyZH&yWGk@W_%ar^SeN6WAw^@zS8TBG&VEEvp4^k5_FDnrBYnG2+VAKCVDUP^&mM$Y^@uc+#5wDK";

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.BISHOPS;
    }

    /**
     * Checks for proper local folders.
     * creates if don't exist.
     *
     * @since 1.0
     */
    @Override
    public void startInitialization() {
        Utils.log.info("DungeonRealms Registering FTP() ... STARTING ...");
        File coreDirectory = DungeonRealms.getInstance().getDataFolder();
        try {
            FileUtils.forceMkdir(new File(coreDirectory + File.separator + "realms/down"));
            FileUtils.forceMkdir(new File(coreDirectory + File.separator + "realms/up"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.log.info("DungeonRealms Finished Registering FTP() ... FINISHED!");
    }

    public class RealmObject {

        private UUID realmOwner;
        private List<Player> playerList;
        private List<Player> realmBuilders;
        private boolean isRealmPortalOpen;

        public RealmObject(UUID realmOwner, List<Player> playerList, List<Player> realmBuilders, boolean isRealmPortalOpen) {
            this.realmOwner = realmOwner;
            this.playerList = playerList;
            this.realmBuilders = realmBuilders;
            this.isRealmPortalOpen = isRealmPortalOpen;
        }

        public UUID getRealmOwner() {
            return realmOwner;
        }
        public List<Player> getPlayerList() {
            return playerList;
        }

        public List<Player> getRealmBuilders() {
            return realmBuilders;
        }

        public boolean isRealmPortalOpen() {
            return isRealmPortalOpen;
        }
    }

    @Override
    public void stopInvocation() {

    }

    private void zipRealm(UUID uuid) throws IOException {
    	File destination = new File("realms/up/" + uuid.toString() + ".zip");
        zip(uuid, destination);
    }

    public static void zip(UUID uuid, File destinationFilePath) {
        
		// Input and OutputStreams are defined outside of the try/catch block
		// to use them in the finally block
		ZipOutputStream outputStream = null;
		InputStream inputStream = null;
		
		try {
			// Prepare the realm files that will be added later in the code
			 ArrayList<File> filesToAdd = new ArrayList<File>();
		     try (Stream<Path> filePathStream=Files.walk(Paths.get(uuid.toString()))) {
		    	    filePathStream.forEach(filePath -> {
		    	        if (Files.isRegularFile(filePath)) {
		    	            filesToAdd.add(filePath.toFile());
		    	        }
		    	    });
		    	}
			
			//Initiate output stream with the path/file of the zip file
			//Please note that ZipOutputStream will overwrite zip file if it already exists so no need to worry about the Realm already existing in the upload folder
			outputStream = new ZipOutputStream(new FileOutputStream(destinationFilePath));
			
			// Initiate Zip Parameters which define various properties such
			// as compression method, etc.
			ZipParameters parameters = new ZipParameters();
			
			//Deflate compression or store(no compression) can be set below
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			
			// Set the compression level. This value has to be in between 0 to 9
			// Several predefined compression levels are available
			// DEFLATE_LEVEL_FASTEST - Lowest compression level but higher speed of compression
			// DEFLATE_LEVEL_FAST - Low compression level but higher speed of compression
			// DEFLATE_LEVEL_NORMAL - Optimal balance between compression level/speed
			// DEFLATE_LEVEL_MAXIMUM - High compression level with a compromise of speed
			// DEFLATE_LEVEL_ULTRA - Highest compression level but low speed
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			
			//Now we loop through each file and read this file with an inputstream
			//and write it to the ZipOutputStream.
			for (int i = 0; i < filesToAdd.size(); i++) {
				File file = (File)filesToAdd.get(i);
				
				//This will initiate ZipOutputStream to include the file
				//with the input parameters
				outputStream.putNextEntry(file,parameters);
				
				//If this file is a directory, then no further processing is required
				//and we close the entry (Please note that we do not want to close the outputstream yet.. This will cause realm zipping errors)
				if (file.isDirectory()) {
					outputStream.closeEntry();
					continue;
				}
				
				//Initialize inputstream
				inputStream = new FileInputStream(file);
				byte[] readBuff = new byte[4096];
				int readLen = -1;
				
				//Read the file content and write it to the OutputStream
				while ((readLen = inputStream.read(readBuff)) != -1) {
					outputStream.write(readBuff, 0, readLen);
				}
				
				//Once the content of the file is copied, this entry to the zip file
				//needs to be closed. ZipOutputStream updates necessary header information
				//for this file in this step
				outputStream.closeEntry();
				
				inputStream.close();
			}
			
			//ZipOutputStream now writes zip header information to the zip file
			outputStream.finish();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
    
    public static void unzip(File targetFolderPath, String password) {
        try {
            ZipFile zipFile = new ZipFile(targetFolderPath);
            zipFile.extractAll("");
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }
    
	public void uploadRealm(UUID uuid) {
		try {
			World w = Bukkit.getWorld(uuid.toString());

			Bukkit.unloadWorld(w, true);
			zipRealm(uuid);
			URL url = new URL("ftp://" + USER + ":" + PASSWORD + "@" + HOST + ROOT_DIR + uuid.toString() + ".zip");
			URLConnection urlc = url.openConnection();
			OutputStream out = urlc.getOutputStream();

			InputStream is = new FileInputStream("realms/up/" + uuid.toString() + ".zip");

			byte buf[] = new byte[1024];
			int len;

			while ((len = is.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			out.close();
			is.close();

			new File("realms/up/" + uuid.toString() + ".zip").delete();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

    /**
     * Will download and extract a players realm zip.
     *
     * @param uuid
     * @since 1.0
     */
	public void downloadRealm(UUID uuid) {
        deleteLocalCache(uuid);
		try {
			URL url = new URL("ftp://" + USER + ":" + PASSWORD + "@" + HOST + ROOT_DIR + uuid.toString() + ".zip");
			URLConnection urlc;

			urlc = url.openConnection();

			InputStream is = urlc.getInputStream();
			OutputStream out = new FileOutputStream("realms/down/" + uuid.toString() + ".zip");

			byte buf[] = new byte[1024];
			int len;

			while ((len = is.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			out.close();
			is.close();

		} catch (IOException first_login) {
            generateBlankRealm(uuid);
			return;
		}
		File TEMP_LOCAL_LOCATION = new File("/realms/down/" + uuid.toString() + ".zip");
        unzip(TEMP_LOCAL_LOCATION, zipPass);
        loadInWorld(uuid.toString(), uuid);
	}

    /**
     * Checks the remote server for existance.
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    boolean checkFileExists(FTPClient ftpClient, String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = ftpClient.retrieveFileStream(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int returnCode = ftpClient.getReplyCode();
        if (inputStream == null || returnCode == 550) {
            return false;
        }
        return true;
    }

    /**
     * Removes the players realm.zip after it's been downloaded
     * and extracted.
     *
     * @param uuid
     * @since 1.0
     */
    public void deleteLocalCache(UUID uuid) {
        Utils.log.info("[REALM] Removing cached realm for " + uuid.toString());
        File TEMP_LOCAL_LOCATION = new File(DungeonRealms.getInstance().getDataFolder() + "realms/down/" + uuid.toString() + ".zip");
        if (TEMP_LOCAL_LOCATION.exists()) {
            TEMP_LOCAL_LOCATION.delete();
        } else {
            Utils.log.warning("[REALM] Unable to find local cache to remove Realm for player " + uuid.toString());
        }
    }


    /**
     * Loads a players realm into BUKKIT.
     *
     * @param worldName name of the world, player.UUID.
     * @since 1.0
     */
    public void loadInWorld(String worldName, UUID uuid) {

        if (Bukkit.getPlayer(uuid) == null) {
            /*
            The player has disconnected before or rightafter the realm
            has downloaded.
             */
        } else {
            WorldCreator worldCreator = new WorldCreator(worldName);
            worldCreator.type(WorldType.FLAT);
            worldCreator.generateStructures(false);

            World w = Bukkit.getServer().createWorld(worldCreator);

            w.setKeepSpawnInMemory(false);
            w.setAutoSave(false);
            w.setStorm(false);
            w.setMonsterSpawnLimit(0);
            Bukkit.getWorlds().add(w);

            Bukkit.getPlayer(uuid).teleport(w.getSpawnLocation());
        }
    }

    /**
     * Removes the instance dungeon from EVERYTHING.
     *
     * @param realmObject The Realm.
     * @since 1.0
     */
    public void removeRealm(RealmObject realmObject, boolean playerLoggingOut) {
        realmObject.isRealmPortalOpen = false;
        if (playerLoggingOut) {
            realmObject.getPlayerList().stream().forEach(player -> {
                if (!player.getWorld().getName().contains("DUNGEON") && !player.getWorld().equals(Bukkit.getWorlds().get(0))) {
                    player.sendMessage(ChatColor.RED + "This Realm has been closed!");
                    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    player.setFlying(false);
                }
            });
            Bukkit.unloadWorld(realmObject.getRealmOwner().toString(), false);
            Utils.log.info("[REALMS] Unloading world: " + realmObject.getRealmOwner().toString() + " in preparation for deletion!");
            CURRENT_REALMS.remove(realmObject);
            uploadRealm(realmObject.getRealmOwner());
        }
    }

    /**
     * Gets the realm of a player
     *
     * @since 1.0
     */
    public RealmObject getPlayerRealm(Player player) {
        if (!CURRENT_REALMS.isEmpty()) {
            for (RealmObject realmObject : CURRENT_REALMS) {
                if (realmObject.getRealmOwner().equals(player.getUniqueId())) {
                    return realmObject;
                }
            }
            return null;
        }
        return null;
    }

    public RealmObject getPlayersCurrentRealm(Player player) {
        if (!CURRENT_REALMS.isEmpty()) {
            for (RealmObject realmObject : CURRENT_REALMS) {
                if (realmObject.getRealmOwner().toString().equals(player.getWorld().getName())) {
                    return realmObject;
                }
            }
            return null;
        }
        return null;
    }

    /**
     * Removes a players realm
     *
     * @since 1.0
     */

    public void removePlayerRealm(Player player, boolean playerLoggingOut) {
        if (getPlayerRealm(player) != null) {
            removeRealm(getPlayerRealm(player), playerLoggingOut);
        }
    }
    
	static HashMap<UUID, Integer> realm_transferpending = new HashMap<UUID, Integer>();
    
    /**
     * Opens a players realm for an Instance.
     *
     * @since 1.0
     */
    @SuppressWarnings("deprecation")
	public void tryToOpenRealmInstance(Player player) {
        if (getPlayerRealm(player) == null || !getPlayerRealm(player).isRealmPortalOpen()) {
            if (!player.getWorld().equals(Bukkit.getWorlds().get(0))) {
                player.sendMessage(ChatColor.RED + "You can only open a realm portal in the main world!");
                return;
            }
            if (doesRealmExistLocally(player.getUniqueId()) && !isRealmLoaded(player.getUniqueId())) {
                Utils.log.info("[REALMS] Player " + player.getUniqueId().toString() + "'s Realm existed locally, loading it!");
                Bukkit.createWorld(new WorldCreator(player.getUniqueId().toString()));
                return;
            }
            if (!doesRealmExistLocally(player.getUniqueId()) && !isRealmLoaded(player.getUniqueId())) {
                Utils.log.info("[REALMS] Player " + player.getUniqueId().toString() + "'s Realm doesn't exist locally, downloading it from FTP!");
                downloadRealm(player.getUniqueId());
            }
            realm_transferpending.put(player.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            	if(Bukkit.getWorld(player.getUniqueId().toString()) != null)
            	{
                    RealmObject realmObject = new RealmObject(player.getUniqueId(), new ArrayList<>(), new ArrayList<>(), true);
                    realmObject.getRealmBuilders().add(player);
                    realmObject.getPlayerList().add(player);
                    CURRENT_REALMS.add(realmObject);
            		player.teleport(Bukkit.getWorld(player.getUniqueId().toString()).getSpawnLocation());
            		Bukkit.getScheduler().cancelTask(realm_transferpending.get(player.getUniqueId()));
            		realm_transferpending.remove(player.getUniqueId());
            	}
            	
            }, 0, 20L));
        } else {
            player.sendMessage(ChatColor.RED + "You already have a Realm Portal in the world, please destroy it!");
        }
    }

    public void generateBlankRealm(UUID ownerUUID) {
    	
		WorldCreator wc = new WorldCreator(ownerUUID.toString());
		wc.type(WorldType.FLAT);
		wc.generateStructures(false);
		wc.generator(new VoidGeneratorGenerator());
		World w = Bukkit.createWorld(wc);
		w.setSpawnLocation(24, 130, 24);
		w.getBlockAt(0, 64, 0).setType(Material.AIR);
		int x = 0, y = 128, z = 0;
		Vector s = new Vector(16, 128, 16);
		// GRASS
		for (x = s.getBlockX(); x < 32; x++) {
			for (z = s.getBlockZ(); z < 32; z++) {
				w.getBlockAt(new Location(w, x, y, z)).setType(Material.GRASS);
			}
		}

		// DIRT
		for (x = s.getBlockX(); x < 32; x++) {
			for (y = 127; y >= 112; y--) {
				for (z = s.getBlockZ(); z < 32; z++) {
					w.getBlockAt(new Location(w, x, y, z)).setType(Material.DIRT);
				}
			}
		}

		// BEDROCK
		for (x = s.getBlockX(); x < 32; x++) {
			for (z = s.getBlockZ(); z < 32; z++) {
				w.getBlockAt(new Location(w, x, y, z)).setType(Material.BEDROCK);
			}
		}

        Utils.log.info("[REALMS] Blank Realm generated for player " + ownerUUID.toString());
    }

    public boolean doesRealmExistLocally(UUID uuid) {
        return new File(ROOT_DIR + "/" + uuid.toString()).exists() && new File(ROOT_DIR + "/" + uuid.toString()).isDirectory();
    }

    public boolean isRealmLoaded(UUID uuid) {
        for (World world : Bukkit.getServer().getWorlds()) {
            if (world.getName().equalsIgnoreCase(uuid.toString())) {
                return true;
            }
        }
        return false;
    }
}
