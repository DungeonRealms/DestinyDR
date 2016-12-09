package net.dungeonrealms.frontend.vgame.player;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.api.creature.lib.damage.EnumDamageSource;
import net.dungeonrealms.common.awt.data.DataPlayer;
import net.dungeonrealms.frontend.Game;
import net.dungeonrealms.frontend.vgame.guild.Guild;
import net.dungeonrealms.frontend.vgame.guild.profile.GuildProfile;
import net.dungeonrealms.frontend.vgame.guild.role.EnumGuildRole;
import net.dungeonrealms.frontend.vgame.player.goal.achievement.EnumAchievement;
import net.dungeonrealms.frontend.vgame.player.goal.objective.Objective;
import net.dungeonrealms.frontend.vgame.player.scoreboard.EnumScoreboardType;
import net.dungeonrealms.frontend.vgame.player.scoreboard.GameScoreboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 8-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GamePlayer implements IPlayer {

    // Containers **
    @Getter
    private DataPlayer data;

    @Getter
    private GuildProfile guildProfile;

    // Transient game data
    @Getter
    private Player player;

    @Setter
    @Getter
    private Objective currentObjective;

    @Getter
    @Setter
    private GameScoreboard gameScoreboard;

    @Getter
    private EnumDamageSource damageSource = EnumDamageSource.PLAYER;

    @Setter
    @Getter
    private boolean inParty;

    @Getter
    @Setter
    private boolean teleporting;

    @Getter
    @Setter
    private boolean combat;

    @Getter
    @Setter
    private Entity lastDamaged;

    @Getter
    @Setter
    private Entity lastDamageCause;

    @Getter
    private PlayerInventory inventory; // TODO

    public GamePlayer(DataPlayer dataPlayer) {
        this.data = dataPlayer;
        this.player = dataPlayer.getPlayer();

        this.guildProfile = new GuildProfile(this.getData().getUniqueId());
        // Update guild profile
        Guild guild = Game.getGame().getRegistryRegistry().getGuildRegistry().getByName(this.getData().getGuildData().getGuild());
        if (guild != null) {
            this.guildProfile.updateTo(guild, EnumGuildRole.MEMBER);
        } // Player has no guild, keep the profile empty
    }

    public boolean hasAchievement(EnumAchievement achievement) {
        return this.data.getCollectionData().getAchievements().contains(achievement.name());
    }

    public void updateScoreboard(EnumScoreboardType to) {
        this.gameScoreboard = new GameScoreboard(this, to);
        this.gameScoreboard.getScoreboardHolder().send(this.player);
    }

    public void createGuild(String name) {
        Game.getGame().getRegistryRegistry().getGuildRegistry().createNew(name, this);
    }
}
