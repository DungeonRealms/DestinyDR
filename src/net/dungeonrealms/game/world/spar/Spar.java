package net.dungeonrealms.game.world.spar;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.world.spar.sparworlds.SparWorldCyren;
import org.apache.commons.io.IOUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Nick on 12/15/2015.
 */
public class Spar implements GenericMechanic {

    ArrayList<Battle> spars = new ArrayList<>();

    @Override
    public void startInitialization() {

        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            spars.stream().forEach(battle -> {
                battle.increaseTime();

                List<Player> all = new ArrayList<Player>();
                all.add(battle.getPlayer1());
                all.add(battle.getPlayer2());

                switch ((int) battle.getTime()) {
                    case 10:
                        all.stream().forEach(player -> player.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "SPAR" + " " + ChatColor.YELLOW.toString() + ChatColor.BOLD +
                                "➜" + " " + ChatColor.GRAY + "Spar has reached 10 seconds!"));
                        break;
                    case 20:
                        all.stream().forEach(player -> player.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "SPAR" + " " + ChatColor.YELLOW.toString() + ChatColor.BOLD +
                                "➜" + " " + ChatColor.GRAY + "Spar has reached 20 seconds!"));
                        break;
                    case 30:
                        all.stream().forEach(player -> player.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "SPAR" + " " + ChatColor.YELLOW.toString() + ChatColor.BOLD +
                                "➜" + " " + ChatColor.GRAY + "Spar has reached 30 seconds!"));
                        break;
                    case 180:
                        all.stream().forEach(player -> player.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "SPAR" + " " + ChatColor.YELLOW.toString() + ChatColor.BOLD +
                                "➜" + " " + ChatColor.GRAY + "Spar has reached maximum time and expired!"));
                        battle.getPlayer1().teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                        battle.getPlayer2().teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                        break;
                }

                all.stream().forEach(player -> {
                    BountifulAPI.sendActionBar(player, ChatColor.AQUA.toString() + ChatColor.BOLD + "SPAR" + " " + ChatColor.YELLOW.toString() + ChatColor.BOLD +
                            "➜" + " " + ChatColor.GRAY + battle.getPlayer1().getName() + ChatColor.RED + " VS " + ChatColor.GRAY + battle.getPlayer2().getName());
                });

            });
        }, 0, 20);

    }


    public void readyWorld(Battle battle) {

        try {
            unZip(new ZipFile(DungeonRealms.getInstance().getDataFolder() + SparWorlds.CYREN_BATTLE.getZipName()), battle.getWorldName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        World w = Bukkit.getServer().createWorld(new WorldCreator(battle.getWorldName()));
        w.setKeepSpawnInMemory(false);
        w.setAutoSave(false);
        w.setPVP(false);
        w.setStorm(false);
        w.setMonsterSpawnLimit(300);
        w.setGameRuleValue("doFireTick", "false");
        w.setGameRuleValue("randomTickSpeed", "0");
        Bukkit.getWorlds().add(w);

        SparWorldCyren s = new SparWorldCyren();

        spars.add(battle);

        battle.getPlayer1().teleport(new Location(w, s.getLocations()[0], s.getLocations()[1], s.getLocations()[2]));
        battle.getPlayer2().teleport(new Location(w, s.getLocations()[3], s.getLocations()[4], s.getLocations()[5]));

    }

    public void unZip(ZipFile zipFile, String worldName) {
        Utils.log.info("[SPAR] Unzipping instance for " + worldName);
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

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void stopInvocation() {

    }

    /*
    Stores the names of the zip files for each sparworld.
     */
    public enum SparWorlds {

        CYREN_BATTLE("/cyrenBattle.zip"),;

        private String zipName;

        SparWorlds(String zipName) {
            this.zipName = zipName;
        }

        public String getZipName() {
            return zipName;
        }
    }

}
