package net.dungeonrealms.game.guild.token;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/25/2016
 */

public class GuildInfoToken {

    @Getter
    @Setter
    private String owner;

    @Getter
    private List<String> officers = new CopyOnWriteArrayList<>();

    @Getter
    private List<String> member = new CopyOnWriteArrayList<>();

}
