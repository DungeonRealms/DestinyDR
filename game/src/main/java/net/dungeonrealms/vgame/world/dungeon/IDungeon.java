package net.dungeonrealms.vgame.world.dungeon;

import net.dungeonrealms.old.game.mastery.Utils;
import net.dungeonrealms.old.game.party.Party;
import org.apache.commons.io.IOUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Copyright © 2016 Matthew E Development - All Rights Reserved
 * You may NOT use, distribute and modify this code.
 * <p>
 * Created by Matthew E on 11/1/2016 at 2:35 PM.
 */
public interface IDungeon {

    Party getParty();

    void startDungeon();

    void endDungeon(EnumDungeonEndReason dungeonEndReason);

    void teleportOut();

    void spawnBoss(Location location);

    EnumDungeon getDungeonEnum();

    String getName();

    void enterDungeon(Player player);

    default void msg(String msg) {
        getParty().getMembers().forEach(player -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg)));
    }

    World getDungeonWorld();

    void spawnInMobs();

    default void unZip(ZipFile zipFile, String worldName) {
        Utils.log.info("[DUNGEON] Unzipping instance for " + worldName);
        new File(worldName).mkdir();
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(worldName, entry.getName());
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
}
