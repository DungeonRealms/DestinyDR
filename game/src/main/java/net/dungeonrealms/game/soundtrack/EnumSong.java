package net.dungeonrealms.game.soundtrack;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/2/2016
 */

public enum EnumSong {

    CYRENNICA_1("soundtrack/cyren_1.nbs"),
    CYRENNICA_2("soundtrack/cyren_2.nbs"),
    WILDERNESS_1("soundtrack/wilderness_1.nbs"),
    CHAOTIC_1("soundtrack/chaotic_1.nbs"),
    HARRISONS_1("soundtrack/harrisons_1.nbs"),
    TEST("soundtrack/test.nbs");

    @Getter
    private String path;

    EnumSong(String path) {
        this.path = path;
    }

    public static EnumSong getByPath(String path) {
        Optional<EnumSong> query = Arrays.asList(EnumSong.values()).stream().
                filter(info -> info.getPath().equals(path)).findFirst();
        return query.isPresent() ? query.get() : null;
    }

}
