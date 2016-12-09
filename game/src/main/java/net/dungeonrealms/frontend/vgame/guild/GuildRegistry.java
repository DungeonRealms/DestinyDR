package net.dungeonrealms.frontend.vgame.guild;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.registry.Registry;
import net.dungeonrealms.frontend.vgame.guild.exception.GuildExistsException;
import net.dungeonrealms.frontend.vgame.guild.exception.InvalidNameException;
import net.dungeonrealms.frontend.vgame.player.GamePlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 9-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GuildRegistry implements Registry {

    @Getter
    private UUID uniqueId;

    @Getter
    private boolean connected;

    @Getter
    private ConcurrentHashMap<String, Guild> guildCache;

    @Override
    public void prepare() {
        this.uniqueId = UUID.randomUUID();
        this.guildCache = new ConcurrentHashMap<>();
        this.connected = true;
    }

    @Override
    public void disable() {
        this.uniqueId = null;
        this.connected = false;
    }

    @Override
    public boolean ignoreEnabled() {
        return false;
    }

    public Guild getByName(String name) {
        return this.guildCache.get(name);
    }

    /**
     * Creates a new guild
     *
     * @param name       The name of the guild
     * @param gamePlayer The owner of the guild
     */
    public void createNew(String name, GamePlayer gamePlayer) {
        Guild guild = null;
        try {
            guild = new Guild(name, gamePlayer.getData().getUniqueId());
        } catch (GuildExistsException | InvalidNameException e) {

        }
        if (guild != null) {
            this.guildCache.put(guild.getName(), guild);
        }
    }
}
