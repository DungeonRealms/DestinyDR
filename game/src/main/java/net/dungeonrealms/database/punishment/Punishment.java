package net.dungeonrealms.database.punishment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Punishment {

    @Getter
    private int punisherID;
    @Getter
    private long issued, expiration;
    @Getter
    private String reason;
    @Getter
    private boolean quashed = false;

}
