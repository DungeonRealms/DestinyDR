package io.vawke.skelframe;

import io.vawke.skelframe.bootstrap.IOBootstrap;
import lombok.Getter;
import net.dungeonrealms.vgame.Game;

import java.io.IOException;

/**
 * Created by Giovanni on 10-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class SkelRuntime {
    @Getter
    private static SkelRuntime skelRuntime;

    public void init() {
        Game.getGame().getInstanceLogger().sendMessage("[ Starting (Skeleton Runtime) by Vawke.. ]");
        skelRuntime = this;
    }

    public int processors() {
        return Runtime.getRuntime().availableProcessors();
    }

    public long memory() {
        return Runtime.getRuntime().totalMemory();
    }

    public void executeCommand(String par1) throws IOException {
        Runtime.getRuntime().exec(par1);
    }

    public IOBootstrap bootstrap() {
        return new IOBootstrap();
    }
}
