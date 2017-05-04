package net.dungeonrealms.common.game.database.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
public class UUIDName {
    @Getter
    private UUID uuid;
    @Getter
    @Setter
    private String name;
}
