package net.dungeonrealms.mechanics;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.Utils;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Nick on 10/19/2015.
 */
public class DungeonManager {

    static DungeonManager instance = null;

    public static DungeonManager getInstance() {
        if (instance == null) {
            instance = new DungeonManager();
        }
        return instance;
    }

    public ArrayList<DungeonObject> Dungeons = new ArrayList<>();

    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (DungeonObject dungeonObject : Dungeons) {
                int time = dungeonObject.getTime();
                switch (time) {
                    //45 minutes
                    case 2700:
                        dungeonObject.getPlayerList().stream().forEach(player -> player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD + "DUNGEON" + ChatColor.WHITE + "]" + " " + ChatColor.RED + "This instance will now close! (45) minutes reached!"));
                        break;
                    //35 minutes
                    case 2100:
                        dungeonObject.getPlayerList().stream().forEach(player -> player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD + "DUNGEON" + ChatColor.WHITE + "]" + " " + ChatColor.RED + "This instance has reached (35) minute marker!"));
                        break;
                    //15 minutes
                    case 900:
                        dungeonObject.getPlayerList().stream().forEach(player -> player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD + "DUNGEON" + ChatColor.WHITE + "]" + " " + ChatColor.RED + "This instance has reached (15) minute marker!"));
                        break;
                }
                dungeonObject.modifyTime(1);
            }
        }, 0, 20l);
    }

    public void createNewInstance(DungeonType type, List<Player> playerList) {
        DungeonObject dungeonObject = new DungeonObject(type, 0, playerList, "DUNGEON_" + String.valueOf(System.currentTimeMillis() / 1000l));
        Dungeons.add(dungeonObject);
        dungeonObject.load();
    }

    /**
     * Will extract a players realm .zip to the correct folder.
     *
     * @param zipFile
     * @since 1.0
     */
    public void unZip(ZipFile zipFile, DungeonObject dungeonObject) {
        Utils.log.info("[DUNGEON] Unzipping instance for " + dungeonObject.getWorldName());
        new File(DungeonRealms.getInstance().getDataFolder().getParent() + dungeonObject.getWorldName()).mkdir();
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(DungeonRealms.getInstance().getDataFolder().getParent() + dungeonObject.getWorldName(), entry.getName());
                if (entry.isDirectory())
                    entryDestination.mkdirs();
                else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class DungeonObject {

        private DungeonType type;
        private Integer time;
        private List<Player> playerList;
        private String worldName;

        public DungeonObject(DungeonType type, Integer time, List<Player> playerList, String worldName) {
            this.type = type;
            this.time = time;
            this.playerList = playerList;
            this.worldName = worldName;
        }

        public DungeonType getType() {
            return type;
        }

        public Integer getTime() {
            return time;
        }

        public List<Player> getPlayerList() {
            return playerList;
        }

        public String getWorldName() {
            return worldName;
        }

        public void modifyTime(int second) {
            time = second;
        }

        public void load() {
            try {
                unZip(new ZipFile(DungeonRealms.getInstance().getDataFolder() + type.getLocation()), this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Enum type of dungeons includes, zip locations & bossNames
     *
     * @since 1.0
     */
    enum DungeonType {
        BANDIT_TROVE("Mayel the Cruel", "/dungeons/banditTrove.zip"),
        VARENGLADE("Burick The Fanatic", "/dungeons/varenglade.zip"),
        THE_INFERNAL_ABYSS("The Infernal Abyss", "/dungeons/theInfernalAbyss.zip");

        /**
         * WIP Dungeons, including The Depths of Aceron and The Crimson Monastery.
         */

        private String bossName;
        private String location;

        DungeonType(String bossName, String location) {
            this.bossName = bossName;
            this.location = location;
        }

        public String getBossName() {
            return bossName;
        }

        public String getLocation() {
            return location;
        }
    }

}
