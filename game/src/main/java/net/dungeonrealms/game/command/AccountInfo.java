package net.dungeonrealms.game.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class AccountInfo {

    private UUID uuid;
    private int characterID;
}

