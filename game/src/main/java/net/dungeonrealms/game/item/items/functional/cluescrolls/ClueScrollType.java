package net.dungeonrealms.game.item.items.functional.cluescrolls;

import java.sql.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rar349 on 6/13/2017.
 */
public enum ClueScrollType {

    FISHING(Arrays.asList(Clue.PUFFER_FISH));

    private List<Clue> clues;
    ClueScrollType(List<Clue> clues) {
        this.clues = clues;
    }
}
