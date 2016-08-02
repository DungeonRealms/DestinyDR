package net.dungeonrealms.game.soundtrack;

import lombok.NoArgsConstructor;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.util.ResourceExtractor;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.soundtrack.decipher.NBSDecoder;
import net.dungeonrealms.game.soundtrack.player.RadioSongPlayer;
import net.dungeonrealms.game.soundtrack.player.song.Song;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/2/2016
 */

@NoArgsConstructor
public class Soundtrack implements GenericMechanic, Listener {

    // INSTANCE //
    protected static Soundtrack instance = null;

    private final long LOOP_DELAY = 30000L;

    public HashMap<String, ArrayList<SongPlayer>> playingSongs = new HashMap<String, ArrayList<SongPlayer>>();
    public HashMap<String, Byte> playerVolume = new HashMap<String, Byte>();

    private Map<EnumSong, SongPlayer> PLAYERS = new HashMap<>();

    public SongPlayer getPlayer(EnumSong enumSong) {
        return PLAYERS.get(enumSong);
    }

    public boolean isReceivingSong(Player p) {
        return ((instance.playingSongs.get(p.getName()) != null) && (!instance.playingSongs.get(p.getName()).isEmpty()));
    }

    public static Soundtrack getInstance() {
        if (instance == null) {
            instance = new Soundtrack();
        }
        return instance;
    }


    public void doLogout(Player player) {

    }

    public void stopPlaying(Player p) {
        if (instance.playingSongs.get(p.getName()) == null) {
            return;
        }
        for (SongPlayer s : instance.playingSongs.get(p.getName())) {
            s.removePlayer(p);
        }
    }

    public static void setPlayerVolume(Player p, byte volume) {
        instance.playerVolume.put(p.getName(), volume);
    }

    public static byte getPlayerVolume(Player p) {
        Byte b = instance.playerVolume.get(p.getName());
        if (b == null) {
            b = 100;
            instance.playerVolume.put(p.getName(), b);
        }
        return b;
    }


    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
    public void startInitialization() {
        Utils.log.info("DungeonRealms Loading Soundtrack...");

        File SOUNDTRACK_FILE = new File(DungeonRealms.getInstance().getDataFolder(), "soundtrack");
        ResourceExtractor extractor = new ResourceExtractor(DungeonRealms.getInstance(), SOUNDTRACK_FILE, "soundtrack", null);

        try {
            // EXTRACT FOLDER //
            extractor.extract();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // LOAD ALL SONGS FROM FOLDER \\
        Arrays.stream(SOUNDTRACK_FILE.listFiles()).forEach(file -> {
            EnumSong enumSong = EnumSong.getByPath(file.getName());

            if (enumSong == null) {
                Utils.log.warning("Could not find song for " + file.getName());
                return;
            }

            Song song = NBSDecoder.parse(file);
            SongPlayer player = new RadioSongPlayer(song);

            PLAYERS.put(enumSong, player);
        });

        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        Utils.log.info("Successfully loaded " + PLAYERS.size() + " songs into the DungeonRealms soundtrack");
    }

    @Override
    public void stopInvocation() {

    }
}
