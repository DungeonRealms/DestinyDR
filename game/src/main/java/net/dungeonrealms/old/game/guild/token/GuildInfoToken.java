package net.dungeonrealms.old.game.guild.token;

import lombok.Data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/25/2016
 */
@Data
public class GuildInfoToken {

    private String owner;

    private List<String> officers = new CopyOnWriteArrayList<>();

    private List<String> member = new CopyOnWriteArrayList<>();

}
