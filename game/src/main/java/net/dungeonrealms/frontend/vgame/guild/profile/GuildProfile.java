package net.dungeonrealms.frontend.vgame.guild.profile;

import lombok.Getter;
import net.dungeonrealms.frontend.Game;
import net.dungeonrealms.frontend.vgame.guild.Guild;
import net.dungeonrealms.frontend.vgame.guild.role.EnumGuildRole;
import net.dungeonrealms.frontend.vgame.player.GamePlayer;

import java.util.UUID;

/**
 * Created by Giovanni on 9-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GuildProfile {

    /**
     * Assigned to a gameplayer
     */

    @Getter
    private UUID owner;

    @Getter
    private EnumGuildRole guildRole;

    @Getter
    private EnumProfileStatus profileStatus;

    @Getter
    private Guild guild;

    public GuildProfile(UUID owner) {
        this.owner = owner;
        //== Build
        this.profileStatus = EnumProfileStatus.NO_GUILD;
    }

    /**
     * Flush the profile
     */
    public void flush() {
        this.guildRole = null;
        this.guild = null;
        this.profileStatus = EnumProfileStatus.NO_GUILD;
    }

    /**
     * Update the player's guild & role
     *
     * @param guild     The new guild
     * @param guildRole The role
     */
    public void updateTo(Guild guild, EnumGuildRole guildRole) {
        this.guild = guild;
        this.guildRole = guildRole;
        this.profileStatus = EnumProfileStatus.IN_GUILD;
    }

    /**
     * Update the player's role in a guild
     *
     * @param role The role
     */
    public void updateRole(EnumGuildRole role) {
        if (this.profileStatus == EnumProfileStatus.IN_GUILD)
            this.guildRole = role;
    }

    /**
     * Invite the profile to a guild
     *
     * @param guild The guild
     */
    public void handleInvite(Guild guild) {
        GamePlayer gamePlayer = Game.getGame().getRegistryRegistry().getPlayerRegistry().getPlayer(this.owner);
        if (!gamePlayer.getData().getGuildData().getGuildInvitations().contains(guild.getName())) {
            gamePlayer.getData().getGuildData().getGuildInvitations().add(guild.getName());
        }
    }

    /**
     * Kicks the profile from it's current guild
     */
    public void handleKick(String kicker) {
        if (this.guild != null && this.guildRole != EnumGuildRole.OWNER) {
            GamePlayer gamePlayer = Game.getGame().getRegistryRegistry().getPlayerRegistry().getPlayer(this.owner);
            this.guild.removeMember(gamePlayer);
            this.guild.notify(new String[]{kicker + " has kicked " + gamePlayer.getPlayer().getName() + " from the guild"});
            this.flush();
        }
    }
}
