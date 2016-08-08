package net.dungeonrealms.game.soundtrack;

import lombok.NoArgsConstructor;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.event.PlayerEnterRegionEvent;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.soundtrack.decipher.NBSDecoder;
import net.dungeonrealms.game.soundtrack.player.RadioSongPlayer;
import net.dungeonrealms.game.soundtrack.player.song.Song;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
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

    protected static final long LOOP_DELAY = 100000L;
    protected static final long START_DELAY = 20000L;

    private static final byte DEFAULT_VOLUME = 30;

    protected HashMap<String, ArrayList<SongPlayer>> playingSongs = new HashMap<String, ArrayList<SongPlayer>>();
    protected HashMap<String, Byte> playerVolume = new HashMap<String, Byte>();

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

        if (isReceivingSong(player))
            stopPlaying(player);
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
            b = DEFAULT_VOLUME;
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

        // LOAD ALL SONGS FROM FOLDER \\
        Arrays.stream(EnumSong.values()).forEach(enumSong -> {
            File file = new File(DungeonRealms.getInstance().getDataFolder(), enumSong.getPath());

            if (file == null || !file.exists()) {
                Utils.log.warning("Could not find song for " + enumSong.getPath());
                return;
            }

            Song song = NBSDecoder.parse(file);
            SongPlayer player = new RadioSongPlayer(song);
            player.setPlaying(true);

            PLAYERS.put(enumSong, player);
        });

        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        Utils.log.info("Successfully loaded " + PLAYERS.size() + " songs into the DungeonRealms soundtrack");
    }

    @Override
    public void stopInvocation() {
        PLAYERS.values().stream().forEach(SongPlayer::destroy);
    }

    @EventHandler
    public void onRegion(PlayerEnterRegionEvent event) {
        Player player = event.getPlayer();

        if (!(boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_SOUNDTRACK, player.getUniqueId())) return;

        if (isReceivingSong(player))
            stopPlaying(player);

        String region = event.getRegion();

        if (region.contains("cyren") && !region.contains("plains")) {
            getPlayer(EnumSong.CYRENNICA_2).addPlayer(player);
        } else if (region.equals("villagesafe")) {
            getPlayer(EnumSong.HARRISONS_1).addPlayer(player);
        } else if (!GameAPI.isInSafeRegion(player.getLocation()) && GameAPI.isNonPvPRegion(player.getLocation())
                && !KarmaHandler.PLAYER_LOCATIONS.get(player).equals(KarmaHandler.EnumPlayerAlignments.NEUTRAL)) {
            getPlayer(EnumSong.WILDERNESS_1).addPlayer(player);
        } else if (!GameAPI.isInSafeRegion(player.getLocation()) && !GameAPI.isNonPvPRegion(player.getLocation())
                && !KarmaHandler.PLAYER_LOCATIONS.get(player).equals(KarmaHandler.EnumPlayerAlignments.CHAOTIC)) {
            getPlayer(EnumSong.CHAOTIC_1).addPlayer(player);
        }
    }
}