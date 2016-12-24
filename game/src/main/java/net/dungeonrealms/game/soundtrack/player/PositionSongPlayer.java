package net.dungeonrealms.game.soundtrack.player;

import net.dungeonrealms.game.soundtrack.SongPlayer;
import net.dungeonrealms.game.soundtrack.Soundtrack;
import net.dungeonrealms.game.soundtrack.note.Instrument;
import net.dungeonrealms.game.soundtrack.note.Note;
import net.dungeonrealms.game.soundtrack.note.NotePitch;
import net.dungeonrealms.game.soundtrack.player.song.Layer;
import net.dungeonrealms.game.soundtrack.player.song.Song;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/2/2016
 */

public class PositionSongPlayer extends SongPlayer {

    private Location targetLocation;

    public PositionSongPlayer(Song song) {
        super(song);
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
    }

    @Override
    public void playTick(Player p, int tick) {
        if (!p.getWorld().getName().equals(targetLocation.getWorld().getName())) {
            // not in same world
            return;
        }
        byte playerVolume = Soundtrack.getPlayerVolume(p);

        for (Layer l : song.getLayerHashMap().values()) {
            Note note = l.getNote(tick);
            if (note == null) {
                continue;
            }
            p.playSound(targetLocation,
                    Instrument.getInstrument(note.getInstrument()),
                    (l.getVolume() * (int) volume * (int) playerVolume) / 1000000f,
                    NotePitch.getPitch(note.getKey() - 33));
        }
    }
}
