package net.dungeonrealms.game.mechanic.rifts;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BeaconColor {
    RED(14),
    ORANGE(1),
    YELLOW(4),
    LIME(5),
    GREEN(13),
    LIGHT_BLUE(3),
    CYAN(9),
    BLUE(11),
    PURPLE(10),
    MAGENTA(2),
    PINK(6);

    private int data;
}
