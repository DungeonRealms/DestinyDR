package net.dungeonrealms.old.game.soundtrack.player;

import net.dungeonrealms.old.game.soundtrack.SongPlayer;
import net.dungeonrealms.old.game.soundtrack.Soundtrack;
import net.dungeonrealms.old.game.soundtrack.note.Instrument;
import net.dungeonrealms.old.game.soundtrack.note.Note;
import net.dungeonrealms.old.game.soundtrack.note.NotePitch;
import net.dungeonrealms.old.game.soundtrack.player.song.Layer;
import net.dungeonrealms.old.game.soundtrack.player.song.Song;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/2/2016
 */

public class RadioSongPlayer extends SongPlayer {

    public RadioSongPlayer(Song song) {
        super(song);
    }

    @Override
    public void playTick(Player p, int tick) {
        byte playerVolume = Soundtrack.getPlayerVolume(p);

        for (Layer l : song.getLayerHashMap().values()) {

            Note note = l.getNote(tick);
            if (note == null) continue;

            p.playSound(p.getEyeLocation(),
                    Instrument.getInstrument(note.getInstrument()),
                    (l.getVolume() * (int) volume * (int) playerVolume) / 1000000f,
                    NotePitch.getPitch(note.getKey() - 33));
        }
    }
}
